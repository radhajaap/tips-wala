package com.marketpulse.billing;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder,UUID> { Optional<PaymentOrder> findByRazorpayOrderId(String id); }
