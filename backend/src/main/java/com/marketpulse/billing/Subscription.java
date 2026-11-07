package com.marketpulse.billing;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="subscriptions")
public class Subscription { @Id public UUID id = UUID.randomUUID(); public UUID userId; public String plan, kind; public Instant startAt, endAt; }
