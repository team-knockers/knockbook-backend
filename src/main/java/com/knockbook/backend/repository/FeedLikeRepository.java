package com.knockbook.backend.repository;

public interface FeedLikeRepository {
    // Post
    boolean insertPostLikeIfAbsent (
            Long postId,
            Long userId
    );
    boolean deletePostLikeIfPresent (
            Long postId,
            Long userId
    );
    void incrementPostLikesCount (
            Long postId
    );
    void decrementPostLikesCount (
            Long postId
    );

    //Comment
    boolean insertCommentLikeIfAbsent (
            Long commentId,
            Long userId
    );
    boolean deleteCommentLikeIfPresent (
            Long commentId,
            Long userId
    );
    void incrementCommentLikesCount (
            Long commentId
    );
    void decrementCommentLikesCount (
            Long commentId
    );
}
