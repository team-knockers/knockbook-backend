package com.knockbook.backend.repository;

import com.knockbook.backend.domain.LoungePostComment;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LoungePostCommentRepository {

    // Create
    LoungePostComment save(LoungePostComment domain);

    // Read
    Optional<LoungePostComment> findByIdAndNotDeleted(Long id);
    List<LoungePostComment> findAllByPostIdAndNotDeleted(Long postId, Pageable pageable);

    // Update
    LoungePostComment updateContentById(Long id, Long userId, String content);

    // Soft Delete
    LoungePostComment softDeleteById(Long id, Long userId);

    // Existence / permission check
    boolean existsByIdAndUserIdAndNotDeleted(Long id, Long userId);
}
