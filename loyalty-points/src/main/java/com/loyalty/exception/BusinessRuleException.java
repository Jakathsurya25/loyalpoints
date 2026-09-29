package com.loyalty.exception;

/** A business rule was violated (maps to HTTP 422). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) { super(message); }
}
