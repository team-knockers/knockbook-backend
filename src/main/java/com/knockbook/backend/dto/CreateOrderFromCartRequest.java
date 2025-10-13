package com.knockbook.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CreateOrderFromCartRequest {
    @NotEmpty
    private List<String> cartItemIds;
}
