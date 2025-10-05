package com.knockbook.backend.dto;

import com.knockbook.backend.domain.Cart;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CartDto {
    private String id;
    private String userId;
    private String status;
    private List<CartItemDto> items;
    private Integer itemCount;
    private Integer subtotalAmount;
    private Integer discountAmount;
    private Integer shippingAmount;
    private Integer rentalAmount;
    private Integer totalAmount;
    private Integer pointsEarnable;

    public static CartDto fromModel(final Cart domain) {
        final var items = domain.getItems() == null
                ? List.<CartItemDto>of()
                : domain.getItems().stream()
                .map(CartItemDto::fromModel)
                .collect(Collectors.toList());

        return CartDto.builder()
                .id(toStr(domain.getId()))
                .userId(toStr(domain.getUserId()))
                .status(domain.getStatus().name())
                .items(items)
                .itemCount(domain.getItemCount())
                .subtotalAmount(domain.getSubtotalAmount())
                .discountAmount(domain.getDiscountAmount())
                .shippingAmount(domain.getShippingAmount())
                .rentalAmount(domain.getRentalAmount())
                .totalAmount(domain.getTotalAmount())
                .pointsEarnable(domain.getPointsEarnable())
                .build();
    }

    private static String toStr(final Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
