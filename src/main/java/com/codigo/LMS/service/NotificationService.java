package com.codigo.LMS.service;

import com.codigo.LMS.entity.Course;
import com.codigo.LMS.entity.Enrollment;
import com.codigo.LMS.entity.User;

public interface NotificationService {
    
    // Enrollment notifications
    void sendEnrollmentConfirmation(User student, Course course, Enrollment enrollment);
    void sendApprovalRequest(Course course, User student, Enrollment enrollment);
    void sendEnrollmentApproved(User student, Course course, Enrollment enrollment);
    void sendEnrollmentDenied(User student, Course course, Enrollment enrollment, String reason);
    void sendWaitlistConfirmation(User student, Course course, Enrollment enrollment);
    void sendWaitlistPromotion(User student, Course course, Enrollment enrollment);
    void sendWithdrawalConfirmation(User student, Course course, Enrollment enrollment);
    
    // Payment notifications
    void sendPaymentConfirmation(User user, Course course, com.codigo.LMS.entity.Payment payment);
    void sendPaymentFailed(User user, Course course, com.codigo.LMS.entity.Payment payment);
    void sendRefundProcessed(User user, Course course, com.codigo.LMS.entity.Payment payment);
    
    // Course notifications
    void sendCourseStartReminder(User student, Course course, Enrollment enrollment);
    void sendProgressMilestone(User student, Course course, Enrollment enrollment, String milestone);
    void sendCourseCompletion(User student, Course course, Enrollment enrollment);
    
    // Admin notifications
    void sendCapacityThresholdAlert(Course course, double currentCapacity);
    void sendVoucherLowBalanceAlert(com.codigo.LMS.entity.Voucher voucher);
    
    // Instructor notifications
    void sendNewEnrollmentNotification(User instructor, Course course, Enrollment enrollment);
    void sendStudentProgressAlert(User instructor, Course course, User student);
}