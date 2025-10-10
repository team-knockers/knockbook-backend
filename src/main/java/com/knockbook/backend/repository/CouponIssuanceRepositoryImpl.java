package com.knockbook.backend.repository;

import com.knockbook.backend.entity.CouponEntity;
import com.knockbook.backend.entity.CouponIssuanceEntity;
import com.knockbook.backend.entity.QCouponEntity;
import com.knockbook.backend.entity.QCouponIssuanceEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CouponIssuanceRepositoryImpl implements CouponIssuanceRepository {

    private final JPAQueryFactory qf;
    private final EntityManager em;

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
}
