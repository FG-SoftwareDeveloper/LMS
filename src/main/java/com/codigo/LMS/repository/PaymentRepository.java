package com.codigo.LMS.repository;

import com.codigo.LMS.entity.Payment;
import com.codigo.LMS.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Payment> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    
    Optional<Payment> findByExternalTxnId(String externalTxnId);
    
    List<Payment> findByInvoiceNumber(String invoiceNumber);
    
    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.course.id = :courseId AND p.status = :status")
    Optional<Payment> findByUserAndCourseAndStatus(@Param("userId") Long userId, 
                                                   @Param("courseId") Long courseId, 
                                                   @Param("status") PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :cutoffTime")
    List<Payment> findExpiredPendingPayments(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.user.id = :userId AND p.status = 'SUCCEEDED'")
    long countSuccessfulPaymentsByUser(@Param("userId") Long userId);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCEEDED' AND p.createdAt BETWEEN :start AND :end")
    java.math.BigDecimal getTotalRevenueInPeriod(@Param("start") LocalDateTime start, 
                                                 @Param("end") LocalDateTime end);
    
    @Query("SELECT p FROM Payment p WHERE p.voucherCode = :voucherCode AND p.status = 'SUCCEEDED'")
    List<Payment> findSuccessfulPaymentsByVoucherCode(@Param("voucherCode") String voucherCode);
}