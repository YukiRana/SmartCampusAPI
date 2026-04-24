package com.mycompany.smartcampusapi.dto;

import java.util.List;

public record SensorReadingsResponse(
        Long sensorId,
        String sensorType,
        Double currentValue,
        Long roomId,
        String roomName,
        List<ReadingResponse> readings
) {
}