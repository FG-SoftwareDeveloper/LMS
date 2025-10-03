package com.codigo.LMS.dto;

import java.util.List;
import java.util.Map;

public class BulkEnrollmentRequest {
    
    private Long courseId;
    private List<String> userEmails;
    private List<Long> userIds;
    private String voucherCode;
    private boolean sendInvitations;
    private String customMessage;
    
    public BulkEnrollmentRequest() {}
    
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    
    public List<String> getUserEmails() { return userEmails; }
    public void setUserEmails(List<String> userEmails) { this.userEmails = userEmails; }
    
    public List<Long> getUserIds() { return userIds; }
    public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
    
    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
    
    public boolean isSendInvitations() { return sendInvitations; }
    public void setSendInvitations(boolean sendInvitations) { this.sendInvitations = sendInvitations; }
    
    public String getCustomMessage() { return customMessage; }
    public void setCustomMessage(String customMessage) { this.customMessage = customMessage; }
}