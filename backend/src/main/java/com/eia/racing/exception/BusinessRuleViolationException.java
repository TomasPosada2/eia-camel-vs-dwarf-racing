package com.eia.racing.exception;

/** Raised when a request is well-formed but violates a domain business rule (maps to 409 Conflict). */
public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
