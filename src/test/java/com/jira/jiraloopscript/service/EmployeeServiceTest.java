package com.jira.jiraloopscript.service;

import com.jira.jiraloopscript.dto.CreateEmployeeRequest;
import com.jira.jiraloopscript.exception.EmailAlreadyExistsException;
import com.jira.jiraloopscript.exception.EmployeeNotFoundException;
import com.jira.jiraloopscript.model.Employee;
import com.jira.jiraloopscript.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    EmployeeRepository repository;
    EmployeeService service;

    @BeforeEach
    void setUp() {
        repository = mock(EmployeeRepository.class);
        service = new EmployeeService(repository);
    }

    @Test
    void getAllEmployees_emptyStore_returnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<Employee> result = service.getAllEmployees();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllEmployees_multipleEmployees_returnsDelegatedList() {
        List<Employee> employees = List.of(
                new Employee(1L, "Alice", "Smith", "alice@example.com"),
                new Employee(2L, "Bob", "Jones", "bob@example.com")
        );
        when(repository.findAll()).thenReturn(employees);

        List<Employee> result = service.getAllEmployees();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void getEmployeeById_found_returnsEmployee() {
        Employee employee = new Employee(1L, "Jane", "Doe", "jane@example.com");
        when(repository.findById(1L)).thenReturn(Optional.of(employee));

        Employee result = service.getEmployeeById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Jane");
    }

    @Test
    void getEmployeeById_notFound_throwsEmployeeNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEmployeeById(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateEmployee_success_updatesAndReturnsEmployee() {
        Employee existing = new Employee(1L, "Jane", "Doe", "jane@example.com");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmailAndIdNot("jane@example.com", 1L)).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("Janet");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");

        Employee result = service.updateEmployee(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Janet");
        verify(repository).save(existing);
    }

    @Test
    void updateEmployee_notFound_throwsEmployeeNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");

        assertThatThrownBy(() -> service.updateEmployee(99L, request))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");

        verify(repository, never()).save(any());
    }

    @Test
    void updateEmployee_duplicateEmailWithDifferentEmployee_throwsEmailAlreadyExistsException() {
        Employee existing = new Employee(1L, "Jane", "Doe", "jane@example.com");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmailAndIdNot("other@example.com", 1L)).thenReturn(true);

        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("other@example.com");

        assertThatThrownBy(() -> service.updateEmployee(1L, request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("other@example.com");

        verify(repository, never()).save(any());
    }

    @Test
    void deleteEmployee_success_callsRepositoryDeleteById() {
        Employee employee = new Employee(1L, "Jane", "Doe", "jane@example.com");
        when(repository.findById(1L)).thenReturn(Optional.of(employee));

        service.deleteEmployee(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteEmployee_notFound_throwsEmployeeNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteEmployee(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");

        verify(repository, never()).deleteById(any());
    }

    @Test
    void createEmployee_success_savesAndReturnsEmployee() {
        when(repository.existsByEmail("jane@example.com")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");

        Employee result = service.createEmployee(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
        verify(repository).save(any(Employee.class));
    }

    @Test
    void createEmployee_duplicateEmail_throwsEmailAlreadyExistsException() {
        when(repository.existsByEmail("jane@example.com")).thenReturn(true);

        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");

        assertThatThrownBy(() -> service.createEmployee(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("jane@example.com");

        verify(repository, never()).save(any());
    }
}
