package com.jira.jiraloopscript.repository;

import com.jira.jiraloopscript.model.Employee;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
        return store.values().stream()
                .sorted(Comparator.comparing(Employee::getId))
                .collect(Collectors.toList());
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

    @Override
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return store.values().stream()
                .anyMatch(e -> e.getEmail() != null && e.getEmail().equalsIgnoreCase(email)
                        && !e.getId().equals(id));
    }
}
