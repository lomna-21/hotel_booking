package com.example.hotelbooking.ExceptionHandler;

public class NoRoomsFoundException extends RuntimeException {
    public NoRoomsFoundException(String message) {
        super(message);
    }
}
