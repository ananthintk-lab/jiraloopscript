package com.jira.jiraloopscript.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jira.jiraloopscript.dto.CreateEmployeeRequest;
import com.jira.jiraloopscript.exception.EmailAlreadyExistsException;
import com.jira.jiraloopscript.exception.EmployeeNotFoundException;
import com.jira.jiraloopscript.model.Employee;
import com.jira.jiraloopscript.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EmployeeService employeeService;

    @Test
    void getAllEmployees_emptyStore_returns200WithEmptyArray() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getAllEmployees_singleEmployee_returns200WithArray() throws Exception {
        when(employeeService.getAllEmployees())
                .thenReturn(List.of(new Employee(1L, "Jane", "Doe", "jane@example.com")));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Jane"));
    }

    @Test
    void getAllEmployees_multipleEmployees_returns200OrderedById() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(
                new Employee(1L, "Alice", "Smith", "alice@example.com"),
                new Employee(2L, "Bob", "Jones", "bob@example.com")
        ));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getEmployeeById_found_returns200WithEmployee() throws Exception {
        Employee employee = new Employee(1L, "Jane", "Doe", "jane@example.com");
        when(employeeService.getEmployeeById(1L)).thenReturn(employee);

        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    void getEmployeeById_notFound_returns404() throws Exception {
        when(employeeService.getEmployeeById(99L))
                .thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee not found with id: 99"));
    }

    @Test
    void getEmployeeById_invalidIdType_returns400() throws Exception {
        mockMvc.perform(get("/employees/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createEmployee_success_returns201WithEmployee() throws Exception {
        Employee saved = new Employee(1L, "Jane", "Doe", "jane@example.com");
        when(employeeService.createEmployee(any())).thenReturn(saved);

        CreateEmployeeRequest request = validRequest();

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    void createEmployee_blankFirstName_returns400() throws Exception {
        CreateEmployeeRequest request = validRequest();
        request.setFirstName("");

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createEmployee_blankLastName_returns400() throws Exception {
        CreateEmployeeRequest request = validRequest();
        request.setLastName("   ");

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createEmployee_invalidEmail_returns400() throws Exception {
        CreateEmployeeRequest request = validRequest();
        request.setEmail("not-a-valid-email");

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createEmployee_duplicateEmail_returns409() throws Exception {
        when(employeeService.createEmployee(any()))
                .thenThrow(new EmailAlreadyExistsException("jane@example.com"));

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already exists: jane@example.com"));
    }

    @Test
    void updateEmployee_success_returns200WithUpdatedEmployee() throws Exception {
        Employee updated = new Employee(1L, "Janet", "Doe", "jane@example.com");
        when(employeeService.updateEmployee(eq(1L), any())).thenReturn(updated);

        CreateEmployeeRequest request = validRequest();
        request.setFirstName("Janet");

        mockMvc.perform(put("/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Janet"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    void updateEmployee_notFound_returns404() throws Exception {
        when(employeeService.updateEmployee(eq(99L), any()))
                .thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(put("/employees/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee not found with id: 99"));
    }

    @Test
    void updateEmployee_blankFirstName_returns400() throws Exception {
        CreateEmployeeRequest request = validRequest();
        request.setFirstName("");

        mockMvc.perform(put("/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateEmployee_duplicateEmailWithDifferentEmployee_returns409() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any()))
                .thenThrow(new EmailAlreadyExistsException("other@example.com"));

        CreateEmployeeRequest request = validRequest();
        request.setEmail("other@example.com");

        mockMvc.perform(put("/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already exists: other@example.com"));
    }

    @Test
    void deleteEmployee_success_returns204() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEmployee_notFound_returns404() throws Exception {
        doThrow(new EmployeeNotFoundException(99L)).when(employeeService).deleteEmployee(99L);

        mockMvc.perform(delete("/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee not found with id: 99"));
    }

    private CreateEmployeeRequest validRequest() {
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setFirstName("Jane");
        req.setLastName("Doe");
        req.setEmail("jane@example.com");
        return req;
    }
}
