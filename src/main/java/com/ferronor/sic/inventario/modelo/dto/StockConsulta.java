package com.ferronor.sic.inventario.modelo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockConsulta {

    private final int idProducto;
    private final String codigoProducto;
    private final String nombreProducto;
    private final String nombreCategoria;
    private final String abreviaturaUnidad;
    private final BigDecimal cantidadActual;
    private final BigDecimal stockMinimo;
    private final BigDecimal costoPromedioActual;
    private final LocalDateTime fechaUltimaActualizacion;
    private final boolean productoActivo;

    public StockConsulta(
            int idProducto,
            String codigoProducto,
            String nombreProducto,
            String nombreCategoria,
            String abreviaturaUnidad,
            BigDecimal cantidadActual,
            BigDecimal stockMinimo,
            BigDecimal costoPromedioActual,
            LocalDateTime fechaUltimaActualizacion,
            boolean productoActivo) {

        this.idProducto = idProducto;
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
        this.nombreCategoria = nombreCategoria;
        this.abreviaturaUnidad = abreviaturaUnidad;
        this.cantidadActual = cantidadActual;
        this.stockMinimo = stockMinimo;
        this.costoPromedioActual = costoPromedioActual;
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
        this.productoActivo = productoActivo;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public String getAbreviaturaUnidad() {
        return abreviaturaUnidad;
    }

    public BigDecimal getCantidadActual() {
        return cantidadActual;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public BigDecimal getCostoPromedioActual() {
        return costoPromedioActual;
    }

    public LocalDateTime getFechaUltimaActualizacion() {
        return fechaUltimaActualizacion;
    }

    public boolean isProductoActivo() {
        return productoActivo;
    }
}
