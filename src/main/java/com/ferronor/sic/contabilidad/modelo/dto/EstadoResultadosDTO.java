/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.modelo.dto;

import java.math.BigDecimal;

public class EstadoResultadosDTO {

    private final BigDecimal totalIngresos;
    private final BigDecimal totalGastos;
    private final BigDecimal utilidadNeta;

    public EstadoResultadosDTO(BigDecimal totalIngresos, BigDecimal totalGastos) {
        this.totalIngresos = totalIngresos;
        this.totalGastos = totalGastos;
        this.utilidadNeta = totalIngresos.subtract(totalGastos);
    }

    public BigDecimal getTotalIngresos() {
        return totalIngresos;
    }

    public BigDecimal getTotalGastos() {
        return totalGastos;
    }

    public BigDecimal getUtilidadNeta() {
        return utilidadNeta;
    }

    public boolean tieneUtilidad() {
        return utilidadNeta.compareTo(BigDecimal.ZERO) >= 0;
    }

    public boolean tienePerdida() {
        return utilidadNeta.compareTo(BigDecimal.ZERO) < 0;
    }
}
