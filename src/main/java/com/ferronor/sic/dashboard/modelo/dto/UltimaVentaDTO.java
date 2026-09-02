package com.ferronor.sic.dashboard.modelo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UltimaVentaDTO {

    private int idVenta;
    private String comprobante;
    private String cliente;
    private LocalDateTime fecha;
    private String formaPago;
    private BigDecimal total = BigDecimal.ZERO;
    private String estado;

    public UltimaVentaDTO() {
    }

    public UltimaVentaDTO(int idVenta, String comprobante, String cliente, LocalDateTime fecha, String formaPago, BigDecimal total, String estado) {
        this.idVenta = idVenta;
        this.comprobante = comprobante;
        this.cliente = cliente;
        this.fecha = fecha;
        this.formaPago = formaPago;
        this.total = total != null ? total : BigDecimal.ZERO;
        this.estado = estado;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public String getComprobante() {
        return comprobante;
    }

    public void setComprobante(String comprobante) {
        this.comprobante = comprobante;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total != null ? total : BigDecimal.ZERO;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
