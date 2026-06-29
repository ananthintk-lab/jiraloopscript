package com.jira.jiraloopscript.service;

import com.jira.jiraloopscript.dto.CreateEmployeeRequest;
import com.jira.jiraloopscript.exception.EmailAlreadyExistsException;
import com.jira.jiraloopscript.exception.EmployeeNotFoundException;
import com.jira.jiraloopscript.model.Employee;
import com.jira.jiraloopscript.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
     * Returns all employees ordered by id ascending.
     */
    public List<Employee> getAllEmployees() {
        return repository.findAll();
    }

    /**
     * Returns a single employee by id.
     *
     * @throws EmployeeNotFoundException if no employee exists with the given id
     */
    public Employee getEmployeeById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    /**
     * Updates an existing employee, enforcing unique email constraint (excluding self).
     *
     * @throws EmployeeNotFoundException if no employee exists with the given id
     * @throws EmailAlreadyExistsException if the email is already in use by another employee
     */
    public Employee updateEmployee(Long id, CreateEmployeeRequest request) {
        Employee employee = repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        if (repository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        return repository.save(employee);
    }

    /**
     * Deletes an employee by id.
     *
     * @throws EmployeeNotFoundException if no employee exists with the given id
     */
    public void deleteEmployee(Long id) {
        if (repository.findById(id).isEmpty()) {
            throw new EmployeeNotFoundException(id);
        }
        repository.deleteById(id);
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
