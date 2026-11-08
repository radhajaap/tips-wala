package com.marketpulse.market;
import static org.junit.jupiter.api.Assertions.*; import java.time.*; import org.junit.jupiter.api.Test;
class MarketStatusTest {
  static final ZoneId IST = ZoneId.of("Asia/Kolkata"); static final LocalTime O = LocalTime.of(9,15), C = LocalTime.of(15,30);
  @Test void openDuringHours(){ assertEquals("OPEN", MarketStatusService.session(ZonedDateTime.of(2026,11,9,10,0,0,0,IST), false, O, C)); }
  @Test void closedAfterHoursWeekendAndHoliday(){
    assertEquals("CLOSED", MarketStatusService.session(ZonedDateTime.of(2026,11,9,15,30,0,0,IST), false, O, C));
    assertEquals("CLOSED", MarketStatusService.session(ZonedDateTime.of(2026,11,8,11,0,0,0,IST), false, O, C));
    assertEquals("CLOSED", MarketStatusService.session(ZonedDateTime.of(2026,11,9,11,0,0,0,IST), true, O, C)); }
}
