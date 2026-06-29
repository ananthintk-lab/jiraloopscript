# Employee Management REST API

## Project Overview
Build a Spring Boot REST API for employee management supporting full CRUD operations with in-memory storage, input validation, standardized error handling, and Swagger UI documentation.

## Tech Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.x
- **Build Tool**: Maven
- **Storage**: In-memory (ConcurrentHashMap)
- **Documentation**: springdoc-openapi (Swagger UI)
- **API Versioning**: None (deferred)

## Branch Name
`feature/employee-management-api`

## Employee Data Model
| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | Auto-generated, read-only |
| firstName | String | Required, non-blank |
| lastName | String | Required, non-blank |
| email | String | Required, valid email format, unique |

## Error Response Format
```json
{
  "timestamp": "2026-06-29T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for field 'email'",
  "path": "/employees"
}
```

## Quality Gates
Every story must pass before marking complete:
- `mvn clean install`
- `mvn test`

---

## User Stories

### Story US-001: Project Setup
**Priority**: 1 (Critical — blocks all other stories)

As a developer, I want a properly configured Spring Boot Maven project so that I have a working foundation to build the API.

**Acceptance Criteria:**
- Maven `pom.xml` targets Java 21 with Spring Boot 3.x parent
- Dependencies included: `spring-boot-starter-web`, `spring-boot-starter-validation`, `springdoc-openapi-starter-webmvc-ui`
- Application starts on port 8080 with no errors
- Main application class with `@SpringBootApplication`
- `application.properties` configured with app name
- `mvn clean install` passes on a fresh checkout

---

### Story US-002: Employee Model and In-Memory Repository
**Priority**: 2

As a developer, I want an Employee model and in-memory repository so that employee data can be stored, retrieved, and managed across requests.

**Acceptance Criteria:**
- `Employee` class with fields: `id` (Long), `firstName` (String), `lastName` (String), `email` (String)
- `EmployeeRepository` interface defining: `save`, `findById`, `findAll`, `deleteById`, `existsByEmail` methods
- `InMemoryEmployeeRepository` implementation using `ConcurrentHashMap` with thread-safe auto-increment ID
- Repository registered as a Spring `@Repository` bean
- `EmployeeNotFoundException` (extends `RuntimeException`) with message including the missing ID
- Unit tests for repository CRUD operations
- All quality gates pass

---

### Story US-003: Create Employee — POST /employees
**Priority**: 3

As an API consumer, I want to create a new employee so that employee records can be added to the system.

**Acceptance Criteria:**
- `POST /employees` accepts JSON: `{ "firstName": "Jane", "lastName": "Doe", "email": "jane@example.com" }`
- Returns `201 Created` with the created employee including generated `id`
- Validates: `firstName` and `lastName` non-blank, `email` is valid format and unique
- Returns `400 Bad Request` with standard error response on validation failure
- Returns `409 Conflict` if email already exists
- Unit tests cover: success case, blank field validation, invalid email, duplicate email
- All quality gates pass

---

### Story US-004: Get All Employees — GET /employees
**Priority**: 4

As an API consumer, I want to retrieve all employees so that I can list all records in the system.

**Acceptance Criteria:**
- `GET /employees` returns `200 OK` with a JSON array of all employees
- Returns empty array `[]` (not null or 404) when no employees exist
- Response is consistently ordered by `id` ascending
- Unit tests cover: empty store, single employee, multiple employees
- All quality gates pass

---

### Story US-005: Get Employee by ID — GET /employees/{id}
**Priority**: 5

As an API consumer, I want to retrieve a specific employee by ID so that I can view an individual record.

**Acceptance Criteria:**
- `GET /employees/{id}` returns `200 OK` with the employee JSON when found
- Returns `404 Not Found` with standard error response when ID does not exist
- `{id}` must be a valid Long; invalid types return `400 Bad Request`
- Unit tests cover: found, not found, invalid ID type
- All quality gates pass

---

### Story US-006: Update Employee — PUT /employees/{id}
**Priority**: 6

As an API consumer, I want to update an existing employee's details so that records can be kept current.

**Acceptance Criteria:**
- `PUT /employees/{id}` accepts same JSON body as create
- Returns `200 OK` with the updated employee on success
- Returns `404 Not Found` when employee ID does not exist
- Applies same validation as create (non-blank fields, valid email)
- Email uniqueness check excludes the current employee (i.e., an employee can keep the same email)
- Returns `400 Bad Request` on validation failure, `409 Conflict` on duplicate email with another employee
- Unit tests cover: success, not found, blank field, duplicate email with different employee
- All quality gates pass

---

### Story US-007: Delete Employee — DELETE /employees/{id}
**Priority**: 7

As an API consumer, I want to delete an employee by ID so that records can be removed from the system.

**Acceptance Criteria:**
- `DELETE /employees/{id}` returns `204 No Content` on successful deletion
- Returns `404 Not Found` with standard error response when ID does not exist
- After deletion, `GET /employees/{id}` returns `404`
- Unit tests cover: success, not found
- All quality gates pass

---

### Story US-008: Global Exception Handling
**Priority**: 8

As an API consumer, I want consistent, predictable error responses across all endpoints so that I can handle errors reliably in client code.

**Acceptance Criteria:**
- `@RestControllerAdvice` global exception handler class
- Handles `EmployeeNotFoundException` → `404 Not Found`
- Handles `MethodArgumentNotValidException` (Bean Validation) → `400 Bad Request` listing all field errors in `message`
- Handles duplicate email conflict → `409 Conflict`
- Handles `MethodArgumentTypeMismatchException` (bad path variable type) → `400 Bad Request`
- All responses use the standard error format: `{ timestamp, status, error, message, path }`
- Unit tests for each handled exception type using `MockMvc`
- All quality gates pass

---

### Story US-009: OpenAPI / Swagger Documentation
**Priority**: 9

As an API consumer, I want interactive Swagger UI documentation so that I can explore and test all endpoints without additional tooling.

**Acceptance Criteria:**
- Swagger UI accessible at `/swagger-ui.html`
- OpenAPI JSON spec available at `/v3/api-docs`
- API info configured: title "Employee Management API", version "1.0.0", description
- All 5 endpoints annotated with `@Operation(summary = ...)` and `@ApiResponse` annotations
- Request and response body schemas visible in Swagger UI
- Validation constraints visible in schema (e.g., `@NotBlank`, `@Email`)
- All quality gates pass