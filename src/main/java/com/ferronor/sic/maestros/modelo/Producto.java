
package com.ferronor.sic.maestros.modelo;

import java.math.BigDecimal;

public class Producto {

    private int idProducto;
    private String codigo;
    private String nombre;
    private int idCategoria;
    private int idUnidadMedida;
    private BigDecimal stockMinimo;
    private BigDecimal precioVenta;
    private boolean activo;

    public Producto() {
    }

    public Producto(String codigo, String nombre, int idCategoria, int idUnidadMedida,
            BigDecimal stockMinimo, BigDecimal precioVenta) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.idCategoria = idCategoria;
        this.idUnidadMedida = idUnidadMedida;
        this.stockMinimo = stockMinimo;
        this.precioVenta = precioVenta;
        this.activo = true;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int id) {
        this.idProducto = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public int getIdUnidadMedida() {
        return idUnidadMedida;
    }

    public void setIdUnidadMedida(int idUnidadMedida) {
        this.idUnidadMedida = idUnidadMedida;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
    
    
}
