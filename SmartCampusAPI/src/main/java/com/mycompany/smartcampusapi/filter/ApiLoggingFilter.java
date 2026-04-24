package com.mycompany.smartcampusapi.filter;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * Cross-cutting logging filter. Logs request and response details.
 * @author Yuki Ranathilaka
 */
@Provider
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        LOGGER.info(String.format("[REQUEST]  %s %s", req.getMethod(), req.getUriInfo().getRequestUri()));
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        LOGGER.info(String.format("[RESPONSE] %s %s -> HTTP %d",
                req.getMethod(), req.getUriInfo().getRequestUri(), res.getStatus()));
    }
}