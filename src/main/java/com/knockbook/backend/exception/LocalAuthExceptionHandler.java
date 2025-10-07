package com.knockbook.backend.exception;

import com.knockbook.backend.controller.LocalAuthController;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = LocalAuthController.class)
public class LocalAuthExceptionHandler {

    @ExceptionHandler({IdentityNotFoundException.class, BadCredentialsException.class})
    public ProblemDetail handleInvalidAuth(Exception ex) {
        return ProblemDetailFactory.of(
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials",
                "이메일 또는 비밀번호가 올바르지 않습니다.",
                "AUTH_INVALID",
                "about:blank#auth");
    }

    @ExceptionHandler({CredentialNotFoundException.class, UserNotFoundException.class})
    public ProblemDetail handleDataInconsistency(RuntimeException ex) {
        return ProblemDetailFactory.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Server error",
                "요청 처리 중 오류가 발생했습니다.",
                "SERVER_ERROR",
                "about:blank#server");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .orElse("요청이 유효하지 않습니다.");
        return ProblemDetailFactory.of(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                msg,
                "VALIDATION_ERROR",
                "about:blank#validation");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArg(IllegalArgumentException ex) {
        return ProblemDetailFactory.of(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                ex.getMessage(),
                "VALIDATION_ERROR",
                "about:blank#validation");
    }

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDatabase(DataAccessException ex) {
        return ProblemDetailFactory.of(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Database unavailable",
                "일시적인 장애입니다. 잠시 후 다시 시도해주세요.",
                "DB_UNAVAILABLE",
                "about:blank#db");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        return ProblemDetailFactory.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Server error",
                "일시적인 오류입니다. 잠시 후 다시 시도해주세요.",
                "SERVER_ERROR",
                "about:blank#server");
    }
}

