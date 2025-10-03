package com.codigo.LMS.entity;

public enum EnrollmentSource {
    SELF,       // Self-enrolled
    INVITE,     // Invited via email/link
    BULK,       // Bulk enrolled by admin
    VOUCHER,    // Enrolled via voucher code
    CORPORATE,  // Corporate/organization enrollment
    ADMIN       // Directly enrolled by admin
}