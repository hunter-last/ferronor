
package com.ferronor.sic.contabilidad.modelo;

import java.math.BigDecimal;

public class DetalleAsiento {

    private int idDetalle;
    private int idAsiento;
    private int idCuenta;
    private BigDecimal debe;
    private BigDecimal haber;

    private DetalleAsiento(int idCuenta, BigDecimal debe, BigDecimal haber) {
        this.idCuenta = idCuenta;
        this.debe = debe;
        this.haber = haber;
    }

    public static DetalleAsiento debe(int idCuenta, BigDecimal monto) {
        return new DetalleAsiento(idCuenta, monto, BigDecimal.ZERO);
    }

    public static DetalleAsiento haber(int idCuenta, BigDecimal monto) {
        return new DetalleAsiento(idCuenta, BigDecimal.ZERO, monto);
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdAsiento() {
        return idAsiento;
    }

    public void setIdAsiento(int idAsiento) {
        this.idAsiento = idAsiento;
    }

    public int getIdCuenta() {
        return idCuenta;
    }

    public BigDecimal getDebe() {
        return debe;
    }

    public BigDecimal getHaber() {
        return haber;
    }
    
}
