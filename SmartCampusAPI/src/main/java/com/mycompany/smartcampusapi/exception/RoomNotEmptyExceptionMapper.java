package com.mycompany.smartcampusapi.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** @author Yuki Ranathilaka */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("status", 409); e.put("error", "Conflict");
        e.put("message", ex.getMessage()); e.put("timestamp", Instant.now().toString());
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON).entity(e).build();
    }
}