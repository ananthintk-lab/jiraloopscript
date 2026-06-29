package com.jira.jiraloopscript.repository;

import com.jira.jiraloopscript.model.Employee;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe in-memory implementation of {@link EmployeeRepository}.
 */
@Repository
public class InMemoryEmployeeRepository implements EmployeeRepository {

    private final ConcurrentHashMap<Long, Employee> store = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Employee save(Employee employee) {
        if (employee.getId() == null) {
            employee.setId(idSequence.getAndIncrement());
        }
        store.put(employee.getId(), employee);
        return employee;
    }

    @Override
    public Optional<Employee> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Employee> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return store.values().stream()
                .anyMatch(e -> e.getEmail() != null && e.getEmail().equalsIgnoreCase(email));
    }
}
