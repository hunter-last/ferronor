CREATE TABLE venta (
    id_venta SERIAL PRIMARY KEY,
    id_cliente INT NOT NULL REFERENCES cliente(id_cliente) ON DELETE RESTRICT ON UPDATE CASCADE,
    fecha TIMESTAMP NOT NULL DEFAULT now(),
    id_forma_pago INT NOT NULL REFERENCES forma_pago(id_forma_pago) ON DELETE RESTRICT ON UPDATE CASCADE,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('INICIADA','PAGO_PENDIENTE','PAGADA','DESPACHADA','CANCELADA')),
    subtotal NUMERIC(12,2) NOT NULL CHECK (subtotal >= 0),
    igv NUMERIC(12,2) NOT NULL CHECK (igv >= 0),
    total NUMERIC(12,2) NOT NULL CHECK (total >= 0),
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE detalle_venta (
    id_detalle SERIAL PRIMARY KEY,
    id_venta INT NOT NULL REFERENCES venta(id_venta) ON DELETE CASCADE ON UPDATE CASCADE,
    id_producto INT NOT NULL REFERENCES producto(id_producto) ON DELETE RESTRICT ON UPDATE CASCADE,
    cantidad NUMERIC(10,2) NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(10,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal NUMERIC(12,2) NOT NULL
);

CREATE TABLE comprobante (
    id_comprobante SERIAL PRIMARY KEY,
    id_venta INT NOT NULL UNIQUE REFERENCES venta(id_venta) ON DELETE RESTRICT ON UPDATE CASCADE,
    id_tipo_comprobante INT NOT NULL REFERENCES tipo_comprobante(id_tipo_comprobante) ON DELETE RESTRICT ON UPDATE CASCADE,
    serie VARCHAR(10) NOT NULL,
    numero VARCHAR(10) NOT NULL,
    fecha_emision TIMESTAMP NOT NULL DEFAULT now(),
    estado VARCHAR(15) NOT NULL CHECK (estado IN ('EMITIDO','ANULADO'))
);

CREATE TABLE correlativo_comprobante (
    id_tipo_comprobante INT PRIMARY KEY REFERENCES tipo_comprobante(id_tipo_comprobante) ON DELETE RESTRICT ON UPDATE CASCADE,
    ultimo_numero INT NOT NULL DEFAULT 0
);

CREATE TABLE cuenta_cobrar (
    id_cuenta_cobrar SERIAL PRIMARY KEY,
    id_venta INT NOT NULL UNIQUE REFERENCES venta(id_venta) ON DELETE RESTRICT ON UPDATE CASCADE,
    monto_total NUMERIC(12,2) NOT NULL,
    monto_cobrado NUMERIC(12,2) NOT NULL DEFAULT 0,
    saldo_pendiente NUMERIC(12,2) NOT NULL,
    fecha_vencimiento DATE,
    estado VARCHAR(10) NOT NULL CHECK (estado IN ('PENDIENTE','PAGADA','VENCIDA'))
);

CREATE TABLE devolucion_venta (
    id_devolucion SERIAL PRIMARY KEY,
    id_venta INT NOT NULL REFERENCES venta(id_venta) ON DELETE RESTRICT ON UPDATE CASCADE,
    id_producto INT NOT NULL REFERENCES producto(id_producto) ON DELETE RESTRICT ON UPDATE CASCADE,
    cantidad NUMERIC(10,2) NOT NULL CHECK (cantidad > 0),
    motivo VARCHAR(200) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT now(),
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

ALTER TABLE venta ADD CONSTRAINT chk_venta_total CHECK (total = subtotal + igv);

ALTER TABLE cuenta_cobrar ADD CONSTRAINT chk_cuenta_cobrar_cobrado CHECK (monto_cobrado <= monto_total);
ALTER TABLE cuenta_cobrar ADD CONSTRAINT chk_cuenta_cobrar_saldo CHECK (saldo_pendiente = monto_total - monto_cobrado);