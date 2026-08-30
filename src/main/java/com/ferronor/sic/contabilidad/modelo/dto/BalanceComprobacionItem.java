/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.modelo.dto;

import java.math.BigDecimal;

public class BalanceComprobacionItem {
    private final int idCuenta;
    private final String codigo;
    private final String nombreCuenta;
    private final BigDecimal totalDebe;
    private final BigDecimal totalHaber;
    private final BigDecimal saldoDeudor;
    private final BigDecimal saldoAcreedor;

    public BalanceComprobacionItem(int idCuenta, String codigo, String nombreCuenta, BigDecimal totalDebe, BigDecimal totalHaber) {
        this.idCuenta = idCuenta;
        this.codigo = codigo;
        this.nombreCuenta = nombreCuenta;
        this.totalDebe = totalDebe;
        this.totalHaber = totalHaber;
        BigDecimal diferencia = totalDebe.subtract(totalHaber);
        this.saldoDeudor = diferencia.compareTo(BigDecimal.ZERO) > 0 ? diferencia : BigDecimal.ZERO;
        this.saldoAcreedor = diferencia.compareTo(BigDecimal.ZERO) < 0 ? diferencia.negate() : BigDecimal.ZERO;
    }

    public int getIdCuenta() { return idCuenta; }
    public String getCodigo() { return codigo; }
    public String getNombreCuenta() { return nombreCuenta; }
    public BigDecimal getTotalDebe() { return totalDebe; }
    public BigDecimal getTotalHaber() { return totalHaber; }
    public BigDecimal getSaldoDeudor() { return saldoDeudor; }
    public BigDecimal getSaldoAcreedor() { return saldoAcreedor; }
}