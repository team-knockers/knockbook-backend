package com.knockbook.backend.exception;

public class CouponExpiredException extends ApplicationException {

    public CouponExpiredException(final Long issuanceId) {
        super(
                "COUPON_EXPIRED",
                "Coupon expired: issuanceId=%d".formatted(issuanceId));
    }
}
