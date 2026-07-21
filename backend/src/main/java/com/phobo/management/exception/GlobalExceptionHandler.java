package com.phobo.management.exception;

import com.phobo.management.common.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiErrorResponse> handleAppException(AppException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiErrorResponse.error(ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(CartException.class)
    public ResponseEntity<ApiErrorResponse> handleCartException(CartException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiErrorResponse.error(ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(AddressException.class)
    public ResponseEntity<ApiErrorResponse> handleAddressException(AddressException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiErrorResponse.error(ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderException(OrderException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiErrorResponse.error(ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentException(PaymentException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiErrorResponse.error(ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(ReviewException.class)
    public ResponseEntity<ApiErrorResponse> handleReviewException(ReviewException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiErrorResponse.error(ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.error(ex.getMessage(), "RESOURCE_NOT_FOUND"));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.error(ex.getMessage(), "BAD_REQUEST"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.error("Bạn không có quyền truy cập chức năng này", "FORBIDDEN"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthError(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.error(ex.getMessage(), "UNAUTHORIZED"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationError(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.error("Dữ liệu đầu vào không hợp lệ", "VALIDATION_ERROR", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.error("Có lỗi xảy ra từ máy chủ: " + ex.getMessage(), "INTERNAL_SERVER_ERROR"));
    }
}
