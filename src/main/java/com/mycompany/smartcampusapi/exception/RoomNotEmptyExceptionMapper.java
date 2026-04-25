package com.mycompany.smartcampusapi.exception;

import com.mycompany.smartcampusapi.model.ApiError;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps RoomNotEmptyException to HTTP 409 Conflict with a structured JSON body.
 * @author Yuki Ranathilaka
 */
@Provider
public class RoomNotEmptyExceptionMapper
        implements ExceptionMapper<RoomNotEmptyException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        ApiError error = new ApiError(
                409,
                "Conflict",
                ex.getMessage(),
                uriInfo != null ? uriInfo.getPath() : "unknown");
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
