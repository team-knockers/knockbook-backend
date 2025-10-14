package com.knockbook.backend.repository;

import com.knockbook.backend.domain.CartItem;
import com.knockbook.backend.domain.OrderAggregate;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    OrderAggregate saveDraftFromCart(OrderAggregate aggregate, List<CartItem> items);
    Optional<OrderAggregate> findDraftById(Long userId, Long orderId);
    OrderAggregate updateDraftAmountsAndCoupon(OrderAggregate draft);
}
