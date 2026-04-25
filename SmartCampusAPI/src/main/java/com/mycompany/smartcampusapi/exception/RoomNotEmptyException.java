package com.mycompany.smartcampusapi.exception;

/**
 * Thrown when a room deletion is attempted while sensors are still assigned.
 * Mapped to HTTP 409 Conflict.
 * @author Yuki Ranathilaka
 */
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}