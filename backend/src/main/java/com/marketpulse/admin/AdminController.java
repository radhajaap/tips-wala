package com.marketpulse.admin;
import com.marketpulse.account.*; import com.marketpulse.billing.*; import com.marketpulse.content.*; import com.marketpulse.notify.EmailService; import com.marketpulse.user.*;
import jakarta.validation.Valid; import jakarta.validation.constraints.NotBlank; import java.time.*; import java.time.temporal.ChronoUnit; import java.util.*;
import org.springframework.data.domain.*; import org.springframework.http.ResponseEntity; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin")
public class AdminController {
  static final String ANY = "hasAnyRole('SUPER_ADMIN','CONTENT_ADMIN','SUPPORT_ADMIN')", SUPER = "hasRole('SUPER_ADMIN')",
    SUPPORT = "hasAnyRole('SUPER_ADMIN','SUPPORT_ADMIN')", CONTENT = "hasAnyRole('SUPER_ADMIN','CONTENT_ADMIN')";
  static final Set<String> ROLES = Set.of("USER","SUPER_ADMIN","CONTENT_ADMIN","SUPPORT_ADMIN");
  record RefundAction(@NotBlank String action, String note, String reason, String txnId) {}
  private final UserRepository users; private final SubscriptionRepository subs; private final PaymentOrderRepository orders; private final RefundRepository refunds;
  private final PostRepository posts; private final ReviewRepository reviews; private final InquiryRepository inquiries; private final AuditLogRepository auditRepo; private final AuditService audit; private final EmailService mail;
  AdminController(UserRepository u, SubscriptionRepository s, PaymentOrderRepository o, RefundRepository r, PostRepository p, ReviewRepository rv, InquiryRepository i, AuditLogRepository ar, AuditService au, EmailService m){
    users=u; subs=s; orders=o; refunds=r; posts=p; reviews=rv; inquiries=i; auditRepo=ar; audit=au; mail=m; }
  private static ResponseEntity<?> bad(String m){ return ResponseEntity.badRequest().body(Map.of("error", m)); }
  private static boolean blank(String s){ return s == null || s.isBlank(); }

  @GetMapping("/stats") @PreAuthorize(ANY) Map<String,Object> stats(Authentication a){
    Instant now = Instant.now(); ZoneId z = ZoneId.of("Asia/Kolkata"); Instant month = LocalDate.now(z).withDayOfMonth(1).atStartOfDay(z).toInstant();
    var m = new LinkedHashMap<String,Object>();
    m.put("totalUsers", users.count()); m.put("newUsers7d", users.countByCreatedAtAfter(now.minus(7, ChronoUnit.DAYS)));
    m.put("activeMembers", subs.countByKindAndEndAtAfter("PAID", now)); m.put("activeDemos", subs.countByKindAndEndAtAfter("TRIAL", now)); m.put("expiredSubscriptions", subs.countByEndAtBefore(now));
    m.put("pendingRefunds", refunds.countByStatus("REQUESTED")); m.put("pendingInquiries", inquiries.countByStatus("NEW"));
    m.put("totalPosts", posts.count()); m.put("totalReviews", reviews.count()); m.put("pendingReviews", reviews.findByStatusOrderByCreatedAtDesc("PENDING").size());
    if (a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_SUPER_ADMIN"))) { m.put("totalRevenuePaise", orders.totalPaid()); m.put("monthRevenuePaise", orders.paidSince(month)); }
    return m; }
  @GetMapping("/users") @PreAuthorize(SUPPORT) List<Map<String,Object>> userList(@RequestParam(defaultValue="0") int page){
    return users.findAll(PageRequest.of(page, 50, Sort.by(Sort.Direction.DESC, "createdAt"))).stream().map(u -> Map.<String,Object>of("id", u.id, "fullName", u.fullName, "email", u.email, "mobile", u.mobile, "role", u.role, "trialUsed", u.trialUsed, "deleteRequested", u.deleteRequested, "createdAt", u.createdAt.toString())).toList(); }
  @PutMapping("/users/{id}/role") @PreAuthorize(SUPER) ResponseEntity<?> role(@PathVariable UUID id, @RequestParam String value, Authentication a){
    if (!ROLES.contains(value)) return bad("Unknown role"); if (id.toString().equals(a.getName())) return bad("You cannot change your own role");
    return users.findById(id).<ResponseEntity<?>>map(u -> { u.role = value; users.save(u); audit.log(a, "ROLE_CHANGE", id + " -> " + value); return ResponseEntity.ok(Map.of("ok", true)); }).orElse(ResponseEntity.notFound().build()); }
  @GetMapping("/payments") @PreAuthorize(SUPER) List<PaymentOrder> payments(){ return orders.findAll(Sort.by(Sort.Direction.DESC, "createdAt")); }
  @GetMapping("/refunds") @PreAuthorize(SUPPORT) List<Refund> refundList(){ return refunds.findAllByOrderByCreatedAtDesc(); }
  @PutMapping("/refunds/{id}") @PreAuthorize(SUPER) ResponseEntity<?> refundAct(@PathVariable UUID id, @Valid @RequestBody RefundAction b, Authentication a){
    var r = refunds.findById(id).orElse(null); if (r == null) return ResponseEntity.notFound().build();
    switch (b.action()) {
      case "APPROVE": if (!"REQUESTED".equals(r.status)) return bad("Only requested refunds can be approved"); r.status = "APPROVED"; break;
      case "REJECT": if (!"REQUESTED".equals(r.status) || blank(b.reason())) return bad("Rejection needs a reason"); r.status = "REJECTED"; r.rejectReason = b.reason(); break;
      case "REFUNDED": if (!"APPROVED".equals(r.status) || blank(b.txnId())) return bad("Refund must be approved and needs the refund transaction ID");
        r.status = "REFUNDED"; r.refundTxnId = b.txnId().trim();
        subs.findFirstByUserIdOrderByEndAtDesc(r.userId).filter(s -> "PAID".equals(s.kind)).ifPresent(s -> { s.endAt = Instant.now(); subs.save(s); }); break;
      default: return bad("Unknown action"); }
    if (b.note() != null) r.adminNote = b.note();
    refunds.save(r); audit.log(a, "REFUND_" + b.action(), r.id.toString());
    users.findById(r.userId).ifPresent(u -> mail.send(u.email, "Tips Wala refund update", "Your refund request status: " + r.status + ("REJECTED".equals(r.status) ? "\nReason: " + r.rejectReason : "") + ("REFUNDED".equals(r.status) ? "\nRefund transaction ID: " + r.refundTxnId : "")));
    return ResponseEntity.ok(r); }
  @GetMapping("/reviews") @PreAuthorize(CONTENT) List<Review> reviewList(@RequestParam(defaultValue="PENDING") String status){ return reviews.findByStatusOrderByCreatedAtDesc(status); }
  @GetMapping("/posts") @PreAuthorize(CONTENT) List<Map<String,Object>> postList(){
    return posts.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream().map(p -> Map.<String,Object>of("id", p.id, "title", p.title, "category", p.category, "published", p.published, "premium", p.premium, "views", p.views)).toList(); }
  @PutMapping("/inquiries/{id}") @PreAuthorize(SUPPORT) ResponseEntity<?> inquiryStatus(@PathVariable UUID id, @RequestParam String status){
    if (!Set.of("NEW","REPLIED","CLOSED").contains(status)) return bad("Unknown status");
    return inquiries.findById(id).<ResponseEntity<?>>map(i -> { i.status = status; inquiries.save(i); return ResponseEntity.ok(Map.of("ok", true)); }).orElse(ResponseEntity.notFound().build()); }
  @GetMapping("/audit") @PreAuthorize(SUPER) List<AuditLog> auditList(){ return auditRepo.findAll(PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent(); }
}
