package com.knockbook.backend.repository;

import com.knockbook.backend.domain.CouponRedemption;

public interface CouponRedemptionRepository {
    boolean existsByIssuanceId(final Long issuanceId);
    CouponRedemption save(final CouponRedemption entity);
}
