
package com.ferronor.sic.contabilidad.modelo.dto;

import java.math.BigDecimal;

public class BalanceGeneralItem {

    private final String codigo;
    private final String nombreCuenta;
    private final BigDecimal saldo;

    public BalanceGeneralItem(String codigo, String nombreCuenta, BigDecimal saldo) {
        this.codigo = codigo;
        this.nombreCuenta = nombreCuenta;
        this.saldo = saldo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombreCuenta() {
        return nombreCuenta;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
