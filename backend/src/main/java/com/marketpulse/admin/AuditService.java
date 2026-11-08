package com.marketpulse.admin;
import java.util.UUID; import org.springframework.security.core.Authentication; import org.springframework.stereotype.Service;
@Service
public class AuditService {
  private final AuditLogRepository repo; AuditService(AuditLogRepository r){ repo = r; }
  public void log(Authentication a, String action, String detail){ var l = new AuditLog(); l.actorId = a == null ? null : UUID.fromString(a.getName()); l.action = action; l.detail = detail; repo.save(l); }
}
