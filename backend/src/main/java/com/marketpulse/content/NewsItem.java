package com.marketpulse.content;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="news_items") public class NewsItem { @Id public UUID id = UUID.randomUUID(); public String title, source, url, summary; public Instant publishedAt = Instant.now(); }
