package com.mycompany.smartcampusapi;

import com.mycompany.smartcampusapi.exception.GlobalExceptionMapper;
import com.mycompany.smartcampusapi.exception.LinkedResourceNotFoundExceptionMapper;
import com.mycompany.smartcampusapi.exception.RoomNotEmptyExceptionMapper;
import com.mycompany.smartcampusapi.exception.SensorUnavailableExceptionMapper;
import com.mycompany.smartcampusapi.filter.ApiLoggingFilter;
import com.mycompany.smartcampusapi.resources.DiscoveryResource;
import com.mycompany.smartcampusapi.resources.RoomResource;
import com.mycompany.smartcampusapi.resources.SensorResource;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application bootstrap. The @ApplicationPath annotation sets the
 * versioned base URI to /api/v1, satisfying Part 1 of the coursework.
 *
 * JAX-RS Resource Lifecycle:
 * By default, JAX-RS creates a NEW instance of each resource class for every
 * incoming HTTP request (per-request scope). Instance fields on resource classes
 * are therefore NOT shared between requests and cannot safely hold application
 * state. To share mutable data across all requests, the DataStore class uses
 * static final ConcurrentHashMap fields. These live for the full lifetime of
 * the deployed application, not the lifetime of any single resource instance.
 * ConcurrentHashMap is essential here because a plain HashMap is not thread-safe:
 * concurrent put/get operations from different request threads can corrupt the
 * map's internal structure, causing silent data loss or ConcurrentModificationException.
 * Multi-step check-then-act sequences (e.g. addRoom, addSensor) are additionally
 * wrapped in synchronized blocks to guarantee atomicity.
 *
 * @author Yuki Ranathilaka
 */
@ApplicationPath("/api/v1")
public class JakartaRestConfiguration extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        // JAX-RS root resources
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);

        // Exception mappers â€” Part 5
        classes.add(RoomNotEmptyExceptionMapper.class);       // 409
        classes.add(LinkedResourceNotFoundExceptionMapper.class); // 422
        classes.add(SensorUnavailableExceptionMapper.class);  // 403
        classes.add(GlobalExceptionMapper.class);             // 500 catch-all

        // Request/Response logging filter â€” Part 5
        classes.add(ApiLoggingFilter.class);

        return classes;
    }
}
