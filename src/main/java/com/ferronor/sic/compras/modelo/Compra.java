package com.ferronor.sic.compras.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Compra {

    private int idCompra;
    private Integer idOrdenCompra; // nullable: compra directa sin cotización previa
    private int idProveedor;
    private LocalDateTime fecha;
    private int idFormaPago;
    private Integer plazoDias; // nullable, solo aplica si formaPago.esCredito
    private String numeroFactura;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private int idUsuario;
    private final List<DetalleCompra> detalles = new ArrayList<>();

    public Compra() {
    }

    public Compra(int idProveedor, int idFormaPago, String numeroFactura, int idUsuario) {
        this.idProveedor = idProveedor;
        this.idFormaPago = idFormaPago;
        this.numeroFactura = numeroFactura;
        this.idUsuario = idUsuario;
    }

    public void agregarDetalle(DetalleCompra detalle) {
        detalles.add(detalle);
    }

    public List<DetalleCompra> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public Integer getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(Integer idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public int getIdFormaPago() {
        return idFormaPago;
    }

    public void setIdFormaPago(int idFormaPago) {
        this.idFormaPago = idFormaPago;
    }

    public Integer getPlazoDias() {
        return plazoDias;
    }

    public void setPlazoDias(Integer plazoDias) {
        this.plazoDias = plazoDias;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getIgv() {
        return igv;
    }

    public void setIgv(BigDecimal igv) {
        this.igv = igv;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
}