package com.codigo.LMS.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_badges",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "badge_id"}))
public class UserBadge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;
    
    @Column(name = "earned_at")
    private LocalDateTime earnedAt;
    
    @Column(name = "is_displayed")
    private Boolean isDisplayed = true;
    
    @Column(name = "earned_reason")
    private String earnedReason;
    
    @PrePersist
    protected void onCreate() {
        earnedAt = LocalDateTime.now();
    }
    
    // Constructors
    public UserBadge() {}
    
    public UserBadge(User user, Badge badge) {
        this.user = user;
        this.badge = badge;
    }
    
    public UserBadge(User user, Badge badge, String earnedReason) {
        this.user = user;
        this.badge = badge;
        this.earnedReason = earnedReason;
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
    
    public Badge getBadge() {
        return badge;
    }
    
    public void setBadge(Badge badge) {
        this.badge = badge;
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
    
    public String getEarnedReason() {
        return earnedReason;
    }
    
    public void setEarnedReason(String earnedReason) {
        this.earnedReason = earnedReason;
    }
}