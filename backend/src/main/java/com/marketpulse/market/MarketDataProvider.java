package com.marketpulse.market;
import java.util.*;
/** Implement one per authorized vendor. Never return simulated values. */
public interface MarketDataProvider {
  String name();
  boolean supports(String symbol);
  Optional<Quote> quote(String symbol);
  default List<Quote> movers(String universe){ return List.of(); }
  /** range: 1D 5D 1M 3M 6M 1Y 5Y */
  default Optional<List<Candle>> history(String symbol, String range){ return Optional.empty(); }
}
