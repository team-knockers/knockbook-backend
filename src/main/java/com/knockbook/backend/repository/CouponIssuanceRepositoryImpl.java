package com.knockbook.backend.repository;

import com.knockbook.backend.domain.CouponIssuance;
import com.knockbook.backend.entity.CouponEntity;
import com.knockbook.backend.entity.CouponIssuanceEntity;
import com.knockbook.backend.entity.QCouponEntity;
import com.knockbook.backend.entity.QCouponIssuanceEntity;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponIssuanceRepositoryImpl implements CouponIssuanceRepository {

    private final JPAQueryFactory qf;
    private final EntityManager em;

    private static final QCouponIssuanceEntity ci = QCouponIssuanceEntity.couponIssuanceEntity;
    private static final QCouponEntity c = QCouponEntity.couponEntity;
    private static final QCouponIssuanceEntity iss = QCouponIssuanceEntity.couponIssuanceEntity;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Override
    @Transactional
    public void insertIfNotExists(final Long userId,
                                  final List<Long> couponIds) {
        final var c = QCouponEntity.couponEntity;
        final var ci = QCouponIssuanceEntity.couponIssuanceEntity;
        final var coupons = qf.selectFrom(c).where(c.id.in(couponIds)).fetch();

        if (coupons.isEmpty()) { return; }

        final var validIds = coupons.stream().map(CouponEntity::getId).toList();
        final var counts = qf.select(ci.couponId, ci.id.count())
                .from(ci)
                .where(ci.userId.eq(userId), ci.couponId.in(validIds))
                .groupBy(ci.couponId)
                .fetch()
                .stream()
                .collect(Collectors.toMap(t -> t.get(ci.couponId), t -> t.get(ci.id.count())));

        final var issuedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        for (final var coupon : coupons) {
            final var current = counts.getOrDefault(coupon.getId(), 0L);
            final var limit   = coupon.getPerUserLimit() == null ? 1L : coupon.getPerUserLimit();
            if (current >= limit) { continue; }

            final LocalDateTime couponEndsAt = coupon.getEndsAt();
            if (couponEndsAt != null && !issuedAt.isBefore(couponEndsAt)) {
                continue;
            }

            final var validityMonths = coupon.getValidityMonths();
            var expiresAt = (validityMonths != null && validityMonths > 0)
                    ? issuedAt.plusMonths(validityMonths)
                    : couponEndsAt;

            if (expiresAt.isAfter(couponEndsAt)) {
                expiresAt = couponEndsAt;
            }

            em.persist(CouponIssuanceEntity.builder()
                    .couponId(coupon.getId())
                    .userId(userId)
                    .issuedAt(issuedAt)
                    .expiresAt(expiresAt)
                    .status(CouponIssuanceEntity.IssuanceStatus.AVAILABLE)
                    .build());
        }
    }


    @Override
    public List<CouponIssuance> findByUserId(final Long userId,
                                             final CouponIssuance.Status status) {
        return qf.select(ci, c)
                .from(ci)
                .join(c).on(ci.couponId.eq(c.id))
                .where(ci.userId.eq(userId), statusEq(status))
                .orderBy(ci.issuedAt.desc(), ci.id.desc())
                .fetch()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<CouponIssuance> findByIdAndUserId(Long id, Long userId) {
        final var t = qf.select(ci, c)
                .from(ci)
                .join(c).on(ci.couponId.eq(c.id))
                .where(ci.id.eq(id), ci.userId.eq(userId))
                .fetchOne();
        return Optional.ofNullable(t).map(this::toDomain);
    }

    @Override
    public Optional<CouponIssuance> findByIdAndUserIdForUpdate(Long id, Long userId) {
        final var entity = qf.selectFrom(iss)
                .where(iss.id.eq(id).and(iss.userId.eq(userId)))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne();
        return Optional.ofNullable(entity).map(CouponIssuanceEntity::toDomain);
    }

    @Override
    @Transactional
    public CouponIssuance save(CouponIssuance issuance) {
        var entity = CouponIssuanceEntity.toEntity(issuance);
        if (entity.getId() == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        return entity.toDomain();
    }

    private CouponIssuance toDomain(Tuple t) {
        CouponIssuanceEntity ie = t.get(ci);
        CouponEntity ce = t.get(c);
        return CouponIssuance.builder()
                .id(ie.getId())
                .couponId(ie.getCouponId())
                .userId(ie.getUserId())
                .issuedAt(ie.getIssuedAt() != null ? ie.getIssuedAt().atZone(KST).toInstant() : null)
                .expiresAt(ie.getExpiresAt() != null ? ie.getExpiresAt().atZone(KST).toInstant() : null)
                .status(CouponIssuance.Status.valueOf(ie.getStatus().name()))
                .code(ce.getCode())
                .name(ce.getName())
                .type(ce.getType().name())
                .discountAmount(ce.getDiscountAmount())
                .discountRateBp(ce.getDiscountRateBp())
                .maxDiscountAmount(ce.getMaxDiscountAmount())
                .minOrderAmount(ce.getMinOrderAmount())
                .scope(ce.getScope().name())
                .build();
    }

    private BooleanExpression statusEq(CouponIssuance.Status s) {
        return s == null ? null : ci.status.eq(CouponIssuanceEntity.IssuanceStatus.valueOf(s.name()));
    }
}
