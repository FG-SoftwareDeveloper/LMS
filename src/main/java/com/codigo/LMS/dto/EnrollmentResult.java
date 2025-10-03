package com.codigo.LMS.dto;

import com.codigo.LMS.entity.Enrollment;
import com.codigo.LMS.entity.Payment;

public class EnrollmentResult {
    
    private boolean success;
    private String message;
    private String errorCode;
    private Enrollment enrollment;
    private Payment payment;
    private boolean paymentRequired;
    
    // Private constructor to force use of factory methods
    private EnrollmentResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    // Factory methods
    public static EnrollmentResult success(Enrollment enrollment, String message) {
        EnrollmentResult result = new EnrollmentResult(true, message);
        result.enrollment = enrollment;
        return result;
    }
    
    public static EnrollmentResult error(String message) {
        return new EnrollmentResult(false, message);
    }
    
    public static EnrollmentResult error(String message, String errorCode) {
        EnrollmentResult result = new EnrollmentResult(false, message);
        result.errorCode = errorCode;
        return result;
    }
    
    public static EnrollmentResult paymentRequired(Enrollment enrollment, Payment payment) {
        EnrollmentResult result = new EnrollmentResult(true, "Payment required to complete enrollment");
        result.enrollment = enrollment;
        result.payment = payment;
        result.paymentRequired = true;
        return result;
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Enrollment getEnrollment() {
        return enrollment;
    }
    
    public Payment getPayment() {
        return payment;
    }
    
    public boolean isPaymentRequired() {
        return paymentRequired;
    }
    
    public boolean hasError() {
        return !success;
    }
    
    public String getErrorMessage() {
        return success ? null : message;
    }
}