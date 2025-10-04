package com.knockbook.backend.dto;

import com.knockbook.backend.domain.CartItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AddCartItemRequest {
    @NotNull
    private CartItem.RefType refType;

    @NotNull @Positive
    private String refId;

    @Positive
    private Integer rentalDays;

    @Min(1)
    private Integer quantity;
}
