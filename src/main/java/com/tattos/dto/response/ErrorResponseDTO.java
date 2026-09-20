package com.tattos.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseDTO(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    Map<String, String> errors
) {
    public static ErrorResponseDTO of(int status, String error, String message, String path) {
        return new ErrorResponseDTO(LocalDateTime.now(), status, error, message, path, null);
    }

    public static ErrorResponseDTO of(int status, String error, String message, String path, Map<String, String> errors) {
        return new ErrorResponseDTO(LocalDateTime.now(), status, error, message, path, errors);
    }
}
