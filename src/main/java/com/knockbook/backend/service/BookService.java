package com.knockbook.backend.service;

import com.knockbook.backend.domain.Book;
import com.knockbook.backend.domain.BookCategory;
import com.knockbook.backend.domain.BookSubcategory;
import com.knockbook.backend.domain.BookSummary;
import com.knockbook.backend.exception.BookNotFoundException;
import com.knockbook.backend.exception.CategoryNotFoundException;
import com.knockbook.backend.repository.BookCategoryRepository;
import com.knockbook.backend.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

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

    public List<BookCategory> getAllCategories() {

        return bookCategoryRepository.findAllCategories();
    }

    public List<BookSubcategory> getSubcategoriesByCategoryCodeName(String categoryCodeName) {
        boolean categoryExists = bookCategoryRepository.existsByCategoryCodeName(categoryCodeName);
        if (!categoryExists) {
            throw new CategoryNotFoundException(categoryCodeName);
        }

        return bookCategoryRepository.findSubcategoriesByCategoryCodeName(categoryCodeName);
    }
}
