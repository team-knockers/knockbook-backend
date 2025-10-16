package com.knockbook.backend.repository;

import com.knockbook.backend.domain.PointBalance;

import java.util.Optional;

public interface PointBalanceRepository {
    Optional<PointBalance> findByUserIdForUpdate(Long userId);
    PointBalance save(PointBalance balance);
}
