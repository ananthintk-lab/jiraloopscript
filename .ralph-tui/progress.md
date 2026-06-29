# Ralph Progress Log

This file tracks progress across iterations. Agents update this file
after each iteration and it's included in prompts for context.

## Codebase Patterns (Study These First)

- Spring Boot 3.4.1 on Java 21 — use `spring-boot-starter-parent` 3.4.1 (not 4.x)
- springdoc-openapi version 2.7.0 is compatible with Spring Boot 3.x (`springdoc-openapi-starter-webmvc-ui`)
- In-memory storage means Java collections (Map/List), not H2/JPA — no `spring-boot-starter-data-jpa` needed
- `spring-boot-starter-data-jpa-test` is not a real Maven artifact — use `spring-boot-starter-test` for tests
- Base package is `com.jira.jiraloopscript`; sub-packages: `model`, `repository`, `exception`, `service`, `controller`
- InMemoryEmployeeRepository uses `ConcurrentHashMap<Long, Employee>` + `AtomicLong` for thread-safe ID generation
- Repository tests are plain unit tests (no Spring context) — instantiate directly in `@BeforeEach`

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
