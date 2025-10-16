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

    // Return a copy with only the orderId injected
    public OrderItem withOrderId(final Long newOrderId) {
        return OrderItem.builder()
                .id(id)
                .orderId(newOrderId)
                .refType(refType)
                .refId(refId)
                .titleSnapshot(titleSnapshot)
                .thumbnailUrl(thumbnailUrl)
                .listPriceSnapshot(listPriceSnapshot)
                .salePriceSnapshot(salePriceSnapshot)
                .quantity(quantity)
                .rentalDays(rentalDays)
                .rentalPriceSnapshot(rentalPriceSnapshot)
                .pointsRate(pointsRate)
                .pointsEarnedItem(pointsEarnedItem)
                .lineSubtotalAmount(lineSubtotalAmount)
                .lineDiscountAmount(lineDiscountAmount)
                .lineTotalAmount(lineTotalAmount)
                .build();
    }
}
