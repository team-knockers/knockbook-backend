package com.knockbook.backend.controller;

import com.knockbook.backend.dto.ApprovePaymentResponse;
import com.knockbook.backend.service.KakaoPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;

    // payment preparation
    @PostMapping("/users/{userId}/orders/{orderId}/payments/kakao/ready")
    public ResponseEntity<Map<String, Object>> ready(
            @PathVariable Long userId,
            @PathVariable Long orderId
    ) {
        final var info = kakaoPayService.ready(userId, orderId);
        return ResponseEntity.ok(Map.of(
                "tid", info.getTid(),
                "next_redirect_pc_url", info.getNextRedirectPcUrl(),
                "next_redirect_mobile_url", info.getNextRedirectMobileUrl(),
                "next_redirect_app_url", info.getNextRedirectAppUrl(),
                "amount", info.getAmount(),
                "orderId", info.getOrderId()
        ));
    }

    // payment approval
    @PostMapping("/payments/kakao/approve")
    public ResponseEntity<ApprovePaymentResponse> approve(@RequestBody Map<String, Object> req) {
        final var userId  = Long.valueOf(String.valueOf(req.get("userId")));
        final var orderId = Long.valueOf(String.valueOf(req.get("orderId")));
        final var pgToken = String.valueOf(req.get("pg_token"));

        final var result = kakaoPayService.approve(userId, orderId, pgToken);
        final var dto = ApprovePaymentResponse.builder()
                .orderId(result.getOrderId())
                .paymentStatus(result.getOrder().getPaymentStatus().name())
                .orderStatus(result.getOrder().getStatus().name())
                .build();

        return ResponseEntity.ok(dto);
    }
}
