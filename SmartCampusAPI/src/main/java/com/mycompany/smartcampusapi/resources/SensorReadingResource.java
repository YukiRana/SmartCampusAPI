package com.mycompany.smartcampusapi.resources;

import com.mycompany.smartcampusapi.dto.ReadingRequest;
import com.mycompany.smartcampusapi.dto.SensorReadingsResponse;
import com.mycompany.smartcampusapi.service.ReadingService;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final long sensorId;
    private final ReadingService readingService;

    public SensorReadingResource(long sensorId, ReadingService readingService) {
        this.sensorId = sensorId;
        this.readingService = readingService;
    }

    @GET
    public SensorReadingsResponse getReadings() {
        return readingService.getReadings(sensorId);
    }

    @POST
    public Response addReading(ReadingRequest request) {
        SensorReadingsResponse updated = readingService.addReading(sensorId, request);
        return Response.status(Response.Status.CREATED).entity(updated).build();
    }
}
