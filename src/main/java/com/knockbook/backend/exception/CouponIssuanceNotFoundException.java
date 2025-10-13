package com.knockbook.backend.exception;

public class CouponIssuanceNotFoundException extends ApplicationException {

    public CouponIssuanceNotFoundException(Long issuanceId, Long userId) {
        super(
                "COUPON_ISSUANCE_NOT_FOUND",
                "Coupon issuanced not found: userId=%d, couponId=%d".formatted(issuanceId, userId)
        );
    }
}
