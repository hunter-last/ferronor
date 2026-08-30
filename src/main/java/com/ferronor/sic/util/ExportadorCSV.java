package com.ferronor.sic.util;

import com.ferronor.sic.exception.ServiceException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// Exportador genérico: cualquier pantalla arma sus propios encabezados y filas
// (List<String>) a partir de su DTO — este exportador no conoce Balance de
// Comprobación, Libro Diario, etc., solo sabe escribir texto tabular a CSV.
public class ExportadorCSV {

    private ExportadorCSV() {
    }

    public static void exportar(String rutaArchivo, List<String> encabezados, List<List<String>> filas) {
        Path path = Path.of(rutaArchivo);
        // BOM UTF-8: sin esto, Excel abre tildes/ñ como caracteres corruptos.
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(path), StandardCharsets.UTF_8))) {
            writer.write('\uFEFF');
            writer.write(formatearFila(encabezados));
            writer.newLine();
            for (List<String> fila : filas) {
                writer.write(formatearFila(fila));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ServiceException("Error al exportar a CSV: " + rutaArchivo, e);
        }
    }

    private static String formatearFila(List<String> valores) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valores.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escapar(valores.get(i)));
        }
        return sb.toString();
    }

    // RFC 4180: si el valor tiene coma, comilla o salto de línea, va entre comillas
    // dobles, y las comillas internas se duplican.
    private static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        boolean necesitaComillas = valor.contains(",") || valor.contains("\"") || valor.contains("\n");
        String resultado = valor.replace("\"", "\"\"");
        return necesitaComillas ? "\"" + resultado + "\"" : resultado;
    }
}
