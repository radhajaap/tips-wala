package com.marketpulse.market;
import java.time.Instant;
public record Quote(String symbol, String name, double price, double prevClose, double open, double high, double low,
                    long volume, Instant asOf, String status, String source) {
  public double changePct(){ return prevClose > 0 ? (price - prevClose) / prevClose * 100 : 0; }
  public static Quote unavailable(String s, String n){ return new Quote(s,n,0,0,0,0,0,0,null,"UNAVAILABLE","none"); }
  public Quote withStatus(String st){ return new Quote(symbol,name,price,prevClose,open,high,low,volume,asOf,st,source); }
}
