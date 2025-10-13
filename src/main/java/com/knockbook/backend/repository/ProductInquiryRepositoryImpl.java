package com.knockbook.backend.repository;

import com.knockbook.backend.domain.ProductInquiry;
import com.knockbook.backend.entity.ProductInquiryEntity;
import com.knockbook.backend.entity.QProductInquiryEntity;
import com.knockbook.backend.entity.QUserEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductInquiryRepositoryImpl implements ProductInquiryRepository {
    private final JPAQueryFactory query;
    private final EntityManager em;
    // QueryDSL Q-types (entity metamodels)
    private static final QProductInquiryEntity PI = QProductInquiryEntity.productInquiryEntity;
    private static final QUserEntity U = QUserEntity.userEntity;

    @Override
    public Page<ProductInquiry> findProductInquiries(
            Long productId,
            Pageable pageable
    ) {
        final var predicate = new BooleanBuilder()
                .and(PI.productId.eq(productId))
                .and(PI.deletedAt.isNull());

        final var rows = query
                .select(
                        PI.inquiryId,
                        U.displayName,
                        PI.title,
                        PI.questionBody,
                        PI.createdAt,
                        PI.answerBody,
                        PI.answeredAt
                )
                .from(PI)
                .join(U).on(PI.userId.eq(U.id))
                .where(predicate)
                .orderBy(PI.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final var content = rows.stream()
                .map(t -> ProductInquiry.builder()
                        .inquiryId(t.get(PI.inquiryId))
                        .displayName(t.get(U.displayName))
                        .title(t.get(PI.title))
                        .questionBody(t.get(PI.questionBody))
                        .createdAt(t.get(PI.createdAt))
                        .answerBody(t.get(PI.answerBody))
                        .answeredAt(t.get(PI.answeredAt))
                        .build())
                .toList();

        final var total = query
                .select(PI.count())
                .from(PI)
                .where(predicate)
                .fetchOne();
        final var totalElements = (total == null) ? 0L : total;

        return new PageImpl<>(content, pageable, totalElements);
    }

    @Override
    public Long createInquiry(Long productId, Long userId, String title, String questionBody) {
        final var inquiry = ProductInquiryEntity.builder()
                .productId(productId)
                .userId(userId)
                .title(title)
                .questionBody(questionBody)
                .answerBody(null)
                .isPublic(true)
                .answeredAt(null)
                .deletedAt(null)
                .build();

        em.persist(inquiry);  // insert
        return inquiry.getInquiryId();
    }
}
