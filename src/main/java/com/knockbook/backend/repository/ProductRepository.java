package com.knockbook.backend.repository;

import com.knockbook.backend.domain.ProductCreateSpec;
import com.knockbook.backend.domain.ProductResult;
import com.knockbook.backend.domain.ProductSummary;
import com.knockbook.backend.domain.ProductUpdateSpec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface ProductRepository {
    Page<ProductSummary> findProductSummaries(
            String category,
            String searchKeyword,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable,
            Long userId
    );

    Optional<ProductResult> findProductById(
            Long productId
    );

    ProductResult createProduct(ProductCreateSpec spec);

    ProductResult updateProduct(Long productId, ProductUpdateSpec spec);
}
