package com.codigo.LMS.dto;

import com.codigo.LMS.entity.Payment;

public class PaymentResult {
    
    private boolean success;
    private String message;
    private String errorMessage;
    private Payment payment;
    private String clientSecret;
    private String redirectUrl;
    
    private PaymentResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public static PaymentResult success(Payment payment, String clientSecret) {
        PaymentResult result = new PaymentResult(true, "Payment intent created successfully");
        result.payment = payment;
        result.clientSecret = clientSecret;
        return result;
    }
    
    public static PaymentResult error(String errorMessage) {
        PaymentResult result = new PaymentResult(false, "Payment failed");
        result.errorMessage = errorMessage;
        return result;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public Payment getPayment() {
        return payment;
    }
    
    public String getClientSecret() {
        return clientSecret;
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}