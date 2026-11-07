package com.marketpulse.content;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface ReviewRepository extends JpaRepository<Review,UUID> { List<Review> findByStatusOrderByCreatedAtDesc(String s); Optional<Review> findByUserId(UUID u); }
