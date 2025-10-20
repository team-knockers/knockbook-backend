package com.knockbook.backend.repository;

public interface FeedSaveRepository {
    boolean insertPostSaveIfAbsent (
            Long postId,
            Long userId
    );
    boolean deletePostSaveIfPresent (
            Long postId,
            Long userId
    );
}
