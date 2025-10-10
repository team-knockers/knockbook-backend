package com.knockbook.backend.controller;

import com.knockbook.backend.domain.BookReview;
import com.knockbook.backend.domain.BookReviewImage;
import com.knockbook.backend.dto.*;
import com.knockbook.backend.service.BookService;
import com.knockbook.backend.service.UserService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/books")
@Validated
public class BookController {

    @Autowired
    private BookService bookService;

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
}
