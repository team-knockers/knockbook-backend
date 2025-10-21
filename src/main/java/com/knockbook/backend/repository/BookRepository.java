package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Book;
import com.knockbook.backend.domain.BookSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BookRepository {

    // TODO: 추후 save() 작성 (현재단계에서 맞지 않음)

    /**
     * Find a book by its ID.
     */
    Optional<Book> findById(final Long id);

    /**
     * Find books by category and subcategory with pagination.
     */
    Page<BookSummary> findBooksByCondition(
            final String categoryCodeName,
            final String subcategoryCodeName,
            final Pageable pageable,
            final String searchBy,
            final String searchKeyword,
            final Integer maxPrice,
            final Integer minPrice
    );

    boolean saveBookWishlist(Long userId, Long bookId);

    boolean deleteBookWishlist(Long userId, Long bookId);
}
