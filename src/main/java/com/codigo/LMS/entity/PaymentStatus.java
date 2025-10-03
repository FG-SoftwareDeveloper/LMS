package com.codigo.LMS.entity;

public enum PaymentStatus {
    PENDING,        // Payment initiated but not completed
    PROCESSING,     // Payment being processed
    SUCCEEDED,      // Payment completed successfully
    FAILED,         // Payment failed
    CANCELLED,      // Payment cancelled by user
    REFUNDED,       // Payment refunded
    PARTIALLY_REFUNDED, // Partial refund processed
    DISPUTED,       // Payment disputed/chargeback
    EXPIRED         // Payment session expired
}