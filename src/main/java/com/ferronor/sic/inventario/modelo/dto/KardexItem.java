/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.inventario.modelo.dto;

import com.ferronor.sic.inventario.modelo.OrigenMovimiento;
import com.ferronor.sic.inventario.modelo.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class KardexItem {

    private final LocalDateTime fecha;
    private final TipoMovimiento tipoMovimiento;
    private final OrigenMovimiento origen;
    private final Integer idDocumentoOrigen;
    private final BigDecimal entrada;
    private final BigDecimal salida;
    private final BigDecimal saldoCantidad;
    private final BigDecimal costoUnitario;
    private final BigDecimal saldoValor;

    public KardexItem(LocalDateTime fecha, TipoMovimiento tipoMovimiento, OrigenMovimiento origen,
            Integer idDocumentoOrigen, BigDecimal entrada, BigDecimal salida,
            BigDecimal saldoCantidad, BigDecimal costoUnitario, BigDecimal saldoValor) {
        this.fecha = fecha;
        this.tipoMovimiento = tipoMovimiento;
        this.origen = origen;
        this.idDocumentoOrigen = idDocumentoOrigen;
        this.entrada = entrada;
        this.salida = salida;
        this.saldoCantidad = saldoCantidad;
        this.costoUnitario = costoUnitario;
        this.saldoValor = saldoValor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public TipoMovimiento getTipoMovimiento() {
        return tipoMovimiento;
    }

    public OrigenMovimiento getOrigen() {
        return origen;
    }

    public Integer getIdDocumentoOrigen() {
        return idDocumentoOrigen;
    }

    public BigDecimal getEntrada() {
        return entrada;
    }

    public BigDecimal getSalida() {
        return salida;
    }

    public BigDecimal getSaldoCantidad() {
        return saldoCantidad;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public BigDecimal getSaldoValor() {
        return saldoValor;
    }
}
