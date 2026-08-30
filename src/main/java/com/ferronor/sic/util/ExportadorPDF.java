package com.ferronor.sic.util;

import com.ferronor.sic.exception.ServiceException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportadorPDF {

    private static final float MARGEN = 40f;
    private static final float ALTO_FILA = 18f;
    private static final float TAMANO_FUENTE = 9f;

    private static final PDFont FUENTE_NORMAL = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FUENTE_NEGRITA = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDColor GRIS_CLARO = new PDColor(new float[]{0.92f, 0.92f, 0.92f}, PDDeviceRGB.INSTANCE);

    private ExportadorPDF() {
    }

    public static void exportarTabla(String rutaArchivo, String titulo,
            List<String> encabezados, List<List<String>> filas) {
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);
            float anchoColumna = (pagina.getMediaBox().getWidth() - 2 * MARGEN) / encabezados.size();
            float y = pagina.getMediaBox().getHeight() - MARGEN;

            PDPageContentStream cs = new PDPageContentStream(documento, pagina);
            y = escribirTitulo(cs, titulo, y);
            y = escribirFila(cs, encabezados, anchoColumna, y, true);

            for (List<String> fila : filas) {
                if (y < MARGEN + ALTO_FILA) {
                    cs.close();
                    pagina = new PDPage(PDRectangle.A4);
                    documento.addPage(pagina);
                    y = pagina.getMediaBox().getHeight() - MARGEN;
                    cs = new PDPageContentStream(documento, pagina);
                    y = escribirFila(cs, encabezados, anchoColumna, y, true);
                }

                boolean esTotal = !fila.isEmpty() && fila.stream().anyMatch(v -> v != null && v.trim().equalsIgnoreCase("TOTAL"));
                y = escribirFila(cs, fila, anchoColumna, y, esTotal);
            }
            cs.close();
            documento.save(rutaArchivo);
        } catch (IOException e) {
            throw new ServiceException("Error al exportar a PDF: " + rutaArchivo, e);
        }
    }

    public record ItemComprobante(String nombreProducto, BigDecimal cantidad,
            BigDecimal precioUnitario, BigDecimal subtotal) {

    }

    public static void exportarComprobante(String rutaArchivo, String tipoComprobante, String serie,
            String numero, LocalDate fecha, String nombreCliente, String documentoCliente,
            List<ItemComprobante> items, BigDecimal subtotal, BigDecimal igv, BigDecimal total) {
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(PDRectangle.A5);
            documento.addPage(pagina);
            float anchoPagina = pagina.getMediaBox().getWidth() - 2 * MARGEN;
            float y = pagina.getMediaBox().getHeight() - MARGEN;
            PDPageContentStream cs = new PDPageContentStream(documento, pagina);

            y = escribirTitulo(cs, tipoComprobante + " " + serie + "-" + numero, y);
            y = escribirTexto(cs, "Fecha: " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), y);
            y = escribirTexto(cs, "Cliente: " + nombreCliente + " (" + documentoCliente + ")", y);
            y -= ALTO_FILA * 0.5f;

            List<String> encabezadosItem = List.of("Producto", "Cant.", "P. Unit.", "Subtotal");
            float anchoColumna = anchoPagina / encabezadosItem.size();
            y = escribirFila(cs, encabezadosItem, anchoColumna, y, true);

            for (ItemComprobante item : items) {
                List<String> fila = List.of(item.nombreProducto(), item.cantidad().toPlainString(),
                        item.precioUnitario().toPlainString(), item.subtotal().toPlainString());
                y = escribirFila(cs, fila, anchoColumna, y, false);
            }

            y -= ALTO_FILA * 0.5f;
            y = escribirTexto(cs, "Subtotal: " + subtotal.toPlainString(), y);
            y = escribirTexto(cs, "IGV: " + igv.toPlainString(), y);
            y -= ALTO_FILA * 0.3f;
            escribirTotalDestacado(cs, "TOTAL: S/ " + total.toPlainString(), y, anchoPagina);

            cs.close();
            documento.save(rutaArchivo);
        } catch (IOException e) {
            throw new ServiceException("Error al exportar comprobante a PDF: " + rutaArchivo, e);
        }
    }

    private static void escribirTotalDestacado(PDPageContentStream cs, String texto, float y, float ancho)
            throws IOException {
        float alturaCaja = ALTO_FILA * 1.4f;
        float yCaja = y - alturaCaja + 4f;

        cs.setNonStrokingColor(GRIS_CLARO);
        cs.addRect(MARGEN, yCaja, ancho, alturaCaja);
        cs.fill();

        cs.setNonStrokingColor(0, 0, 0);
        cs.beginText();
        cs.setFont(FUENTE_NEGRITA, 13);
        cs.newLineAtOffset(MARGEN + 8f, yCaja + alturaCaja / 2f - 4f);
        cs.showText(texto);
        cs.endText();
    }

    private static float escribirTitulo(PDPageContentStream cs, String titulo, float y) throws IOException {
        cs.beginText();
        cs.setFont(FUENTE_NEGRITA, 14);
        cs.newLineAtOffset(MARGEN, y);
        cs.showText(titulo);
        cs.endText();
        return y - ALTO_FILA * 1.5f;
    }

    private static float escribirTexto(PDPageContentStream cs, String texto, float y) throws IOException {
        cs.beginText();
        cs.setFont(FUENTE_NORMAL, TAMANO_FUENTE + 1);
        cs.newLineAtOffset(MARGEN, y);
        cs.showText(texto);
        cs.endText();
        return y - ALTO_FILA;
    }

    private static float escribirFila(PDPageContentStream cs, List<String> valores, float anchoColumna,
            float y, boolean negrita) throws IOException {
        PDFont fuente = negrita ? FUENTE_NEGRITA : FUENTE_NORMAL;
        for (int i = 0; i < valores.size(); i++) {
            cs.beginText();
            cs.setFont(fuente, TAMANO_FUENTE);
            cs.newLineAtOffset(MARGEN + i * anchoColumna, y);
            cs.showText(valores.get(i) == null ? "" : valores.get(i));
            cs.endText();
        }
        return y - ALTO_FILA;
    }
}
