package com.knockbook.backend.validation;

import com.knockbook.backend.domain.CartItem;
import com.knockbook.backend.dto.AddCartItemRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CartItemRequestValidator implements ConstraintValidator<ValidCartItemRequest, AddCartItemRequest> {
    @Override
    public boolean isValid(AddCartItemRequest v, ConstraintValidatorContext ctx) {
        if (v == null) {
            return true;
        }

        if (v.getRefType() == CartItem.RefType.BOOK_RENTAL) {
            final var rentalDays = v.getRentalDays();
            if (rentalDays == null || rentalDays < 1) {
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate("rentalDays must be >= 1 when refType is BOOK_RENTAL")
                        .addPropertyNode("rentalDays").addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}

