/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.modelo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LibroMayorItem {

    private final LocalDateTime fecha;
    private final String glosa;
    private final BigDecimal debe;
    private final BigDecimal haber;
    private final BigDecimal saldo;

    public LibroMayorItem(LocalDateTime fecha, String glosa, BigDecimal debe, BigDecimal haber, BigDecimal saldo) {
        this.fecha = fecha;
        this.glosa = glosa;
        this.debe = debe;
        this.haber = haber;
        this.saldo = saldo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getGlosa() {
        return glosa;
    }

    public BigDecimal getDebe() {
        return debe;
    }

    public BigDecimal getHaber() {
        return haber;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
