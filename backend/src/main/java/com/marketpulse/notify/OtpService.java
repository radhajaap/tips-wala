package com.marketpulse.notify;
import java.security.*; import java.time.Duration; import java.util.UUID; import org.springframework.data.redis.core.StringRedisTemplate; import org.springframework.stereotype.Service;
@Service
public class OtpService {
  private final StringRedisTemplate r; private final SmsSender sms; private final SecureRandom rnd = new SecureRandom();
  OtpService(StringRedisTemplate r, SmsSender s){ this.r = r; sms = s; }
  public void send(UUID id, String mobile){
    String c = String.format("%06d", rnd.nextInt(1_000_000));
    sms.send(mobile, "Tips Wala OTP: " + c + " (valid 10 min)");
    r.opsForValue().set("otp:" + id, c, Duration.ofMinutes(10)); r.delete("otptry:" + id); }
  public boolean verify(UUID id, String code){
    Long t = r.opsForValue().increment("otptry:" + id); r.expire("otptry:" + id, Duration.ofMinutes(10));
    if (t != null && t > 5) return false;
    String c = r.opsForValue().get("otp:" + id);
    if (c != null && MessageDigest.isEqual(c.getBytes(), code.getBytes())) { r.delete("otp:" + id); return true; }
    return false; }
}
