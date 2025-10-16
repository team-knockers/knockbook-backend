package com.knockbook.backend.dto;

import com.knockbook.backend.domain.OrderPayment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ApprovePaymentRequest {
    @NotNull
    private OrderPayment.Method method;
    @NotBlank
    private String provider;
    @NotBlank
    private String txId;
    @NotNull
    private Integer amount;
}
