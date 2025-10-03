package com.codigo.LMS.entity;

public enum EnrollmentStatus {
    ACTIVE,           // Successfully enrolled and can access content
    PENDING_REVIEW,   // Waiting for instructor/admin approval
    DENIED,          // Enrollment request denied
    WAITLISTED,      // On waitlist due to capacity limits
    WITHDRAWN,       // Voluntarily withdrawn by student
    COMPLETED,       // Successfully completed the course
    DROPPED,         // Dropped/removed by admin
    SUSPENDED,       // Temporarily suspended
    EXPIRED,         // Enrollment expired (for time-limited courses)
    REFUNDED         // Enrollment refunded and revoked
}