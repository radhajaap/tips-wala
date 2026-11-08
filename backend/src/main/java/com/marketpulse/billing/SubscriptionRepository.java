package com.marketpulse.billing;
import org.springframework.data.jpa.repository.JpaRepository; import java.time.Instant; import java.util.*;
public interface SubscriptionRepository extends JpaRepository<Subscription,UUID> {
  Optional<Subscription> findFirstByUserIdOrderByEndAtDesc(UUID userId);
  boolean existsByUserIdAndEndAtAfter(UUID userId, Instant t);
  boolean existsByUserIdAndKind(UUID userId, String kind);
  long countByKindAndEndAtAfter(String kind, Instant t);
  long countByEndAtBefore(Instant t);
}
