package com.codigo.LMS.controller;

import com.codigo.LMS.dto.*;
import com.codigo.LMS.entity.*;
import com.codigo.LMS.service.EnrollmentService;
import com.codigo.LMS.service.CourseService;
import com.codigo.LMS.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/enrollment")
public class EnrollmentController {
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private VoucherService voucherService;
    
    /**
     * Show enrollment page for a course
     */
    @GetMapping("/course/{courseId}")
    public String showEnrollmentPage(@PathVariable Long courseId, 
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        try {
            Course course = courseService.findById(courseId);
            if (course == null) {
                model.addAttribute("error", "Course not found");
                return "error/404";
            }
            
            // Get current user's enrollment status
            User currentUser = courseService.getCurrentUser(userDetails);
            Enrollment existingEnrollment = enrollmentService.findByStudentAndCourse(currentUser.getId(), courseId);
            
            model.addAttribute("course", course);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("existingEnrollment", existingEnrollment);
            model.addAttribute("enrollmentStats", enrollmentService.getCourseEnrollmentStats(courseId));
            
            return "enrollment/enroll";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading enrollment page: " + e.getMessage());
            return "error/500";
        }
    }
    
    /**
     * Process enrollment request
     */
    @PostMapping("/enroll")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> enrollStudent(@RequestBody EnrollmentRequest request,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = courseService.getCurrentUser(userDetails);
            EnrollmentResult result = enrollmentService.enrollStudent(currentUser.getId(), 
                request.getCourseId(), request);
            
            Map<String, Object> response = Map.of(
                "success", result.isSuccess(),
                "message", result.getMessage(),
                "enrollment", result.getEnrollment(),
                "paymentRequired", result.isPaymentRequired(),
                "payment", result.getPayment()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Enrollment failed: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Validate voucher code via AJAX
     */
    @PostMapping("/validate-voucher")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateVoucher(@RequestBody Map<String, Object> request) {
        try {
            String voucherCode = (String) request.get("voucherCode");
            Long courseId = Long.valueOf(request.get("courseId").toString());
            
            VoucherValidationResult result = voucherService.validateVoucher(voucherCode, courseId);
            
            Map<String, Object> response = Map.of(
                "valid", result.isValid(),
                "message", result.getMessage(),
                "discount", result.isValid() ? result.getVoucher().calculateDiscount(
                    courseService.findById(courseId).getPrice()) : 0
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "valid", false,
                "message", "Voucher validation failed: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Check enrollment capacity in real-time
     */
    @GetMapping("/capacity/{courseId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkCapacity(@PathVariable Long courseId) {
        try {
            EnrollmentCapacityInfo capacity = enrollmentService.getCapacityInfo(courseId);
            
            Map<String, Object> response = Map.of(
                "capacity", capacity.getCapacity(),
                "enrolled", capacity.getEnrolled(),
                "available", capacity.getAvailable(),
                "waitlistSize", capacity.getWaitlistSize(),
                "isAtCapacity", capacity.isAtCapacity()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "error", "Failed to check capacity: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Validate prerequisites
     */
    @GetMapping("/prerequisites/{courseId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkPrerequisites(@PathVariable Long courseId,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = courseService.getCurrentUser(userDetails);
            PrerequisiteCheckResult result = enrollmentService.checkPrerequisites(currentUser.getId(), courseId);
            
            Map<String, Object> response = Map.of(
                "met", result.isMet(),
                "missing", result.getMissingPrerequisites(),
                "completed", result.getCompletedPrerequisites(),
                "canRequestWaiver", result.isCanRequestWaiver()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "error", "Failed to check prerequisites: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * My enrollments page
     */
    @GetMapping("/my-enrollments")
    public String myEnrollments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            User currentUser = courseService.getCurrentUser(userDetails);
            List<Enrollment> enrollments = enrollmentService.findByStudentId(currentUser.getId());
            
            model.addAttribute("enrollments", enrollments);
            model.addAttribute("currentUser", currentUser);
            
            return "enrollment/my-enrollments";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading enrollments: " + e.getMessage());
            return "error/500";
        }
    }
    
    /**
     * Withdraw from course
     */
    @PostMapping("/withdraw/{enrollmentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> withdrawEnrollment(@PathVariable Long enrollmentId,
                                                                 @RequestBody Map<String, String> request,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = courseService.getCurrentUser(userDetails);
            String reason = request.get("reason");
            
            EnrollmentResult result = enrollmentService.withdrawEnrollment(enrollmentId, currentUser.getId(), reason);
            
            Map<String, Object> response = Map.of(
                "success", result.isSuccess(),
                "message", result.getMessage()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Withdrawal failed: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Instructor: View pending approvals
     */
    @GetMapping("/pending-approvals")
    public String pendingApprovals(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            User currentUser = courseService.getCurrentUser(userDetails);
            
            // Check if user is an instructor
            if (!hasRole(currentUser, "INSTRUCTOR")) {
                return "error/403";
            }
            
            List<Enrollment> pendingApprovals = enrollmentService.findPendingApprovalsByInstructor(currentUser.getId());
            model.addAttribute("pendingApprovals", pendingApprovals);
            model.addAttribute("currentUser", currentUser);
            
            return "enrollment/pending-approvals";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading pending approvals: " + e.getMessage());
            return "error/500";
        }
    }
    
    /**
     * Instructor: Approve enrollment
     */
    @PostMapping("/approve/{enrollmentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> approveEnrollment(@PathVariable Long enrollmentId,
                                                                @RequestBody Map<String, String> request,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = courseService.getCurrentUser(userDetails);
            String reason = request.get("reason");
            
            EnrollmentResult result = enrollmentService.approveEnrollment(enrollmentId, currentUser.getId(), reason);
            
            Map<String, Object> response = Map.of(
                "success", result.isSuccess(),
                "message", result.getMessage()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Approval failed: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Instructor: Deny enrollment
     */
    @PostMapping("/deny/{enrollmentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> denyEnrollment(@PathVariable Long enrollmentId,
                                                             @RequestBody Map<String, String> request,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = courseService.getCurrentUser(userDetails);
            String reason = request.get("reason");
            
            EnrollmentResult result = enrollmentService.denyEnrollment(enrollmentId, currentUser.getId(), reason);
            
            Map<String, Object> response = Map.of(
                "success", result.isSuccess(),
                "message", result.getMessage()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Denial failed: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Admin: Bulk enrollment
     */
    @PostMapping("/bulk-enroll")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bulkEnroll(@RequestBody BulkEnrollmentRequest request,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = courseService.getCurrentUser(userDetails);
            
            // Check if user is an admin
            if (!hasRole(currentUser, "ADMIN")) {
                Map<String, Object> response = Map.of(
                    "success", false,
                    "message", "Insufficient permissions"
                );
                return ResponseEntity.status(403).body(response);
            }
            
            BulkEnrollmentResult result = enrollmentService.bulkEnroll(request, currentUser.getId());
            
            Map<String, Object> response = Map.of(
                "success", result.isSuccess(),
                "message", result.getMessage(),
                "successCount", result.getSuccessCount(),
                "failureCount", result.getFailureCount(),
                "failures", result.getFailures()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Bulk enrollment failed: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Real-time enrollment updates via WebSocket endpoint
     */
    @GetMapping("/updates/{courseId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEnrollmentUpdates(@PathVariable Long courseId) {
        try {
            EnrollmentStats stats = enrollmentService.getRealtimeStats(courseId);
            
            Map<String, Object> response = Map.of(
                "enrolled", stats.getEnrolled(),
                "capacity", stats.getCapacity(),
                "waitlist", stats.getWaitlistSize(),
                "lastUpdated", stats.getLastUpdated()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "error", "Failed to get updates: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Helper methods
    private boolean hasRole(User user, String role) {
        return user.getRole().name().equals(role);
    }
}