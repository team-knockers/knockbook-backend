package com.knockbook.backend.repository;

public interface LoungePostLikeRepository {

    boolean savePostLike(Long userId, Long postId);

    boolean deletePostLikeIfExists(Long userId, Long postId);

    void incrementLikeCount(Long reviewId);

    void decrementLikeCount(Long reviewId);
}
