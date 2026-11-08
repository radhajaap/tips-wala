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
  /** Short-lived single-purpose token (email verify / password reset). Never accepted as a login token. */
  public String issuePurpose(java.util.UUID id, String purpose, int minutes, String ver){
    return Jwts.builder().subject(id.toString()).claim("purpose",purpose).claim("ver",ver).issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis()+minutes*60000L)).signWith(key).compact(); }
  public Claims parsePurpose(String t, String purpose){ Claims c = parse(t);
    if (!purpose.equals(c.get("purpose",String.class))) throw new JwtException("wrong purpose"); return c; }
}
