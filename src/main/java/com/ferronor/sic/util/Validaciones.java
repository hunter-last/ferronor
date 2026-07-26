/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.util;

import com.ferronor.sic.shared.RespuestaOperacion;

public final class Validaciones {

    private Validaciones() {
    }

    public static RespuestaOperacion<String> requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return RespuestaOperacion.error(campo + " es obligatorio");
        }
        return RespuestaOperacion.ok(valor.trim());
    }

    public static RespuestaOperacion<String> requerido(String valor, String campo, int maximo) {
        RespuestaOperacion<String> base = requerido(valor, campo);
        if (!base.isExito()) {
            return base;
        }
        if (base.getResultado().length() > maximo) {
            return RespuestaOperacion.error(campo + " no puede superar " + maximo + " caracteres");
        }
        return base;
    }
}
