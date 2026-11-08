package com.marketpulse.notify;
import com.google.auth.oauth2.GoogleCredentials; import com.google.firebase.*; import com.google.firebase.messaging.*;
import java.io.FileInputStream; import java.util.List; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
@Service
public class PushService {
  private final boolean on;
  PushService(@Value("${app.firebase.credentials-file:}") String file){
    boolean ok = false;
    if (!file.isBlank()) { try (var in = new FileInputStream(file)) {
      if (FirebaseApp.getApps().isEmpty()) FirebaseApp.initializeApp(FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(in)).build());
      ok = true; } catch (Exception e) { /* push disabled */ } }
    on = ok; }
  public boolean enabled(){ return on; }
  public int send(PushNotice n, List<String> tokens){
    if (!on || tokens.isEmpty()) return 0; int sent = 0;
    for (int i = 0; i < tokens.size(); i += 500) {
      var nb = Notification.builder().setTitle(n.title).setBody(n.message); if (n.imageUrl != null && !n.imageUrl.isBlank()) nb.setImage(n.imageUrl);
      var mb = MulticastMessage.builder().addAllTokens(tokens.subList(i, Math.min(i + 500, tokens.size()))).setNotification(nb.build());
      if (n.deepLink != null && !n.deepLink.isBlank()) mb.putData("deepLink", n.deepLink);
      try { sent += FirebaseMessaging.getInstance().sendEachForMulticast(mb.build()).getSuccessCount(); } catch (Exception e) { /* count stays */ } }
    return sent; }
}
