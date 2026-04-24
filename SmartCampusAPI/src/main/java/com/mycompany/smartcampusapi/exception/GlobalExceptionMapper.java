package com.mycompany.smartcampusapi.exception;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mycompany.smartcampusapi.dto.ApiErrorResponse;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        LOGGER.log(Level.SEVERE, "Unhandled server error", exception);
        ApiErrorResponse entity = new ApiErrorResponse(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "An unexpected system error occurred.",
            Instant.now().toString()
        );
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(entity)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
