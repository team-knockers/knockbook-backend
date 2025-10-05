package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Cart;
import com.knockbook.backend.domain.CartItem;

import java.util.Optional;

public interface CartRepository {
    Optional<Cart> findOpenByUserId(Long userId);
    Optional<Cart> findById(Long cartId);
    Cart createEmpty(Long userId);
    Cart addItem(Long cartId, CartItem item);
    Cart deleteItem(Long cartId, Long cartItemId);
    Cart decrementItem(Long userId, Long cartItemId, int qty);
}
