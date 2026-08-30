package com.ferronor.sic.tesoreria.modelo;

public enum OrigenMovimientoCaja {
    VENTA_CONTADO,
    COBRO_CLIENTE,
    COMPRA_CONTADO,
    PAGO_PROVEEDOR,
    GASTO_OPERATIVO,
    DEPOSITO_CAJA // agregado al CHECK de movimiento_caja.origen manualmente por el equipo (06_tesoreria.sql)
}