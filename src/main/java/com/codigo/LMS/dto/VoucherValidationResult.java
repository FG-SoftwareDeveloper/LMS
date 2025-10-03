package com.codigo.LMS.dto;

import com.codigo.LMS.entity.Voucher;

public class VoucherValidationResult {
    
    private boolean valid;
    private String message;
    private Voucher voucher;
    
    private VoucherValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }
    
    private VoucherValidationResult(boolean valid, String message, Voucher voucher) {
        this.valid = valid;
        this.message = message;
        this.voucher = voucher;
    }
    
    public static VoucherValidationResult valid(Voucher voucher) {
        return new VoucherValidationResult(true, "Voucher is valid", voucher);
    }
    
    public static VoucherValidationResult invalid(String message) {
        return new VoucherValidationResult(false, message);
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Voucher getVoucher() {
        return voucher;
    }
}