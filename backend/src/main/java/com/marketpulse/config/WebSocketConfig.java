package com.marketpulse.config;
import com.marketpulse.market.MarketWsHandler;
import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
@Configuration @EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
  private final MarketWsHandler h; private final String[] origins;
  WebSocketConfig(MarketWsHandler h, @Value("${app.cors.origins}") String[] o){ this.h = h; origins = o; }
  @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry r){ r.addHandler(h, "/ws/market").setAllowedOrigins(origins); }
}
