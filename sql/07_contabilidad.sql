CREATE TABLE asiento_contable (
    id_asiento SERIAL PRIMARY KEY,
    fecha TIMESTAMP NOT NULL DEFAULT now(),
    origen VARCHAR(20) NOT NULL CHECK (origen IN ('VENTA','COMPRA','COBRO','PAGO','DEPOSITO_CAJA')),
    id_documento_origen INT NOT NULL,
    glosa VARCHAR(200) NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','ANULADO')),
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE detalle_asiento (
    id_detalle SERIAL PRIMARY KEY,
    id_asiento INT NOT NULL REFERENCES asiento_contable(id_asiento) ON DELETE CASCADE ON UPDATE CASCADE,
    id_cuenta INT NOT NULL REFERENCES plan_cuenta(id_cuenta) ON DELETE RESTRICT ON UPDATE CASCADE,
    debe NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (debe >= 0),
    haber NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (haber >= 0),
    CHECK ((debe > 0 AND haber = 0) OR (haber > 0 AND debe = 0))
);