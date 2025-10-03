package com.codigo.LMS.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "lesson_id"}))
public class UserProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProgressStatus status = ProgressStatus.NOT_STARTED;
    
    @Column(name = "completion_percentage")
    private Double completionPercentage = 0.0;
    
    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes = 0;
    
    @Column(name = "points_earned")
    private Integer pointsEarned = 0;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;
    
    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        lastAccessedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastAccessedAt = LocalDateTime.now();
        if (status == ProgressStatus.COMPLETED && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
    
    // Constructors
    public UserProgress() {}
    
    public UserProgress(User user, Lesson lesson) {
        this.user = user;
        this.lesson = lesson;
    }
    
    // Business methods
    public void markAsStarted() {
        if (status == ProgressStatus.NOT_STARTED) {
            status = ProgressStatus.IN_PROGRESS;
            if (startedAt == null) {
                startedAt = LocalDateTime.now();
            }
        }
    }
    
    public void markAsCompleted() {
        status = ProgressStatus.COMPLETED;
        completionPercentage = 100.0;
        if (completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
    
    public void updateProgress(double percentage) {
        this.completionPercentage = Math.min(100.0, Math.max(0.0, percentage));
        if (percentage > 0 && status == ProgressStatus.NOT_STARTED) {
            markAsStarted();
        }
        if (percentage >= 100.0) {
            markAsCompleted();
        }
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
    
    public Lesson getLesson() {
        return lesson;
    }
    
    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }
    
    public ProgressStatus getStatus() {
        return status;
    }
    
    public void setStatus(ProgressStatus status) {
        this.status = status;
    }
    
    public Double getCompletionPercentage() {
        return completionPercentage;
    }
    
    public void setCompletionPercentage(Double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }
    
    public Integer getTimeSpentMinutes() {
        return timeSpentMinutes;
    }
    
    public void setTimeSpentMinutes(Integer timeSpentMinutes) {
        this.timeSpentMinutes = timeSpentMinutes;
    }
    
    public Integer getPointsEarned() {
        return pointsEarned;
    }
    
    public void setPointsEarned(Integer pointsEarned) {
        this.pointsEarned = pointsEarned;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }
    
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }
    
    public enum ProgressStatus {
        NOT_STARTED("Not Started"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        PAUSED("Paused");
        
        private final String displayName;
        
        ProgressStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}