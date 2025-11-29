package com.stoliar.exception;

public class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }