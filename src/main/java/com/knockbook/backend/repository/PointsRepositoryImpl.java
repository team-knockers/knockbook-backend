package com.knockbook.backend.repository;

import com.knockbook.backend.entity.PointBalanceEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PointsRepositoryImpl implements PointsRepository {

    private final EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> findBalance(Long userId) {
        final var pb = em.find(PointBalanceEntity.class, userId);
        return Optional.ofNullable(pb).map(PointBalanceEntity::getBalance);
    }
}
