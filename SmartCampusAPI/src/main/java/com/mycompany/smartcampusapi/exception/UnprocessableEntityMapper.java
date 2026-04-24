package com.mycompany.smartcampusapi.exception;

import java.time.Instant;

import com.mycompany.smartcampusapi.dto.ApiErrorResponse;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UnprocessableEntityMapper implements ExceptionMapper<ValidationException> {

    @Override
    public Response toResponse(ValidationException exception) {
        ApiErrorResponse error = new ApiErrorResponse(
                422,
                "Unprocessable Entity",
                exception.getMessage(),
                Instant.now().toString()
        );
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}