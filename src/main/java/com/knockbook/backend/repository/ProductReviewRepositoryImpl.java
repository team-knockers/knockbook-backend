package com.knockbook.backend.repository;

import com.knockbook.backend.domain.ProductReview;
import com.knockbook.backend.domain.ProductReviewStats;
import com.knockbook.backend.domain.ProductReviewsResult;
import com.knockbook.backend.entity.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.JPAExpressions;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class ProductReviewRepositoryImpl implements ProductReviewRepository{
    private final JPAQueryFactory query;
    private final EntityManager em;

    // QueryDSL Q-types (entity metamodels)
    private static final QProductReviewEntity PR = QProductReviewEntity.productReviewEntity;
    private static final QUserEntity U = QUserEntity.userEntity;
    private static final QProductReviewLikeEntity PRL = QProductReviewLikeEntity.productReviewLikeEntity;
    private static final QProductEntity       P  = QProductEntity.productEntity;

    @Override
    public ProductReviewsResult findProductReviews (
            Long productId,
            Long userId,
            Pageable pageable
    ) {
        // filters: product + visible + not deleted
        final var predicate = new BooleanBuilder()
                .and(PR.productId.eq(productId))
                .and(PR.status.eq(ProductReviewEntity.Status.VISIBLE))
                .and(PR.deletedAt.isNull());

        // sort from pageable
        final var orderSpecifiers = toOrderSpecifiers(pageable, PR);

        // likedByMe check (EXISTS review_likes)
        final var likedByMeExpr = JPAExpressions.selectOne()
                .from(PRL)
                .where(PRL.reviewId.eq(PR.reviewId)
                        .and(PRL.userId.eq(userId)))
                .exists();

        // fetch rows for the current page
        final var rows = query
                .select(
                        PR.reviewId,
                        U.displayName,
                        PR.body,
                        PR.rating,
                        PR.createdAt,
                        PR.likesCount,
                        likedByMeExpr
                )
                .from(PR)
                .join(U).on(PR.userId.eq(U.id))
                .where(predicate)
                .orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // map rows → domain
        final var productReviews = rows.stream()
                .map(t -> ProductReview.builder()
                        .reviewId(t.get(PR.reviewId))
                        .displayName(t.get(U.displayName))
                        .body(t.get(PR.body))
                        .rating(t.get(PR.rating))
                        .createdAt(t.get(PR.createdAt))
                        .likesCount(t.get(PR.likesCount))
                        .likedByMe(Boolean.TRUE.equals(t.get(likedByMeExpr)))
                        .build())
                .toList();

        // stats query: avg + rating counts (5..1) + total
        final var avgExpr = PR.rating.avg();
        final var cntExpr = PR.count();
        final var c5 = PR.rating.when(5).then(1).otherwise(0).sum();
        final var c4 = PR.rating.when(4).then(1).otherwise(0).sum();
        final var c3 = PR.rating.when(3).then(1).otherwise(0).sum();
        final var c2 = PR.rating.when(2).then(1).otherwise(0).sum();
        final var c1 = PR.rating.when(1).then(1).otherwise(0).sum();

        final var stat = query
                .select(avgExpr, c5, c4, c3, c2, c1, cntExpr)
                .from(PR)
                .where(predicate)
                .fetchOne();

        // stats values
        final var averageRating = toOneDecimal(stat == null ? null : stat.get(avgExpr));
        final var totalItems    = stat == null ? 0L : Objects.requireNonNullElse(stat.get(cntExpr), 0L);
        final var starCounts = new HashMap<Integer, Integer>(5);
        starCounts.put(5, stat == null ? 0 : Objects.requireNonNullElse(stat.get(c5), 0));
        starCounts.put(4, stat == null ? 0 : Objects.requireNonNullElse(stat.get(c4), 0));
        starCounts.put(3, stat == null ? 0 : Objects.requireNonNullElse(stat.get(c3), 0));
        starCounts.put(2, stat == null ? 0 : Objects.requireNonNullElse(stat.get(c2), 0));
        starCounts.put(1, stat == null ? 0 : Objects.requireNonNullElse(stat.get(c1), 0));

        final var stats = ProductReviewStats.builder()
                .averageRating(averageRating)
                .starCounts(starCounts)
                .totalItems(totalItems)
                .build();

        // page info
        final var page      = pageable.getPageNumber() + 1;
        final var size       = pageable.getPageSize();
        final var totalPages = (size == 0) ? 0 : (int) Math.ceil(totalItems / (double) size);

        // build result
        final var result = ProductReviewsResult.builder()
                .productReviews(productReviews)
                .page(page)
                .size(size)
                .totalPages(totalPages)
                .stats(stats)
                .build();

        return result;
    }

    @Override
    public ProductReview insertReview(final Long productId, final Long userId, final String body, final int rating) {
        final var e = ProductReviewEntity.builder()
                .productId(productId)
                .userId(userId)
                .body(body)
                .rating(rating)
                .status(ProductReviewEntity.Status.VISIBLE)
                .build();
        em.persist(e);
        em.flush();
        em.refresh(e);

        final String displayName = query
                .select(U.displayName)
                .from(U)
                .where(U.id.eq(userId))
                .fetchOne();

        recalcProductStats(productId);

        return ProductReview.builder()
                .reviewId(e.getReviewId())
                .displayName(displayName)
                .body(e.getBody())
                .rating(e.getRating())
                .createdAt(e.getCreatedAt())
                .likesCount(0)
                .likedByMe(false)
                .build();
    }

    @Override
    public boolean deleteReview(final Long productId, final Long reviewId, final Long userId) {
        final long affected = query.update(PR)
                .set(PR.status, ProductReviewEntity.Status.HIDDEN)
                .set(PR.deletedAt, Instant.now())
                .where(PR.reviewId.eq(reviewId)
                        .and(PR.userId.eq(userId))
                        .and(PR.deletedAt.isNull()))
                .execute();
        if (affected == 1) { recalcProductStats(productId); }
        return affected == 1;
    }

    private void recalcProductStats(final Long productId) {
        final var cntExpr = PR.reviewId.count();
        final var avgExpr = PR.rating.avg();

        final Tuple stats = query
                .select(cntExpr, avgExpr)
                .from(PR)
                .where(PR.productId.eq(productId)
                        .and(PR.status.eq(ProductReviewEntity.Status.VISIBLE))
                        .and(PR.deletedAt.isNull()))
                .fetchOne();

        final long cnt = stats == null || stats.get(cntExpr) == null ? 0L : stats.get(cntExpr);
        final double avg = stats == null || stats.get(avgExpr) == null ? 0.0 : stats.get(avgExpr);
        final double avg1 = Math.round(avg * 10.0) / 10.0;

        query.update(P)
                .set(P.reviewCount, (int) cnt)
                .set(P.averageRating, BigDecimal.valueOf(avg1))
                .where(P.productId.eq(productId))
                .execute();
    }

    @Override
    public boolean addLikeIfAbsent(Long reviewId, Long userId) {
        boolean likeRowExists = query.selectOne()
                .from(PRL)
                .where(PRL.reviewId.eq(reviewId).and(PRL.userId.eq(userId)))
                .fetchFirst() != null;
        if (likeRowExists) return false;

        var like = ProductReviewLikeEntity.builder()
                .reviewId(reviewId)
                .userId(userId)
                .build();
        em.persist(like);
        return true;
    }

    @Override
    public boolean removeLikeIfPresent(Long reviewId, Long userId) {
        long affected = query.delete(PRL)
                .where(PRL.reviewId.eq(reviewId).and(PRL.userId.eq(userId)))
                .execute();
        return affected == 1;
    }

    @Override
    public void incrementLikesCount(Long reviewId) {
        query.update(PR)
                .set(PR.likesCount, PR.likesCount.add(1))
                .where(PR.reviewId.eq(reviewId))
                .execute();
    }

    @Override
    public void decrementLikesCount(Long reviewId) {
        query.update(PR)
                .set(PR.likesCount, PR.likesCount.subtract(1))
                .where(PR.reviewId.eq(reviewId).and(PR.likesCount.gt(0)))
                .execute();
    }

    // pageable sort → QueryDSL order
    private static List<OrderSpecifier<?>> toOrderSpecifiers(Pageable pageable, QProductReviewEntity pr) {
        final var list = new ArrayList<OrderSpecifier<?>>();
        pageable.getSort().forEach(o -> {
            final var asc = o.isAscending();
            switch (o.getProperty()) {
                case "createdAt"    -> list.add(asc ? pr.createdAt.asc() : pr.createdAt.desc());
                case "rating"       -> {
                    list.add(asc ? pr.rating.asc() : pr.rating.desc());
                    list.add(pr.createdAt.desc());
                }
                case "likesCount"   -> {
                    list.add(asc ? pr.likesCount.asc() : pr.likesCount.desc());
                    list.add(pr.createdAt.desc());
                }
                default -> { /* ignore unknown keys */ }
            }
        });
        if (list.isEmpty()) {
            list.add(pr.createdAt.desc());
        }
        return list;
    }
    // round to 1 decimal (HALF_UP)
    private static double toOneDecimal(Double v) {
        if (v == null) return 0.0;
        return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
