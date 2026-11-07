package com.marketpulse.billing;
import com.marketpulse.user.*; import java.time.*; import java.util.*;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service
public class SubscriptionService {
  static final ZoneId IST = ZoneId.of("Asia/Kolkata");
  private final SubscriptionRepository subs; private final UserRepository users; private final int trialDays;
  SubscriptionService(SubscriptionRepository s, UserRepository u, @Value("${app.trial.business-days}") int d){ subs=s; users=u; trialDays=d; }
  /** Adds n business days (Mon-Fri). TODO: also skip NSE trading holidays from a holidays table. */
  static Instant addBusinessDays(Instant from, int n){
    LocalDate d = from.atZone(IST).toLocalDate();
    while (n > 0) { d = d.plusDays(1); if (d.getDayOfWeek()!=DayOfWeek.SATURDAY && d.getDayOfWeek()!=DayOfWeek.SUNDAY) n--; }
    return d.atTime(23,59).atZone(IST).toInstant(); }
  @Transactional public Optional<Subscription> startTrial(UUID userId){
    User u = users.findById(userId).orElseThrow();
    if (u.trialUsed) return Optional.empty();           // one demo per user
    u.trialUsed = true; users.save(u);
    var s = new Subscription(); s.userId=userId; s.plan="TRIAL"; s.kind="TRIAL"; s.startAt=Instant.now(); s.endAt=addBusinessDays(s.startAt, trialDays);
    return Optional.of(subs.save(s)); }
  @Transactional public Subscription activatePaid(UUID userId, Plan plan){
    Instant now = Instant.now();
    Instant base = subs.findFirstByUserIdOrderByEndAtDesc(userId).map(x -> x.endAt).filter(e -> e.isAfter(now)).orElse(now);
    var s = new Subscription(); s.userId=userId; s.plan=plan.name(); s.kind="PAID"; s.startAt=now;
    s.endAt = base.atZone(ZoneOffset.UTC).plusMonths(plan.months).toInstant();
    return subs.save(s); }
}
