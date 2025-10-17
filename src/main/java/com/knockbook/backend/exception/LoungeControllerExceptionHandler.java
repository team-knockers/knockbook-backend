package com.knockbook.backend.exception;

import com.knockbook.backend.controller.LoungePostController;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = LoungePostController.class)
public class LoungeControllerExceptionHandler {

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

    @ExceptionHandler(PostNotFoundException.class)
    public ProblemDetail postNotFound(PostNotFoundException ex) {
        return ProblemDetailFactory.of(
                HttpStatus.NOT_FOUND, "Post not found",
                ex.getMessage(), "POST_NOT_FOUND", "about:blank#post");
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ProblemDetail commentNotFound(CommentNotFoundException ex) {
        return ProblemDetailFactory.of(
                HttpStatus.NOT_FOUND, "Comment not found",
                ex.getMessage(), "COMMENT_NOT_FOUND", "about:blank#comment");
    }

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail dbUnavailable(DataAccessException ex) {
        return ProblemDetailFactory.of(
                HttpStatus.SERVICE_UNAVAILABLE, "Database unavailable",
                "일시적인 장애입니다. 잠시 후 다시 시도해주세요.",
                "DB_UNAVAILABLE", "about:blank#db");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        return ProblemDetailFactory.of(
                HttpStatus.CONFLICT,
                "Integrity violation",
                "요청을 처리할 수 없습니다. 이미 다른 주문에 사용되었거나 유효하지 않은 값입니다.",
                "INTEGRITY_VIOLATION",
                "about:blank#integrity");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail generic(Exception ex) {
        return ProblemDetailFactory.of(
                HttpStatus.INTERNAL_SERVER_ERROR, "Server error",
                "일시적인 오류입니다. 잠시 후 다시 시도해주세요.",
                "SERVER_ERROR", "about:blank#server");
    }
}
