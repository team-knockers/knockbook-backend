package com.knockbook.backend.service;

import com.knockbook.backend.domain.ProductInquiry;
import com.knockbook.backend.domain.ProductResult;
import com.knockbook.backend.domain.ProductReviewsResult;
import com.knockbook.backend.domain.ProductSummary;
import com.knockbook.backend.exception.ProductNotFoundException;
import com.knockbook.backend.repository.ProductInquiryRepository;
import com.knockbook.backend.repository.ProductRepository;
import com.knockbook.backend.repository.ProductReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ProductInquiryRepository productInquiryRepository;

    public Page<ProductSummary> getProductList(
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

     public ProductResult getProduct(Long productId) {
         return productRepository.findProductById(productId)
                 .orElseThrow(()-> new ProductNotFoundException(productId));
     }

     public ProductReviewsResult getProductReviews (
             Long productId,
             Long userId,
             Pageable pageable
     ){
        return productReviewRepository.findProductReviews(
                productId, userId, pageable
        );
     }

     public Page<ProductInquiry> getProductInquiries(
             Long productId,
             Pageable pageable
     ){
        return productInquiryRepository.findProductInquiries(
                productId, pageable
        );
     }

    @Transactional
    public void likeReview(Long reviewId, Long userId) {
        if (productReviewRepository.addLikeIfAbsent(reviewId, userId)) {
            productReviewRepository.incrementLikesCount(reviewId);
        }
    }
    @Transactional
    public void unlikeReview(Long reviewId, Long userId) {
        if (productReviewRepository.removeLikeIfPresent(reviewId, userId)) {
            productReviewRepository.decrementLikesCount(reviewId);
        }
    }
}
