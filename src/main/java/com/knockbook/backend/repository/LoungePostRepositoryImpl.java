package com.knockbook.backend.repository;

import com.knockbook.backend.domain.LoungePost;
import com.knockbook.backend.domain.LoungePostSummary;
import com.knockbook.backend.entity.LoungePostEntity;
import com.knockbook.backend.entity.QLoungePostEntity;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LoungePostRepositoryImpl implements LoungePostRepository{

    private final JPAQueryFactory query;
    private final EntityManager em;

    private final QLoungePostEntity qPost = QLoungePostEntity.loungePostEntity;

    @Override
    public LoungePost save(LoungePost post) {
        final var entity = LoungePostEntity.builder()
                .userId(post.getUserId())
                .title(post.getTitle())
                .subtitle(post.getSubtitle())
                .content(post.getContent())
                .previewImageUrl(post.getPreviewImageUrl())
                .status(LoungePostEntity.Status.VISIBLE)
                .likeCount(0)
                .build();

        em.persist(entity);
        em.flush();
        em.refresh(entity);

        return LoungePost.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .subtitle(entity.getSubtitle())
                .content(entity.getContent())
                .status(LoungePost.Status.VISIBLE)
                .likeCount(0)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Override
    public Optional<LoungePost> findById(Long id) {
        final var postEntity = query
                .selectFrom(qPost)
                .where(qPost.id.eq(id))
                .fetchOne();

        if (postEntity == null) {
            return Optional.empty();
        }

        final var found = LoungePost.builder()
                .id(postEntity.getId())
                .userId(postEntity.getUserId())
                .title(postEntity.getTitle())
                .subtitle(postEntity.getSubtitle())
                .content(postEntity.getContent())
                .status(LoungePost.Status.valueOf(postEntity.getStatus().name()))
                .likeCount(postEntity.getLikeCount())
                .createdAt(postEntity.getCreatedAt())
                .build();

        return Optional.of(found);
    }

    @Override
    public Page<LoungePostSummary> findPostsByPageable(Pageable pageable) {

        final var predicate = qPost.status.eq(LoungePostEntity.Status.VISIBLE);

        final var tuples = query
                .select(
                        qPost.id,
                        qPost.userId,
                        qPost.title,
                        qPost.previewImageUrl,
                        qPost.status,
                        qPost.likeCount,
                        qPost.createdAt
                )
                .from(qPost)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .fetch();

        final List<LoungePostSummary> content = tuples.stream()
                .map(t -> {
                    var s = t.get(qPost.status);
                    return LoungePostSummary.builder()
                            .id(t.get(qPost.id))
                            .userId(t.get(qPost.userId))
                            .title(t.get(qPost.title))
                            .previewImageUrl(t.get(qPost.previewImageUrl))
                            .status(s == null ? LoungePostSummary.Status.HIDDEN
                                    : LoungePostSummary.Status.valueOf(s.name()))
                            .likeCount(t.get(qPost.likeCount))
                            .createdAt(t.get(qPost.createdAt))
                            .build();
                })
                .toList();

        final var total = query
                .select(qPost.count())
                .from(qPost)
                .where(predicate)
                .fetchOne();

        final var totalItems = total == null ? 0L : total;

        return new PageImpl<>(content, pageable, totalItems);
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return new OrderSpecifier<?>[]{ qPost.createdAt.desc() };
        }

        final var key = sort.stream()
                .map(o -> o.getProperty().toLowerCase())
                .findFirst()
                .orElse("newest");

        return switch (key) {
            case "popular" -> new OrderSpecifier<?>[]{ qPost.likeCount.desc() };
            case "newest"  -> new OrderSpecifier<?>[]{ qPost.createdAt.desc() };
            default -> throw new IllegalArgumentException("Invalid sort key: " + key);
        };
    }
}
