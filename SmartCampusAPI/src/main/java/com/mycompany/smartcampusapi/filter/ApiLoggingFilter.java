package com.mycompany.smartcampusapi.filter;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * Cross-cutting API observability filter implementing both
 * ContainerRequestFilter and ContainerResponseFilter in one class.
 *
 * Advantages of a filter over manual Logger.info() in each resource method:
 * (1) DRY — one class handles all endpoints, no duplicated boilerplate.
 * (2) Completeness — captures requests that fail before reaching a resource
 *     method (e.g. 404 for unknown paths, 415 for wrong Content-Type).
 *     Manual in-method logging would silently miss all these cases.
 * (3) Separation of concerns — resource classes focus solely on business logic.
 * (4) Centralised control — the entire logging strategy can be changed or
 *     disabled in one place without touching any resource class.
 *
 * @author Yuki Ranathilaka
 */
@Provider
public class ApiLoggingFilter
        implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER =
            Logger.getLogger(ApiLoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info(String.format("[REQUEST]  Method=%-6s  URI=%s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()));
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.info(String.format("[RESPONSE] Method=%-6s  URI=%s  Status=%d",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri(),
                responseContext.getStatus()));
    }
}