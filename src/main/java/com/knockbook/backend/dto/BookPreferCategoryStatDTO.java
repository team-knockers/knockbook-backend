package com.knockbook.backend.dto;

import com.knockbook.backend.domain.BookPreferCategoryStat;
import lombok.*;

import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookPreferCategoryStatDTO {
    private String bookCategoryDisplayName;
    private double categoryReadRatio;

    public static BookPreferCategoryStatDTO of(String name, double ratio) {
        return BookPreferCategoryStatDTO.builder()
                .bookCategoryDisplayName(name)
                .categoryReadRatio(ratio)
                .build();
    }
}

