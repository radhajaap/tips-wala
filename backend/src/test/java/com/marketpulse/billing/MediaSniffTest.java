package com.marketpulse.billing;
import static org.junit.jupiter.api.Assertions.*; import org.junit.jupiter.api.Test;
class MediaSniffTest {
  @Test void typeComesFromBytes(){
    assertEquals("image/jpeg", com.marketpulse.content.MediaTypes.of(new byte[]{(byte)0xFF,(byte)0xD8,(byte)0xFF,0,0,0,0,0,0,0,0,0}));
    assertNull(com.marketpulse.content.MediaTypes.of("<svg onload=alert(1)>".getBytes())); }
}
