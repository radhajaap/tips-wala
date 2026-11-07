package com.marketpulse.market;
import java.util.*; import org.springframework.web.bind.annotation.*;
/** No fake prices: until an authorized provider is wired in, every instrument reports UNAVAILABLE. */
@RestController @RequestMapping("/api/market")
public class MarketController {
  private static final List<String> SYMBOLS = List.of("NIFTY50","BANKNIFTY","MCXGOLD","MCXSILVER","BTC");
  @GetMapping("/overview") List<Map<String,Object>> overview(){
    return SYMBOLS.stream().map(s -> Map.<String,Object>of("symbol", s, "status", "UNAVAILABLE", "lastUpdated", "")).toList(); }
}
