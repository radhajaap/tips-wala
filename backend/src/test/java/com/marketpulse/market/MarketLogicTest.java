package com.marketpulse.market;
import static org.junit.jupiter.api.Assertions.*; import java.time.Instant; import org.junit.jupiter.api.Test;
class MarketLogicTest {
  @Test void changePercent(){ var q = new Quote("X","X",110,100,100,111,99,1,Instant.now(),"LIVE","t"); assertEquals(10.0, q.changePct(), 1e-9); }
  @Test void unavailableQuoteHasNoPrice(){ var q = Quote.unavailable("NIFTY50","NIFTY 50"); assertEquals("UNAVAILABLE", q.status()); assertEquals(0, q.price()); assertNull(q.asOf()); }
}
