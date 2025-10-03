package com.codigo.LMS.dto;

import java.time.LocalDateTime;

public class EnrollmentStats {
    
    private long enrolled;
    private Integer capacity;
    private long waitlistSize;
    private LocalDateTime lastUpdated;
    private double enrollmentRate;
    private long pendingApprovals;
    
    public EnrollmentStats() {
        this.lastUpdated = LocalDateTime.now();
    }
    
    public EnrollmentStats(long enrolled, Integer capacity, long waitlistSize, long pendingApprovals) {
        this.enrolled = enrolled;
        this.capacity = capacity;
        this.waitlistSize = waitlistSize;
        this.pendingApprovals = pendingApprovals;
        this.lastUpdated = LocalDateTime.now();
        
        if (capacity != null && capacity > 0) {
            this.enrollmentRate = (double) enrolled / capacity * 100;
        }
    }
    
    public long getEnrolled() { return enrolled; }
    public void setEnrolled(long enrolled) { this.enrolled = enrolled; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public long getWaitlistSize() { return waitlistSize; }
    public void setWaitlistSize(long waitlistSize) { this.waitlistSize = waitlistSize; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public double getEnrollmentRate() { return enrollmentRate; }
    public void setEnrollmentRate(double enrollmentRate) { this.enrollmentRate = enrollmentRate; }
    
    public long getPendingApprovals() { return pendingApprovals; }
    public void setPendingApprovals(long pendingApprovals) { this.pendingApprovals = pendingApprovals; }
}