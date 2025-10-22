package com.knockbook.backend.dto;

import com.knockbook.backend.domain.BookPreferCategoryStat;
import lombok.*;

import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookPreferCategoryStatDTO {
    private Map<String, Double> bookCategoryDisplayNameAndReadRatePair;

    public static BookPreferCategoryStatDTO fromDomain(BookPreferCategoryStat d) {
        return BookPreferCategoryStatDTO.builder()
                .bookCategoryDisplayNameAndReadRatePair(d.getBookCategoryDisplayNameAndReadRatePair())
                .build();
    }
}
