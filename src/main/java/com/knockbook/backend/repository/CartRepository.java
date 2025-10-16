package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Cart;
import com.knockbook.backend.domain.CartItem;
import com.knockbook.backend.domain.CartRef;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CartRepository {
    Optional<Cart> findOpenByUserId(Long userId);
    Optional<Cart> findById(Long cartId);
    List<CartItem> findSelectableItems(Long userId, List<Long> cartItemIds);
    Cart createEmpty(Long userId);
    Cart addItem(Long cartId, CartItem item);
    Cart deleteItem(Long cartId, Long cartItemId);
    Cart incrementItem(Long cartId, Long cartItemId, int qty);
    Cart decrementItem(Long cartId, Long cartItemId, int qty);
    void deleteByUserIdAndRefs(Long userId, Collection<CartRef> refs);
}
