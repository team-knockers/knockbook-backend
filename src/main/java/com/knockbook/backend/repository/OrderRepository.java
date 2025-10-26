package com.knockbook.backend.repository;

import com.knockbook.backend.domain.CartItem;
import com.knockbook.backend.domain.OrderAggregate;
import com.knockbook.backend.domain.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    OrderAggregate saveDraftFromCart(final OrderAggregate aggregate,
                                     final List<CartItem> items);

    OrderAggregate saveDraftWithItems(final OrderAggregate aggregate,
                                      final List<OrderItem> items);

    OrderAggregate replaceDraftWithItems(final OrderAggregate existing,
                                         final List<OrderItem> items,
                                         final boolean resetDiscounts);

    OrderAggregate saveAggregate(final OrderAggregate aggregate); // save timeline

    Optional<OrderAggregate> findDraftById(final Long userId,
                                           final Long orderId);

    Optional<OrderAggregate> findPendingDraftByUser(final Long userId);

    List<OrderAggregate> findOrdersByUser(final Long userId,
                                          final OrderAggregate.PaymentStatus status);

    // for payment approval
    Optional<OrderAggregate> findByIdAndUserIdForUpdate(final Long userId,
                                                        final Long orderId);

    List<OrderAggregate> findAllOrders(final OrderAggregate.PaymentStatus status);

    Optional<OrderAggregate> findById(final Long orderId);

    Optional<OrderAggregate> findByIdForUpdate(final Long orderId);

    OrderAggregate replaceDraftFromCart(final OrderAggregate existing,
                                        final List<CartItem> items,
                                        final boolean resetDiscounts);

    OrderAggregate updateDraftAmountsAndCoupon(final OrderAggregate draft);

    OrderAggregate updateStatusesOnly(final Long userId, Long orderId,
                                      final OrderAggregate.Status statusOrNull,
                                      final OrderAggregate.RentalStatus rentalStatusOrNull);

    List<OrderItem> findItemsByOrderId(final Long orderId);
}
