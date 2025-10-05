package com.knockbook.backend.dto;

import com.knockbook.backend.domain.CartItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CartItemDto {
    private String id;
    private String refType;
    private String refId;
    private String titleSnapshot;
    private String thumbnailUrl;
    private Integer listPriceSnapshot;
    private Integer salePriceSnapshot;
    private Integer rentalDays;
    private Integer rentalPriceSnapshot;
    private Integer quantity;
    private Integer pointsRate;

    public static CartItemDto fromModel(final CartItem domain) {
        return CartItemDto.builder()
                .id(toStr(domain.getId()))
                .refType(domain.getRefType().name())
                .refId(toStr(domain.getRefId()))
                .titleSnapshot(domain.getTitleSnapshot())
                .thumbnailUrl(domain.getThumbnailUrl())
                .listPriceSnapshot(domain.getListPriceSnapshot())
                .salePriceSnapshot(domain.getSalePriceSnapshot())
                .rentalDays(domain.getRentalDays())
                .rentalPriceSnapshot(domain.getRentalPriceSnapshot())
                .quantity(domain.getQuantity())
                .pointsRate(domain.getPointsRate())
                .build();
    }

    private static String toStr(final Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
