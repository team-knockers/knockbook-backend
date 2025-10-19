package com.knockbook.backend.controller;

import com.knockbook.backend.dto.ApprovePaymentRequest;
import com.knockbook.backend.dto.ApprovePaymentResponse;
import com.knockbook.backend.service.OrderPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/orders/{orderId}/payments")
public class OrderPaymentController {

    private final OrderPaymentService orderPaymentService;

    @PostMapping("/approve")
    @PreAuthorize("#userId.toString() == authentication.name")
    public ResponseEntity<ApprovePaymentResponse> approve(
            @PathVariable Long userId,
            @PathVariable Long orderId,
            @Valid @RequestBody ApprovePaymentRequest req
    ) {
        final var method = req.getMethod();
        final var provider = req.getProvider();
        final var txId = req.getTxId();
        final var amount = req.getAmount();
        final var domain = orderPaymentService.approve(userId, orderId, method, provider, txId, amount);
        final var dto = ApprovePaymentResponse.builder()
                .orderId(domain.getOrderId())
                .paymentId(domain.getPayment().getId())
                .paymentStatus(domain.getOrder().getPaymentStatus().name())
                .orderStatus(domain.getOrder().getStatus().name())
                .build();

        return ResponseEntity.ok(dto);
    }
}
