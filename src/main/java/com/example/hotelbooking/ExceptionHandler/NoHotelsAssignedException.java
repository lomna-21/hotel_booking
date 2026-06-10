package com.example.hotelbooking.ExceptionHandler;

public class NoHotelsAssignedException extends RuntimeException {
    public NoHotelsAssignedException(String message) {
        super(message);
    }
}
