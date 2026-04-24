package com.mycompany.smartcampusapi.resources;

import com.mycompany.smartcampusapi.exception.LinkedResourceNotFoundException;
import com.mycompany.smartcampusapi.exception.ResourceNotFoundException;
import com.mycompany.smartcampusapi.model.Room;
import com.mycompany.smartcampusapi.model.Sensor;
import com.mycompany.smartcampusapi.service.DataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/** @author Yuki Ranathilaka */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {
    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(store.getSensors().values());
        if (type != null && !type.isBlank()) {
            String f = type.trim().toLowerCase();
            sensors = sensors.stream()
                .filter(s -> s.getType() != null && s.getType().toLowerCase().contains(f))
                .collect(Collectors.toList());
        }
        sensors.sort(Comparator.comparing(Sensor::getId));
        return Response.ok(sensors).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) throw new ResourceNotFoundException("Sensor '" + sensorId + "' was not found.");
        return Response.ok(sensor).build();
    }

    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor == null || sensor.getType() == null || sensor.getType().isBlank()) {
            throw new WebApplicationException(Response.status(400)
                .entity(err(400, "Bad Request", "Sensor 'type' is required.")).build());
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            throw new LinkedResourceNotFoundException("Sensor 'roomId' is required.");
        }
        Room room = store.getRoom(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                "The roomId '" + sensor.getRoomId() + "' does not reference an existing room.");
        }
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            sensor.setId(sensor.getType().toUpperCase().replaceAll("[^A-Z0-9]", "")
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        }
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }
        room.getSensorIds().add(sensor.getId());
        store.putSensor(sensor);
        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location).entity(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsLocator(@PathParam("sensorId") String sensorId) {
        if (store.getSensor(sensorId) == null)
            throw new ResourceNotFoundException("Sensor '" + sensorId + "' was not found.");
        return new SensorReadingResource(sensorId, store);
    }

    private Map<String, Object> err(int s, String e, String m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("status", s); map.put("error", e);
        map.put("message", m); map.put("timestamp", Instant.now().toString());
        return map;
    }
}
