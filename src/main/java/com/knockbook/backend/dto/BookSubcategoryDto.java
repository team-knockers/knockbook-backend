package com.knockbook.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSubcategoryDto {

    private String id;
    private String subcategoryCodeName;
    private String subcategoryDisplayName;
}
