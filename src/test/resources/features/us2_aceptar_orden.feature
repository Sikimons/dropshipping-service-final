# language: es
Característica: Aceptar una orden asignada
  Como proveedor autenticado
  Quiero aceptar una orden en estado PENDIENTE proporcionando fecha estimada de despacho
  Para confirmar que puedo cumplir el pedido

  Escenario: Aceptar orden con fecha válida futura
    Dado que el header X-Provider-Id es "prov-001"
    Y existe una orden PENDIENTE con id "550e8400-e29b-41d4-a716-446655440000" para "prov-001"
    Cuando el proveedor acepta la orden "550e8400-e29b-41d4-a716-446655440000" con fecha "2026-12-01"
    Entonces la respuesta HTTP es 200
    Y el estado de la orden en la respuesta es "ACEPTADO"

  Escenario: Rechazar aceptación con fecha en el pasado
    Dado que el header X-Provider-Id es "prov-001"
    Y existe una orden PENDIENTE con id "550e8400-e29b-41d4-a716-446655440001" para "prov-001"
    Cuando el proveedor acepta la orden "550e8400-e29b-41d4-a716-446655440001" con fecha "2020-01-01"
    Entonces la respuesta HTTP es 400
    Y el código de error es "INVALID_DISPATCH_DATE"

  Escenario: Conflicto al intentar aceptar una orden ya procesada
    Dado que el header X-Provider-Id es "prov-001"
    Y la orden "550e8400-e29b-41d4-a716-446655440000" ya fue aceptada
    Cuando el proveedor acepta la orden "550e8400-e29b-41d4-a716-446655440000" con fecha "2026-12-01"
    Entonces la respuesta HTTP es 409
    Y el código de error es "ORDER_ALREADY_PROCESSED"

  Escenario: Orden no encontrada al intentar aceptar
    Dado que el header X-Provider-Id es "prov-001"
    Cuando el proveedor acepta la orden "00000000-0000-0000-0000-000000000099" con fecha "2026-12-01"
    Entonces la respuesta HTTP es 404
