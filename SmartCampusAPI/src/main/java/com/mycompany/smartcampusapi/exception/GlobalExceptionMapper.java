package com.mycompany.smartcampusapi.exception;

import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.*;

/** @author Yuki Ranathilaka */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        LOGGER.log(Level.SEVERE, "Unhandled server error", ex);
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("status", 500); e.put("error", "Internal Server Error");
        e.put("message", "An unexpected error occurred. Please contact the system administrator.");
        e.put("timestamp", Instant.now().toString());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON).entity(e).build();
    }
}
