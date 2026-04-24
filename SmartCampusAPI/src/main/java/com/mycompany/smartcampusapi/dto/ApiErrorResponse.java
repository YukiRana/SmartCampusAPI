package com.mycompany.smartcampusapi.dto;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String timestamp
) {
}
