/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.util;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;

public class TablaExportUtil {

    private TablaExportUtil() {
    }

    public static List<String> obtenerEncabezados(JTable tabla) {
        List<String> encabezados = new ArrayList<>();
        for (int col = 0; col < tabla.getColumnCount(); col++) {
            encabezados.add(tabla.getColumnName(col));
        }
        return encabezados;
    }

    public static List<List<String>> obtenerFilas(JTable tabla) {
        List<List<String>> filas = new ArrayList<>();
        for (int fila = 0; fila < tabla.getRowCount(); fila++) {
            List<String> valores = new ArrayList<>();
            for (int col = 0; col < tabla.getColumnCount(); col++) {
                Object valor = tabla.getValueAt(fila, col);
                valores.add(valor == null ? "" : valor.toString());
            }
            filas.add(valores);
        }
        return filas;
    }
}