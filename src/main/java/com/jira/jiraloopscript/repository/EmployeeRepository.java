package com.jira.jiraloopscript.repository;

import com.jira.jiraloopscript.model.Employee;

import java.util.List;
import java.util.Optional;

/**
 * Contract for employee persistence operations.
 */
public interface EmployeeRepository {

    Employee save(Employee employee);

    Optional<Employee> findById(Long id);

    List<Employee> findAll();

    void deleteById(Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
