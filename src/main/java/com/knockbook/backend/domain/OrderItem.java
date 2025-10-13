package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OrderItem {

    public enum RefType { BOOK_PURCHASE, BOOK_RENTAL, PRODUCT }

    private Long id;
    private Long orderId;
    private RefType refType;
    private Long refId;

    private String titleSnapshot;
    private String thumbnailUrl;

    private Integer listPriceSnapshot;
    private Integer salePriceSnapshot;
    private Integer quantity;

    private Integer rentalDays;
    private Integer rentalPriceSnapshot;

    private Integer pointsRate;
    private Integer pointsEarnedItem;

    private Integer lineSubtotalAmount;
    private Integer lineDiscountAmount;
    private Integer lineTotalAmount;
}
