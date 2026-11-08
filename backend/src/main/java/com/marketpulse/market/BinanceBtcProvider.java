package com.marketpulse.market;
import java.time.Instant; import java.util.*;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Component; import org.springframework.web.client.RestClient;
/** BTC/USDT. Check the exchange's API terms before redistributing commercially. Prices are USDT, 24h rolling window for the quote. */
@Component
public class BinanceBtcProvider implements MarketDataProvider {
  private final RestClient http = RestClient.create(); private final String url, klines;
  BinanceBtcProvider(@Value("${app.market.btc-url}") String u, @Value("${app.market.btc-klines-url:https://api.binance.com/api/v3/klines?symbol=BTCUSDT}") String k){ url = u; klines = k; }
  public String name(){ return "Binance BTC/USDT"; }
  public boolean supports(String s){ return "BTC".equals(s); }
  public Optional<Quote> quote(String s){
    try {
      Map<?,?> m = http.get().uri(url).retrieve().body(Map.class);
      return Optional.of(new Quote("BTC","Bitcoin (USDT)", d(m,"lastPrice"), d(m,"prevClosePrice"), d(m,"openPrice"), d(m,"highPrice"), d(m,"lowPrice"),
        (long) d(m,"volume"), Instant.ofEpochMilli(((Number) m.get("closeTime")).longValue()), "LIVE", name()));
    } catch (Exception e) { return Optional.empty(); } }
  @Override public Optional<List<Candle>> history(String s, String range){
    String[] cfg = switch (range) { case "1D" -> new String[]{"5m","288"}; case "5D" -> new String[]{"30m","240"}; case "1M" -> new String[]{"4h","180"};
      case "3M" -> new String[]{"1d","90"}; case "6M" -> new String[]{"1d","180"}; case "1Y" -> new String[]{"1d","365"}; case "5Y" -> new String[]{"1w","260"}; default -> null; };
    if (!"BTC".equals(s) || cfg == null) return Optional.empty();
    try { List<?> rows = http.get().uri(klines + "&interval=" + cfg[0] + "&limit=" + cfg[1]).retrieve().body(List.class);
      return Optional.of(rows.stream().map(r -> { List<?> a = (List<?>) r; return new Candle(((Number) a.get(0)).longValue(), n(a,1), n(a,2), n(a,3), n(a,4), n(a,5)); }).toList());
    } catch (Exception e) { return Optional.empty(); } }
  private static double d(Map<?,?> m, String k){ return Double.parseDouble(String.valueOf(m.get(k))); }
  private static double n(List<?> a, int i){ return Double.parseDouble(String.valueOf(a.get(i))); }
}
