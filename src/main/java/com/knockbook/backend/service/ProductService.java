package com.knockbook.backend.service;

import com.knockbook.backend.domain.ProductDetail;
import com.knockbook.backend.domain.ProductSummary;
import com.knockbook.backend.exception.ProductNotFoundException;
import com.knockbook.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductSummary> getProducts(
            String category,
            String searchKeyword,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable
    ) {
        return productRepository.findProductSummaries(
                category, searchKeyword, minPrice, maxPrice, pageable
        );
    }

     public ProductDetail getProductDetail(Long productId) {
         return productRepository.findProductDetail(productId)
                 .orElseThrow(()-> new ProductNotFoundException(productId));
     }
}
