package com.knockbook.backend.repository;

import com.knockbook.backend.entity.FeedPostSaveEntity;
import com.knockbook.backend.entity.QFeedPostSaveEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedSaveRepositoryImpl implements FeedSaveRepository {
    private final JPAQueryFactory query;
    private final EntityManager em;

    private static final QFeedPostSaveEntity PS = QFeedPostSaveEntity.feedPostSaveEntity;

    @Override
    public boolean insertPostSaveIfAbsent (
            Long postId,
            Long userId
    ) {
        final var SaveRowExists = query.selectOne()
                .from(PS)
                .where(PS.postId.eq(postId).and(PS.userId.eq(userId)))
                .fetchFirst() != null;
        if (SaveRowExists) { return false; }

        final var save = FeedPostSaveEntity.builder()
                .postId(postId)
                .userId(userId)
                .build();

        em.persist(save);

        return true;
        }

    @Override
    public boolean deletePostSaveIfPresent (
            Long postId,
            Long userId
    ) {
        final var affected = query.delete(PS)
                .where(PS.postId.eq(postId).and(PS.userId.eq(userId)))
                .execute();

        return affected == 1;
    }
}
