package com.mycompany.smartcampusapi.dto;

public record SensorResponse(
        Long id,
        String type,
        Double currentValue,
        Long roomId,
        String roomName
) {
}