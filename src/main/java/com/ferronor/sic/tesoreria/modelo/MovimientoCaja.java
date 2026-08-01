package com.ferronor.sic.tesoreria.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimientoCaja {

    private int idMovimiento;
    private int idCaja;
    private LocalDateTime fecha;
    private TipoMovimientoCaja tipo;
    private OrigenMovimientoCaja origen;
    private Integer idDocumentoOrigen; // nullable: solo nulo para GASTO_OPERATIVO
    private BigDecimal monto;
    private String descripcion; // nullable
    private int idUsuario;

    public MovimientoCaja() {
    }

    public MovimientoCaja(int idCaja, TipoMovimientoCaja tipo, OrigenMovimientoCaja origen,
            Integer idDocumentoOrigen, BigDecimal monto, String descripcion, int idUsuario) {
        this.idCaja = idCaja;
        this.tipo = tipo;
        this.origen = origen;
        this.idDocumentoOrigen = idDocumentoOrigen;
        this.monto = monto;
        this.descripcion = descripcion;
        this.idUsuario = idUsuario;
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public int getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(int idCaja) {
        this.idCaja = idCaja;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public TipoMovimientoCaja getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimientoCaja tipo) {
        this.tipo = tipo;
    }

    public OrigenMovimientoCaja getOrigen() {
        return origen;
    }

    public void setOrigen(OrigenMovimientoCaja origen) {
        this.origen = origen;
    }

    public Integer getIdDocumentoOrigen() {
        return idDocumentoOrigen;
    }

    public void setIdDocumentoOrigen(Integer idDocumentoOrigen) {
        this.idDocumentoOrigen = idDocumentoOrigen;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
}