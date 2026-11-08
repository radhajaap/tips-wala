package com.marketpulse.user;
import org.springframework.data.jpa.repository.JpaRepository; import java.time.Instant; import java.util.*;
public interface UserRepository extends JpaRepository<User,UUID> { Optional<User> findByEmail(String email); long countByCreatedAtAfter(Instant t); }
