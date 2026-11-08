package com.marketpulse.billing;
import java.time.Instant; import java.util.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import org.springframework.transaction.annotation.Transactional;
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder,UUID> {
  Optional<PaymentOrder> findByRazorpayOrderId(String id);
  List<PaymentOrder> findByUserIdOrderByCreatedAtDesc(UUID u);
  @Query("select coalesce(sum(p.amountPaise),0) from PaymentOrder p where p.status='PAID'") long totalPaid();
  @Query("select coalesce(sum(p.amountPaise),0) from PaymentOrder p where p.status='PAID' and p.createdAt>=:from") long paidSince(@Param("from") Instant from);
  /** Atomically moves CREATED -> PAID. Returns 1 only for the first caller (verify endpoint or webhook), so membership is never activated twice. */
  @Modifying @Transactional @Query("update PaymentOrder p set p.status='PAID', p.razorpayPaymentId=:pid where p.razorpayOrderId=:oid and p.status='CREATED'")
  int claim(@Param("oid") String orderId, @Param("pid") String paymentId);
}
