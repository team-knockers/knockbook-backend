package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Cart;
import com.knockbook.backend.domain.CartItem;
import com.knockbook.backend.entity.CartEntity;
import com.knockbook.backend.entity.CartItemEntity;
import com.knockbook.backend.entity.QCartEntity;
import com.knockbook.backend.entity.QCartItemEntity;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public Optional<Cart> findOpenByUserId(Long userId) {
        final var c = QCartEntity.cartEntity;
        final var i = QCartItemEntity.cartItemEntity;

        final var cartEntity = query.selectFrom(c)
                .where(c.userId.eq(userId), c.status.eq(CartEntity.Status.OPEN))
                .fetchOne();
        if (cartEntity == null) return Optional.empty();

        final var itemsEntity = query.selectFrom(i)
                .where(i.cartId.eq(cartEntity.getId()))
                .fetch();

        return Optional.of(toDomain(cartEntity, itemsEntity));
    }

    @Override
    public Optional<Cart> findById(Long cartId) {
        final var c = QCartEntity.cartEntity;
        final var i = QCartItemEntity.cartItemEntity;

        final var cart = query.selectFrom(c)
                .where(c.id.eq(cartId))
                .fetchOne();

        if (cart == null) {
            return Optional.empty();
        }

        final var items = query.selectFrom(i)
                .where(i.cartId.eq(cartId))
                .fetch();

        return Optional.of(toDomain(cart, items));
    }

    @Override
    @Transactional
    public Cart createEmpty(Long userId) {
        final var existing = findOpenByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }

        final var e = CartEntity.builder()
                .userId(userId)
                .status(CartEntity.Status.OPEN)
                .itemCount(0)
                .subtotalAmount(0)
                .discountAmount(0)
                .shippingAmount(0)
                .rentalAmount(0)
                .totalAmount(0)
                .pointsEarnable(0)
                .build();

        em.persist(e);
        em.flush();
        return findById(e.getId()).orElseThrow();
    }

    @Override
    @Transactional
    public Cart addItem(Long cartId, CartItem item) {

        if (item.getRefType() == null || item.getRefId() == null) {
            throw new IllegalArgumentException("refType/refId required");
        }

        if (item.getCartId() != null && !item.getCartId().equals(cartId)) {
            throw new IllegalArgumentException("cartId mismatch");
        }

        final var i = QCartItemEntity.cartItemEntity;
        final var refTypeDomain = item.getRefType();
        final var refType = CartItemEntity.RefType.valueOf(refTypeDomain.name());
        final int rentalDays = (refTypeDomain == CartItem.RefType.BOOK_RENTAL)
                ? Math.max(1, nz(item.getRentalDays())) : 0;
        final int delta = Math.max(1, item.getQuantity());

        final var updated = query.update(i)
                .set(i.quantity, i.quantity.add(delta))
                .where(i.cartId.eq(cartId),
                        i.refType.eq(refType),
                        i.refId.eq(item.getRefId()),
                        i.rentalDays.coalesce(0).eq(rentalDays))
                .execute();

        if (updated == 0) {
            final var e = CartItemEntity.builder()
                    .cartId(cartId)
                    .refType(refType)
                    .refId(item.getRefId())
                    .rentalDays(rentalDays)
                    .quantity(delta)
                    .titleSnapshot(item.getTitleSnapshot())
                    .thumbnailUrl(item.getThumbnailUrl())
                    .listPrice(nz(item.getListPriceSnapshot()))
                    .salePrice(nz(item.getSalePriceSnapshot()))
                    .rentalPrice(nz(item.getRentalPriceSnapshot()))
                    .pointsRate(nz(item.getPointsRate()))
                    .build();
            em.persist(e);
        } else {
            query.update(i)
                    .set(i.titleSnapshot, i.titleSnapshot.coalesce(item.getTitleSnapshot()))
                    .set(i.thumbnailUrl, i.thumbnailUrl.coalesce(item.getThumbnailUrl()))
                    .set(i.salePrice, i.salePrice.coalesce(nz(item.getSalePriceSnapshot())))
                    .set(i.listPrice, i.listPrice.coalesce(nz(item.getListPriceSnapshot())))
                    .set(i.rentalPrice, i.rentalPrice.coalesce(nz(item.getRentalPriceSnapshot())))
                    .set(i.pointsRate, i.pointsRate.coalesce(nz(item.getPointsRate())))
                    .where(i.cartId.eq(cartId),
                            i.refType.eq(refType),
                            i.refId.eq(item.getRefId()),
                            i.rentalDays.coalesce(0).eq(rentalDays))
                    .execute();
        }

        em.flush();
        recalcAndPersist(cartId);
        return findById(cartId).orElseThrow();
    }

    @Override
    @Transactional
    public Cart deleteItem(Long cartId, Long cartItemId) {
        final var i = QCartItemEntity.cartItemEntity;
        final var n = query.delete(i)
                .where(i.id.eq(cartItemId), i.cartId.eq(cartId))
                .execute();
        if (n == 0) {
            throw new IllegalArgumentException("cart item not found or not in cart: " + cartItemId);
        }

        em.flush();
        recalcAndPersist(cartId);
        return findById(cartId).orElseThrow();
    }

    private void recalcAndPersist(Long cartId) {
        final var c = QCartEntity.cartEntity;
        final var i = QCartItemEntity.cartItemEntity;

        final var itemCountExpr = i.quantity.sum().coalesce(0);

        final var subtotalPerRow = new CaseBuilder()
                .when(i.refType.ne(CartItemEntity.RefType.BOOK_RENTAL))
                .then(i.listPrice.coalesce(i.salePrice).coalesce(0).multiply(i.quantity))
                .otherwise(0);
        final var subtotalExpr = subtotalPerRow.sum().coalesce(0);

        final var effectiveUnitPrice = new CaseBuilder()
                .when(i.salePrice.isNull().or(i.salePrice.loe(0)))
                .then(i.listPrice.coalesce(0))
                .otherwise(i.salePrice.coalesce(0));

        final var discountedPerRow = new CaseBuilder()
                .when(i.refType.ne(CartItemEntity.RefType.BOOK_RENTAL))
                .then(effectiveUnitPrice.multiply(i.quantity))
                .otherwise(0);
        final var discountedExpr = discountedPerRow.sum().coalesce(0);

        final var rentalPerRow = new CaseBuilder()
                .when(i.refType.eq(CartItemEntity.RefType.BOOK_RENTAL))
                .then(i.rentalPrice.coalesce(0).multiply(i.quantity))
                .otherwise(0);
        final var rentalExpr = rentalPerRow.sum().coalesce(0);

        final var nonRentalPointsPerRow =
                i.pointsRate.coalesce(0)
                        .multiply(effectiveUnitPrice.multiply(i.quantity))
                        .divide(100);
        final var rentalPointsPerRow =
                i.pointsRate.coalesce(0)
                        .multiply(i.rentalPrice.coalesce(0).multiply(i.quantity))
                        .divide(100);

        final var pointsPerRow = new CaseBuilder()
                .when(i.refType.eq(CartItemEntity.RefType.BOOK_RENTAL)).then(rentalPointsPerRow)
                .otherwise(nonRentalPointsPerRow);
        final var pointsExpr = pointsPerRow.sum().coalesce(0);

        final var agg = query.select(itemCountExpr, subtotalExpr, discountedExpr, rentalExpr, pointsExpr)
                .from(i).where(i.cartId.eq(cartId)).fetchOne();

        final var itemCount  = agg.get(0, Integer.class);
        final var subtotal   = agg.get(1, Integer.class);
        final var discounted = agg.get(2, Integer.class);
        final var rental     = agg.get(3, Integer.class);
        final var points     = agg.get(4, Integer.class);
        final var total      = rental + discounted;

        final var n = query.update(c)
                .set(c.itemCount, itemCount)
                .set(c.subtotalAmount, subtotal)
                .set(c.discountAmount, discounted)
                .set(c.rentalAmount, rental)
                .set(c.totalAmount, total)
                .set(c.pointsEarnable, points)
                .where(c.id.eq(cartId))
                .execute();
        if (n == 0) { throw new IllegalStateException("cart update failed: " + cartId); }
        em.flush();
        em.clear();
    }

    private static int nz(Integer v) { return v == null ? 0 : v; }

    private Cart toDomain(CartEntity e, List<CartItemEntity> items) {
        return Cart.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .status(Cart.Status.valueOf(e.getStatus().name()))
                .items(items.stream().map(CartItemEntity::toDomain).toList())
                .itemCount(e.getItemCount())
                .subtotalAmount(e.getSubtotalAmount())
                .discountAmount(e.getDiscountAmount())
                .shippingAmount(e.getShippingAmount())
                .rentalAmount(e.getRentalAmount())
                .totalAmount(e.getTotalAmount())
                .pointsEarnable(e.getPointsEarnable())
                .build();
    }
}
