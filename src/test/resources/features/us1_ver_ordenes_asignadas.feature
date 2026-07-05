# language: es
Característica: Ver órdenes asignadas al proveedor
  Como proveedor autenticado
  Quiero ver las órdenes dropshipping asignadas a mí
  Para operar desde un solo canal

  Escenario: Ver órdenes asignadas cuando el proveedor tiene órdenes
    Dado que el header X-Provider-Id es "prov-001"
    Cuando el proveedor consulta GET /api/v1/providers/prov-001/orders
    Entonces la respuesta HTTP es 200
    Y la respuesta contiene una lista con al menos 1 orden
    Y cada orden incluye código de producto, descripción, cantidad, dirección, contacto y fecha esperada

  Escenario: Lista vacía cuando el proveedor no tiene órdenes
    Dado que el header X-Provider-Id es "prov-999"
    Cuando el proveedor consulta GET /api/v1/providers/prov-999/orders
    Entonces la respuesta HTTP es 200
    Y la respuesta contiene una lista vacía

  Escenario: Acceso denegado al consultar órdenes de otro proveedor
    Dado que el header X-Provider-Id es "prov-001"
    Cuando el proveedor consulta GET /api/v1/providers/prov-002/orders
    Entonces la respuesta HTTP es 403
