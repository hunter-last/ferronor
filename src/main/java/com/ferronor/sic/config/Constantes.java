/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.config;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Constantes {

    public static final BigDecimal IGV_PORCENTAJE = new BigDecimal("0.18");
    public static final BigDecimal FACTOR_IGV = BigDecimal.ONE.add(IGV_PORCENTAJE);
    public static final int ESCALA_MONEDA = 2;
    public static final RoundingMode REDONDEO = RoundingMode.HALF_UP;

    private Constantes() {
    }
}
