package com.mycompany.smartcampusapi.resources;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.mycompany.smartcampusapi.model.ApiError;
import com.mycompany.smartcampusapi.model.Sensor;
import com.mycompany.smartcampusapi.service.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Sensor resource at /api/v1/sensors.
 *
 * @Consumes(APPLICATION_JSON) consequences:
 * If a client sends a POST with Content-Type: text/plain or application/xml,
 * JAX-RS automatically returns HTTP 415 Unsupported Media Type before this
 * method is ever invoked. No MessageBodyReader exists for those types in this
 * configuration, so the framework rejects the request at the content-negotiation
 * layer. This protects the resource method from receiving malformed input.
 *
 * @QueryParam vs path segment for filtering:
 * GET /sensors?type=CO2 uses a query parameter, which is semantically correct
 * because it represents an optional constraint on the collection, not a resource
 * identity. A path-segment approach (/sensors/type/CO2) incorrectly implies that
 * "CO2" is a uniquely addressable sub-resource with its own identity in the URI
 * hierarchy. Query parameters also compose cleanly without new routes:
 * ?type=CO2&status=ACTIVE requires no extra path definitions, whereas
 * path segments would need a new route per combination.
 *
 * Sub-Resource Locator pattern benefits:
 * The getReadingsLocator method has no HTTP method annotation â€” JAX-RS recognises
 * it as a locator and delegates /sensors/{id}/readings to SensorReadingResource.
 * This keeps reading-history logic isolated in its own class, reducing the size
 * and complexity of this controller. Adding new reading endpoints (e.g. /summary)
 * only requires changes to SensorReadingResource, not this file.
 *
 * @author Yuki Ranathilaka
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = DataStore.getSensorsByType(type);
        sensors.sort((a, b) -> a.getId().compareTo(b.getId()));
        return Response.ok(sensors).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId,
                                  @Context UriInfo uriInfo) {
        Sensor sensor = DataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found",
                            "Sensor '" + sensorId + "' was not found.",
                            uriInfo.getPath()))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor == null
                || isBlank(sensor.getType())
                || isBlank(sensor.getRoomId())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError(400, "Bad Request",
                            "Sensor must include 'type' and 'roomId'.",
                            uriInfo.getPath()))
                    .build();
        }
        if (isBlank(sensor.getId())) {
            sensor.setId(sensor.getType().toUpperCase()
                    .replaceAll("[^A-Z0-9]", "")
                    + "-" + UUID.randomUUID().toString()
                    .substring(0, 6).toUpperCase());
        }
        if (isBlank(sensor.getStatus())) {
            sensor.setStatus("ACTIVE");
        }
        Sensor clean = new Sensor(
                sensor.getId().trim(),
                sensor.getType().trim(),
                sensor.getStatus().trim().toUpperCase(),
                sensor.getCurrentValue(),
                sensor.getRoomId().trim());

        // DataStore.addSensor throws LinkedResourceNotFoundException (-> 422)
        // if roomId does not exist â€” caught by LinkedResourceNotFoundExceptionMapper
        boolean created = DataStore.addSensor(clean);
        if (!created) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ApiError(409, "Conflict",
                            "A sensor with id '" + clean.getId() + "' already exists.",
                            uriInfo.getPath()))
                    .build();
        }
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(clean.getId()).build();
        return Response.created(location).entity(clean).build();
    }

    /**
     * Sub-resource locator â€” no HTTP method annotation.
     * Delegates all /sensors/{sensorId}/readings requests to SensorReadingResource.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsLocator(
            @PathParam("sensorId") String sensorId,
            @Context UriInfo uriInfo) {
        if (DataStore.getSensor(sensorId) == null) {
            throw new javax.ws.rs.NotFoundException(
                    "Sensor '" + sensorId + "' was not found.");
        }
        return new SensorReadingResource(sensorId);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
