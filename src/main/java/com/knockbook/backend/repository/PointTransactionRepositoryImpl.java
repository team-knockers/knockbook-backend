package com.knockbook.backend.repository;

import com.knockbook.backend.domain.PointTransaction;
import com.knockbook.backend.entity.PointTransactionEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointTransactionRepositoryImpl implements PointTransactionRepository {

    private final EntityManager em;

    @Override
    @Transactional
    public PointTransaction save(PointTransaction tx) {
        var entity = PointTransactionEntity.toEntity(tx);
        if (entity.getId() == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        return entity.toDomain();
    }
}
