package com.marketpulse.content;
import java.util.*; import java.time.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface CommentRepository extends JpaRepository<Comment,UUID> { List<Comment> findByPostIdAndStatusOrderByCreatedAtAsc(UUID p, String s); List<Comment> findByStatusOrderByCreatedAtDesc(String s); }
