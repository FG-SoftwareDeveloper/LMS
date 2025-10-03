package com.codigo.LMS.dto;

import java.math.BigDecimal;

public class EnrollmentRequest {
    
    private Long courseId;
    private String voucherCode;
    private String invitationToken;
    private Long prereqOverrideBy;
    private String notes;
    private String paymentMethod;
    private boolean agreeToTerms;
    
    // Constructors
    public EnrollmentRequest() {}
    
    public EnrollmentRequest(String voucherCode) {
        this.voucherCode = voucherCode;
    }
    
    // Getters and Setters
    public Long getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    
    public String getVoucherCode() {
        return voucherCode;
    }
    
    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }
    
    public String getInvitationToken() {
        return invitationToken;
    }
    
    public void setInvitationToken(String invitationToken) {
        this.invitationToken = invitationToken;
    }
    
    public Long getPrereqOverrideBy() {
        return prereqOverrideBy;
    }
    
    public void setPrereqOverrideBy(Long prereqOverrideBy) {
        this.prereqOverrideBy = prereqOverrideBy;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public boolean isAgreeToTerms() {
        return agreeToTerms;
    }
    
    public void setAgreeToTerms(boolean agreeToTerms) {
        this.agreeToTerms = agreeToTerms;
    }
}