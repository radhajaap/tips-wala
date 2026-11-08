package com.marketpulse.billing;
import static org.junit.jupiter.api.Assertions.*; import java.time.*; import org.junit.jupiter.api.Test;
class BillingLogicTest {
  static final ZoneId IST = ZoneId.of("Asia/Kolkata");
  @Test void businessDaysSkipWeekend(){
    Instant fri = ZonedDateTime.of(2026,11,6,10,0,0,0,IST).toInstant();
    assertEquals(LocalDate.of(2026,11,9), SubscriptionService.addBusinessDays(fri,1).atZone(IST).toLocalDate()); }
  @Test void tenBusinessDaysFromMondayIsTwoWeeksLater(){
    Instant mon = ZonedDateTime.of(2026,11,9,10,0,0,0,IST).toInstant();
    assertEquals(LocalDate.of(2026,11,23), SubscriptionService.addBusinessDays(mon,10).atZone(IST).toLocalDate()); }
  @Test void planPricesLiveOnServer(){
    assertEquals(49900, Plan.MONTHLY.paise); assertEquals(250000, Plan.HALF_YEARLY.paise); assertEquals(450000, Plan.YEARLY.paise); assertEquals(12, Plan.YEARLY.months); }
  @Test void hmacKnownVector(){ // RFC 4231 test case 2
    assertEquals("5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843", Hmac.hex("Jefe","what do ya want for nothing?"));
    assertTrue(Hmac.same("ab","ab")); assertFalse(Hmac.same("ab","ac")); }
}
