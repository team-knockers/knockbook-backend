package com.knockbook.backend.dto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ApprovePaymentResponse {
    private Long orderId;
    private Long paymentId;
    private String paymentStatus;
    private String orderStatus;
}
