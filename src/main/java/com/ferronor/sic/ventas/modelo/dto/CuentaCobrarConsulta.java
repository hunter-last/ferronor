package com.ferronor.sic.ventas.modelo.dto;

import com.ferronor.sic.ventas.modelo.EstadoCuenta;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CuentaCobrarConsulta {

    private final int idCuentaCobrar;
    private final int idVenta;
    private final String nombreRazonSocialCliente;
    private final String tipoDocumentoCliente;
    private final String numeroDocumentoCliente;
    private final LocalDateTime fechaVenta;
    private final LocalDate fechaVencimiento;
    private final BigDecimal montoTotal;
    private final BigDecimal montoCobrado;
    private final BigDecimal saldoPendiente;
    private final EstadoCuenta estado;

    public CuentaCobrarConsulta(
            int idCuentaCobrar,
            int idVenta,
            String nombreRazonSocialCliente,
            String tipoDocumentoCliente,
            String numeroDocumentoCliente,
            LocalDateTime fechaVenta,
            LocalDate fechaVencimiento,
            BigDecimal montoTotal,
            BigDecimal montoCobrado,
            BigDecimal saldoPendiente,
            EstadoCuenta estado) {

        this.idCuentaCobrar = idCuentaCobrar;
        this.idVenta = idVenta;
        this.nombreRazonSocialCliente = nombreRazonSocialCliente;
        this.tipoDocumentoCliente = tipoDocumentoCliente;
        this.numeroDocumentoCliente = numeroDocumentoCliente;
        this.fechaVenta = fechaVenta;
        this.fechaVencimiento = fechaVencimiento;
        this.montoTotal = montoTotal;
        this.montoCobrado = montoCobrado;
        this.saldoPendiente = saldoPendiente;
        this.estado = estado;
    }

    public int getIdCuentaCobrar() {
        return idCuentaCobrar;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public String getNombreRazonSocialCliente() {
        return nombreRazonSocialCliente;
    }

    public String getTipoDocumentoCliente() {
        return tipoDocumentoCliente;
    }

    public String getNumeroDocumentoCliente() {
        return numeroDocumentoCliente;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public BigDecimal getMontoCobrado() {
        return montoCobrado;
    }

    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }
}