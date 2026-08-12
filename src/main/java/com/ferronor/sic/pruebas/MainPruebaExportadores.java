package com.ferronor.sic.pruebas;

import com.ferronor.sic.util.ExportadorCSV;
import com.ferronor.sic.util.ExportadorPDF;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MainPruebaExportadores {

    public static void main(String[] args) {
        probarCSV();
        probarPDFTabla();
        probarPDFComprobante();
        System.out.println("Listo. Revisa los 3 archivos generados en la carpeta del proyecto.");
    }

    private static void probarCSV() {
        List<String> encabezados = List.of("Código", "Cuenta", "Saldo Deudor", "Saldo Acreedor");
        List<List<String>> filas = List.of(
                List.of("101", "Caja", "5000.00", "0.00"),
                List.of("20", "Mercaderías", "30000.00", "0.00"),
                List.of("42", "Cuentas por pagar, \"comerciales\"", "0.00", "8000.00"),
                List.of("50", "Capital", "0.00", "20000.00")
        );
        ExportadorCSV.exportar("balance_prueba.csv", encabezados, filas);
        System.out.println("CSV generado: balance_prueba.csv");
    }

    private static void probarPDFTabla() {
        // Ahora con datos que sí cuadran (Debe = Haber = 35000), y la fila TOTAL
        // para probar la línea + negrita automática que detecta "TOTAL" en la fila.
        List<String> encabezados = List.of("Código", "Cuenta", "Saldo Deudor", "Saldo Acreedor");
        List<List<String>> filas = List.of(
                List.of("101", "Caja", "5000.00", "0.00"),
                List.of("20", "Mercaderías", "30000.00", "0.00"),
                List.of("42", "Cuentas por pagar comerciales", "0.00", "8000.00"),
                List.of("50", "Capital", "0.00", "27000.00"),
                List.of("", "TOTAL", "35000.00", "35000.00")
        );
        ExportadorPDF.exportarTabla("balance_prueba.pdf", "BALANCE DE COMPROBACIÓN AL 31/12/2026",
                encabezados, filas);
        System.out.println("PDF (tabla) generado: balance_prueba.pdf");
    }

    private static void probarPDFComprobante() {
        List<ExportadorPDF.ItemComprobante> items = List.of(
                new ExportadorPDF.ItemComprobante("Tornillo 1/2\"", new BigDecimal("10"),
                        new BigDecimal("2.50"), new BigDecimal("25.00")),
                new ExportadorPDF.ItemComprobante("Martillo", new BigDecimal("1"),
                        new BigDecimal("45.00"), new BigDecimal("45.00"))
        );
        ExportadorPDF.exportarComprobante("comprobante_prueba.pdf", "BOLETA DE VENTA", "B001", "000123",
                LocalDate.now(), "Juan Pérez", "12345678",
                items, new BigDecimal("59.32"), new BigDecimal("10.68"), new BigDecimal("70.00"));
        System.out.println("PDF (comprobante) generado: comprobante_prueba.pdf");
    }
}