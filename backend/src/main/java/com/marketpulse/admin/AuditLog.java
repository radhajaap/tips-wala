package com.marketpulse.admin;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="audit_logs") public class AuditLog { @Id public UUID id = UUID.randomUUID(); public UUID actorId; public String action, detail; public Instant createdAt = Instant.now(); }
