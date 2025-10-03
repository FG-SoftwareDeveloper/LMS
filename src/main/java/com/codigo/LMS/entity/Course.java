package com.codigo.LMS.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Course title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "Course description is required")
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "short_description")
    private String shortDescription;
    
    @NotBlank(message = "Course category is required")
    private String category;
    
    @Column(name = "difficulty_level")
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel = DifficultyLevel.BEGINNER;
    
    @Column(name = "estimated_duration_hours")
    private Integer estimatedDurationHours;
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @Column(name = "is_published")
    private Boolean isPublished = false;
    
    @Column(name = "enrollment_count")
    private Integer enrollmentCount = 0;
    
    @Column(name = "rating")
    private Double rating = 0.0;
    
    // Enrollment related fields
    @Column(name = "price", precision = 10, scale = 2)
    private java.math.BigDecimal price = java.math.BigDecimal.ZERO;
    
    @Column(name = "currency", length = 3)
    private String currency = "USD";
    
    @Column(name = "capacity")
    private Integer capacity;
    
    @Column(name = "requires_approval")
    private Boolean requiresApproval = false;
    
    @Column(name = "enrollment_window_start")
    private LocalDateTime enrollmentWindowStart;
    
    @Column(name = "enrollment_window_end")
    private LocalDateTime enrollmentWindowEnd;
    
    @Column(name = "cohort_id")
    private String cohortId;
    
    @ElementCollection
    @CollectionTable(name = "course_prerequisites", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "prerequisite_course_id")
    private List<Long> prerequisiteCourseIds = new ArrayList<>();
    
    @Column(name = "enrollment_policy")
    @Enumerated(EnumType.STRING)
    private EnrollmentPolicy enrollmentPolicy = EnrollmentPolicy.OPEN;
    
    @Column(name = "refund_policy_days")
    private Integer refundPolicyDays = 30;
    
    @Column(name = "max_progress_for_refund")
    private Double maxProgressForRefund = 0.25; // 25%
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private User instructor;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enrollment> enrollments = new ArrayList<>();
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Module> modules = new ArrayList<>();
    
    @Column(name = "created_at")
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
    public Course() {}
    
    public Course(String title, String description, String category, User instructor) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.instructor = instructor;
    }
    
    public Course(String title, String description, User instructor) {
        this.title = title;
        this.description = description;
        this.instructor = instructor;
        this.category = "General"; // Default category
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getShortDescription() {
        return shortDescription;
    }
    
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }
    
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
    
    public Integer getEstimatedDurationHours() {
        return estimatedDurationHours;
    }
    
    public void setEstimatedDurationHours(Integer estimatedDurationHours) {
        this.estimatedDurationHours = estimatedDurationHours;
    }
    
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    public Boolean getIsPublished() {
        return isPublished;
    }
    
    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }
    
    public Integer getEnrollmentCount() {
        return enrollmentCount;
    }
    
    public void setEnrollmentCount(Integer enrollmentCount) {
        this.enrollmentCount = enrollmentCount;
    }
    
    public Double getRating() {
        return rating;
    }
    
    public void setRating(Double rating) {
        this.rating = rating;
    }
    
    public java.math.BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(java.math.BigDecimal price) {
        this.price = price;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    public Boolean getRequiresApproval() {
        return requiresApproval;
    }
    
    public void setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }
    
    public LocalDateTime getEnrollmentWindowStart() {
        return enrollmentWindowStart;
    }
    
    public void setEnrollmentWindowStart(LocalDateTime enrollmentWindowStart) {
        this.enrollmentWindowStart = enrollmentWindowStart;
    }
    
    public LocalDateTime getEnrollmentWindowEnd() {
        return enrollmentWindowEnd;
    }
    
    public void setEnrollmentWindowEnd(LocalDateTime enrollmentWindowEnd) {
        this.enrollmentWindowEnd = enrollmentWindowEnd;
    }
    
    public String getCohortId() {
        return cohortId;
    }
    
    public void setCohortId(String cohortId) {
        this.cohortId = cohortId;
    }
    
    public List<Long> getPrerequisiteCourseIds() {
        return prerequisiteCourseIds;
    }
    
    public void setPrerequisiteCourseIds(List<Long> prerequisiteCourseIds) {
        this.prerequisiteCourseIds = prerequisiteCourseIds;
    }
    
    public EnrollmentPolicy getEnrollmentPolicy() {
        return enrollmentPolicy;
    }
    
    public void setEnrollmentPolicy(EnrollmentPolicy enrollmentPolicy) {
        this.enrollmentPolicy = enrollmentPolicy;
    }
    
    public Integer getRefundPolicyDays() {
        return refundPolicyDays;
    }
    
    public void setRefundPolicyDays(Integer refundPolicyDays) {
        this.refundPolicyDays = refundPolicyDays;
    }
    
    public Double getMaxProgressForRefund() {
        return maxProgressForRefund;
    }
    
    public void setMaxProgressForRefund(Double maxProgressForRefund) {
        this.maxProgressForRefund = maxProgressForRefund;
    }
    
    public User getInstructor() {
        return instructor;
    }
    
    public void setInstructor(User instructor) {
        this.instructor = instructor;
    }
    
    public List<Enrollment> getEnrollments() {
        return enrollments;
    }
    
    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }
    
    public List<Module> getModules() {
        return modules;
    }
    
    public void setModules(List<Module> modules) {
        this.modules = modules;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Utility methods
    public void addEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
        enrollment.setCourse(this);
        enrollmentCount++;
    }
    
    public void removeEnrollment(Enrollment enrollment) {
        enrollments.remove(enrollment);
        enrollment.setCourse(null);
        enrollmentCount = Math.max(0, enrollmentCount - 1);
    }
    
    public boolean isEnrolledBy(User user) {
        return enrollments.stream()
                .anyMatch(enrollment -> enrollment.getStudent().equals(user));
    }
    
    // Additional helper methods
    public void setImageUrl(String imageUrl) {
        this.thumbnailUrl = imageUrl;
    }
    
    public String getImageUrl() {
        return this.thumbnailUrl;
    }
    
    public void setDurationHours(Integer durationHours) {
        this.estimatedDurationHours = durationHours;
    }
    
    public Integer getDurationHours() {
        return this.estimatedDurationHours;
    }
    
    public enum DifficultyLevel {
        BEGINNER("Beginner"),
        INTERMEDIATE("Intermediate"),
        ADVANCED("Advanced"),
        EXPERT("Expert");
        
        private final String displayName;
        
        DifficultyLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}