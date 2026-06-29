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
