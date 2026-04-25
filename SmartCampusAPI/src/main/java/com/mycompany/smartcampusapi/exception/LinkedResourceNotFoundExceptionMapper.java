package com.mycompany.smartcampusapi.exception;

import com.mycompany.smartcampusapi.model.ApiError;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps LinkedResourceNotFoundException to HTTP 422 Unprocessable Entity.
 *
 * HTTP 422 is more semantically accurate than 404 in this scenario because
 * the request URI (/api/v1/sensors) is perfectly valid — the endpoint exists.
 * The problem is that a value inside the JSON payload (roomId) references a
 * resource that does not exist. HTTP 404 implies the endpoint itself is missing,
 * which would mislead clients. HTTP 422 precisely signals that the server
 * understood and parsed the request but cannot process it because a business
 * rule is violated: the referenced linked resource is absent.
 *
 * @author Yuki Ranathilaka
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        ApiError error = new ApiError(
                422,
                "Unprocessable Entity",
                ex.getMessage(),
                uriInfo != null ? uriInfo.getPath() : "unknown");
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}