package com.knockbook.backend.service;

import com.knockbook.backend.component.ImgbbUploader;
import com.knockbook.backend.domain.*;
import com.knockbook.backend.dto.BookReviewsLikeResponse;
import com.knockbook.backend.exception.BookNotFoundException;
import com.knockbook.backend.exception.CategoryNotFoundException;
import com.knockbook.backend.exception.RandomReviewNotFoundException;
import com.knockbook.backend.repository.BookCategoryRepository;
import com.knockbook.backend.repository.BookRepository;
import com.knockbook.backend.repository.BookReviewRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Log4j2
@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookReviewRepository bookReviewRepository;

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    @Autowired
    private ImgbbUploader imgbbUploader;

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

    public List<BookReview> getAllReviewsByUser(Long userId) {
        return bookReviewRepository.findAllBy(userId);
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

    @Transactional
    public BookWishlistAction addToWishlist(Long userId, Long bookId) {
        final var changed = bookRepository.activateBookWishlist(userId, bookId);

        return changed ? BookWishlistAction.ADDED : BookWishlistAction.ALREADY_EXISTS;
    }

    @Transactional
    public BookWishlistAction removeFromWishlist(Long userId, Long bookId) {
        final var changed = bookRepository.deactivateBookWishlist(userId, bookId);

        return changed ? BookWishlistAction.REMOVED : BookWishlistAction.NOT_FOUND;
    }

    public boolean hasBookInWishlist(Long userId, Long bookId) {
        return bookRepository.existsBookWishlist(userId, bookId);
    }

    public List<BookSummary> getUserWishlist(Long userId) {
        return bookRepository.findAllWishlistedBookIdsByUserId(userId);
    }

    @Transactional
    public BookReview createReview(BookReview review, List<MultipartFile> images) {

        final var savedReview = bookReviewRepository.save(review);
        final var userInfo = userService.getUser(review.getUserId());

        // try image upload
        final var uploadedImages = uploadImages(savedReview.getId(), images);

        // toBulider
        return savedReview.toBuilder()
                .imageUrls(uploadedImages)
                .displayName(userInfo.getDisplayName())
                .mbti(userInfo.getMbti())
                .build();
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        bookReviewRepository.softDeleteById(reviewId, userId);
    }

    public RandomBookReview getRandomReview(Integer rating) {

        final var review = bookReviewRepository.findRandomReviewByRating(rating)
                .orElseThrow(() -> new RandomReviewNotFoundException(rating));

        final var userInfo = userService.getUser(review.getUserId());
        final var bookInfo = bookRepository.findById(review.getBookId())
                .orElseThrow(() -> new BookNotFoundException(review.getBookId().toString()));

        return review.toBuilder()
                .displayName(userInfo.getDisplayName())
                .coverThumbnailUrl(bookInfo.getCoverThumbnailUrl())
                .build();
    }

    private List<BookReviewImage> uploadImages(Long reviewId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return Collections.emptyList();

        return IntStream.range(0, images.size())
                .mapToObj(i -> {
                    try {
                        String imageUrl = imgbbUploader.upload(images.get(i));

                        return bookReviewRepository.saveImageAndReturnDomain(reviewId, imageUrl, i + 1);
                    } catch (Exception e) {
                        log.warn("Image upload failed: index={}, reviewId={}", i, reviewId, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
