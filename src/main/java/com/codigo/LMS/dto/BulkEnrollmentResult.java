package com.codigo.LMS.dto;

import java.util.List;
import java.util.Map;

public class BulkEnrollmentResult {
    
    private boolean success;
    private String message;
    private int successCount;
    private int failureCount;
    private List<Map<String, Object>> failures;
    
    public BulkEnrollmentResult() {}
    
    public BulkEnrollmentResult(boolean success, String message, int successCount, int failureCount, List<Map<String, Object>> failures) {
        this.success = success;
        this.message = message;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.failures = failures;
    }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    
    public int getFailureCount() { return failureCount; }
    public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
    
    public List<Map<String, Object>> getFailures() { return failures; }
    public void setFailures(List<Map<String, Object>> failures) { this.failures = failures; }
}