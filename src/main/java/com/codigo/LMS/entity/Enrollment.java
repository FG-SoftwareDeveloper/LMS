package com.codigo.LMS.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
public class Enrollment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "progress_percentage")
    private Double progressPercentage = 0.0;
    
    @Column(name = "total_points")
    private Integer totalPoints = 0;
    
    @Enumerated(EnumType.STRING)
    private com.codigo.LMS.entity.EnrollmentStatus status = com.codigo.LMS.entity.EnrollmentStatus.ACTIVE;
    
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;
    
    // Enhanced enrollment fields
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;
    
    @Column(name = "grade")
    private String grade;
    
    @Column(name = "source")
    @Enumerated(EnumType.STRING)
    private EnrollmentSource source = EnrollmentSource.SELF;
    
    @Column(name = "prereq_override_by")
    private Long prereqOverrideBy;
    
    @Column(name = "denial_reason")
    private String denialReason;
    
    @Column(name = "approval_requested_at")
    private LocalDateTime approvalRequestedAt;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "waitlist_position")
    private Integer waitlistPosition;
    
    @Column(name = "voucher_code_used")
    private String voucherCodeUsed;
    
    @Column(name = "payment_id")
    private Long paymentId;
    
    @Column(name = "refund_eligible_until")
    private LocalDateTime refundEligibleUntil;
    
    @Column(name = "invitation_token")
    private String invitationToken;
    
    @Column(name = "invited_by")
    private Long invitedBy;
    
    @PrePersist
    protected void onCreate() {
        enrolledAt = LocalDateTime.now();
        lastAccessedAt = LocalDateTime.now();
        if (status == com.codigo.LMS.entity.EnrollmentStatus.ACTIVE) {
            activatedAt = LocalDateTime.now();
        }
    }
    
    // Constructors
    public Enrollment() {}
    
    public Enrollment(User student, Course course) {
        this.student = student;
        this.course = course;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getStudent() {
        return student;
    }
    
    public void setStudent(User student) {
        this.student = student;
    }
    
    public Course getCourse() {
        return course;
    }
    
    public void setCourse(Course course) {
        this.course = course;
    }
    
    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }
    
    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public Double getProgressPercentage() {
        return progressPercentage;
    }
    
    public void setProgressPercentage(Double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    
    public Integer getTotalPoints() {
        return totalPoints;
    }
    
    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }
    
    public com.codigo.LMS.entity.EnrollmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(com.codigo.LMS.entity.EnrollmentStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }
    
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }
    
    // New enrollment fields getters and setters
    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }
    
    public void setActivatedAt(LocalDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }
    
    public String getGrade() {
        return grade;
    }
    
    public void setGrade(String grade) {
        this.grade = grade;
    }
    
    public EnrollmentSource getSource() {
        return source;
    }
    
    public void setSource(EnrollmentSource source) {
        this.source = source;
    }
    
    public Long getPrereqOverrideBy() {
        return prereqOverrideBy;
    }
    
    public void setPrereqOverrideBy(Long prereqOverrideBy) {
        this.prereqOverrideBy = prereqOverrideBy;
    }
    
    public String getDenialReason() {
        return denialReason;
    }
    
    public void setDenialReason(String denialReason) {
        this.denialReason = denialReason;
    }
    
    public LocalDateTime getApprovalRequestedAt() {
        return approvalRequestedAt;
    }
    
    public void setApprovalRequestedAt(LocalDateTime approvalRequestedAt) {
        this.approvalRequestedAt = approvalRequestedAt;
    }
    
    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }
    
    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
    
    public Long getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(Long approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public Integer getWaitlistPosition() {
        return waitlistPosition;
    }
    
    public void setWaitlistPosition(Integer waitlistPosition) {
        this.waitlistPosition = waitlistPosition;
    }
    
    public String getVoucherCodeUsed() {
        return voucherCodeUsed;
    }
    
    public void setVoucherCodeUsed(String voucherCodeUsed) {
        this.voucherCodeUsed = voucherCodeUsed;
    }
    
    public Long getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
    
    public LocalDateTime getRefundEligibleUntil() {
        return refundEligibleUntil;
    }
    
    public void setRefundEligibleUntil(LocalDateTime refundEligibleUntil) {
        this.refundEligibleUntil = refundEligibleUntil;
    }
    
    public String getInvitationToken() {
        return invitationToken;
    }
    
    public void setInvitationToken(String invitationToken) {
        this.invitationToken = invitationToken;
    }
    
    public Long getInvitedBy() {
        return invitedBy;
    }
    
    public void setInvitedBy(Long invitedBy) {
        this.invitedBy = invitedBy;
    }
    
    // Utility methods
    public boolean isCompleted() {
        return completedAt != null && status == com.codigo.LMS.entity.EnrollmentStatus.COMPLETED;
    }
    
    public void updateLastAccessed() {
        this.lastAccessedAt = LocalDateTime.now();
    }
}