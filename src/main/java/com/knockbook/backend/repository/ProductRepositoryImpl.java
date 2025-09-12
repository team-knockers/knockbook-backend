package com.knockbook.backend.repository;

import com.knockbook.backend.domain.ProductSummary;
import com.knockbook.backend.entity.ProductEntity;
import com.knockbook.backend.entity.ProductImageEntity;
import com.knockbook.backend.entity.QProductCategoryEntity;
import com.knockbook.backend.entity.QProductEntity;
import com.knockbook.backend.entity.QProductImageEntity;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final JPAQueryFactory query;

    @Override
    public Page<ProductSummary> findProductSummaries(
            String category,        // 문자열 코드 (예: "bookStand")
            String sort,
            String searchKeyword,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable
    ) {
        // Q-클래스: 엔티티의 QueryDSL 메타모델 (풀네임 변수로 가독성 확보)
        QProductEntity product = QProductEntity.productEntity;
        QProductImageEntity productImage = QProductImageEntity.productImageEntity;
        QProductCategoryEntity productCategory = QProductCategoryEntity.productCategoryEntity;

        // ───────────────────────────────── WHERE 조건 (값이 없으면 null 반환 → where에서 자동 무시)
        BooleanExpression activeOnly      = product.status.eq(ProductEntity.Status.ACTIVE);
        BooleanExpression notDeleted      = product.deletedAt.isNull();
        BooleanExpression categoryCond    =
                (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category))
                        ? productCategory.codeName.eq(category.trim()) : null;
        BooleanExpression searchCond      =
                (searchKeyword != null && !searchKeyword.trim().isEmpty())
                        ? product.name.like("%" + searchKeyword.trim() + "%") : null;
        BooleanExpression minPriceCond    = (minPrice != null) ? product.unitPriceAmount.goe(minPrice) : null;
        BooleanExpression maxPriceCond    = (maxPrice != null) ? product.unitPriceAmount.loe(maxPrice) : null;

        // ───────────────────────────────── 정렬
        List<OrderSpecifier<?>> orderSpecifiers = toOrderSpecifiers(sort, product);

        // ───────────────────────────────── 본문(내용) 조회
        List<Tuple> rows = query
                .select(
                        product.productId,
                        product.categoryId,
                        product.sku,
                        product.name,
                        product.unitPriceAmount,
                        product.salePriceAmount,
                        product.stockQty,
                        product.status,
                        product.availability,
                        product.averageRating,
                        product.reviewCount,
                        // 썸네일 1장 (GALLERY & sort_order=1)
                        productImage.imageUrl
                )
                .from(product)
                .leftJoin(productCategory).on(productCategory.categoryId.eq(product.categoryId))  // 코드 비교용
                .leftJoin(productImage).on(
                        productImage.productId.eq(product.productId)
                                .and(productImage.imageUsage.eq(ProductImageEntity.ImageUsage.GALLERY))
                                .and(productImage.sortOrder.eq(1))
                )
                .where(
                        activeOnly,
                        notDeleted,
                        categoryCond,
                        searchCond,
                        minPriceCond,
                        maxPriceCond
                )
                .orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())              // Pageable → offset
                .limit(pageable.getPageSize())             // Pageable → limit
                .fetch();

        // ───────────────────────────────── 총 개수 (커스텀 구현이므로 count 직접 실행)
        Long total = query
                .select(product.count())
                .from(product)
                .leftJoin(productCategory).on(productCategory.categoryId.eq(product.categoryId))
                .where(
                        notDeleted,
                        categoryCond,
                        searchCond,
                        minPriceCond,
                        maxPriceCond
                )
                .fetchOne();
        long totalElements = (total == null) ? 0L : total;

        // ───────────────────────────────── Tuple → 도메인(ProductSummary) 매핑 (stream + builder)
        // 조인해서 만든 한 행(tuple)에서 값을 꺼낼 때 t.get() 에는 원래 선택할 때 썻던 Qentity 출처를 그대로 씀
        List<ProductSummary> content = rows.stream()
                .map(t -> ProductSummary.builder()
                        .id(t.get(product.productId))
                        .categoryId(t.get(product.categoryId))
                        .sku(t.get(product.sku))
                        .name(t.get(product.name))
                        .unitPriceAmount(nz(t.get(product.unitPriceAmount)))
                        .salePriceAmount(t.get(product.salePriceAmount))
                        .stockQty(nz(t.get(product.stockQty)))
                        // 엔티티 enum -> 도메인 enum
                        // enum 은 클래스(타입) 이 달라서 변환작업이 필요
                        // t.get(product.status).name() -> enum 을 문자열로 뽑아냄 ex) "ACTIVE"
                        //ProductSummary.Status.valueOf("ACTIVE") -> 문자열 "ACTIVE" 를 도메인 enum 으로 바꿈
                        .status(
                                t.get(product.status) == null ? null
                                        : ProductSummary.Status.valueOf(t.get(product.status).name())
                        )
                        .availability(
                                t.get(product.availability) == null ? null
                                        : ProductSummary.Availability.valueOf(t.get(product.availability).name())
                        )
                        .averageRating(t.get(product.averageRating))
                        .reviewCount(nz(t.get(product.reviewCount)))
                        .thumbnailUrl(t.get(productImage.imageUrl))
                        .build())
                .toList();

        // PageImpl(content, pageable, totalElements) → 목록 + 페이지정보 + 전체개수
        return new PageImpl<>(content, pageable, totalElements);
    }

//    @Override
//    public Optional<ProductDetail> findProductDetail(Long productId) {
//        // TODO: 상세는 나중에 구현
//        return Optional.empty();
//    }

    /* ─────────────────────────────── 정렬 헬퍼 ───────────────────────────────
       popular: 평점 내림차순 → 리뷰수 내림차순 → 생성일 내림차순
       recent : 생성일 내림차순
       priceAsc/priceDesc : 가격 오름/내림차순
    ------------------------------------------------------------------------ */
    private static List<OrderSpecifier<?>> toOrderSpecifiers(String sort, QProductEntity product) {
        return switch (sort == null ? "" : sort) {
            case "recent"    -> List.of(product.createdAt.desc());
            case "priceAsc"  -> List.of(product.unitPriceAmount.asc());
            case "priceDesc" -> List.of(product.unitPriceAmount.desc());
            case "", "popular" -> List.of(
                    product.averageRating.desc().nullsLast(),
                    product.reviewCount.desc(),
                    product.createdAt.desc()
            );
            default -> List.of(
                    product.averageRating.desc().nullsLast(),
                    product.reviewCount.desc(),
                    product.createdAt.desc()
            );
        };
    }

    /* ─────────────────────────────── 작은 도우미 ─────────────────────────────── */

    private static Integer nz(Integer n) { return (n == null) ? 0 : n; }

}
