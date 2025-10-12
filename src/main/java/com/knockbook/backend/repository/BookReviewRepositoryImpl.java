package com.knockbook.backend.repository;

import com.knockbook.backend.domain.BookReview;
import com.knockbook.backend.domain.BookReviewImage;
import com.knockbook.backend.entity.*;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BookReviewRepositoryImpl implements BookReviewRepository  {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    private static final QBookReviewEntity R = QBookReviewEntity.bookReviewEntity;
    private static final QBookReviewImageEntity I = QBookReviewImageEntity.bookReviewImageEntity;
    private static final QBookReviewLikeEntity L = QBookReviewLikeEntity.bookReviewLikeEntity;
    private static final QUserEntity U = QUserEntity.userEntity;

    @Override
    public Page<BookReview> findAllBy(Long bookId, Pageable pageable,
                                            String transactionType, Boolean sameMbti, String currentUserMbti) {
        // 0) Define base predicate (visible + not deleted) and add filter (transaction type, same MBTI)
        BooleanExpression predicate = R.bookId.eq(bookId)
                .and(R.status.eq(BookReviewEntity.Status.VISIBLE))
                .and(R.deletedAt.isNull());

        if (transactionType != null && !"all".equalsIgnoreCase(transactionType)) {
            predicate = predicate.and(R.transactionType.eq(
                    BookReviewEntity.TransactionType.valueOf(transactionType.toUpperCase())
            ));
        }

        final var applySameMbti = Boolean.TRUE.equals(sameMbti)
                && currentUserMbti != null && !currentUserMbti.isBlank();

        if (applySameMbti) {
            predicate = predicate.and(U.mbti.eq(currentUserMbti));
        }

        // 1) projection single-query: fetch review + required user columns in one query
        final var reviewWithUserTuples = queryFactory
                .select(R, U.displayName, U.mbti)
                .from(R)
                .leftJoin(U).on(U.id.eq(R.userId))
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .fetch();

        if (reviewWithUserTuples.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0L);
        }

        // 2) Prepare ids and counts (null-safe)
        final var reviewIds = reviewWithUserTuples.stream()
                .map(t -> t.get(R))
                .filter(Objects::nonNull)
                .map(BookReviewEntity::getId)
                .filter(Objects::nonNull)
                .toList();

        final var totalItems = Optional.ofNullable(
                queryFactory.select(R.count())
                        .from(R)
                        .leftJoin(U).on(U.id.eq(R.userId))
                        .where(predicate)
                        .fetchOne()
        ).orElse(0L);

        // 3) Fetch images once for all reviews on page and group by reviewId
        final var imageEntities = queryFactory
                .selectFrom(I)
                .where(I.bookReviewId.in(reviewIds).and(I.deletedAt.isNull()))
                .orderBy(I.bookReviewId.asc(), I.sortOrder.asc())
                .fetch();

        final var imagesByReviewId = imageEntities.stream()
                .collect(Collectors.groupingBy(BookReviewImageEntity::getBookReviewId, LinkedHashMap::new, Collectors.toList()));

        // 4) Map tuples -> domain BookReview preserving original order
        final var content = reviewWithUserTuples.stream()
                .map(t -> {
                    final var re = t.get(R);
                    if (re == null) {
                        return null;
                    }

                    final var images = Optional.ofNullable(imagesByReviewId.get(re.getId()))
                            .orElse(List.of())
                            .stream()
                            .map(img -> BookReviewImage.builder()
                                    .imageUrl(img.getImageUrl())
                                    .sortOrder(img.getSortOrder())
                                    .build())
                            .toList();

                    return BookReview.builder()
                            .id(re.getId())
                            .bookId(re.getBookId())
                            .userId(re.getUserId())
                            .displayName(t.get(U.displayName))
                            .mbti(t.get(U.mbti))
                            .transactionType(BookReview.TransactionType.valueOf(re.getTransactionType().name()))
                            .content(re.getBody())
                            .rating(re.getRating())
                            .likesCount(re.getLikesCount())
                            .imageUrls(images)
                            .createdAt(re.getCreatedAt())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(content, pageable, totalItems);
    }

    @Override
    public Set<Long> findLikedReviewIdsBy(Long userId, List<Long> reviewIds) {
        if (userId == null || reviewIds == null || reviewIds.isEmpty()) {
            return Collections.emptySet();
        }

        // Find IDs of reviews liked by the given user (for likedByMe check)
        final var likedReviewIds = queryFactory.select(L.bookReviewId)
                .from(L)
                .where(L.bookReviewId.in(reviewIds)
                        .and(L.userId.eq(userId))
                        .and(L.isLiked.eq(true)))
                .fetch();

        return Set.copyOf(likedReviewIds);
    }

    @Override
    @Transactional
    public boolean saveReviewLike(Long userId, Long reviewId) {
        // 1) restore if exists with isLiked = false
        final var updated = queryFactory.update(L)
                .where(L.bookReviewId.eq(reviewId)
                        .and(L.userId.eq(userId))
                        .and(L.isLiked.eq(false)))
                .set(L.isLiked, true)
                .execute();

        if (updated > 0) {
            return true;
        }

        // 2) try insert
        try {
            final var like = BookReviewLikeEntity.builder()
                    .bookReviewId(reviewId)
                    .userId(userId)
                    .isLiked(true)
                    .build();
            em.persist(like);
            em.flush();
            return true;
        } catch (PersistenceException ex) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteReviewLikeIfExists(Long userId, Long reviewId) {
        final var updated = queryFactory.update(L)
                .where(L.bookReviewId.eq(reviewId)
                        .and(L.userId.eq(userId))
                        .and(L.isLiked.eq(true)))
                .set(L.isLiked, false)
                .execute();

        return updated > 0;
    }

    @Override
    @Transactional
    public void incrementLikeCount(Long reviewId) {
        queryFactory.update(R)
                .where(R.id.eq(reviewId))
                .set(R.likesCount, R.likesCount.add(1))
                .execute();
    }

    @Override
    @Transactional
    public void decrementLikeCount(Long reviewId) {
        queryFactory.update(R)
                .where(R.id.eq(reviewId).and(R.likesCount.gt(0)))
                .set(R.likesCount, R.likesCount.subtract(1))
                .execute();
    }

    /**
     * Converts Spring Sort into QueryDSL OrderSpecifiers.
     */
    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        PathBuilder<BookReviewEntity> builder = new PathBuilder<>(BookReviewEntity.class, R.getMetadata());

        return sort.stream()
                .map(order -> {
                    final var prop = mapToEntityField(order.getProperty());
                    final var direction = order.isAscending() ? Order.ASC : Order.DESC;

                    if ("createdAt".equals(prop)) {
                        return new OrderSpecifier<>(direction, builder.getDateTime(prop, Instant.class));
                    } else if ("rating".equals(prop) || "likesCount".equals(prop)) {
                        return new OrderSpecifier<>(direction, builder.getNumber(prop, Integer.class));
                    } else {
                        throw new IllegalArgumentException("Invalid sort property: " + prop);
                    }
                })
                .toArray(OrderSpecifier[]::new);
    }

    /**
     * Maps API sort keys (e.g. "likes") to entity fields (e.g. "likesCount").
     */
    private String mapToEntityField(String sortBy) {
        return switch (sortBy) {
            case "likes"     -> "likesCount";
            case "createdAt" -> "createdAt";
            case "rating"    -> "rating";
            default -> throw new IllegalArgumentException("Invalid sortBy value: " + sortBy);
        };
    }

}
