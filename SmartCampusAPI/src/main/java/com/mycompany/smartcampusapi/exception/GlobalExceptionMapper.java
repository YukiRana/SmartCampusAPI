package com.mycompany.smartcampusapi.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.mycompany.smartcampusapi.model.ApiError;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Catch-all safety net. Intercepts any unhandled Throwable and returns a
 * sanitised HTTP 500 response â€” never exposing raw stack traces to clients.
 *
 * Cybersecurity rationale: raw stack traces reveal internal package names and
 * class paths (allowing targeted exploitation), exact framework and library
 * versions (enabling CVE lookups), internal method call sequences (exposing
 * business logic), and sometimes sensitive data embedded in exception messages.
 * This mapper logs the full detail server-side for administrators while returning
 * only a safe generic message to the external consumer.
 *
 * Also handles WebApplicationException subtypes (e.g. built-in JAX-RS 404 for
 * unknown paths, or 415 for wrong Content-Type) to ensure every error response
 * across the API is a consistent structured JSON body.
 *
 * @author Yuki Ranathilaka
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER =
            Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable ex) {
        String path = uriInfo != null ? uriInfo.getPath() : "unknown";

        // Pass WebApplicationExceptions through with their correct status code
        // but wrapped in our consistent JSON ApiError body
        if (ex instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) ex;
            int status = wae.getResponse().getStatus();
            String reason = wae.getResponse().getStatusInfo().getReasonPhrase();
            String msg = wae.getMessage() != null ? wae.getMessage() : reason;
            ApiError error = new ApiError(status, reason, msg, path);
            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error)
                    .build();
        }

        // Unexpected server errors: log full detail, return safe 500
        LOGGER.log(Level.SEVERE, "Unhandled server error at path: " + path, ex);
        ApiError error = new ApiError(
                500,
                "Internal Server Error",
                "An unexpected server error occurred. "
                + "Please contact the system administrator.",
                path);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
