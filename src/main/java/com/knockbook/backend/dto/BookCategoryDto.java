package com.knockbook.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCategoryDto {

    private String id;
    private String categoryCodeName;
    private String categoryDisplayName;
}
