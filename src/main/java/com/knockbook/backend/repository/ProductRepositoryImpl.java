package com.knockbook.backend.repository;

import com.knockbook.backend.domain.ProductDetail;
import com.knockbook.backend.domain.ProductSummary;
import com.knockbook.backend.entity.ProductEntity;
import com.knockbook.backend.entity.ProductImageEntity;
import com.knockbook.backend.entity.QProductCategoryEntity;
import com.knockbook.backend.entity.QProductEntity;
import com.knockbook.backend.entity.QProductImageEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final JPAQueryFactory query;
    // Q-클래스: 엔티티의 QueryDSL 메타모델
    QProductEntity P = QProductEntity.productEntity;
    QProductImageEntity PI = QProductImageEntity.productImageEntity;
    QProductCategoryEntity PC = QProductCategoryEntity.productCategoryEntity;

    @Override
    public Page<ProductSummary> findProductSummaries(
            String category,
            String sort,
            String searchKeyword,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable
    ) {
        // 정렬
        List<OrderSpecifier<?>> orderSpecifiers = toOrderSpecifiers(sort, P);

        // ───────────────────────────────── WHERE 조건 (값이 없으면 null 반환 → where에서 자동 무시)
        BooleanBuilder predicate = new BooleanBuilder()
                .and(P.status.eq(ProductEntity.Status.ACTIVE))
                .and(P.deletedAt.isNull());

        if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category)) {
            predicate.and(PC.codeName.eq(category.trim()));
        }
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            predicate.and(P.name.like("%" + searchKeyword.trim() + "%"));
        }
        if (minPrice != null) {
            predicate.and(P.unitPriceAmount.goe(minPrice));
        }
        if (maxPrice != null) {
            predicate.and(P.unitPriceAmount.loe(maxPrice));
        }


        // ───────────────────────────────── 본문(내용) 조회
        List<Tuple> rows = query
                .select(
                        P.productId,
                        P.categoryId,
                        P.sku,
                        P.name,
                        P.unitPriceAmount,
                        P.salePriceAmount,
                        P.stockQty,
                        P.status,
                        P.availability,
                        P.averageRating,
                        P.reviewCount,
                        // 썸네일 1장 (GALLERY & sort_order=1)
                        PI.imageUrl
                )
                .from(P)
                .leftJoin(PC).on(PC.categoryId.eq(P.categoryId))  // 코드 비교용
                .leftJoin(PI).on(
                        PI.productId.eq(P.productId)
                                .and(PI.imageUsage.eq(ProductImageEntity.ImageUsage.GALLERY))
                                .and(PI.sortOrder.eq(1))
                )
                .where(predicate)
                .orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())              // Pageable → offset
                .limit(pageable.getPageSize())             // Pageable → limit
                .fetch();

        // ───────────────────────────────── 총 개수 (커스텀 구현이므로 count 직접 실행)
        Long total = query
                .select(P.count())
                .from(P)
                .leftJoin(PC).on(PC.categoryId.eq(P.categoryId))
                .where(predicate)
                .fetchOne();
        long totalElements = (total == null) ? 0L : total;

        // ───────────────────────────────── Tuple → 도메인(ProductSummary) 매핑 (stream + builder)
        // 조인해서 만든 한 행(tuple)에서 값을 꺼낼 때 t.get() 에는 원래 선택할 때 썻던 Qentity 출처를 그대로 씀
        List<ProductSummary> content = rows.stream()
                .map(t -> ProductSummary.builder()
                        .id(t.get(P.productId))
                        .categoryId(t.get(P.categoryId))
                        .sku(t.get(P.sku))
                        .name(t.get(P.name))
                        .unitPriceAmount(nz(t.get(P.unitPriceAmount)))
                        .salePriceAmount(t.get(P.salePriceAmount))
                        .stockQty(nz(t.get(P.stockQty)))
                        // 엔티티 enum -> 도메인 enum
                        // enum 은 클래스(타입) 이 달라서 변환작업이 필요
                        // t.get(product.status).name() -> enum 을 문자열로 뽑아냄 ex) "ACTIVE"
                        //ProductSummary.Status.valueOf("ACTIVE") -> 문자열 "ACTIVE" 를 도메인 enum 으로 바꿈
                        .status(
                                t.get(P.status) == null ? null
                                        : ProductSummary.Status.valueOf(t.get(P.status).name())
                        )
                        .availability(
                                t.get(P.availability) == null ? null
                                        : ProductSummary.Availability.valueOf(t.get(P.availability).name())
                        )
                        .averageRating(t.get(P.averageRating))
                        .reviewCount(nz(t.get(P.reviewCount)))
                        .thumbnailUrl(t.get(PI.imageUrl))
                        .build())
                .toList();

        // PageImpl(content, pageable, totalElements) → 목록 + 페이지정보 + 전체개수
        return new PageImpl<>(content, pageable, totalElements);
    }

    @Override
    public Optional<ProductDetail> findProductDetail(Long productId) {
        // 상세 조회 1건
        Tuple t = query
                .select(
                        P.productId,
                        P.categoryId,
                        P.sku,
                        P.name,
                        P.unitPriceAmount,
                        P.salePriceAmount,
                        P.manufacturerName,
                        P.isImported,
                        P.importCountry,
                        P.stockQty,
                        P.status,
                        P.availability,
                        P.averageRating,
                        P.reviewCount
                ).from(P)
                .where(
                        P.productId.eq(productId),
                        P.status.eq(ProductEntity.Status.ACTIVE),
                        P.deletedAt.isNull()
                )
                .fetchOne();

        if (t == null) return Optional.empty();

        // GALLERY 이미지 : 최대 4장
        List<String> galleryUrls = query
                .select(PI.imageUrl)
                .from(PI)
                .where(
                        PI.productId.eq(productId)
                                .and(PI.imageUsage.eq(ProductImageEntity.ImageUsage.GALLERY))
                ).orderBy(PI.sortOrder.asc())
                .limit(4)
                .fetch();

        // DESCRIPTION 이미지 : 전체
        List<String> descriptionUrls = query
                .select(PI.imageUrl)
                .from(PI)
                .where(
                        PI.productId.eq(productId)
                                .and(PI.imageUsage.eq(ProductImageEntity.ImageUsage.DESCRIPTION))
                )
                .orderBy(PI.sortOrder.asc())
                .fetch();

        // 매핑
        ProductDetail detail = ProductDetail.builder()
                .id(t.get(P.productId))
                .categoryId(t.get(P.categoryId))
                .sku(t.get(P.sku))
                .name(t.get(P.name))
                .unitPriceAmount(nz(t.get(P.unitPriceAmount)))
                .salePriceAmount(t.get(P.salePriceAmount))
                .manufacturerName(t.get(P.manufacturerName))
                .isImported(t.get(P.isImported))
                .importCountry(t.get(P.importCountry))
                .stockQty(nz(t.get(P.stockQty)))
                .status(t.get(P.status) == null ? null
                        : ProductDetail.Status.valueOf(t.get(P.status).name()))
                .availability(t.get(P.availability) == null ? null
                        : ProductDetail.Availability.valueOf(t.get(P.availability).name()))
                .averageRating(t.get(P.averageRating))
                .reviewCount(nz(t.get(P.reviewCount)))
                .galleryImageUrls(galleryUrls)
                .descriptionImageUrls(descriptionUrls)
                .build();

        return Optional.of(detail);

    }
    /* ─────────────────────────────── 작은 도우미 ─────────────────────────────── */

    private static Integer nz(Integer n) {
        return (n == null) ? 0 : n;
    }
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
}




