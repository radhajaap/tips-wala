package com.marketpulse.notify;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean; import org.springframework.stereotype.Component;
@Component @ConditionalOnMissingBean(value = SmsSender.class, ignored = DisabledSmsSender.class)
public class DisabledSmsSender implements SmsSender { public void send(String m, String t){ throw new IllegalStateException("SMS provider not configured"); } }
