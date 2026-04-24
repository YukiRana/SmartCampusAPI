package com.mycompany.smartcampusapi.dto;

public record RoomResponse(
        Long id,
        String name,
        long sensorCount
) {
}