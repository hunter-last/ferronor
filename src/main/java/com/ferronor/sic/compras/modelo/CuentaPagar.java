package com.ferronor.sic.compras.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CuentaPagar {

    private int idCuentaPagar;
    private int idCompra;
    private BigDecimal montoTotal;
    private BigDecimal montoPagado;
    private BigDecimal saldoPendiente;
    private LocalDate fechaVencimiento; // NOT NULL en BD: se calcula en CompraService a partir de plazoDias
    private EstadoCuenta estado;

    public CuentaPagar() {
    }

    public CuentaPagar(int idCompra, BigDecimal montoTotal, LocalDate fechaVencimiento) {
        this.idCompra = idCompra;
        this.montoTotal = montoTotal;
        this.montoPagado = BigDecimal.ZERO;
        this.saldoPendiente = montoTotal;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = EstadoCuenta.PENDIENTE;
    }

    public int getIdCuentaPagar() {
        return idCuentaPagar;
    }

    public void setIdCuentaPagar(int idCuentaPagar) {
        this.idCuentaPagar = idCuentaPagar;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(BigDecimal montoPagado) {
        this.montoPagado = montoPagado;
    }

    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(BigDecimal saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuenta estado) {
        this.estado = estado;
    }
}