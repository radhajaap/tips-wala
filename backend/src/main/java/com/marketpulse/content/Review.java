package com.marketpulse.content;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="reviews") public class Review { @Id public UUID id = UUID.randomUUID(); public UUID userId; public String authorName, body, status = "PENDING";
  public int rating; public boolean verified; public Instant createdAt = Instant.now(); }
