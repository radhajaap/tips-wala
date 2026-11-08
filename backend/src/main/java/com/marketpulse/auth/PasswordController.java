package com.marketpulse.auth;
import com.marketpulse.config.JwtService; import com.marketpulse.notify.EmailService; import com.marketpulse.user.*;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.util.*;
import org.springframework.beans.factory.annotation.Value; import org.springframework.http.ResponseEntity; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth")
public class PasswordController {
  record ForgotReq(@Email @NotBlank String email) {}
  record ResetReq(@NotBlank String token, @Size(min=8,max=72) String newPassword) {}
  private final UserRepository users; private final PasswordEncoder enc; private final JwtService jwt; private final EmailService mail; private final String url;
  PasswordController(UserRepository u, PasswordEncoder e, JwtService j, EmailService m, @Value("${app.public-url}") String pu){ users=u; enc=e; jwt=j; mail=m; url=pu; }
  private static String ver(User u){ return Integer.toHexString(u.passwordHash.hashCode()); }
  @PostMapping("/forgot") ResponseEntity<?> forgot(@Valid @RequestBody ForgotReq r){ // same answer whether or not the email exists
    users.findByEmail(r.email().toLowerCase()).ifPresent(u -> mail.send(u.email, "Reset your Tips Wala password",
      "Open this link to set a new password (valid 30 minutes):\n" + url + "/account.html?mode=reset&token=" + jwt.issuePurpose(u.id, "reset", 30, ver(u)) + "\nIgnore this email if you did not ask for it."));
    return ResponseEntity.ok(Map.of("ok", true)); }
  @PostMapping("/reset") ResponseEntity<?> reset(@Valid @RequestBody ResetReq r){
    try { var c = jwt.parsePurpose(r.token(), "reset"); var u = users.findById(UUID.fromString(c.getSubject())).orElseThrow();
      if (!ver(u).equals(c.get("ver", String.class))) throw new IllegalStateException("used");   // link works once
      u.passwordHash = enc.encode(r.newPassword()); users.save(u); return ResponseEntity.ok(Map.of("ok", true));
    } catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error","Invalid or expired link")); } }
  @GetMapping("/verify-email") ResponseEntity<?> verify(@RequestParam String token){
    try { var c = jwt.parsePurpose(token, "verify"); var u = users.findById(UUID.fromString(c.getSubject())).orElseThrow(); u.emailVerified = true; users.save(u); return ResponseEntity.ok(Map.of("ok", true));
    } catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error","Invalid or expired link")); } }
}
