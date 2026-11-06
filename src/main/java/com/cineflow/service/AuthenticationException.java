package com.cineflow.service;

/**
 * Exception thrown when authentication credentials fail validation or the user is inactive.
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
