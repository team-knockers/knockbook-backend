package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Book;
import com.knockbook.backend.domain.BookSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BookRepository {


    /**
     * Insert a new book or update an existing book.
     */
    Book save(final Book book);

    /**
     * Find a book by its ID.
     */
    Optional<Book> findById(final Long id);

    Map<Long, Book> findByIdsAsMap(final List<Long> ids);
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

    boolean activateBookWishlist(Long userId, Long bookId);

    boolean deactivateBookWishlist(Long userId, Long bookId);

    boolean existsBookWishlist(Long userId, Long bookId);

    List<BookSummary> findAllWishlistedBookIdsByUserId(Long userId);
}
