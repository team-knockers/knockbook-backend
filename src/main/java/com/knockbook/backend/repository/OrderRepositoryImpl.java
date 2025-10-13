package com.knockbook.backend.repository;

import com.knockbook.backend.domain.CartItem;
import com.knockbook.backend.domain.OrderAggregate;

import java.util.List;
import java.util.Optional;

public class OrderRepositoryImpl implements OrderRepository {

    @Override
    public OrderAggregate saveDraftFromCart(OrderAggregate aggregate, List<CartItem> items) {
        return null;
    }

    @Override
    public Optional<OrderAggregate> findDraftById(Long userId, Long orderId) {
        return Optional.empty();
    }
}
