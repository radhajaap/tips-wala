package com.marketpulse.market;
import static org.junit.jupiter.api.Assertions.*; import com.fasterxml.jackson.databind.ObjectMapper; import java.time.*; import java.util.Map; import org.junit.jupiter.api.Test;
class VendorProviderTest {
  final ObjectMapper om = new ObjectMapper();
  ConfigurableJsonProvider provider(){
    var fields = Map.of("price","/ltp","prevClose","/close","open","/o","high","/h","low","/l","volume","/vol","time","/ts","symbol","/sym","name","/nm");
    return new ConfigurableJsonProvider(new VendorProps(true,"TestVendor",null,null,true,"ISO",null,"",null,"/data",null,null,null,Map.of("NIFTY50","X"),fields,null), om); }
  @Test void parsesMappedQuoteAndMarksDelayed() throws Exception {
    var q = provider().parse(om.readTree("{\"ltp\":\"101.5\",\"close\":100,\"o\":99,\"h\":102,\"l\":98,\"vol\":12345,\"ts\":\"2026-11-06T15:29:00+05:30\",\"sym\":\"INFY\",\"nm\":\"Infosys\"}"), "INFY", "Infosys").orElseThrow();
    assertEquals(101.5, q.price()); assertEquals("DELAYED", q.status()); assertEquals("TestVendor", q.source()); assertEquals(1.5, q.changePct(), 1e-9);
    assertEquals(Instant.parse("2026-11-06T09:59:00Z"), q.asOf()); }
  @Test void rejectsQuoteWithoutTimestamp() throws Exception {
    assertTrue(provider().parse(om.readTree("{\"ltp\":101.5,\"close\":100}"), "X", "X").isEmpty()); }
  @Test void supportsOnlyConfiguredSymbols(){ assertTrue(provider().supports("NIFTY50")); assertFalse(provider().supports("BTC")); }
}
