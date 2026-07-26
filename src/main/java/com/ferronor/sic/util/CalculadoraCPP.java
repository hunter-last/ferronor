
package com.ferronor.sic.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CalculadoraCPP {

    private static final int ESCALA_COSTO = 4;

    private CalculadoraCPP() {
    }

    public static BigDecimal calcularNuevoCPP(BigDecimal cantidadActual, BigDecimal costoPromedioActual,
            BigDecimal cantidadEntrante, BigDecimal costoUnitarioEntrante) {
        BigDecimal valorActual = cantidadActual.multiply(costoPromedioActual);
        BigDecimal valorEntrante = cantidadEntrante.multiply(costoUnitarioEntrante);
        BigDecimal cantidadTotal = cantidadActual.add(cantidadEntrante);

        if (cantidadTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return valorActual.add(valorEntrante).divide(cantidadTotal, ESCALA_COSTO, RoundingMode.HALF_UP);
    }
}
