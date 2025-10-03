package com.codigo.LMS.dto;

public class EnrollmentCapacityInfo {
    
    private Integer capacity;
    private long enrolled;
    private Integer available;
    private long waitlistSize;
    private boolean atCapacity;
    
    public EnrollmentCapacityInfo() {}
    
    public EnrollmentCapacityInfo(Integer capacity, long enrolled, long waitlistSize) {
        this.capacity = capacity;
        this.enrolled = enrolled;
        this.available = capacity != null ? Math.max(0, capacity - (int)enrolled) : null;
        this.waitlistSize = waitlistSize;
        this.atCapacity = capacity != null && enrolled >= capacity;
    }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public long getEnrolled() { return enrolled; }
    public void setEnrolled(long enrolled) { this.enrolled = enrolled; }
    
    public Integer getAvailable() { return available; }
    public void setAvailable(Integer available) { this.available = available; }
    
    public long getWaitlistSize() { return waitlistSize; }
    public void setWaitlistSize(long waitlistSize) { this.waitlistSize = waitlistSize; }
    
    public boolean isAtCapacity() { return atCapacity; }
    public void setAtCapacity(boolean atCapacity) { this.atCapacity = atCapacity; }
}