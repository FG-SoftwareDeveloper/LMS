package com.codigo.LMS.entity;

public enum EntitlementStatus {
    ACTIVE,     // Entitlement is active and can be used
    REVOKED,    // Entitlement has been revoked
    EXPIRED,    // Entitlement has expired
    SUSPENDED,  // Entitlement is temporarily suspended
    PENDING     // Entitlement is pending activation
}