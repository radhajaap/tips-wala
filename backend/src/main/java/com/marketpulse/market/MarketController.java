package com.marketpulse.market;
import java.util.*; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/market")
public class MarketController {
  private final MarketService svc; private final MarketStatusService st; MarketController(MarketService s, MarketStatusService t){ svc = s; st = t; }
  static final Set<String> UNIVERSES = Set.of("NIFTY50","NIFTYNEXT50","NIFTY100","NIFTY500","NSE");
  @GetMapping("/status") Map<String,String> status(){ return st.all(); }
  @GetMapping("/overview") List<Quote> overview(){ return svc.overview(); }
  @GetMapping("/search") List<Map<String,String>> search(@RequestParam String q){ return svc.search(q); }
  @GetMapping("/history") ResponseEntity<?> history(@RequestParam String symbol, @RequestParam(defaultValue="1M") String range){
    if (!MarketService.INSTRUMENTS.containsKey(symbol)) return ResponseEntity.badRequest().body(Map.of("error","unknown symbol"));
    var h = svc.history(symbol, range);
    return ResponseEntity.ok(Map.of("symbol", symbol, "range", range, "available", h.isPresent(), "source", svc.sourceOf(symbol), "candles", h.orElse(List.of()))); }
  @GetMapping("/movers") ResponseEntity<?> movers(@RequestParam(defaultValue="gainers") String type, @RequestParam(defaultValue="NIFTY50") String universe){
    if (!type.equals("gainers") && !type.equals("losers")) return ResponseEntity.badRequest().body(Map.of("error","type must be gainers or losers"));
    if (!UNIVERSES.contains(universe)) return ResponseEntity.badRequest().body(Map.of("error","universe must be one of " + UNIVERSES));
    var l = svc.movers(type, universe);
    return ResponseEntity.ok(Map.of("type", type, "universe", universe, "available", !l.isEmpty(), "session", st.nse(), "items", l)); }
}
