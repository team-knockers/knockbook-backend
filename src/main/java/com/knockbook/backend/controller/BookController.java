package com.knockbook.backend.controller;

import com.knockbook.backend.domain.Book;
import com.knockbook.backend.dto.BookDtoMapper;
import com.knockbook.backend.dto.BookSummaryDto;
import com.knockbook.backend.dto.BookSummaryResponse;
import com.knockbook.backend.service.BookService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping(path = "/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @PreAuthorize("#userId == authentication.name")
    @GetMapping(path = "/{userId}")
    public ResponseEntity<BookSummaryResponse> getBooksByCategory(
            @PathVariable("userId") String userId,
            @RequestParam("category")   String category,
            @RequestParam("subcategory") String subcategory,
            @RequestParam("page")@Min(value = 1) int page,
            @RequestParam("size")@Min(value = 1)@Max(value = 50) int size,
            @RequestParam(defaultValue ="published") String sortBy,
            @RequestParam(defaultValue ="desc") String order,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice

    ) {

        // 1) PageRequest 생성 및 추가 조건

        // 초기페이지 설정 변경(0 -> 1)
        int zeroBasedPage = page - 1;
        PageRequest pageRequest = PageRequest.of(zeroBasedPage, size);

        // 카테고리, 서브카테고리 입력값이 "all"이면 null로 넘기도록
        String categoryFilter = "all".equalsIgnoreCase(category) ? null : category;
        String subcategoryFilter = "all".equalsIgnoreCase(subcategory) ? null : subcategory;

        // 2) 도메인 Book 페이지 조회
        Page<Book> domainPage = bookService.getBooksByCategory(
                categoryFilter, subcategoryFilter, pageRequest, sortBy, order, minPrice, maxPrice
        );

        // 3) 도메인 → DTO 매핑
        Page<BookSummaryDto> dtoPage = domainPage.map(BookDtoMapper::toSummaryDto);

        // 4) BooksSummaryResponse 조립
        BookSummaryResponse response = BookSummaryResponse.builder()
                .books(dtoPage.getContent())
                .page(domainPage.getNumber()+1)
                .size(domainPage.getSize())
                .totalItems((int) domainPage.getTotalElements())
                .totalPages(domainPage.getTotalPages())
                .build();

        // 5) 최종 반환
        return ResponseEntity.ok(response);
    }

}
