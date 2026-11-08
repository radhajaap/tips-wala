package com.marketpulse.notify;
import java.util.*; import java.time.Instant; import org.springframework.data.jpa.repository.JpaRepository;
public interface PushNoticeRepository extends JpaRepository<PushNotice,UUID> { List<PushNotice> findByScheduledAtBeforeAndSentAtIsNull(Instant t); List<PushNotice> findAllByOrderByCreatedAtDesc(); }
