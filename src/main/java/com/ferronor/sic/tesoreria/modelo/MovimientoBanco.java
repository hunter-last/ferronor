package com.ferronor.sic.tesoreria.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimientoBanco {

    private int idMovimiento;
    private int idCuentaBancaria;
    private LocalDateTime fecha;
    private TipoMovimientoBanco tipo;
    private OrigenMovimientoBanco origen;
    private Integer idDocumentoOrigen; // nullable
    private BigDecimal monto;
    private String numeroOperacion; // nullable
    private int idUsuario;

    public MovimientoBanco() {
    }

    public MovimientoBanco(int idCuentaBancaria, TipoMovimientoBanco tipo, OrigenMovimientoBanco origen,
            Integer idDocumentoOrigen, BigDecimal monto, String numeroOperacion, int idUsuario) {
        this.idCuentaBancaria = idCuentaBancaria;
        this.tipo = tipo;
        this.origen = origen;
        this.idDocumentoOrigen = idDocumentoOrigen;
        this.monto = monto;
        this.numeroOperacion = numeroOperacion;
        this.idUsuario = idUsuario;
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public int getIdCuentaBancaria() {
        return idCuentaBancaria;
    }

    public void setIdCuentaBancaria(int idCuentaBancaria) {
        this.idCuentaBancaria = idCuentaBancaria;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public TipoMovimientoBanco getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimientoBanco tipo) {
        this.tipo = tipo;
    }

    public OrigenMovimientoBanco getOrigen() {
        return origen;
    }

    public void setOrigen(OrigenMovimientoBanco origen) {
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

    public String getNumeroOperacion() {
        return numeroOperacion;
    }

    public void setNumeroOperacion(String numeroOperacion) {
        this.numeroOperacion = numeroOperacion;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
}