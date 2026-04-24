package com.mycompany.smartcampusapi.dto;

public record SensorRequest(
        String type,
        Long roomId,
        Double currentValue
) {
}