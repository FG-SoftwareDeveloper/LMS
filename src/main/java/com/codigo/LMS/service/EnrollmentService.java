package com.codigo.LMS.service;

import com.codigo.LMS.entity.*;
import com.codigo.LMS.repository.*;
import com.codigo.LMS.dto.*;
// Explicit import to avoid ambiguity with java.lang.Module
import com.codigo.LMS.entity.Module;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class EnrollmentService {
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private VoucherRepository voucherRepository;
    
    @Autowired
    private EntitlementRepository entitlementRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private GameService gameService;
    
    /**
     * Smart enrollment logic with comprehensive validation
     */
    public EnrollmentResult enrollStudent(Long studentId, Long courseId, EnrollmentRequest request) {
        try {
            // Validate basic requirements
            User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
            Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
            
            // Check for duplicate enrollment
            Optional<Enrollment> existingEnrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
            if (existingEnrollment.isPresent()) {
                Enrollment existing = existingEnrollment.get();
                if (existing.getStatus() == EnrollmentStatus.ACTIVE || existing.getStatus() == EnrollmentStatus.COMPLETED) {
                    return EnrollmentResult.error("Student is already enrolled in this course");
                }
                // Allow re-enrollment if previously withdrawn/denied
            }
            
            // Validate instructor cannot enroll in own course
            if (isInstructorOfCourse(studentId, courseId)) {
                return EnrollmentResult.error("Instructors cannot enroll in their own courses");
            }
            
            // Check enrollment window
            if (!isEnrollmentWindowOpen(course)) {
                return EnrollmentResult.error("Enrollment window is closed for this course");
            }
            
            // Validate prerequisites
            if (!hasMetPrerequisites(studentId, course.getPrerequisiteCourseIds()) && 
                request.getPrereqOverrideBy() == null) {
                return EnrollmentResult.error("Prerequisites not met. Contact instructor for waiver.");
            }
            
            // Handle different enrollment types
            return handleEnrollmentByPolicy(student, course, request);
            
        } catch (Exception e) {
            auditLogRepository.save(createErrorAuditLog(studentId, "ENROLLMENT_FAILED", "enrollment", null, e.getMessage()));
            return EnrollmentResult.error("Enrollment failed: " + e.getMessage());
        }
    }
    
    private EnrollmentResult handleEnrollmentByPolicy(User student, Course course, EnrollmentRequest request) {
        switch (course.getEnrollmentPolicy()) {
            case OPEN:
                return handleOpenEnrollment(student, course, request);
            case APPROVAL_REQUIRED:
                return handleApprovalRequiredEnrollment(student, course, request);
            case PAID:
                return handlePaidEnrollment(student, course, request);
            case INVITE_ONLY:
                return handleInviteOnlyEnrollment(student, course, request);
            case VOUCHER_ONLY:
                return handleVoucherOnlyEnrollment(student, course, request);
            case COHORT_BASED:
                return handleCohortBasedEnrollment(student, course, request);
            case CORPORATE_BULK:
                return handleCorporateBulkEnrollment(student, course, request);
            default:
                return EnrollmentResult.error("Invalid enrollment policy");
        }
    }
    
    private EnrollmentResult handleOpenEnrollment(User student, Course course, EnrollmentRequest request) {
        // Check capacity
        long activeEnrollments = enrollmentRepository.countActiveEnrollmentsByCourse(course.getId());
        if (course.getCapacity() != null && activeEnrollments >= course.getCapacity()) {
            return handleWaitlist(student, course, request);
        }
        
        // Create active enrollment
        Enrollment enrollment = createEnrollment(student, course, EnrollmentStatus.ACTIVE, EnrollmentSource.SELF);
        enrollment = enrollmentRepository.save(enrollment);
        
        // Grant entitlements
        grantCourseEntitlements(student, course, enrollment);
        
        // Award enrollment points
        gameService.awardEnrollmentPoints(student.getId(), course.getId());
        
        // Send confirmation
        notificationService.sendEnrollmentConfirmation(student, course, enrollment);
        
        // Audit log
        auditLogRepository.save(AuditLog.enrollmentCreated(student.getId(), enrollment.getId(), 
            createEnrollmentPayload(enrollment)));
        
        return EnrollmentResult.success(enrollment, "Successfully enrolled in course");
    }
    
    private EnrollmentResult handleApprovalRequiredEnrollment(User student, Course course, EnrollmentRequest request) {
        // Create pending enrollment
        Enrollment enrollment = createEnrollment(student, course, EnrollmentStatus.PENDING_REVIEW, EnrollmentSource.SELF);
        enrollment.setApprovalRequestedAt(LocalDateTime.now());
        enrollment = enrollmentRepository.save(enrollment);
        
        // Notify instructors
        notificationService.sendApprovalRequest(course, student, enrollment);
        
        // Audit log
        auditLogRepository.save(AuditLog.enrollmentCreated(student.getId(), enrollment.getId(), 
            createEnrollmentPayload(enrollment)));
        
        return EnrollmentResult.success(enrollment, "Enrollment request submitted for approval");
    }
    
    private EnrollmentResult handlePaidEnrollment(User student, Course course, EnrollmentRequest request) {
        if (course.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            return handleOpenEnrollment(student, course, request);
        }
        
        // Check capacity first
        long activeEnrollments = enrollmentRepository.countActiveEnrollmentsByCourse(course.getId());
        if (course.getCapacity() != null && activeEnrollments >= course.getCapacity()) {
            return handleWaitlist(student, course, request);
        }
        
        // Calculate final price with voucher discount
        BigDecimal finalPrice = course.getPrice();
        Voucher voucher = null;
        
        if (request.getVoucherCode() != null) {
            VoucherValidationResult voucherResult = validateVoucher(request.getVoucherCode(), course, student);
            if (voucherResult.isValid()) {
                voucher = voucherResult.getVoucher();
                finalPrice = finalPrice.subtract(voucher.calculateDiscount(finalPrice));
            }
        }
        
        // If final price is zero, treat as free enrollment
        if (finalPrice.compareTo(BigDecimal.ZERO) == 0) {
            Enrollment enrollment = createEnrollment(student, course, EnrollmentStatus.ACTIVE, 
                request.getVoucherCode() != null ? EnrollmentSource.VOUCHER : EnrollmentSource.SELF);
            
            if (voucher != null) {
                enrollment.setVoucherCodeUsed(voucher.getCode());
                voucher.incrementUsage();
                voucherRepository.save(voucher);
            }
            
            enrollment = enrollmentRepository.save(enrollment);
            grantCourseEntitlements(student, course, enrollment);
            gameService.awardEnrollmentPoints(student.getId(), course.getId());
            notificationService.sendEnrollmentConfirmation(student, course, enrollment);
            
            return EnrollmentResult.success(enrollment, "Successfully enrolled with voucher");
        }
        
        // Create payment intent
        PaymentResult paymentResult = paymentService.createPaymentIntent(student, course, finalPrice, voucher);
        if (!paymentResult.isSuccess()) {
            return EnrollmentResult.error("Payment setup failed: " + paymentResult.getErrorMessage());
        }
        
        // Create pending enrollment linked to payment
        Enrollment enrollment = createEnrollment(student, course, EnrollmentStatus.PENDING_REVIEW, EnrollmentSource.SELF);
        enrollment.setPaymentId(paymentResult.getPayment().getId());
        if (voucher != null) {
            enrollment.setVoucherCodeUsed(voucher.getCode());
        }
        enrollment = enrollmentRepository.save(enrollment);
        
        return EnrollmentResult.paymentRequired(enrollment, paymentResult.getPayment());
    }
    
    private EnrollmentResult handleWaitlist(User student, Course course, EnrollmentRequest request) {
        Integer nextPosition = enrollmentRepository.findMaxWaitlistPosition(course.getId());
        nextPosition = (nextPosition == null) ? 1 : nextPosition + 1;
        
        Enrollment enrollment = createEnrollment(student, course, EnrollmentStatus.WAITLISTED, EnrollmentSource.SELF);
        enrollment.setWaitlistPosition(nextPosition);
        enrollment = enrollmentRepository.save(enrollment);
        
        notificationService.sendWaitlistConfirmation(student, course, enrollment);
        
        return EnrollmentResult.success(enrollment, "Added to waitlist at position " + nextPosition);
    }
    
    /**
     * Approve pending enrollment
     */
    public EnrollmentResult approveEnrollment(Long enrollmentId, Long approverId, String reason) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
        
        if (enrollment.getStatus() != EnrollmentStatus.PENDING_REVIEW) {
            return EnrollmentResult.error("Enrollment is not pending approval");
        }
        
        // Check capacity
        long activeEnrollments = enrollmentRepository.countActiveEnrollmentsByCourse(enrollment.getCourse().getId());
        if (enrollment.getCourse().getCapacity() != null && activeEnrollments >= enrollment.getCourse().getCapacity()) {
            return EnrollmentResult.error("Course is at capacity");
        }
        
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setApprovedAt(LocalDateTime.now());
        enrollment.setApprovedBy(approverId);
        enrollment.setActivatedAt(LocalDateTime.now());
        enrollment = enrollmentRepository.save(enrollment);
        
        // Grant entitlements
        grantCourseEntitlements(enrollment.getStudent(), enrollment.getCourse(), enrollment);
        
        // Award enrollment points
        gameService.awardEnrollmentPoints(enrollment.getStudent().getId(), enrollment.getCourse().getId());
        
        // Send notification
        notificationService.sendEnrollmentApproved(enrollment.getStudent(), enrollment.getCourse(), enrollment);
        
        // Audit log
        auditLogRepository.save(AuditLog.enrollmentApproved(approverId, enrollmentId, enrollment.getStudent().getId()));
        
        return EnrollmentResult.success(enrollment, "Enrollment approved successfully");
    }
    
    /**
     * Deny pending enrollment
     */
    public EnrollmentResult denyEnrollment(Long enrollmentId, Long denierId, String reason) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
        
        if (enrollment.getStatus() != EnrollmentStatus.PENDING_REVIEW) {
            return EnrollmentResult.error("Enrollment is not pending approval");
        }
        
        enrollment.setStatus(EnrollmentStatus.DENIED);
        enrollment.setDenialReason(reason);
        enrollment = enrollmentRepository.save(enrollment);
        
        // Send notification
        notificationService.sendEnrollmentDenied(enrollment.getStudent(), enrollment.getCourse(), enrollment, reason);
        
        // Audit log
        auditLogRepository.save(AuditLog.enrollmentDenied(denierId, enrollmentId, enrollment.getStudent().getId(), reason));
        
        return EnrollmentResult.success(enrollment, "Enrollment denied");
    }
    
    /**
     * Process payment webhook to activate enrollment
     */
    public void processPaymentWebhook(Long paymentId, String webhookStatus) {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null) return;
        
        // Find enrollment linked to this payment
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByStudentIdAndCourseId(
            payment.getUser().getId(), payment.getCourse().getId());
        
        if (!enrollmentOpt.isPresent()) return;
        
        Enrollment enrollment = enrollmentOpt.get();
        
        if ("succeeded".equals(webhookStatus)) {
            // Activate enrollment
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollment.setActivatedAt(LocalDateTime.now());
            
            // Set refund eligibility
            if (enrollment.getCourse().getRefundPolicyDays() != null) {
                enrollment.setRefundEligibleUntil(LocalDateTime.now().plusDays(enrollment.getCourse().getRefundPolicyDays()));
            }
            
            final Enrollment finalEnrollment = enrollmentRepository.save(enrollment);
            enrollment = finalEnrollment;
            
            // Grant entitlements
            grantCourseEntitlements(finalEnrollment.getStudent(), finalEnrollment.getCourse(), finalEnrollment);
            
            // Award points
            gameService.awardEnrollmentPoints(finalEnrollment.getStudent().getId(), finalEnrollment.getCourse().getId());
            
            // Use voucher if applicable
            if (finalEnrollment.getVoucherCodeUsed() != null) {
                voucherRepository.findByCode(finalEnrollment.getVoucherCodeUsed()).ifPresent(voucher -> {
                    voucher.incrementUsage();
                    voucherRepository.save(voucher);
                    auditLogRepository.save(AuditLog.voucherUsed(finalEnrollment.getStudent().getId(), voucher.getId(), voucher.getCode()));
                });
            }
            
            // Send confirmation
            notificationService.sendEnrollmentConfirmation(enrollment.getStudent(), enrollment.getCourse(), enrollment);
            
        } else if ("failed".equals(webhookStatus)) {
            // Mark enrollment as failed
            enrollment.setStatus(EnrollmentStatus.DENIED);
            enrollment.setDenialReason("Payment failed");
            enrollmentRepository.save(enrollment);
            
            notificationService.sendPaymentFailed(enrollment.getStudent(), enrollment.getCourse(), payment);
        }
    }
    
    /**
     * Withdraw from course
     */
    public EnrollmentResult withdrawEnrollment(Long enrollmentId, Long userId, String reason) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
        
        if (!enrollment.getStudent().getId().equals(userId)) {
            return EnrollmentResult.error("Unauthorized withdrawal attempt");
        }
        
        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            return EnrollmentResult.error("Can only withdraw from active enrollments");
        }
        
        enrollment.setStatus(EnrollmentStatus.WITHDRAWN);
        enrollment = enrollmentRepository.save(enrollment);
        
        // Revoke entitlements
        revokeCourseEntitlements(enrollment);
        
        // Process refund if eligible
        if (enrollment.getRefundEligibleUntil() != null && 
            enrollment.getRefundEligibleUntil().isAfter(LocalDateTime.now()) &&
            enrollment.getProgressPercentage() <= enrollment.getCourse().getMaxProgressForRefund()) {
            
            processRefund(enrollment);
        }
        
        // Promote waitlist
        promoteFromWaitlist(enrollment.getCourse().getId());
        
        // Send confirmation
        notificationService.sendWithdrawalConfirmation(enrollment.getStudent(), enrollment.getCourse(), enrollment);
        
        return EnrollmentResult.success(enrollment, "Successfully withdrawn from course");
    }
    
    /**
     * Promote next person from waitlist
     */
    private void promoteFromWaitlist(Long courseId) {
        List<Enrollment> waitlist = enrollmentRepository.findWaitlistByCourse(courseId);
        if (!waitlist.isEmpty()) {
            Enrollment nextInLine = waitlist.get(0);
            
            // Check capacity again
            long activeEnrollments = enrollmentRepository.countActiveEnrollmentsByCourse(courseId);
            Course course = courseRepository.findById(courseId).orElse(null);
            
            if (course != null && (course.getCapacity() == null || activeEnrollments < course.getCapacity())) {
                nextInLine.setStatus(EnrollmentStatus.ACTIVE);
                nextInLine.setActivatedAt(LocalDateTime.now());
                nextInLine.setWaitlistPosition(null);
                enrollmentRepository.save(nextInLine);
                
                // Grant entitlements
                grantCourseEntitlements(nextInLine.getStudent(), course, nextInLine);
                
                // Award points
                gameService.awardEnrollmentPoints(nextInLine.getStudent().getId(), courseId);
                
                // Send promotion notification
                notificationService.sendWaitlistPromotion(nextInLine.getStudent(), course, nextInLine);
            }
        }
    }
    
    // Helper methods
    private boolean isInstructorOfCourse(Long userId, Long courseId) {
        // Implementation depends on your instructor-course relationship
        // This is a placeholder
        return false;
    }
    
    private boolean isEnrollmentWindowOpen(Course course) {
        LocalDateTime now = LocalDateTime.now();
        return (course.getEnrollmentWindowStart() == null || course.getEnrollmentWindowStart().isBefore(now)) &&
               (course.getEnrollmentWindowEnd() == null || course.getEnrollmentWindowEnd().isAfter(now));
    }
    
    private boolean hasMetPrerequisites(Long studentId, List<Long> prerequisiteCourseIds) {
        if (prerequisiteCourseIds == null || prerequisiteCourseIds.isEmpty()) {
            return true;
        }
        
        List<Enrollment> completedPrereqs = enrollmentRepository.findCompletedPrerequisites(studentId, prerequisiteCourseIds);
        return completedPrereqs.size() == prerequisiteCourseIds.size();
    }
    
    private VoucherValidationResult validateVoucher(String voucherCode, Course course, User user) {
        Optional<Voucher> voucherOpt = voucherRepository.findValidVoucherByCode(voucherCode, LocalDateTime.now());
        
        if (!voucherOpt.isPresent()) {
            return VoucherValidationResult.invalid("Voucher code is invalid or expired");
        }
        
        Voucher voucher = voucherOpt.get();
        
        if (voucher.getFirstTimeUsersOnly() && hasUserMadeSuccessfulPayment(user.getId())) {
            return VoucherValidationResult.invalid("Voucher is for first-time users only");
        }
        
        if (!voucher.getAppliesToAll() && !voucher.getAppliesToCourseId().equals(course.getId())) {
            return VoucherValidationResult.invalid("Voucher is not applicable to this course");
        }
        
        if (!voucher.canBeUsedForAmount(course.getPrice())) {
            return VoucherValidationResult.invalid("Course price does not meet voucher minimum amount requirement");
        }
        
        return VoucherValidationResult.valid(voucher);
    }
    
    private boolean hasUserMadeSuccessfulPayment(Long userId) {
        return paymentRepository.countSuccessfulPaymentsByUser(userId) > 0;
    }
    
    private Enrollment createEnrollment(User student, Course course, EnrollmentStatus status, EnrollmentSource source) {
        Enrollment enrollment = new Enrollment(student, course);
        enrollment.setStatus(status);
        enrollment.setSource(source);
        
        if (status == EnrollmentStatus.ACTIVE) {
            enrollment.setActivatedAt(LocalDateTime.now());
        }
        
        return enrollment;
    }
    
    private void grantCourseEntitlements(User user, Course course, Enrollment enrollment) {
        // Grant access to course modules
        if (course.getModules() != null) {
            for (Module module : course.getModules()) {
                Entitlement entitlement = new Entitlement(user, ResourceType.LESSON, module.getId(), enrollment.getId());
                entitlementRepository.save(entitlement);
                
                // Grant access to lessons within module
                if (module.getLessons() != null) {
                    for (Lesson lesson : module.getLessons()) {
                        Entitlement lessonEntitlement = new Entitlement(user, ResourceType.LESSON, lesson.getId(), enrollment.getId());
                        entitlementRepository.save(lessonEntitlement);
                    }
                }
            }
        }
    }
    
    private void revokeCourseEntitlements(Enrollment enrollment) {
        List<Entitlement> entitlements = entitlementRepository.findByGrantedByEnrollmentId(enrollment.getId());
        for (Entitlement entitlement : entitlements) {
            entitlement.revoke(enrollment.getStudent().getId(), "Enrollment withdrawn");
            entitlementRepository.save(entitlement);
        }
    }
    
    private void processRefund(Enrollment enrollment) {
        if (enrollment.getPaymentId() != null) {
            paymentService.processRefund(enrollment.getPaymentId(), "Course withdrawal");
        }
    }
    
    private String createEnrollmentPayload(Enrollment enrollment) {
        return String.format("{\"student_id\":%d,\"course_id\":%d,\"status\":\"%s\",\"source\":\"%s\"}", 
            enrollment.getStudent().getId(), enrollment.getCourse().getId(), 
            enrollment.getStatus(), enrollment.getSource());
    }
    
    private AuditLog createErrorAuditLog(Long actorId, String action, String entityType, Long entityId, String error) {
        AuditLog log = new AuditLog(actorId, action, entityType, entityId);
        log.setSuccess(false);
        log.setErrorMessage(error);
        return log;
    }
    
    // Public methods for controller integration
    public Enrollment findByStudentAndCourse(Long studentId, Long courseId) {
        return enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId).orElse(null);
    }
    
    public EnrollmentStats getCourseEnrollmentStats(Long courseId) {
        long enrolled = enrollmentRepository.countActiveEnrollmentsByCourse(courseId);
        long waitlist = enrollmentRepository.findWaitlistByCourse(courseId).size();
        long pending = enrollmentRepository.findPendingApprovalsByCourse(courseId).size();
        Course course = courseRepository.findById(courseId).orElse(null);
        Integer capacity = course != null ? course.getCapacity() : null;
        
        return new EnrollmentStats(enrolled, capacity, waitlist, pending);
    }
    
    public EnrollmentCapacityInfo getCapacityInfo(Long courseId) {
        long enrolled = enrollmentRepository.countActiveEnrollmentsByCourse(courseId);
        long waitlist = enrollmentRepository.findWaitlistByCourse(courseId).size();
        Course course = courseRepository.findById(courseId).orElse(null);
        Integer capacity = course != null ? course.getCapacity() : null;
        
        return new EnrollmentCapacityInfo(capacity, enrolled, waitlist);
    }
    
    public PrerequisiteCheckResult checkPrerequisites(Long studentId, Long courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null || course.getPrerequisiteCourseIds().isEmpty()) {
            return new PrerequisiteCheckResult(true, java.util.Collections.emptyList(), 
                java.util.Collections.emptyList(), false);
        }
        
        List<Enrollment> completedPrereqs = enrollmentRepository.findCompletedPrerequisites(
            studentId, course.getPrerequisiteCourseIds());
        
        boolean met = completedPrereqs.size() == course.getPrerequisiteCourseIds().size();
        
        List<Course> missing = java.util.Collections.emptyList();
        List<Course> completed = java.util.Collections.emptyList();
        
        if (!met) {
            // Get missing prerequisites
            List<Long> completedIds = completedPrereqs.stream()
                .map(e -> e.getCourse().getId()).toList();
            List<Long> missingIds = course.getPrerequisiteCourseIds().stream()
                .filter(id -> !completedIds.contains(id)).toList();
            missing = courseRepository.findByIdIn(missingIds);
            completed = completedPrereqs.stream().map(Enrollment::getCourse).toList();
        }
        
        return new PrerequisiteCheckResult(met, missing, completed, true);
    }
    
    public List<Enrollment> findByStudentId(Long studentId) {
        return enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(studentId);
    }
    
    public List<Enrollment> findPendingApprovalsByInstructor(Long instructorId) {
        // This would need a more sophisticated query to find enrollments for courses taught by the instructor
        return enrollmentRepository.findPendingApprovals();
    }
    
    public BulkEnrollmentResult bulkEnroll(BulkEnrollmentRequest request, Long adminId) {
        int successCount = 0;
        int failureCount = 0;
        List<java.util.Map<String, Object>> failures = new java.util.ArrayList<>();
        
        // Process user IDs if provided
        if (request.getUserIds() != null) {
            for (Long userId : request.getUserIds()) {
                try {
                    EnrollmentRequest enrollReq = new EnrollmentRequest();
                    enrollReq.setCourseId(request.getCourseId());
                    enrollReq.setVoucherCode(request.getVoucherCode());
                    
                    EnrollmentResult result = enrollStudent(userId, request.getCourseId(), enrollReq);
                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failureCount++;
                        failures.add(java.util.Map.of("userId", userId, "error", result.getMessage()));
                    }
                } catch (Exception e) {
                    failureCount++;
                    failures.add(java.util.Map.of("userId", userId, "error", e.getMessage()));
                }
            }
        }
        
        // Process emails (would need user lookup by email)
        if (request.getUserEmails() != null) {
            for (String email : request.getUserEmails()) {
                // This would require a method to find user by email
                failureCount++;
                failures.add(java.util.Map.of("email", email, "error", "Email-based enrollment not implemented"));
            }
        }
        
        String message = String.format("Bulk enrollment completed: %d successful, %d failed", successCount, failureCount);
        return new BulkEnrollmentResult(failureCount == 0, message, successCount, failureCount, failures);
    }
    
    public EnrollmentStats getRealtimeStats(Long courseId) {
        return getCourseEnrollmentStats(courseId);
    }
    
    // Additional methods for handling other enrollment types would go here...
    private EnrollmentResult handleInviteOnlyEnrollment(User student, Course course, EnrollmentRequest request) {
        // Implementation for invite-only enrollment
        return EnrollmentResult.error("Invite-only enrollment not yet implemented");
    }
    
    private EnrollmentResult handleVoucherOnlyEnrollment(User student, Course course, EnrollmentRequest request) {
        // Implementation for voucher-only enrollment
        return EnrollmentResult.error("Voucher-only enrollment not yet implemented");
    }
    
    private EnrollmentResult handleCohortBasedEnrollment(User student, Course course, EnrollmentRequest request) {
        // Implementation for cohort-based enrollment
        return EnrollmentResult.error("Cohort-based enrollment not yet implemented");
    }
    
    private EnrollmentResult handleCorporateBulkEnrollment(User student, Course course, EnrollmentRequest request) {
        // Implementation for corporate bulk enrollment
        return EnrollmentResult.error("Corporate bulk enrollment not yet implemented");
    }
}