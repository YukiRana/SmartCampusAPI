package com.mycompany.smartcampusapi.dto;

public record ReadingResponse(
        Long id,
        Double value,
        String recordedAt
) {
}