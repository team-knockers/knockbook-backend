package com.knockbook.backend.repository;

import java.time.Instant;
import java.util.List;

public interface CouponRepository {
    List<Long> findActiveIdsByCodes(final List<String> codes,
                                    Instant now);
}
