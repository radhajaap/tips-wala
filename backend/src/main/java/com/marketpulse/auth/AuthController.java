package com.marketpulse.auth;
import com.marketpulse.config.JwtService; import com.marketpulse.user.*;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.util.Map;
import org.springframework.http.*; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth")
public class AuthController {
  record RegisterReq(@NotBlank @Size(max=120) String fullName, @Email @NotBlank String email,
    @Pattern(regexp="^[6-9]\\d{9}$", message="Valid 10-digit Indian mobile required") String mobile,
    @Size(min=8,max=72) String password, @NotBlank String confirmPassword) {}
  record LoginReq(@Email @NotBlank String email, @NotBlank String password) {}
  private final UserRepository users; private final PasswordEncoder enc; private final JwtService jwt; private final com.marketpulse.notify.EmailService mail; private final String url;
  public AuthController(UserRepository u, PasswordEncoder e, JwtService j, com.marketpulse.notify.EmailService m, @org.springframework.beans.factory.annotation.Value("${app.public-url}") String pu){ users=u; enc=e; jwt=j; mail=m; url=pu; }
  @PostMapping("/register") ResponseEntity<?> register(@Valid @RequestBody RegisterReq r){
    if (!r.password().equals(r.confirmPassword())) return ResponseEntity.badRequest().body(Map.of("error","Passwords do not match"));
    String email = r.email().toLowerCase();
    if (users.findByEmail(email).isPresent()) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","Email already registered"));
    var u = new User(); u.fullName=r.fullName().trim(); u.email=email; u.mobile=r.mobile(); u.passwordHash=enc.encode(r.password());
    users.save(u);
    mail.send(u.email, "Verify your Tips Wala email", "Open this link to verify your email (valid 48 hours):\n"+url+"/account.html?mode=verify&token="+jwt.issuePurpose(u.id,"verify",2880,""));
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("token", jwt.issue(u)));
  }
  @PostMapping("/login") ResponseEntity<?> login(@Valid @RequestBody LoginReq r){
    var u = users.findByEmail(r.email().toLowerCase()).filter(x -> enc.matches(r.password(), x.passwordHash));
    return u.<ResponseEntity<?>>map(x -> ResponseEntity.ok(Map.of("token", jwt.issue(x))))
      .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Invalid email or password")));
  }
}
