# Feature Specification: Ver y Gestionar Órdenes Asignadas

**Feature Branch**: `001-gestionar-ordenes-proveedor`

**Created**: 2026-07-04

**Status**: Draft

**Épica**: E-01 | **Story**: US-01 | **Puntos**: 5

**Input**: Como Proveedor, quiero ver mis órdenes asignadas con información completa y aceptarlas o rechazarlas, para operar desde un solo canal sin depender del correo.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Visualizar Órdenes Asignadas (Priority: P1)

El proveedor accede al portal y ve la lista de órdenes dropshipping que le han sido asignadas. Cada orden muestra toda la información necesaria para tomar una decisión operativa: código de producto, descripción, cantidad, dirección completa de entrega, contacto del cliente, fecha esperada de entrega y condiciones especiales.

**Why this priority**: Sin visibilidad de las órdenes, el proveedor no puede operar. Es el punto de entrada que habilita todas las demás acciones.

**Independent Test**: Se puede verificar de forma independiente accediendo al portal con credenciales de proveedor y comprobando que las órdenes asignadas aparecen con todos sus campos.

**Acceptance Scenarios**:

1. **Dado que** el sistema ha generado una orden Dropshipping y la ha asignado al proveedor, **cuando** el proveedor autenticado accede al portal de órdenes, **entonces** el sistema muestra la orden con los siguientes datos: código de producto, descripción, cantidad, dirección completa de entrega, información de contacto del cliente, fecha esperada de entrega y condiciones especiales.
2. **Dado que** el proveedor está en el portal, **cuando** no tiene órdenes asignadas, **entonces** el sistema muestra un mensaje informativo indicando que no hay órdenes pendientes.
3. **Dado que** el proveedor está en el portal, **cuando** intenta acceder a una orden asignada a otro proveedor, **entonces** el sistema deniega el acceso y muestra un error de autorización.

---

### User Story 2 - Aceptar una Orden (Priority: P2)

El proveedor revisa una orden asignada y decide aceptarla. Ingresa la fecha estimada de despacho y confirma. El sistema registra la aceptación con el actor y el timestamp, y cambia el estado de la orden a "Aceptado". El analista puede ver la actualización de estado en tiempo real.

**Why this priority**: La aceptación es la acción más frecuente y habilita el flujo operativo principal del canal dropshipping.

**Independent Test**: Se puede verificar de forma independiente aceptando una orden en estado "Pendiente" y comprobando que el estado cambia a "Aceptado" con actor y timestamp registrados, y que el analista lo ve reflejado.

**Acceptance Scenarios**:

1. **Dado que** existe una orden en estado "Pendiente" asignada al proveedor, **cuando** el proveedor confirma la orden proporcionando una fecha estimada de despacho válida, **entonces** el estado de la orden cambia a "Aceptado", se registra el identificador del proveedor y el timestamp de la acción, y el cambio es visible para el analista.
2. **Dado que** el proveedor intenta aceptar una orden, **cuando** no proporciona una fecha estimada de despacho, **entonces** el sistema rechaza la acción y muestra un mensaje indicando que la fecha es obligatoria.
3. **Dado que** el proveedor intenta aceptar una orden, **cuando** proporciona una fecha estimada de despacho anterior a la fecha actual, **entonces** el sistema rechaza la acción y solicita una fecha futura válida.

---

### User Story 3 - Rechazar una Orden (Priority: P3)

El proveedor revisa una orden asignada y decide rechazarla. Ingresa el motivo del rechazo y confirma. El sistema cambia el estado de la orden a "Rechazado" y envía una alerta inmediata al equipo comercial para que ofrezca una alternativa al cliente.

**Why this priority**: El rechazo es un caso de excepción crítico: sin alerta inmediata al equipo comercial, el cliente queda sin atención.

**Independent Test**: Se puede verificar de forma independiente rechazando una orden en estado "Pendiente" y comprobando que el estado cambia a "Rechazado" y que el equipo comercial recibe la alerta dentro del tiempo definido.

**Acceptance Scenarios**:

1. **Dado que** existe una orden en estado "Pendiente" asignada al proveedor, **cuando** el proveedor rechaza la orden indicando un motivo, **entonces** el estado de la orden cambia a "Rechazado" y el equipo comercial recibe una alerta inmediata con los datos de la orden y el motivo del rechazo.
2. **Dado que** el proveedor intenta rechazar una orden, **cuando** no proporciona un motivo de rechazo, **entonces** el sistema rechaza la acción y muestra un mensaje indicando que el motivo es obligatorio.
3. **Dado que** la orden ha sido rechazada, **cuando** el equipo comercial recibe la alerta, **entonces** la alerta contiene el código de orden, el nombre del cliente, el producto y el motivo del rechazo para facilitar la gestión de alternativa.

---

### Edge Cases

- ¿Qué ocurre si el proveedor intenta aceptar o rechazar una orden que ya fue aceptada o rechazada previamente?
- ¿Qué sucede si el sistema de alertas al equipo comercial no está disponible en el momento del rechazo?
- ¿Puede un proveedor ver el historial de órdenes ya procesadas (aceptadas/rechazadas), o solo las pendientes?
- ¿Qué sucede si dos usuarios del proveedor intentan procesar la misma orden simultáneamente?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE mostrar al proveedor autenticado únicamente las órdenes dropshipping que le han sido asignadas.
- **FR-002**: Cada orden mostrada DEBE incluir: código de producto, descripción del producto, cantidad solicitada, dirección completa de entrega, nombre y datos de contacto del cliente, fecha esperada de entrega y condiciones especiales.
- **FR-003**: El proveedor DEBE poder aceptar una orden en estado "Pendiente" proporcionando una fecha estimada de despacho futura.
- **FR-004**: Al aceptar, el sistema DEBE cambiar el estado de la orden a "Aceptado", registrar el identificador del proveedor que realizó la acción y el timestamp exacto de la operación.
- **FR-005**: El cambio de estado a "Aceptado" DEBE ser accesible para el analista mediante consulta REST al endpoint de órdenes dentro de los 5 segundos posteriores a la acción del proveedor. La visibilidad se satisface mediante polling periódico; no se requiere mecanismo push en esta versión (ver SC-003).
- **FR-006**: El proveedor DEBE poder rechazar una orden en estado "Pendiente" proporcionando un motivo de rechazo no vacío.
- **FR-007**: Al rechazar, el sistema DEBE cambiar el estado de la orden a "Rechazado", registrar el identificador del proveedor que realizó la acción y el timestamp exacto de la operación, y enviar una alerta al equipo comercial dentro de los 60 segundos siguientes.
- **FR-008**: La alerta al equipo comercial DEBE contener: código de orden, nombre del cliente, producto solicitado y motivo de rechazo.
- **FR-009**: El sistema DEBE impedir que un proveedor acceda, acepte o rechace órdenes no asignadas a él.
- **FR-010**: El sistema DEBE impedir que una orden ya en estado "Aceptado" o "Rechazado" sea modificada nuevamente.

### Key Entities *(include if feature involves data)*

- **Orden Dropshipping**: Representa el pedido generado por el sistema y asignado a un proveedor. Atributos clave: código de orden, código de producto, descripción, cantidad, dirección de entrega, contacto del cliente, fecha esperada, condiciones especiales, estado (Pendiente/Aceptado/Rechazado), fecha estimada de despacho (cuando aplica), motivo de rechazo (cuando aplica), actor de la última acción, timestamp de la última acción.
- **Proveedor**: Actor que opera en el portal. Tiene credenciales de acceso y un conjunto de órdenes asignadas.
- **Analista**: Actor que monitorea el estado de las órdenes en tiempo real.
- **Equipo Comercial**: Actor que recibe alertas de rechazo para gestionar alternativas al cliente.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El proveedor puede visualizar todas sus órdenes asignadas en menos de 3 segundos desde el acceso al portal.
- **SC-002**: El proveedor puede completar la acción de aceptar o rechazar una orden en menos de 2 minutos.
- **SC-003**: Los cambios de estado (Aceptado/Rechazado) son accesibles para el analista en menos de 5 segundos tras la acción del proveedor mediante polling REST (ver FR-005). No se requiere mecanismo push en v1.
- **SC-004**: El equipo comercial recibe la alerta de rechazo en menos de 60 segundos tras la acción del proveedor.
- **SC-005**: El 100% de las órdenes asignadas muestran todos los campos requeridos por FR-002.
- **SC-006**: Cero órdenes de otros proveedores son accesibles o modificables por un proveedor no asignado.

## Assumptions

- El sistema de autenticación de proveedores es preexistente; esta feature reutiliza la identidad del proveedor autenticado sin implementar un nuevo mecanismo de login.
- Las órdenes Dropshipping son generadas y asignadas a proveedores por un proceso externo o un módulo separado del sistema; esta feature solo consume las órdenes ya creadas.
- El "equipo comercial" dispone de un canal de notificación ya configurado (correo electrónico, sistema interno de alertas o similar); esta feature dispara la notificación pero no construye el canal.
- El "analista" accede al portal a través de un rol diferente al de proveedor, con una vista propia del estado de las órdenes.
- Una orden puede estar en uno de tres estados mutuamente excluyentes: Pendiente, Aceptado, Rechazado. No existen subestados en el alcance de esta feature.
- El soporte para dispositivos móviles queda fuera del alcance de esta feature (v1 es portal web de escritorio).
- La identidad del proveedor autenticado se transmite mediante el header HTTP `X-Provider-Id`, inyectado por la capa de autenticación preexistente (API Gateway). Este servicio confía en dicho header sin re-validar credenciales.
- SC-003 se satisface mediante polling REST: el analista consulta el endpoint `GET /providers/{providerId}/orders` periódicamente con un intervalo ≤ 5 segundos. No se implementa WebSocket ni SSE en esta versión.
