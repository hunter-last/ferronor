/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.inventario.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class MovimientoInventario {

    private int idMovimiento;
    private int idProducto;
    private LocalDateTime fecha;
    private TipoMovimiento tipo;
    private OrigenMovimiento origen;
    private Integer idDocumentoOrigen;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal costoTotal;
    private int idUsuario;

    public MovimientoInventario() {
    }
    
    public MovimientoInventario(int idProducto, TipoMovimiento tipo, OrigenMovimiento origen,
            Integer idDocumentoOrigen, BigDecimal cantidad,
            BigDecimal costoUnitario, int idUsuario) {
        this.idProducto = idProducto;
        this.tipo = tipo;
        this.origen = origen;
        this.idDocumentoOrigen = idDocumentoOrigen;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.idUsuario = idUsuario;
        recalcularCostoTotal();
    }

    // Fábrica exclusiva para reconstrucción desde BD (uso de los DAO). No recalcula costoTotal.
    public static MovimientoInventario reconstruir(int idMovimiento, int idProducto, LocalDateTime fecha,
            TipoMovimiento tipo, OrigenMovimiento origen, Integer idDocumentoOrigen,
            BigDecimal cantidad, BigDecimal costoUnitario, BigDecimal costoTotal, int idUsuario) {
        MovimientoInventario mov = new MovimientoInventario();
        mov.idMovimiento = idMovimiento;
        mov.idProducto = idProducto;
        mov.fecha = fecha;
        mov.tipo = tipo;
        mov.origen = origen;
        mov.idDocumentoOrigen = idDocumentoOrigen;
        mov.cantidad = cantidad;
        mov.costoUnitario = costoUnitario;
        mov.costoTotal = costoTotal; // valor exacto de la BD, sin recalcular
        mov.idUsuario = idUsuario;
        return mov;
    }

    private void recalcularCostoTotal() {
        if (cantidad != null && costoUnitario != null) {
            this.costoTotal = cantidad.multiply(costoUnitario);
        }
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public OrigenMovimiento getOrigen() {
        return origen;
    }

    public void setOrigen(OrigenMovimiento origen) {
        this.origen = origen;
    }

    public Integer getIdDocumentoOrigen() {
        return idDocumentoOrigen;
    }

    public void setIdDocumentoOrigen(Integer idDocumentoOrigen) {
        this.idDocumentoOrigen = idDocumentoOrigen;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
        recalcularCostoTotal();
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
        recalcularCostoTotal();
    }

    // sin setCostoTotal(): es un campo derivado, solo se calcula internamente
    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    @Override
    public String toString() {
        return "MovimientoInventario{idMovimiento=" + idMovimiento
                + ", idProducto=" + idProducto + ", tipo=" + tipo
                + ", origen=" + origen + ", cantidad=" + cantidad
                + ", costoUnitario=" + costoUnitario + ", costoTotal=" + costoTotal + '}';
    }
}
