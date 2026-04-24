package com.mycompany.smartcampusapi.exception;
/** @author Yuki Ranathilaka */
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) { super(message); }
}