package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CartItem {

    public enum RefType { BOOK_PURCHASE, BOOK_RENTAL, PRODUCT }

    private final Long id;
    private final Long cartId;
    private final RefType refType;
    private final Long refId;

    private final String titleSnapshot;
    private final String thumbnailUrl;

    private final Integer listPriceSnapshot;
    private final Integer salePriceSnapshot;

    private final int rentalDays;
    private final Integer rentalPriceSnapshot;

    private final int quantity;
    private final BigDecimal pointsRate;
}

