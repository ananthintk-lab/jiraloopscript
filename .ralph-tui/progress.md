# Ralph Progress Log

This file tracks progress across iterations. Agents update this file
after each iteration and it's included in prompts for context.

## Codebase Patterns (Study These First)

- Spring Boot 3.4.1 on Java 21 — use `spring-boot-starter-parent` 3.4.1 (not 4.x)
- springdoc-openapi version 2.7.0 is compatible with Spring Boot 3.x (`springdoc-openapi-starter-webmvc-ui`)
- In-memory storage means Java collections (Map/List), not H2/JPA — no `spring-boot-starter-data-jpa` needed
- `spring-boot-starter-data-jpa-test` is not a real Maven artifact — use `spring-boot-starter-test` for tests
- Base package is `com.jira.jiraloopscript`; sub-packages: `model`, `repository`, `exception`, `service`, `controller`, `dto`
- InMemoryEmployeeRepository uses `ConcurrentHashMap<Long, Employee>` + `AtomicLong` for thread-safe ID generation
- Repository tests are plain unit tests (no Spring context) — instantiate directly in `@BeforeEach`
- DTOs (request/response bodies) go in `dto` package; `CreateEmployeeRequest` uses Jakarta validation annotations
- `GlobalExceptionHandler` (`@RestControllerAdvice`) in `exception` package handles `MethodArgumentNotValidException` (400) and `EmailAlreadyExistsException` (409)
- Controller tests use `@WebMvcTest` + `@MockBean` for the service — loads only the web slice, no full Spring context needed
- Service tests use plain Mockito (`mock()`) — no Spring context, instantiate `EmployeeService` with mocked repo in `@BeforeEach`

---

## 2026-06-29 - US-001
- What was implemented: Fixed pom.xml (Spring Boot 3.4.1, added validation + springdoc deps, removed fake JPA test artifact), updated application.properties with explicit port 8080
- Files changed: `pom.xml`, `src/main/resources/application.properties`
- **Learnings:**
  - The generated project used Spring Boot 4.0.7 (pre-release/invalid) — must use 3.4.1 per PRD
  - `spring-boot-starter-data-jpa-test` doesn't exist; the correct test dep is `spring-boot-starter-test`
  - springdoc-openapi 2.7.0 works with Spring Boot 3.x; incompatible with Boot 4.x
  - Mockito self-attach JVM warnings on Java 21 are harmless and don't affect build/test results
---

## 2026-06-29 - US-002
- What was implemented: Employee model, EmployeeRepository interface, InMemoryEmployeeRepository (@Repository bean), EmployeeNotFoundException, and repository unit tests
- Files changed:
  - `src/main/java/com/jira/jiraloopscript/model/Employee.java`
  - `src/main/java/com/jira/jiraloopscript/repository/EmployeeRepository.java`
  - `src/main/java/com/jira/jiraloopscript/repository/InMemoryEmployeeRepository.java`
  - `src/main/java/com/jira/jiraloopscript/exception/EmployeeNotFoundException.java`
  - `src/test/java/com/jira/jiraloopscript/repository/InMemoryEmployeeRepositoryTest.java`
- **Learnings:**
  - `ConcurrentHashMap` + `AtomicLong` is the right combo for thread-safe in-memory repos — no synchronization needed elsewhere
  - Repository unit tests don't need Spring context; instantiate `InMemoryEmployeeRepository` directly in `@BeforeEach` for fast, isolated tests
  - `existsByEmail` uses `equalsIgnoreCase` for case-insensitive email matching — important for duplicate detection in later stories
  - `mvn clean install` ran 12 tests (1 context + 11 repo) all passing
---

## 2026-06-29 - US-003
- What was implemented: POST /employees endpoint with validation, duplicate-email detection (409 Conflict), and standardised error responses (400 Bad Request)
- Files changed:
  - `src/main/java/com/jira/jiraloopscript/dto/CreateEmployeeRequest.java` (new — request DTO with @NotBlank/@Email)
  - `src/main/java/com/jira/jiraloopscript/dto/ErrorResponse.java` (new — standard error response body)
  - `src/main/java/com/jira/jiraloopscript/exception/EmailAlreadyExistsException.java` (new)
  - `src/main/java/com/jira/jiraloopscript/exception/GlobalExceptionHandler.java` (new — @RestControllerAdvice)
  - `src/main/java/com/jira/jiraloopscript/service/EmployeeService.java` (new)
  - `src/main/java/com/jira/jiraloopscript/controller/EmployeeController.java` (new)
  - `src/test/java/com/jira/jiraloopscript/controller/EmployeeControllerTest.java` (new — 5 @WebMvcTest tests)
  - `src/test/java/com/jira/jiraloopscript/service/EmployeeServiceTest.java` (new — 2 plain unit tests)
- **Learnings:**
  - `@WebMvcTest(EmployeeController.class)` loads only the web slice and auto-picks up `@RestControllerAdvice` — no extra config needed
  - `@MockBean EmployeeService` is the right way to provide the service dep in a web-slice test
  - Service tests need no Spring context; use `mock(EmployeeRepository.class)` and construct `new EmployeeService(repo)` directly
  - `mvn test` ran 19 tests (5 controller + 1 app context + 11 repo + 2 service) — all passing
---

## 2026-06-29 - US-004
- What was implemented: GET /employees endpoint returning all employees ordered by id ascending, with an empty array when no records exist
- Files changed:
  - `src/main/java/com/jira/jiraloopscript/repository/InMemoryEmployeeRepository.java` (findAll() now sorts by id ascending using stream + Comparator)
  - `src/main/java/com/jira/jiraloopscript/service/EmployeeService.java` (added getAllEmployees() method)
  - `src/main/java/com/jira/jiraloopscript/controller/EmployeeController.java` (added GET /employees → 200 OK with List<Employee>)
  - `src/test/java/com/jira/jiraloopscript/controller/EmployeeControllerTest.java` (3 new tests: empty store, single, multiple)
  - `src/test/java/com/jira/jiraloopscript/service/EmployeeServiceTest.java` (2 new tests: empty and multiple)
- **Learnings:**
  - `ConcurrentHashMap.values()` has no iteration-order guarantee — must sort after collecting if ordered output is required
  - `List.of()` (Java 9+) is available and convenient for test fixtures in immutable-list scenarios
  - `mvn test` ran 24 tests (8 controller + 1 app context + 11 repo + 4 service) — all passing
---

## 2026-06-29 - US-006
- What was implemented: PUT /employees/{id} endpoint to update an employee's details, with 200 on success, 404 when not found, 400 on validation failure, 409 on duplicate email with another employee. Email uniqueness check excludes the current employee's own email.
- Files changed:
  - `src/main/java/com/jira/jiraloopscript/repository/EmployeeRepository.java` (added `existsByEmailAndIdNot(String email, Long id)`)
  - `src/main/java/com/jira/jiraloopscript/repository/InMemoryEmployeeRepository.java` (implemented `existsByEmailAndIdNot`)
  - `src/main/java/com/jira/jiraloopscript/service/EmployeeService.java` (added `updateEmployee(Long id, CreateEmployeeRequest request)`)
  - `src/main/java/com/jira/jiraloopscript/controller/EmployeeController.java` (added `PUT /employees/{id}`)
  - `src/test/java/com/jira/jiraloopscript/service/EmployeeServiceTest.java` (3 new tests: success, not found, duplicate email with different employee)
  - `src/test/java/com/jira/jiraloopscript/controller/EmployeeControllerTest.java` (4 new tests: success 200, not found 404, blank field 400, duplicate email 409)
- **Learnings:**
  - For email uniqueness on update, add `existsByEmailAndIdNot(email, id)` to the repository — filters out the current employee's own record so they can keep the same email
  - `@MockBean` service + `eq(id)` matcher from `org.mockito.ArgumentMatchers` is the right combo to stub `updateEmployee(Long, CreateEmployeeRequest)` in `@WebMvcTest`
  - `mvn test` ran 36 tests (15 controller + 1 app context + 11 repo + 9 service) — all passing
---

## 2026-06-29 - US-005
- What was implemented: GET /employees/{id} endpoint returning 200 with employee JSON, 404 when not found, 400 when path variable is not a valid Long
- Files changed:
  - `src/main/java/com/jira/jiraloopscript/exception/GlobalExceptionHandler.java` (added handlers for `EmployeeNotFoundException` → 404 and `MethodArgumentTypeMismatchException` → 400)
  - `src/main/java/com/jira/jiraloopscript/service/EmployeeService.java` (added `getEmployeeById(Long id)`)
  - `src/main/java/com/jira/jiraloopscript/controller/EmployeeController.java` (added `GET /employees/{id}`)
  - `src/test/java/com/jira/jiraloopscript/controller/EmployeeControllerTest.java` (3 new tests: found 200, not found 404, invalid id 400)
  - `src/test/java/com/jira/jiraloopscript/service/EmployeeServiceTest.java` (2 new tests: found, not found)
- **Learnings:**
  - `MethodArgumentTypeMismatchException` (from `org.springframework.web.method.annotation`) is thrown when a path variable can't be converted to the declared type — must be handled explicitly in `@RestControllerAdvice` to return our standard `ErrorResponse` format (400)
  - `EmployeeNotFoundException` was already defined in US-002; just needed to add an `@ExceptionHandler` in `GlobalExceptionHandler` and use it in the service
  - `mvn test` ran 29 tests (11 controller + 1 app context + 11 repo + 6 service) — all passing
---

## 2026-06-29 - US-007
- What was implemented: DELETE /employees/{id} endpoint returning 204 No Content on success and 404 when employee not found
- Files changed:
  - `src/main/java/com/jira/jiraloopscript/service/EmployeeService.java` (added `deleteEmployee(Long id)` — checks existence via `findById`, throws `EmployeeNotFoundException` if absent, then calls `deleteById`)
  - `src/main/java/com/jira/jiraloopscript/controller/EmployeeController.java` (added `DELETE /employees/{id}` → 204 No Content, imported `@DeleteMapping`)
  - `src/test/java/com/jira/jiraloopscript/service/EmployeeServiceTest.java` (2 new tests: success calls deleteById, not found throws exception)
  - `src/test/java/com/jira/jiraloopscript/controller/EmployeeControllerTest.java` (2 new tests: 204 on success, 404 with error body when not found)
- **Learnings:**
  - `deleteById` in `InMemoryEmployeeRepository` was already implemented (US-002 stub) — service just needed existence check before calling it
  - `GlobalExceptionHandler` already had `EmployeeNotFoundException` → 404 handler (US-005), so no new exception handling was needed
  - For void service methods, use `doNothing().when(service).method(id)` and `doThrow(...).when(service).method(id)` in MockMvc tests (not `when(...).thenReturn/thenThrow`)
  - PowerShell's `$?` is set to `$false` when a native command writes to stderr — Mockito's JVM warnings on Java 21 cause this even when build is 100% successful; use `Select-String "BUILD"` to see true outcome
  - `mvn clean install` ran 40 tests (17 controller + 1 app context + 11 repo + 11 service) — all passing
---

## 2026-06-29 - US-008
- What was implemented: Completed Global Exception Handling — added `path` field to `ErrorResponse`, updated `GlobalExceptionHandler` to inject `HttpServletRequest` and populate path on all handlers, changed validation error `message` to join all field errors (instead of "Validation failed"), added `path`/`message`/`error` assertions to controller tests
- Files changed:
  - `src/main/java/com/jira/jiraloopscript/dto/ErrorResponse.java` (replaced `List<String> errors` with `String path`)
  - `src/main/java/com/jira/jiraloopscript/exception/GlobalExceptionHandler.java` (added `HttpServletRequest` param to all 4 handlers, populate path, join field errors into message)
  - `src/test/java/com/jira/jiraloopscript/controller/EmployeeControllerTest.java` (added `$.path`, `$.error`, `$.message` assertions to all exception-handling tests)
- **Learnings:**
  - `HttpServletRequest` can be added as a parameter to any `@ExceptionHandler` method — Spring injects it automatically; use `request.getRequestURI()` for the `path` field
  - For `MethodArgumentNotValidException`, join field errors with `Collectors.joining("; ")` to list them all in the `message` field instead of a separate list
  - `org.hamcrest.Matchers.containsString(...)` works directly in `jsonPath(...).value(...)` assertions for partial string matching
  - `mvn clean install` ran 40 tests (17 controller + 1 app context + 11 repo + 11 service) — all passing
---
