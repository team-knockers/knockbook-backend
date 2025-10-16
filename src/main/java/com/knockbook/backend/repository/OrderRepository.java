package com.knockbook.backend.repository;

import com.knockbook.backend.domain.CartItem;
import com.knockbook.backend.domain.OrderAggregate;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    OrderAggregate saveDraftFromCart(final OrderAggregate aggregate,
                                     final List<CartItem> items);
    Optional<OrderAggregate> findDraftById(final Long userId,
                                           final Long orderId);
    Optional<OrderAggregate> findPendingDraftByUser(final Long userId);
    OrderAggregate replaceDraftFromCart(final OrderAggregate existing,
                                        final List<CartItem> items,
                                        final boolean resetDiscounts);
    OrderAggregate updateDraftAmountsAndCoupon(final OrderAggregate draft);

    Optional<OrderAggregate> findByIdAndUserIdForUpdate(final Long userId,
                                                        final Long orderId); // for payment approval
    OrderAggregate saveAggregate(final OrderAggregate aggregate); // save timeline
}
