package com.ferronor.sic.ventas.modelo;

import java.math.BigDecimal;

public class DetalleVenta {

    private int idDetalle;
    private int idVenta;
    private int idProducto;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario; // copia congelada de producto.precio_venta (incluye IGV)
    private BigDecimal subtotal; // cantidad * precioUnitario, con IGV incluido

    public DetalleVenta() {
    }

    public DetalleVenta(int idProducto, BigDecimal cantidad, BigDecimal precioUnitario) {
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = cantidad.multiply(precioUnitario);
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}