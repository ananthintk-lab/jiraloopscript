package com.jira.jiraloopscript.repository;

import com.jira.jiraloopscript.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryEmployeeRepositoryTest {

    private InMemoryEmployeeRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEmployeeRepository();
    }

    @Test
    void save_assignsIdAndPersists() {
        Employee employee = new Employee(null, "Alice", "Smith", "alice@example.com");
        Employee saved = repository.save(employee);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void save_multipleEmployees_assignsUniqueIds() {
        Employee e1 = repository.save(new Employee(null, "Alice", "Smith", "alice@example.com"));
        Employee e2 = repository.save(new Employee(null, "Bob", "Jones", "bob@example.com"));

        assertThat(e1.getId()).isNotEqualTo(e2.getId());
    }

    @Test
    void save_existingId_updatesEmployee() {
        Employee saved = repository.save(new Employee(null, "Alice", "Smith", "alice@example.com"));
        saved.setFirstName("Updated");
        repository.save(saved);

        Optional<Employee> result = repository.findById(saved.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("Updated");
    }

    @Test
    void findById_notFound_returnsEmpty() {
        assertThat(repository.findById(999L)).isEmpty();
    }

    @Test
    void findAll_returnsAllEmployees() {
        repository.save(new Employee(null, "Alice", "Smith", "alice@example.com"));
        repository.save(new Employee(null, "Bob", "Jones", "bob@example.com"));

        List<Employee> all = repository.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    void findAll_emptyStore_returnsEmptyList() {
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void deleteById_removesEmployee() {
        Employee saved = repository.save(new Employee(null, "Alice", "Smith", "alice@example.com"));
        repository.deleteById(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    void deleteById_nonExistentId_doesNotThrow() {
        repository.deleteById(999L);
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void existsByEmail_returnsTrue_whenEmailExists() {
        repository.save(new Employee(null, "Alice", "Smith", "alice@example.com"));

        assertThat(repository.existsByEmail("alice@example.com")).isTrue();
    }

    @Test
    void existsByEmail_caseInsensitive() {
        repository.save(new Employee(null, "Alice", "Smith", "alice@example.com"));

        assertThat(repository.existsByEmail("ALICE@EXAMPLE.COM")).isTrue();
    }

    @Test
    void existsByEmail_returnsFalse_whenEmailAbsent() {
        assertThat(repository.existsByEmail("nobody@example.com")).isFalse();
    }
}
