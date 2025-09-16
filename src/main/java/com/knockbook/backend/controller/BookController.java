package com.knockbook.backend.controller;

import com.knockbook.backend.dto.GetBookDetailsResponse;
import com.knockbook.backend.dto.BookDtoMapper;
import com.knockbook.backend.dto.GetBooksSummaryResponse;
import com.knockbook.backend.service.BookService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
}
