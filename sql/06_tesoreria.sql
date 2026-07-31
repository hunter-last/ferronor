CREATE TABLE caja (
    id_caja SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    saldo_actual NUMERIC(12,2) NOT NULL DEFAULT 0,
    estado VARCHAR(10) NOT NULL CHECK (estado IN ('ABIERTA','CERRADA')),
    id_usuario_actual INT REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    fecha_apertura TIMESTAMP
);

CREATE TABLE movimiento_caja (
    id_movimiento SERIAL PRIMARY KEY,
    id_caja INT NOT NULL REFERENCES caja(id_caja) ON DELETE RESTRICT ON UPDATE CASCADE,
    fecha TIMESTAMP NOT NULL DEFAULT now(),
    tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('INGRESO','EGRESO')),
    origen VARCHAR(20) NOT NULL CHECK (origen IN ('VENTA_CONTADO','COBRO_CLIENTE','COMPRA_CONTADO','PAGO_PROVEEDOR','GASTO_OPERATIVO')),
    id_documento_origen INT,
    monto NUMERIC(12,2) NOT NULL CHECK (monto > 0),
    descripcion VARCHAR(200),
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE cuenta_bancaria (
    id_cuenta_bancaria SERIAL PRIMARY KEY,
    banco VARCHAR(30) NOT NULL,
    alias VARCHAR(50) NOT NULL,
    numero_cuenta VARCHAR(20) NOT NULL UNIQUE,
    cci VARCHAR(20),
    moneda VARCHAR(3) NOT NULL CHECK (moneda IN ('PEN','USD')),
    saldo_actual NUMERIC(12,2) NOT NULL DEFAULT 0,
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE movimiento_banco (
    id_movimiento SERIAL PRIMARY KEY,
    id_cuenta_bancaria INT NOT NULL REFERENCES cuenta_bancaria(id_cuenta_bancaria) ON DELETE RESTRICT ON UPDATE CASCADE,
    fecha TIMESTAMP NOT NULL DEFAULT now(),
    tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('DEPOSITO','RETIRO','TRANSFERENCIA')),
    origen VARCHAR(20) NOT NULL CHECK (origen IN ('VENTA_CONTADO','COBRO_CLIENTE','COMPRA_CONTADO','PAGO_PROVEEDOR','DEPOSITO_CAJA')),
    id_documento_origen INT,
    monto NUMERIC(12,2) NOT NULL CHECK (monto > 0),
    numero_operacion VARCHAR(50),
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE cierre_caja (
    id_cierre SERIAL PRIMARY KEY,
    id_caja INT NOT NULL REFERENCES caja(id_caja) ON DELETE RESTRICT ON UPDATE CASCADE,
    fecha TIMESTAMP NOT NULL DEFAULT now(),
    saldo_inicial NUMERIC(12,2) NOT NULL,
    saldo_final_sistema NUMERIC(12,2) NOT NULL,
    saldo_final_real NUMERIC(12,2) NOT NULL,
    diferencia NUMERIC(12,2) NOT NULL,
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
);