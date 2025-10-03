package com.codigo.LMS.entity;

public enum PaymentProcessor {
    STRIPE,     // Stripe payment processing
    PAYPAL,     // PayPal payment processing
    SQUARE,     // Square payment processing
    RAZORPAY,   // Razorpay payment processing
    INTERNAL,   // Internal/manual processing
    FREE        // Free enrollment (no payment processor)
}