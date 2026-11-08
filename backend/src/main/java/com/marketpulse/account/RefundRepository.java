package com.marketpulse.account;
import java.util.*; import java.time.Instant; import org.springframework.data.jpa.repository.JpaRepository;
public interface RefundRepository extends JpaRepository<Refund,UUID> { Optional<Refund> findByOrderId(UUID o); List<Refund> findByUserIdOrderByCreatedAtDesc(UUID u); List<Refund> findAllByOrderByCreatedAtDesc(); long countByStatus(String s); }
