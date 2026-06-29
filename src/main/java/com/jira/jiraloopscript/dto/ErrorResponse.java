package com.jira.jiraloopscript.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response returned for 4xx/5xx responses.
 */
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private List<String> errors;

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, List<String> errors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.errors = errors;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public List<String> getErrors() { return errors; }
}
