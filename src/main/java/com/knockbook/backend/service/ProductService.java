package com.knockbook.backend.service;

import com.knockbook.backend.domain.*;
import com.knockbook.backend.dto.CreateProductInquiryRequest;
import com.knockbook.backend.dto.CreateProductReviewRequest;
import com.knockbook.backend.exception.ProductNotFoundException;
import com.knockbook.backend.repository.ProductInquiryRepository;
import com.knockbook.backend.repository.ProductRepository;
import com.knockbook.backend.repository.ProductReviewRepository;
import com.knockbook.backend.repository.ProductWishRepository;
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
    private final ProductWishRepository productWishRepository;

    public Page<ProductSummary> getProductList(
            String category,
            String searchKeyword,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable,
            Long userId
    ) {
        return productRepository.findProductSummaries(
                category, searchKeyword, minPrice, maxPrice, pageable, userId
        );
    }

     public ProductResult getProduct(
             Long productId,
             Long userId
     ) {
         final var result = productRepository.findProductById(productId)
                 .orElseThrow(()-> new ProductNotFoundException(productId));
         final var isWished = productWishRepository.isWishedOrNot(userId, productId);
         result.getProductSummary().setWishedByMe(isWished);
         return result;

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

     @Transactional
     public ProductReview createReview (
             Long productId,
             Long userId,
             CreateProductReviewRequest req
     ) {
        return productReviewRepository.insertReview(
                productId, userId, req.getBody(), req.getRating()
        );
     }

     @Transactional
     public void deleteReview (
             Long productId,
             Long reviewId,
             Long userId
     ) {
         final var ok = productReviewRepository.deleteReview(productId, reviewId, userId);
         if (!ok) { throw new IllegalStateException("Not owner or already deleted"); }
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

    @Transactional
    public Long createInquiry(Long productId, Long userId, CreateProductInquiryRequest req) {
        final var title = req.getTitle().trim();
        final var questionBody = req.getQuestionBody().trim();

        return productInquiryRepository.createInquiry(productId, userId, title, questionBody);
    }

    @Transactional
    public void addToWishlist(
            Long productId,
            Long userId
    ) {
        productWishRepository.insertWishlist(productId, userId);
    }

    @Transactional
    public void removeFromWishlist(
            Long productId,
            Long userId
    ) {
        productWishRepository.deleteWishlist(productId, userId);
    }

    public ProductWishlist getProductWishlist(
            Long userId
    ) {
        return productWishRepository.findWishlist(userId);
    }

    @Transactional
    public ProductResult createProduct(ProductCreateSpec spec) {
        return productRepository.createProduct(spec);
    }

    @Transactional
    public ProductResult updateProduct(Long productId, ProductUpdateSpec spec) {
        return productRepository.updateProduct(productId, spec);
    }
}
