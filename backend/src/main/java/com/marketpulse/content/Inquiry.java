package com.marketpulse.content;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="contact_inquiries") public class Inquiry { @Id public UUID id = UUID.randomUUID(); public String name, email, mobile, subject, message, status = "NEW"; public Instant createdAt = Instant.now(); }
