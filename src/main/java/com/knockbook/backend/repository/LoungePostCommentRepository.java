package com.knockbook.backend.repository;

import com.knockbook.backend.domain.LoungePostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LoungePostCommentRepository {

    // Create
    LoungePostComment save(LoungePostComment domain);

    // Read
    Optional<LoungePostComment> findByIdAndNotDeleted(Long id);
    Page<LoungePostComment> findAllByPostIdAndNotDeleted(Long postId, Pageable pageable);

    // Update
    LoungePostComment updateContentById(Long id, Long userId, String content);

    // Soft Delete
    void softDeleteById(Long id, Long userId);

    // Existence / permission check
    boolean existsByIdAndUserIdAndNotDeleted(Long id, Long userId);
}
