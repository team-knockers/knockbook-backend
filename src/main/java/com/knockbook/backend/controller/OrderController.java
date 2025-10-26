package com.knockbook.backend.controller;

import com.knockbook.backend.domain.OrderDirectRefType;
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
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/{userId}/draft-from-cart")
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

    @PostMapping("/{userId}/draft")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<OrderResponse> createDraftDirect(
            @PathVariable final String userId,
            @Valid @RequestBody final CreateOrderDirectRequest req) {

        final var agg = orderService.createDraftDirect(
                Long.valueOf(userId),
                OrderDirectRefType.valueOf(req.getRefType()),
                Long.valueOf(req.getRefId()),
                req.getQuantity());
        final var dto = OrderResponse.toResponse(agg);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{userId}/{orderId}")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable final String userId,
            @PathVariable final String orderId) {
        final var domain = orderService.getById(Long.valueOf(userId), Long.valueOf(orderId));
        final var dto = OrderResponse.toResponse(domain);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<List<OrderResponse>> listOrders(
            @PathVariable final String userId,
            @RequestParam(required = false) String paymentStatus)
    {
        final var domains = orderService.getOrdersByUser(Long.valueOf(userId), paymentStatus);
        final var dtoList = domains.stream()
                .map(OrderResponse::toResponse)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MODERATOR')")
    public ResponseEntity<List<OrderResponse>> listOrders(
            @RequestParam(required = false) String paymentStatus)
    {
        final var domains = orderService.getAllOrders(paymentStatus);
        final var dtoList = domains.stream()
                .map(OrderResponse::toResponse)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/{userId}/{orderId}/coupon")
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

    @DeleteMapping("/{userId}/{orderId}/coupon")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<OrderResponse> removeCoupon(
            @PathVariable final String userId,
            @PathVariable final String orderId) {

        final var domain = orderService.removeCoupon(
                Long.valueOf(userId), Long.valueOf(orderId));
        final var dto = OrderResponse.toResponse(domain);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{userId}/{orderId}/points")
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

    @DeleteMapping("/{userId}/{orderId}/points")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<OrderResponse> removePoints(
            @PathVariable final String userId,
            @PathVariable final String orderId) {

        final var domain = orderService.removePoints(Long.valueOf(userId), Long.valueOf(orderId));
        final var dto = OrderResponse.toResponse(domain);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{userId}/{orderId}/address")
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

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MODERATOR')")
    @PatchMapping("/{userId}/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatuses(
            @PathVariable final String userId,
            @PathVariable final String orderId,
            @RequestParam(required = false) final String status,
            @RequestParam(required = false) final String rentalStatus) {

        final var agg = orderService.updateStatuses(
                Long.valueOf(userId), Long.valueOf(orderId), status, rentalStatus);
        return ResponseEntity.ok(OrderResponse.toResponse(agg));
    }
}
