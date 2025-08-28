package me.geohod.geohodbackend.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.api.response.ApiResponse;

/**
 * Global exception handler for consistent error responses across the API.
 * Provides structured error responses and proper logging for debugging.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletRequest request) {

        log.warn("Invalid request parameter at {}: {}", request.getRequestURI(), e.getMessage());

        return ResponseEntity.badRequest()
                .body(new ApiResponse<>("ERROR", "Invalid request: " + e.getMessage(), null));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalStateException(
            IllegalStateException e, HttpServletRequest request) {

        log.warn("Invalid state for operation at {}: {}", request.getRequestURI(), e.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>("ERROR", "Operation not allowed: " + e.getMessage(), null));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleDataIntegrityViolation(
            DataIntegrityViolationException e, HttpServletRequest request) {

        log.error("Data integrity violation at {}: {}", request.getRequestURI(), e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>("ERROR", "Data conflict occurred. Please try again.", null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {

        log.warn("Access denied at {}: {}", request.getRequestURI(), e.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>("ERROR", "Access denied", null));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<String>> handleAuthenticationException(
            AuthenticationException e, HttpServletRequest request) {

        log.warn("Authentication failed at {}: {}", request.getRequestURI(), e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>("ERROR", "Authentication required", null));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<String>> handleSecurityException(
            SecurityException e, HttpServletRequest request) {

        log.error("Security violation at {}: {}", request.getRequestURI(), e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>("ERROR", "Authentication failed", null));
    }

    @ExceptionHandler(TelegramNotificationException.class)
    public ResponseEntity<ApiResponse<String>> handleTelegramNotificationException(
            TelegramNotificationException e, HttpServletRequest request) {

        log.error("Telegram notification failed at {}: {}", request.getRequestURI(), e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("ERROR", "Notification service temporarily unavailable", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(
            Exception e, HttpServletRequest request) {

        log.error("Unexpected error at {}: {}", request.getRequestURI(), e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("ERROR", "An unexpected error occurred. Please try again later.", null));
    }
}