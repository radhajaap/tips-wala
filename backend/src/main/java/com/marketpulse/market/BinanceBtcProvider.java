package com.marketpulse.market;
import java.time.Instant; import java.util.*;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Component; import org.springframework.web.client.RestClient;
/** BTC/USDT 24h ticker. Check the exchange's API terms before redistributing commercially. Prices are USDT, 24h rolling window. */
@Component
public class BinanceBtcProvider implements MarketDataProvider {
  private final RestClient http = RestClient.create(); private final String url;
  BinanceBtcProvider(@Value("${app.market.btc-url}") String u){ url = u; }
  public String name(){ return "Binance BTC/USDT"; }
  public boolean supports(String s){ return "BTC".equals(s); }
  public Optional<Quote> quote(String s){
    try {
      Map<?,?> m = http.get().uri(url).retrieve().body(Map.class);
      return Optional.of(new Quote("BTC","Bitcoin (USDT)", d(m,"lastPrice"), d(m,"prevClosePrice"), d(m,"openPrice"), d(m,"highPrice"), d(m,"lowPrice"),
        (long) d(m,"volume"), Instant.ofEpochMilli(((Number) m.get("closeTime")).longValue()), "LIVE", name()));
    } catch (Exception e) { return Optional.empty(); } }
  private static double d(Map<?,?> m, String k){ return Double.parseDouble(String.valueOf(m.get(k))); }
}
