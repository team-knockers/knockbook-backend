package com.knockbook.backend.validation;

import jakarta.validation.Constraint;
import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CartItemRequestValidator.class)
public @interface ValidCartItemRequest {
    String message() default "Invalid cart item request";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};
}
