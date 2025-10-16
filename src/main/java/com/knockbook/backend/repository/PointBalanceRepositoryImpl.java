package com.knockbook.backend.repository;

import com.knockbook.backend.domain.PointBalance;
import com.knockbook.backend.entity.PointBalanceEntity;
import com.knockbook.backend.entity.QPointBalanceEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointBalanceRepositoryImpl implements PointBalanceRepository {

    private final EntityManager em;
    private final JPAQueryFactory qf;
    private static final QPointBalanceEntity qBal = QPointBalanceEntity.pointBalanceEntity;

    @Override
    public Optional<PointBalance> findByUserIdForUpdate(final Long userId) {
        final var entity = qf.selectFrom(qBal)
                .where(qBal.userId.eq(userId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne();
        return Optional.ofNullable(entity).map(PointBalanceEntity::toDomain);
    }

    @Override
    @Transactional
    public PointBalance save(final PointBalance balance) {
        var entity = PointBalanceEntity.toEntity(balance);
        if (entity.getUserId() == null) {
            throw new IllegalArgumentException("USER_ID_REQUIRED");
        }
        if (em.find(PointBalanceEntity.class, entity.getUserId()) == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        return entity.toDomain();
    }
}

