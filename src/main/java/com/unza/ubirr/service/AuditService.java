package com.unza.ubirr.service;

import com.unza.ubirr.model.AppUser;
import com.unza.ubirr.model.AuditLog;
import com.unza.ubirr.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditService {
  private final AuditLogRepository auditLogRepository;

  public AuditService(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  public void log(AppUser user, String action, String entityType, String entityId) {
    log(user, action, entityType, entityId, null, null);
  }

  public void log(AppUser user, String action, String entityType, String entityId,
                  String oldValue, String newValue) {
    AuditLog entry = new AuditLog();
    entry.user = user;
    entry.action = action;
    entry.entityType = entityType;
    entry.entityId = entityId;
    entry.oldValue = oldValue;
    entry.newValue = newValue;
    entry.ipAddress = currentIp();
    entry.userAgent = currentUserAgent();
    auditLogRepository.save(entry);
  }

  private String currentIp() {
    try {
      HttpServletRequest req = currentRequest();
      if (req == null) return null;
      String xff = req.getHeader("X-Forwarded-For");
      return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : req.getRemoteAddr();
    } catch (Exception e) {
      return null;
    }
  }

  private String currentUserAgent() {
    try {
      HttpServletRequest req = currentRequest();
      return req != null ? req.getHeader("User-Agent") : null;
    } catch (Exception e) {
      return null;
    }
  }

  private HttpServletRequest currentRequest() {
    var attrs = RequestContextHolder.getRequestAttributes();
    if (attrs instanceof ServletRequestAttributes sra) return sra.getRequest();
    return null;
  }
}
