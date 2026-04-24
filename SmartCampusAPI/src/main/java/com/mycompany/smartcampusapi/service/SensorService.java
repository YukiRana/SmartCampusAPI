package com.mycompany.smartcampusapi.service;

import java.util.List;
import java.util.Locale;

import com.mycompany.smartcampusapi.dto.SensorRequest;
import com.mycompany.smartcampusapi.dto.SensorResponse;
import com.mycompany.smartcampusapi.exception.ResourceNotFoundException;
import com.mycompany.smartcampusapi.model.Room;
import com.mycompany.smartcampusapi.model.Sensor;
import com.mycompany.smartcampusapi.util.InputValidator;

import jakarta.persistence.EntityManager;

public class SensorService extends JpaSupport {

    public List<SensorResponse> listSensors(String type) {
        return execute(entityManager -> {
            String normalizedType = type == null ? null : type.trim();
            var query = entityManager.createQuery(
                    "select s from Sensor s join fetch s.room " +
                            "where (:type is null or lower(s.type) like concat('%', :type, '%')) " +
                            "order by s.id",
                    Sensor.class);

            if (normalizedType == null || normalizedType.isEmpty()) {
                query.setParameter("type", null);
            } else {
                query.setParameter("type", normalizedType.toLowerCase(Locale.ROOT));
            }

            return query.getResultList()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        });
    }

    public SensorResponse getSensor(long sensorId) {
        InputValidator.requirePositiveId(sensorId, "sensorId");
        return execute(entityManager -> toResponse(findSensorEntity(entityManager, sensorId)));
    }

    public SensorResponse createSensor(SensorRequest request) {
        InputValidator.requireNonNull(request, "request");
        String type = InputValidator.requireNonBlank(request.type(), "type", 80);
        long roomId = InputValidator.requirePositiveId(request.roomId(), "roomId");
        Double currentValue = request.currentValue();

        return execute(entityManager -> {
            Room room = entityManager.find(Room.class, roomId);
            if (room == null) {
                throw new ResourceNotFoundException("Room " + roomId + " was not found.");
            }

            Sensor sensor = new Sensor();
            sensor.setType(type);
            sensor.setRoom(room);
            sensor.setCurrentValue(currentValue);
            entityManager.persist(sensor);
            entityManager.flush();
            return toResponse(sensor);
        });
    }

    Sensor findSensorEntity(EntityManager entityManager, long sensorId) {
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

    private SensorResponse toResponse(Sensor sensor) {
        return new SensorResponse(
                sensor.getId(),
                sensor.getType(),
                sensor.getCurrentValue(),
                sensor.getRoom().getId(),
                sensor.getRoom().getName()
        );
    }
}