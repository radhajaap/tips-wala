package com.marketpulse.admin;
import org.aspectj.lang.ProceedingJoinPoint; import org.aspectj.lang.annotation.*; import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.stereotype.Component;
import org.springframework.web.context.request.*;
/** Records every POST/PUT/DELETE under /api/admin made through content controllers (AdminController logs its own, richer entries). */
@Aspect @Component
public class AdminAuditAspect {
  private final AuditService audit; AdminAuditAspect(AuditService a){ audit = a; }
  @Around("(@annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.PutMapping) || @annotation(org.springframework.web.bind.annotation.DeleteMapping)) && within(com.marketpulse..*Controller) && !within(com.marketpulse.admin.AdminController)")
  public Object around(ProceedingJoinPoint pjp) throws Throwable {
    Object r = pjp.proceed();
    if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a && a.getRequest().getRequestURI().startsWith("/api/admin")) {
      var auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null) audit.log(auth, "ADMIN_" + a.getRequest().getMethod() + "_" + pjp.getSignature().getName(), a.getRequest().getRequestURI()); }
    return r; }
}
