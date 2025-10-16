package com.knockbook.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CartRef {
    private final String refType; // "BOOK_PURCHASE"/"PRODUCT"/"BOOK_RENTAL"
    private final Long refId;
    private final Integer rentalDays;
}
