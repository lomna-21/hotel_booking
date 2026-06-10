package com.example.hotelbooking.ExceptionHandler;

public class NoRoomsAvailableException extends RuntimeException {
    public NoRoomsAvailableException(String message) {
        super(message);
    }
}
