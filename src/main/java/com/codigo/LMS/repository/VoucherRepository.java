package com.codigo.LMS.repository;

import com.codigo.LMS.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    
    Optional<Voucher> findByCode(String code);
    
    List<Voucher> findByIsActiveTrue();
    
    List<Voucher> findByOrgId(Long orgId);
    
    List<Voucher> findByCreatedBy(Long createdBy);
    
    @Query("SELECT v FROM Voucher v WHERE v.code = :code AND v.isActive = true AND " +
           "(v.expiresAt IS NULL OR v.expiresAt > :now) AND " +
           "(v.maxUses IS NULL OR v.uses < v.maxUses)")
    Optional<Voucher> findValidVoucherByCode(@Param("code") String code, @Param("now") LocalDateTime now);
    
    @Query("SELECT v FROM Voucher v WHERE v.appliesToCourseId = :courseId AND v.isActive = true AND " +
           "(v.expiresAt IS NULL OR v.expiresAt > :now) AND " +
           "(v.maxUses IS NULL OR v.uses < v.maxUses)")
    List<Voucher> findValidVouchersForCourse(@Param("courseId") Long courseId, @Param("now") LocalDateTime now);
    
    @Query("SELECT v FROM Voucher v WHERE v.appliesToAll = true AND v.isActive = true AND " +
           "(v.expiresAt IS NULL OR v.expiresAt > :now) AND " +
           "(v.maxUses IS NULL OR v.uses < v.maxUses)")
    List<Voucher> findValidUniversalVouchers(@Param("now") LocalDateTime now);
    
    @Query("SELECT v FROM Voucher v WHERE v.expiresAt < :now AND v.isActive = true")
    List<Voucher> findExpiredActiveVouchers(@Param("now") LocalDateTime now);
    
    @Query("SELECT v FROM Voucher v WHERE v.maxUses IS NOT NULL AND v.uses >= v.maxUses AND v.isActive = true")
    List<Voucher> findFullyUsedActiveVouchers();
    
    @Query("SELECT COUNT(v) FROM Voucher v WHERE v.orgId = :orgId AND v.isActive = true")
    long countActiveVouchersByOrg(@Param("orgId") Long orgId);
}