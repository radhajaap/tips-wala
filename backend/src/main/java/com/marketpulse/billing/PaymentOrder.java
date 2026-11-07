package com.marketpulse.billing;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="payment_orders")
public class PaymentOrder { @Id public UUID id = UUID.randomUUID(); public UUID userId; public String plan, razorpayOrderId, razorpayPaymentId, status = "CREATED";
  public long amountPaise; public Instant createdAt = Instant.now(); }
