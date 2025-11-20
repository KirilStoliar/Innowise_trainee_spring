package com.stoliar.user_service.exception;

public class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }