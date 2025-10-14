package com.knockbook.backend.dto;

import com.knockbook.backend.domain.OrderAggregate;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OrderResponse {
    private String id;
    private String userId;
    private String cartId;
    private String status;
    private String paymentStatus;

    private Integer itemCount;
    private Integer subtotalAmount;
    private Integer discountAmount;
    private Integer couponDiscountAmount;
    private Integer shippingAmount;
    private Integer rentalAmount;
    private Integer totalAmount;

    private String appliedCouponIssuanceId;
    private Integer pointsSpent;
    private Integer pointsEarned;

    private List<OrderItemResponse> items;

    public static OrderResponse toResponse(final OrderAggregate agg) {
        return OrderResponse.builder()
                .id(String.valueOf(agg.getId()))
                .userId(String.valueOf(agg.getUserId()))
                .cartId(String.valueOf(agg.getCartId()))
                .status(agg.getStatus().name())
                .paymentStatus(agg.getPaymentStatus().name())
                .itemCount(agg.getItemCount())
                .subtotalAmount(agg.getSubtotalAmount())
                .discountAmount(agg.getDiscountAmount())
                .shippingAmount(agg.getShippingAmount())
                .rentalAmount(agg.getRentalAmount())
                .totalAmount(agg.getTotalAmount())
                .appliedCouponIssuanceId(safeConvert(agg.getAppliedCouponIssuanceId()))
                .couponDiscountAmount(agg.getCouponDiscountAmount())
                .pointsSpent(agg.getPointsSpent())
                .pointsEarned(agg.getPointsEarned())
                .items(agg.getItems().stream()
                        .map(OrderItemResponse::toResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private static String safeConvert(final Long value) {
        return value == null ? null : String.valueOf(value);
    }
}
