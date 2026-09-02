package com.ferronor.sic.dashboard.modelo.dto;

import java.math.BigDecimal;

public class AlertaStockDTO {

    private int idProducto;
    private String codigo;
    private String nombre;
    private String categoria;
    private String unidad;
    private BigDecimal stockActual = BigDecimal.ZERO;
    private BigDecimal stockMinimo = BigDecimal.ZERO;
    private BigDecimal costoPromedio = BigDecimal.ZERO;
    private String estadoStock;

    public AlertaStockDTO() {
    }

    public AlertaStockDTO(int idProducto, String codigo, String nombre, String categoria, String unidad, BigDecimal stockActual, BigDecimal stockMinimo, BigDecimal costoPromedio, String estadoStock) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.unidad = unidad;
        this.stockActual = stockActual != null ? stockActual : BigDecimal.ZERO;
        this.stockMinimo = stockMinimo != null ? stockMinimo : BigDecimal.ZERO;
        this.costoPromedio = costoPromedio != null ? costoPromedio : BigDecimal.ZERO;
        this.estadoStock = estadoStock;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public BigDecimal getStockActual() {
        return stockActual;
    }

    public void setStockActual(BigDecimal stockActual) {
        this.stockActual = stockActual != null ? stockActual : BigDecimal.ZERO;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo != null ? stockMinimo : BigDecimal.ZERO;
    }

    public BigDecimal getCostoPromedio() {
        return costoPromedio;
    }

    public void setCostoPromedio(BigDecimal costoPromedio) {
        this.costoPromedio = costoPromedio != null ? costoPromedio : BigDecimal.ZERO;
    }

    public String getEstadoStock() {
        return estadoStock;
    }

    public void setEstadoStock(String estadoStock) {
        this.estadoStock = estadoStock;
    }
}
