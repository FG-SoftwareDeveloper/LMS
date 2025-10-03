package com.codigo.LMS.repository;

import com.codigo.LMS.entity.Enrollment;
import com.codigo.LMS.entity.EnrollmentStatus;
import com.codigo.LMS.entity.EnrollmentSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    List<Enrollment> findByStudentIdOrderByEnrolledAtDesc(Long studentId);
    
    List<Enrollment> findByCourseIdOrderByEnrolledAtDesc(Long courseId);
    
    List<Enrollment> findByStatus(EnrollmentStatus status);
    
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);
    
    List<Enrollment> findByCourseIdAndStatus(Long courseId, EnrollmentStatus status);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'ACTIVE'")
    long countActiveEnrollmentsByCourse(@Param("courseId") Long courseId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.status = 'PENDING_REVIEW' ORDER BY e.approvalRequestedAt ASC")
    List<Enrollment> findPendingApprovals();
    
    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'PENDING_REVIEW' ORDER BY e.approvalRequestedAt ASC")
    List<Enrollment> findPendingApprovalsByCourse(@Param("courseId") Long courseId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.status = 'WAITLISTED' AND e.course.id = :courseId ORDER BY e.enrolledAt ASC")
    List<Enrollment> findWaitlistByCourse(@Param("courseId") Long courseId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.status = 'WAITLISTED' AND e.course.id = :courseId AND e.waitlistPosition = :position")
    Optional<Enrollment> findByWaitlistPosition(@Param("courseId") Long courseId, @Param("position") Integer position);
    
    @Query("SELECT MAX(e.waitlistPosition) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'WAITLISTED'")
    Integer findMaxWaitlistPosition(@Param("courseId") Long courseId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.invitationToken = :token")
    Optional<Enrollment> findByInvitationToken(@Param("token") String token);
    
    @Query("SELECT e FROM Enrollment e WHERE e.voucherCodeUsed = :voucherCode")
    List<Enrollment> findByVoucherCode(@Param("voucherCode") String voucherCode);
    
    @Query("SELECT e FROM Enrollment e WHERE e.source = :source")
    List<Enrollment> findBySource(@Param("source") EnrollmentSource source);
    
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.course.id IN :courseIds AND e.status = 'COMPLETED'")
    List<Enrollment> findCompletedPrerequisites(@Param("studentId") Long studentId, @Param("courseIds") List<Long> courseIds);
    
    @Query("SELECT e FROM Enrollment e WHERE e.refundEligibleUntil > :now AND e.status = 'ACTIVE'")
    List<Enrollment> findRefundEligibleEnrollments(@Param("now") LocalDateTime now);
    
    @Query("SELECT e FROM Enrollment e WHERE e.lastAccessedAt < :cutoffDate AND e.status = 'ACTIVE'")
    List<Enrollment> findInactiveEnrollments(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :studentId AND e.status IN ('ACTIVE', 'COMPLETED')")
    long countActiveEnrollmentsByStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.approvalRequestedAt < :cutoffTime AND e.status = 'PENDING_REVIEW'")
    List<Enrollment> findStaleApprovalRequests(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT e FROM Enrollment e WHERE e.enrolledAt BETWEEN :start AND :end")
    List<Enrollment> findEnrollmentsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT e.course.id, COUNT(e) FROM Enrollment e WHERE e.status = 'ACTIVE' GROUP BY e.course.id ORDER BY COUNT(e) DESC")
    List<Object[]> findMostPopularCourses();
    
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :instructorId AND e.course.id = :courseId")
    Optional<Enrollment> findInstructorSelfEnrollment(@Param("instructorId") Long instructorId, @Param("courseId") Long courseId);
}