package com.knockbook.backend.repository;

import java.util.List;

public interface CouponIssuanceRepository {
    void insertIfNotExists(final Long userId,
                           List<Long> couponIds);
}
