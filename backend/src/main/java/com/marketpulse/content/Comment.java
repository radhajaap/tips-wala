package com.marketpulse.content;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="comments") public class Comment { @Id public UUID id = UUID.randomUUID(); public UUID postId, userId; public String authorName, body, status = "PENDING"; public Instant createdAt = Instant.now(); }
