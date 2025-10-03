package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Cart;
import com.knockbook.backend.domain.CartItem;
import com.knockbook.backend.entity.CartEntity;
import com.knockbook.backend.entity.CartItemEntity;
import com.knockbook.backend.entity.QCartEntity;
import com.knockbook.backend.entity.QCartItemEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepository {

    private EntityManager em;
    private JPAQueryFactory query;

    @Override
    public Optional<Cart> findOpenByUserId(Long userId) {
        final var c = QCartEntity.cartEntity;
        final var i = QCartItemEntity.cartItemEntity;

        final var cartEntity = query.selectFrom(c)
                .where(c.userId.eq(userId), c.status.eq(CartEntity.Status.OPEN))
                .fetchOne();

        if (cartEntity == null) {
            return Optional.empty();
        }

        final var itemsEntity = query.selectFrom(i)
                .where(i.cartId.eq(cartEntity.getId()))
                .fetch();

        return Optional.of(toDomain(cartEntity, itemsEntity));
    }

    @Override
    public Optional<Cart> findById(Long cartId) {
        final var c = QCartEntity.cartEntity;
        final var i = QCartItemEntity.cartItemEntity;

        final var cart = query
                .selectFrom(c)
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
        if (existing.isPresent()) return existing.get();

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
        final var rentalDays = (refTypeDomain == CartItem.RefType.BOOK_RENTAL) ?
                Math.max(1, item.getRentalDays()) : 0;
        final var delta = Math.max(1, item.getQuantity());

        final var updated = query.update(i)
                .set(i.quantity, i.quantity.add(delta))
                .where(i.cartId.eq(cartId),
                        i.refType.eq(refType),
                        i.refId.eq(item.getRefId()),
                        i.rentalDays.eq(rentalDays))
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
                    .listPrice(item.getListPriceSnapshot())
                    .salePrice(item.getSalePriceSnapshot())
                    .rentalPrice(item.getRentalPriceSnapshot())
                    .pointsRate(item.getPointsRate())
                    .build();
            em.persist(e);
        }

        em.flush();
        recalcAndPersist(cartId);
        return findById(cartId).orElseThrow();
    }

    @Override
    @Transactional
    public Cart deleteItem(Long cartId, Long cartItemId) {
        final var i = QCartItemEntity.cartItemEntity;
        final var target = query.selectFrom(i)
                .where(i.id.eq(cartItemId), i.cartId.eq(cartId))
                .fetchOne();

        if (target == null) {
            throw new IllegalArgumentException(
                    "cart item not found or not in cart: " + cartItemId);
        }

        em.remove(target);
        em.flush();

        recalcAndPersist(cartId);
        return findById(cartId).orElseThrow();
    }

    private void recalcAndPersist(Long cartId) {
        final var c = em.find(CartEntity.class, cartId);
        if (c == null) {
            throw new IllegalArgumentException("cart not found: " + cartId);
        }

        final var i = QCartItemEntity.cartItemEntity;
        final var items = query
                .selectFrom(i)
                .where(i.cartId.eq(cartId))
                .fetch();

        final var itemCount = items.stream()
                .mapToInt(CartItemEntity::getQuantity)
                .sum();

        final var subtotal = items.stream()
                .filter(item ->
                        item.getRefType() != CartItemEntity.RefType.BOOK_RENTAL)
                .mapToInt(item -> {
                    int unit = (item.getSalePrice()!=null) ? item.getSalePrice()
                            : (item.getListPrice()!=null) ? item.getListPrice() : 0;
                    return unit * item.getQuantity();
                }).sum();

        final var rental = items.stream()
                .filter(item ->
                        item.getRefType() == CartItemEntity.RefType.BOOK_RENTAL)
                .mapToInt(item -> {
                    final var rentalPrice = item.getRentalPrice();
                    final var perDay = (rentalPrice != null) ? rentalPrice : 0;
                    return perDay * item.getRentalDays() * item.getQuantity();
                }).sum();

        final var discount = c.getDiscountAmount();
        final var shipping = c.getShippingAmount();
        final var total = subtotal + rental + shipping - discount;

        final var points = items.stream()
                .mapToInt(item -> {
                    final var refType = item.getRefType();
                    final var base = ( refType == CartItemEntity.RefType.BOOK_RENTAL)
                            ? ((long) (item.getRentalPrice() != null ? item.getRentalPrice() : 0)
                            * item.getRentalDays() * item.getQuantity())
                            : ((long) ((item.getSalePrice() != null) ?
                                item.getSalePrice()
                                : (item.getListPrice() != null ? item.getListPrice() : 0))
                                * item.getQuantity());
                    return  BigDecimal.valueOf(item.getPointsRate())
                            .multiply(BigDecimal.valueOf(base))
                            .divide(BigDecimal.valueOf(100), RoundingMode.DOWN)
                            .intValue();
                }).sum();

        final var updated = CartEntity.builder()
                        .id(c.getId())
                        .userId(c.getUserId())
                        .status(c.getStatus())
                        .itemCount(itemCount)
                        .subtotalAmount(subtotal)
                        .rentalAmount(rental)
                        .totalAmount(total)
                        .pointsEarnable(points);
        em.merge(c);
        em.flush();
    }

    private Cart toDomain(CartEntity e,
                          List<CartItemEntity> items) {
        return Cart.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .status(Cart.Status.valueOf(e.getStatus().name()))
                .items(items.stream().map(this::toDomain).toList())
                .itemCount(e.getItemCount())
                .subtotalAmount(e.getSubtotalAmount())
                .discountAmount(e.getDiscountAmount())
                .shippingAmount(e.getShippingAmount())
                .rentalAmount(e.getRentalAmount())
                .totalAmount(e.getTotalAmount())
                .pointsEarnable(e.getPointsEarnable())
                .build();
    }

    private CartItem toDomain(CartItemEntity i) {
        return CartItem.builder()
                .id(i.getId())
                .cartId(i.getCartId())
                .refType(CartItem.RefType.valueOf(i.getRefType().name()))
                .refId(i.getRefId())
                .titleSnapshot(i.getTitleSnapshot())
                .thumbnailUrl(i.getThumbnailUrl())
                .listPriceSnapshot(i.getListPrice())
                .salePriceSnapshot(i.getSalePrice())
                .rentalDays(i.getRentalDays())
                .rentalPriceSnapshot(i.getRentalPrice())
                .quantity(i.getQuantity())
                .pointsRate(i.getPointsRate())
                .build();
    }
}
