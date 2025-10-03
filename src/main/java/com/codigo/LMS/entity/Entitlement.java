package com.codigo.LMS.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "entitlements", indexes = {
    @Index(name = "idx_entitlement_user", columnList = "user_id"),
    @Index(name = "idx_entitlement_resource", columnList = "resource_type,resource_id"),
    @Index(name = "idx_entitlement_enrollment", columnList = "granted_by_enrollment_id")
})
public class Entitlement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;
    
    @NotNull
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;
    
    @Column(name = "granted_by_enrollment_id")
    private Long grantedByEnrollmentId;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EntitlementStatus status = EntitlementStatus.ACTIVE;
    
    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
    
    @Column(name = "revoked_by")
    private Long revokedBy;
    
    @Column(name = "revoke_reason")
    private String revokeReason;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    @PrePersist
    protected void onCreate() {
        grantedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Entitlement() {}
    
    public Entitlement(User user, ResourceType resourceType, Long resourceId, Long grantedByEnrollmentId) {
        this.user = user;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.grantedByEnrollmentId = grantedByEnrollmentId;
    }
    
    // Business methods
    public boolean isActive() {
        return status == EntitlementStatus.ACTIVE && 
               (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }
    
    public void revoke(Long revokedBy, String reason) {
        this.status = EntitlementStatus.REVOKED;
        this.revokedAt = LocalDateTime.now();
        this.revokedBy = revokedBy;
        this.revokeReason = reason;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }
    
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    
    public Long getGrantedByEnrollmentId() { return grantedByEnrollmentId; }
    public void setGrantedByEnrollmentId(Long grantedByEnrollmentId) { this.grantedByEnrollmentId = grantedByEnrollmentId; }
    
    public EntitlementStatus getStatus() { return status; }
    public void setStatus(EntitlementStatus status) { this.status = status; }
    
    public LocalDateTime getGrantedAt() { return grantedAt; }
    public void setGrantedAt(LocalDateTime grantedAt) { this.grantedAt = grantedAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
    
    public Long getRevokedBy() { return revokedBy; }
    public void setRevokedBy(Long revokedBy) { this.revokedBy = revokedBy; }
    
    public String getRevokeReason() { return revokeReason; }
    public void setRevokeReason(String revokeReason) { this.revokeReason = revokeReason; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}