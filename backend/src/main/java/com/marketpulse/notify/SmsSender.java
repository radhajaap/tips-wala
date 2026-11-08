package com.marketpulse.notify;
/** Implement with a DLT-registered Indian SMS provider (MSG91, Twilio, etc.). */
public interface SmsSender { void send(String mobile, String text); }
