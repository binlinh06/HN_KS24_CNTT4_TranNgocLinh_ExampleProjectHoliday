package com.phobo.management.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private boolean success;
    private String message;
    private String errorCode;
    private List<String> errors;
    private String timestamp;

    public static ApiErrorResponse error(String message, String errorCode, List<String> errors) {
        return ApiErrorResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .errors(errors)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public static ApiErrorResponse error(String message, String errorCode) {
        return error(message, errorCode, List.of());
    }
}
