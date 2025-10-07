package com.knockbook.backend.repository;

import com.knockbook.backend.domain.ProductReviewsResult;
import org.springframework.data.domain.Pageable;

public interface ProductReviewRepository {
    ProductReviewsResult findProductReviews (
            Long productId,
            Long userId,
            Pageable pageable
    );
}
