
# Research: Ver y Gestionar Órdenes Asignadas

**Feature**: 001-gestionar-ordenes-proveedor
**Date**: 2026-07-04

## Decision 1 — BDD Testing Framework

**Decision**: Cucumber-JVM (cucumber-java 7.x + cucumber-spring + cucumber-junit-platform-engine)

**Rationale**: Cucumber maps directly to the Given-When-Then acceptance scenarios defined in spec.md,
produces living HTML documentation, and integrates natively with Spring Boot test context via
`@SpringBootTest` + `CucumberContextConfiguration`. JUnit 5 is already on the classpath via the
Spring Boot test starters.

**Alternatives considered**:
- JUnit 5 `@DisplayName` with manual Given/When/Then naming — simpler but no business-readable
  report output; rejected because BDD documentation value is lost.
- Spock Framework (Groovy) — excellent BDD syntax but introduces a second language; rejected (YAGNI).

**Gradle coordinates**:
```
testImplementation 'io.cucumber:cucumber-java:7.18.0'
testImplementation 'io.cucumber:cucumber-spring:7.18.0'
testImplementation 'io.cucumber:cucumber-junit-platform-engine:7.18.0'
```

---

## Decision 2 — OpenAPI Generator Integration

**Decision**: `org.openapi.generator` Gradle plugin v7.6.0, generator `spring`, `interfaceOnly=true`

**Rationale**: Generates Java interfaces from the OpenAPI 3.1 contract. Controllers implement the
generated interface, guaranteeing contract compliance at compile time. `interfaceOnly=true` avoids
generating full controller boilerplate while keeping the contract as the single source of truth.
`useSpringBoot3=true` produces annotations compatible with Spring Boot 4.x (Jakarta EE namespace).

**Alternatives considered**:
- Hand-written controllers — rejected: contract and implementation can diverge silently.
- springdoc-openapi (code-first) — rejected: violates API First principle; contract must precede code.

**Generated output directory**: `build/generated/src/main/java`
**API package**: `org.ups.dropshippingservice.adapter.in.web.generated`
**Model package**: `org.ups.dropshippingservice.adapter.in.web.generated.model`

---

## Decision 3 — JaCoCo Configuration

**Decision**: JaCoCo Gradle plugin v0.8.12 with `jacocoTestCoverageVerification` task enforcing
per-class > 80% and global ≥ 80%; build fails on violation.

**Rationale**: Aligns exactly with the constitution's Quality Gates (Principle V). Gradle `check`
task chains `jacocoTestCoverageVerification` so CI fails automatically. HTML + XML + CSV reports are
generated at `build/reports/jacoco/test/`.

**Exclusions policy**: Generated sources under `build/generated/` and the `@SpringBootApplication`
entry-point class are excluded from coverage verification (documented in build.gradle).

---

## Decision 4 — Notification Mechanism for Rejection Alert

**Decision**: Direct call to `NotifyRejectionPort.notifyRejection(OrderRejectedEvent)` from
`RejectOrderService` (Use Case layer). The port implementation is `LogNotificationAdapter`
(Frameworks & Drivers layer), which logs a structured `[COMMERCIAL-ALERT]` INFO line.

**Rationale**: `NotifyRejectionPort` is an output port (Dependency Inversion Principle). The Use
Case depends only on the port interface — the logging/email/SMS adapter is injected at runtime.
The synchronous call executes within the HTTP request cycle, guaranteeing the alert is dispatched
in < 60s (SC-004). In production, `LogNotificationAdapter` is replaced by an email or messaging
adapter without touching the Use Case — satisfying OCP. No Spring `ApplicationEventPublisher` or
`@EventListener` is involved; the direct port call is simpler and fully testable with mocks (YAGNI).

**Alternatives considered**:
- Spring `ApplicationEventPublisher` + `@EventListener` — more decoupled but adds indirection and
  async-testing complexity not warranted for a single synchronous port call; rejected (YAGNI).
- Direct injection of a messaging client into the Use Case — rejected: violates Dependency Inversion
  and Clean Architecture Dependency Rule.

---

## Decision 5 — Optimistic Locking for Concurrent Access

**Decision**: `@Version` field on `OrderJpaEntity` (JPA optimistic locking).

**Rationale**: Prevents two simultaneous provider users from accepting/rejecting the same order
with conflicting state (edge case identified in spec.md). Throws `OptimisticLockException` which
the adapter layer maps to HTTP 409 Conflict. No pessimistic locking needed at this scale (YAGNI).

---

## Decision 6 — Storage

**Decision**: H2 in-memory database (already configured). No PostgreSQL migration in this feature
scope.

**Rationale**: The project build.gradle declares `runtimeOnly 'com.h2database:h2'` and Spring Data
JPA. H2 is sufficient for the development and test scope of US-01. A production migration to
PostgreSQL is a separate infrastructure concern outside this feature's scope.

---

## Decision 7 — Database Schema and Seed Data Strategy

**Decision**: SQL scripts in `src/main/resources/db/schema.sql` (DDL) and
`src/main/resources/db/data.sql` (DML), configured via Spring Boot's SQL initializer.

**Rationale**: Declarative SQL scripts are more transparent and portable than a
`DataLoader` Spring bean: they work outside the Spring context (e.g., in a migration
tool), are version-controlled as plain SQL, and avoid loading Lombok/Spring lifecycle
complexity just to INSERT rows. Spring Boot auto-executes them against the embedded H2
database when `spring.sql.init.mode=always` and `spring.jpa.defer-datasource-initialization=true`
are set. `schema.sql` defines the `orders` table DDL; `data.sql` inserts sample orders for
`prov-001` and `prov-002` used by tests and quickstart validation.

**Alternatives considered**:
- `DataLoader` `@Component` with `CommandLineRunner` — rejected: mixes infrastructure concern
  into the Spring bean graph; harder to reuse outside Spring context.
- Flyway/Liquibase migrations — rejected: adds heavy dependency for a single-service prototype
  with an in-memory database (YAGNI); can be introduced later for production persistence.

**application.properties keys added**:
```
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:db/schema.sql
spring.sql.init.data-locations=classpath:db/data.sql
spring.jpa.defer-datasource-initialization=true
spring.jpa.hibernate.ddl-auto=none
```

---

## Decision 8 — State Machine

**Decision**: Enum `OrderStatus { PENDIENTE, ACEPTADO, RECHAZADO }` with guard validation in the
Use Case layer. No external state machine library.

**Rationale**: Three states, two transitions — a library adds unnecessary complexity (YAGNI). The
Use Case services validate current status before transition and throw a domain exception if the
order is not in `PENDIENTE` state.
