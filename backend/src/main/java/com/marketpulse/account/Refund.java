package com.marketpulse.account;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="refunds") public class Refund { @Id public UUID id = UUID.randomUUID(); public UUID orderId, userId; public String reason, status = "REQUESTED", adminNote, rejectReason, refundTxnId; public Instant createdAt = Instant.now(); }
