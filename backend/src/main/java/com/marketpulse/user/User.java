package com.marketpulse.user;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="users")
public class User {
  @Id public UUID id = UUID.randomUUID();
  public String fullName, email, mobile, passwordHash, role = "USER";
  public boolean trialUsed, emailVerified, mobileVerified, deleteRequested, notifyPush = true, notifyEmail = true;
  public Instant createdAt = Instant.now();
}
