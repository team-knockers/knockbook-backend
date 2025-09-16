package com.knockbook.backend.service;

import com.knockbook.backend.domain.Book;
import com.knockbook.backend.domain.BookSummary;
import com.knockbook.backend.exception.BookNotFoundException;
import com.knockbook.backend.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public Book getBookDetails(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(String.valueOf(id)));
    }

    public Page<BookSummary> getBooksSummary(
            String categoryCodeName, String subcategoryCodeName, Pageable pageable,
            String searchBy, String searchKeyword, Integer maxPrice, Integer minPrice) {

        return bookRepository.findBooksByCondition(categoryCodeName, subcategoryCodeName, pageable,
                searchBy, searchKeyword, maxPrice, minPrice);
    }
}
