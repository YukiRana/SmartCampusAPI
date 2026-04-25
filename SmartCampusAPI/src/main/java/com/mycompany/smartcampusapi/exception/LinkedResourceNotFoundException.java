package com.mycompany.smartcampusapi.exception;

/**
 * Thrown when a request payload references a resource (e.g. roomId) that
 * does not exist. Mapped to HTTP 422 Unprocessable Entity.
 * @author Yuki Ranathilaka
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}