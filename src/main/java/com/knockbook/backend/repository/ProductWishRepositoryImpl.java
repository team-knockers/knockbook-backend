package com.knockbook.backend.repository;

import com.knockbook.backend.domain.ProductSummary;
import com.knockbook.backend.domain.ProductWishlist;
import com.knockbook.backend.entity.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Repository
@RequiredArgsConstructor
public class ProductWishRepositoryImpl implements ProductWishRepository{
    private final JPAQueryFactory query;
    private final EntityManager em;

    private static final QProductEntity P = QProductEntity.productEntity;
    private static final QProductImageEntity PI = QProductImageEntity.productImageEntity;
    private static final QProductWishlistEntity W = QProductWishlistEntity.productWishlistEntity;

    @Override
    public boolean insertWishlist(
            Long productId,
            Long userId
    ) {
        final var WishRowExists = query.selectOne()
                .from(W)
                .where(W.productId.eq(productId).and(W.userId.eq(userId)))
                .fetchFirst() != null;
        if(WishRowExists) { return false; }

        final var wish = ProductWishlistEntity.builder()
                .productId(productId)
                .userId(userId)
                .build();

        em.persist(wish);

        return true;
    }

    @Override
    public boolean deleteWishlist(
            Long productId,
            Long userId
    ) {
        final var affected = query.delete(W)
                .where(W.productId.eq(productId).and(W.userId.eq(userId)))
                .execute();

        return affected == 1;
    }

    @Override
    public ProductWishlist findWishlist(
        Long userId
    ) {
        final var rows = query
                .select(
                        P.productId,
                        P.categoryId,
                        P.sku,
                        P.name,
                        P.unitPriceAmount,
                        P.salePriceAmount,
                        P.stockQty,
                        P.status,
                        P.availability,
                        P.averageRating,
                        P.reviewCount,
                        PI.imageUrl
                )
                .from(W)
                .join(P).on(P.productId.eq(W.productId))
                .leftJoin(PI).on(
                        PI.productId.eq(P.productId)
                                .and(PI.imageUsage.eq(ProductImageEntity.ImageUsage.GALLERY))
                                .and(PI.sortOrder.eq(1))
                )
                .where(
                        W.userId.eq(userId)
                                .and(P.status.eq(ProductEntity.Status.ACTIVE))
                                .and(P.deletedAt.isNull())
                )
                .orderBy(W.createdAt.desc())
                .fetch();

        final var content = rows.stream()
                .map(t -> ProductSummary.builder()
                        .id(t.get(P.productId))
                        .categoryId(t.get(P.categoryId))
                        .sku(t.get(P.sku))
                        .name(t.get(P.name))
                        .unitPriceAmount(t.get(P.unitPriceAmount))
                        .salePriceAmount(t.get(P.salePriceAmount))
                        .stockQty(t.get(P.stockQty))
                        // Entity enum → domain enum
                        .status(
                                t.get(P.status) == null ? null
                                        : ProductSummary.Status.valueOf(t.get(P.status).name())
                        )
                        .availability(
                                t.get(P.availability) == null ? null
                                        : ProductSummary.Availability.valueOf(t.get(P.availability).name())
                        )
                        // BigDecimal → double (1dp, HALF_UP)
                        .averageRating(toScale(t.get(P.averageRating)))
                        .reviewCount(t.get(P.reviewCount))
                        .thumbnailUrl(t.get(PI.imageUrl))
                        .build())
                .toList();


        final var result = ProductWishlist.builder()
                .products(content)
                .build();

        return result;
    }

    private static Double toScale(BigDecimal v) {
        return v == null ? null : v.setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

}
