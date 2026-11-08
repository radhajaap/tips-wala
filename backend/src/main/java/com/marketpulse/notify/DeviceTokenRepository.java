package com.marketpulse.notify;
import java.util.*; import java.time.Instant; import org.springframework.data.jpa.repository.JpaRepository;
public interface DeviceTokenRepository extends JpaRepository<DeviceToken,UUID> { Optional<DeviceToken> findByToken(String t); }
