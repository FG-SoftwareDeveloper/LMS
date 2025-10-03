package com.codigo.LMS.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
public class Voucher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;
    
    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;
    
    @NotNull
    @Positive
    @Column(name = "discount_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal value;
    
    @Column(name = "max_uses")
    private Integer maxUses;
    
    @Column(name = "uses")
    private Integer uses = 0;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "applies_to_course_id")
    private Long appliesToCourseId;
    
    @Column(name = "applies_to_track_id")
    private Long appliesToTrackId;
    
    @Column(name = "applies_to_all")
    private Boolean appliesToAll = false;
    
    @Column(name = "org_id")
    private Long orgId;
    
    @Column(name = "minimum_amount", precision = 10, scale = 2)
    private BigDecimal minimumAmount;
    
    @Column(name = "maximum_discount", precision = 10, scale = 2)
    private BigDecimal maximumDiscount;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "first_time_users_only")
    private Boolean firstTimeUsersOnly = false;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Voucher() {}
    
    public Voucher(String code, String name, DiscountType discountType, BigDecimal value) {
        this.code = code;
        this.name = name;
        this.discountType = discountType;
        this.value = value;
    }
    
    // Business methods
    public boolean isValid() {
        return isActive && 
               (expiresAt == null || expiresAt.isAfter(LocalDateTime.now())) &&
               (maxUses == null || uses < maxUses);
    }
    
    public boolean canBeUsedForAmount(BigDecimal amount) {
        return minimumAmount == null || amount.compareTo(minimumAmount) >= 0;
    }
    
    public BigDecimal calculateDiscount(BigDecimal originalAmount) {
        if (!isValid() || !canBeUsedForAmount(originalAmount)) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discount;
        if (discountType == DiscountType.PERCENT) {
            discount = originalAmount.multiply(value.divide(BigDecimal.valueOf(100)));
        } else {
            discount = value;
        }
        
        // Apply maximum discount limit if set
        if (maximumDiscount != null && discount.compareTo(maximumDiscount) > 0) {
            discount = maximumDiscount;
        }
        
        // Discount cannot exceed original amount
        if (discount.compareTo(originalAmount) > 0) {
            discount = originalAmount;
        }
        
        return discount;
    }
    
    public void incrementUsage() {
        this.uses = (this.uses == null ? 0 : this.uses) + 1;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
    
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    
    public Integer getMaxUses() { return maxUses; }
    public void setMaxUses(Integer maxUses) { this.maxUses = maxUses; }
    
    public Integer getUses() { return uses; }
    public void setUses(Integer uses) { this.uses = uses; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public Long getAppliesToCourseId() { return appliesToCourseId; }
    public void setAppliesToCourseId(Long appliesToCourseId) { this.appliesToCourseId = appliesToCourseId; }
    
    public Long getAppliesToTrackId() { return appliesToTrackId; }
    public void setAppliesToTrackId(Long appliesToTrackId) { this.appliesToTrackId = appliesToTrackId; }
    
    public Boolean getAppliesToAll() { return appliesToAll; }
    public void setAppliesToAll(Boolean appliesToAll) { this.appliesToAll = appliesToAll; }
    
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    
    public BigDecimal getMinimumAmount() { return minimumAmount; }
    public void setMinimumAmount(BigDecimal minimumAmount) { this.minimumAmount = minimumAmount; }
    
    public BigDecimal getMaximumDiscount() { return maximumDiscount; }
    public void setMaximumDiscount(BigDecimal maximumDiscount) { this.maximumDiscount = maximumDiscount; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getFirstTimeUsersOnly() { return firstTimeUsersOnly; }
    public void setFirstTimeUsersOnly(Boolean firstTimeUsersOnly) { this.firstTimeUsersOnly = firstTimeUsersOnly; }
    
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}