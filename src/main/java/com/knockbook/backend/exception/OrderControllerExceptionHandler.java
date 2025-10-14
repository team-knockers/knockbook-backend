package com.knockbook.backend.exception;

import com.knockbook.backend.controller.OrderController;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes = OrderController.class)
public class OrderControllerExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail unauthorized(BadCredentialsException ex) {
        return ProblemDetailFactory.of(
                HttpStatus.UNAUTHORIZED, "Invalid credentials",
                "이메일 또는 비밀번호가 올바르지 않습니다.",
                "AUTH_INVALID", "about:blank#auth");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail forbidden(AccessDeniedException ex) {
        return ProblemDetailFactory.of(
                HttpStatus.FORBIDDEN, "Forbidden",
                "접근 권한이 없습니다.",
                "AUTH_FORBIDDEN", "about:blank#auth");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException ex) {
        final var msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .orElse("요청이 유효하지 않습니다.");
        return ProblemDetailFactory.of(
                HttpStatus.BAD_REQUEST, "Validation failed",
                msg, "VALIDATION_ERROR", "about:blank#validation");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail illegalArg(IllegalArgumentException ex) {
        return ProblemDetailFactory.of(
                HttpStatus.BAD_REQUEST, "Validation failed",
                ex.getMessage(), "VALIDATION_ERROR", "about:blank#validation");
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail orderNotFound(OrderNotFoundException ex) {
        return ProblemDetailFactory.of(
                HttpStatus.NOT_FOUND, "Order not found",
                ex.getMessage(), "ORDER_NOT_FOUND", "about:blank#order");
    }

    @ExceptionHandler(InvalidCartItemsException.class)
    public ProblemDetail invalidCart(InvalidCartItemsException ex) {
        return ProblemDetailFactory.of(
                HttpStatus.BAD_REQUEST, "Invalid cart items",
                "선택한 장바구니 항목이 유효하지 않습니다.",
                "CART_INVALID", "about:blank#cart");
    }

    @ExceptionHandler(CouponIssuanceNotFoundException.class)
    public ProblemDetail couponIssuanceNotFound(CouponIssuanceNotFoundException ex) {
        return ProblemDetailFactory.of(
                HttpStatus.NOT_FOUND, "Coupon issuance not found",
                ex.getMessage(), "COUPON_ISSUANCE_NOT_FOUND", "about:blank#coupon");
    }

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail dbUnavailable(DataAccessException ex) {
        return ProblemDetailFactory.of(
                HttpStatus.SERVICE_UNAVAILABLE, "Database unavailable",
                "일시적인 장애입니다. 잠시 후 다시 시도해주세요.",
                "DB_UNAVAILABLE", "about:blank#db");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail generic(Exception ex) {
        return ProblemDetailFactory.of(
                HttpStatus.INTERNAL_SERVER_ERROR, "Server error",
                "일시적인 오류입니다. 잠시 후 다시 시도해주세요.",
                "SERVER_ERROR", "about:blank#server");
    }
}
