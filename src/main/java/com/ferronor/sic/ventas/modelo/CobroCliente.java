package com.ferronor.sic.ventas.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Objeto de entrada para VentaService.aplicarCobro(...). No mapea una tabla propia:
// agrupa los datos necesarios para aplicar un cobro contra cuenta_cobrar.
public class CobroCliente {

    private int idVenta;
    private BigDecimal monto;
    private LocalDateTime fecha;

    public CobroCliente() {
    }

    public CobroCliente(int idVenta, BigDecimal monto, LocalDateTime fecha) {
        this.idVenta = idVenta;
        this.monto = monto;
        this.fecha = fecha;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}