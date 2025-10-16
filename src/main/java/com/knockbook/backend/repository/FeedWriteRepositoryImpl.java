package com.knockbook.backend.repository;

import com.knockbook.backend.entity.FeedCommentEntity;
import com.knockbook.backend.entity.QFeedCommentEntity;
import com.knockbook.backend.entity.QFeedPostEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedWriteRepositoryImpl implements FeedWriteRepository{

    private final JPAQueryFactory query;
    private final EntityManager em;

    private static final QFeedPostEntity P = QFeedPostEntity.feedPostEntity;
    private static final QFeedCommentEntity C = QFeedCommentEntity.feedCommentEntity;

    @Override
    public Long insertComment (
            Long postId,
            Long userId,
            String commentBody
    ) {
        final var comment = FeedCommentEntity.builder()
                .postId(postId)
                .userId(userId)
                .body(commentBody)
                .build();

        em.persist(comment);
        em.flush();

        return comment.getCommentId();
    }

    @Override
    public void incrementPostCommentsCount (
            Long postId
    ) {
        query.update(P)
                .set(P.commentsCount, P.commentsCount.add(1))
                .where(P.postId.eq(postId))
                .execute();
    }
}
