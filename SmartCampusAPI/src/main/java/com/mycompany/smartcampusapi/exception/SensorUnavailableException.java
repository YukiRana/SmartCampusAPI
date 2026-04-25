package com.mycompany.smartcampusapi.exception;

/**
 * Thrown when a POST reading is attempted on a sensor in MAINTENANCE status.
 * Mapped to HTTP 403 Forbidden.
 * @author Yuki Ranathilaka
 */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}