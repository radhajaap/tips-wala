package com.marketpulse.market;
import java.util.*;
/** Implement one per authorized vendor. Never return simulated values. */
public interface MarketDataProvider {
  String name();
  boolean supports(String symbol);
  Optional<Quote> quote(String symbol);
  default List<Quote> movers(String universe){ return List.of(); }
}
