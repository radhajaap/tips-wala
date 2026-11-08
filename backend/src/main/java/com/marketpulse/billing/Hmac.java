package com.marketpulse.billing;
import java.nio.charset.StandardCharsets; import java.security.MessageDigest; import java.util.HexFormat; import javax.crypto.Mac; import javax.crypto.spec.SecretKeySpec;
public final class Hmac {
  private Hmac(){}
  public static String hex(String secret, String msg){
    try { Mac m = Mac.getInstance("HmacSHA256"); m.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return HexFormat.of().formatHex(m.doFinal(msg.getBytes(StandardCharsets.UTF_8))); } catch (Exception e) { throw new IllegalStateException(e); } }
  public static boolean same(String a, String b){ return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8)); }
}
