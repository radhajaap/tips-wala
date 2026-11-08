package com.marketpulse.market;
import com.marketpulse.content.CalendarEventRepository; import java.time.*; import java.util.Map;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
/** Trading-hours helper. Holidays come from the admin calendar (kind HOLIDAY); keep that calendar updated. */
@Service
public class MarketStatusService {
  static final ZoneId IST = ZoneId.of("Asia/Kolkata");
  private final CalendarEventRepository events; private final LocalTime mcxClose;
  MarketStatusService(CalendarEventRepository e, @Value("${app.market.mcx-close:23:30}") String close){ events = e; mcxClose = LocalTime.parse(close); }
  static String session(ZonedDateTime now, boolean holiday, LocalTime open, LocalTime close){
    DayOfWeek d = now.getDayOfWeek(); LocalTime t = now.toLocalTime();
    if (holiday || d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY) return "CLOSED";
    return !t.isBefore(open) && t.isBefore(close) ? "OPEN" : "CLOSED"; }
  private String of(LocalTime open, LocalTime close){ ZonedDateTime n = ZonedDateTime.now(IST); return session(n, events.existsByKindAndEventDate("HOLIDAY", n.toLocalDate()), open, close); }
  public String nse(){ return of(LocalTime.of(9, 15), LocalTime.of(15, 30)); }
  /** MCX hours differ by contract and season: this is an approximation, verify with MCX. */
  public String mcx(){ return of(LocalTime.of(9, 0), mcxClose); }
  public Map<String,String> all(){ String n = nse(), m = mcx(); return Map.of("NIFTY50", n, "BANKNIFTY", n, "MCXGOLD", m, "MCXSILVER", m, "BTC", "ALWAYS"); }
}
