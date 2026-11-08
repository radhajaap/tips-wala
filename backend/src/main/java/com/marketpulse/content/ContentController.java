package com.marketpulse.content;
import com.marketpulse.billing.SubscriptionRepository; import com.marketpulse.user.UserRepository;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.time.Instant; import java.util.*;
import org.springframework.beans.factory.annotation.Value; import org.springframework.data.domain.Sort; import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api")
public class ContentController {
  static final String CONTENT = "hasAnyRole('SUPER_ADMIN','CONTENT_ADMIN')", SUPPORT = "hasAnyRole('SUPER_ADMIN','SUPPORT_ADMIN')";
  static final Set<String> OK = Set.of("DAILY_OVERVIEW","NIFTY_ANALYSIS","BANKNIFTY_ANALYSIS","GOLD","SILVER","CRYPTO","EDUCATION","ECONOMIC_NEWS","TECHNICAL_ANALYSIS");
  /** Needs SEBI registration / legal authorization. Blocked unless RESTRICTED_CONTENT_ENABLED=true. */
  static final Set<String> RESTRICTED = Set.of("STOCK_RECOMMENDATION","TARGET_PRICE","STOP_LOSS","PORTFOLIO_ADVICE");
  record FaqReq(@NotBlank String question, @NotBlank String answer, Integer sortOrder) {}
  record PostReq(@NotBlank @Size(max=200) String title, @NotBlank String category, @NotBlank String body, boolean premium, boolean published, String seoTitle, String seoDescription, String featuredImageUrl, String videoUrl) {}
  record ReviewReq(@Min(1) @Max(5) int rating, @NotBlank @Size(max=1500) String body) {}
  record ContactReq(@NotBlank @Size(max=120) String name, @Email @NotBlank String email, @Pattern(regexp="^$|^[6-9]\\d{9}$") String mobile,
                    @NotBlank @Size(max=200) String subject, @NotBlank @Size(max=3000) String message, String website) {}
  private final FaqRepository faqs; private final PostRepository posts; private final ReviewRepository reviews; private final InquiryRepository inquiries;
  private final UserRepository users; private final SubscriptionRepository subs; @Value("${app.compliance.restricted-categories-enabled}") boolean restrictedOn;
  private final com.marketpulse.notify.EmailService mail; @Value("${app.support-email}") String supportEmail;
  ContentController(FaqRepository f, PostRepository p, ReviewRepository r, InquiryRepository i, UserRepository u, SubscriptionRepository s, com.marketpulse.notify.EmailService m){ faqs=f; posts=p; reviews=r; inquiries=i; users=u; subs=s; mail=m; }
  private static boolean blank(String s){ return s == null || s.isBlank(); }
  private static ResponseEntity<?> bad(String m){ return ResponseEntity.badRequest().body(Map.of("error", m)); }

  private static Map<String,Object> card(Post p){ var m = new LinkedHashMap<String,Object>(); m.put("slug",p.slug); m.put("title",p.title); m.put("category",p.category); m.put("premium",p.premium);
    m.put("publishedAt",p.publishedAt.toString()); m.put("featuredImageUrl",p.featuredImageUrl); m.put("likes",p.likes); return m; }
  static boolean okUrl(String u, boolean video){
    if (u == null || u.isBlank()) return true;
    if (u.startsWith("/media/")) return u.length() < 120 && !u.contains("..");
    return video ? u.matches("^https://(www\\.youtube\\.com/(watch\\?v=|embed/)|youtu\\.be/)[A-Za-z0-9_\\-]{11}.*") : u.startsWith("https://"); }
  // FAQ
  @GetMapping("/faqs") List<Faq> faqList(){ return faqs.findAllByOrderBySortOrderAsc(); }
  @PostMapping("/admin/faqs") @PreAuthorize(CONTENT) Faq faqAdd(@Valid @RequestBody FaqReq r){ return faqs.save(fill(new Faq(), r)); }
  @PutMapping("/admin/faqs/{id}") @PreAuthorize(CONTENT) ResponseEntity<?> faqEdit(@PathVariable UUID id, @Valid @RequestBody FaqReq r){
    return faqs.findById(id).<ResponseEntity<?>>map(f -> ResponseEntity.ok(faqs.save(fill(f, r)))).orElse(ResponseEntity.notFound().build()); }
  @DeleteMapping("/admin/faqs/{id}") @PreAuthorize(CONTENT) void faqDel(@PathVariable UUID id){ faqs.deleteById(id); }
  private static Faq fill(Faq f, FaqReq r){ f.question=r.question(); f.answer=r.answer(); f.sortOrder = r.sortOrder()==null ? 0 : r.sortOrder(); return f; }

  // Posts (premium bodies need an active subscription or demo)
  @GetMapping("/posts") List<Map<String,Object>> postList(){
    return posts.findByPublishedTrueOrderByPublishedAtDesc().stream().map(p -> card(p)).toList(); }
  @GetMapping("/posts/{slug}") ResponseEntity<?> post(@PathVariable String slug, Authentication a){
    var p = posts.findBySlugAndPublishedTrue(slug).orElse(null);
    if (p == null) return ResponseEntity.notFound().build();
    if (p.premium && !(a != null && subs.existsByUserIdAndEndAtAfter(UUID.fromString(a.getName()), Instant.now())))
      return ResponseEntity.status(403).body(Map.of("error","Premium content: active membership or free demo required"));
    posts.bumpViews(p.id); p.views++; return ResponseEntity.ok(p); }
  @PostMapping("/admin/posts") @PreAuthorize(CONTENT) ResponseEntity<?> postAdd(@Valid @RequestBody PostReq r, Authentication a){
    String c = r.category().toUpperCase();
    if (!okUrl(r.featuredImageUrl(), false) || !okUrl(r.videoUrl(), true)) return bad("Image must be /media/… or https; video must be a YouTube link or /media/…");
    if (RESTRICTED.contains(c) && !restrictedOn) return bad("Category disabled until SEBI registration / legal authorization is confirmed");
    if (!OK.contains(c) && !RESTRICTED.contains(c)) return bad("Unknown category");
    var u = users.findById(UUID.fromString(a.getName())).orElseThrow();
    var p = new Post(); p.title=r.title().trim(); p.category=c; p.body=r.body(); p.premium=r.premium(); p.published=r.published(); p.author=u.fullName; p.seoTitle=r.seoTitle(); p.seoDescription=r.seoDescription();
    p.featuredImageUrl = blank(r.featuredImageUrl()) ? null : r.featuredImageUrl(); p.videoUrl = blank(r.videoUrl()) ? null : r.videoUrl();
    p.slug = r.title().toLowerCase().replaceAll("[^a-z0-9]+","-").replaceAll("^-|-$","") + "-" + Long.toString(System.currentTimeMillis(), 36);
    if (p.published) p.publishedAt = Instant.now();
    return ResponseEntity.ok(posts.save(p)); }
  @DeleteMapping("/admin/posts/{id}") @PreAuthorize(CONTENT) void postDel(@PathVariable UUID id){ posts.deleteById(id); }

  // Reviews (moderated; no fabricated content)
  @GetMapping("/reviews") Map<String,Object> reviewList(){
    var l = reviews.findByStatusOrderByCreatedAtDesc("APPROVED"); long n = l.size();
    double avg = l.stream().mapToInt(r -> r.rating).average().orElse(0); long five = l.stream().filter(r -> r.rating == 5).count();
    return Map.<String,Object>of("count", n, "average", Math.round(avg * 10) / 10.0, "fiveStarPct", n == 0 ? 0 : Math.round(five * 100.0 / n),
      "items", l.stream().map(r -> Map.<String,Object>of("name", r.authorName, "rating", r.rating, "body", r.body, "verified", r.verified, "createdAt", r.createdAt.toString())).toList()); }
  @PostMapping("/reviews") ResponseEntity<?> reviewSave(@Valid @RequestBody ReviewReq r, Authentication a){
    UUID id = UUID.fromString(a.getName()); var u = users.findById(id).orElseThrow();
    var rv = reviews.findByUserId(id).orElseGet(Review::new);
    rv.userId=id; rv.authorName=u.fullName.trim().split(" ")[0]; rv.rating=r.rating(); rv.body=r.body().trim(); rv.status="PENDING"; rv.verified=subs.existsByUserIdAndKind(id,"PAID");
    reviews.save(rv); return ResponseEntity.ok(Map.of("status","PENDING")); }
  @DeleteMapping("/reviews/mine") ResponseEntity<?> reviewDel(Authentication a){ reviews.findByUserId(UUID.fromString(a.getName())).ifPresent(reviews::delete); return ResponseEntity.noContent().build(); }
  @PutMapping("/admin/reviews/{id}/status") @PreAuthorize(CONTENT) ResponseEntity<?> reviewMod(@PathVariable UUID id, @RequestParam String value){
    if (!Set.of("APPROVED","HIDDEN").contains(value)) return bad("value must be APPROVED or HIDDEN");
    return reviews.findById(id).<ResponseEntity<?>>map(x -> { x.status = value; return ResponseEntity.ok(reviews.save(x)); }).orElse(ResponseEntity.notFound().build()); }

  // Contact
  @PostMapping("/contact") ResponseEntity<?> contact(@Valid @RequestBody ContactReq r){
    if (r.website() != null && !r.website().isBlank()) return ResponseEntity.ok(Map.of("ok", true)); // honeypot: bots fill it
    var i = new Inquiry(); i.name=r.name().trim(); i.email=r.email(); i.mobile=r.mobile(); i.subject=r.subject().trim(); i.message=r.message().trim();
    inquiries.save(i);
    mail.send(supportEmail, "New Tips Wala inquiry: " + i.subject, "From: " + i.name + " <" + i.email + "> " + (i.mobile == null ? "" : i.mobile) + "\n\n" + i.message);
    return ResponseEntity.ok(Map.of("ok", true)); }
  @GetMapping("/admin/inquiries") @PreAuthorize(SUPPORT) List<Inquiry> inquiryList(){ return inquiries.findAll(Sort.by(Sort.Direction.DESC, "createdAt")); }
}
