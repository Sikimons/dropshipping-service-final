# Implementation Plan: Ver y Gestionar Órdenes Asignadas

**Branch**: `001-gestionar-ordenes-proveedor` | **Date**: 2026-07-04 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/001-gestionar-ordenes-proveedor/spec.md`

## Summary

Implementar la funcionalidad US-01 que permite a un proveedor: (1) ver las órdenes dropshipping
asignadas con información completa del producto, dirección y cliente; (2) aceptar una orden
indicando fecha estimada de despacho; (3) rechazar una orden con motivo, disparando alerta al
equipo comercial. Arquitectura: Clean Architecture con Spring Boot 4.1.0 / Java 26. API diseñada
mediante contrato OpenAPI 3.1 con generación de stubs vía openapi-generator. Tests BDD con Cucumber.
Cobertura ≥ 80% verificada con JaCoCo en el pipeline Gradle.

## Technical Context

**Language/Version**: Java 26

**Primary Dependencies**: Spring Boot 4.1.0 (spring-boot-starter-webmvc, spring-boot-starter-data-jpa),
Lombok, Cucumber-JVM 7.18.0, openapi-generator Gradle plugin 7.6.0, JaCoCo 0.8.12

**Storage**: H2 in-memory (development & test); Spring Data JPA

**Testing**: JUnit 5 (via Spring Boot test starters), Cucumber-JVM (BDD), Spring Boot Test

**Target Platform**: JVM — Linux server / local JVM; REST web service

**Project Type**: web-service (REST API)

**Performance Goals**: Órdenes visibles < 3s (SC-001); cambio de estado visible < 5s (SC-003);
alerta de rechazo enviada < 60s (SC-004)

**Constraints**: Cobertura por clase > 80%, cobertura global ≥ 80% (JaCoCo); sin anotaciones de
framework en capas de dominio ni casos de uso; contrato OpenAPI previo a toda implementación

**Scale/Scope**: Servicio único, feature US-01 (3 user stories, 10 FRs)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **I. Clean Architecture**: Capas identificadas: `domain/` (Entities), `application/` (Use Cases),
  `adapter/` (Interface Adapters), `infrastructure/` (Frameworks & Drivers). La regla de dependencia
  apunta hacia adentro: `adapter` → `application` → `domain`. Spring Boot no penetra en `domain` ni
  `application`.
- [x] **II. BDD Testing**: Cada uno de los 9 Acceptance Scenarios de spec.md tiene un escenario
  Cucumber correspondiente. Los tests se escriben primero (Red) antes de implementar (Green).
  Tres tipos: unit (dominio/usecase), integración (adaptadores), funcionales (Cucumber E2E).
- [x] **III. Best Practices**: Cada servicio de Use Case tiene responsabilidad única (SRP). Los
  puertos de salida permiten extensión sin modificación (OCP). No hay abstracciones especulativas
  (YAGNI — sin EventBus, sin caching, sin paginación no solicitada). Lógica de dominio sin
  duplicación entre servicios (DRY).
- [x] **IV. API First**: Contrato `contracts/openapi.yaml` creado antes de toda tarea de
  implementación. El plugin `openapi-generator` generará interfaces que los controladores
  implementarán. Cualquier cambio en endpoints modifica el YAML primero.
- [x] **V. Quality Gates**: JaCoCo 0.8.12 configurado con `jacocoTestCoverageVerification`; build
  falla si clase < 80% o global < 80%. Código generado (`build/generated/`) excluido de métricas.
  Reportes en `build/reports/jacoco/test/html/`.

## Project Structure

### Documentation (this feature)

```text
specs/001-gestionar-ordenes-proveedor/
├── plan.md              # Este archivo
├── spec.md              # Especificación de feature
├── research.md          # Decisiones de investigación
├── data-model.md        # Modelo de datos y entidades
├── quickstart.md        # Guía de validación E2E
├── contracts/
│   └── openapi.yaml     # Contrato OpenAPI 3.1 (fuente de verdad)
├── checklists/
│   └── requirements.md  # Checklist de calidad de spec
└── tasks.md             # Generado por /speckit-tasks (pendiente)
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/org/ups/dropshippingservice/
│   │   ├── domain/                          # Layer 1: Entities (pure Java, NO framework deps)
│   │   │   ├── Order.java
│   │   │   ├── OrderStatus.java
│   │   │   ├── OrderProduct.java
│   │   │   ├── DeliveryAddress.java
│   │   │   ├── CustomerContact.java
│   │   │   └── event/
│   │   │       └── OrderRejectedEvent.java
│   │   ├── application/                     # Layer 2: Use Cases
│   │   │   ├── port/
│   │   │   │   ├── in/
│   │   │   │   │   ├── GetAssignedOrdersUseCase.java
│   │   │   │   │   ├── AcceptOrderUseCase.java
│   │   │   │   │   └── RejectOrderUseCase.java
│   │   │   │   └── out/
│   │   │   │       ├── LoadOrderPort.java
│   │   │   │       ├── SaveOrderPort.java
│   │   │   │       └── NotifyRejectionPort.java
│   │   │   ├── service/
│   │   │   │   ├── GetAssignedOrdersService.java
│   │   │   │   ├── AcceptOrderService.java
│   │   │   │   └── RejectOrderService.java
│   │   │   └── exception/
│   │   │       ├── OrderNotFoundException.java
│   │   │       ├── OrderAlreadyProcessedException.java
│   │   │       └── InvalidDispatchDateException.java
│   │   ├── adapter/                         # Layer 3: Interface Adapters
│   │   │   ├── in/
│   │   │   │   └── web/
│   │   │   │       ├── OrderController.java          # Implements generated API interface
│   │   │   │       ├── OrderControllerMapper.java
│   │   │   │       └── GlobalExceptionHandler.java
│   │   │   └── out/
│   │   │       ├── persistence/
│   │   │       │   ├── OrderJpaEntity.java
│   │   │       │   ├── SpringDataOrderRepository.java
│   │   │       │   ├── OrderPersistenceAdapter.java  # Implements LoadOrderPort + SaveOrderPort
│   │   │       │   └── OrderPersistenceMapper.java
│   │   │       └── notification/
│   │   │           └── LogNotificationAdapter.java   # Implements NotifyRejectionPort
│   │   └── infrastructure/                  # Layer 4: Frameworks & Drivers (Spring config)
│   │       └── config/
│   │           └── ApplicationBeanConfig.java
│   └── resources/
│       ├── application.properties           # Datasource, JPA, SQL init config
│       └── db/
│           ├── schema.sql                   # DDL: CREATE TABLE orders (with all columns)
│           └── data.sql                     # DML: INSERT sample orders for prov-001, prov-002
│
└── test/
    └── java/org/ups/dropshippingservice/
        ├── domain/
        │   └── OrderTest.java
        ├── application/
        │   ├── GetAssignedOrdersServiceTest.java
        │   ├── AcceptOrderServiceTest.java
        │   └── RejectOrderServiceTest.java
        ├── architecture/
        │   └── CleanArchitectureTest.java
        ├── adapter/
        │   ├── web/
        │   │   └── OrderControllerIT.java
        │   └── persistence/
        │       └── OrderPersistenceAdapterIT.java
        └── bdd/
            ├── CucumberRunnerIT.java
            ├── steps/
            │   ├── VerOrdenesSteps.java
            │   ├── AceptarOrdenSteps.java
            │   └── RechazarOrdenSteps.java
            └── config/
                └── CucumberSpringContextConfig.java

src/test/resources/
└── features/
    ├── us1_ver_ordenes_asignadas.feature
    ├── us2_aceptar_orden.feature
    └── us3_rechazar_orden.feature
```

**Structure Decision**: Single Spring Boot project. Clean Architecture 4-layer package structure
under `org.ups.dropshippingservice`. Schema and seed data managed via `src/main/resources/db/`
SQL scripts (Spring Boot auto-init); no `DataLoader` bean needed. Tests organized by layer
(unit → service → integration → BDD).

## Complexity Tracking

> No violations detected — Constitution Check passes on all 5 principles.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|
| — | — | — |

---

## Edge Case Decisions

### EC-02 — Fallo del sistema de alertas al rechazar *(resolved)*

**Decision**: Fail-open — el rechazo persiste aunque `NotifyRejectionPort` lance una excepción en tiempo de ejecución.

**Rationale**: El cambio de estado de la orden a `RECHAZADO` es la operación primaria y debe completarse independientemente del canal de notificación. La alerta al equipo comercial es un efecto secundario de calidad de servicio (SC-004 / FR-007). Bloquear o revertir el rechazo ante un fallo de notificación genera una experiencia operativa degradada sin beneficio de consistencia de negocio. YAGNI descarta mecanismos de retry/outbox en esta versión.

**Implementation impact**: `RejectOrderService` envuelve la llamada a `notifyRejectionPort.notifyRejection(event)` en un bloque `try-catch (RuntimeException)` que registra `WARN` con el error y no relanza la excepción. La persistencia del rechazo (`saveOrderPort.save(order)`) ocurre antes de la notificación y no forma parte de la misma transacción de notificación.

**Test required**: `rejectOrder_whenNotificationFails_rejectionStillPersists` en `RejectOrderServiceTest` (ver T057).

---

### EC-04 — Acceso concurrente a la misma orden *(resolved)*

**Decision**: Optimistic locking vía `@Version` (ya configurado en `OrderJpaEntity`). Si dos sesiones concurrentes intentan procesar la misma orden, la segunda recibe HTTP 409 con código `CONCURRENT_MODIFICATION`.

**Rationale**: El campo `version BIGINT NOT NULL DEFAULT 0` ya existe en `schema.sql` y `OrderJpaEntity` ya declara `@Version`. Spring Data JPA lanza `ObjectOptimisticLockingFailureException` automáticamente cuando el número de versión no coincide. No se requiere ningún cambio en el modelo de datos ni en la capa de dominio. Solo falta capturar la excepción en `GlobalExceptionHandler` y agregar el código de error al contrato OpenAPI (el HTTP 409 ya está documentado en ambos endpoints).

**Implementation impact**: Agregar `@ExceptionHandler(ObjectOptimisticLockingFailureException.class)` en `GlobalExceptionHandler` → HTTP 409 `ErrorResponse("CONCURRENT_MODIFICATION", "La orden fue modificada concurrentemente; reintente la operación")`. Actualizar el contrato OpenAPI con el nuevo código de error en las respuestas 409 de accept y reject. Cubrir `Order.getVersion()` en `OrderTest` (actualmente 0%).
