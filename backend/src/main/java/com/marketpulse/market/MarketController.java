package com.marketpulse.market;
import java.util.*; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/market")
public class MarketController {
  private final MarketService svc; MarketController(MarketService s){ svc = s; }
  @GetMapping("/overview") List<Quote> overview(){ return svc.overview(); }
  @GetMapping("/movers") ResponseEntity<?> movers(@RequestParam(defaultValue="gainers") String type, @RequestParam(defaultValue="NIFTY50") String universe){
    if (!type.equals("gainers") && !type.equals("losers")) return ResponseEntity.badRequest().body(Map.of("error","type must be gainers or losers"));
    var l = svc.movers(type, universe);
    return ResponseEntity.ok(Map.of("type", type, "universe", universe, "available", !l.isEmpty(), "items", l)); }
}
