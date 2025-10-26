package com.knockbook.backend.repository;

import com.knockbook.backend.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface ProductRepository {

    Page<Product> findAllPaged(Pageable pageable);

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
