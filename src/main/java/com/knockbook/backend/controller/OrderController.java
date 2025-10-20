package com.knockbook.backend.controller;

import com.knockbook.backend.dto.*;
import com.knockbook.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/paid")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<List<OrderResponse>> listPaidOrders(
            @PathVariable final String userId) {

        final var domains = orderService.listPaidByUser(Long.valueOf(userId));
        final var dtoList = domains.stream()
                .map(OrderResponse::toResponse)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/{orderId}/coupon")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<OrderResponse> applyCoupon(
            @PathVariable final String userId,
            @PathVariable final String orderId,
            @RequestBody final ApplyCouponRequest req) {

        final var domain = orderService.applyCoupon(
                Long.valueOf(userId),
                Long.valueOf(orderId),
                req.getCouponIssuanceId());
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

    @PostMapping("/{orderId}/points")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<OrderResponse> applyPoints(
            @PathVariable final String userId,
            @PathVariable final String orderId,
            @RequestBody final ApplyPointsRequest req) {

        final var domain = orderService.applyPoints(
                Long.valueOf(userId), Long.valueOf(orderId), req.getPoints());
        final var dto = OrderResponse.toResponse(domain);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{orderId}/points")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<OrderResponse> removePoints(
            @PathVariable final String userId,
            @PathVariable final String orderId) {

        final var domain = orderService.removePoints(Long.valueOf(userId), Long.valueOf(orderId));
        final var dto = OrderResponse.toResponse(domain);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{orderId}/address")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<OrderResponse> applyAddress(
            @PathVariable final String userId,
            @PathVariable final String orderId,
            @RequestBody final ApplyAddressRequest req) {

        final var domain = orderService.applyAddress(
                Long.valueOf(userId),
                Long.valueOf(orderId),
                Long.valueOf(req.getAddressId())
        );
        final var dto = OrderResponse.toResponse(domain);
        return ResponseEntity.ok(dto);
    }
}
