package com.knockbook.backend.repository;

import com.knockbook.backend.domain.LoungePostComment;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LoungePostCommentRepository {

    // Create
    LoungePostComment save(LoungePostComment domain);

    // Read
    // 답글 id로 단건조회
    Optional<LoungePostComment> findByIdAndNotDeleted(Long id);
    // post Id로 여러건 조회
    List<LoungePostComment> findAllByPostIdAndNotDeleted(Long postId, Pageable pageable);

    // Update
    // 게시글 수정
    LoungePostComment updateContentById(Long id, Long userId, String content);

    // Soft Delete
    // 게시글 삭제 (Soft Delete)
    LoungePostComment softDeleteById(Long id, Long userId);

    // Existence / permission check
    // DB에 존재여부, 삭제 여부 체크 / 요청한 사용자가 권한이 있는지 체크
    boolean existsByIdAndUserIdAndNotDeleted(Long id, Long userId);
}
