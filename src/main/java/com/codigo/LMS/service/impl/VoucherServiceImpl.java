package com.codigo.LMS.service.impl;

import com.codigo.LMS.entity.Voucher;
import com.codigo.LMS.entity.DiscountType;
import com.codigo.LMS.dto.VoucherValidationResult;
import com.codigo.LMS.repository.VoucherRepository;
import com.codigo.LMS.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Override
    public Voucher findById(Long voucherId) {
        return voucherRepository.findById(voucherId).orElse(null);
    }

    @Override
    public Voucher findByCode(String code) {
        return voucherRepository.findByCode(code).orElse(null);
    }

    @Override
    public List<Voucher> findAll() {
        return voucherRepository.findAll();
    }

    @Override
    public List<Voucher> findActiveVouchers() {
        return voucherRepository.findByIsActiveTrue();
    }

    @Override
    public List<Voucher> findByOrganization(Long orgId) {
        return voucherRepository.findByOrgId(orgId);
    }

    @Override
    public Voucher save(Voucher voucher) {
        if (voucher.getCreatedAt() == null) {
            voucher.setCreatedAt(LocalDateTime.now());
        }
        voucher.setUpdatedAt(LocalDateTime.now());
        return voucherRepository.save(voucher);
    }

    @Override
    public void delete(Long voucherId) {
        voucherRepository.deleteById(voucherId);
    }

    @Override
    public VoucherValidationResult validateVoucher(String code, Long courseId) {
        return validateVoucher(code, courseId, null);
    }

    @Override
    public VoucherValidationResult validateVoucher(String code, Long courseId, Long userId) {
        Voucher voucher = findByCode(code);
        
        if (voucher == null) {
            return VoucherValidationResult.invalid("Voucher code not found");
        }
        
        if (!voucher.isValid()) {
            return VoucherValidationResult.invalid("Voucher is no longer valid");
        }
        
        // Check if voucher applies to this course
        if (!voucher.getAppliesToAll() && 
            voucher.getAppliesToCourseId() != null && 
            !voucher.getAppliesToCourseId().equals(courseId)) {
            return VoucherValidationResult.invalid("Voucher is not applicable to this course");
        }
        
        // TODO: Check if user is first-time user when voucher requires it
        // TODO: Check if user has already used this voucher (if single-use)
        
        return VoucherValidationResult.valid(voucher);
    }

    @Override
    public boolean isVoucherValid(String code) {
        VoucherValidationResult result = validateVoucher(code, null);
        return result.isValid();
    }

    @Override
    public void useVoucher(String code, Long userId) {
        Voucher voucher = findByCode(code);
        if (voucher != null && voucher.isValid()) {
            voucher.incrementUsage();
            save(voucher);
        }
    }

    @Override
    public BigDecimal calculateDiscount(String code, BigDecimal originalAmount) {
        Voucher voucher = findByCode(code);
        if (voucher != null) {
            return voucher.calculateDiscount(originalAmount);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public Voucher createVoucher(String code, String name, String discountType, BigDecimal value) {
        Voucher voucher = new Voucher();
        voucher.setCode(code);
        voucher.setName(name);
        voucher.setDiscountType(DiscountType.valueOf(discountType.toUpperCase()));
        voucher.setValue(value);
        voucher.setIsActive(true);
        voucher.setCreatedAt(LocalDateTime.now());
        voucher.setAppliesToAll(true);
        voucher.setFirstTimeUsersOnly(false);
        
        return save(voucher);
    }

    @Override
    public void deactivateVoucher(String code) {
        Voucher voucher = findByCode(code);
        if (voucher != null) {
            voucher.setIsActive(false);
            save(voucher);
        }
    }

    @Override
    public void deactivateExpiredVouchers() {
        List<Voucher> expiredVouchers = voucherRepository.findExpiredActiveVouchers(LocalDateTime.now());
        for (Voucher voucher : expiredVouchers) {
            voucher.setIsActive(false);
            save(voucher);
        }
    }

    @Override
    public List<Voucher> findVouchersForCourse(Long courseId) {
        return voucherRepository.findValidVouchersForCourse(courseId, LocalDateTime.now());
    }

    @Override
    public List<Voucher> findUniversalVouchers() {
        return voucherRepository.findValidUniversalVouchers(LocalDateTime.now());
    }

    @Override
    public List<Voucher> findExpiredVouchers() {
        return voucherRepository.findExpiredActiveVouchers(LocalDateTime.now());
    }

    @Override
    public List<Voucher> findFullyUsedVouchers() {
        return voucherRepository.findFullyUsedActiveVouchers();
    }

    @Override
    public long getActiveVoucherCount(Long orgId) {
        return voucherRepository.countActiveVouchersByOrg(orgId);
    }
}