package com.ferronor.sic.compras.modelo;

import java.math.BigDecimal;

public class DetalleOrdenCompra {

    private int idDetalle;
    private int idOrdenCompra;
    private int idProducto;
    private BigDecimal cantidad;

    public DetalleOrdenCompra() {
    }

    public DetalleOrdenCompra(int idProducto, BigDecimal cantidad) {
        this.idProducto = idProducto;
        this.cantidad = cantidad;
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(int idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
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
}