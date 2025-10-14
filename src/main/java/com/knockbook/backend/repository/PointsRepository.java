package com.knockbook.backend.repository;

import java.util.Optional;

public interface PointsRepository {
    Optional<Integer> findBalance(final Long userId);
}
