# Data Model: Ver y Gestionar Órdenes Asignadas

**Feature**: 001-gestionar-ordenes-proveedor
**Date**: 2026-07-04

## Entity: Order (Orden Dropshipping)

**Layer**: Domain (Entities)
**Package**: `org.ups.dropshippingservice.domain`

### Fields

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `id` | UUID | Yes | PK, immutable | System-generated identifier |
| `orderCode` | String | Yes | Non-blank, unique | Business-readable order code (e.g., ORD-20260704-001) |
| `providerId` | String | Yes | Non-blank | ID of the provider assigned to this order |
| `status` | OrderStatus | Yes | PENDIENTE \| ACEPTADO \| RECHAZADO | Current lifecycle state |
| `product` | OrderProduct | Yes | Embedded value object | Product details |
| `deliveryAddress` | DeliveryAddress | Yes | Embedded value object | Full delivery address |
| `customerContact` | CustomerContact | Yes | Embedded value object | Customer contact info |
| `expectedDeliveryDate` | LocalDate | Yes | Present or future at order creation | Date the client expects delivery |
| `specialConditions` | String | No | Nullable | Free-text special handling instructions |
| `estimatedDispatchDate` | LocalDate | No | Nullable; required on accept; must be ≥ today | Dispatch date committed by provider |
| `rejectionReason` | String | No | Nullable; required on reject; non-blank | Reason given by provider for rejection |
| `lastActionBy` | String | No | Nullable; set on accept/reject | Provider identifier who last acted |
| `lastActionAt` | Instant | No | Nullable; set on accept/reject | Timestamp of last action |
| `version` | Long | Yes | Optimistic lock version; managed by JPA | Concurrency control |

### State Transitions

```
PENDIENTE ──[accept(estimatedDispatchDate)]──► ACEPTADO
PENDIENTE ──[reject(rejectionReason)]────────► RECHAZADO
ACEPTADO  ──(terminal — no transitions allowed)
RECHAZADO ──(terminal — no transitions allowed)
```

**Guard rule**: Any attempt to call `accept()` or `reject()` on an order not in `PENDIENTE` state
MUST throw `OrderAlreadyProcessedException`.

### Business Rules

1. `accept()` sets `status = ACEPTADO`, `estimatedDispatchDate`, `lastActionBy`, `lastActionAt`.
2. `reject()` sets `status = RECHAZADO`, `rejectionReason`, `lastActionBy`, `lastActionAt`.
3. `estimatedDispatchDate` MUST be today or in the future (validated in Use Case before calling domain method).
4. `rejectionReason` MUST be non-blank (validated in Use Case).

---

## Value Object: OrderProduct

**Layer**: Domain
**Embedded in**: Order

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `productCode` | String | Yes | Non-blank |
| `description` | String | Yes | Non-blank |
| `quantity` | int | Yes | ≥ 1 |

---

## Value Object: DeliveryAddress

**Layer**: Domain
**Embedded in**: Order

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `street` | String | Yes | Non-blank |
| `city` | String | Yes | Non-blank |
| `state` | String | Yes | Non-blank |
| `postalCode` | String | Yes | Non-blank |
| `country` | String | Yes | Non-blank |

---

## Value Object: CustomerContact

**Layer**: Domain
**Embedded in**: Order

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `name` | String | Yes | Non-blank |
| `phone` | String | Yes | Non-blank |
| `email` | String | No | Nullable; valid email format if present |

---

## Enum: OrderStatus

**Layer**: Domain

| Value | Meaning |
|-------|---------|
| `PENDIENTE` | Order assigned, awaiting provider decision |
| `ACEPTADO` | Provider confirmed with estimated dispatch date |
| `RECHAZADO` | Provider declined with rejection reason |

---

## Domain Event: OrderRejectedEvent

**Layer**: Domain
**Published by**: `RejectOrderService` (Use Case)
**Consumed by**: `CommercialTeamNotificationAdapter` (Frameworks & Drivers)

| Field | Type | Description |
|-------|------|-------------|
| `orderId` | UUID | The rejected order's ID |
| `orderCode` | String | Business-readable code |
| `customerName` | String | Client name for alert content |
| `productCode` | String | Product code for alert content |
| `rejectionReason` | String | Reason to include in alert |
| `rejectedAt` | Instant | Timestamp of rejection |

---

## JPA Entity: OrderJpaEntity

**Layer**: Frameworks & Drivers (Persistence Adapter)
**Package**: `org.ups.dropshippingservice.adapter.out.persistence`
**Table**: `orders`

Maps 1:1 to the `Order` domain entity via `OrderPersistenceMapper`.
Annotations: `@Entity`, `@Table(name = "orders")`, `@Version` for optimistic locking.
Value objects (`OrderProduct`, `DeliveryAddress`, `CustomerContact`) are mapped as `@Embedded`.

---

## Repository Port Interfaces

**Layer**: Application (Output Ports)

```
LoadOrderPort
  findByIdAndProviderId(UUID orderId, String providerId): Optional<Order>
  findAllByProviderId(String providerId): List<Order>

SaveOrderPort
  save(Order order): Order
```

---

## Relationship Summary

```
Order (1) ──── (1) OrderProduct       [embedded]
Order (1) ──── (1) DeliveryAddress    [embedded]
Order (1) ──── (1) CustomerContact    [embedded]
Order ────────────► OrderRejectedEvent [published on reject]
```
