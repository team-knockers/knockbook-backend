package com.knockbook.backend.repository;

import com.knockbook.backend.domain.ProductDetail;
import com.knockbook.backend.domain.ProductResult;
import com.knockbook.backend.domain.ProductSummary;
import com.knockbook.backend.entity.ProductEntity;
import com.knockbook.backend.entity.ProductImageEntity;
import com.knockbook.backend.entity.QProductCategoryEntity;
import com.knockbook.backend.entity.QProductEntity;
import com.knockbook.backend.entity.QProductImageEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final JPAQueryFactory query;
    // QueryDSL Q-types (entity metamodels)
    private static final QProductEntity P = QProductEntity.productEntity;
    private static final QProductImageEntity PI = QProductImageEntity.productImageEntity;
    private static final QProductCategoryEntity PC = QProductCategoryEntity.productCategoryEntity;

    @Override
    public Page<ProductSummary> findProductSummaries(
            String category,
            String searchKeyword,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable
    ) {
        // Step 1: Base filters (ACTIVE + not deleted)
        final var predicate = new BooleanBuilder()
                .and(P.status.eq(ProductEntity.Status.ACTIVE))
                .and(P.deletedAt.isNull());

        // Step 2: Optional filters (category, keyword, price range)
        if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category)) {
            predicate.and(PC.codeName.eq(category.trim()));
        }
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            predicate.and(P.name.like("%" + searchKeyword.trim() + "%"));
        }
        if (minPrice != null) predicate.and(P.unitPriceAmount.goe(minPrice));
        if (maxPrice != null) predicate.and(P.unitPriceAmount.loe(maxPrice));

        // Step 3: Sorting (translate Pageable → QueryDSL OrderSpecifiers)
        final var orderSpecifiers = toOrderSpecifiers(pageable, P);

        // Step 4: Fetch page content (join category for code_name; join first GALLERY image as thumbnail)
        final var rows = query
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
                        PI.imageUrl // thumbnail (GALLERY, sort_order=1)
                )
                .from(P)
                .leftJoin(PC).on(PC.categoryId.eq(P.categoryId))
                .leftJoin(PI).on(
                        PI.productId.eq(P.productId)
                                .and(PI.imageUsage.eq(ProductImageEntity.ImageUsage.GALLERY))
                                .and(PI.sortOrder.eq(1))
                )
                .where(predicate)
                .orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Step 5: Count total (same predicate; separate count query)
        final var total = query
                .select(P.count())
                .from(P)
                .leftJoin(PC).on(PC.categoryId.eq(P.categoryId))
                .where(predicate)
                .fetchOne();
        final var totalElements = (total == null) ? 0L : total;

        // Step 6: Map tuples → domain objects
        final var content = rows.stream()
                .map(t -> ProductSummary.builder()
                        .id(t.get(P.productId))
                        .categoryId(t.get(P.categoryId))
                        .sku(t.get(P.sku))
                        .name(t.get(P.name))
                        .unitPriceAmount(t.get(P.unitPriceAmount))
                        .salePriceAmount(t.get(P.salePriceAmount))
                        .stockQty(t.get(P.stockQty))
                        // Entity enum → domain enum
                        .status(
                                t.get(P.status) == null ? null
                                        : ProductSummary.Status.valueOf(t.get(P.status).name())
                        )
                        .availability(
                                t.get(P.availability) == null ? null
                                        : ProductSummary.Availability.valueOf(t.get(P.availability).name())
                        )
                        // BigDecimal → double (1dp, HALF_UP)
                        .averageRating(toScale(t.get(P.averageRating)))
                        .reviewCount(t.get(P.reviewCount))
                        .thumbnailUrl(t.get(PI.imageUrl))
                        .build())
                .toList();

        // Step 7: Return Page
        return new PageImpl<>(content, pageable, totalElements);
    }

    @Override
    public Optional<ProductResult> findProductById(Long productId) {
        // Step 1: Base row fetch (ACTIVE + not deleted)
        final var t = query
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

        // Step 2: Not found → empty
        if (t == null) return Optional.empty();

        // Step 3: Load GALLERY images (max 4, by sort_order)
        final var galleryUrls = query
                .select(PI.imageUrl)
                .from(PI)
                .where(
                        PI.productId.eq(productId)
                                .and(PI.imageUsage.eq(ProductImageEntity.ImageUsage.GALLERY))
                ).orderBy(PI.sortOrder.asc())
                .limit(4)
                .fetch();

        // Step 4: Load DESCRIPTION images (all, by sort_order)
        final var descriptionUrls = query
                .select(PI.imageUrl)
                .from(PI)
                .where(
                        PI.productId.eq(productId)
                                .and(PI.imageUsage.eq(ProductImageEntity.ImageUsage.DESCRIPTION))
                )
                .orderBy(PI.sortOrder.asc())
                .fetch();

        // Step 5: Map to ProductDetail
        final var detail = ProductDetail.builder()
                .id(t.get(P.productId))
                .manufacturerName(t.get(P.manufacturerName))
                .isImported(t.get(P.isImported))
                .importCountry(t.get(P.importCountry))
                .galleryImageUrls(galleryUrls)
                .descriptionImageUrls(descriptionUrls)
                .build();

        // Step 6: Map to ProductSummary
        final var summary = ProductSummary.builder()
                .id(t.get(P.productId))
                .categoryId(t.get(P.categoryId))
                .sku(t.get(P.sku))
                .name(t.get(P.name))
                .unitPriceAmount(t.get(P.unitPriceAmount))
                .salePriceAmount(t.get(P.salePriceAmount))
                .stockQty(t.get(P.stockQty))
                .status(t.get(P.status) == null ? null
                        : ProductSummary.Status.valueOf(t.get(P.status).name()))
                .availability(t.get(P.availability) == null ? null
                        : ProductSummary.Availability.valueOf(t.get(P.availability).name()))
                // BigDecimal → double (1dp, HALF_UP)
                .averageRating(toScale(t.get(P.averageRating)))
                .reviewCount(t.get(P.reviewCount))
                .thumbnailUrl(galleryUrls.getFirst())
                .build();

        // Step 7: Wrap to ProductResult and return
        final var result = ProductResult.builder()
                .productDetail(detail)
                .productSummary(summary)
                .build();

        return Optional.of(result);

    }

    // ===== Helpers =====

    // BigDecimal → double (1dp, HALF_UP)
    private static Double toScale(BigDecimal v) {
        return v == null ? null : v.setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    // Translate Spring Pageable sorting → QueryDSL order specifiers
    // Supported keys: createdAt, unitPriceAmount, averageRating, reviewCount
    // If empty, fallback: averageRating DESC → reviewCount DESC → createdAt DESC
    private static List<OrderSpecifier<?>> toOrderSpecifiers(Pageable pageable, QProductEntity p) {
        final var list = new ArrayList<OrderSpecifier<?>>();
        pageable.getSort().forEach(o -> {
            final var asc = o.isAscending();
            switch (o.getProperty()) {
                case "createdAt"       -> list.add(asc ? p.createdAt.asc() : p.createdAt.desc());
                case "unitPriceAmount" -> list.add(asc ? p.unitPriceAmount.asc() : p.unitPriceAmount.desc());
                case "averageRating"   -> list.add(asc ? p.averageRating.asc() : p.averageRating.desc());
                case "reviewCount"     -> list.add(asc ? p.reviewCount.asc() : p.reviewCount.desc());
                default -> { /* ignore unknown keys */ }
            }
        });
        if (list.isEmpty()) {
            list.add(p.averageRating.desc());
            list.add(p.reviewCount.desc());
            list.add(p.createdAt.desc());
        }
        return list;
    }
}
