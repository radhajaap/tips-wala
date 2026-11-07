package com.marketpulse.config;
import com.marketpulse.user.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Component;
/** Creates the first SUPER_ADMIN from ADMIN_EMAIL / ADMIN_PASSWORD env vars if no such user exists. */
@Component
public class AdminBootstrap implements CommandLineRunner {
  private final UserRepository users; private final PasswordEncoder enc; private final String email, password;
  AdminBootstrap(UserRepository u, PasswordEncoder e, @Value("${app.admin.email}") String em, @Value("${app.admin.password}") String pw){ users=u; enc=e; email=em; password=pw; }
  @Override public void run(String... args){
    if (email.isBlank() || password.length() < 12 || users.findByEmail(email.toLowerCase()).isPresent()) return;
    var u = new User(); u.fullName="Super Admin"; u.email=email.toLowerCase(); u.mobile="0000000000"; u.passwordHash=enc.encode(password); u.role="SUPER_ADMIN"; u.emailVerified=true;
    users.save(u); }
}
