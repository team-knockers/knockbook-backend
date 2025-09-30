package com.knockbook.backend.repository;

import com.knockbook.backend.domain.BookCategory;
import com.knockbook.backend.domain.BookSubcategory;

import java.util.List;

public interface BookCategoryRepository {

    /**
     * Retrieve all categories (excluding subcategories)
     */
    List<BookCategory> findAllCategories();

    /**
     * Retrieve all subcategories for the category identified by the given code name
     */
    List<BookSubcategory> findSubcategoriesByCategoryCodeName(String categoryCodeName);

    /**
     * Check whether a category with the given code name exists
     * Intended for pre-checks before performing operations such as subcategory retrieval
     */
    boolean existsByCategoryCodeName(String categoryCodeName);

}
