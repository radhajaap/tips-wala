package com.marketpulse.market;
import com.fasterxml.jackson.databind.*; import java.net.*; import java.nio.charset.StandardCharsets; import java.time.*; import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; import org.springframework.stereotype.Component; import org.springframework.web.client.RestClient;
/**
 * Generic adapter: point it at ANY vendor that returns JSON, map the fields with JSON pointers, done.
 * A quote without a parsable timestamp is rejected, so stale or undated data is never shown as live.
 * Only use sources whose licence allows showing the data to your users.
 */
@Component @ConditionalOnProperty(prefix = "app.market.vendor", name = "enabled", havingValue = "true")
public class ConfigurableJsonProvider implements MarketDataProvider {
  private final VendorProps p; private final ObjectMapper om; private final RestClient http = RestClient.create();
  public ConfigurableJsonProvider(VendorProps p, ObjectMapper om){ this.p = p; this.om = om; }
  public String name(){ return p.name() == null ? "Vendor" : p.name(); }
  public boolean supports(String s){ return p.symbols() != null && p.symbols().containsKey(s); }
  private static boolean blank(String s){ return s == null || s.isBlank(); }
  private static String enc(String s){ return URLEncoder.encode(s, StandardCharsets.UTF_8); }
  private JsonNode get(String url) throws Exception {
    RestClient.RequestHeadersSpec<?> spec = http.get().uri(URI.create(url));
    if (!blank(p.apiKeyHeader()) && !blank(p.apiKey())) spec.header(p.apiKeyHeader(), p.apiKey());
    return om.readTree(spec.retrieve().body(String.class)); }
  private String f(String k){ return p.fields() == null ? null : p.fields().get(k); }
  private static double or0(double d){ return Double.isNaN(d) ? 0 : d; }
  private static double num(JsonNode n, String ptr){ if (ptr == null) return Double.NaN; JsonNode v = n.at(ptr); return v.isMissingNode() || v.isNull() ? Double.NaN : v.asDouble(Double.NaN); }
  private static String text(JsonNode n, String ptr){ if (ptr == null) return null; JsonNode v = n.at(ptr); return v.isMissingNode() || v.isNull() ? null : v.asText(); }
  private Instant toInstant(JsonNode v){
    if (v == null || v.isMissingNode() || v.isNull()) return null;
    try { return switch (p.timeFormat() == null ? "ISO" : p.timeFormat()) {
      case "EPOCH_MS" -> Instant.ofEpochMilli(v.asLong()); case "EPOCH_S" -> Instant.ofEpochSecond(v.asLong()); default -> OffsetDateTime.parse(v.asText()).toInstant(); };
    } catch (Exception e) { return null; } }
  Optional<Quote> parse(JsonNode n, String sym, String name){
    double price = num(n, f("price")), prev = num(n, f("prevClose")); Instant t = f("time") == null ? null : toInstant(n.at(f("time")));
    if (Double.isNaN(price) || Double.isNaN(prev) || t == null) return Optional.empty();
    return Optional.of(new Quote(sym, name, price, prev, or0(num(n, f("open"))), or0(num(n, f("high"))), or0(num(n, f("low"))), (long) or0(num(n, f("volume"))), t, p.delayed() ? "DELAYED" : "LIVE", name())); }
  public Optional<Quote> quote(String sym){
    if (blank(p.quoteUrl())) return Optional.empty();
    try { JsonNode root = get(p.quoteUrl().replace("{symbol}", enc(p.symbols().get(sym))));
      return parse(root.at(blank(p.quotePath()) ? "" : p.quotePath()), sym, MarketService.INSTRUMENTS.getOrDefault(sym, sym)); } catch (Exception e) { return Optional.empty(); } }
  @Override public List<Quote> movers(String universe){
    if (blank(p.moversUrl())) return List.of();
    try { var out = new ArrayList<Quote>();
      for (JsonNode n : get(p.moversUrl().replace("{universe}", enc(universe))).at(blank(p.listPath()) ? "" : p.listPath())) {
        String sym = text(n, f("symbol")); if (sym == null) continue; parse(n, sym, Optional.ofNullable(text(n, f("name"))).orElse(sym)).ifPresent(out::add); }
      return out; } catch (Exception e) { return List.of(); } }
  @Override public List<Map<String,String>> search(String q){
    if (blank(p.searchUrl())) return List.of();
    try { var out = new ArrayList<Map<String,String>>();
      for (JsonNode n : get(p.searchUrl().replace("{q}", enc(q))).at(blank(p.listPath()) ? "" : p.listPath())) {
        String sym = text(n, f("symbol")); if (sym != null) out.add(Map.of("symbol", sym, "name", Optional.ofNullable(text(n, f("name"))).orElse(sym))); }
      return out; } catch (Exception e) { return List.of(); } }
  @Override public Optional<List<Candle>> history(String sym, String range){
    if (blank(p.historyUrl()) || !supports(sym) || p.candleFields() == null) return Optional.empty();
    try { var cf = p.candleFields(); var out = new ArrayList<Candle>();
      for (JsonNode n : get(p.historyUrl().replace("{symbol}", enc(p.symbols().get(sym))).replace("{range}", enc(range))).at(blank(p.candlesPath()) ? "" : p.candlesPath())) {
        Instant t = toInstant(n.at(cf.getOrDefault("t", "/t"))); double o = num(n, cf.get("o")), h = num(n, cf.get("h")), l = num(n, cf.get("l")), c = num(n, cf.get("c"));
        if (t == null || Double.isNaN(o) || Double.isNaN(h) || Double.isNaN(l) || Double.isNaN(c)) continue; out.add(new Candle(t.toEpochMilli(), o, h, l, c, or0(num(n, cf.get("v"))))); }
      return out.isEmpty() ? Optional.empty() : Optional.of(out); } catch (Exception e) { return Optional.empty(); } }
}
