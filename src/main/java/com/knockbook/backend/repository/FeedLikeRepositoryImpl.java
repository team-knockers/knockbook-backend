package com.knockbook.backend.repository;

import com.knockbook.backend.entity.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedLikeRepositoryImpl implements FeedLikeRepository{
    private final JPAQueryFactory query;
    private final EntityManager em;

    private static final QFeedPostLikeEntity PL = QFeedPostLikeEntity.feedPostLikeEntity;
    private static final QFeedPostEntity P = QFeedPostEntity.feedPostEntity;

    private final QFeedCommentLikeEntity CL = QFeedCommentLikeEntity.feedCommentLikeEntity;
    private final QFeedCommentEntity C = QFeedCommentEntity.feedCommentEntity;

    @Override
    public boolean insertPostLikeIfAbsent (
            Long postId,
            Long userId
    ) {
        final var likeRowExists = query.selectOne()
                .from(PL)
                .where(PL.postId.eq(postId).and(PL.userId.eq(userId)))
                .fetchFirst() != null;
        if (likeRowExists) return false;

        final var like = FeedPostLikeEntity.builder()
                .postId(postId)
                .userId(userId)
                .build();
        em.persist(like);

        return true;
    }

    @Override
    public boolean deletePostLikeIfPresent (
            Long postId,
            Long userId
    ) {
        final var affected = query.delete(PL)
                .where(PL.postId.eq(postId).and(PL.userId.eq(userId)))
                .execute();
        return affected == 1;
    }

    @Override
    public void incrementPostLikesCount(Long postId) {
        query.update(P)
                .set(P.likesCount, P.likesCount.add(1))
                .where(P.postId.eq(postId))
                .execute();
    }

    @Override
    public void decrementPostLikesCount(Long postId) {
        query.update(P)
                .set(P.likesCount, P.likesCount.subtract(1))
                .where(P.postId.eq(postId).and(P.likesCount.gt(0)))
                .execute();
    }

    @Override
    public boolean insertCommentLikeIfAbsent (
            Long commentId,
            Long userId
    ) {
        final var likeRowExists = query.selectOne()
                .from(CL)
                .where(CL.commentId.eq(commentId).and(CL.userId.eq(userId)))
                .fetchFirst() != null;
        if (likeRowExists) return false;

        final var like = FeedCommentLikeEntity.builder()
                .commentId(commentId)
                .userId(userId)
                .build();
        em.persist(like);

        return true;
    }

    @Override
    public boolean deleteCommentLikeIfPresent (
            Long commentId,
            Long userId
    ) {
        final var affected = query.delete(CL)
                .where(CL.commentId.eq(commentId).and(CL.userId.eq(userId)))
                .execute();
        return affected == 1;
    }

    @Override
    public void incrementCommentLikesCount(Long commentId) {
        query.update(C)
                .set(C.likesCount, C.likesCount.add(1))
                .where(C.commentId.eq(commentId))
                .execute();
    }

    @Override
    public void decrementCommentLikesCount(Long commentId) {
        query.update(C)
                .set(C.likesCount, C.likesCount.subtract(1))
                .where(C.commentId.eq(commentId).and(C.likesCount.gt(0)))
                .execute();
    }
}
