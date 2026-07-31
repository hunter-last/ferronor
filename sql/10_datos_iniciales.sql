INSERT INTO rol (nombre) VALUES
('Administrador'), ('Cajero'), ('Contador'), ('Logistica');

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
('TESORERIA','Acceso al módulo de Tesorería'),
('CONTABILIDAD','Acceso al módulo de Contabilidad'),
('SEGURIDAD','Acceso al módulo de Seguridad');

-- Administrador: acceso a todos los permisos
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso FROM rol r, permiso p WHERE r.nombre = 'Administrador';

-- Cajero: registra ventas y maneja caja
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso FROM rol r, permiso p
WHERE r.nombre = 'Cajero' AND p.codigo IN ('VENTAS','TESORERIA','REGISTRAR_VENTA');

-- Contador: revisa contabilidad y reportes
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso FROM rol r, permiso p
WHERE r.nombre = 'Contador' AND p.codigo IN ('CONTABILIDAD','VER_BALANCE','GENERAR_REPORTES');

-- Logistica: inventario y compras
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso FROM rol r, permiso p
WHERE r.nombre = 'Logistica' AND p.codigo IN ('INVENTARIO','MAESTROS','COMPRAS','AJUSTAR_STOCK','REGISTRAR_COMPRA');





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

INSERT INTO plan_cuenta (codigo, nombre_cuenta, id_cuenta_padre, nivel) VALUES
('1','Activo disponible y exigible', NULL, 1),
('2','Existencias', NULL, 1),
('4','Pasivo', NULL, 1),
('6','Gastos por naturaleza', NULL, 1),
('7','Ingresos', NULL, 1);

INSERT INTO plan_cuenta (codigo, nombre_cuenta, id_cuenta_padre, nivel)
SELECT '10','Efectivo y equivalentes de efectivo',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='1'),2
UNION ALL SELECT '12','Cuentas por cobrar comerciales - terceros',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='1'),2
UNION ALL SELECT '20','Mercaderías',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='2'),2
UNION ALL SELECT '40','Tributos, contraprestaciones y aportes por pagar',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='4'),2
UNION ALL SELECT '42','Cuentas por pagar comerciales - terceros',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='4'),2
UNION ALL SELECT '61','Variación de existencias',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='6'),2
UNION ALL SELECT '69','Costo de ventas',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='6'),2
UNION ALL SELECT '70','Ventas',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='7'),2;

INSERT INTO caja (nombre, saldo_actual, estado) VALUES ('Caja Principal', 0, 'CERRADA');

INSERT INTO cuenta_bancaria (banco, alias, numero_cuenta, moneda) VALUES
('BCP','Cuenta Corriente BCP','00000000000000','PEN'),
('BBVA','Cuenta Corriente BBVA','00000000000001','PEN');
-- reemplaza los numero_cuenta con los reales de la empresa

INSERT INTO plan_cuenta (codigo, nombre_cuenta, id_cuenta_padre, nivel)
SELECT '101','Caja',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='10'),3
UNION ALL SELECT '104','Cuentas corrientes en instituciones financieras',(SELECT id_cuenta FROM plan_cuenta WHERE codigo='10'),3;

INSERT INTO plan_cuenta (codigo, nombre_cuenta, id_cuenta_padre, nivel) VALUES
('5','Patrimonio', NULL, 1);

INSERT INTO plan_cuenta (codigo, nombre_cuenta, id_cuenta_padre, nivel)
SELECT '50','Capital', (SELECT id_cuenta FROM plan_cuenta WHERE codigo='5'), 2;

-- CREACION DE UN USUSARIO ADMINISTRADOR
INSERT INTO usuario (
    nombres,
    apellidos,
    usuario_login,
    password_hash,
    id_rol,
    activo
)
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

