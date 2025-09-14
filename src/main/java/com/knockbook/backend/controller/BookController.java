package com.knockbook.backend.controller;

import com.knockbook.backend.domain.BookSummary;
import com.knockbook.backend.dto.BookDtoMapper;
import com.knockbook.backend.dto.BookSummaryDto;
import com.knockbook.backend.dto.BookSummaryResponse;
import com.knockbook.backend.service.BookService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @PreAuthorize("#userId == authentication.name")
    @GetMapping(path = "/{userId}")
    public ResponseEntity<BookSummaryResponse> getBooksByCategory(
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

        // 1) PageRequest 생성 및 추가 조건
        final var zeroBasedPage = page - 1;
        final Sort sort = order.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        final Pageable pageable = PageRequest.of(zeroBasedPage, size, sort);

        // 2) 도메인 Book 페이지 조회
        final Page<BookSummary> bookPage = bookService.getBooksByCategory(
                category, subcategory, pageable, searchBy, searchKeyword, minPrice, maxPrice
        );

        // 3) 도메인 → DTO 매핑
        final Page<BookSummaryDto> dtoPage = bookPage.map(BookDtoMapper::toSummaryDto);

        // 4) BooksSummaryResponse 조립
        final BookSummaryResponse response = BookSummaryResponse.builder()
                .books(dtoPage.getContent())
                .page(dtoPage.getNumber()+1)
                .size(dtoPage.getSize())
                .totalItems((int) dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .build();

        // 5) 최종 반환
        return ResponseEntity.ok(response);
    }

}
