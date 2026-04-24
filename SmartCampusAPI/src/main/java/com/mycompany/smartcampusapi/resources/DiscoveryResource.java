package com.mycompany.smartcampusapi.resources;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {
    @GET
    public Map<String, Object> discover(@Context UriInfo uriInfo, @QueryParam("trigger") String trigger) {
        if ("forbidden".equalsIgnoreCase(trigger)) {
            throw new ForbiddenException("Access to this diagnostic path is forbidden.");
        }
        if ("boom".equalsIgnoreCase(trigger)) {
            throw new IllegalStateException("Forced failure for coursework verification.");
        }

        Map<String, Object> api = new LinkedHashMap<>();
        api.put("name", "SmartCampusAPI");
        api.put("version", "v1");
        api.put("basePath", "/api/v1");
        api.put("timestamp", Instant.now().toString());

        Map<String, Object> contact = new LinkedHashMap<>();
        contact.put("name", "Yuki Ranathilaka");
        contact.put("email", "yuki@smartcampus.local");
        api.put("contact", contact);

        Map<String, Object> resources = new LinkedHashMap<>();
        resources.put("rooms", link(uriInfo.getBaseUriBuilder().path(RoomResource.class).build().toString(), "GET", "POST"));
        resources.put("sensors", link(uriInfo.getBaseUriBuilder().path(SensorResource.class).build().toString(), "GET", "POST"));
        resources.put("sensorReadings", link(uriInfo.getBaseUriBuilder().path(SensorResource.class).build().toString() + "/{id}/readings", "GET", "POST"));
        api.put("resources", resources);

        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", uriInfo.getAbsolutePath().toString());
        links.put("rooms", uriInfo.getBaseUriBuilder().path(RoomResource.class).build().toString());
        links.put("sensors", uriInfo.getBaseUriBuilder().path(SensorResource.class).build().toString());
        api.put("_links", links);
        return api;
    }

    private Map<String, Object> link(String href, String... methods) {
        Map<String, Object> link = new LinkedHashMap<>();
        link.put("href", href);
        link.put("methods", methods);
        return link;
    }
}