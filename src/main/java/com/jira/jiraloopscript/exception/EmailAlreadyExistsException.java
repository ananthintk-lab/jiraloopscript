package com.jira.jiraloopscript.exception;

/**
 * Thrown when attempting to create an employee with an email that already exists.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
    }
}
