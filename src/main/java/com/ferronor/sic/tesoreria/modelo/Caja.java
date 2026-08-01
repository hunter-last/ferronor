package com.ferronor.sic.tesoreria.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Caja {

    private int idCaja;
    private String nombre;
    private BigDecimal saldoActual;
    private EstadoCaja estado;
    private Integer idUsuarioActual; // nullable: solo tiene valor mientras está ABIERTA
    private LocalDateTime fechaApertura; // nullable

    public Caja() {
    }

    public int getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(int idCaja) {
        this.idCaja = idCaja;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getSaldoActual() {
        return saldoActual;
    }

    public void setSaldoActual(BigDecimal saldoActual) {
        this.saldoActual = saldoActual;
    }

    public EstadoCaja getEstado() {
        return estado;
    }

    public void setEstado(EstadoCaja estado) {
        this.estado = estado;
    }

    public Integer getIdUsuarioActual() {
        return idUsuarioActual;
    }

    public void setIdUsuarioActual(Integer idUsuarioActual) {
        this.idUsuarioActual = idUsuarioActual;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }
}