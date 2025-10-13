package com.knockbook.backend.repository;

import com.knockbook.backend.domain.CouponIssuance;

import java.util.List;
import java.util.Optional;

public interface CouponIssuanceRepository {
    void insertIfNotExists(final Long userId,
                           final List<Long> couponIds);

    List<CouponIssuance> findByUserId(final Long userId,
                                      final CouponIssuance.Status status);
    Optional<CouponIssuance> findByIdAndUserId(final Long id,
                                               final Long userId);
}
