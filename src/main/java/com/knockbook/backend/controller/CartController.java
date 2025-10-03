package com.knockbook.backend.controller;

import com.knockbook.backend.domain.CartItem;
import com.knockbook.backend.dto.AddCartItemRequest;
import com.knockbook.backend.dto.CartDto;
import com.knockbook.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Add an item to the user's cart (supports book purchase, book rental, and product).
     * The service will create an OPEN cart if one does not exist, and the repository
     * will upsert (merge quantity) and recalculate totals internally.
     *
     * Security:
     *  - Only the owner (authenticated principal) can modify their own cart.
     */
    @PreAuthorize("#userId == authentication.name")
    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(
            @PathVariable String userId,
            @Valid @RequestBody AddCartItemRequest req) {

        final var userIdLong = Long.valueOf(userId);
        final var refType = CartItem.RefType.valueOf(req.getRefType());
        final var refId = Long.valueOf(req.getRefId());
        final var rentalDays = req.getRentalDays();
        final var qty = req.getQuantity();

        final var cart = switch (refType) {
            case BOOK_PURCHASE -> cartService.addBookPurchase(userIdLong, refId, qty);
            case BOOK_RENTAL -> cartService.addBookRental(userIdLong, refId,rentalDays, qty);
            case PRODUCT -> cartService.addProduct(userIdLong, refId, qty);
        };

        final var dto = CartDto.fromModel(cart);
        return ResponseEntity.ok().body(dto);
    }

    /**
     * Remove an item from the user's cart by cartItemId.
     * If the item does not belong to the cart or does not exist, returns 404.
     *
     * Security:
     *  - Only the owner (authenticated principal) can modify their own cart.
     */
    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartDto> deleteItem(
            @PathVariable String userId,
            @PathVariable String cartItemId) {
        try {
            final var userIdLong = Long.valueOf(userId);
            final var cartItemIdLong = Long.valueOf(cartItemId);
            final var cart = cartService.removeItem(userIdLong, cartItemIdLong);
            final var dto = CartDto.fromModel(cart);
            return ResponseEntity.ok().body(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Fetch the current (OPEN) cart for the user.
     * If none exists, the service will create an empty cart and return it.
     *
     * Security:
     *  - Only the owner (authenticated principal) can view their own cart.
     */
    @PreAuthorize("#userId == authentication.name")
    @GetMapping
    public ResponseEntity<CartDto> getCart(@PathVariable Long userId) {
        final var cart = cartService.getCart(userId);
        final var dto = CartDto.fromModel(cart);
        return ResponseEntity.ok().body(dto);
    }
}


