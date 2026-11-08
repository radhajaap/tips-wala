# Live NIFTY / MCX / gainers & losers: how to connect a data source
The platform never shows prices unless a source supplies them with a timestamp. Until then cards say UNAVAILABLE.

1. **Get a source you are allowed to use.** Display of NSE/MCX data to your users needs a licence from the exchange or an authorized data vendor. Free/unofficial endpoints, scraped pages, and broker APIs meant for personal trading usually forbid redistribution. If you found a GitHub project that "gives data", check **where it gets the data**: if it scrapes NSE/Yahoo/Google, do not use it for a paid public product.
2. **Ask the vendor for**: quote API (index + MCX symbols), a "list all stocks of universe with LTP, prev close, volume" API (for gainers/losers; NIFTY 50/Next 50/100/500/NSE), optional search and candle history APIs, plus the licence for public display.
3. **Configure**: copy `config/application.example.yml` to `config/application.yml`, fill URLs and JSON pointers, put the key in `VENDOR_API_KEY` in `.env`. Restart the backend. No code changes needed for JSON-over-HTTP vendors.
4. **Check**: `GET /api/market/overview` shows `status` LIVE/DELAYED, `GET /api/market/movers?type=gainers&universe=NIFTY50` returns the top 25, `GET /api/market/status` shows market OPEN/CLOSED (needs trading holidays in Admin → Calendar, type HOLIDAY).
5. If the vendor streams over WebSocket/FIX, write a small `MarketDataProvider` class instead (see `BinanceBtcProvider`).
Rules built in: undated quotes are dropped; quotes older than 60 s show STALE; duplicate ticks are ignored; gainers need change > 0, losers < 0; suspended/invalid instruments (no price or prev close) are excluded.
