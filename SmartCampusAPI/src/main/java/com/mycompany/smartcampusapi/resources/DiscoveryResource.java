package com.mycompany.smartcampusapi.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** @author Yuki Ranathilaka */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {
    @GET
    public Response discover(@Context UriInfo uriInfo) {
        String base = uriInfo.getBaseUri().toString();
        Map<String, Object> contact = new LinkedHashMap<>();
        contact.put("name", "Yuki Ranathilaka");
        contact.put("email", "yuki.ranathilaka@smartcampus.ac.uk");
        contact.put("role", "Lead Backend Architect");

        Map<String, Object> resources = new LinkedHashMap<>();
        resources.put("rooms", base + "rooms");
        resources.put("sensors", base + "sensors");
        resources.put("sensorReadings", base + "sensors/{sensorId}/readings");

        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", base);
        links.put("rooms", base + "rooms");
        links.put("sensors", base + "sensors");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", "Smart Campus API");
        response.put("version", "v1");
        response.put("description", "RESTful API for Smart Campus sensor and room management");
        response.put("timestamp", Instant.now().toString());
        response.put("contact", contact);
        response.put("resources", resources);
        response.put("_links", links);
        return Response.ok(response).build();
    }
}