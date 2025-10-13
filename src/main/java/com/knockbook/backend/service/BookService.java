package com.knockbook.backend.service;

import com.knockbook.backend.domain.*;
import com.knockbook.backend.dto.BookReviewsLikeResponse;
import com.knockbook.backend.exception.BookNotFoundException;
import com.knockbook.backend.exception.CategoryNotFoundException;
import com.knockbook.backend.repository.BookCategoryRepository;
import com.knockbook.backend.repository.BookRepository;
import com.knockbook.backend.repository.BookReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookReviewRepository bookReviewRepository;

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    @Autowired
    private UserService userService;

    public Page<BookSummary> getBooksSummary(
            String categoryCodeName, String subcategoryCodeName, Pageable pageable,
            String searchBy, String searchKeyword, Integer maxPrice, Integer minPrice) {

        return bookRepository.findBooksByCondition(categoryCodeName, subcategoryCodeName, pageable,
                searchBy, searchKeyword, maxPrice, minPrice);
    }

    public Book getBookDetails(Long id) {
        final var rentalPointRate = PointsPolicy.of(CartItem.RefType.BOOK_RENTAL);
        final var purchasePointRate = PointsPolicy.of(CartItem.RefType.BOOK_PURCHASE);
        final var res = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(String.valueOf(id)));

        final var rentalPoint = (int) Math.floor(res.getRentalAmount() * rentalPointRate / 100.0);
        final var purchasePoint = (int) Math.floor(res.getDiscountedPurchaseAmount() * purchasePointRate / 100.0);

        return res.toBuilder()
                .rentalPoint(rentalPoint)
                .purchasePoint(purchasePoint)
                .build();
    }

    public Page<BookReview> getBookReviews(Long bookId, Pageable pageable, String transactionType,
                                           Long currentUserId, Boolean sameMbti) {

        final String currentUserMbti;
        if (Boolean.TRUE.equals(sameMbti) && currentUserId != null) {
            final var currentUser = userService.getUser(currentUserId);
            currentUserMbti = currentUser == null ? null : currentUser.getMbti();
        } else {
            currentUserMbti = null;
        }

        return bookReviewRepository.findAllBy(bookId, pageable, transactionType, sameMbti, currentUserMbti);
    }

    public BookReviewStatistic getBookReviewStatistics(Long bookId) {
        return bookReviewRepository.findBookReviewStatisticsBy(bookId);
    }

    public Set<Long> getLikedReviewIds(Long userId, List<Long> reviewIds) {
        return bookReviewRepository.findLikedReviewIdsBy(userId, reviewIds);
    }

    public BookReviewsLikeResponse likeReview(Long userId, Long reviewId) {
        final var changed = bookReviewRepository.saveReviewLike(userId, reviewId);
        if (changed) {
            bookReviewRepository.incrementLikeCount(reviewId);
        }

        final var liked = bookReviewRepository.existsReviewLike(userId, reviewId);
        final var count = bookReviewRepository.getLikeCount(reviewId);

        return BookReviewsLikeResponse.builder()
                .liked(liked)
                .count(count)
                .build();
    }

    public void unlikeReview(Long userId, Long reviewId) {
        final var deleted = bookReviewRepository.deleteReviewLikeIfExists(userId, reviewId);
        if (deleted) {
            bookReviewRepository.decrementLikeCount(reviewId);
        }
    }

    public List<BookCategory> getAllCategories() {

        return bookCategoryRepository.findAllCategories();
    }

    public List<BookSubcategory> getSubcategoriesByCategoryCodeName(String categoryCodeName) {
        boolean categoryExists = bookCategoryRepository.existsByCategoryCodeName(categoryCodeName);
        if (!categoryExists) {
            throw new CategoryNotFoundException(categoryCodeName);
        }

        return bookCategoryRepository.findSubcategoriesByCategoryCodeName(categoryCodeName);
    }
}
