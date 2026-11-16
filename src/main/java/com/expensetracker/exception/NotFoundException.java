package com.expensetracker.exception;

/** Thrown when a referenced resource does not exist; mapped to HTTP 404 by {@link GlobalExceptionHandler}. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
