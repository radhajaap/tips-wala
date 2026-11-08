package com.marketpulse.admin;
import java.util.*; import java.time.Instant; import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditLogRepository extends JpaRepository<AuditLog,UUID> {  }
