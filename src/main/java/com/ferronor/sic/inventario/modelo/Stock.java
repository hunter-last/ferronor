
package com.ferronor.sic.inventario.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class Stock {

    private int idProducto;
    private BigDecimal cantidadActual;
    private BigDecimal costoPromedioActual;
    private LocalDateTime fechaUltimaActualizacion;

    public Stock() {
    }

    public Stock(int idProducto, BigDecimal cantidadActual, BigDecimal costoPromedioActual,
            LocalDateTime fechaUltimaActualizacion) {
        this.idProducto = idProducto;
        this.cantidadActual = cantidadActual;
        this.costoPromedioActual = costoPromedioActual;
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public BigDecimal getCantidadActual() {
        return cantidadActual;
    }

    public void setCantidadActual(BigDecimal cantidadActual) {
        this.cantidadActual = cantidadActual;
    }

    public BigDecimal getCostoPromedioActual() {
        return costoPromedioActual;
    }

    public void setCostoPromedioActual(BigDecimal costoPromedioActual) {
        this.costoPromedioActual = costoPromedioActual;
    }

    public LocalDateTime getFechaUltimaActualizacion() {
        return fechaUltimaActualizacion;
    }

    public void setFechaUltimaActualizacion(LocalDateTime fecha) {
        this.fechaUltimaActualizacion = fecha;
    }
}
