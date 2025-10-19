package com.knockbook.backend.repository;

import com.knockbook.backend.domain.LoungePostComment;
import com.knockbook.backend.entity.LoungePostCommentEntity;
import com.knockbook.backend.entity.QLoungePostCommentEntity;
import com.knockbook.backend.exception.CommentNotFoundException;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LoungePostCommentRepositoryImpl implements LoungePostCommentRepository {

    private final JPAQueryFactory query;
    private final EntityManager em;

    private static final QLoungePostCommentEntity c = QLoungePostCommentEntity.loungePostCommentEntity;

    @Override
    public LoungePostComment save(LoungePostComment domain) {
        LoungePostCommentEntity entity = domainToEntity(domain);
        if (domain.getId() == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        em.flush();
        em.refresh(entity);
        return entityToDomain(entity);
    }

    @Override
    public Optional<LoungePostComment> findByIdAndNotDeleted(Long id) {
        final var result = query
                .selectFrom(c)
                .where(c.id.eq(id)
                        .and(c.deletedAt.isNull()))
                .fetchOne();
        return Optional.ofNullable(entityToDomain(result));
    }

    @Override
    public Page<LoungePostComment> findAllByPostIdAndNotDeleted(Long postId, Pageable pageable) {
        if (pageable == null) pageable = Pageable.unpaged();

        final var q = query.selectFrom(c)
                .where(c.postId.eq(postId)
                        .and(c.deletedAt.isNull()))
                .orderBy(c.createdAt.asc(), c.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());


        final var content = q.fetch().stream()
                .map(this::entityToDomain)
                .toList();

        final Long totalCount = query
                .select(c.count())
                .from(c)
                .where(c.postId.eq(postId).and(c.deletedAt.isNull()))
                .fetchOne();
        final long totalItems = totalCount != null ? totalCount : 0L;

        return new PageImpl<>(content, pageable, totalItems);
    }

    @Override
    public LoungePostComment updateContentById(Long id, Long userId, String content) {
        final var comment = query.selectFrom(c)
                .where(c.id.eq(id)
                        .and(c.deletedAt.isNull()))
                .fetchOne();

        if (comment == null) {
            throw new CommentNotFoundException("댓글이 존재하지 않습니다");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        final var detached = comment.toBuilder()
                .content(content)
                .build();

        final var managed = em.merge(detached);

        em.flush();
        em.refresh(managed);

        return entityToDomain(managed);
    }

    @Override
    public LoungePostComment softDeleteById(Long id, Long userId) {
        final var comment = query.selectFrom(c)
                .where(c.id.eq(id).and(c.deletedAt.isNull()))
                .fetchOne();

        if (comment == null) {
            throw new CommentNotFoundException("댓글이 존재하지 않습니다");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        final var detached = comment.toBuilder()
                .deletedAt(Instant.now())
                .build();

        final var managed = em.merge(detached);

        em.flush();
        em.refresh(managed);

        return entityToDomain(managed);
    }

    @Override
    public boolean existsByIdAndUserIdAndNotDeleted(Long id, Long userId) {
        final var count = query
                .select(c.count())
                .from(c)
                .where(
                        c.id.eq(id),
                        c.userId.eq(userId),
                        c.deletedAt.isNull()
                )
                .fetchOne();

        return count != null && count > 0;
    }

    private LoungePostComment entityToDomain(LoungePostCommentEntity e) {
        if (e == null) { return null; }
        return LoungePostComment.builder()
                .id(e.getId())
                .postId(e.getPostId())
                .userId(e.getUserId())
                .content(e.getContent())
                .status(LoungePostComment.Status.valueOf(e.getStatus().name()))
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private LoungePostCommentEntity domainToEntity(LoungePostComment d) {
        if (d == null) { return null; }

        var builder = LoungePostCommentEntity.builder()
                .postId(d.getPostId())
                .userId(d.getUserId())
                .content(d.getContent())
                .status(d.getStatus() == null
                        ? LoungePostCommentEntity.Status.VISIBLE
                        : LoungePostCommentEntity.Status.valueOf(d.getStatus().name()));

        if (d.getId() != null) {
            builder.id(d.getId());
        }

        return builder.build();
    }
}
