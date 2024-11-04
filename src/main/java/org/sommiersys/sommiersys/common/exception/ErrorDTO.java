package org.sommiersys.sommiersys.common.exception;

import lombok.Data;

import java.util.Set;

@Data
public class ErrorDTO {
    private final String path;
    private final String error;
    private final String message;
    private final String timestamp;
    private final int status;
    private Set<String> validationErrors;

    public ErrorDTO(String path, String error, String message, String timestamp, int status) {
        this.path = path;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }

    public ErrorDTO(String path, String error, String message, String timestamp, int status, Set<String> validationErrors) {
        this.path = path;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
        this.validationErrors = validationErrors;
    }
}