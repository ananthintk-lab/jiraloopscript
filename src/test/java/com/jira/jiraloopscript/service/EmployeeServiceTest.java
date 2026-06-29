package com.jira.jiraloopscript.service;

import com.jira.jiraloopscript.dto.CreateEmployeeRequest;
import com.jira.jiraloopscript.exception.EmailAlreadyExistsException;
import com.jira.jiraloopscript.model.Employee;
import com.jira.jiraloopscript.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
