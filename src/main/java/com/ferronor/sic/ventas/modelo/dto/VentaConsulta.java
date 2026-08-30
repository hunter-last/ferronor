package com.ferronor.sic.ventas.modelo.dto;

import com.ferronor.sic.ventas.modelo.EstadoComprobante;
import com.ferronor.sic.ventas.modelo.EstadoVenta;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VentaConsulta {

    private final int idVenta;
    private final LocalDateTime fecha;
    private final int idCliente;
    private final String nombreRazonSocialCliente;
    private final String tipoDocumentoCliente;
    private final String numeroDocumentoCliente;
    private final int idFormaPago;
    private final String nombreFormaPago;
    private final EstadoVenta estado;
    private final BigDecimal subtotal;
    private final BigDecimal igv;
    private final BigDecimal total;
    private final int idUsuario;
    private final String nombreUsuario;
    private final int idComprobante;
    private final String nombreTipoComprobante;
    private final String serie;
    private final String numero;
    private final LocalDateTime fechaEmisionComprobante;
    private final EstadoComprobante estadoComprobante;

    public VentaConsulta(
            int idVenta,
            LocalDateTime fecha,
            int idCliente,
            String nombreRazonSocialCliente,
            String tipoDocumentoCliente,
            String numeroDocumentoCliente,
            int idFormaPago,
            String nombreFormaPago,
            EstadoVenta estado,
            BigDecimal subtotal,
            BigDecimal igv,
            BigDecimal total,
            int idUsuario,
            String nombreUsuario,
            int idComprobante,
            String nombreTipoComprobante,
            String serie,
            String numero,
            LocalDateTime fechaEmisionComprobante,
            EstadoComprobante estadoComprobante) {

        this.idVenta = idVenta;
        this.fecha = fecha;
        this.idCliente = idCliente;
        this.nombreRazonSocialCliente = nombreRazonSocialCliente;
        this.tipoDocumentoCliente = tipoDocumentoCliente;
        this.numeroDocumentoCliente = numeroDocumentoCliente;
        this.idFormaPago = idFormaPago;
        this.nombreFormaPago = nombreFormaPago;
        this.estado = estado;
        this.subtotal = subtotal;
        this.igv = igv;
        this.total = total;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.idComprobante = idComprobante;
        this.nombreTipoComprobante = nombreTipoComprobante;
        this.serie = serie;
        this.numero = numero;
        this.fechaEmisionComprobante = fechaEmisionComprobante;
        this.estadoComprobante = estadoComprobante;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public int getIdCliente() {
        return idCliente;
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

    public int getIdFormaPago() {
        return idFormaPago;
    }

    public String getNombreFormaPago() {
        return nombreFormaPago;
    }

    public EstadoVenta getEstado() {
        return estado;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getIgv() {
        return igv;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public int getIdComprobante() {
        return idComprobante;
    }

    public String getNombreTipoComprobante() {
        return nombreTipoComprobante;
    }

    public String getSerie() {
        return serie;
    }

    public String getNumero() {
        return numero;
    }

    public LocalDateTime getFechaEmisionComprobante() {
        return fechaEmisionComprobante;
    }

    public EstadoComprobante getEstadoComprobante() {
        return estadoComprobante;
    }
}
