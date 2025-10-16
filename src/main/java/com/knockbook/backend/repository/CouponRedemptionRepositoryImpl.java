package com.knockbook.backend.repository;

import com.knockbook.backend.domain.CouponRedemption;
import com.knockbook.backend.entity.CouponRedemptionEntity;
import com.knockbook.backend.entity.QCouponRedemptionEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponRedemptionRepositoryImpl implements CouponRedemptionRepository {

    private final EntityManager em;
    private final JPAQueryFactory qf;
    private static final QCouponRedemptionEntity qRed = QCouponRedemptionEntity.couponRedemptionEntity;

    @Override
    public boolean existsByIssuanceId(final Long issuanceId) {
        Long cnt = qf.select(qRed.id.count())
                .from(qRed)
                .where(qRed.issuanceId.eq(issuanceId))
                .fetchOne();
        return cnt != null && cnt > 0;
    }

    @Override
    @Transactional
    public CouponRedemption save(final CouponRedemption domin) {
        var entity = CouponRedemptionEntity.toEntity(domin);
        try {
            if (entity.getId() == null) {
                em.persist(entity);
            } else {
                entity = em.merge(entity);
            }
        } catch (PersistenceException ex) {
            Throwable root = ex.getCause();
            if (root instanceof ConstraintViolationException
                    || String.valueOf(ex.getMessage()).contains("uk_cr_issuance_once")) {
                throw new IllegalStateException("COUPON_ALREADY_REDEEMED", ex);
            }
            throw ex;
        }
        return entity.toDomain();
    }
}

