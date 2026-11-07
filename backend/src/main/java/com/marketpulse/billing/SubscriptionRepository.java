package com.marketpulse.billing;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface SubscriptionRepository extends JpaRepository<Subscription,UUID> { Optional<Subscription> findFirstByUserIdOrderByEndAtDesc(UUID userId); }
