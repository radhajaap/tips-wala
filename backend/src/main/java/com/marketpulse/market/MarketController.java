package com.marketpulse.market;
import java.util.*; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/market")
public class MarketController {
  private final MarketService svc; MarketController(MarketService s){ svc = s; }
  @GetMapping("/overview") List<Quote> overview(){ return svc.overview(); }
  @GetMapping("/history") ResponseEntity<?> history(@RequestParam String symbol, @RequestParam(defaultValue="1M") String range){
    if (!MarketService.INSTRUMENTS.containsKey(symbol)) return ResponseEntity.badRequest().body(Map.of("error","unknown symbol"));
    var h = svc.history(symbol, range);
    return ResponseEntity.ok(Map.of("symbol", symbol, "range", range, "available", h.isPresent(), "source", svc.sourceOf(symbol), "candles", h.orElse(List.of()))); }
  @GetMapping("/movers") ResponseEntity<?> movers(@RequestParam(defaultValue="gainers") String type, @RequestParam(defaultValue="NIFTY50") String universe){
    if (!type.equals("gainers") && !type.equals("losers")) return ResponseEntity.badRequest().body(Map.of("error","type must be gainers or losers"));
    var l = svc.movers(type, universe);
    return ResponseEntity.ok(Map.of("type", type, "universe", universe, "available", !l.isEmpty(), "items", l)); }
}
