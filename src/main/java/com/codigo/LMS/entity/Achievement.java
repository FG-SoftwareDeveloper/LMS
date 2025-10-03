package com.codigo.LMS.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "achievements")
public class Achievement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Achievement name is required")
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "icon_url")
    private String iconUrl;
    
    @NotNull
    @Column(name = "points_required", nullable = false)
    private Integer pointsRequired;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_type")
    private AchievementType achievementType;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "achievement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserAchievement> userAchievements = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public Achievement() {}
    
    public Achievement(String name, String description, Integer pointsRequired, AchievementType achievementType) {
        this.name = name;
        this.description = description;
        this.pointsRequired = pointsRequired;
        this.achievementType = achievementType;
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
    
    public Integer getPointsRequired() {
        return pointsRequired;
    }
    
    public void setPointsRequired(Integer pointsRequired) {
        this.pointsRequired = pointsRequired;
    }
    
    public AchievementType getAchievementType() {
        return achievementType;
    }
    
    public void setAchievementType(AchievementType achievementType) {
        this.achievementType = achievementType;
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
    
    public Set<UserAchievement> getUserAchievements() {
        return userAchievements;
    }
    
    public void setUserAchievements(Set<UserAchievement> userAchievements) {
        this.userAchievements = userAchievements;
    }
    
    public enum AchievementType {
        COURSE_COMPLETION("Course Completion"),
        POINTS_MILESTONE("Points Milestone"),
        STREAK("Learning Streak"),
        PARTICIPATION("Participation"),
        EXCELLENCE("Excellence"),
        SOCIAL("Social"),
        SPECIAL("Special Event");
        
        private final String displayName;
        
        AchievementType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}