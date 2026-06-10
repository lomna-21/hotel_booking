package com.example.hotelbooking.ExceptionHandler;

public class CustomerPhoneAlreadyExistsException extends RuntimeException {
    public CustomerPhoneAlreadyExistsException(String message) {
        super(message);
    }
}
