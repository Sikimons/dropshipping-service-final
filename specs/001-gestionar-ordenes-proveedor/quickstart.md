# Quickstart — Ver y Gestionar Órdenes Asignadas

**Feature**: 001-gestionar-ordenes-proveedor
**Date**: 2026-07-04

Guía para ejecutar y validar el funcionamiento de la feature US-01 end-to-end.

---

## Prerequisites

| Requisito | Detalle |
|-----------|---------|
| Java 26+ | `java -version` |
| Gradle Wrapper | incluido en el proyecto (`./gradlew`) |
| H2 (in-memory) | configurado automáticamente por Spring Boot |
| curl o Postman | para llamadas REST manuales |

---

## Build & Run

```bash
# Desde la raíz del proyecto

# 1. Compilar y generar stubs desde el contrato OpenAPI
./gradlew openApiGenerate compileJava

# 2. Ejecutar todos los tests (unitarios + integración + BDD funcionales)
./gradlew test

# 3. Ver reporte de cobertura JaCoCo
#    Abre: build/reports/jacoco/test/html/index.html

# 4. Levantar el servicio localmente
./gradlew bootRun
# Servidor disponible en http://localhost:8080
```

---

## Scenario 1 — Visualizar Órdenes Asignadas (US1 / P1)

**Objetivo**: Verificar que el proveedor `prov-001` ve la orden `ORD-20260704-001` con todos los campos.

```bash
curl -s http://localhost:8080/api/v1/providers/prov-001/orders | python -m json.tool
```

**Resultado esperado** — HTTP 200, array con al menos una orden que contenga:
- `orderCode` = `"ORD-20260704-001"`
- `status` = `"PENDIENTE"`
- `product.productCode` = `"PROD-001"`
- `deliveryAddress.city` = `"Lima"`
- `customerContact.name` presente y no vacío
- `expectedDeliveryDate` presente

**Resultado esperado con proveedor diferente** — acceder con `prov-999` que no tiene órdenes asignadas:

```bash
curl -s http://localhost:8080/api/v1/providers/prov-999/orders
# Espera: HTTP 200 con array vacío []
```

---

## Scenario 2 — Aceptar una Orden (US2 / P2)

**Prerequisito**: La orden debe estar en estado `PENDIENTE`.

```bash
# Obtener el ID de la orden desde el Scenario 1
ORDER_ID="550e8400-e29b-41d4-a716-446655440000"

curl -s -X PUT http://localhost:8080/api/v1/orders/$ORDER_ID/accept \
  -H "Content-Type: application/json" \
  -d '{"estimatedDispatchDate": "2026-07-10"}' | python -m json.tool
```

**Resultado esperado** — HTTP 200:
- `status` = `"ACEPTADO"`
- `estimatedDispatchDate` = `"2026-07-10"`
- `lastActionBy` presente (identifica al proveedor)
- `lastActionAt` presente (timestamp ISO-8601)

**Resultado esperado — intento de aceptar con fecha pasada**:

```bash
curl -s -X PUT http://localhost:8080/api/v1/orders/$ORDER_ID/accept \
  -H "Content-Type: application/json" \
  -d '{"estimatedDispatchDate": "2020-01-01"}'
# Espera: HTTP 400, body { "code": "INVALID_DISPATCH_DATE", "message": "..." }
```

**Resultado esperado — intento de aceptar una orden ya procesada**:

```bash
# Misma llamada de aceptación sobre la misma orden (ya ACEPTADA)
# Espera: HTTP 409, body { "code": "ORDER_ALREADY_PROCESSED", "message": "..." }
```

---

## Scenario 3 — Rechazar una Orden (US3 / P3)

**Prerequisito**: Usar una orden diferente en estado `PENDIENTE` (crear una nueva en la BD de prueba o resetear la aplicación).

```bash
ORDER_ID_2="660e8400-e29b-41d4-a716-446655440001"

curl -s -X PUT http://localhost:8080/api/v1/orders/$ORDER_ID_2/reject \
  -H "Content-Type: application/json" \
  -d '{"rejectionReason": "Producto sin stock disponible"}' | python -m json.tool
```

**Resultado esperado** — HTTP 200:
- `status` = `"RECHAZADO"`
- `rejectionReason` = `"Producto sin stock disponible"`
- `lastActionBy` presente
- `lastActionAt` presente

**Verificar alerta al equipo comercial**:
- En el log del servidor debe aparecer una entrada con nivel INFO o superior del tipo:
  `[COMMERCIAL-ALERT] Order ORD-XXXX rejected by prov-001. Reason: Producto sin stock disponible`
- En v1 la notificación es estructurada en log; en producción se reemplaza por el canal real.

**Resultado esperado — rechazo sin motivo**:

```bash
curl -s -X PUT http://localhost:8080/api/v1/orders/$ORDER_ID_2/reject \
  -H "Content-Type: application/json" \
  -d '{"rejectionReason": ""}'
# Espera: HTTP 400, body { "code": "REJECTION_REASON_REQUIRED", "message": "..." }
```

---

## BDD Test Execution

Los escenarios anteriores están automatizados como pruebas Cucumber en:

```
src/test/resources/features/
├── us1_ver_ordenes_asignadas.feature
├── us2_aceptar_orden.feature
└── us3_rechazar_orden.feature
```

Para ejecutarlos:

```bash
./gradlew test --tests "*CucumberRunnerIT"
# Reporte HTML: build/reports/tests/test/index.html
# Reporte Cucumber: build/reports/cucumber/
```

---

## Coverage Validation

```bash
./gradlew test jacocoTestReport jacocoTestCoverageVerification
```

- El build falla si cobertura global < 80% o cualquier clase < 80%.
- Reportes en: `build/reports/jacoco/test/html/index.html`

---

## OpenAPI Contract Reference

- Contrato fuente: `specs/001-gestionar-ordenes-proveedor/contracts/openapi.yaml`
- Stubs generados: `build/generated/src/main/java/org/ups/dropshippingservice/adapter/in/web/generated/`
- El contrato es la fuente de verdad. Cualquier cambio en endpoints DEBE modificar el YAML primero.
