package com.marketpulse.notify;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="device_tokens") public class DeviceToken { @Id public UUID id = UUID.randomUUID(); public UUID userId; public String token, platform = "android"; public Instant createdAt = Instant.now(); }
