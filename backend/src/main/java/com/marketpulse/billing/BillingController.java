package com.marketpulse.billing;
import jakarta.validation.Valid; import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets; import java.security.MessageDigest; import java.util.*;
import javax.crypto.Mac; import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value; import org.springframework.http.*;
import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import org.springframework.web.client.RestClient;
@RestController @RequestMapping("/api")
public class BillingController {
  record OrderReq(@NotNull Plan plan) {}
  record VerifyReq(@NotBlank String orderId, @NotBlank String paymentId, @NotBlank String signature) {}
  private final PaymentOrderRepository orders; private final SubscriptionService subs; private final String keyId, keySecret;
  private final RestClient http = RestClient.create();
  private final com.marketpulse.user.UserRepository users; private final com.marketpulse.notify.EmailService mail;
  BillingController(PaymentOrderRepository o, SubscriptionService s, @Value("${app.razorpay.key-id}") String k, @Value("${app.razorpay.key-secret}") String ks, com.marketpulse.user.UserRepository ur, com.marketpulse.notify.EmailService m){ orders=o; subs=s; keyId=k; keySecret=ks; users=ur; mail=m; }

  @PostMapping("/trial/start") ResponseEntity<?> trial(Authentication a){
    return subs.startTrial(UUID.fromString(a.getName()))
      .<ResponseEntity<?>>map(s -> ResponseEntity.ok(Map.of("endsAt", s.endAt.toString())))
      .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","Free demo already used"))); }

  @PostMapping("/payments/order") ResponseEntity<?> createOrder(@Valid @RequestBody OrderReq r, Authentication a){
    if (keyId.isBlank()) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error","Payments not configured"));
    Map<?,?> res = http.post().uri("https://api.razorpay.com/v1/orders").headers(h -> h.setBasicAuth(keyId, keySecret))
      .contentType(MediaType.APPLICATION_JSON).body(Map.of("amount", r.plan().paise, "currency","INR", "receipt","mp_"+System.currentTimeMillis()))
      .retrieve().body(Map.class);
    var o = new PaymentOrder(); o.userId=UUID.fromString(a.getName()); o.plan=r.plan().name(); o.amountPaise=r.plan().paise; o.razorpayOrderId=(String)res.get("id");
    orders.save(o);
    return ResponseEntity.ok(Map.of("orderId", o.razorpayOrderId, "amount", o.amountPaise, "currency","INR", "keyId", keyId)); }

  /** Membership is activated ONLY after the HMAC signature is verified on the server. */
  @PostMapping("/payments/verify") ResponseEntity<?> verify(@Valid @RequestBody VerifyReq r, Authentication a) throws Exception {
    var o = orders.findByRazorpayOrderId(r.orderId()).filter(x -> x.userId.equals(UUID.fromString(a.getName()))).orElse(null);
    if (o == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error","Order not found"));
    if (!"CREATED".equals(o.status)) return ResponseEntity.ok(Map.of("status", o.status)); // idempotent
    Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    String expected = HexFormat.of().formatHex(mac.doFinal((r.orderId()+"|"+r.paymentId()).getBytes(StandardCharsets.UTF_8)));
    if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), r.signature().getBytes(StandardCharsets.UTF_8))) {
      o.status="FAILED"; orders.save(o); return ResponseEntity.badRequest().body(Map.of("error","Invalid payment signature")); }
    o.status="PAID"; o.razorpayPaymentId=r.paymentId(); orders.save(o);
    var s = subs.activatePaid(o.userId, Plan.valueOf(o.plan));
    users.findById(o.userId).ifPresent(u -> mail.send(u.email, "Tips Wala payment confirmed", "Your "+o.plan+" membership is active until "+s.endAt+". Amount: Rs "+(o.amountPaise/100.0)+". Download your receipt from your dashboard."));
    return ResponseEntity.ok(Map.of("status","PAID","expiresAt", s.endAt.toString())); }
}
