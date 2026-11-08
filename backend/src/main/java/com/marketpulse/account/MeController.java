package com.marketpulse.account;
import com.marketpulse.billing.*; import com.marketpulse.notify.OtpService; import com.marketpulse.user.*;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.time.*; import java.util.*;
import org.springframework.beans.factory.annotation.Value; import org.springframework.http.*; import org.springframework.security.core.Authentication; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api")
public class MeController {
  record PrefsReq(boolean notifyPush, boolean notifyEmail) {}
  record PwReq(@NotBlank String current, @Size(min=8,max=72) String newPassword) {}
  record RefundReq(@NotNull UUID orderId, @NotBlank @Size(max=1000) String reason) {}
  record OtpReq(@NotBlank String code) {}
  private final UserRepository users; private final SubscriptionRepository subs; private final PaymentOrderRepository orders; private final RefundRepository refunds;
  private final PasswordEncoder enc; private final OtpService otp; private final int windowDays;
  MeController(UserRepository u, SubscriptionRepository s, PaymentOrderRepository o, RefundRepository r, PasswordEncoder e, OtpService ot, @Value("${app.refund.window-days}") int w){ users=u; subs=s; orders=o; refunds=r; enc=e; otp=ot; windowDays=w; }
  private static UUID id(Authentication a){ return UUID.fromString(a.getName()); }

  @GetMapping("/me") Map<String,Object> me(Authentication a){
    UUID id = id(a); var u = users.findById(id).orElseThrow(); var m = new LinkedHashMap<String,Object>();
    m.put("fullName", u.fullName); m.put("email", u.email); m.put("mobile", u.mobile); m.put("role", u.role); m.put("emailVerified", u.emailVerified); m.put("mobileVerified", u.mobileVerified);
    m.put("notifyPush", u.notifyPush); m.put("notifyEmail", u.notifyEmail); m.put("trialUsed", u.trialUsed); m.put("deleteRequested", u.deleteRequested); m.put("refundWindowDays", windowDays);
    var sub = subs.findFirstByUserIdOrderByEndAtDesc(id).orElse(null);
    m.put("subscription", sub == null ? null : Map.of("plan", sub.plan, "kind", sub.kind, "endsAt", sub.endAt.toString(), "active", sub.endAt.isAfter(Instant.now())));
    m.put("payments", orders.findByUserIdOrderByCreatedAtDesc(id).stream().map(o -> Map.<String,Object>of("id", o.id, "plan", o.plan, "amountPaise", o.amountPaise, "status", o.status, "createdAt", o.createdAt.toString())).toList());
    m.put("refunds", refunds.findByUserIdOrderByCreatedAtDesc(id).stream().map(r -> { var x = new LinkedHashMap<String,Object>(); x.put("orderId", r.orderId); x.put("status", r.status); x.put("rejectReason", r.rejectReason); x.put("createdAt", r.createdAt.toString()); return x; }).toList());
    return m; }
  @PutMapping("/me/prefs") ResponseEntity<?> prefs(@RequestBody PrefsReq r, Authentication a){ var u = users.findById(id(a)).orElseThrow(); u.notifyPush = r.notifyPush(); u.notifyEmail = r.notifyEmail(); users.save(u); return ResponseEntity.ok(Map.of("ok", true)); }
  @PutMapping("/me/password") ResponseEntity<?> password(@Valid @RequestBody PwReq r, Authentication a){
    var u = users.findById(id(a)).orElseThrow();
    if (!enc.matches(r.current(), u.passwordHash)) return ResponseEntity.badRequest().body(Map.of("error","Current password is wrong"));
    u.passwordHash = enc.encode(r.newPassword()); users.save(u); return ResponseEntity.ok(Map.of("ok", true)); }
  @PostMapping("/me/delete-request") ResponseEntity<?> del(Authentication a){ var u = users.findById(id(a)).orElseThrow(); u.deleteRequested = true; users.save(u); return ResponseEntity.ok(Map.of("ok", true)); }

  @PostMapping("/me/mobile-otp") ResponseEntity<?> sendOtp(Authentication a){
    var u = users.findById(id(a)).orElseThrow();
    try { otp.send(u.id, u.mobile); return ResponseEntity.ok(Map.of("ok", true)); }
    catch (IllegalStateException e) { return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error","Mobile verification is not available yet")); } }
  @PostMapping("/me/mobile-verify") ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpReq r, Authentication a){
    if (!otp.verify(id(a), r.code())) return ResponseEntity.badRequest().body(Map.of("error","Wrong or expired code"));
    var u = users.findById(id(a)).orElseThrow(); u.mobileVerified = true; users.save(u); return ResponseEntity.ok(Map.of("ok", true)); }

  @GetMapping(value = "/me/receipts/{orderId}", produces = "text/html") ResponseEntity<String> receipt(@PathVariable UUID orderId, Authentication a){
    var u = users.findById(id(a)).orElseThrow(); var o = orders.findById(orderId).filter(x -> x.userId.equals(u.id) && "PAID".equals(x.status)).orElse(null);
    if (o == null) return ResponseEntity.notFound().build();
    String h = "<!DOCTYPE html><meta charset=utf-8><title>Receipt</title><body style='font-family:sans-serif;max-width:520px;margin:30px auto'><h2>Tips Wala – Payment Receipt</h2>"
      + "<p>Receipt for: " + esc(u.fullName) + " (" + esc(u.email) + ")</p><p>Plan: " + o.plan + "<br>Amount: Rs " + String.format("%.2f", o.amountPaise / 100.0)
      + "<br>Order ID: " + esc(o.razorpayOrderId) + "<br>Payment ID: " + esc(String.valueOf(o.razorpayPaymentId)) + "<br>Date: " + o.createdAt + "</p><p>Not a tax invoice unless GST details are added.</p></body>";
    return ResponseEntity.ok(h); }
  private static String esc(String s){ return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;"); }

  /** Refund concerns the service fee only; never tied to trading outcomes. Window is configurable (REFUND_WINDOW_DAYS) and must match the published policy. */
  @PostMapping("/refunds") ResponseEntity<?> refund(@Valid @RequestBody RefundReq r, Authentication a){
    UUID uid = id(a); var o = orders.findById(r.orderId()).filter(x -> x.userId.equals(uid) && "PAID".equals(x.status)).orElse(null);
    if (o == null) return ResponseEntity.badRequest().body(Map.of("error","Paid order not found"));
    if (refunds.findByOrderId(o.id).isPresent()) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","Refund already requested for this order"));
    if (o.createdAt.plus(Duration.ofDays(windowDays)).isBefore(Instant.now())) return ResponseEntity.badRequest().body(Map.of("error","Refund request period (" + windowDays + " days) has passed"));
    var f = new Refund(); f.orderId = o.id; f.userId = uid; f.reason = r.reason().trim(); refunds.save(f); return ResponseEntity.ok(Map.of("status", "REQUESTED")); }
}
