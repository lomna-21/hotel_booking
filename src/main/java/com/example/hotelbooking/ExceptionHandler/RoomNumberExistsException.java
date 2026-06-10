package com.example.hotelbooking.ExceptionHandler;

public class RoomNumberExistsException extends RuntimeException {
    public RoomNumberExistsException(String message) {
        super(message);
    }
}
