package com.marketpulse.content;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.time.*; import java.util.*;
import org.springframework.http.ResponseEntity; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;
/** Admin-curated only: no scraped or invented events. News stores headline + source + link + your own summary (no copied article text). */
@RestController @RequestMapping("/api")
public class CalendarController {
  static final String CONTENT = "hasAnyRole('SUPER_ADMIN','CONTENT_ADMIN')";
  static final Set<String> KINDS = Set.of("HOLIDAY","RBI","INFLATION","GDP","FED","GLOBAL","ANNOUNCEMENT","OTHER");
  record EventReq(@NotBlank @Size(max=200) String title, @NotBlank String kind, @NotNull LocalDate eventDate, @Size(max=500) String details, @Size(max=120) String source) {}
  record NewsReq(@NotBlank @Size(max=250) String title, @NotBlank @Size(max=120) String source, @NotBlank @Size(max=500) String url, @Size(max=500) String summary) {}
  private final CalendarEventRepository events; private final NewsItemRepository news;
  CalendarController(CalendarEventRepository e, NewsItemRepository n){ events=e; news=n; }
  @GetMapping("/calendar") List<CalendarEvent> calendar(){ return events.findTop200ByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate.now(ZoneId.of("Asia/Kolkata")).minusDays(1)); }
  @PostMapping("/admin/calendar") @PreAuthorize(CONTENT) ResponseEntity<?> addEvent(@Valid @RequestBody EventReq r){
    String k = r.kind().toUpperCase(); if (!KINDS.contains(k)) return ResponseEntity.badRequest().body(Map.of("error","Unknown kind"));
    var e = new CalendarEvent(); e.title=r.title().trim(); e.kind=k; e.eventDate=r.eventDate(); e.details=r.details(); e.source=r.source(); return ResponseEntity.ok(events.save(e)); }
  @DeleteMapping("/admin/calendar/{id}") @PreAuthorize(CONTENT) void delEvent(@PathVariable UUID id){ events.deleteById(id); }
  @GetMapping("/news") List<NewsItem> news(){ return news.findTop50ByOrderByPublishedAtDesc(); }
  @PostMapping("/admin/news") @PreAuthorize(CONTENT) ResponseEntity<?> addNews(@Valid @RequestBody NewsReq r){
    if (!r.url().startsWith("https://")) return ResponseEntity.badRequest().body(Map.of("error","Link must start with https://"));
    var n = new NewsItem(); n.title=r.title().trim(); n.source=r.source().trim(); n.url=r.url().trim(); n.summary=r.summary(); return ResponseEntity.ok(news.save(n)); }
  @DeleteMapping("/admin/news/{id}") @PreAuthorize(CONTENT) void delNews(@PathVariable UUID id){ news.deleteById(id); }
}
