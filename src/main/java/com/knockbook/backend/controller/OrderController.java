package com.knockbook.backend.controller;

import com.knockbook.backend.dto.ApplyCouponRequest;
import com.knockbook.backend.dto.CreateOrderFromCartRequest;
import com.knockbook.backend.dto.OrderResponse;
import com.knockbook.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/draft-from-cart")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<OrderResponse> createDraftFromCart(
            @PathVariable final String userId,
            @Valid @RequestBody final CreateOrderFromCartRequest req) {
        final var userIdLong = Long.valueOf(userId);
        final var cartItemIds = req.getCartItemIds();
        final var domain = orderService.createDraftFromCart(userIdLong, cartItemIds);
        final var dto = OrderResponse.toResponse(domain);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable final String userId,
            @PathVariable final String orderId) {
        final var domain = orderService.getById(Long.valueOf(userId), Long.valueOf(orderId));
        final var dto = OrderResponse.toResponse(domain);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{orderId}/coupon")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<OrderResponse> applyCoupon(
            @PathVariable final String userId,
            @PathVariable final String orderId,
            @RequestBody final ApplyCouponRequest req) {

        final var domain = orderService.applyCoupon(
                Long.valueOf(userId), Long.valueOf(orderId),
                req.getCouponIssuanceId(), req.getCode());
        final var dto = OrderResponse.toResponse(domain);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{orderId}/coupon")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<OrderResponse> removeCoupon(
            @PathVariable final String userId,
            @PathVariable final String orderId) {

        final var domain = orderService.removeCoupon(
                Long.valueOf(userId), Long.valueOf(orderId));
        final var dto = OrderResponse.toResponse(domain);
        return ResponseEntity.ok(dto);
    }
}
