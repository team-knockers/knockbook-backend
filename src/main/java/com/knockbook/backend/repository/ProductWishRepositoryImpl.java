package com.knockbook.backend.repository;

import com.knockbook.backend.entity.ProductWishlistEntity;
import com.knockbook.backend.entity.QProductWishlistEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductWishRepositoryImpl implements ProductWishRepository{
    private final JPAQueryFactory query;
    private final EntityManager em;

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
}
