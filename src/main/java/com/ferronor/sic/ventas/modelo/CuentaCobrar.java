package com.ferronor.sic.ventas.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CuentaCobrar {

    private int idCuentaCobrar;
    private int idVenta;
    private BigDecimal montoTotal;
    private BigDecimal montoCobrado;
    private BigDecimal saldoPendiente;
    private LocalDate fechaVencimiento; // nullable en BD: solo si aplica plazo de crédito al cliente
    private EstadoCuenta estado;

    public CuentaCobrar() {
    }

    public CuentaCobrar(int idVenta, BigDecimal montoTotal, LocalDate fechaVencimiento) {
        this.idVenta = idVenta;
        this.montoTotal = montoTotal;
        this.montoCobrado = BigDecimal.ZERO;
        this.saldoPendiente = montoTotal;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = EstadoCuenta.PENDIENTE;
    }

    public int getIdCuentaCobrar() {
        return idCuentaCobrar;
    }

    public void setIdCuentaCobrar(int idCuentaCobrar) {
        this.idCuentaCobrar = idCuentaCobrar;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public BigDecimal getMontoCobrado() {
        return montoCobrado;
    }

    public void setMontoCobrado(BigDecimal montoCobrado) {
        this.montoCobrado = montoCobrado;
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