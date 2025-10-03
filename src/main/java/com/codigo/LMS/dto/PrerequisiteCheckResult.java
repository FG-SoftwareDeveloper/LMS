package com.codigo.LMS.dto;

import com.codigo.LMS.entity.Course;
import java.util.List;

public class PrerequisiteCheckResult {
    
    private boolean met;
    private List<Course> missingPrerequisites;
    private List<Course> completedPrerequisites;
    private boolean canRequestWaiver;
    
    public PrerequisiteCheckResult() {}
    
    public PrerequisiteCheckResult(boolean met, List<Course> missing, List<Course> completed, boolean canRequestWaiver) {
        this.met = met;
        this.missingPrerequisites = missing;
        this.completedPrerequisites = completed;
        this.canRequestWaiver = canRequestWaiver;
    }
    
    public boolean isMet() { return met; }
    public void setMet(boolean met) { this.met = met; }
    
    public List<Course> getMissingPrerequisites() { return missingPrerequisites; }
    public void setMissingPrerequisites(List<Course> missingPrerequisites) { this.missingPrerequisites = missingPrerequisites; }
    
    public List<Course> getCompletedPrerequisites() { return completedPrerequisites; }
    public void setCompletedPrerequisites(List<Course> completedPrerequisites) { this.completedPrerequisites = completedPrerequisites; }
    
    public boolean isCanRequestWaiver() { return canRequestWaiver; }
    public void setCanRequestWaiver(boolean canRequestWaiver) { this.canRequestWaiver = canRequestWaiver; }
}