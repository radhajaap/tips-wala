package com.marketpulse.content;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="faqs") public class Faq { @Id public UUID id = UUID.randomUUID(); @Column(columnDefinition="TEXT") public String question, answer; public int sortOrder; }
