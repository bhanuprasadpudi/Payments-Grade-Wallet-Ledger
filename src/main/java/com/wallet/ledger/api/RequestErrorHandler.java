package com.wallet.ledger.api;

import com.wallet.ledger.service.LedgerException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RequestErrorHandler {

    @ExceptionHandler(LedgerException.class)
    public ResponseEntity<ApiError> ledger(LedgerException exception, HttpServletRequest request) {
        HttpStatus status = switch (exception.getCode()) {
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "INSUFFICIENT_FUNDS", "ACCOUNT_NOT_FOUND", "USER_NOT_FOUND" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "IDEMPOTENCY_KEY_REUSED", "IDEMPOTENCY_IN_PROGRESS" -> HttpStatus.CONFLICT;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
        return ResponseEntity.status(status).body(error(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(error("INVALID_REQUEST", "Request validation failed"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception exception) {
        return ResponseEntity.internalServerError().body(error("INTERNAL_ERROR", "An internal error occurred"));
    }

    private ApiError error(String code, String message) {
        return new ApiError(code, message, UUID.randomUUID().toString());
    }
}
