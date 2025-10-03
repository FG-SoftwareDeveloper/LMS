package com.codigo.LMS.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_actor", columnList = "actor_id"),
    @Index(name = "idx_audit_entity", columnList = "entity_type,entity_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action", columnList = "action")
})
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "actor_id")
    private Long actorId;
    
    @Column(name = "actor_username")
    private String actorUsername;
    
    @Column(name = "actor_role")
    private String actorRole;
    
    @NotBlank
    @Column(name = "action", nullable = false)
    private String action;
    
    @NotBlank
    @Column(name = "entity_type", nullable = false)
    private String entityType;
    
    @NotNull
    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    
    @Column(name = "target_user_id")
    private Long targetUserId;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @NotNull
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;
    
    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;
    
    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;
    
    @Column(name = "success")
    private Boolean success = true;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
    
    // Constructors
    public AuditLog() {}
    
    public AuditLog(Long actorId, String action, String entityType, Long entityId) {
        this.actorId = actorId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.timestamp = LocalDateTime.now();
    }
    
    // Static factory methods for common audit events
    public static AuditLog enrollmentCreated(Long actorId, Long enrollmentId, String payloadJson) {
        AuditLog log = new AuditLog(actorId, "ENROLLMENT_CREATED", "enrollment", enrollmentId);
        log.setPayloadJson(payloadJson);
        return log;
    }
    
    public static AuditLog enrollmentApproved(Long actorId, Long enrollmentId, Long targetUserId) {
        AuditLog log = new AuditLog(actorId, "ENROLLMENT_APPROVED", "enrollment", enrollmentId);
        log.setTargetUserId(targetUserId);
        return log;
    }
    
    public static AuditLog enrollmentDenied(Long actorId, Long enrollmentId, Long targetUserId, String reason) {
        AuditLog log = new AuditLog(actorId, "ENROLLMENT_DENIED", "enrollment", enrollmentId);
        log.setTargetUserId(targetUserId);
        log.setPayloadJson("{\"denial_reason\":\"" + reason + "\"}");
        return log;
    }
    
    public static AuditLog paymentProcessed(Long actorId, Long paymentId, String status) {
        AuditLog log = new AuditLog(actorId, "PAYMENT_" + status.toUpperCase(), "payment", paymentId);
        return log;
    }
    
    public static AuditLog voucherUsed(Long actorId, Long voucherId, String voucherCode) {
        AuditLog log = new AuditLog(actorId, "VOUCHER_USED", "voucher", voucherId);
        log.setPayloadJson("{\"voucher_code\":\"" + voucherCode + "\"}");
        return log;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }
    
    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }
    
    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    
    public String getOldValues() { return oldValues; }
    public void setOldValues(String oldValues) { this.oldValues = oldValues; }
    
    public String getNewValues() { return newValues; }
    public void setNewValues(String newValues) { this.newValues = newValues; }
    
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}