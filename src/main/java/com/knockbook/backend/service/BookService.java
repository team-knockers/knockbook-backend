package com.knockbook.backend.service;

import com.knockbook.backend.domain.Book;
import com.knockbook.backend.repository.BookCategoryRepository;
import com.knockbook.backend.repository.BookRepository;
import com.knockbook.backend.repository.BookSubcategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCategoryRepository categoryRepository;

    @Autowired
    private BookSubcategoryRepository subcategoryRepository;

    public Page<Book> getBooksByCategory(
            String categoryCodeName, String subcategoryCodeName, Pageable pageable,
            String sortBy, String order, Integer maxPrice, Integer minPrice) {

        // BookRepository에 ID 전달 → 페이징 결과(Page<Book>) 반환
        return bookRepository.findByCategory(categoryCodeName, subcategoryCodeName, pageable, sortBy, order, maxPrice, minPrice);
    }

}
