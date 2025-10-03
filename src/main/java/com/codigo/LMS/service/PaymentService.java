package com.codigo.LMS.service;

import com.codigo.LMS.entity.Course;
import com.codigo.LMS.entity.Payment;
import com.codigo.LMS.entity.User;
import com.codigo.LMS.entity.Voucher;
import com.codigo.LMS.dto.PaymentResult;
import java.math.BigDecimal;

public interface PaymentService {
    
    // Payment processing
    PaymentResult createPaymentIntent(User user, Course course, BigDecimal amount, Voucher voucher);
    PaymentResult processPayment(Long paymentId, String paymentMethodId);
    PaymentResult processRefund(Long paymentId, String reason);
    PaymentResult processPartialRefund(Long paymentId, BigDecimal amount, String reason);
    
    // Payment status
    Payment getPaymentById(Long paymentId);
    Payment getPaymentByStripeIntentId(String stripeIntentId);
    boolean isPaymentSuccessful(Long paymentId);
    
    // Webhook handling
    void handleStripeWebhook(String payload, String signature);
    void handlePayPalWebhook(String payload, String signature);
    
    // Invoice generation
    String generateInvoice(Payment payment);
    byte[] generateInvoicePDF(Payment payment);
    
    // Payment analytics
    BigDecimal getTotalRevenue(java.time.LocalDateTime start, java.time.LocalDateTime end);
    long getSuccessfulPaymentCount(Long userId);
    
    // Subscription/recurring payments (future)
    PaymentResult createSubscription(User user, Course course, String planId);
    PaymentResult cancelSubscription(String subscriptionId);
}