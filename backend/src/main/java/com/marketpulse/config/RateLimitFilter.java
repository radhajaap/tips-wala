package com.marketpulse.config;
import jakarta.servlet.*; import jakarta.servlet.http.*; import java.io.IOException; import java.util.Map; import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter;
/** Simple per-IP fixed window limiter (per instance). Use Redis-based limiting when running several instances. */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
  private final Map<String,long[]> win = new ConcurrentHashMap<>();
  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
    String p = req.getRequestURI();
    if (!p.startsWith("/api/")) { chain.doFilter(req,res); return; }
    boolean strict = p.startsWith("/api/auth") || p.equals("/api/contact");
    long minute = System.currentTimeMillis() / 60000; if (win.size() > 10000) win.clear();
    long[] w = win.compute(req.getRemoteAddr() + (strict ? "|s" : "|g"), (k,v) -> (v == null || v[0] != minute) ? new long[]{minute,1} : new long[]{minute, v[1]+1});
    if (w[1] > (strict ? 10 : 120)) { res.setStatus(429); res.setContentType("application/json"); res.getWriter().write("{\"error\":\"Too many requests\"}"); return; }
    chain.doFilter(req,res); }
}
