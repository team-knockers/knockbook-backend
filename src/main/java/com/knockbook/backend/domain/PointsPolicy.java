package com.knockbook.backend.domain;

public final class PointsPolicy {
    public static int of(CartItem.RefType type){
        return switch (type) {
            case BOOK_RENTAL -> 3;
            case BOOK_PURCHASE, PRODUCT -> 5;
        };
    }
}
