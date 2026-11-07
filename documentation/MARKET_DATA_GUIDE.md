# Market data guide
- BTC works out of the box via `BinanceBtcProvider` (USDT, 24h window). Check the exchange API terms for commercial use; for INR pricing add an INR-quoting provider.
- NIFTY 50, BANK NIFTY, MCX Gold/Silver and Top 25 gainers/losers need an **authorized NSE/MCX data vendor with redistribution rights** (NSE/MCX data licensing or a licensed vendor). Personal-use broker APIs usually do not allow redistribution.
- To add a vendor: implement `MarketDataProvider` (`supports`, `quote`, `movers`), annotate with `@Component`. Nothing else changes: caching, dedupe, stale detection and WebSocket broadcast are in `MarketService`.
- Until a provider supports a symbol, the API returns `UNAVAILABLE` and the UI says so. No simulated prices.
