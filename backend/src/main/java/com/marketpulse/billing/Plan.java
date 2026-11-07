package com.marketpulse.billing;
/** Prices live ONLY on the server; the client never sends an amount. */
public enum Plan {
  MONTHLY(49900, 1), HALF_YEARLY(250000, 6), YEARLY(450000, 12);
  public final long paise; public final int months;
  Plan(long p, int m){ paise=p; months=m; }
}
