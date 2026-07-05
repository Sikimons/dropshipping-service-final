# language: es
Característica: Rechazar una orden asignada
  Como proveedor autenticado
  Quiero rechazar una orden en estado PENDIENTE indicando el motivo
  Para que el equipo comercial ofrezca una alternativa al cliente

  Escenario: Rechazar orden con motivo válido
    Dado que el header X-Provider-Id es "prov-001"
    Y existe una orden PENDIENTE con id "550e8400-e29b-41d4-a716-446655440001" para "prov-001"
    Cuando el proveedor rechaza la orden "550e8400-e29b-41d4-a716-446655440001" con motivo "Producto sin stock disponible"
    Entonces la respuesta HTTP es 200
    Y el estado de la orden en la respuesta es "RECHAZADO"

  Escenario: Error al rechazar sin motivo
    Dado que el header X-Provider-Id es "prov-001"
    Y existe una orden PENDIENTE con id "550e8400-e29b-41d4-a716-446655440001" para "prov-001"
    Cuando el proveedor rechaza la orden "550e8400-e29b-41d4-a716-446655440001" con motivo ""
    Entonces la respuesta HTTP es 400
    Y el código de error es "REJECTION_REASON_REQUIRED"

  Escenario: Conflicto al intentar rechazar una orden ya procesada
    Dado que el header X-Provider-Id es "prov-001"
    Y la orden "550e8400-e29b-41d4-a716-446655440000" ya fue aceptada
    Cuando el proveedor rechaza la orden "550e8400-e29b-41d4-a716-446655440000" con motivo "Sin stock"
    Entonces la respuesta HTTP es 409
    Y el código de error es "ORDER_ALREADY_PROCESSED"
