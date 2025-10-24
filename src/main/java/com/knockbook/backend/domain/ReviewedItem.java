package com.knockbook.backend.domain;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ReviewedItem {

    public enum ItemType { BOOK, PRODUCT }

    private ItemType itemType;
    private String id;
}
