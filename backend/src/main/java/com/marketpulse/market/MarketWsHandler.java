package com.marketpulse.market;
import com.fasterxml.jackson.databind.ObjectMapper; import java.io.IOException; import java.util.Set; import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component; import org.springframework.web.socket.*; import org.springframework.web.socket.handler.TextWebSocketHandler;
@Component
public class MarketWsHandler extends TextWebSocketHandler {
  private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet(); private final ObjectMapper om;
  MarketWsHandler(ObjectMapper om){ this.om = om; }
  @Override public void afterConnectionEstablished(WebSocketSession s){ sessions.add(s); }
  @Override public void afterConnectionClosed(WebSocketSession s, CloseStatus st){ sessions.remove(s); }
  public void broadcast(Quote q){
    try { TextMessage msg = new TextMessage(om.writeValueAsString(q));
      for (var s : sessions) { try { synchronized (s) { if (s.isOpen()) s.sendMessage(msg); } } catch (IOException e) { sessions.remove(s); } }
    } catch (Exception ignored) {} }
}
