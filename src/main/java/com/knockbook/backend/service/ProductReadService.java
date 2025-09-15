package com.knockbook.backend.service;

import com.knockbook.backend.domain.ProductDetail;
import com.knockbook.backend.domain.ProductSummary;
import com.knockbook.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductReadService {

    private final ProductRepository productRepository;

    // 목록 조회
    public Page<ProductSummary> getProducts(
            String category,        // "all" 포함 그대로 전달
            String sort,
            String searchKeyword,
            Integer minPrice,
            Integer maxPrice,
            int page,               // 1-base
            int size
    ) {
        int safePage = Math.max(1, page); //page 가 1보다 작게 들어오면 강제로 1로 올림
        int safeSize = Math.max(1, size); //size 가 1보다 작게 들어오면 강제로 1로 올림
        var pageable = PageRequest.of(safePage - 1, safeSize);

        return productRepository.findProductSummaries(
                category, sort, searchKeyword, minPrice, maxPrice, pageable
        );
    }

    // 단건 상세 조회
     public Optional<ProductDetail> getProductDetail(Long productId) {
         return productRepository.findProductDetail(productId);
     }
}