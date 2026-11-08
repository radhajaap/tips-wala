package com.marketpulse.content;
import java.util.*; import java.time.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface CalendarEventRepository extends JpaRepository<CalendarEvent,UUID> { List<CalendarEvent> findTop200ByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate d); boolean existsByKindAndEventDate(String kind, LocalDate d); }
