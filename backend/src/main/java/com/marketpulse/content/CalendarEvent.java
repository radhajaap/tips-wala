package com.marketpulse.content;
import jakarta.persistence.*; import java.time.LocalDate; import java.util.UUID;
@Entity @Table(name="calendar_events") public class CalendarEvent { @Id public UUID id = UUID.randomUUID(); public String title, kind, details, source; public LocalDate eventDate; }
