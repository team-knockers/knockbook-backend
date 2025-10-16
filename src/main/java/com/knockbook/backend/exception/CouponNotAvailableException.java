package com.knockbook.backend.exception;

public class CouponNotAvailableException extends ApplicationException {

    public CouponNotAvailableException(final Long issuanceId) {
        super(
                "COUPON_NOT_AVAILABLE",
                "Coupon not available: issuanceId=%d".formatted(issuanceId));
    }
}
