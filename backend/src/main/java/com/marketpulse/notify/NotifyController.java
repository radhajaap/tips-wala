package com.marketpulse.notify;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.time.Instant; import java.util.*;
import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api")
public class NotifyController {
  static final String CONTENT = "hasAnyRole('SUPER_ADMIN','CONTENT_ADMIN')";
  record DeviceReq(@NotBlank @Size(max=400) String token) {}
  record NoticeReq(@NotBlank @Size(max=120) String title, @NotBlank @Size(max=500) String message, String imageUrl, String deepLink, String audience, Instant scheduledAt) {}
  private final DeviceTokenRepository tokens; private final PushNoticeRepository notices; private final NotifyService svc; private final PushService push;
  NotifyController(DeviceTokenRepository t, PushNoticeRepository n, NotifyService s, PushService p){ tokens=t; notices=n; svc=s; push=p; }
  @PostMapping("/devices") ResponseEntity<?> register(@Valid @RequestBody DeviceReq r, Authentication a){
    var d = tokens.findByToken(r.token()).orElseGet(DeviceToken::new); d.userId = UUID.fromString(a.getName()); d.token = r.token(); tokens.save(d); return ResponseEntity.ok(Map.of("ok", true)); }
  @PostMapping("/admin/notifications") @PreAuthorize(CONTENT) ResponseEntity<?> create(@Valid @RequestBody NoticeReq r){
    String aud = r.audience() == null ? "ALL" : r.audience().toUpperCase();
    if (!aud.equals("ALL") && !aud.equals("MEMBERS")) return ResponseEntity.badRequest().body(Map.of("error","audience must be ALL or MEMBERS"));
    if (r.imageUrl() != null && !r.imageUrl().isBlank() && !r.imageUrl().startsWith("https://")) return ResponseEntity.badRequest().body(Map.of("error","imageUrl must be https"));
    if (r.deepLink() != null && !r.deepLink().isBlank() && !r.deepLink().startsWith("/")) return ResponseEntity.badRequest().body(Map.of("error","deepLink must be an in-app path like /post.html?slug=..."));
    var n = new PushNotice(); n.title=r.title().trim(); n.message=r.message().trim(); n.imageUrl=r.imageUrl(); n.deepLink=r.deepLink(); n.audience=aud; n.scheduledAt=r.scheduledAt();
    if (r.scheduledAt() != null && r.scheduledAt().isAfter(Instant.now())) { notices.save(n); return ResponseEntity.ok(Map.of("status","SCHEDULED")); }
    if (!push.enabled()) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error","Push not configured (set FIREBASE_CREDENTIALS_FILE)"));
    return ResponseEntity.ok(Map.of("status","SENT","count", svc.dispatch(n))); }
  @GetMapping("/admin/notifications") @PreAuthorize(CONTENT) List<PushNotice> history(){ return notices.findAllByOrderByCreatedAtDesc(); }
}
