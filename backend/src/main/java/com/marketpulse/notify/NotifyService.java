package com.marketpulse.notify;
import com.marketpulse.billing.SubscriptionRepository; import com.marketpulse.user.*; import java.time.Instant; import java.util.*; import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Service;
@Service
public class NotifyService {
  private final DeviceTokenRepository tokens; private final UserRepository users; private final SubscriptionRepository subs; private final PushNoticeRepository notices; private final PushService push;
  NotifyService(DeviceTokenRepository t, UserRepository u, SubscriptionRepository s, PushNoticeRepository n, PushService p){ tokens=t; users=u; subs=s; notices=n; push=p; }
  /** Respects each user's push preference. Audience MEMBERS = active subscription (paid or demo). */
  public int dispatch(PushNotice n){
    var all = tokens.findAll();
    Map<UUID,User> um = users.findAllById(all.stream().map(t -> t.userId).distinct().toList()).stream().collect(Collectors.toMap(x -> x.id, x -> x));
    var list = all.stream().filter(t -> { User x = um.get(t.userId);
      return x != null && x.notifyPush && ("ALL".equals(n.audience) || subs.existsByUserIdAndEndAtAfter(x.id, Instant.now())); }).map(t -> t.token).toList();
    n.sentCount = push.send(n, list); n.sentAt = Instant.now(); notices.save(n); return n.sentCount; }
  @Scheduled(fixedDelay = 60000) void due(){ if (push.enabled()) notices.findByScheduledAtBeforeAndSentAtIsNull(Instant.now()).forEach(this::dispatch); }
}
