package com.knockbook.backend.repository;

import com.knockbook.backend.domain.BookReview;
import com.knockbook.backend.domain.BookReviewImage;
import com.knockbook.backend.domain.BookReviewStatistic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface BookReviewRepository {

    /**
     * Save a book review entity.
     */
    BookReview save(BookReview review);

    /**
     * Save a book review image entity and return its corresponding domain object.
     */
    BookReviewImage saveImageAndReturnDomain(Long reviewId, String imageUrl, int sortOrder);

    /**
     * Returns paginated book reviews with optional filters, including images and likes count
     */
    Page<BookReview> findAllBy(Long bookId, Pageable pageable,
                                     String transactionType, Boolean sameMbti, String currentUserMbti);

    /**
     * Finds review IDs liked by the given user (used for likedByMe check)
     */
    Set<Long> findLikedReviewIdsBy(Long userId, List<Long> reviewIds);

    /**
     * Persist a like row for the given user and review.
     * Should throw DataIntegrityViolationException on unique constraint violation (caller may ignore).
     */
    boolean saveReviewLike(Long userId, Long reviewId);

    /**
     * Delete a like row for the given user and review if exists.
     * Returns true if a row was deleted, false otherwise.
     */
    boolean deleteReviewLikeIfExists(Long userId, Long reviewId);

    /**
     * Atomic increment of likes count on the review (DB update).
     */
    void incrementLikeCount(Long reviewId);

    /**
     * Atomic decrement of likes count on the review (DB update, should not go below zero).
     */
    void decrementLikeCount(Long reviewId);

    /**
     * Checks if the given user has liked the specified review.
     */
    boolean existsReviewLike(Long userId, Long reviewId);

    /**
     * Returns the total number of likes for the specified review.
     */
    int getLikeCount(Long reviewId);

    /**
     * Returns book review stats: avg rating, total, score counts, and MBTI counts.
     */
    BookReviewStatistic findBookReviewStatisticsBy(Long bookId);
}
