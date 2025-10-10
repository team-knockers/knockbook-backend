package com.knockbook.backend.repository;

import com.knockbook.backend.entity.CouponEntity;
import com.knockbook.backend.entity.QCouponEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final JPAQueryFactory query;

    @Override
    public List<Long> findActiveIdsByCodes(List<String> codes, Instant now) {
        final var c = QCouponEntity.couponEntity;
        final var ldtNow = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        return query
                .select(c.id)
                .from(c)
                .where(
                        c.code.in(codes),
                        c.status.eq(CouponEntity.CouponStatus.ACTIVE),
                        c.startsAt.loe(ldtNow),
                        c.endsAt.goe(ldtNow)
                )
                .fetch();
    }
}
