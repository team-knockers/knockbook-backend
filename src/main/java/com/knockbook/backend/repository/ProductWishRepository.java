package com.knockbook.backend.repository;

public interface ProductWishRepository {
    boolean insertWishlist (
            Long productId,
            Long userId
    );
    boolean deleteWishlist (
            Long productId,
            Long userId
    );
}
