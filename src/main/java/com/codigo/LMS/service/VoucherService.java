package com.codigo.LMS.service;

import com.codigo.LMS.entity.Voucher;
import com.codigo.LMS.dto.VoucherValidationResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface VoucherService {
    
    Voucher findById(Long voucherId);
    Voucher findByCode(String code);
    List<Voucher> findAll();
    List<Voucher> findActiveVouchers();
    List<Voucher> findByOrganization(Long orgId);
    
    Voucher save(Voucher voucher);
    void delete(Long voucherId);
    
    // Validation
    VoucherValidationResult validateVoucher(String code, Long courseId);
    VoucherValidationResult validateVoucher(String code, Long courseId, Long userId);
    boolean isVoucherValid(String code);
    
    // Voucher operations
    void useVoucher(String code, Long userId);
    BigDecimal calculateDiscount(String code, BigDecimal originalAmount);
    
    // Admin operations
    Voucher createVoucher(String code, String name, String discountType, BigDecimal value);
    void deactivateVoucher(String code);
    void deactivateExpiredVouchers();
    
    // Analytics
    List<Voucher> findVouchersForCourse(Long courseId);
    List<Voucher> findUniversalVouchers();
    List<Voucher> findExpiredVouchers();
    List<Voucher> findFullyUsedVouchers();
    long getActiveVoucherCount(Long orgId);
}