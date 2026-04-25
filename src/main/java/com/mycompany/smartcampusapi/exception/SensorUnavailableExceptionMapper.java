package com.mycompany.smartcampusapi.exception;

import com.mycompany.smartcampusapi.model.ApiError;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps SensorUnavailableException to HTTP 403 Forbidden with a JSON body.
 * Triggered when a client POSTs a reading to a sensor in MAINTENANCE status.
 * @author Yuki Ranathilaka
 */
@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(SensorUnavailableException ex) {
        ApiError error = new ApiError(
                403,
                "Forbidden",
                ex.getMessage(),
                uriInfo != null ? uriInfo.getPath() : "unknown");
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
