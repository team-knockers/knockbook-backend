package com.knockbook.backend.repository;

import com.knockbook.backend.domain.ProductDetail;
import com.knockbook.backend.domain.ProductSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepository {
    Page<ProductSummary> findProductSummaries(
            String category,
            String sort,
            String searchKeyword,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable
    );

    Optional<ProductDetail> findProductDetail(Long productId);
}
