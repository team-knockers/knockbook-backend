package com.knockbook.backend.repository;

import com.knockbook.backend.domain.CartItem;
import com.knockbook.backend.domain.OrderAggregate;
import com.knockbook.backend.domain.OrderItem;
import com.knockbook.backend.entity.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderRepositoryImpl implements OrderRepository {

    private final EntityManager em;
    private final JPAQueryFactory qf;

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final QOrderEntity qOrder = QOrderEntity.orderEntity;
    private static final QOrderItemEntity qOrderItem = QOrderItemEntity.orderItemEntity;
    private static final QUserAddressEntity qAddress = QUserAddressEntity.userAddressEntity;

    @Override
    @Transactional
    public OrderAggregate saveDraftFromCart(OrderAggregate aggregate, List<CartItem> items) {
        final var order = OrderEntity.fromModel(aggregate);

        final var defaultAddressId = qf
                .select(qAddress.id)
                .from(qAddress)
                .where(
                        qAddress.userId.eq(aggregate.getUserId()),
                        qAddress.isDefault.isTrue()
                )
                .orderBy(qAddress.id.desc())
                .fetchFirst();

        order.setShippingAddressId(defaultAddressId);
        em.persist(order);
        em.flush();
        final var orderId = order.getId();

        em.refresh(order);
        final var createdAt = order.getCreatedAt();
        final var orderNo = "O" + createdAt.format(YMD) + String.format("%06d", order.getId());

        qf.update(qOrder)
                .set(qOrder.orderNo, orderNo)
                .where(qOrder.id.eq(order.getId()))
                .execute();
        em.refresh(order);

        final var savedDomainItems = new ArrayList<OrderItem>();
        int subtotal = 0, discount = 0, rental = 0, total = 0, points = 0, count = 0;

        for (final var s : items) {
            final var unitList   = nz(s.getListPriceSnapshot());
            final var unitSale   = nz(s.getSalePriceSnapshot());
            final var unitRental = nz(s.getRentalPriceSnapshot());
            final var qty        = (s.getQuantity() == null ? 1 : s.getQuantity());

            final int lineSubtotal;
            final int lineDiscount;
            final int lineTotal;
            switch (s.getRefType()) {
                case BOOK_PURCHASE, PRODUCT -> {
                    lineSubtotal = (unitList > 0 ? unitList : unitSale) * qty;
                    final var effective = (unitSale > 0 ? unitSale : unitList);
                    lineTotal = effective * qty;
                    lineDiscount = Math.max(0, lineSubtotal - lineTotal);
                }
                case BOOK_RENTAL -> {
                    lineSubtotal = unitRental * qty;
                    lineTotal = lineSubtotal;
                    lineDiscount = 0;
                    rental += lineTotal;
                }
                default -> throw new IllegalStateException("Unexpected refType: " + s.getRefType());
            }
            final var pointsRate = nz(s.getPointsRate());
            final var pointsItem = (lineTotal * pointsRate) / 100;

            subtotal += lineSubtotal;
            discount += lineDiscount;
            total    += lineTotal;
            points   += pointsItem;
            count    += qty;

            final var entity = OrderItemEntity.builder()
                    .orderId(orderId)
                    .refType(OrderItemEntity.RefType.valueOf(s.getRefType().name()))
                    .refId(s.getRefId())
                    .titleSnapshot(s.getTitleSnapshot())
                    .thumbnailUrl(s.getThumbnailUrl())
                    .listPriceSnapshot(unitList)
                    .salePriceSnapshot(unitSale)
                    .quantity(qty)
                    .rentalDays(nz(s.getRentalDays()))
                    .rentalPriceSnapshot(unitRental)
                    .pointsRate(pointsRate)
                    .pointsEarnedItem(pointsItem)
                    .lineSubtotalAmount(lineSubtotal)
                    .lineDiscountAmount(lineDiscount)
                    .lineTotalAmount(lineTotal)
                    .build();

            em.persist(entity);
            savedDomainItems.add(entity.toModel());
        }

        order.setItemCount(count);
        order.setSubtotalAmount(subtotal);
        order.setDiscountAmount(discount);
        order.setRentalAmount(rental);
        order.setCouponDiscountAmount(0);
        order.setTotalAmount(subtotal - discount + nz(order.getShippingAmount()));
        order.setPointsEarned(points);
        em.flush();

        return order.toDomain(savedDomainItems);
    }

    @Override
    @Transactional
    public Optional<OrderAggregate> findDraftById(Long userId, Long orderId) {
        final var order = em.find(OrderEntity.class, orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return Optional.empty();
        }

        final var oi = QOrderItemEntity.orderItemEntity;
        final var itemEntities = qf
                .selectFrom(oi)
                .where(oi.orderId.eq(orderId))
                .orderBy(oi.id.asc())
                .fetch();

        final var domainItems = itemEntities.stream()
                .map(OrderItemEntity::toModel).toList();

        final var aggregate = order.toDomain(domainItems);
        return Optional.of(aggregate);
    }

    @Override
    @Transactional
    public Optional<OrderAggregate> findPendingDraftByUser(Long userId) {
        final var entity = qf
                .selectFrom(qOrder)
                .where(
                        qOrder.userId.eq(userId),
                        qOrder.status.eq(com.knockbook.backend.entity.OrderEntity.OrderStatus.PENDING),
                        qOrder.paymentStatus.eq(com.knockbook.backend.entity.OrderEntity.PaymentStatus.READY)
                )
                .orderBy(qOrder.id.desc())
                .fetchFirst();

        if (entity == null) {
            return Optional.empty();
        }

        final var items = qf.selectFrom(qOrderItem)
                .where(qOrderItem.orderId.eq(entity.getId()))
                .orderBy(qOrderItem.id.asc())
                .fetch()
                .stream().map(OrderItemEntity::toModel).toList();

        return Optional.of(entity.toDomain(items));
    }

    @Override
    @Transactional
    public OrderAggregate replaceDraftFromCart(OrderAggregate existing, List<CartItem> items, boolean resetDiscounts) {
        final var order = em.find(OrderEntity.class, existing.getId());
        if (order == null || !order.getUserId().equals(existing.getUserId())) {
            throw new IllegalStateException("ORDER_NOT_OWNED");
        }

        qf.delete(qOrderItem).where(qOrderItem.orderId.eq(order.getId())).execute();

        final var savedDomainItems = new ArrayList<OrderItem>();
        int subtotal = 0, discount = 0, rental = 0, total = 0, points = 0, count = 0;

        for (final var s : items) {
            final var unitList   = nz(s.getListPriceSnapshot());
            final var unitSale   = nz(s.getSalePriceSnapshot());
            final var unitRental = nz(s.getRentalPriceSnapshot());
            final var qty        = (s.getQuantity() == null ? 1 : s.getQuantity());

            final int lineSubtotal, lineDiscount, lineTotal;
            switch (s.getRefType()) {
                case BOOK_PURCHASE, PRODUCT -> {
                    lineSubtotal = (unitList > 0 ? unitList : unitSale) * qty;
                    final var effective = (unitSale > 0 ? unitSale : unitList);
                    lineTotal = effective * qty;
                    lineDiscount = Math.max(0, lineSubtotal - lineTotal);
                }
                case BOOK_RENTAL -> {
                    lineSubtotal = unitRental * qty;
                    lineTotal = lineSubtotal;
                    lineDiscount = 0;
                    rental += lineTotal;
                }
                default -> throw new IllegalStateException("Unexpected refType: " + s.getRefType());
            }

            final var pointsRate = nz(s.getPointsRate());
            final var pointsItem = (lineTotal * pointsRate) / 100;

            subtotal += lineSubtotal;
            discount += lineDiscount;
            total    += lineTotal;
            points   += pointsItem;
            count    += qty;

            final var entity = OrderItemEntity.builder()
                    .orderId(order.getId())
                    .refType(OrderItemEntity.RefType.valueOf(s.getRefType().name()))
                    .refId(s.getRefId())
                    .titleSnapshot(s.getTitleSnapshot())
                    .thumbnailUrl(s.getThumbnailUrl())
                    .listPriceSnapshot(unitList)
                    .salePriceSnapshot(unitSale)
                    .quantity(qty)
                    .rentalDays(nz(s.getRentalDays()))
                    .rentalPriceSnapshot(unitRental)
                    .pointsRate(pointsRate)
                    .pointsEarnedItem(pointsItem)
                    .lineSubtotalAmount(lineSubtotal)
                    .lineDiscountAmount(lineDiscount)
                    .lineTotalAmount(lineTotal)
                    .build();

            em.persist(entity);
            savedDomainItems.add(entity.toModel());
        }

        order.setItemCount(count);
        order.setSubtotalAmount(subtotal);
        order.setDiscountAmount(discount);
        order.setCouponDiscountAmount(0);
        order.setRentalAmount(rental);
        order.setTotalAmount(subtotal - discount + nz(order.getShippingAmount()));
        order.setPointsEarned(points);

        if (resetDiscounts) {
            if (order.getAppliedCouponIssuanceId() != null) {
                order.setAppliedCouponIssuanceId(null);
                em.flush();
            }
            order.setPointsSpent(0);
        }

        em.flush();
        return order.toDomain(savedDomainItems);
    }

    @Override
    @Transactional
    public OrderAggregate updateDraftAmountsAndCoupon(OrderAggregate draft) {
        final var order = em.find(OrderEntity.class, draft.getId());
        if (order == null || !order.getUserId().equals(draft.getUserId())) {
            return null;
        }

        order.setSubtotalAmount(nz(draft.getSubtotalAmount()));
        order.setDiscountAmount(nz(draft.getDiscountAmount()));
        order.setCouponDiscountAmount(nz(draft.getCouponDiscountAmount()));
        order.setShippingAmount(nz(draft.getShippingAmount()));
        order.setRentalAmount(nz(draft.getRentalAmount()));
        order.setTotalAmount(nz(draft.getTotalAmount()));
        order.setPointsEarned(nz(draft.getPointsEarned()));
        order.setPointsSpent(nz(draft.getPointsSpent()));

        final Long current = order.getAppliedCouponIssuanceId();
        final Long next = draft.getAppliedCouponIssuanceId();

        if (current != null && (!current.equals(next))) {
            order.setAppliedCouponIssuanceId(null);
            em.flush();
        }

        if (next != null && !next.equals(current)) {
            order.setAppliedCouponIssuanceId(next);
        }

        em.flush();
        return findDraftById(order.getUserId(), order.getId()).orElseThrow();
    }

    @Override
    public Optional<OrderAggregate> findByIdAndUserIdForUpdate(Long userId, Long orderId) {
        final var entity = qf.selectFrom(qOrder)
                .where(qOrder.id.eq(orderId).and(qOrder.userId.eq(userId)))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE).fetchOne();

        if (entity == null) {
            return Optional.empty();
        }

        final var items = loadItems(entity.getId());
        return Optional.of(entity.toDomain(items));
    }

    @Override
    @Transactional
    public Optional<OrderAggregate> findById(Long orderId) {
        final var entity = em.find(OrderEntity.class, orderId);
        if (entity == null) { return Optional.empty(); }
        final var items = loadItems(orderId);
        return Optional.of(entity.toDomain(items));
    }

    @Override
    @Transactional
    public Optional<OrderAggregate> findByIdForUpdate(Long orderId) {
        final var entity = qf.selectFrom(qOrder)
                .where(qOrder.id.eq(orderId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne();
        if (entity == null) { return Optional.empty(); }
        final var items = loadItems(entity.getId());
        return Optional.of(entity.toDomain(items));
    }

    @Override
    @Transactional
    public OrderAggregate saveAggregate(OrderAggregate aggregate) {
        var entity = OrderEntity.fromModel(aggregate);
        if (entity.getId() == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        if (aggregate.getItems() != null) {
            replaceItems(entity.getId(), aggregate.getItems());
        }

        final var items = loadItems(entity.getId());
        return entity.toDomain(items);
    }

    private static int nz(final Integer v) {
        return v == null ? 0 : v;
    }

    private List<OrderItem> loadItems(final Long orderId) {
        final var rows = qf.selectFrom(qOrderItem)
                .where(qOrderItem.orderId.eq(orderId))
                .orderBy(qOrderItem.id.asc())
                .fetch();
        final var items = new ArrayList<OrderItem>(rows.size());
        for (final var e : rows) {
            items.add(e.toModel());
        }
        return items;
    }

    private void replaceItems(final Long orderId,
                              final List<OrderItem> items) {

        final var existingRows = qf.selectFrom(qOrderItem)
                .where(qOrderItem.orderId.eq(orderId))
                .orderBy(qOrderItem.id.asc())
                .fetch();

        record Key(String refType, Long refId, Integer rentalDays) {}
        final var existingMap = new HashMap<Key, OrderItemEntity>(existingRows.size());
        for (final var e : existingRows) {
            existingMap.put(new Key(
                    e.getRefType().name(),
                    e.getRefId(),
                    e.getRentalDays()
            ), e);
        }

        final var seen = new HashSet<>();
        if (items != null) {
            for (final var it : items) {
                Key k = new Key(
                        it.getRefType().name(),
                        it.getRefId(),
                        it.getRentalDays()
                );
                seen.add(k);

                OrderItemEntity target = existingMap.get(k);
                if (target != null) {
                    target.setTitleSnapshot(it.getTitleSnapshot());
                    target.setThumbnailUrl(it.getThumbnailUrl());
                    target.setListPriceSnapshot(nz(it.getListPriceSnapshot()));
                    target.setSalePriceSnapshot(nz(it.getSalePriceSnapshot()));
                    target.setRentalPriceSnapshot(nz(it.getRentalPriceSnapshot()));
                    target.setQuantity(it.getQuantity() == null ? 1 : it.getQuantity());
                    target.setPointsRate(nz(it.getPointsRate()));
                    target.setPointsEarnedItem(nz(it.getPointsEarnedItem()));
                    target.setLineSubtotalAmount(nz(it.getLineSubtotalAmount()));
                    target.setLineDiscountAmount(nz(it.getLineDiscountAmount()));
                    target.setLineTotalAmount(nz(it.getLineTotalAmount()));
                } else {
                    final var fresh = OrderItemEntity.fromModel(
                            it.getOrderId() != null && it.getOrderId().equals(orderId) ? it : it.withOrderId(orderId)
                    );
                    fresh.setId(null);
                    fresh.setOrderId(orderId);
                    em.persist(fresh);
                }
            }
        }

        for (var entry : existingMap.entrySet()) {
            if (!seen.contains(entry.getKey())) {
                em.remove(entry.getValue());
            }
        }

        em.flush();
    }
}
