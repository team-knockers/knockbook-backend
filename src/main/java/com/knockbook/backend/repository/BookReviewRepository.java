package com.knockbook.backend.repository;

import com.knockbook.backend.domain.BookReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface BookReviewRepository {

    /**
     * Returns paginated book reviews with optional filters, including images and likes count
     */
    Page<BookReview> findAllBy(Long bookId, Pageable pageable,
                                     String transactionType, Boolean sameMbti, String currentUserMbti);

    /**
     * Finds review IDs liked by the given user (used for likedByMe check)
     */
    Set<Long> findLikedReviewIdsBy(Long userId, List<Long> reviewIds);
}
