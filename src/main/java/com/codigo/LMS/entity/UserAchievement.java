package com.codigo.LMS.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_id"}))
public class UserAchievement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;
    
    @Column(name = "earned_at")
    private LocalDateTime earnedAt;
    
    @Column(name = "is_displayed")
    private Boolean isDisplayed = true;
    
    @PrePersist
    protected void onCreate() {
        earnedAt = LocalDateTime.now();
    }
    
    // Constructors
    public UserAchievement() {}
    
    public UserAchievement(User user, Achievement achievement) {
        this.user = user;
        this.achievement = achievement;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Achievement getAchievement() {
        return achievement;
    }
    
    public void setAchievement(Achievement achievement) {
        this.achievement = achievement;
    }
    
    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }
    
    public void setEarnedAt(LocalDateTime earnedAt) {
        this.earnedAt = earnedAt;
    }
    
    public Boolean getIsDisplayed() {
        return isDisplayed;
    }
    
    public void setIsDisplayed(Boolean isDisplayed) {
        this.isDisplayed = isDisplayed;
    }
    
    // Alias method for compatibility
    public void setUnlockedAt(LocalDateTime unlockedAt) {
        this.earnedAt = unlockedAt;
    }
    
    public LocalDateTime getUnlockedAt() {
        return this.earnedAt;
    }
}