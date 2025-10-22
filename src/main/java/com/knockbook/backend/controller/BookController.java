package com.knockbook.backend.controller;

import com.knockbook.backend.domain.BookReview;
import com.knockbook.backend.domain.BookReviewImage;
import com.knockbook.backend.domain.BookWishlistAction;
import com.knockbook.backend.dto.*;
import com.knockbook.backend.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(path = "/books")
@Validated
public class BookController {

    @Autowired
    private BookService bookService;

    // API-BOOKS-01 - Retrieve paginated book summaries for a user with optional filters and sorting
    @PreAuthorize("#userId == authentication.name")
    @GetMapping(path = "/{userId}")
    public ResponseEntity<GetBooksSummaryResponse> getBooksSummary(
            @PathVariable("userId") String userId,
            @RequestParam("category") String category,
            @RequestParam("subcategory") String subcategory,
            @RequestParam("page") @Min(value = 1) int page,
            @RequestParam("size") @Min(value = 1) @Max(value = 50) int size,
            @RequestParam(required = false, defaultValue ="published") String sortBy,
            @RequestParam(required = false, defaultValue ="desc") String order,
            @RequestParam(required = false) String searchBy,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice
    ) {

        // 1) Create PageRequest
        final var zeroBasedPage = page - 1;
        final var sort = order.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        final var pageable = PageRequest.of(zeroBasedPage, size, sort);

        // 2) Retrieve paged BookSummary from domain
        final var bookPage = bookService.getBooksSummary(
                category, subcategory, pageable, searchBy, searchKeyword, minPrice, maxPrice
        );

        // 3) Map domain object to DTO
        final var dtoPage = bookPage.map(BookDtoMapper::toSummaryDto);

        // 4) Build BooksSummaryResponse
        final var response = GetBooksSummaryResponse.builder()
                .books(dtoPage.getContent())
                .page(dtoPage.getNumber()+1)
                .size(dtoPage.getSize())
                .totalItems((int) dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .build();

        // 5) Return final response
        return ResponseEntity.ok(response);
    }

    // API-BOOKS-02 - Retrieve detailed information for a specific book
    @PreAuthorize("#userId == authentication.name")
    @GetMapping(path = "/{userId}/{bookId}")
    public ResponseEntity<GetBookDetailsResponse> getBookDetails(
            @PathVariable("userId") String userId,
            @PathVariable("bookId") String bookId
    ) {
        // 1) Convert input ID (String -> Long)
        final var id = Long.valueOf(bookId);

        // 2) Retrieve detailed book information from domain
        final var bookDetail = bookService.getBookDetails(id);

        // 3) Map domain object to DTO
        final var response = BookDtoMapper.toDetailDto(bookDetail);

        // 4) Return final response
        return ResponseEntity.ok(response);
    }

    // API-BOOKS-03 - Retrieve paginated reviews for a specific book, optionally filtered by transaction type and MBTI
    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}/{bookId}/reviews")
    public ResponseEntity<GetBookReviewsResponse> getBookReviews(
            @PathVariable("userId") String userId,
            @PathVariable("bookId") String bookId,
            @RequestParam("page") @Min(1) int page,
            @RequestParam("size") @Min(1) @Max(50) int size,
            @RequestParam(required = false, defaultValue = "all") String transactionType,
            @RequestParam(required = false, defaultValue = "likes") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order,
            @RequestParam(required = false, defaultValue = "false") Boolean sameMbti
    ) {

        final var zeroBasedPage = page - 1;
        final var sort = order.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        final var pageable = PageRequest.of(zeroBasedPage, size, sort);

        final var currentUserId = Long.valueOf(userId); // current authenticated user id

        // 1) Fetch paged reviews including reviewer fields
        final var reviewsPage = bookService.getBookReviews(Long.valueOf(bookId), pageable,
                transactionType, currentUserId, sameMbti);

        // 2) Fetch liked review ids for the current user
        final var reviewIds = reviewsPage.stream()
                .map(BookReview::getId)
                .toList();
        final var likedReviewIds = bookService.getLikedReviewIds(currentUserId, reviewIds);

        // 3) Map domain reviews to DTOs
        final var reviewDtos = reviewsPage.stream()
                .map(r -> BookReviewDto.builder()
                        .id(String.valueOf(r.getId()))
                        .bookId(String.valueOf(r.getBookId()))
                        .userId(String.valueOf(r.getUserId()))
                        .displayName(r.getDisplayName())
                        .mbti(r.getMbti())
                        .transactionType(r.getTransactionType().name())
                        .createdAt(r.getCreatedAt())
                        .content(r.getContent())
                        .rating(r.getRating())
                        .imageUrls(r.getImageUrls() == null ? List.of() : r.getImageUrls().stream()
                                .map(BookReviewImage::getImageUrl)
                                .toList())
                        .likesCount(r.getLikesCount())
                        .likedByMe(likedReviewIds.contains(r.getId()))
                        .build()
                )
                .toList();

        // 4) build response using incoming page/size and page metadata
        final var response = GetBookReviewsResponse.builder()
                .reviews(reviewDtos)
                .page(page)
                .size(size)
                .totalItems((int) reviewsPage.getTotalElements())
                .totalPages(reviewsPage.getTotalPages())
                .build();

        return ResponseEntity.ok(response);
    }

    // API-BOOKS-04 - Retrieve aggregated review statistics for a specific book
    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}/{bookId}/reviews/statistics")
    public ResponseEntity<GetBookReviewStatisticsResponse> getBookReviewStatistics(
            @PathVariable("userId") String userId,
            @PathVariable("bookId") String bookId
    ) {
        // 1) Fetch aggregated statistics from service
        final var stat = bookService.getBookReviewStatistics(Long.valueOf(bookId));

        // 2) Extract aggregated values (totalCount, averageRating)
        final var totalCount =  stat.getTotalCount();
        final var averageRating = stat.getAverageRating();

        // 3) Map score counts to DTOs
        final var scoreDtos = stat.getScoreCounts().stream()
                .map(s -> BookReviewScoreCountDto.builder()
                        .score(s.getScore())
                        .count(s.getCount() == null ? 0 : s.getCount().intValue())
                        .build())
                .toList();

        // 4) Compute MBTI percentages (rounded to 1 decimal) and map to DTOs
        final var mbtiDtos = stat.getMbtiCounts().stream()
                .map(m -> {
                    final var pct = totalCount == 0L ? 0.0 : (m.getCount() * 100.0) / totalCount;
                    final var rounded = Math.round(pct * 10.0) / 10.0;
                    return BookReviewMbtiPercentageDto.builder()
                            .mbti(m.getMbti())
                            .percentage(rounded)
                            .build();
                })
                .sorted(java.util.Comparator.comparing(BookReviewMbtiPercentageDto::getPercentage).reversed())
                .toList();

        // 5) Build response DTO (clamp reviewCount to Integer.MAX_VALUE to avoid overflow)
        final var reviewCount = totalCount > Integer.MAX_VALUE
                ? Integer.MAX_VALUE
                : totalCount.intValue();

        final var response = GetBookReviewStatisticsResponse.builder()
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .scoreCounts(scoreDtos)
                .mbtiPercentage(mbtiDtos)
                .build();

        return ResponseEntity.ok(response);
    }

    // API-BOOKS-05 - Like a specific review for the current user
    @PreAuthorize("#userId == authentication.name")
    @PutMapping("/{userId}/{bookId}/reviews/{reviewId}/likes")
    public ResponseEntity<BookReviewsLikeResponse> likeReview(
            @PathVariable String userId,
            @PathVariable String bookId,
            @PathVariable String reviewId
    ) {

        var response = bookService.likeReview(Long.valueOf(userId), Long.valueOf(reviewId));
        return ResponseEntity.ok(response);
    }

    // API-BOOKS-06 - Remove a like from a specific review for the current user
    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/{userId}/{bookId}/reviews/{reviewId}/likes")
    public ResponseEntity<Void> unlikeReview(
            @PathVariable String userId,
            @PathVariable String bookId,
            @PathVariable String reviewId
    ) {

        bookService.unlikeReview(Long.valueOf(userId), Long.valueOf(reviewId));
        return ResponseEntity.noContent().build();
    }

    // API-BOOKS-07 - Add a specific book to the user's wishlist
    @PreAuthorize("#userId == authentication.name")
    @PutMapping("/{userId}/{bookId}/wish")
    public ResponseEntity<BookWishlistActionResponse> addToWishlist(
            @PathVariable String userId,
            @PathVariable String bookId
    ) {
        final var action = bookService.addToWishlist(Long.valueOf(userId), Long.valueOf(bookId));
        final var response = BookWishlistActionResponse.builder()
                .bookId(bookId)
                .wishlisted(action == BookWishlistAction.ADDED || action == BookWishlistAction.ALREADY_EXISTS)
                .action(action.name())
                .build();

        return ResponseEntity.ok(response);
    }

    // API-BOOKS-08 - Remove a specific book from the user's wishlist
    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/{userId}/{bookId}/wish")
    public ResponseEntity<BookWishlistActionResponse> removeFromWishlist(
            @PathVariable String userId,
            @PathVariable String bookId
    ) {
        final var action = bookService.removeFromWishlist(Long.valueOf(userId), Long.valueOf(bookId));
        final var response = BookWishlistActionResponse.builder()
                .bookId(bookId)
                .wishlisted(action == BookWishlistAction.ADDED || action == BookWishlistAction.ALREADY_EXISTS)
                .action(action.name())
                .build();

        return ResponseEntity.ok(response);
    }

    // API-BOOKS-09 - Check if a specific book is in the user's wishlist
    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}/{bookId}/wish")
    public ResponseEntity<BookWishStatusResponse> hasBookInWishlist(
            @PathVariable String userId,
            @PathVariable String bookId
    ) {
        final var wished = bookService.hasBookInWishlist(Long.valueOf(userId), Long.valueOf(bookId));
        final var response = BookWishStatusResponse.builder()
                .wished(wished)
                .build();

        return ResponseEntity.ok(response);
    }

    // API-BOOKS-10 - Retrieve all books in the user's wishlist
    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}/wishlist")
    public ResponseEntity<List<BookSummaryDto>> getUserWishlist(
            @PathVariable String userId
    ) {
        final var bookSummaries = bookService.getUserWishlist(Long.valueOf(userId));

        final var response = bookSummaries.stream()
                .map(BookDtoMapper::toSummaryDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    // API-BOOKS-11 - Retrieve all book categories
    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}/categories")
    public ResponseEntity<List<BookCategoryDto>> getAllCategories(
            @PathVariable String userId
    ) {
        final var bookCategoryList = bookService.getAllCategories();

        final var response = bookCategoryList.stream()
                .map(c -> BookCategoryDto.builder()
                        .id(String.valueOf(c.getId()))
                        .categoryCodeName(c.getCodeName().name())
                        .categoryDisplayName(c.getDisplayName())
                        .build())
                .toList();

        return ResponseEntity.ok(response);
    }

    // API-BOOKS-12 - Retrieve all subcategories for a given category code
    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}/categories/{categoryCodeName}/subcategories")
    public ResponseEntity<List<BookSubcategoryDto>> getSubcategories(
            @PathVariable String userId,
            @PathVariable String categoryCodeName
    ) {
        final var bookSubcategoryList = bookService.getSubcategoriesByCategoryCodeName(categoryCodeName);

        final var response = bookSubcategoryList.stream()
                .map(s -> BookSubcategoryDto.builder()
                        .id(String.valueOf(s.getId()))
                        .subcategoryCodeName(s.getCodeName().name())
                        .subcategoryDisplayName(s.getDisplayName())
                        .build())
                .toList();

        return ResponseEntity.ok(response);
    }

    // API-BOOKS-13 - Create a review for a specific book with optional images
    @PreAuthorize("#userId == authentication.name")
    @PostMapping(value = "/{userId}/{bookId}/reviews", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<BookReviewDto> createReview(
            @PathVariable String userId,
            @PathVariable String bookId,
            @RequestPart("review") @Valid BookReviewCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        final var review = BookReview.builder()
                .userId(Long.valueOf(userId))
                .bookId(Long.valueOf(bookId))
                .transactionType(BookReview.TransactionType.valueOf(request.getTransactionType().toUpperCase()))
                .content(request.getContent())
                .rating(request.getRating())
                .build();

        final var savedReview = bookService.createReview(review, images);

        final var response = BookReviewDto.builder()
                .id(String.valueOf(savedReview.getId()))
                .bookId(String.valueOf(savedReview.getBookId()))
                .userId(String.valueOf(savedReview.getUserId()))
                .displayName(savedReview.getDisplayName())
                .mbti(savedReview.getMbti())
                .transactionType(savedReview.getTransactionType().name())
                .content(savedReview.getContent())
                .rating(savedReview.getRating())
                .likesCount(savedReview.getLikesCount())
                .createdAt(savedReview.getCreatedAt())
                .imageUrls(savedReview.getImageUrls() == null
                        ? List.of()
                        : savedReview.getImageUrls().stream()
                        .map(BookReviewImage::getImageUrl)
                        .toList())
                .likedByMe(false)
                .build();

        return ResponseEntity.ok(response);
    }

    // API-BOOKS-14 - Soft delete a specific review for the current user
    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/{userId}/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable("userId") String userId,
            @PathVariable String reviewId
    ) {
        bookService.deleteReview(Long.valueOf(reviewId), Long.valueOf(userId));
        return ResponseEntity.noContent().build();
    }
}
