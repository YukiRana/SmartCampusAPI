package com.mycompany.smartcampusapi.exception;

import java.time.Instant;

import com.mycompany.smartcampusapi.dto.ApiErrorResponse;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {

    @Override
    public Response toResponse(ForbiddenException exception) {
        ApiErrorResponse error = new ApiErrorResponse(
                Response.Status.FORBIDDEN.getStatusCode(),
                Response.Status.FORBIDDEN.getReasonPhrase(),
                exception.getMessage() != null ? exception.getMessage() : "Access is forbidden.",
                Instant.now().toString()
        );
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}