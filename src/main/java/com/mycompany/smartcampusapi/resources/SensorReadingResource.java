package com.mycompany.smartcampusapi.resources;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mycompany.smartcampusapi.model.ApiError;
import com.mycompany.smartcampusapi.model.Sensor;
import com.mycompany.smartcampusapi.model.SensorReading;
import com.mycompany.smartcampusapi.service.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Sub-resource for sensor reading history.
 * Served at /api/v1/sensors/{sensorId}/readings via SensorResource's locator.
 * Never registered directly in JakartaRestConfiguration.
 *
 * A successful POST performs a side-effect update on the parent Sensor's
 * currentValue field so that GET /sensors/{id} always reflects the most
 * recent recorded measurement, maintaining data consistency across the API.
 *
 * @author Yuki Ranathilaka
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings(@Context UriInfo uriInfo) {
        Sensor sensor = DataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found",
                            "Sensor '" + sensorId + "' was not found.",
                            uriInfo.getPath()))
                    .build();
        }
        List<SensorReading> readings = DataStore.getReadings(sensorId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sensorId",      sensorId);
        body.put("sensorType",    sensor.getType());
        body.put("sensorStatus",  sensor.getStatus());
        body.put("currentValue",  sensor.getCurrentValue());
        body.put("totalReadings", readings.size());
        body.put("readings",      readings);
        return Response.ok(body).build();
    }

    @POST
    public Response createReading(SensorReading reading,
                                  @Context UriInfo uriInfo) {
        Sensor sensor = DataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found",
                            "Sensor '" + sensorId + "' was not found.",
                            uriInfo.getPath()))
                    .build();
        }
        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError(400, "Bad Request",
                            "Request body must contain a SensorReading with a 'value'.",
                            uriInfo.getPath()))
                    .build();
        }
        // DataStore.addReading throws SensorUnavailableException (-> 403)
        // if sensor status is MAINTENANCE â€” caught by SensorUnavailableExceptionMapper
        SensorReading saved = DataStore.addReading(sensorId, reading);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sensorId",            sensorId);
        body.put("readingId",           saved.getId());
        body.put("value",               saved.getValue());
        body.put("timestamp",           saved.getTimestamp());
        body.put("updatedCurrentValue", sensor.getCurrentValue());
        body.put("message",
                "Reading recorded successfully. Sensor currentValue updated.");

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(saved.getId()).build();
        return Response.created(location).entity(body).build();
    }
}
