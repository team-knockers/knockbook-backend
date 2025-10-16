package com.knockbook.backend.repository;

import com.knockbook.backend.domain.PointTransaction;

public interface PointTransactionRepository {
    PointTransaction save(PointTransaction tx);
}
