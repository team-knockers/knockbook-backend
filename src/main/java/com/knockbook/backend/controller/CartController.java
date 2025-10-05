package com.knockbook.backend.controller;

import com.knockbook.backend.dto.AddCartItemRequest;
import com.knockbook.backend.dto.CartDto;
import com.knockbook.backend.service.CartService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PreAuthorize("#userId == authentication.name")
    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(
            @PathVariable String userId,
            @Validated @RequestBody AddCartItemRequest req) {

        final var userIdLong = Long.valueOf(userId);
        final var refType = req.getRefType();
        final var refId = Long.valueOf(req.getRefId());
        final var qty = req.getQuantity();

        final var cart = switch (refType) {
            case BOOK_PURCHASE ->
                    cartService.addBookPurchase(userIdLong, refId, qty);
            case BOOK_RENTAL ->
                    cartService.addBookRental(userIdLong, refId, req.getRentalDays(), qty);
            case PRODUCT ->
                    cartService.addProduct(userIdLong, refId, qty);
        };

        final var dto = CartDto.fromModel(cart);
        return ResponseEntity.ok().body(dto);
    }

//    /items/{cartItemId}?all=true
    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping(path = "/items/{cartItemId}", params = "all=true")
    public ResponseEntity<CartDto> deleteItem(
            @PathVariable String userId,
            @PathVariable String cartItemId,
            @RequestParam boolean all) {

        final var userIdLong = Long.valueOf(userId);
        final var cartItemIdLong = Long.valueOf(cartItemId);
        final var cart = cartService.removeAllOfThem(userIdLong, cartItemIdLong);
        final var dto = CartDto.fromModel(cart);
        return ResponseEntity.ok().body(dto);
    }

//    /items/{cartItemId}?qty={qty} (qty >= 1)
    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping(path = "/items/{cartItemId}", params = "qty")
    public ResponseEntity<CartDto> decrementItem(
            @PathVariable String userId,
            @PathVariable String cartItemId,
            @RequestParam @Min(1) int qty) {

        final var userIdLong = Long.valueOf(userId);
        final var cartItemIdLong = Long.valueOf(cartItemId);
        final var cart = cartService.decrementItem(userIdLong, cartItemIdLong, qty);
        final var dto = CartDto.fromModel(cart);
        return ResponseEntity.ok().body(dto);
    }

    @PreAuthorize("#userId == authentication.name")
    @GetMapping
    public ResponseEntity<CartDto> getCart(@PathVariable String userId) {
        final var cart = cartService.getCart(Long.valueOf(userId));
        final var dto = CartDto.fromModel(cart);
        return ResponseEntity.ok().body(dto);
    }
}


