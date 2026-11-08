package com.marketpulse.market;
import static org.junit.jupiter.api.Assertions.*; import com.fasterxml.jackson.databind.ObjectMapper; import java.time.Instant; import java.util.Map; import org.junit.jupiter.api.Test;
/** Verifies config/yahoo-testing.example.yml field mapping against the response shape used by the Mudra Vault page. */
class YahooMappingTest {
  final ObjectMapper om = new ObjectMapper();
  @Test void mapsYahooChartMeta() throws Exception {
    var fields = Map.of("price","/regularMarketPrice","prevClose","/chartPreviousClose","high","/regularMarketDayHigh","low","/regularMarketDayLow","volume","/regularMarketVolume","time","/regularMarketTime");
    var p = new ConfigurableJsonProvider(new VendorProps(true,"Yahoo",null,null,true,"EPOCH_S","u","/chart/result/0/meta",null,null,null,null,null,Map.of("NIFTY50","^NSEI"),fields,null), om);
    var root = om.readTree("{\"chart\":{\"result\":[{\"meta\":{\"currency\":\"INR\",\"symbol\":\"^NSEI\",\"regularMarketPrice\":24310.4,\"chartPreviousClose\":24160.1,\"regularMarketDayHigh\":24350.0,\"regularMarketDayLow\":24200.5,\"regularMarketVolume\":0,\"regularMarketTime\":1762599000}}]}}");
    var q = p.parse(root.at("/chart/result/0/meta"), "NIFTY50", "NIFTY 50").orElseThrow();
    assertEquals(24310.4, q.price()); assertEquals(24160.1, q.prevClose()); assertEquals("DELAYED", q.status()); assertEquals(Instant.ofEpochSecond(1762599000L), q.asOf()); }
}
