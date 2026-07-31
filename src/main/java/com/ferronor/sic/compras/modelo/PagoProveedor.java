package com.ferronor.sic.compras.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Objeto de entrada para CompraService.aplicarPago(...). No mapea una tabla propia:
// agrupa los datos necesarios para aplicar un pago contra cuenta_pagar.
public class PagoProveedor {

    private int idCompra;
    private BigDecimal monto;
    private LocalDateTime fecha;

    public PagoProveedor() {
    }

    public PagoProveedor(int idCompra, BigDecimal monto, LocalDateTime fecha) {
        this.idCompra = idCompra;
        this.monto = monto;
        this.fecha = fecha;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
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