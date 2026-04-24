package com.mycompany.smartcampusapi.resources;

import com.mycompany.smartcampusapi.exception.SensorUnavailableException;
import com.mycompany.smartcampusapi.model.Sensor;
import com.mycompany.smartcampusapi.model.SensorReading;
import com.mycompany.smartcampusapi.service.DataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

/** @author Yuki Ranathilaka */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {
    private final String sensorId;
    private final DataStore store;

    public SensorReadingResource(String sensorId, DataStore store) {
        this.sensorId = sensorId;
        this.store = store;
    }

    @GET
    public Response getReadings() {
        List<SensorReading> readings = new ArrayList<>(store.getReadings(sensorId));
        readings.sort(Comparator.comparingLong(SensorReading::getTimestamp));
        Sensor sensor = store.getSensor(sensorId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sensorId", sensorId);
        if (sensor != null) {
            response.put("sensorType", sensor.getType());
            response.put("sensorStatus", sensor.getStatus());
            response.put("currentValue", sensor.getCurrentValue());
        }
        response.put("totalReadings", readings.size());
        response.put("readings", readings);
        return Response.ok(response).build();
    }

    @POST
    public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
        Sensor sensor = store.getSensor(sensorId);
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor '" + sensorId
                + "' is in MAINTENANCE status and cannot accept new readings.");
        }
        if (reading == null) reading = new SensorReading();
        reading.setId(UUID.randomUUID().toString());
        reading.setTimestamp(System.currentTimeMillis());
        store.addReading(sensorId, reading);
        // Side effect: update parent sensor currentValue for data consistency
        sensor.setCurrentValue(reading.getValue());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sensorId", sensorId);
        response.put("readingId", reading.getId());
        response.put("value", reading.getValue());
        response.put("timestamp", reading.getTimestamp());
        response.put("currentValue", sensor.getCurrentValue());
        response.put("message", "Reading recorded. Sensor currentValue updated.");
        return Response.status(Response.Status.CREATED).entity(response).build();
    }
}
