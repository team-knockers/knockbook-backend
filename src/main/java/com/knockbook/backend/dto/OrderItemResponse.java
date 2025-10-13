package com.knockbook.backend.dto;

import com.knockbook.backend.domain.OrderItem;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OrderItemResponse {
    private String id;
    private String orderId;
    private String refType;
    private String refId;

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

    public static OrderItemResponse toResponse(final OrderItem i) {
        return OrderItemResponse.builder()
                .id(i.getId() == null ? null : String.valueOf(i.getId()))
                .refType(i.getRefType().name())
                .refId(String.valueOf(i.getRefId()))
                .titleSnapshot(i.getTitleSnapshot())
                .thumbnailUrl(i.getThumbnailUrl())
                .listPriceSnapshot(i.getListPriceSnapshot())
                .salePriceSnapshot(i.getSalePriceSnapshot())
                .quantity(i.getQuantity())
                .rentalDays(i.getRentalDays())
                .rentalPriceSnapshot(i.getRentalPriceSnapshot())
                .pointsRate(i.getPointsRate())
                .pointsEarnedItem(i.getPointsEarnedItem())
                .lineSubtotalAmount(i.getLineSubtotalAmount())
                .lineDiscountAmount(i.getLineDiscountAmount())
                .lineTotalAmount(i.getLineTotalAmount())
                .build();
    }
}
