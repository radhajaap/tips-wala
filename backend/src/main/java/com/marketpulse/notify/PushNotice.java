package com.marketpulse.notify;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="notifications") public class PushNotice { @Id public UUID id = UUID.randomUUID(); public String title, message, imageUrl, deepLink, audience = "ALL";
  public Instant scheduledAt, sentAt; public int sentCount; public Instant createdAt = Instant.now(); }
