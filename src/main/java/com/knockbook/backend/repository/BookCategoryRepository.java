package com.knockbook.backend.repository;

import com.knockbook.backend.domain.BookCategory;
import com.knockbook.backend.domain.BookSubcategory;

import java.util.List;

public interface BookCategoryRepository {

    /**
     * 모든 카테고리와 그 기본 정보를 조회 (서브카테고리 미포함)
     */
    List<BookCategory> findAllCategories();

    /**
     * categoryCodeName으로 해당 카테고리의 모든 서브카테고리 조회
     */
    List<BookSubcategory> findSubcategoriesByCategoryCodeName(String categoryCodeName);

    /**
     * findSubcategoriesByCategoryCodeName의 조회전 존재여부를 검토(에러 반환용)
     */
    boolean existsByCategoryCodeName(String categoryCodeName);

}
