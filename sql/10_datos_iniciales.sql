-- ============================================================
-- 10_datos_iniciales.sql
-- Decor Home Ferronor — datos de arranque + datos de prueba
-- ============================================================

-- ------------------------------------------------------------
-- ROLES Y PERMISOS
-- ------------------------------------------------------------
INSERT INTO rol (nombre) VALUES
('Administrador'), ('Cajero'), ('Contador'), ('Logistica'), ('Tesoreria');

INSERT INTO permiso (codigo, nombre) VALUES
('REGISTRAR_VENTA','Registrar venta'),
('ANULAR_VENTA','Anular venta'),
('REGISTRAR_COMPRA','Registrar compra'),
('VER_BALANCE','Ver balance'),
('ADMIN_USUARIOS','Administrar usuarios'),
('AJUSTAR_STOCK','Ajustar stock'),
('GENERAR_REPORTES','Generar reportes');

INSERT INTO permiso (codigo, nombre) VALUES
('MAESTROS','Acceso al módulo de Maestros'),
('INVENTARIO','Acceso al módulo de Inventario'),
('COMPRAS','Acceso al módulo de Compras'),
('VENTAS','Acceso al módulo de Ventas'),
('CAJA','Acceso al módulo de Caja'),
('TESORERIA','Acceso al módulo de Tesorería'),
('CONTABILIDAD','Acceso al módulo de Contabilidad'),
('SEGURIDAD','Acceso al módulo de Seguridad');

-- Administrador: acceso a todos los permisos
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso FROM rol r, permiso p WHERE r.nombre = 'Administrador';

-- Cajero: vende y opera su propia caja (YA NO valida ni deposita)
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso FROM rol r, permiso p
WHERE r.nombre = 'Cajero' AND p.codigo IN ('VENTAS','CAJA','REGISTRAR_VENTA');

-- Tesoreria: valida liquidaciones, deposita, controla movimientos, gastos operativos
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso FROM rol r, permiso p
WHERE r.nombre = 'Tesoreria' AND p.codigo IN ('TESORERIA');

-- Contador: revisa contabilidad y reportes
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso FROM rol r, permiso p
WHERE r.nombre = 'Contador' AND p.codigo IN ('CONTABILIDAD','VER_BALANCE','GENERAR_REPORTES');

-- Logistica: inventario y compras
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso FROM rol r, permiso p
WHERE r.nombre = 'Logistica' AND p.codigo IN ('INVENTARIO','MAESTROS','COMPRAS','AJUSTAR_STOCK','REGISTRAR_COMPRA');


-- ------------------------------------------------------------
-- USUARIOS (admin + uno de prueba por cada rol — contraseña
-- para los de prueba: Ferronor123. El hash de admin no se toca.)
-- ------------------------------------------------------------
INSERT INTO usuario (nombres, apellidos, usuario_login, password_hash, id_rol, activo)
SELECT
    'Administrador',
    'Sistema',
    'admin',
    '$2a$10$CVpd.DShhesSwKw0.IyeDuqF/H8qqimwScABwpoWUXknztFkamIN.',
    r.id_rol,
    TRUE
FROM rol r
WHERE r.nombre = 'Administrador'
ON CONFLICT (usuario_login) DO NOTHING;

INSERT INTO usuario (nombres, apellidos, usuario_login, password_hash, id_rol, activo)
SELECT 'Rafael', 'Cajero Prueba', 'cajero1',
       '$2a$10$9oBDcki9cIW.HWrDVrp0cu6EThc/Y1YdBrNn5YEEb9b1NWAQRUF92',
       r.id_rol, TRUE
FROM rol r WHERE r.nombre = 'Cajero'

UNION ALL

SELECT 'Jeferson', 'Tesorería Prueba', 'tesoreria1',
       '$2a$10$UPhPD.zxBHqnOP8EXeSmZuxB6Rjnog.Z4jTg1WR9M9WmN6lAVTpxK',
       r.id_rol, TRUE
FROM rol r WHERE r.nombre = 'Tesoreria'

UNION ALL

SELECT 'Juan', 'Contadora Prueba', 'contador1',
       '$2a$10$hiV9zwE4lt5cgiMEMzFUQ.FrNoq4rCUEfq/hjV21OCvZdLeZISJXG',
       r.id_rol, TRUE
FROM rol r WHERE r.nombre = 'Contador'

UNION ALL

SELECT 'Luis', 'Logística Prueba', 'logistica1',
       '$2a$10$NdJWaPHazDZ5fFcvQvB3KucFIfET7nBYOZnyWwu2OCpZ8RDIyKQBO',
       r.id_rol, TRUE
FROM rol r WHERE r.nombre = 'Logistica'

ON CONFLICT (usuario_login) DO NOTHING;


-- ------------------------------------------------------------
-- CATÁLOGOS BASE
-- ------------------------------------------------------------
INSERT INTO categoria (nombre) VALUES
('Cerámicos'), ('Porcelanatos'), ('Sanitarios'), ('Grifería'), ('Pegamentos'), ('Fraguas');

INSERT INTO unidad_medida (nombre, abreviatura) VALUES
('Metro cuadrado','m²'), ('Unidad','und'), ('Bolsa','bls');

INSERT INTO forma_pago (nombre, es_credito) VALUES
('Efectivo', FALSE), ('Transferencia', FALSE), ('Tarjeta', FALSE), ('Yape', FALSE), ('Crédito', TRUE);

INSERT INTO tipo_comprobante (nombre, serie) VALUES
('Boleta','B001'), ('Factura','F001');

INSERT INTO correlativo_comprobante (id_tipo_comprobante, ultimo_numero)
SELECT id_tipo_comprobante, 0 FROM tipo_comprobante;


-- ------------------------------------------------------------
-- PLAN DE CUENTAS (PCGE — no es dato de prueba, es tu catálogo
-- contable oficial)
-- ------------------------------------------------------------
INSERT INTO plan_cuenta (codigo, nombre_cuenta, id_cuenta_padre, nivel) VALUES
('1','Activo disponible y exigible', NULL, 1),
('2','Existencias', NULL, 1),
('4','Pasivo', NULL, 1),
('5','Patrimonio', NULL, 1),
('6','Gastos por naturaleza', NULL, 1),
('7','Ingresos', NULL, 1);

INSERT INTO plan_cuenta (codigo, nombre_cuenta, id_cuenta_padre, nivel)
SELECT '10','Efectivo y equivalentes de efectivo',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='1'),2
UNION ALL SELECT '12','Cuentas por cobrar comerciales - terceros',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='1'),2
UNION ALL SELECT '20','Mercaderías',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='2'),2
UNION ALL SELECT '40','Tributos, contraprestaciones y aportes por pagar',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='4'),2
UNION ALL SELECT '42','Cuentas por pagar comerciales - terceros',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='4'),2
UNION ALL SELECT '50','Capital',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='5'),2
UNION ALL SELECT '61','Variación de existencias',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='6'),2
UNION ALL SELECT '69','Costo de ventas',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='6'),2
UNION ALL SELECT '70','Ventas',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='7'),2;

INSERT INTO plan_cuenta (codigo, nombre_cuenta, id_cuenta_padre, nivel)
SELECT '101','Caja',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='10'),3
UNION ALL SELECT '104','Cuentas corrientes en instituciones financieras',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='10'),3;

-- ------------------------------------------------------------
-- PROVEEDORES DE PRUEBA
-- ------------------------------------------------------------
INSERT INTO proveedor (razon_social, ruc, direccion, telefono, contacto, activo) VALUES
('Cerámica San Lorenzo S.A.',     '20100047218', 'Av. Argentina 4090, Callao',    '014191919', 'Luis Ramírez',   TRUE),
('Corporación Celima S.A.',       '20100154254', 'Av. Elmer Faucett 2050, Lima',  '016143434', 'Karina Salazar', TRUE),
('Distribuidora Vainsa E.I.R.L.', '20481234567', 'Av. Bolognesi 320, Chiclayo',   '074234567', 'Jorge Effio',    TRUE),
('Trébol Distribución S.A.C.',    '20512345678', 'Carretera Panamericana Km 780', '074987654', 'María Chirinos', TRUE);


-- ------------------------------------------------------------
-- CLIENTES DE PRUEBA
-- ------------------------------------------------------------
INSERT INTO cliente (tipo_documento, numero_documento, nombre_razon_social, telefono, activo) VALUES
('DNI', '16758423', 'Carlos Puse Díaz',                  '979112233', TRUE),
('DNI', '45219876', 'Rosa Elvira Chapoñan',              '968554411', TRUE),
('RUC', '20601122334', 'Constructora Lambayeque S.A.C.',     '074556677', TRUE),
('RUC', '20609988776', 'Acabados & Diseño Norte E.I.R.L.',   '074998877', TRUE);


-- ------------------------------------------------------------
-- PRODUCTOS DE PRUEBA (enlazados por nombre, no por id)
-- ------------------------------------------------------------
INSERT INTO producto (codigo, nombre, id_categoria, id_unidad_medida, stock_minimo, precio_venta, activo)
SELECT 'CER-001', 'Cerámico Blanco Brillante 30x30',
       (SELECT id_categoria FROM categoria WHERE nombre = 'Cerámicos'),
       (SELECT id_unidad_medida FROM unidad_medida WHERE nombre = 'Metro cuadrado'),
       10, 28.50, TRUE
UNION ALL
SELECT 'POR-001', 'Porcelanato Gris Cemento 60x60',
       (SELECT id_categoria FROM categoria WHERE nombre = 'Porcelanatos'),
       (SELECT id_unidad_medida FROM unidad_medida WHERE nombre = 'Metro cuadrado'),
       8, 45.90, TRUE
UNION ALL
SELECT 'SAN-001', 'Inodoro One Piece Blanco',
       (SELECT id_categoria FROM categoria WHERE nombre = 'Sanitarios'),
       (SELECT id_unidad_medida FROM unidad_medida WHERE nombre = 'Unidad'),
       3, 289.00, TRUE
UNION ALL
SELECT 'GRI-001', 'Grifería Monocomando para Lavatorio',
       (SELECT id_categoria FROM categoria WHERE nombre = 'Grifería'),
       (SELECT id_unidad_medida FROM unidad_medida WHERE nombre = 'Unidad'),
       5, 79.90, TRUE
UNION ALL
SELECT 'PEG-001', 'Pegamento para Cerámico x 25kg',
       (SELECT id_categoria FROM categoria WHERE nombre = 'Pegamentos'),
       (SELECT id_unidad_medida FROM unidad_medida WHERE nombre = 'Bolsa'),
       15, 22.00, TRUE;

INSERT INTO stock (id_producto, cantidad_actual, costo_promedio_actual)
SELECT id_producto, 0, 0 FROM producto;


-- ------------------------------------------------------------
-- CAJA Y CUENTAS BANCARIAS
-- ------------------------------------------------------------
INSERT INTO caja (nombre, saldo_actual, estado) VALUES ('Caja Principal', 0, 'CERRADA');

INSERT INTO cuenta_bancaria (banco, alias, numero_cuenta, moneda) VALUES
('BCP','Cuenta Corriente BCP','19312345678012','PEN'),
('BBVA','Cuenta Corriente BBVA','00112345670015','PEN');

