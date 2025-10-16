package com.knockbook.backend.controller;

import com.knockbook.backend.service.KakaoPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class KakaoPayAuthController {

    private final KakaoPayService kakaoPayService;

    // payment preparation
    @PreAuthorize("#userId == authentication.name")
    @PostMapping("/users/{userId}/orders/{orderId}/payments/kakao/ready")
    public ResponseEntity<Map<String, Object>> ready(
            @PathVariable String userId,
            @PathVariable String orderId
    ) {
        final var info = kakaoPayService.ready(Long.valueOf(userId), Long.valueOf(orderId));
        return ResponseEntity.ok(Map.of(
                "tid", info.getTid(),
                "next_redirect_pc_url", info.getNextRedirectPcUrl(),
                "next_redirect_mobile_url", info.getNextRedirectMobileUrl(),
                "next_redirect_app_url", info.getNextRedirectAppUrl(),
                "amount", info.getAmount(),
                "orderId", info.getOrderId()
        ));
    }
}
