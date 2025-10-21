package com.knockbook.backend.repository;

import com.knockbook.backend.domain.PointTransaction;

import java.util.List;

public interface PointTransactionRepository {
    PointTransaction save(PointTransaction tx);
    List<PointTransaction> findAllByUserId(Long userId);
}
