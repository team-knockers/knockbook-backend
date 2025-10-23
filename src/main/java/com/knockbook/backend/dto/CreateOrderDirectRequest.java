package com.knockbook.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CreateOrderDirectRequest {

    @NotNull
    private String refType;

    @NotNull
    private String refId; // bookId or productId

    @Min(1)
    private Integer quantity;
}
