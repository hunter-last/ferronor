CREATE TABLE stock (
    id_producto INT PRIMARY KEY REFERENCES producto(id_producto) ON DELETE RESTRICT ON UPDATE CASCADE,
    cantidad_actual NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (cantidad_actual >= 0),
    costo_promedio_actual NUMERIC(10,4) NOT NULL DEFAULT 0 CHECK (costo_promedio_actual >= 0),
    fecha_ultima_actualizacion TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE movimiento_inventario (
    id_movimiento SERIAL PRIMARY KEY,
    id_producto INT NOT NULL REFERENCES producto(id_producto) ON DELETE RESTRICT ON UPDATE CASCADE,
    fecha TIMESTAMP NOT NULL DEFAULT now(),
    tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('ENTRADA','SALIDA','AJUSTE')),
    origen VARCHAR(20) NOT NULL CHECK (origen IN ('COMPRA','VENTA','AJUSTE_INVENTARIO','DEVOLUCION_COMPRA','DEVOLUCION_VENTA')),
    id_documento_origen INT,
    cantidad NUMERIC(10,2) NOT NULL CHECK (cantidad > 0),
    costo_unitario NUMERIC(10,4) NOT NULL CHECK (costo_unitario >= 0),
    costo_total NUMERIC(12,4) NOT NULL CHECK (costo_total >= 0),
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE ajuste_inventario (
    id_ajuste SERIAL PRIMARY KEY,
    id_producto INT NOT NULL REFERENCES producto(id_producto) ON DELETE RESTRICT ON UPDATE CASCADE,
    fecha TIMESTAMP NOT NULL DEFAULT now(),
    cantidad_sistema NUMERIC(10,2) NOT NULL,
    cantidad_fisica NUMERIC(10,2) NOT NULL CHECK (cantidad_fisica >= 0),
    diferencia NUMERIC(10,2) NOT NULL,
    motivo VARCHAR(200) NOT NULL,
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    id_movimiento_generado INT NOT NULL REFERENCES movimiento_inventario(id_movimiento) ON DELETE RESTRICT ON UPDATE CASCADE
);