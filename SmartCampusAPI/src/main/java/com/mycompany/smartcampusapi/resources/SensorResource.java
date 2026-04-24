package com.mycompany.smartcampusapi.resources;

import java.util.List;
import java.net.URI;

import com.mycompany.smartcampusapi.dto.SensorRequest;
import com.mycompany.smartcampusapi.dto.SensorResponse;
import com.mycompany.smartcampusapi.service.ReadingService;
import com.mycompany.smartcampusapi.service.SensorService;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final SensorService sensorService = new SensorService();
    private final ReadingService readingService = new ReadingService();
    
    @GET
    public List<SensorResponse> getSensors(@QueryParam("type") String type) {
        return sensorService.listSensors(type);
    }

    @GET
    @Path("/{id}")
    public SensorResponse getSensor(@PathParam("id") long id) {
        return sensorService.getSensor(id);
    }
    
    @POST
    public Response createSensor(SensorRequest request, @Context UriInfo uriInfo) {
        SensorResponse created = sensorService.createSensor(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return Response.created(location).entity(created).build();
    }
    
    @Path("{id}/readings")
    public SensorReadingResource getReadingsLocator(@PathParam("id") long sensorId) {
        return new SensorReadingResource(sensorId, readingService);
    }
}
