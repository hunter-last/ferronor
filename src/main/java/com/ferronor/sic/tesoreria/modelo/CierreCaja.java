package com.ferronor.sic.tesoreria.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CierreCaja {

    private int idCierre;
    private int idCaja;
    private LocalDateTime fecha;
    private BigDecimal saldoInicial;
    private BigDecimal saldoFinalSistema;
    private BigDecimal saldoFinalReal;
    private BigDecimal diferencia;
    private int idUsuario;

    public CierreCaja() {
    }

    public CierreCaja(int idCaja, BigDecimal saldoInicial, BigDecimal saldoFinalSistema,
            BigDecimal saldoFinalReal, int idUsuario) {
        this.idCaja = idCaja;
        this.saldoInicial = saldoInicial;
        this.saldoFinalSistema = saldoFinalSistema;
        this.saldoFinalReal = saldoFinalReal;
        this.diferencia = saldoFinalReal.subtract(saldoFinalSistema);
        this.idUsuario = idUsuario;
    }

    public int getIdCierre() {
        return idCierre;
    }

    public void setIdCierre(int idCierre) {
        this.idCierre = idCierre;
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

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(BigDecimal saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    public BigDecimal getSaldoFinalSistema() {
        return saldoFinalSistema;
    }

    public void setSaldoFinalSistema(BigDecimal saldoFinalSistema) {
        this.saldoFinalSistema = saldoFinalSistema;
    }

    public BigDecimal getSaldoFinalReal() {
        return saldoFinalReal;
    }

    public void setSaldoFinalReal(BigDecimal saldoFinalReal) {
        this.saldoFinalReal = saldoFinalReal;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
}