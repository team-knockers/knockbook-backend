package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookRepository {

    // TODO: 추후 save(), findById() 작성 (현재단계에서 맞지 않음)

    /**
     * Find books by category and subcategory with pagination.
     */
    Page<Book> findByCategory(
            final String categoryCodeName,
            final String subcategoryCodeName,
            final Pageable pageable,
            final String sortBy,
            final String order,
            final Integer maxPrice,
            final Integer minPrice

    );

    // TODO: 추후 검색어 기반 조건 작성 (API-BOOKs_01 추가분)
}
