package com.example.xcel_loader.exception;

public class NameSearchException extends RuntimeException {
    public NameSearchException(String message) {
        super(message);
    }

    public NameSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
