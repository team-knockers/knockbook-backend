package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Cart {

    public enum Status { OPEN, ORDERED, ABANDONED }

    private final Long id;
    private final Long userId;
    private final Status status;
    private final List<CartItem> items;

    private final int itemCount;
    private final int subtotalAmount;
    private final int discountAmount;
    private final int shippingAmount;
    private final int rentalAmount;
    private final int totalAmount;
    private final int pointsEarnable;
}
