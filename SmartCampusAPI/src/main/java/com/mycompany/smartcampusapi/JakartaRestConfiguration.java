package com.mycompany.smartcampusapi;

import com.mycompany.smartcampusapi.exception.GlobalExceptionMapper;
import com.mycompany.smartcampusapi.exception.LinkedResourceNotFoundExceptionMapper;
import com.mycompany.smartcampusapi.exception.ResourceNotFoundExceptionMapper;
import com.mycompany.smartcampusapi.exception.RoomNotEmptyExceptionMapper;
import com.mycompany.smartcampusapi.exception.SensorUnavailableExceptionMapper;
import com.mycompany.smartcampusapi.filter.ApiLoggingFilter;
import com.mycompany.smartcampusapi.resources.DiscoveryResource;
import com.mycompany.smartcampusapi.resources.RoomResource;
import com.mycompany.smartcampusapi.resources.SensorResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application configuration.
 * Lifecycle: JAX-RS creates a new resource instance per request (per-request lifecycle).
 * Shared state is held in the static DataStore singleton using ConcurrentHashMap.
 * @author Yuki Ranathilaka
 */
@ApplicationPath("/api/v1")
public class JakartaRestConfiguration extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<>();
		classes.add(DiscoveryResource.class);
		classes.add(RoomResource.class);
		classes.add(SensorResource.class);
		classes.add(RoomNotEmptyExceptionMapper.class);
		classes.add(LinkedResourceNotFoundExceptionMapper.class);
		classes.add(SensorUnavailableExceptionMapper.class);
		classes.add(ResourceNotFoundExceptionMapper.class);
		classes.add(GlobalExceptionMapper.class);
		classes.add(ApiLoggingFilter.class);
		return classes;
	}
}
