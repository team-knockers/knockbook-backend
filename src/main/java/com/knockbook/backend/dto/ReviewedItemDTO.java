package com.knockbook.backend.dto;

import com.knockbook.backend.domain.ReviewedItem;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ReviewedItemDTO {

    private String itemType; // BOOK_RENTAL, BOOK_PURCHASE, PRODUCT
    private String id;

    public static ReviewedItemDTO fromModel(ReviewedItem item) {
        return ReviewedItemDTO.builder()
                .itemType(item.getItemType().name())
                .id(item.getId())
                .build();
    }
}
