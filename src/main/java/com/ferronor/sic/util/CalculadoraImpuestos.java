/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.util;

import com.ferronor.sic.config.Constantes;
import java.math.BigDecimal;

public final class CalculadoraImpuestos {

    private CalculadoraImpuestos() {
    }

    public static BigDecimal calcularValorVenta(BigDecimal totalConIgv) {
        return totalConIgv.divide(Constantes.FACTOR_IGV, Constantes.ESCALA_MONEDA, Constantes.REDONDEO);
    }

    public static BigDecimal calcularIGV(BigDecimal totalConIgv) {
        return totalConIgv.subtract(calcularValorVenta(totalConIgv));
    }

    // Dirección inversa a la anterior: usada por Compras, donde costo_unitario
    // (y por tanto el subtotal de detalle_compra) NO incluye IGV.
    public static BigDecimal calcularIGVDesdeSubtotal(BigDecimal subtotalSinIgv) {
        return subtotalSinIgv.multiply(Constantes.IGV_PORCENTAJE)
                .setScale(Constantes.ESCALA_MONEDA, Constantes.REDONDEO);
    }

    public static BigDecimal calcularTotalConIgv(BigDecimal subtotalSinIgv) {
        return subtotalSinIgv.add(calcularIGVDesdeSubtotal(subtotalSinIgv));
    }
}