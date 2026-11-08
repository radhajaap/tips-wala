package com.marketpulse.content;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="posts") public class Post { @Id public UUID id = UUID.randomUUID(); public String title, slug, category, author, seoTitle, seoDescription;
  @Column(columnDefinition="TEXT") public String body; public String featuredImageUrl, videoUrl; public boolean premium, published; public long views, likes; public Instant publishedAt; public Instant createdAt = Instant.now(); }
