package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "vw_book_categories_with_subs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA default constructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder only
@Builder
public class VwBookCategoriesWithSubsEntity {

    @Id
    @Column(name = "vw_id", nullable = false)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "category_code_name", nullable = false)
    private String categoryCodeName;

    @Column(name = "category_display_name", nullable = false)
    private String categoryDisplayName;

    @Column(name = "subcategory_id", nullable = false)
    private Long subcategoryId;

    @Column(name = "subcategory_code_name", nullable = false)
    private String subcategoryCodeName;

    @Column(name = "subcategory_display_name", nullable = false)
    private String subcategoryDisplayName;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;
}
