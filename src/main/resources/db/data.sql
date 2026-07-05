MERGE INTO orders (id, order_code, provider_id, status, product_code, product_description, quantity,
    street, city, state, postal_code, country,
    customer_name, phone, email,
    expected_delivery_date, special_conditions, version)
KEY (id)
VALUES
    ('550e8400-e29b-41d4-a716-446655440000', 'ORD-20260704-001', 'prov-001', 'PENDIENTE',
     'PROD-001', 'Auriculares Bluetooth inalambricos', 2,
     'Av. Principal 123, Piso 2', 'Lima', 'Lima', '15001', 'Peru',
     'Juan Perez', '+51987654321', 'juan.perez@example.com',
     '2026-08-20', 'Fragil - manejar con cuidado', 0),
    ('550e8400-e29b-41d4-a716-446655440001', 'ORD-20260704-002', 'prov-001', 'PENDIENTE',
     'PROD-002', 'Teclado mecanico inalambrico', 1,
     'Jr. Los Pinos 456', 'Arequipa', 'Arequipa', '04001', 'Peru',
     'Maria Garcia', '+51976543210', 'maria.garcia@example.com',
     '2026-08-25', null, 0),
    ('660e8400-e29b-41d4-a716-446655440001', 'ORD-20260704-003', 'prov-002', 'PENDIENTE',
     'PROD-003', 'Monitor LED 24 pulgadas', 1,
     'Calle Comercio 789', 'Cusco', 'Cusco', '08001', 'Peru',
     'Carlos Lopez', '+51965432109', null,
     '2026-08-30', null, 0);
