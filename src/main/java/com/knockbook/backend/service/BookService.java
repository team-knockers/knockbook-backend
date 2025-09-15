package com.knockbook.backend.service;

import com.knockbook.backend.domain.Book;
import com.knockbook.backend.domain.BookSummary;
import com.knockbook.backend.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public Optional<Book> getBookDetailById(Long id) {

        // BookRepository에 ID 전달 → 조회 결과(Optional<Book>) 반환
        return bookRepository.findById(id);
    }

    public Page<BookSummary> getBooksByCategory(
            String categoryCodeName, String subcategoryCodeName, Pageable pageable,
            String searchBy, String searchKeyword, Integer maxPrice, Integer minPrice) {

        // BookRepository에 ID 전달 → 페이징 결과(Page<BookSummary>) 반환
        return bookRepository.findBooksByCondition(categoryCodeName, subcategoryCodeName, pageable,
                searchBy, searchKeyword, maxPrice, minPrice);
    }
}
