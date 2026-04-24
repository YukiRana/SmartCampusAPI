package com.mycompany.smartcampusapi.service;

import java.util.List;

import com.mycompany.smartcampusapi.dto.ReadingRequest;
import com.mycompany.smartcampusapi.dto.ReadingResponse;
import com.mycompany.smartcampusapi.dto.SensorReadingsResponse;
import com.mycompany.smartcampusapi.exception.ResourceNotFoundException;
import com.mycompany.smartcampusapi.model.Reading;
import com.mycompany.smartcampusapi.model.Sensor;
import com.mycompany.smartcampusapi.util.InputValidator;

import jakarta.persistence.EntityManager;

public class ReadingService extends JpaSupport {

    public SensorReadingsResponse getReadings(long sensorId) {
        InputValidator.requirePositiveId(sensorId, "sensorId");
        return execute(entityManager -> snapshot(entityManager, sensorId));
    }

    public SensorReadingsResponse addReading(long sensorId, ReadingRequest request) {
        InputValidator.requirePositiveId(sensorId, "sensorId");
        InputValidator.requireNonNull(request, "request");
        Double value = request.value();
        InputValidator.requireNonNull(value, "value");

        return execute(entityManager -> {
            Sensor sensor = findSensorEntity(entityManager, sensorId);

            Reading reading = new Reading();
            reading.setValue(value);
            sensor.addReading(reading);
            sensor.setCurrentValue(value);
            entityManager.persist(reading);
            entityManager.flush();

            return snapshot(entityManager, sensorId);
        });
    }

    private SensorReadingsResponse snapshot(EntityManager entityManager, long sensorId) {
        Sensor sensor = findSensorEntity(entityManager, sensorId);
        List<ReadingResponse> readings = entityManager.createQuery(
                        "select r from Reading r where r.sensor.id = :sensorId order by r.recordedAt asc, r.id asc",
                        Reading.class)
                .setParameter("sensorId", sensorId)
                .getResultList()
                .stream()
                .map(this::toResponse)
                .toList();

        return new SensorReadingsResponse(
                sensor.getId(),
                sensor.getType(),
                sensor.getCurrentValue(),
                sensor.getRoom().getId(),
                sensor.getRoom().getName(),
                readings
        );
    }

    private Sensor findSensorEntity(EntityManager entityManager, long sensorId) {
        Sensor sensor = entityManager.createQuery(
                        "select s from Sensor s join fetch s.room where s.id = :sensorId",
                        Sensor.class)
                .setParameter("sensorId", sensorId)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor " + sensorId + " was not found.");
        }
        return sensor;
    }

    private ReadingResponse toResponse(Reading reading) {
        return new ReadingResponse(
                reading.getId(),
                reading.getValue(),
                reading.getRecordedAt() != null ? reading.getRecordedAt().toString() : null
        );
    }
}