package com.knockbook.backend.repository;

import com.knockbook.backend.domain.LoungePost;
import com.knockbook.backend.domain.LoungePostSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LoungePostRepository {

    /**
     * Save a lounge post entity.
     */
    LoungePost save(LoungePost post);

    /**
     * Find a post by its ID.
     */
    Optional<LoungePost> findById(final Long id);

    /**
     * Find posts with pagination.
     */
    Page<LoungePostSummary> findPostsByPageable(final Pageable pageable);

    /**
     * Hard delete a lounge post entity.
     */
    void deleteByIdAndUserId(Long postId, Long userId);
}
