package com.marketpulse.market;
import com.fasterxml.jackson.databind.ObjectMapper; import java.time.*; import java.util.*; import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.redis.core.StringRedisTemplate; import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Service;
@Service
public class MarketService {
  public static final Map<String,String> INSTRUMENTS = new LinkedHashMap<>();
  static { INSTRUMENTS.put("NIFTY50","NIFTY 50"); INSTRUMENTS.put("BANKNIFTY","BANK NIFTY"); INSTRUMENTS.put("MCXGOLD","MCX GOLD");
           INSTRUMENTS.put("MCXSILVER","MCX SILVER"); INSTRUMENTS.put("BTC","Bitcoin"); }
  private final List<MarketDataProvider> providers; private final MarketWsHandler ws; private final StringRedisTemplate redis; private final ObjectMapper om;
  private final Map<String,Quote> latest = new ConcurrentHashMap<>();
  MarketService(List<MarketDataProvider> p, MarketWsHandler w, StringRedisTemplate r, ObjectMapper o){ providers=p; ws=w; redis=r; om=o; }

  @Scheduled(fixedDelay = 5000)
  void refresh(){
    for (String s : INSTRUMENTS.keySet()) {
      providers.stream().filter(p -> p.supports(s)).findFirst().flatMap(p -> p.quote(s)).filter(this::valid).ifPresent(q -> {
        Quote old = latest.get(s);
        if (old != null && old.price() == q.price() && Objects.equals(old.asOf(), q.asOf())) return; // dedupe
        latest.put(s, q); cache(q); ws.broadcast(q); });
    } }
  private boolean valid(Quote q){ return Double.isFinite(q.price()) && q.price() > 0 && q.prevClose() > 0 && q.asOf() != null; }
  private void cache(Quote q){ try { redis.opsForValue().set("quote:"+q.symbol(), om.writeValueAsString(q), Duration.ofMinutes(2)); } catch (Exception ignored) {} }

  public Quote get(String s){
    Quote q = latest.get(s);
    if (q == null) return Quote.unavailable(s, INSTRUMENTS.get(s));
    return Duration.between(q.asOf(), Instant.now()).toSeconds() > 60 ? q.withStatus("STALE") : q; }
  public Optional<List<Candle>> history(String symbol, String range){
    return providers.stream().filter(p -> p.supports(symbol)).findFirst().flatMap(p -> p.history(symbol, range)); }
  public String sourceOf(String symbol){ return providers.stream().filter(p -> p.supports(symbol)).findFirst().map(MarketDataProvider::name).orElse("none"); }
  public List<Quote> overview(){ return INSTRUMENTS.keySet().stream().map(this::get).toList(); }
  public List<Quote> movers(String type, String universe){
    boolean up = type.equals("gainers");
    return providers.stream().flatMap(p -> p.movers(universe).stream()).filter(this::valid)
      .filter(q -> up ? q.changePct() > 0 : q.changePct() < 0)
      .sorted(up ? Comparator.comparingDouble(Quote::changePct).reversed() : Comparator.comparingDouble(Quote::changePct))
      .limit(25).toList(); }
}
