package com.ferronor.sic.compras.modelo.dto;

import com.ferronor.sic.compras.modelo.EstadoCuenta;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Fila enriquecida de la consulta de cuentas por pagar (cuenta_pagar + compra + proveedor).
// Exclusivamente de lectura: no reemplaza a CuentaPagar ni se persiste.
public class CuentaPagarConsulta {

    private final int idCuentaPagar;
    private final int idCompra;
    private final String razonSocialProveedor;
    private final String rucProveedor;
    private final LocalDateTime fechaCompra;
    private final LocalDate fechaVencimiento;
    private final BigDecimal montoTotal;
    private final BigDecimal montoPagado;
    private final BigDecimal saldoPendiente;
    private final EstadoCuenta estado;

    public CuentaPagarConsulta(int idCuentaPagar, int idCompra, String razonSocialProveedor,
            String rucProveedor, LocalDateTime fechaCompra, LocalDate fechaVencimiento,
            BigDecimal montoTotal, BigDecimal montoPagado, BigDecimal saldoPendiente,
            EstadoCuenta estado) {
        this.idCuentaPagar = idCuentaPagar;
        this.idCompra = idCompra;
        this.razonSocialProveedor = razonSocialProveedor;
        this.rucProveedor = rucProveedor;
        this.fechaCompra = fechaCompra;
        this.fechaVencimiento = fechaVencimiento;
        this.montoTotal = montoTotal;
        this.montoPagado = montoPagado;
        this.saldoPendiente = saldoPendiente;
        this.estado = estado;
    }

    public int getIdCuentaPagar() {
        return idCuentaPagar;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public String getRazonSocialProveedor() {
        return razonSocialProveedor;
    }

    public String getRucProveedor() {
        return rucProveedor;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }
}