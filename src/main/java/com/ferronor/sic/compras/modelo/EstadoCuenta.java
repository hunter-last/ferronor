package com.ferronor.sic.compras.modelo;
 
// Duplicado idéntico en ventas/modelo/EstadoCuenta.java. Mismo CHECK físico en
// cuenta_pagar y cuenta_cobrar, pero se mantiene un enum por módulo para no
// acoplar compras/ <-> ventas/ (ver acuerdo de arquitectura del 2026-07-31).
public enum EstadoCuenta {
    PENDIENTE,
    PAGADA,
    VENCIDA
}