package com.marketpulse.notify;
import org.slf4j.*; import org.springframework.beans.factory.ObjectProvider; import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage; import org.springframework.mail.javamail.JavaMailSender; import org.springframework.scheduling.annotation.Async; import org.springframework.stereotype.Service;
@Service
public class EmailService {
  private static final Logger log = LoggerFactory.getLogger(EmailService.class);
  private final ObjectProvider<JavaMailSender> mail; private final String from;
  EmailService(ObjectProvider<JavaMailSender> m, @Value("${spring.mail.username:}") String f){ mail = m; from = f; }
  @Async public void send(String to, String subject, String body){
    var s = mail.getIfAvailable(); if (s == null) return;
    try { var m = new SimpleMailMessage(); if (!from.isBlank()) m.setFrom(from); m.setTo(to); m.setSubject(subject); m.setText(body); s.send(m); }
    catch (Exception e) { log.warn("Email to {} failed: {}", to, e.getMessage()); } }
}
