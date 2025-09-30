package com.knockbook.backend.exception;

public class CategoryNotFoundException extends ApplicationException {

    public CategoryNotFoundException(String categoryCodeName) {
        super("CATEGORY_NOT_FOUND", "Category not found: category=%s".formatted(categoryCodeName));
    }
}
