package com.codigo.LMS.entity;

public enum EnrollmentPolicy {
    OPEN,           // Free, instant enrollment
    APPROVAL_REQUIRED, // Learner requests, instructor approves
    INVITE_ONLY,    // Email link or code
    PAID,           // Checkout required
    VOUCHER_ONLY,   // Must use voucher/scholarship code
    COHORT_BASED,   // Fixed start/end with enrollment window
    CORPORATE_BULK  // Seats purchased by org admin
}