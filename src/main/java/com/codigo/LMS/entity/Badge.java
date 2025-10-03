package com.codigo.LMS.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "badges")
public class Badge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Badge name is required")
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "icon_url")
    private String iconUrl;
    
    @Column(name = "color_code")
    private String colorCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "badge_tier")
    private BadgeTier badgeTier = BadgeTier.BRONZE;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "badge_category")
    private BadgeCategory badgeCategory;
    
    @Column(name = "criteria_description", columnDefinition = "TEXT")
    private String criteriaDescription;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "badge", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserBadge> userBadges = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public Badge() {}
    
    public Badge(String name, String description, BadgeTier badgeTier, BadgeCategory badgeCategory) {
        this.name = name;
        this.description = description;
        this.badgeTier = badgeTier;
        this.badgeCategory = badgeCategory;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIconUrl() {
        return iconUrl;
    }
    
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
    
    public String getColorCode() {
        return colorCode;
    }
    
    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }
    
    public BadgeTier getBadgeTier() {
        return badgeTier;
    }
    
    public void setBadgeTier(BadgeTier badgeTier) {
        this.badgeTier = badgeTier;
    }
    
    public BadgeCategory getBadgeCategory() {
        return badgeCategory;
    }
    
    public void setBadgeCategory(BadgeCategory badgeCategory) {
        this.badgeCategory = badgeCategory;
    }
    
    public String getCriteriaDescription() {
        return criteriaDescription;
    }
    
    public void setCriteriaDescription(String criteriaDescription) {
        this.criteriaDescription = criteriaDescription;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Set<UserBadge> getUserBadges() {
        return userBadges;
    }
    
    public void setUserBadges(Set<UserBadge> userBadges) {
        this.userBadges = userBadges;
    }
    
    public enum BadgeTier {
        BRONZE("Bronze", "#CD7F32"),
        SILVER("Silver", "#C0C0C0"),
        GOLD("Gold", "#FFD700"),
        PLATINUM("Platinum", "#E5E4E2"),
        DIAMOND("Diamond", "#B9F2FF");
        
        private final String displayName;
        private final String defaultColor;
        
        BadgeTier(String displayName, String defaultColor) {
            this.displayName = displayName;
            this.defaultColor = defaultColor;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDefaultColor() {
            return defaultColor;
        }
    }
    
    public enum BadgeCategory {
        LEARNING("Learning"),
        ACHIEVEMENT("Achievement"),
        PARTICIPATION("Participation"),
        SOCIAL("Social"),
        MILESTONE("Milestone"),
        SPECIAL("Special");
        
        private final String displayName;
        
        BadgeCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}