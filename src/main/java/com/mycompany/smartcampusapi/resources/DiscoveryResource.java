package com.mycompany.smartcampusapi.resources;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Root discovery endpoint at GET /api/v1.
 * Returns API metadata, versioning, administrative contact, and HATEOAS links.
 *
 * HATEOAS (Hypermedia As The Engine Of Application State):
 * Embedding navigational links in API responses is a hallmark of mature REST
 * design because it lets clients discover all available resources dynamically
 * from a single well-known entry point, rather than relying on hard-coded URLs
 * or external static documentation. When the server changes a resource path,
 * clients that follow discovery links adapt automatically without a code change.
 * This reduces coupling between client and server and eliminates the risk of
 * stale documentation causing client-side breakage. Compared to static docs,
 * HATEOAS makes the API self-describing, self-navigable, and more resilient to
 * server-side evolution.
 *
 * @author Yuki Ranathilaka
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover(@Context UriInfo uriInfo) {
        String base = uriInfo.getBaseUri().toString();

        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("name",  "Yuki Ranathilaka");
        contact.put("email", "yuki.ranathilaka@smartcampus.westminster.ac.uk");
        contact.put("role",  "Lead Backend Architect");

        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms",          base + "rooms");
        resources.put("sensors",        base + "sensors");
        resources.put("sensorReadings", base + "sensors/{sensorId}/readings");

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self",    base);
        links.put("rooms",   base + "rooms");
        links.put("sensors", base + "sensors");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name",        "Smart Campus Sensor & Room Management API");
        response.put("version",     "v1");
        response.put("description", "RESTful JAX-RS API for managing campus rooms, sensors and readings.");
        response.put("timestamp",   System.currentTimeMillis());
        response.put("contact",     contact);
        response.put("resources",   resources);
        response.put("_links",      links);

        return Response.ok(response).build();
    }
}
