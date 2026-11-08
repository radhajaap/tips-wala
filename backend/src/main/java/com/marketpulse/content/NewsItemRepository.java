package com.marketpulse.content;
import java.util.*; import java.time.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface NewsItemRepository extends JpaRepository<NewsItem,UUID> { List<NewsItem> findTop50ByOrderByPublishedAtDesc(); }
