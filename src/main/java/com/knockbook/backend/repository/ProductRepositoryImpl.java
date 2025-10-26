package com.knockbook.backend.repository;

import com.knockbook.backend.domain.*;
import com.knockbook.backend.entity.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
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
    private final EntityManager em;
    private final JPAQueryFactory query;
    // QueryDSL Q-types (entity metamodels)
    private static final QProductEntity P = QProductEntity.productEntity;
    private static final QProductImageEntity PI = QProductImageEntity.productImageEntity;
    private static final QProductCategoryEntity PC = QProductCategoryEntity.productCategoryEntity;
    private static final QProductWishlistEntity PW = QProductWishlistEntity.productWishlistEntity;

    @Override
    public Page<Product> findAllPaged(Pageable pageable) {

        final var predicate = P.deletedAt.isNull();
        final var orderSpecifiers = toOrderSpecifiers(pageable, P);
        final var entities = query
                .selectFrom(P)
                .where(predicate)
                .orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final Long total = query
                .select(P.count())
                .from(P)
                .where(predicate)
                .fetchOne();
        final long totalElements = (total == null) ? 0L : total;

        final var content = entities.stream()
                .map(ProductEntity::toDomain)
                .toList();

        return new PageImpl<>(content, pageable, totalElements);
    }

    @Override
    public Page<ProductSummary> findProductSummaries(
            String category,
            String searchKeyword,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable,
            Long userId
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

        final var wishedByMeExpr = JPAExpressions.selectOne()
                .from(PW)
                .where(P.productId.eq(PW.productId).and(PW.userId.eq(userId)))
                .exists();

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
                        PI.imageUrl, // thumbnail (GALLERY, sort_order=1)
                        wishedByMeExpr
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
                        .wishedByMe(Boolean.TRUE.equals(t.get(wishedByMeExpr)))
                        .build())
                .toList();

        // Step 7: Return Page
        return new PageImpl<>(content, pageable, totalElements);
    }

    @Override
    public Optional<ProductResult> findProductById(
            Long productId
    ) {
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

    @Override
    public ProductResult createProduct(ProductCreateSpec spec) {
        final var categoryIdList = query
                .select(PC.categoryId)
                .from(PC)
                .where(
                        PC.codeName.eq(spec.getCategoryCode()),
                        PC.deletedAt.isNull()
                )
                .limit(1)
                .fetch();

        if (categoryIdList.isEmpty()) {
            throw new IllegalArgumentException("Invalid categoryCode: " + spec.getCategoryCode());
        }

        final var categoryId = categoryIdList.getFirst();

        final var productEntity = ProductEntity.builder()
                .categoryId(categoryId)
                .sku(spec.getSku())
                .name(spec.getName())
                .stockQty(spec.getStockQty())
                .unitPriceAmount(spec.getUnitPriceAmount())
                .salePriceAmount(spec.getSalePriceAmount())
                .manufacturerName(spec.getManufacturerName())
                .isImported(spec.getIsImported())
                .importCountry(spec.getImportCountry())
                .releasedAt(spec.getReleasedAt())
                .status(spec.getStatus())
                .availability(spec.getAvailability())
                .averageRating(new BigDecimal("0.0"))
                .reviewCount(0)
                .build();

        em.persist(productEntity);
        em.flush();
        final var newProductId = productEntity.getProductId();

        for (int i = 0; i < spec.getGalleryImageUrls().size(); i++) {
            final var url = spec.getGalleryImageUrls().get(i);

            final var galleryImage = ProductImageEntity.builder()
                    .productId(newProductId)
                    .sortOrder(i + 1)
                    .imageUsage(ProductImageEntity.ImageUsage.GALLERY)
                    .imageUrl(url)
                    .altText(spec.getName())
                    .build();

            em.persist(galleryImage);
        }

        for (int i = 0; i < spec.getDescriptionImageUrls().size(); i++) {
            final var url = spec.getDescriptionImageUrls().get(i);

            final var descImage = ProductImageEntity.builder()
                    .productId(newProductId)
                    .sortOrder(i + 1)
                    .imageUsage(ProductImageEntity.ImageUsage.DESCRIPTION)
                    .imageUrl(url)
                    .altText(spec.getName())
                    .build();

            em.persist(descImage);
        }

        em.flush();

        final var detail = ProductDetail.builder()
                .id(newProductId)
                .manufacturerName(spec.getManufacturerName())
                .isImported(spec.getIsImported())
                .importCountry(spec.getImportCountry())
                .galleryImageUrls(spec.getGalleryImageUrls())
                .descriptionImageUrls(spec.getDescriptionImageUrls())
                .build();

        final var summary = ProductSummary.builder()
                .id(newProductId)
                .categoryId(categoryId)
                .sku(spec.getSku())
                .name(spec.getName())
                .unitPriceAmount(spec.getUnitPriceAmount())
                .salePriceAmount(spec.getSalePriceAmount())
                .stockQty(spec.getStockQty())
                .status(ProductSummary.Status.valueOf(spec.getStatus().name()))
                .availability(ProductSummary.Availability.valueOf(spec.getAvailability().name()))
                .averageRating(0.0d)
                .reviewCount(0)
                .thumbnailUrl(
                        spec.getGalleryImageUrls().isEmpty()
                                ? null
                                : spec.getGalleryImageUrls().getFirst()
                )
                .build();

        return ProductResult.builder()
                .productDetail(detail)
                .productSummary(summary)
                .build();
    }

    @Override
    public ProductResult updateProduct(Long productId, ProductUpdateSpec spec) {

        final var existing = em.find(ProductEntity.class, productId);
        if (existing == null || existing.getDeletedAt() != null) {
            throw new IllegalArgumentException("Product not found or deleted: " + productId);
        }

    final var patchedEntity = ProductEntity.builder()
                .productId(existing.getProductId())
                .categoryId(existing.getCategoryId())
                .sku(existing.getSku())
                .name(existing.getName())
                .stockQty(spec.getStockQty() != null ? spec.getStockQty() : existing.getStockQty())
                .unitPriceAmount(spec.getUnitPriceAmount() != null ? spec.getUnitPriceAmount() : existing.getUnitPriceAmount())
                .salePriceAmount(spec.getSalePriceAmount() != null ? spec.getSalePriceAmount() : existing.getSalePriceAmount())
                .manufacturerName(existing.getManufacturerName())
                .isImported(existing.getIsImported())
                .importCountry(existing.getImportCountry())
                .createdAt(existing.getCreatedAt())
                .updatedAt(existing.getUpdatedAt())
                .deletedAt(existing.getDeletedAt())
                .releasedAt(existing.getReleasedAt())
                .status(spec.getStatus() != null ? spec.getStatus() : existing.getStatus())
                .availability(spec.getAvailability() != null ? spec.getAvailability() : existing.getAvailability())
                .averageRating(existing.getAverageRating())
                .reviewCount(existing.getReviewCount())
                .build();

        final var merged = em.merge(patchedEntity);
        em.flush();

        final var refreshed = em.find(ProductEntity.class, merged.getProductId());

        final var galleryUrls = query
                .select(QProductImageEntity.productImageEntity.imageUrl)
                .from(QProductImageEntity.productImageEntity)
                .where(
                        QProductImageEntity.productImageEntity.productId.eq(productId)
                                .and(QProductImageEntity.productImageEntity.imageUsage.eq(ProductImageEntity.ImageUsage.GALLERY))
                )
                .orderBy(QProductImageEntity.productImageEntity.sortOrder.asc())
                .fetch();

        final var descriptionUrls = query
                .select(QProductImageEntity.productImageEntity.imageUrl)
                .from(QProductImageEntity.productImageEntity)
                .where(
                        QProductImageEntity.productImageEntity.productId.eq(productId)
                                .and(QProductImageEntity.productImageEntity.imageUsage.eq(ProductImageEntity.ImageUsage.DESCRIPTION))
                )
                .orderBy(QProductImageEntity.productImageEntity.sortOrder.asc())
                .fetch();

        final var detail = ProductDetail.builder()
                .id(refreshed.getProductId())
                .manufacturerName(refreshed.getManufacturerName())
                .isImported(refreshed.getIsImported())
                .importCountry(refreshed.getImportCountry())
                .galleryImageUrls(galleryUrls)
                .descriptionImageUrls(descriptionUrls)
                .build();

        final var summary = ProductSummary.builder()
                .id(refreshed.getProductId())
                .categoryId(refreshed.getCategoryId())
                .sku(refreshed.getSku())
                .name(refreshed.getName())
                .unitPriceAmount(refreshed.getUnitPriceAmount())
                .salePriceAmount(refreshed.getSalePriceAmount())
                .stockQty(refreshed.getStockQty())
                .status(
                        refreshed.getStatus() == null ? null
                                : ProductSummary.Status.valueOf(refreshed.getStatus().name())
                )
                .availability(
                        refreshed.getAvailability() == null ? null
                                : ProductSummary.Availability.valueOf(refreshed.getAvailability().name())
                )
                .averageRating(
                        refreshed.getAverageRating() == null
                                ? 0.0d
                                : refreshed.getAverageRating()
                                .setScale(1, java.math.RoundingMode.HALF_UP)
                                .doubleValue()
                )
                .reviewCount(refreshed.getReviewCount())
                .thumbnailUrl(galleryUrls.isEmpty() ? null : galleryUrls.getFirst())
                .build();

        return ProductResult.builder()
                .productDetail(detail)
                .productSummary(summary)
                .build();
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
