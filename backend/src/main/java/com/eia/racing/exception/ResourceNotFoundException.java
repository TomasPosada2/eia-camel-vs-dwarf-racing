package com.eia.racing.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entityName, Object id) {
        return new ResourceNotFoundException(entityName + " with ID " + id + " was not found");
    }
}
