package com.marketpulse.billing;
import java.time.Instant; import java.util.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder,UUID> {
  Optional<PaymentOrder> findByRazorpayOrderId(String id);
  List<PaymentOrder> findByUserIdOrderByCreatedAtDesc(UUID u);
  @Query("select coalesce(sum(p.amountPaise),0) from PaymentOrder p where p.status='PAID'") long totalPaid();
  @Query("select coalesce(sum(p.amountPaise),0) from PaymentOrder p where p.status='PAID' and p.createdAt>=:from") long paidSince(@Param("from") Instant from);
}
