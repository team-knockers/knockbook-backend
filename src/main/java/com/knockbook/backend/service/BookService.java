package com.knockbook.backend.service;

import com.knockbook.backend.domain.*;
import com.knockbook.backend.exception.BookNotFoundException;
import com.knockbook.backend.exception.CategoryNotFoundException;
import com.knockbook.backend.repository.BookCategoryRepository;
import com.knockbook.backend.repository.BookRepository;
import com.knockbook.backend.repository.BookReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(String.valueOf(id)));
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

    public Set<Long> getLikedReviewIds(Long userId, List<Long> reviewIds) {
        return bookReviewRepository.findLikedReviewIdsBy(userId, reviewIds);
    }

    public void likeReview(Long userId, Long reviewId) {
        final var changed = bookReviewRepository.saveReviewLike(userId, reviewId);
        if (changed) {
            bookReviewRepository.incrementLikeCount(reviewId);
        }
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
