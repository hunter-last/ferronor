CREATE TABLE orden_compra (
    id_orden_compra SERIAL PRIMARY KEY,
    id_proveedor INT NOT NULL REFERENCES proveedor(id_proveedor) ON DELETE RESTRICT ON UPDATE CASCADE,
    fecha TIMESTAMP NOT NULL DEFAULT now(),
    estado VARCHAR(15) NOT NULL CHECK (estado IN ('PENDIENTE','APROBADA','RECHAZADA')),
    id_usuario_solicita INT NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    id_usuario_aprueba INT REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    fecha_aprobacion TIMESTAMP
);

CREATE TABLE detalle_orden_compra (
    id_detalle SERIAL PRIMARY KEY,
    id_orden_compra INT NOT NULL REFERENCES orden_compra(id_orden_compra) ON DELETE CASCADE ON UPDATE CASCADE,
    id_producto INT NOT NULL REFERENCES producto(id_producto) ON DELETE RESTRICT ON UPDATE CASCADE,
    cantidad NUMERIC(10,2) NOT NULL CHECK (cantidad > 0)
);

CREATE TABLE compra (
    id_compra SERIAL PRIMARY KEY,
    id_orden_compra INT REFERENCES orden_compra(id_orden_compra) ON DELETE RESTRICT ON UPDATE CASCADE,
    id_proveedor INT NOT NULL REFERENCES proveedor(id_proveedor) ON DELETE RESTRICT ON UPDATE CASCADE,
    fecha TIMESTAMP NOT NULL DEFAULT now(),
    id_forma_pago INT NOT NULL REFERENCES forma_pago(id_forma_pago) ON DELETE RESTRICT ON UPDATE CASCADE,
    plazo_dias SMALLINT CHECK (plazo_dias > 0),
    numero_factura VARCHAR(20) NOT NULL,
    subtotal NUMERIC(12,2) NOT NULL CHECK (subtotal >= 0),
    igv NUMERIC(12,2) NOT NULL CHECK (igv >= 0),
    total NUMERIC(12,2) NOT NULL CHECK (total >= 0),
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE (id_proveedor, numero_factura)
);

CREATE TABLE detalle_compra (
    id_detalle SERIAL PRIMARY KEY,
    id_compra INT NOT NULL REFERENCES compra(id_compra) ON DELETE CASCADE ON UPDATE CASCADE,
    id_producto INT NOT NULL REFERENCES producto(id_producto) ON DELETE RESTRICT ON UPDATE CASCADE,
    cantidad NUMERIC(10,2) NOT NULL CHECK (cantidad > 0),
    costo_unitario NUMERIC(10,4) NOT NULL CHECK (costo_unitario >= 0),
    subtotal NUMERIC(12,4) NOT NULL
);

CREATE TABLE cuenta_pagar (
    id_cuenta_pagar SERIAL PRIMARY KEY,
    id_compra INT NOT NULL UNIQUE REFERENCES compra(id_compra) ON DELETE RESTRICT ON UPDATE CASCADE,
    monto_total NUMERIC(12,2) NOT NULL,
    monto_pagado NUMERIC(12,2) NOT NULL DEFAULT 0,
    saldo_pendiente NUMERIC(12,2) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado VARCHAR(10) NOT NULL CHECK (estado IN ('PENDIENTE','PAGADA','VENCIDA'))
);

CREATE TABLE devolucion_compra (
    id_devolucion SERIAL PRIMARY KEY,
    id_compra INT NOT NULL REFERENCES compra(id_compra) ON DELETE RESTRICT ON UPDATE CASCADE,
    id_producto INT NOT NULL REFERENCES producto(id_producto) ON DELETE RESTRICT ON UPDATE CASCADE,
    cantidad NUMERIC(10,2) NOT NULL CHECK (cantidad > 0),
    motivo VARCHAR(200) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT now(),
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

ALTER TABLE compra ADD CONSTRAINT chk_compra_total CHECK (total = subtotal + igv);

ALTER TABLE cuenta_pagar ADD CONSTRAINT chk_cuenta_pagar_pagado CHECK (monto_pagado <= monto_total);
ALTER TABLE cuenta_pagar ADD CONSTRAINT chk_cuenta_pagar_saldo CHECK (saldo_pendiente = monto_total - monto_pagado);
