/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.modelo.dto;

import java.math.BigDecimal;

public record DatosPagoParaAsiento(int idCompra, BigDecimal monto, String codigoCuentaEfectivo) {

}
