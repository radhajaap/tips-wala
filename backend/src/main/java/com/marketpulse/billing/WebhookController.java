package com.marketpulse.billing;
import com.fasterxml.jackson.databind.*; import com.marketpulse.notify.EmailService; import com.marketpulse.user.UserRepository; import java.util.Map;
import org.springframework.beans.factory.annotation.Value; import org.springframework.http.*; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.web.bind.annotation.*;
/** Safety net: activates the membership even if the customer closes the browser before /verify runs. Configure in Razorpay dashboard: event payment.captured. */
@RestController @RequestMapping("/api/payments")
public class WebhookController {
  private final PaymentOrderRepository orders; private final SubscriptionService subs; private final JdbcTemplate jdbc; private final ObjectMapper om; private final UserRepository users; private final EmailService mail; private final String secret;
  WebhookController(PaymentOrderRepository o, SubscriptionService s, JdbcTemplate j, ObjectMapper m, UserRepository u, EmailService e, @Value("${app.razorpay.webhook-secret}") String sec){ orders=o; subs=s; jdbc=j; om=m; users=u; mail=e; secret=sec; }
  @PostMapping("/webhook") ResponseEntity<?> hook(@RequestBody String body, @RequestHeader(value="X-Razorpay-Signature", required=false) String sig, @RequestHeader(value="X-Razorpay-Event-Id", required=false) String eid){
    if (secret.isBlank() || sig == null || !Hmac.same(Hmac.hex(secret, body), sig)) return ResponseEntity.badRequest().body(Map.of("error","bad signature"));
    try {
      JsonNode r = om.readTree(body);
      if ("payment.captured".equals(r.path("event").asText())) {
        JsonNode pay = r.path("payload").path("payment").path("entity"); String oid = pay.path("order_id").asText(""), pid = pay.path("id").asText("");
        if (!oid.isEmpty() && orders.claim(oid, pid) == 1) {
          var o = orders.findByRazorpayOrderId(oid).orElseThrow(); var s = subs.activatePaid(o.userId, Plan.valueOf(o.plan));
          users.findById(o.userId).ifPresent(u -> mail.send(u.email, "Tips Wala payment confirmed", "Your " + o.plan + " membership is active until " + s.endAt + ".")); } }
      if (eid != null) jdbc.update("insert into webhook_events(event_id) values (?) on conflict do nothing", eid);
      return ResponseEntity.ok(Map.of("ok", true));
    } catch (Exception e) { return ResponseEntity.status(500).body(Map.of("error","processing failed")); } } // 500 makes Razorpay retry
}
