---
description: "Task list for US-01 — Ver y Gestionar Órdenes Asignadas"
---

# Tasks: Ver y Gestionar Órdenes Asignadas

**Input**: Design documents from `specs/001-gestionar-ordenes-proveedor/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/openapi.yaml ✅

**Architecture**: Clean Architecture (4 layers) — Java 26 / Spring Boot 4.1.0 / Gradle
**Testing**: BDD mandatory (constitution Principle II) — Cucumber-JVM 7.18.0 + JUnit 5
**Coverage**: JaCoCo per-class > 80%, global ≥ 80% (constitution Principle V)
**DB Init**: SQL scripts in `src/main/resources/db/` (no DataLoader bean)

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no shared dependency on an incomplete task)
- **[Story]**: User story label (US1, US2, US3)
- Exact file paths are included in every task description

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Build tooling, plugins, and runtime configuration that enable all subsequent tasks.

- [X] T001 Update `build.gradle`: add `org.openapi.generator` plugin v7.6.0, configure `openApiGenerate` task (`generatorName=spring`, `inputSpec` pointing to `specs/001-gestionar-ordenes-proveedor/contracts/openapi.yaml`, `apiPackage=org.ups.dropshippingservice.adapter.in.web.generated`, `modelPackage=org.ups.dropshippingservice.adapter.in.web.generated.model`, `interfaceOnly=true`, `useSpringBoot3=true`), add generated output `$buildDir/generated/src/main/java` to `sourceSets.main.java.srcDirs`, and wire `compileJava.dependsOn openApiGenerate` in `build.gradle`
- [X] T002 Update `build.gradle`: add `jacoco` plugin v0.8.12, configure `jacocoTestReport` task to generate HTML, XML, and CSV reports under `build/reports/jacoco/test/`, configure `jacocoTestCoverageVerification` with two violation rules — global `MINIMUM=0.8` and per-CLASS `MINIMUM=0.8` — excluding `**/DropshippingServiceApplication*` and `**/generated/**`; chain `test.finalizedBy jacocoTestReport` and `check.dependsOn jacocoTestCoverageVerification` in `build.gradle`
- [X] T003 Update `build.gradle`: add Cucumber-JVM 7.18.0 test dependencies (`io.cucumber:cucumber-java:7.18.0`, `io.cucumber:cucumber-spring:7.18.0`, `io.cucumber:cucumber-junit-platform-engine:7.18.0`) under `testImplementation` in `build.gradle`
- [X] T004 [P] Create `src/main/resources/application.properties` with: H2 datasource URL `jdbc:h2:mem:dropshipping;DB_CLOSE_DELAY=-1`, `spring.jpa.hibernate.ddl-auto=none`, `spring.sql.init.mode=always`, `spring.sql.init.schema-locations=classpath:db/schema.sql`, `spring.sql.init.data-locations=classpath:db/data.sql`, `spring.jpa.defer-datasource-initialization=true`, H2 console enabled at `/h2-console`, and `spring.jpa.show-sql=true` in `src/main/resources/application.properties`
- [X] T005 [P] Create `src/main/resources/db/schema.sql` with `CREATE TABLE IF NOT EXISTS orders` DDL: columns `id UUID PRIMARY KEY`, `order_code VARCHAR(100) NOT NULL UNIQUE`, `provider_id VARCHAR(100) NOT NULL`, `status VARCHAR(20) NOT NULL`, `product_code VARCHAR(100) NOT NULL`, `product_description VARCHAR(500) NOT NULL`, `quantity INT NOT NULL`, `street VARCHAR(300) NOT NULL`, `city VARCHAR(100) NOT NULL`, `state VARCHAR(100) NOT NULL`, `postal_code VARCHAR(20) NOT NULL`, `country VARCHAR(100) NOT NULL`, `customer_name VARCHAR(200) NOT NULL`, `phone VARCHAR(50) NOT NULL`, `email VARCHAR(200)`, `expected_delivery_date DATE NOT NULL`, `special_conditions VARCHAR(500)`, `estimated_dispatch_date DATE`, `rejection_reason VARCHAR(500)`, `last_action_by VARCHAR(100)`, `last_action_at TIMESTAMP`, `version BIGINT NOT NULL DEFAULT 0` in `src/main/resources/db/schema.sql`
- [X] T006 [P] Create `src/main/resources/db/data.sql` with `INSERT INTO orders` statements for: two PENDIENTE orders for `prov-001` (IDs `550e8400-e29b-41d4-a716-446655440000` and `550e8400-e29b-41d4-a716-446655440001`) and one PENDIENTE order for `prov-002` (ID `660e8400-e29b-41d4-a716-446655440001`); all with realistic product, address, customer and future expected_delivery_date values matching quickstart.md scenarios in `src/main/resources/db/data.sql`

**Checkpoint**: `./gradlew openApiGenerate compileJava` succeeds; `GET /h2-console` shows `orders` table pre-populated with 3 rows after `./gradlew bootRun`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Domain model, output ports, persistence adapter, notification adapter, and BDD runner.
**⚠️ CRITICAL**: No user story implementation can begin until this phase is complete.

### Domain Value Objects and Enum (parallelizable group — different files, no shared deps)

- [X] T007 [P] Create `OrderStatus.java` enum with values `PENDIENTE`, `ACEPTADO`, `RECHAZADO` in `src/main/java/org/ups/dropshippingservice/domain/OrderStatus.java`
- [X] T008 [P] Create `OrderProduct.java` as a Java record with fields `String productCode`, `String description`, `int quantity`; add constructor validation (`quantity >= 1`, non-blank strings) in `src/main/java/org/ups/dropshippingservice/domain/OrderProduct.java`
- [X] T009 [P] Create `DeliveryAddress.java` as a Java record with fields `String street`, `String city`, `String state`, `String postalCode`, `String country`; add non-blank validation in constructor in `src/main/java/org/ups/dropshippingservice/domain/DeliveryAddress.java`
- [X] T010 [P] Create `CustomerContact.java` as a Java record with fields `String name`, `String phone`, `String email` (nullable); add non-blank validation for `name` and `phone` in `src/main/java/org/ups/dropshippingservice/domain/CustomerContact.java`

### Domain Entity and Event (depends on T007–T010)

- [X] T011 Create `Order.java` domain entity (plain Java, NO Spring/JPA annotations) with all fields from data-model.md (`UUID id`, `String orderCode`, `String providerId`, `OrderStatus status`, `OrderProduct product`, `DeliveryAddress deliveryAddress`, `CustomerContact customerContact`, `LocalDate expectedDeliveryDate`, `String specialConditions`, `LocalDate estimatedDispatchDate`, `String rejectionReason`, `String lastActionBy`, `Instant lastActionAt`, `long version`), business method `accept(LocalDate estimatedDispatchDate, String actorId)` that throws `OrderAlreadyProcessedException` if status != PENDIENTE, and business method `reject(String rejectionReason, String actorId)` that throws `OrderAlreadyProcessedException` if status != PENDIENTE in `src/main/java/org/ups/dropshippingservice/domain/Order.java`
- [X] T012 [P] Create `OrderRejectedEvent.java` as a Java record with fields `UUID orderId`, `String orderCode`, `String customerName`, `String productCode`, `String rejectionReason`, `Instant rejectedAt` in `src/main/java/org/ups/dropshippingservice/domain/event/OrderRejectedEvent.java`

### Application Exceptions (parallelizable group)

- [X] T013 [P] Create `OrderNotFoundException.java` extending `RuntimeException` with constructor `(UUID orderId)` that sets message `"Order not found: " + orderId` in `src/main/java/org/ups/dropshippingservice/application/exception/OrderNotFoundException.java`
- [X] T014 [P] Create `OrderAlreadyProcessedException.java` extending `RuntimeException` with constructor `(UUID orderId, OrderStatus currentStatus)` that sets message `"Order " + orderId + " is already " + currentStatus` in `src/main/java/org/ups/dropshippingservice/application/exception/OrderAlreadyProcessedException.java`
- [X] T015 [P] Create `InvalidDispatchDateException.java` extending `RuntimeException` with constructor `(LocalDate date)` that sets message `"Dispatch date must be today or in the future: " + date` in `src/main/java/org/ups/dropshippingservice/application/exception/InvalidDispatchDateException.java`

### Output Port Interfaces (depends on T011, T012)

- [X] T016 [P] Create `LoadOrderPort.java` interface with methods `Optional<Order> findByIdAndProviderId(UUID orderId, String providerId)` and `List<Order> findAllByProviderId(String providerId)` in `src/main/java/org/ups/dropshippingservice/application/port/out/LoadOrderPort.java`
- [X] T017 [P] Create `SaveOrderPort.java` interface with method `Order save(Order order)` in `src/main/java/org/ups/dropshippingservice/application/port/out/SaveOrderPort.java`
- [X] T018 [P] Create `NotifyRejectionPort.java` interface with method `void notifyRejection(OrderRejectedEvent event)` in `src/main/java/org/ups/dropshippingservice/application/port/out/NotifyRejectionPort.java`

### Persistence Adapter (depends on T007–T011, T016, T017)

- [X] T019 Create `OrderJpaEntity.java` `@Entity @Table(name="orders")` class with `@Id` UUID field, `@Version` long field, all scalar columns as `@Column` fields, and three `@Embedded` groups (`OrderProductEmbeddable`, `DeliveryAddressEmbeddable`, `CustomerContactEmbeddable`) matching the `schema.sql` column names; NO domain logic in `src/main/java/org/ups/dropshippingservice/adapter/out/persistence/OrderJpaEntity.java`
- [X] T020 Create `SpringDataOrderRepository.java` interface extending `JpaRepository<OrderJpaEntity, UUID>` with methods `Optional<OrderJpaEntity> findByIdAndProviderId(UUID id, String providerId)` and `List<OrderJpaEntity> findAllByProviderId(String providerId)` in `src/main/java/org/ups/dropshippingservice/adapter/out/persistence/SpringDataOrderRepository.java`
- [X] T021 Create `OrderPersistenceMapper.java` `@Component` with methods `Order toDomain(OrderJpaEntity entity)` and `OrderJpaEntity toJpaEntity(Order order)` that translate between the domain `Order` and `OrderJpaEntity` in `src/main/java/org/ups/dropshippingservice/adapter/out/persistence/OrderPersistenceMapper.java`
- [X] T022 Create `OrderPersistenceAdapter.java` `@Component` implementing `LoadOrderPort` and `SaveOrderPort`; inject `SpringDataOrderRepository` and `OrderPersistenceMapper`; `findByIdAndProviderId` delegates to repository and maps result; `findAllByProviderId` delegates and maps; `save` maps to entity, calls `repository.save()`, maps result back in `src/main/java/org/ups/dropshippingservice/adapter/out/persistence/OrderPersistenceAdapter.java`

### Notification Adapter (depends on T012, T018)

- [X] T023 Create `LogNotificationAdapter.java` `@Component` implementing `NotifyRejectionPort`; inject SLF4J logger; `notifyRejection` logs one INFO line: `[COMMERCIAL-ALERT] Order {} rejected by {}. Reason: {}` with `orderCode`, `actorId` (from event fields), and `rejectionReason` in `src/main/java/org/ups/dropshippingservice/adapter/out/notification/LogNotificationAdapter.java`

### BDD Test Infrastructure (parallelizable group)

- [X] T024 [P] Create `CucumberSpringContextConfig.java` annotated with `@CucumberContextConfiguration`, `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`, and `@ActiveProfiles("test")` in `src/test/java/org/ups/dropshippingservice/bdd/config/CucumberSpringContextConfig.java`
- [X] T025 [P] Create `CucumberRunnerIT.java` annotated with `@Suite`, `@IncludeEngines("cucumber")`, `@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "org.ups.dropshippingservice.bdd")`, and `@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "src/test/resources/features")` in `src/test/java/org/ups/dropshippingservice/bdd/CucumberRunnerIT.java`

**Checkpoint**: `./gradlew compileJava compileTestJava` succeeds — all domain entities, ports, adapters, and Cucumber runner compile with zero errors.

---

## Phase 3: User Story 1 — Ver Órdenes Asignadas (Priority: P1) 🎯 MVP

**Goal**: Provider authenticates and sees all their assigned orders with every required field (FR-001, FR-002, FR-009).

**Independent Test**: `GET /api/v1/providers/prov-001/orders` returns HTTP 200 with a JSON array containing the 2 pre-seeded orders for `prov-001`, each showing all fields defined in `OrderResponse` (see `quickstart.md` Scenario 1).

### Tests for US1 — Write FIRST, confirm FAIL before implementing (constitution Principle II)

- [X] T026 [P] [US1] Write Cucumber feature `us1_ver_ordenes_asignadas.feature` with 3 scenarios: (a) `Given` header `X-Provider-Id: prov-001` `When` GET providers/prov-001/orders `Then` HTTP 200 with all FR-002 fields; (b) `Given` header `X-Provider-Id: prov-999` `When` GET providers/prov-999/orders `Then` HTTP 200 empty array; (c) `Given` header `X-Provider-Id: prov-001` `When` GET providers/prov-002/orders (mismatch) `Then` HTTP 403 `ACCESS_DENIED` in `src/test/resources/features/us1_ver_ordenes_asignadas.feature`
- [X] T027 [P] [US1] Write `OrderTest.java` unit tests: (a) `Order.accept()` on ACEPTADO order throws `OrderAlreadyProcessedException`; (b) `Order.reject()` on RECHAZADO order throws `OrderAlreadyProcessedException`; (c) `Order.accept()` sets status, estimatedDispatchDate, lastActionBy, lastActionAt in `src/test/java/org/ups/dropshippingservice/domain/OrderTest.java`
- [X] T028 [P] [US1] Write `GetAssignedOrdersServiceTest.java`: mock `LoadOrderPort`; assert service returns all orders when provider has orders; assert service returns empty list when no orders; verify `findAllByProviderId` called with correct providerId in `src/test/java/org/ups/dropshippingservice/application/GetAssignedOrdersServiceTest.java`

### Implementation for US1

- [X] T029 [US1] Create `GetAssignedOrdersUseCase.java` input port interface with method `List<Order> getAssignedOrders(String providerId)` in `src/main/java/org/ups/dropshippingservice/application/port/in/GetAssignedOrdersUseCase.java`
- [X] T030 [US1] Implement `GetAssignedOrdersService.java` `@Service` implementing `GetAssignedOrdersUseCase`; inject `LoadOrderPort`; `getAssignedOrders` delegates to `loadOrderPort.findAllByProviderId(providerId)` in `src/main/java/org/ups/dropshippingservice/application/service/GetAssignedOrdersService.java`
- [X] T031 [US1] Create `OrderControllerMapper.java` `@Component` with method `OrderResponse toResponse(Order order)` that maps all domain fields to the generated `OrderResponse` DTO (including nested `ProductDetails`, `DeliveryAddress`, `CustomerContact`) in `src/main/java/org/ups/dropshippingservice/adapter/in/web/OrderControllerMapper.java`
- [X] T032 [US1] Create `OrderController.java` `@RestController` implementing the generated `ProviderOrdersApi` interface; inject `GetAssignedOrdersUseCase` and `OrderControllerMapper`; implement `getAssignedOrders(@RequestHeader("X-Provider-Id") String xProviderId, @PathVariable String providerId)` — compare `xProviderId` with `providerId` and return `ResponseEntity` with HTTP 403 and `ErrorResponse("ACCESS_DENIED", "Access denied")` if they do not match; otherwise delegate to `GetAssignedOrdersUseCase` and return HTTP 200 in `src/main/java/org/ups/dropshippingservice/adapter/in/web/OrderController.java`
- [X] T033 [US1] Create `GlobalExceptionHandler.java` `@RestControllerAdvice` with `@ExceptionHandler` methods: `OrderNotFoundException` → 404 `ErrorResponse`, `OrderAlreadyProcessedException` → 409 `ErrorResponse`, `InvalidDispatchDateException` → 400 `ErrorResponse`, `MissingRequestHeaderException` → 401 `ErrorResponse("MISSING_PROVIDER_IDENTITY", "X-Provider-Id header is required")` in `src/main/java/org/ups/dropshippingservice/adapter/in/web/GlobalExceptionHandler.java`
- [X] T034 [US1] Create `ApplicationBeanConfig.java` `@Configuration` class that declares `@Bean` for `GetAssignedOrdersService` (injecting `LoadOrderPort`) in `src/main/java/org/ups/dropshippingservice/infrastructure/config/ApplicationBeanConfig.java`
- [X] T035 [P] [US1] Write `VerOrdenesSteps.java` Cucumber step definitions wiring `us1_ver_ordenes_asignadas.feature` scenarios using `@Autowired TestRestTemplate`; assert HTTP status and response body fields in `src/test/java/org/ups/dropshippingservice/bdd/steps/VerOrdenesSteps.java`
- [X] T036 [US1] Write `OrderControllerIT.java` `@WebMvcTest(OrderController.class)` integration test: mock `GetAssignedOrdersUseCase`; test GET returns 200 with correct payload; test GET with unknown providerId returns 200 empty array in `src/test/java/org/ups/dropshippingservice/adapter/web/OrderControllerIT.java`
- [X] T037 [US1] Write `OrderPersistenceAdapterIT.java` `@DataJpaTest` annotated with `@Sql(scripts = {"classpath:db/schema.sql", "classpath:db/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)` to ensure H2 schema and seed data are present (required because `spring.jpa.hibernate.ddl-auto=none`); assert `findByIdAndProviderId` returns the seeded prov-001 order; assert `findAllByProviderId("prov-001")` returns 2 orders; assert `findAllByProviderId("prov-999")` returns empty list in `src/test/java/org/ups/dropshippingservice/adapter/persistence/OrderPersistenceAdapterIT.java`

**Checkpoint**: `./gradlew test` green — all US1 unit, integration, and BDD tests pass. `GET /api/v1/providers/prov-001/orders` returns HTTP 200 with 2 seeded orders containing all FR-002 fields.

---

## Phase 4: User Story 2 — Aceptar Orden (Priority: P2)

**Goal**: Provider accepts a PENDIENTE order by providing a valid future estimated dispatch date; status changes to ACEPTADO with actor and timestamp; analyst sees update within 5s (SC-003).

**Independent Test**: `PUT /api/v1/orders/550e8400-e29b-41d4-a716-446655440000/accept` with `{"estimatedDispatchDate":"2026-07-10"}` returns HTTP 200 with `status=ACEPTADO`, `lastActionBy` non-null, and `lastActionAt` non-null (see `quickstart.md` Scenario 2).

### Tests for US2 — Write FIRST, confirm FAIL

- [X] T038 [P] [US2] Write Cucumber feature `us2_aceptar_orden.feature` with 4 scenarios: (a) accept with valid future date → HTTP 200, status=ACEPTADO; (b) accept with past date → HTTP 400 INVALID_DISPATCH_DATE; (c) accept already-ACEPTADO order → HTTP 409 ORDER_ALREADY_PROCESSED; (d) accept without date field → HTTP 400 in `src/test/resources/features/us2_aceptar_orden.feature`
- [X] T039 [P] [US2] Write `AcceptOrderServiceTest.java`: mock `LoadOrderPort` and `SaveOrderPort`; assert happy path transitions order to ACEPTADO and calls `save`; assert `InvalidDispatchDateException` when date is yesterday; assert `OrderAlreadyProcessedException` when order is already ACEPTADO; assert `OrderNotFoundException` when order not found in `src/test/java/org/ups/dropshippingservice/application/AcceptOrderServiceTest.java`

### Implementation for US2

- [X] T040 [US2] Create `AcceptOrderUseCase.java` input port interface with method `Order acceptOrder(UUID orderId, String providerId, LocalDate estimatedDispatchDate)` in `src/main/java/org/ups/dropshippingservice/application/port/in/AcceptOrderUseCase.java`
- [X] T041 [US2] Implement `AcceptOrderService.java` `@Service` implementing `AcceptOrderUseCase`; inject `LoadOrderPort` and `SaveOrderPort`; load order via `findByIdAndProviderId` (throw `OrderNotFoundException` if absent); validate `estimatedDispatchDate >= LocalDate.now()` (throw `InvalidDispatchDateException` if past); call `order.accept(estimatedDispatchDate, providerId)`; return `saveOrderPort.save(order)` in `src/main/java/org/ups/dropshippingservice/application/service/AcceptOrderService.java`
- [X] T042 [US2] Add `acceptOrder()` method to `OrderController.java` implementing the generated `ProviderOrdersApi.acceptOrder()`; extract `providerId` from the `@RequestHeader("X-Provider-Id") String xProviderId` parameter (not from path); call `AcceptOrderUseCase.acceptOrder(orderId, xProviderId, estimatedDispatchDate)`; map result to `OrderResponse` in `src/main/java/org/ups/dropshippingservice/adapter/in/web/OrderController.java`
- [X] T043 [US2] Update `ApplicationBeanConfig.java` to declare `@Bean AcceptOrderService` injecting `LoadOrderPort` and `SaveOrderPort` in `src/main/java/org/ups/dropshippingservice/infrastructure/config/ApplicationBeanConfig.java`
- [X] T044 [P] [US2] Write `AceptarOrdenSteps.java` Cucumber step definitions for `us2_aceptar_orden.feature` using `TestRestTemplate`; assert HTTP status, response status field, and error codes in `src/test/java/org/ups/dropshippingservice/bdd/steps/AceptarOrdenSteps.java`
- [X] T045 [US2] Add US2 test cases to `OrderControllerIT.java`: mock `AcceptOrderUseCase`; test PUT accept happy path returns 200; test PUT with past date returns 400; test PUT on already-processed returns 409 in `src/test/java/org/ups/dropshippingservice/adapter/web/OrderControllerIT.java`

**Checkpoint**: `./gradlew test` green — all US1 + US2 tests pass. `PUT /orders/{id}/accept` works for all 4 scenarios.

---

## Phase 5: User Story 3 — Rechazar Orden (Priority: P3)

**Goal**: Provider rejects a PENDIENTE order with a non-blank reason; status changes to RECHAZADO; commercial team receives alert within 60s (SC-004, FR-007, FR-008).

**Independent Test**: `PUT /api/v1/orders/550e8400-e29b-41d4-a716-446655440001/reject` with `{"rejectionReason":"Producto sin stock"}` returns HTTP 200 with `status=RECHAZADO` and server logs contain `[COMMERCIAL-ALERT]` line (see `quickstart.md` Scenario 3).

### Tests for US3 — Write FIRST, confirm FAIL

- [X] T046 [P] [US3] Write Cucumber feature `us3_rechazar_orden.feature` with 3 scenarios: (a) reject with valid reason → HTTP 200, status=RECHAZADO; (b) reject with blank reason → HTTP 400 REJECTION_REASON_REQUIRED; (c) reject already-RECHAZADO order → HTTP 409 ORDER_ALREADY_PROCESSED in `src/test/resources/features/us3_rechazar_orden.feature`
- [X] T047 [P] [US3] Write `RejectOrderServiceTest.java`: mock `LoadOrderPort`, `SaveOrderPort`, `NotifyRejectionPort`; assert happy path transitions to RECHAZADO, calls `save`, and calls `notifyRejection` with correct `OrderRejectedEvent` fields; assert `OrderAlreadyProcessedException` on non-PENDIENTE; assert blank reason validation fires before domain call in `src/test/java/org/ups/dropshippingservice/application/RejectOrderServiceTest.java`

### Implementation for US3

- [X] T048 [US3] Create `RejectOrderUseCase.java` input port interface with method `Order rejectOrder(UUID orderId, String providerId, String rejectionReason)` in `src/main/java/org/ups/dropshippingservice/application/port/in/RejectOrderUseCase.java`
- [X] T049 [US3] Implement `RejectOrderService.java` `@Service` implementing `RejectOrderUseCase`; inject `LoadOrderPort`, `SaveOrderPort`, `NotifyRejectionPort`; validate `rejectionReason` is not blank (throw `IllegalArgumentException` mapped to 400 by handler); load order (throw `OrderNotFoundException` if absent); call `order.reject(rejectionReason, providerId)`; save; build `OrderRejectedEvent` and call `notifyRejectionPort.notifyRejection(event)`; return saved order in `src/main/java/org/ups/dropshippingservice/application/service/RejectOrderService.java`
- [X] T050 [US3] Add `rejectOrder()` method to `OrderController.java` implementing `ProviderOrdersApi.rejectOrder()`; extract `providerId` from the `@RequestHeader("X-Provider-Id") String xProviderId` parameter (not from path); call `RejectOrderUseCase.rejectOrder(orderId, xProviderId, rejectionReason)`; map result to `OrderResponse` in `src/main/java/org/ups/dropshippingservice/adapter/in/web/OrderController.java`
- [X] T051 [US3] Update `ApplicationBeanConfig.java` to declare `@Bean RejectOrderService` injecting `LoadOrderPort`, `SaveOrderPort`, and `NotifyRejectionPort` in `src/main/java/org/ups/dropshippingservice/infrastructure/config/ApplicationBeanConfig.java`
- [X] T052 [P] [US3] Write `RechazarOrdenSteps.java` Cucumber step definitions for `us3_rechazar_orden.feature` using `TestRestTemplate`; assert HTTP status, response status field, and error codes in `src/test/java/org/ups/dropshippingservice/bdd/steps/RechazarOrdenSteps.java`
- [X] T053 [US3] Add US3 test cases to `OrderControllerIT.java`: mock `RejectOrderUseCase`; test PUT reject happy path returns 200; test blank reason returns 400; test already-processed returns 409 in `src/test/java/org/ups/dropshippingservice/adapter/web/OrderControllerIT.java`

**Checkpoint**: All user stories independently functional and testable.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Quality gate verification and final integration validation.

- [X] T054 [P] Run `./gradlew test jacocoTestReport jacocoTestCoverageVerification` and confirm build passes with global coverage ≥ 80% and per-class coverage > 80%; if any class fails, add missing test cases to bring it above threshold; adjust JaCoCo exclusions in `build.gradle` for generated code and `DataLoader` (if any) as needed
- [X] T054b Add ArchUnit dependency to `build.gradle`: `testImplementation 'com.tngtech.archunit:archunit-junit5:1.3.0'`; then create `CleanArchitectureTest.java` annotated with `@AnalyzeClasses(packages = "org.ups.dropshippingservice")` with `@ArchTest` rules verifying: (1) domain layer has no imports from application/adapter/infrastructure packages; (2) application layer has no imports from adapter/infrastructure packages; (3) adapter layer has no imports from infrastructure package; (4) no class in domain or application packages is annotated with Spring stereotypes (`@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController`, `@Configuration`) in `src/test/java/org/ups/dropshippingservice/architecture/CleanArchitectureTest.java`
- [X] T054c Add Checkstyle plugin to `build.gradle`: `id 'checkstyle'`; configure `checkstyle { toolVersion = '10.17.0'; configFile = file('config/checkstyle/checkstyle.xml') }`; create `config/checkstyle/checkstyle.xml` based on Google Java Style with module `FileTabCharacter` (no tabs), `LineLength` (max=120), `MethodName` (camelCase), `TypeName` (PascalCase), and `LeftCurly`/`RightCurly`; chain `check.dependsOn checkstyleMain checkstyleTest` so CI fails on style violations in `config/checkstyle/checkstyle.xml` and `build.gradle`
- [X] T055 [P] Run full Cucumber suite `./gradlew test --tests "*CucumberRunnerIT"` and confirm all 10 acceptance scenarios (across 3 feature files) pass; fix any failing step definitions
- [X] T056 Run end-to-end build `./gradlew clean openApiGenerate compileJava test jacocoTestCoverageVerification` and confirm zero compilation errors, zero test failures, and coverage thresholds pass; verify `build/reports/jacoco/test/html/index.html` shows ≥ 80% global coverage

---

## Phase 7: Edge Case Coverage

**Purpose**: Cubrir los dos edge cases del spec (EC-02 y EC-04) que quedaron sin cobertura de prueba tras la implementación inicial.

- [X] T057 [EC-02] In `RejectOrderService.java`, wrap the call to `notifyRejectionPort.notifyRejection(event)` in a `try-catch (RuntimeException e)` block that logs `log.warn("[COMMERCIAL-ALERT-FAILED] Could not notify rejection for order {}: {}", order.getOrderCode(), e.getMessage())` and does NOT re-throw. Then add test `rejectOrder_whenNotificationFails_rejectionStillPersists` to `RejectOrderServiceTest.java`: configure `notifyRejectionPort` mock to throw `new RuntimeException("service unavailable")`; invoke `rejectOrderService.rejectOrder(...)`; verify `saveOrderPort.save()` was invoked exactly once (order state persisted); assert no exception is thrown from the service method in `src/main/java/org/ups/dropshippingservice/application/service/RejectOrderService.java` and `src/test/java/org/ups/dropshippingservice/application/RejectOrderServiceTest.java`

- [X] T058 [EC-04] Add `@ExceptionHandler(ObjectOptimisticLockingFailureException.class)` to `GlobalExceptionHandler.java` that returns `ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("CONCURRENT_MODIFICATION", "La orden fue modificada concurrentemente; reintente la operación"))`. Add test `acceptOrder_whenConcurrentModification_returns409` to `OrderControllerIT.java`: configure `acceptOrderUseCase` mock to throw `new ObjectOptimisticLockingFailureException(Order.class, UUID.randomUUID())`; assert HTTP 409 and `code == "CONCURRENT_MODIFICATION"`. Add test `getVersion_returnsInitialVersion` to `OrderTest.java`: build an `Order` instance and assert `getVersion() == 0` to bring `version` field coverage above zero in `src/main/java/org/ups/dropshippingservice/adapter/in/web/GlobalExceptionHandler.java`, `src/test/java/org/ups/dropshippingservice/adapter/web/OrderControllerIT.java`, and `src/test/java/org/ups/dropshippingservice/domain/OrderTest.java`

**Checkpoint**: `./gradlew test` green — EC-02 and EC-04 tests pass; `Order.getVersion()` coverage > 0%; global JaCoCo coverage ≥ 80% maintained.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
  - T001 → T002 → T003 are SEQUENTIAL (all modify `build.gradle`)
  - T004, T005, T006 are PARALLEL with each other and with T001–T003 (different files)
- **Foundational (Phase 2)**: Depends on Phase 1 completion
  - T007–T010 parallel; T011 waits for T007–T010; T012–T015 parallel after any point
  - T016–T018 wait for T011 and T012 respectively; T019 waits for T007–T010
  - T020 waits for T019; T021 waits for T011 + T019; T022 waits for T016, T017, T020, T021
  - T023 waits for T012, T018; T024–T025 parallel at any point
- **User Stories (Phases 3–5)**: Depend on Phase 2; phases must run P1 → P2 → P3 (US2 reuses controller from US1)
- **Polish (Phase 6)**: All user story phases complete

### User Story Dependencies

- **US1 (P1)**: Independent — first story; builds controller, mapper, exception handler, config
- **US2 (P2)**: Depends on Phase 3 complete — adds method to existing `OrderController` and `ApplicationBeanConfig`
- **US3 (P3)**: Depends on Phase 4 complete — adds method to existing `OrderController` and `ApplicationBeanConfig`

### Within Each User Story

1. Write feature file + unit tests FIRST (Red — must FAIL before implementation)
2. Create input port interface
3. Implement service (Green for service unit tests)
4. Implement controller method (Green for integration tests)
5. Write BDD step definitions
6. Confirm all tests pass before moving to next story

### Parallel Opportunities (Phase 2 core group)

```bash
# Launch value objects in parallel:
T007 OrderStatus.java
T008 OrderProduct.java
T009 DeliveryAddress.java
T010 CustomerContact.java

# After T007–T010 complete, these can also run together:
T012 OrderRejectedEvent.java
T013 OrderNotFoundException.java
T014 OrderAlreadyProcessedException.java
T015 InvalidDispatchDateException.java

# After T011 (Order.java) complete:
T016 LoadOrderPort.java
T017 SaveOrderPort.java
T018 NotifyRejectionPort.java
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001–T006)
2. Complete Phase 2: Foundational (T007–T025)
3. Complete Phase 3: User Story 1 (T026–T037) — tests first
4. **STOP and VALIDATE**: `./gradlew test` → green; `GET /api/v1/providers/prov-001/orders` → HTTP 200 ✅
5. Demo/deploy if ready

### Incremental Delivery

1. Phase 1 + Phase 2 → Infrastructure ready (SQL schema + data, domain, adapters)
2. Phase 3 (US1) → MVP: providers see their orders
3. Phase 4 (US2) → Providers accept orders
4. Phase 5 (US3) → Providers reject orders + commercial team alerted
5. Phase 6 → Quality gates verified

---

## Notes

- **T001–T003 MUST run sequentially** — they modify the same `build.gradle` file
- **[P] tasks are safe to parallelize** — each operates on a distinct file
- **BDD feature files MUST be written and FAIL before any service/controller code** (constitution Principle II)
- **`Order.java` (T011) MUST have zero Spring/JPA annotations** — framework leakage into domain violates constitution Principle I
- **`schema.sql` columns MUST match `@Column` names in `OrderJpaEntity`** — T005 and T019 must be kept in sync
- **Generated code in `build/generated/` is excluded from JaCoCo** (configured in T002)
- **`data.sql` UUIDs MUST match the IDs used in `quickstart.md`** (T006) for reproducible manual validation
