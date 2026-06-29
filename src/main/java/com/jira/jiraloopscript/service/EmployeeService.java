package com.jira.jiraloopscript.service;

import com.jira.jiraloopscript.dto.CreateEmployeeRequest;
import com.jira.jiraloopscript.exception.EmailAlreadyExistsException;
import com.jira.jiraloopscript.model.Employee;
import com.jira.jiraloopscript.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

/**
 * Business logic for employee management operations.
 */
@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new employee, enforcing unique email constraint.
     *
     * @throws EmailAlreadyExistsException if the email is already in use
     */
    public Employee createEmployee(CreateEmployeeRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        return repository.save(employee);
    }
}
