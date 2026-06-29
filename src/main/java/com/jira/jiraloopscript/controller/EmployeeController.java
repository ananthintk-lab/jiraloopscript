package com.jira.jiraloopscript.controller;

import com.jira.jiraloopscript.dto.CreateEmployeeRequest;
import com.jira.jiraloopscript.dto.ErrorResponse;
import com.jira.jiraloopscript.model.Employee;
import com.jira.jiraloopscript.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for employee CRUD operations.
 */
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee Management", description = "APIs for creating, reading, updating, and deleting employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "Get all employees", description = "Returns all employees ordered by id ascending. Returns an empty array when no employees exist.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of employees retrieved successfully",
                content = @Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = Employee.class))))
    })
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @Operation(summary = "Get employee by ID", description = "Returns a single employee by their identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Employee.class))),
        @ApiResponse(responseCode = "404", description = "Employee not found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Operation(summary = "Create a new employee", description = "Creates an employee with the provided details and returns the persisted resource with its generated id.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Employee created successfully",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Employee.class))),
        @ApiResponse(responseCode = "400", description = "Validation error — one or more fields are invalid",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email address is already in use",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        Employee created = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update an existing employee", description = "Updates an employee's first name, last name, and email by ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee updated successfully",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Employee.class))),
        @ApiResponse(responseCode = "400", description = "Validation error — one or more fields are invalid",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Employee not found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email address is already in use by another employee",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id,
                                                   @Valid @RequestBody CreateEmployeeRequest request) {
        Employee updated = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete an employee", description = "Permanently deletes an employee by their identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Employee not found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
