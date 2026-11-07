package com.marketpulse.config;
import com.marketpulse.user.User; import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
import javax.crypto.SecretKey; import java.nio.charset.StandardCharsets; import java.util.Date;
@Service
public class JwtService {
  private final SecretKey key;
  public JwtService(@Value("${app.jwt.secret}") String s){ key = Keys.hmacShaKeyFor(s.getBytes(StandardCharsets.UTF_8)); }
  public String issue(User u){
    return Jwts.builder().subject(u.id.toString()).claim("role",u.role).issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis()+24L*3600*1000)).signWith(key).compact(); }
  public Claims parse(String t){ return Jwts.parser().verifyWith(key).build().parseSignedClaims(t).getPayload(); }
}
