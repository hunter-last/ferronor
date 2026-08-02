package com.ferronor.sic.ventas.modelo;

// Duplicado idéntico en compras/modelo/EstadoCuenta.java. Mismo CHECK físico en
// cuenta_pagar y cuenta_cobrar, pero se mantiene un enum por módulo para no
// acoplar compras/ <-> ventas/ (acuerdo de arquitectura ya aplicado en Compras).
public enum EstadoCuenta {
    PENDIENTE,
    PAGADA,
    VENCIDA
}