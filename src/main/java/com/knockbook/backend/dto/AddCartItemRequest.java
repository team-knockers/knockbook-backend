package com.knockbook.backend.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AddCartItemRequest {
    private String refType;
    private String refId;
    @Min(1)
    private int rentalDays;
    @Min(1)
    private int quantity;
}
