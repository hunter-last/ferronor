CREATE TABLE categoria (
    id_categoria SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE unidad_medida (
    id_unidad_medida SERIAL PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL,
    abreviatura VARCHAR(10) NOT NULL
);

CREATE TABLE forma_pago (
    id_forma_pago SERIAL PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE,
    es_credito BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE tipo_comprobante (
    id_tipo_comprobante SERIAL PRIMARY KEY,
    nombre VARCHAR(20) NOT NULL UNIQUE,
    serie VARCHAR(10)
);

CREATE TABLE proveedor (
    id_proveedor SERIAL PRIMARY KEY,
    razon_social VARCHAR(150) NOT NULL,
    ruc CHAR(11) NOT NULL UNIQUE CHECK (length(ruc) = 11),
    direccion VARCHAR(200),
    telefono VARCHAR(20),
    contacto VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE cliente (
    id_cliente SERIAL PRIMARY KEY,
    tipo_documento VARCHAR(3) NOT NULL CHECK (tipo_documento IN ('DNI','RUC')),
    numero_documento VARCHAR(11) NOT NULL UNIQUE,
    nombre_razon_social VARCHAR(150) NOT NULL,
    telefono VARCHAR(20),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CHECK (
        (tipo_documento = 'DNI' AND length(numero_documento) = 8)
        OR (tipo_documento = 'RUC' AND length(numero_documento) = 11)
    )
);

CREATE TABLE producto (
    id_producto SERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    id_categoria INT NOT NULL REFERENCES categoria(id_categoria) ON DELETE RESTRICT ON UPDATE CASCADE,
    id_unidad_medida INT NOT NULL REFERENCES unidad_medida(id_unidad_medida) ON DELETE RESTRICT ON UPDATE CASCADE,
    stock_minimo NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    precio_venta NUMERIC(10,2) NOT NULL CHECK (precio_venta >= 0),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE plan_cuenta (
    id_cuenta SERIAL PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL UNIQUE,
    nombre_cuenta VARCHAR(150) NOT NULL,
    id_cuenta_padre INT REFERENCES plan_cuenta(id_cuenta) ON DELETE RESTRICT ON UPDATE CASCADE,
    nivel SMALLINT NOT NULL CHECK (nivel BETWEEN 1 AND 5)
);