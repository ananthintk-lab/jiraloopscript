package com.jira.jiraloopscript.exception;

/**
 * Thrown when an employee with the given ID does not exist.
 */
public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(Long id) {
        super("Employee not found with id: " + id);
    }
}
