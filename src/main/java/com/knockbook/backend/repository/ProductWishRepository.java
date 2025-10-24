package com.knockbook.backend.repository;

import com.knockbook.backend.domain.ProductWishlist;

public interface ProductWishRepository {
    boolean insertWishlist (
            Long productId,
            Long userId
    );
    boolean deleteWishlist (
            Long productId,
            Long userId
    );
    ProductWishlist findWishlist (
            Long userId
    );
    boolean isWishedOrNot (
            Long userId,
            Long productId
    );
}
