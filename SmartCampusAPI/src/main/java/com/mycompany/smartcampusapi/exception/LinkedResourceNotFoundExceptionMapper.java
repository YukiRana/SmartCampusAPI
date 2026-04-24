package com.mycompany.smartcampusapi.exception;

import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** @author Yuki Ranathilaka */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("status", 422); e.put("error", "Unprocessable Entity");
        e.put("message", ex.getMessage()); e.put("timestamp", Instant.now().toString());
        return Response.status(422).type(MediaType.APPLICATION_JSON).entity(e).build();
    }
}