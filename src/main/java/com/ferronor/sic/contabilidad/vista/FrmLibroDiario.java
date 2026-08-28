package com.ferronor.sic.contabilidad.vista;

import com.ferronor.sic.contabilidad.logica.ContabilidadService;
import com.ferronor.sic.contabilidad.modelo.AsientoContable;
import com.ferronor.sic.contabilidad.modelo.DetalleAsiento;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * FrmLibroDiario
 *
 * Reporte jerárquico de asientos contables.
 *
 * Arquitectura:
 *
 * FrmLibroDiario ↓ ServiceFactory.contabilidadService() ↓ ContabilidadService ↓
 * obtenerLibroDiario(desde, hasta)
 *
 * La vista NO accede directamente a DAO ni a LibroDiarioService.
 */
public class FrmLibroDiario extends javax.swing.JDialog {

    // ============================================================
    // SERVICIO
    // ============================================================
    private final ContabilidadService contabilidadService
            = ServiceFactory.contabilidadService();

    // ============================================================
    // ESTADO DE LA VISTA
    // ============================================================
    private List<AsientoContable> asientosConsultados
            = new ArrayList<>();

    // ============================================================
    // FORMATOS
    // ============================================================
    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_HORA
            = DateTimeFormatter.ofPattern("HH:mm");

    private static final DecimalFormat FORMATO_MONEDA;

    static {

        DecimalFormatSymbols symbols
                = DecimalFormatSymbols.getInstance(
                        Locale.US
                );

        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');

        FORMATO_MONEDA = new DecimalFormat(
                "#,##0.00",
                symbols
        );

        FORMATO_MONEDA.setGroupingUsed(true);
    }

    // ============================================================
    // COLORES / ESTILO
    // ============================================================
    private static final Color COLOR_TEXTO
            = new Color(43, 47, 46);

    private static final Color COLOR_TEXTO_SECUNDARIO
            = new Color(107, 113, 110);

    private static final Color COLOR_TEXTO_SUAVE
            = new Color(154, 160, 157);

    private static final Color COLOR_PETROLEO
            = new Color(53, 84, 92);

    private static final Color COLOR_BORDE
            = new Color(216, 220, 218);

    private static final Color COLOR_FONDO_DETALLE
            = new Color(245, 246, 245);

    private static final Color COLOR_EXITO
            = new Color(63, 125, 82);

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    public FrmLibroDiario(
            java.awt.Frame parent,
            boolean modal) {

        super(parent, modal);

        initComponents();

        configurarFormulario();
    }

    // ============================================================
    // CONFIGURACIÓN GENERAL
    // ============================================================
    private void configurarFormulario() {

        configurarCabecera();

        configurarFechas();

        configurarAdvertenciaFecha();

        configurarPanelAcomodador();

        configurarTablasPlantilla();

        configurarResumenInicial();

        limpiarResultados();
    }

    // ============================================================
    // CABECERA
    // ============================================================
    private void configurarCabecera() {

        try {

            SesionUsuario sesion
                    = SesionUsuario.actual();

            lblNombreApellidoUsuario.setText(
                    valorTexto(
                            sesion.getNombreCompleto()
                    )
            );

        } catch (RuntimeException ex) {

            lblNombreApellidoUsuario.setText(
                    "Usuario actual"
            );
        }

        actualizarFechaHoraCabecera();
    }

    private void actualizarFechaHoraCabecera() {

        LocalDateTime ahora
                = LocalDateTime.now();

        lblFechaActual.setText(
                ahora.format(
                        FORMATO_FECHA
                )
        );

        lblHoraActual.setText(
                ahora.format(
                        FORMATO_HORA
                )
        );
    }

    // ============================================================
    // FECHAS
    // ============================================================
    private void configurarFechas() {

        jdcFechaDesde.setDateFormatString(
                "dd/MM/yyyy"
        );

        jdcFechaHasta.setDateFormatString(
                "dd/MM/yyyy"
        );

        jdcFechaDesde.setFocusable(true);
        jdcFechaHasta.setFocusable(true);
    }

    // ============================================================
    // ADVERTENCIA DE RANGO
    // ============================================================
    private void configurarAdvertenciaFecha() {

        /*
         * El .form ya tiene este panel comprimido.
         *
         * Solo se muestra cuando:
         *
         * fechaDesde > fechaHasta
         *
         * Al cambiar la visibilidad GroupLayout redistribuye
         * automáticamente el espacio.
         */
        pnlAdvertenciaRangoFechaInvalido.setVisible(false);
    }

    private void mostrarAdvertenciaRangoFecha(
            boolean mostrar) {

        pnlAdvertenciaRangoFechaInvalido.setVisible(
                mostrar
        );

        pnlConsultaLibroDiario.revalidate();
        pnlConsultaLibroDiario.repaint();
    }

    // ============================================================
    // PANEL ACOMODADOR
    // ============================================================
    private void configurarPanelAcomodador() {

        /*
         * Los pnlAsientoNroN y pnlAsientoNroN1 del .form son
         * referencias visuales.
         *
         * En ejecución, los bloques deben construirse de manera
         * dinámica porque el número de asientos es variable.
         */
        pnlAcomodador.removeAll();

        pnlAcomodador.setLayout(
                new BoxLayout(
                        pnlAcomodador,
                        BoxLayout.Y_AXIS
                )
        );

        pnlAcomodador.setBorder(
                BorderFactory.createEmptyBorder(
                        8,
                        8,
                        8,
                        8
                )
        );

        pnlAcomodador.revalidate();
        pnlAcomodador.repaint();
    }

    // ============================================================
    // TABLAS PLANTILLA
    // ============================================================
    private void configurarTablasPlantilla() {

        configurarTablaDetalle(
                tblDetalleAsientoN
        );

        configurarTablaDetalle(
                tblDetalleAsientoN1
        );
    }

    private void configurarTablaDetalle(
            JTable tabla) {

        if (tabla == null) {
            return;
        }

        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tabla.setRowSelectionAllowed(false);

        tabla.setColumnSelectionAllowed(false);

        tabla.setCellSelectionEnabled(false);

        tabla.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        tabla.getTableHeader().setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        10
                )
        );

        tabla.setRowHeight(22);

        tabla.setShowVerticalLines(false);

        tabla.setShowHorizontalLines(false);

        tabla.setGridColor(
                COLOR_BORDE
        );

        tabla.getTableHeader().setReorderingAllowed(
                false
        );

        tabla.getTableHeader().setResizingAllowed(
                true
        );

        configurarRendererTabla(
                tabla
        );
    }

    private void configurarRendererTabla(
            JTable tabla) {

        DefaultTableCellRenderer rendererTexto
                = new DefaultTableCellRenderer();

        rendererTexto.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        DefaultTableCellRenderer rendererNumero
                = new DefaultTableCellRenderer();

        rendererNumero.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        if (tabla.getColumnModel().getColumnCount() >= 3) {

            tabla.getColumnModel()
                    .getColumn(0)
                    .setCellRenderer(
                            rendererTexto
                    );

            tabla.getColumnModel()
                    .getColumn(1)
                    .setCellRenderer(
                            rendererNumero
                    );

            tabla.getColumnModel()
                    .getColumn(2)
                    .setCellRenderer(
                            rendererNumero
                    );
        }
    }

    // ============================================================
    // RESUMEN INICIAL
    // ============================================================
    private void configurarResumenInicial() {

        lblCantAsientos.setText(
                "0"
        );

        lblValorTotalDebe.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        lblValorTotalHaber.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        txtCondicionDebeHaber.setText(
                "• SIN DATOS"
        );

        txtCondicionDebeHaber.setEditable(
                false
        );
    }

    // ============================================================
    // CONSULTA
    // ============================================================
    private void consultarLibroDiario() {

        LocalDate fechaDesde
                = obtenerFecha(
                        jdcFechaDesde
                );

        LocalDate fechaHasta
                = obtenerFecha(
                        jdcFechaHasta
                );

        /*
         * El servicio necesita un rango completo.
         */
        if (fechaDesde == null
                || fechaHasta == null) {

            mostrarAdvertenciaRangoFecha(
                    false
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione la fecha desde y la fecha hasta.",
                    "Consulta del Libro Diario",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        /*
         * Regla:
         *
         * fechaDesde > fechaHasta
         *
         * no se consulta.
         */
        if (fechaDesde.isAfter(
                fechaHasta
        )) {

            mostrarAdvertenciaRangoFecha(
                    true
            );

            limpiarResultados();

            return;
        }

        mostrarAdvertenciaRangoFecha(
                false
        );

        try {

            List<AsientoContable> resultados
                    = contabilidadService.obtenerLibroDiario(
                            fechaDesde,
                            fechaHasta
                    );

            asientosConsultados
                    = resultados == null
                            ? new ArrayList<>()
                            : new ArrayList<>(
                                    resultados
                            );

            cargarReporte(
                    asientosConsultados
            );

            actualizarResumen(
                    asientosConsultados
            );

            actualizarFechaHoraCabecera();

        } catch (RuntimeException ex) {

            asientosConsultados
                    = new ArrayList<>();

            limpiarResultados();

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al consultar el Libro Diario",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // CARGAR REPORTE
    // ============================================================
    private void cargarReporte(
            List<AsientoContable> asientos) {

        pnlAcomodador.removeAll();

        if (asientos == null
                || asientos.isEmpty()) {

            mostrarEstadoSinResultados();

            pnlAcomodador.revalidate();
            pnlAcomodador.repaint();

            return;
        }

        ocultarEstadoSinResultados();

        for (AsientoContable asiento
                : asientos) {

            if (asiento == null) {
                continue;
            }

            JPanel panelAsiento
                    = construirPanelAsiento(
                            asiento
                    );

            pnlAcomodador.add(
                    panelAsiento
            );

            pnlAcomodador.add(
                    Box.createVerticalStrut(
                            8
                    )
            );
        }

        pnlAcomodador.add(
                Box.createVerticalGlue()
        );

        pnlAcomodador.revalidate();
        pnlAcomodador.repaint();
    }

    // ============================================================
    // CONSTRUIR ASIENTO
    // ============================================================
    private JPanel construirPanelAsiento(
            AsientoContable asiento) {

        JPanel panel
                = new JPanel();

        panel.setOpaque(true);

        panel.setBackground(
                Color.WHITE
        );

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                COLOR_BORDE
                        ),
                        BorderFactory.createEmptyBorder(
                                8,
                                10,
                                10,
                                10
                        )
                )
        );

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        // --------------------------------------------------------
        // CABECERA DEL ASIENTO
        // --------------------------------------------------------
        JPanel cabecera
                = new JPanel(
                        new BorderLayout()
                );

        cabecera.setOpaque(false);

        JLabel lblAsiento
                = crearLabel(
                        "ASIENTO N.° "
                        + asiento.getIdAsiento(),
                        new Font(
                                "Consolas",
                                Font.BOLD,
                                12
                        ),
                        COLOR_TEXTO
                );

        String estado
                = asiento.getEstado() == null
                ? "-"
                : asiento.getEstado().name();

        JTextField txtEstado
                = crearCampoVisual(
                        "• " + estado
                );

        cabecera.add(
                lblAsiento,
                BorderLayout.WEST
        );

        cabecera.add(
                txtEstado,
                BorderLayout.EAST
        );

        panel.add(
                cabecera
        );

        panel.add(
                Box.createVerticalStrut(
                        5
                )
        );

        panel.add(
                new javax.swing.JSeparator()
        );

        panel.add(
                Box.createVerticalStrut(
                        8
                )
        );

        // --------------------------------------------------------
        // DATOS GENERALES
        // --------------------------------------------------------
        JPanel datos
                = construirDatosAsiento(
                        asiento
                );

        panel.add(
                datos
        );

        panel.add(
                Box.createVerticalStrut(
                        10
                )
        );

        // --------------------------------------------------------
        // DETALLE
        // --------------------------------------------------------
        JLabel lblDetalle
                = crearLabel(
                        "DETALLE",
                        new Font(
                                "Consolas",
                                Font.BOLD,
                                12
                        ),
                        COLOR_TEXTO
                );

        panel.add(
                lblDetalle
        );

        panel.add(
                Box.createVerticalStrut(
                        5
                )
        );

        JScrollPane scrollDetalle
                = construirDetalleAsiento(
                        asiento
                );

        panel.add(
                scrollDetalle
        );

        return panel;
    }

    // ============================================================
    // DATOS DEL ASIENTO
    // ============================================================
    private JPanel construirDatosAsiento(
            AsientoContable asiento) {

        JPanel contenedor
                = new JPanel(
                        new GridBagLayout()
                );

        contenedor.setOpaque(false);

        GridBagConstraints gbc
                = new GridBagConstraints();

        gbc.insets = new Insets(
                2,
                4,
                4,
                18
        );

        gbc.anchor
                = GridBagConstraints.NORTHWEST;

        gbc.fill
                = GridBagConstraints.HORIZONTAL;

        gbc.weightx = 1.0;

        // --------------------------------------------------------
        // FECHA
        // --------------------------------------------------------
        agregarDato(
                contenedor,
                gbc,
                0,
                0,
                "FECHA",
                formatearFechaAsiento(
                        asiento.getFecha()
                )
        );

        // --------------------------------------------------------
        // ORIGEN
        // --------------------------------------------------------
        agregarDato(
                contenedor,
                gbc,
                1,
                0,
                "ORIGEN",
                formatearOrigen(
                        asiento
                                .getOrigen()
                )
        );

        // --------------------------------------------------------
        // DOCUMENTO
        // --------------------------------------------------------
        agregarDato(
                contenedor,
                gbc,
                2,
                0,
                "DOCUMENTO",
                formatearDocumento(
                        asiento
                                .getIdDocumentoOrigen()
                )
        );

        // --------------------------------------------------------
        // ESTADO
        // --------------------------------------------------------
        agregarDato(
                contenedor,
                gbc,
                3,
                0,
                "ESTADO",
                asiento.getEstado() == null
                ? "-"
                : asiento
                        .getEstado()
                        .name()
        );

        // --------------------------------------------------------
        // GLOSA
        // --------------------------------------------------------
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;

        JPanel glosa
                = construirDato(
                        "GLOSA",
                        valorTexto(
                                asiento.getGlosa()
                        )
                );

        contenedor.add(
                glosa,
                gbc
        );

        return contenedor;
    }

    private void agregarDato(
            JPanel contenedor,
            GridBagConstraints plantilla,
            int columna,
            int fila,
            String etiqueta,
            String valor) {

        GridBagConstraints gbc
                = (GridBagConstraints) plantilla.clone();

        gbc.gridx = columna;
        gbc.gridy = fila;
        gbc.gridwidth = 1;

        contenedor.add(
                construirDato(
                        etiqueta,
                        valor
                ),
                gbc
        );
    }

    private JPanel construirDato(
            String etiqueta,
            String valor) {

        JPanel panel
                = new JPanel();

        panel.setOpaque(false);

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel lblEtiqueta
                = crearLabel(
                        etiqueta,
                        new Font(
                                "Segoe UI",
                                Font.PLAIN,
                                10
                        ),
                        COLOR_TEXTO_SECUNDARIO
                );

        JLabel lblValor
                = crearLabel(
                        valor,
                        new Font(
                                "Consolas",
                                Font.PLAIN,
                                12
                        ),
                        COLOR_TEXTO
                );

        panel.add(
                lblEtiqueta
        );

        panel.add(
                Box.createVerticalStrut(
                        2
                )
        );

        panel.add(
                lblValor
        );

        return panel;
    }

    // ============================================================
    // TABLA DETALLE
    // ============================================================
    private JScrollPane construirDetalleAsiento(
            AsientoContable asiento) {

        DefaultTableModel modelo
                = new DefaultTableModel(
                        new Object[]{
                            "ID CUENTA",
                            "DEBE",
                            "HABER"
                        },
                        0
                ) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };

        List<DetalleAsiento> detalles
                = asiento.getDetalles();

        BigDecimal totalDebe
                = BigDecimal.ZERO;

        BigDecimal totalHaber
                = BigDecimal.ZERO;

        if (detalles != null) {

            for (DetalleAsiento detalle
                    : detalles) {

                if (detalle == null) {
                    continue;
                }

                BigDecimal debe
                        = normalizarMonto(
                                detalle.getDebe()
                        );

                BigDecimal haber
                        = normalizarMonto(
                                detalle.getHaber()
                        );

                totalDebe
                        = totalDebe.add(
                                debe
                        );

                totalHaber
                        = totalHaber.add(
                                haber
                        );

                modelo.addRow(
                        new Object[]{
                            String.valueOf(
                                    detalle.getIdCuenta()
                            ),
                            debe.signum() == 0
                            ? ""
                            : formatearMoneda(
                                    debe
                            ),
                            haber.signum() == 0
                            ? ""
                            : formatearMoneda(
                                    haber
                            )
                        }
                );
            }
        }

        /*
         * Fila visual de total del asiento.
         */
        modelo.addRow(
                new Object[]{
                    "TOTAL",
                    formatearMoneda(
                            totalDebe
                    ),
                    formatearMoneda(
                            totalHaber
                    )
                }
        );

        JTable tabla
                = new JTable(
                        modelo
                );

        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tabla.setRowSelectionAllowed(false);

        tabla.setColumnSelectionAllowed(false);

        tabla.setCellSelectionEnabled(false);

        tabla.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        tabla.getTableHeader().setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        10
                )
        );

        tabla.setRowHeight(
                22
        );

        tabla.setShowVerticalLines(false);

        tabla.setShowHorizontalLines(false);

        tabla.setIntercellSpacing(
                new Dimension(
                        0,
                        0
                )
        );

        tabla.setGridColor(
                COLOR_BORDE
        );

        tabla.getTableHeader().setReorderingAllowed(
                false
        );

        tabla.setAutoResizeMode(
                JTable.AUTO_RESIZE_LAST_COLUMN
        );

        tabla.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(
                        140
                );

        tabla.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(
                        220
                );

        tabla.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(
                        220
                );

        /*
         * ID cuenta
         */
        DefaultTableCellRenderer rendererCuenta
                = new DefaultTableCellRenderer();

        rendererCuenta.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        rendererCuenta.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        11
                )
        );

        /*
         * Debe/Haber
         */
        DefaultTableCellRenderer rendererMonto
                = new DefaultTableCellRenderer();

        rendererMonto.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        rendererMonto.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        11
                )
        );

        tabla.getColumnModel()
                .getColumn(0)
                .setCellRenderer(
                        rendererCuenta
                );

        tabla.getColumnModel()
                .getColumn(1)
                .setCellRenderer(
                        rendererMonto
                );

        tabla.getColumnModel()
                .getColumn(2)
                .setCellRenderer(
                        rendererMonto
                );

        /*
         * Última fila = total.
         */
        int filaTotal
                = modelo.getRowCount() - 1;

        tabla.getSelectionModel().clearSelection();

        for (int columna = 0;
                columna < tabla.getColumnCount();
                columna++) {

            DefaultTableCellRenderer rendererTotal
                    = new DefaultTableCellRenderer() {

                @Override
                public java.awt.Component
                        getTableCellRendererComponent(
                                JTable table,
                                Object value,
                                boolean isSelected,
                                boolean hasFocus,
                                int row,
                                int column) {

                    java.awt.Component componente
                            = super
                                    .getTableCellRendererComponent(
                                            table,
                                            value,
                                            false,
                                            false,
                                            row,
                                            column
                                    );

                    setBackground(
                            COLOR_FONDO_DETALLE
                    );

                    setForeground(
                            COLOR_TEXTO
                    );

                    setFont(
                            new Font(
                                    "Consolas",
                                    Font.BOLD,
                                    11
                            )
                    );

                    if (column == 0) {

                        setHorizontalAlignment(
                                SwingConstants.LEFT
                        );

                    } else {

                        setHorizontalAlignment(
                                SwingConstants.RIGHT
                        );
                    }

                    return componente;
                }
            };

            tabla.getColumnModel()
                    .getColumn(columna)
                    .setCellRenderer(
                            crearRendererConTotal(
                                    columna,
                                    filaTotal
                            )
                    );
        }

        /*
         * Renderer por fila para resaltar solo la fila TOTAL.
         */
        tabla.setDefaultRenderer(
                Object.class,
                new DefaultTableCellRenderer() {

            @Override
            public java.awt.Component
                    getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column) {

                JLabel label
                        = (JLabel) super
                                .getTableCellRendererComponent(
                                        table,
                                        value,
                                        false,
                                        false,
                                        row,
                                        column
                                );

                label.setBorder(
                        BorderFactory.createEmptyBorder(
                                4,
                                6,
                                4,
                                6
                        )
                );

                label.setOpaque(
                        true
                );

                if (row == filaTotal) {

                    label.setBackground(
                            COLOR_FONDO_DETALLE
                    );

                    label.setFont(
                            new Font(
                                    "Consolas",
                                    Font.BOLD,
                                    11
                            )
                    );

                    label.setHorizontalAlignment(
                            column == 0
                                    ? SwingConstants.LEFT
                                    : SwingConstants.RIGHT
                    );

                } else {

                    label.setBackground(
                            Color.WHITE
                    );

                    label.setFont(
                            new Font(
                                    "Consolas",
                                    Font.PLAIN,
                                    11
                            )
                    );

                    label.setHorizontalAlignment(
                            column == 0
                                    ? SwingConstants.LEFT
                                    : SwingConstants.RIGHT
                    );
                }

                return label;
            }
        });

        JScrollPane scroll
                = new JScrollPane(
                        tabla
                );

        scroll.setBorder(
                BorderFactory.createLineBorder(
                        COLOR_BORDE
                )
        );

        scroll.setPreferredSize(
                new Dimension(
                        700,
                        Math.max(
                                82,
                                Math.min(
                                        150,
                                        24
                                        * (modelo
                                                .getRowCount())
                                        + 32
                                )
                        )
                )
        );

        return scroll;
    }

    // ============================================================
    // RENDERER ESPECIAL PARA TOTAL
    // ============================================================
    private javax.swing.table.TableCellRenderer
            crearRendererConTotal(
                    int columna,
                    int filaTotal) {

        return new DefaultTableCellRenderer() {

            @Override
            public java.awt.Component
                    getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column) {

                JLabel label
                        = (JLabel) super
                                .getTableCellRendererComponent(
                                        table,
                                        value,
                                        false,
                                        false,
                                        row,
                                        column
                                );

                label.setOpaque(true);

                label.setBorder(
                        BorderFactory.createEmptyBorder(
                                4,
                                6,
                                4,
                                6
                        )
                );

                if (row == filaTotal) {

                    label.setBackground(
                            COLOR_FONDO_DETALLE
                    );

                    label.setFont(
                            new Font(
                                    "Consolas",
                                    Font.BOLD,
                                    11
                            )
                    );

                } else {

                    label.setBackground(
                            Color.WHITE
                    );

                    label.setFont(
                            new Font(
                                    "Consolas",
                                    Font.PLAIN,
                                    11
                            )
                    );
                }

                if (column == 0) {

                    label.setHorizontalAlignment(
                            SwingConstants.LEFT
                    );

                } else {

                    label.setHorizontalAlignment(
                            SwingConstants.RIGHT
                    );
                }

                return label;
            }
        };
    }

    // ============================================================
    // RESUMEN
    // ============================================================
    private void actualizarResumen(
            List<AsientoContable> asientos) {

        if (asientos == null
                || asientos.isEmpty()) {

            configurarResumenInicial();

            return;
        }

        int cantidadAsientos
                = 0;

        BigDecimal totalDebe
                = BigDecimal.ZERO;

        BigDecimal totalHaber
                = BigDecimal.ZERO;

        for (AsientoContable asiento
                : asientos) {

            if (asiento == null) {
                continue;
            }

            cantidadAsientos++;

            List<DetalleAsiento> detalles
                    = asiento.getDetalles();

            if (detalles == null) {
                continue;
            }

            for (DetalleAsiento detalle
                    : detalles) {

                if (detalle == null) {
                    continue;
                }

                totalDebe
                        = totalDebe.add(
                                normalizarMonto(
                                        detalle.getDebe()
                                )
                        );

                totalHaber
                        = totalHaber.add(
                                normalizarMonto(
                                        detalle.getHaber()
                                )
                        );
            }
        }

        lblCantAsientos.setText(
                String.valueOf(
                        cantidadAsientos
                )
        );

        lblValorTotalDebe.setText(
                formatearMoneda(
                        totalDebe
                )
        );

        lblValorTotalHaber.setText(
                formatearMoneda(
                        totalHaber
                )
        );

        boolean cuadrado
                = totalDebe.compareTo(
                        totalHaber
                ) == 0;

        if (cuadrado) {

            txtCondicionDebeHaber.setText(
                    "• DEBE = HABER"
            );

            txtCondicionDebeHaber.setForeground(
                    COLOR_EXITO
            );

        } else {

            txtCondicionDebeHaber.setText(
                    "• DEBE ≠ HABER"
            );

            txtCondicionDebeHaber.setForeground(
                    new Color(
                            171,
                            58,
                            52
                    )
            );
        }
    }

    // ============================================================
    // LIMPIAR
    // ============================================================
    private void limpiarFormulario() {

        jdcFechaDesde.setDate(null);

        jdcFechaHasta.setDate(null);

        mostrarAdvertenciaRangoFecha(
                false
        );

        limpiarResultados();

        actualizarFechaHoraCabecera();
    }

    private void limpiarResultados() {

        asientosConsultados
                = new ArrayList<>();

        pnlAcomodador.removeAll();

        mostrarEstadoSinResultados();

        configurarResumenInicial();

        pnlAcomodador.revalidate();
        pnlAcomodador.repaint();
    }

    // ============================================================
    // ESTADO SIN RESULTADOS
    // ============================================================
    private void mostrarEstadoSinResultados() {

        /*
         * El .form no dispone de un panel separado para estado
         * vacío, por lo que mostramos un mensaje dentro del
         * propio contenedor dinámico.
         */
        JPanel estado
                = new JPanel();

        estado.setOpaque(false);

        estado.setLayout(
                new BoxLayout(
                        estado,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel titulo
                = crearLabel(
                        "NO SE ENCONTRARON ASIENTOS",
                        new Font(
                                "Consolas",
                                Font.BOLD,
                                13
                        ),
                        COLOR_TEXTO_SECUNDARIO
                );

        titulo.setAlignmentX(
                JPanel.CENTER_ALIGNMENT
        );

        JLabel mensaje
                = crearLabel(
                        "No existen asientos contables dentro del rango seleccionado.",
                        new Font(
                                "Segoe UI",
                                Font.PLAIN,
                                12
                        ),
                        COLOR_TEXTO_SUAVE
                );

        mensaje.setAlignmentX(
                JPanel.CENTER_ALIGNMENT
        );

        estado.add(
                Box.createVerticalStrut(
                        40
                )
        );

        estado.add(
                titulo
        );

        estado.add(
                Box.createVerticalStrut(
                        6
                )
        );

        estado.add(
                mensaje
        );

        estado.add(
                Box.createVerticalStrut(
                        40
                )
        );

        pnlAcomodador.add(
                estado
        );
    }

    private void ocultarEstadoSinResultados() {

        pnlAcomodador.removeAll();
    }

    // ============================================================
    // CONVERSIÓN DE FECHAS
    // ============================================================
    private LocalDate obtenerFecha(
            com.toedter.calendar.JDateChooser chooser) {

        if (chooser == null
                || chooser.getDate() == null) {

            return null;
        }

        return chooser.getDate()
                .toInstant()
                .atZone(
                        ZoneId.systemDefault()
                )
                .toLocalDate();
    }

    // ============================================================
    // FORMATO FECHA ASIENTO
    // ============================================================
    private String formatearFechaAsiento(
            LocalDateTime fecha) {

        if (fecha == null) {

            return "-";
        }

        return fecha.toLocalDate()
                .format(
                        FORMATO_FECHA
                );
    }

    // ============================================================
    // ORIGEN
    // ============================================================
    private String formatearOrigen(
            Object origen) {

        if (origen == null) {

            return "-";
        }

        String texto
                = origen.toString();

        if (texto.isBlank()) {

            return "-";
        }

        /*
         * No se inventa una descripción de negocio.
         * Únicamente se transforma el nombre técnico para mejorar
         * la lectura visual.
         */
        return texto.replace(
                "_",
                " "
        );
    }

    // ============================================================
    // DOCUMENTO
    // ============================================================
    private String formatearDocumento(
            int idDocumentoOrigen) {

        if (idDocumentoOrigen <= 0) {

            return "-";
        }

        return String.valueOf(
                idDocumentoOrigen
        );
    }

    // ============================================================
    // FORMATO MONEDA
    // ============================================================
    private String formatearMoneda(
            BigDecimal valor) {

        BigDecimal monto
                = normalizarMonto(
                        valor
                );

        return "S/ "
                + FORMATO_MONEDA.format(
                        monto
                );
    }

    private BigDecimal normalizarMonto(
            BigDecimal valor) {

        if (valor == null) {

            return BigDecimal.ZERO;
        }

        return valor;
    }

    // ============================================================
    // LABEL
    // ============================================================
    private JLabel crearLabel(
            String texto,
            Font fuente,
            Color color) {

        JLabel label
                = new JLabel(
                        valorTexto(
                                texto
                        )
                );

        label.setFont(
                fuente
        );

        label.setForeground(
                color
        );

        return label;
    }

    // ============================================================
    // CAMPO VISUAL
    // ============================================================
    private JTextField crearCampoVisual(
            String texto) {

        JTextField campo
                = new JTextField(
                        texto
                );

        campo.setEditable(
                false
        );

        campo.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        campo.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        10
                )
        );

        campo.setOpaque(
                false
        );

        campo.setBorder(
                BorderFactory.createEmptyBorder(
                        2,
                        5,
                        2,
                        5
                )
        );

        return campo;
    }

    // ============================================================
    // TEXTO
    // ============================================================
    private String valorTexto(
            String texto) {

        if (texto == null
                || texto.isBlank()) {

            return "-";
        }

        return texto;
    }

    // ============================================================
    // ERROR
    // ============================================================
    private String obtenerMensajeError(
            RuntimeException ex) {

        if (ex == null
                || ex.getMessage() == null
                || ex.getMessage().isBlank()) {

            return "Ocurrió un error inesperado.";
        }

        return ex.getMessage();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSuperior = new javax.swing.JPanel();
        lblLibroDiario = new javax.swing.JLabel();
        lblRegistroCronologicoAsientosContables = new javax.swing.JLabel();
        lblNombreApellidoUsuario = new javax.swing.JLabel();
        lblFechaActual = new javax.swing.JLabel();
        lblHoraActual = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        pnlConsultaLibroDiario = new javax.swing.JPanel();
        lblFechaDesde = new javax.swing.JLabel();
        jdcFechaDesde = new com.toedter.calendar.JDateChooser();
        lblFechaHasta = new javax.swing.JLabel();
        jdcFechaHasta = new com.toedter.calendar.JDateChooser();
        btnConsultar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        pnlAdvertenciaRangoFechaInvalido = new javax.swing.JPanel();
        lblDiferenciaDeCaja = new javax.swing.JLabel();
        spnlLibroDiario = new javax.swing.JScrollPane();
        pnlAcomodador = new javax.swing.JPanel();
        pnlAsientoNroN = new javax.swing.JPanel();
        lblAsientoNroN = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        lblFechaN = new javax.swing.JLabel();
        lblValorFechaN = new javax.swing.JLabel();
        lblOrigenN = new javax.swing.JLabel();
        lblValorOrigenN = new javax.swing.JLabel();
        lblDocumentoN = new javax.swing.JLabel();
        lblValorDocumentoN = new javax.swing.JLabel();
        lblEstadoN = new javax.swing.JLabel();
        lblValorEstadoN = new javax.swing.JLabel();
        lblGlosaN = new javax.swing.JLabel();
        lblValorGlosaN = new javax.swing.JLabel();
        lblDetalleN = new javax.swing.JLabel();
        spnlDetalleAsientoN = new javax.swing.JScrollPane();
        tblDetalleAsientoN = new javax.swing.JTable();
        txtEstadoAsientoN = new javax.swing.JTextField();
        pnlAsientoNroN1 = new javax.swing.JPanel();
        lblAsientoNroN1 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        lblFechaN1 = new javax.swing.JLabel();
        lblValorFechaN1 = new javax.swing.JLabel();
        lblOrigenN1 = new javax.swing.JLabel();
        lblValorOrigenN1 = new javax.swing.JLabel();
        lblDocumentoN1 = new javax.swing.JLabel();
        lblValorDocumentoN1 = new javax.swing.JLabel();
        lblEstadoN1 = new javax.swing.JLabel();
        lblValorEstadoN1 = new javax.swing.JLabel();
        lblGlosaN1 = new javax.swing.JLabel();
        lblValorGlosaN1 = new javax.swing.JLabel();
        lblDetalleN1 = new javax.swing.JLabel();
        spnlDetalleAsientoN1 = new javax.swing.JScrollPane();
        tblDetalleAsientoN1 = new javax.swing.JTable();
        txtEstadoAsientoN1 = new javax.swing.JTextField();
        pnlResumen = new javax.swing.JPanel();
        lblAsientos = new javax.swing.JLabel();
        lblTotalDebe = new javax.swing.JLabel();
        lblTotalHaber = new javax.swing.JLabel();
        lblCantAsientos = new javax.swing.JLabel();
        lblValorTotalDebe = new javax.swing.JLabel();
        lblValorTotalHaber = new javax.swing.JLabel();
        txtCondicionDebeHaber = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblLibroDiario.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblLibroDiario.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblLibroDiario.setText("LIBRO DIARIO");
        lblLibroDiario.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        lblRegistroCronologicoAsientosContables.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblRegistroCronologicoAsientosContables.setText("Registro cronológico de los asientos contables ");

        lblNombreApellidoUsuario.setText("Nombre Apellido");

        lblFechaActual.setText("21/08/2026");

        lblHoraActual.setText("10:40");

        lblUsuario.setText("Usuario:");

        javax.swing.GroupLayout pnlSuperiorLayout = new javax.swing.GroupLayout(pnlSuperior);
        pnlSuperior.setLayout(pnlSuperiorLayout);
        pnlSuperiorLayout.setHorizontalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSuperiorLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblRegistroCronologicoAsientosContables, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblLibroDiario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(408, 408, 408)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblNombreApellidoUsuario))
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblFechaActual)
                        .addGap(18, 18, 18)
                        .addComponent(lblHoraActual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(12, 12, 12))
        );
        pnlSuperiorLayout.setVerticalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSuperiorLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNombreApellidoUsuario)
                            .addComponent(lblUsuario))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblRegistroCronologicoAsientosContables, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFechaActual)
                            .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblLibroDiario, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlConsultaLibroDiario.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. CONSULTA DEL LIBRO DIARIO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblFechaDesde.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFechaDesde.setText("FECHA DESDE");

        lblFechaHasta.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFechaHasta.setText("FECHA HASTA");

        btnConsultar.setBackground(new java.awt.Color(153, 51, 0));
        btnConsultar.setText("Consultar");
        btnConsultar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConsultarActionPerformed(evt);
            }
        });

        btnLimpiar.setBackground(new java.awt.Color(51, 51, 51));
        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        pnlAdvertenciaRangoFechaInvalido.setBackground(new java.awt.Color(102, 0, 0));

        lblDiferenciaDeCaja.setFont(new java.awt.Font("Consolas", 1, 14)); // NOI18N
        lblDiferenciaDeCaja.setForeground(new java.awt.Color(255, 0, 0));
        lblDiferenciaDeCaja.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblDiferenciaDeCaja.setText("• La fecha desde no puede ser posterior a la fecha hasta. ");

        javax.swing.GroupLayout pnlAdvertenciaRangoFechaInvalidoLayout = new javax.swing.GroupLayout(pnlAdvertenciaRangoFechaInvalido);
        pnlAdvertenciaRangoFechaInvalido.setLayout(pnlAdvertenciaRangoFechaInvalidoLayout);
        pnlAdvertenciaRangoFechaInvalidoLayout.setHorizontalGroup(
            pnlAdvertenciaRangoFechaInvalidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdvertenciaRangoFechaInvalidoLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lblDiferenciaDeCaja)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlAdvertenciaRangoFechaInvalidoLayout.setVerticalGroup(
            pnlAdvertenciaRangoFechaInvalidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdvertenciaRangoFechaInvalidoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblDiferenciaDeCaja, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlConsultaLibroDiarioLayout = new javax.swing.GroupLayout(pnlConsultaLibroDiario);
        pnlConsultaLibroDiario.setLayout(pnlConsultaLibroDiarioLayout);
        pnlConsultaLibroDiarioLayout.setHorizontalGroup(
            pnlConsultaLibroDiarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlConsultaLibroDiarioLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(pnlConsultaLibroDiarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlAdvertenciaRangoFechaInvalido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlConsultaLibroDiarioLayout.createSequentialGroup()
                        .addGroup(pnlConsultaLibroDiarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jdcFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(pnlConsultaLibroDiarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jdcFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnLimpiar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnConsultar)))
                .addGap(30, 30, 30))
        );
        pnlConsultaLibroDiarioLayout.setVerticalGroup(
            pnlConsultaLibroDiarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsultaLibroDiarioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlConsultaLibroDiarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlConsultaLibroDiarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnConsultar)
                        .addComponent(btnLimpiar))
                    .addGroup(pnlConsultaLibroDiarioLayout.createSequentialGroup()
                        .addGroup(pnlConsultaLibroDiarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblFechaDesde, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblFechaHasta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlConsultaLibroDiarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jdcFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jdcFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlAdvertenciaRangoFechaInvalido, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        spnlLibroDiario.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. LIBRO DIARIO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        pnlAsientoNroN.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblAsientoNroN.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblAsientoNroN.setText("ASIENTO N.° 15");

        lblFechaN.setBackground(new java.awt.Color(51, 51, 51));
        lblFechaN.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFechaN.setForeground(new java.awt.Color(153, 153, 153));
        lblFechaN.setText("FECHA");

        lblValorFechaN.setFont(new java.awt.Font("CourierThai", 0, 12)); // NOI18N
        lblValorFechaN.setText("25/08/2026 ");

        lblOrigenN.setBackground(new java.awt.Color(51, 51, 51));
        lblOrigenN.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblOrigenN.setForeground(new java.awt.Color(153, 153, 153));
        lblOrigenN.setText("ORIGEN");

        lblValorOrigenN.setFont(new java.awt.Font("CourierThai", 0, 12)); // NOI18N
        lblValorOrigenN.setText("COMPRA");

        lblDocumentoN.setBackground(new java.awt.Color(51, 51, 51));
        lblDocumentoN.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblDocumentoN.setForeground(new java.awt.Color(153, 153, 153));
        lblDocumentoN.setText("DOCUMENTO");

        lblValorDocumentoN.setFont(new java.awt.Font("CourierThai", 0, 12)); // NOI18N
        lblValorDocumentoN.setText("27");

        lblEstadoN.setBackground(new java.awt.Color(51, 51, 51));
        lblEstadoN.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblEstadoN.setForeground(new java.awt.Color(153, 153, 153));
        lblEstadoN.setText("ESTADO");

        lblValorEstadoN.setFont(new java.awt.Font("CourierThai", 0, 12)); // NOI18N
        lblValorEstadoN.setText("ACTIVO");

        lblGlosaN.setBackground(new java.awt.Color(51, 51, 51));
        lblGlosaN.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblGlosaN.setForeground(new java.awt.Color(153, 153, 153));
        lblGlosaN.setText("GLOSA");

        lblValorGlosaN.setFont(new java.awt.Font("CourierThai", 0, 12)); // NOI18N
        lblValorGlosaN.setText("COMPRA DE MERCADERIA");

        lblDetalleN.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblDetalleN.setText("DETALLE");

        tblDetalleAsientoN.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID CUENTA", "DEBE", "HABER"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlDetalleAsientoN.setViewportView(tblDetalleAsientoN);

        txtEstadoAsientoN.setEditable(false);
        txtEstadoAsientoN.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        txtEstadoAsientoN.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtEstadoAsientoN.setText("• ACTIVO");

        javax.swing.GroupLayout pnlAsientoNroNLayout = new javax.swing.GroupLayout(pnlAsientoNroN);
        pnlAsientoNroN.setLayout(pnlAsientoNroNLayout);
        pnlAsientoNroNLayout.setHorizontalGroup(
            pnlAsientoNroNLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAsientoNroNLayout.createSequentialGroup()
                .addGroup(pnlAsientoNroNLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlAsientoNroNLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(lblAsientoNroN)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtEstadoAsientoN, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlAsientoNroNLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlAsientoNroNLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1)
                            .addGroup(pnlAsientoNroNLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(pnlAsientoNroNLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnlAsientoNroNLayout.createSequentialGroup()
                                        .addComponent(lblDetalleN, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(pnlAsientoNroNLayout.createSequentialGroup()
                                        .addGroup(pnlAsientoNroNLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(pnlAsientoNroNLayout.createSequentialGroup()
                                                .addGroup(pnlAsientoNroNLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(lblValorFechaN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(lblFechaN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(lblGlosaN, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(56, 56, 56)
                                                .addGroup(pnlAsientoNroNLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(lblOrigenN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(lblValorOrigenN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(57, 57, 57)
                                                .addGroup(pnlAsientoNroNLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(lblDocumentoN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(lblValorDocumentoN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(91, 91, 91)
                                                .addGroup(pnlAsientoNroNLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(lblEstadoN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(lblValorEstadoN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                            .addComponent(lblValorGlosaN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(294, 294, 294))
                                    .addComponent(spnlDetalleAsientoN, javax.swing.GroupLayout.Alignment.TRAILING))))))
                .addContainerGap())
        );
        pnlAsientoNroNLayout.setVerticalGroup(
            pnlAsientoNroNLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAsientoNroNLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(pnlAsientoNroNLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAsientoNroN)
                    .addComponent(txtEstadoAsientoN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAsientoNroNLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFechaN, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblOrigenN, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDocumentoN, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEstadoN, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAsientoNroNLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblValorFechaN)
                    .addComponent(lblValorOrigenN)
                    .addComponent(lblValorDocumentoN)
                    .addComponent(lblValorEstadoN))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblGlosaN, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorGlosaN)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblDetalleN)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnlDetalleAsientoN, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlAsientoNroN1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblAsientoNroN1.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblAsientoNroN1.setText("ASIENTO N.° 16");

        lblFechaN1.setBackground(new java.awt.Color(51, 51, 51));
        lblFechaN1.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFechaN1.setForeground(new java.awt.Color(153, 153, 153));
        lblFechaN1.setText("FECHA");

        lblValorFechaN1.setFont(new java.awt.Font("CourierThai", 0, 12)); // NOI18N
        lblValorFechaN1.setText("25/08/2026 ");

        lblOrigenN1.setBackground(new java.awt.Color(51, 51, 51));
        lblOrigenN1.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblOrigenN1.setForeground(new java.awt.Color(153, 153, 153));
        lblOrigenN1.setText("ORIGEN");

        lblValorOrigenN1.setFont(new java.awt.Font("CourierThai", 0, 12)); // NOI18N
        lblValorOrigenN1.setText("VENTA ");

        lblDocumentoN1.setBackground(new java.awt.Color(51, 51, 51));
        lblDocumentoN1.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblDocumentoN1.setForeground(new java.awt.Color(153, 153, 153));
        lblDocumentoN1.setText("DOCUMENTO");

        lblValorDocumentoN1.setFont(new java.awt.Font("CourierThai", 0, 12)); // NOI18N
        lblValorDocumentoN1.setText("38 ");

        lblEstadoN1.setBackground(new java.awt.Color(51, 51, 51));
        lblEstadoN1.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblEstadoN1.setForeground(new java.awt.Color(153, 153, 153));
        lblEstadoN1.setText("ESTADO");

        lblValorEstadoN1.setFont(new java.awt.Font("CourierThai", 0, 12)); // NOI18N
        lblValorEstadoN1.setText("ACTIVO ");

        lblGlosaN1.setBackground(new java.awt.Color(51, 51, 51));
        lblGlosaN1.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblGlosaN1.setForeground(new java.awt.Color(153, 153, 153));
        lblGlosaN1.setText("GLOSA");

        lblValorGlosaN1.setFont(new java.awt.Font("CourierThai", 0, 12)); // NOI18N
        lblValorGlosaN1.setText("VENTA DE MERCADERIA");

        lblDetalleN1.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblDetalleN1.setText("DETALLE");

        tblDetalleAsientoN1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID CUENTA", "DEBE", "HABER"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlDetalleAsientoN1.setViewportView(tblDetalleAsientoN1);

        txtEstadoAsientoN1.setEditable(false);
        txtEstadoAsientoN1.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        txtEstadoAsientoN1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtEstadoAsientoN1.setText("• ACTIVO");

        javax.swing.GroupLayout pnlAsientoNroN1Layout = new javax.swing.GroupLayout(pnlAsientoNroN1);
        pnlAsientoNroN1.setLayout(pnlAsientoNroN1Layout);
        pnlAsientoNroN1Layout.setHorizontalGroup(
            pnlAsientoNroN1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAsientoNroN1Layout.createSequentialGroup()
                .addGroup(pnlAsientoNroN1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlAsientoNroN1Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(lblAsientoNroN1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtEstadoAsientoN1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlAsientoNroN1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlAsientoNroN1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator2)
                            .addGroup(pnlAsientoNroN1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(pnlAsientoNroN1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnlAsientoNroN1Layout.createSequentialGroup()
                                        .addComponent(lblDetalleN1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(pnlAsientoNroN1Layout.createSequentialGroup()
                                        .addGroup(pnlAsientoNroN1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(pnlAsientoNroN1Layout.createSequentialGroup()
                                                .addGroup(pnlAsientoNroN1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(lblValorFechaN1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(lblFechaN1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(lblGlosaN1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(56, 56, 56)
                                                .addGroup(pnlAsientoNroN1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(lblOrigenN1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(lblValorOrigenN1, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE))
                                                .addGap(57, 57, 57)
                                                .addGroup(pnlAsientoNroN1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(lblDocumentoN1, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)
                                                    .addComponent(lblValorDocumentoN1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(91, 91, 91)
                                                .addGroup(pnlAsientoNroN1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(lblEstadoN1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(lblValorEstadoN1, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)))
                                            .addComponent(lblValorGlosaN1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(294, 294, 294))
                                    .addComponent(spnlDetalleAsientoN1, javax.swing.GroupLayout.Alignment.TRAILING))))))
                .addContainerGap())
        );
        pnlAsientoNroN1Layout.setVerticalGroup(
            pnlAsientoNroN1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAsientoNroN1Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(pnlAsientoNroN1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAsientoNroN1)
                    .addComponent(txtEstadoAsientoN1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAsientoNroN1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFechaN1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblOrigenN1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDocumentoN1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEstadoN1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAsientoNroN1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblValorFechaN1)
                    .addComponent(lblValorOrigenN1)
                    .addComponent(lblValorDocumentoN1)
                    .addComponent(lblValorEstadoN1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblGlosaN1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorGlosaN1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblDetalleN1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnlDetalleAsientoN1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlAcomodadorLayout = new javax.swing.GroupLayout(pnlAcomodador);
        pnlAcomodador.setLayout(pnlAcomodadorLayout);
        pnlAcomodadorLayout.setHorizontalGroup(
            pnlAcomodadorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAcomodadorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAcomodadorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlAsientoNroN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlAsientoNroN1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlAcomodadorLayout.setVerticalGroup(
            pnlAcomodadorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAcomodadorLayout.createSequentialGroup()
                .addComponent(pnlAsientoNroN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAsientoNroN1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        spnlLibroDiario.setViewportView(pnlAcomodador);

        pnlResumen.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "03. RESUMEN", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblAsientos.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblAsientos.setText("ASIENTOS");

        lblTotalDebe.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalDebe.setText("TOTAL DEBE");

        lblTotalHaber.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalHaber.setText("TOTAL HABER");

        lblCantAsientos.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblCantAsientos.setText("5");

        lblValorTotalDebe.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalDebe.setText("S/ 5,420.00");

        lblValorTotalHaber.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalHaber.setText("S/ 5,420.00");

        txtCondicionDebeHaber.setEditable(false);
        txtCondicionDebeHaber.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCondicionDebeHaber.setText("• DEBE = HABER");

        javax.swing.GroupLayout pnlResumenLayout = new javax.swing.GroupLayout(pnlResumen);
        pnlResumen.setLayout(pnlResumenLayout);
        pnlResumenLayout.setHorizontalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblCantAsientos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblAsientos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(65, 65, 65)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblTotalDebe, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorTotalDebe, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                .addGap(65, 65, 65)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblValorTotalHaber, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                    .addComponent(lblTotalHaber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtCondicionDebeHaber, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlResumenLayout.setVerticalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtCondicionDebeHaber, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlResumenLayout.createSequentialGroup()
                        .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAsientos)
                            .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblTotalDebe, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblTotalHaber)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblCantAsientos)
                                .addComponent(lblValorTotalDebe))
                            .addComponent(lblValorTotalHaber))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(spnlLibroDiario, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlConsultaLibroDiario, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlSuperior, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlResumen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlConsultaLibroDiario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnlLibroDiario, javax.swing.GroupLayout.PREFERRED_SIZE, 452, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResumen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        // TODO add your handling code here:
        consultarLibroDiario();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
        limpiarResultados();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrmLibroDiario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmLibroDiario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmLibroDiario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmLibroDiario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmLibroDiario dialog = new FrmLibroDiario(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private com.toedter.calendar.JDateChooser jdcFechaDesde;
    private com.toedter.calendar.JDateChooser jdcFechaHasta;
    private javax.swing.JLabel lblAsientoNroN;
    private javax.swing.JLabel lblAsientoNroN1;
    private javax.swing.JLabel lblAsientos;
    private javax.swing.JLabel lblCantAsientos;
    private javax.swing.JLabel lblDetalleN;
    private javax.swing.JLabel lblDetalleN1;
    private javax.swing.JLabel lblDiferenciaDeCaja;
    private javax.swing.JLabel lblDocumentoN;
    private javax.swing.JLabel lblDocumentoN1;
    private javax.swing.JLabel lblEstadoN;
    private javax.swing.JLabel lblEstadoN1;
    private javax.swing.JLabel lblFechaActual;
    private javax.swing.JLabel lblFechaDesde;
    private javax.swing.JLabel lblFechaHasta;
    private javax.swing.JLabel lblFechaN;
    private javax.swing.JLabel lblFechaN1;
    private javax.swing.JLabel lblGlosaN;
    private javax.swing.JLabel lblGlosaN1;
    private javax.swing.JLabel lblHoraActual;
    private javax.swing.JLabel lblLibroDiario;
    private javax.swing.JLabel lblNombreApellidoUsuario;
    private javax.swing.JLabel lblOrigenN;
    private javax.swing.JLabel lblOrigenN1;
    private javax.swing.JLabel lblRegistroCronologicoAsientosContables;
    private javax.swing.JLabel lblTotalDebe;
    private javax.swing.JLabel lblTotalHaber;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblValorDocumentoN;
    private javax.swing.JLabel lblValorDocumentoN1;
    private javax.swing.JLabel lblValorEstadoN;
    private javax.swing.JLabel lblValorEstadoN1;
    private javax.swing.JLabel lblValorFechaN;
    private javax.swing.JLabel lblValorFechaN1;
    private javax.swing.JLabel lblValorGlosaN;
    private javax.swing.JLabel lblValorGlosaN1;
    private javax.swing.JLabel lblValorOrigenN;
    private javax.swing.JLabel lblValorOrigenN1;
    private javax.swing.JLabel lblValorTotalDebe;
    private javax.swing.JLabel lblValorTotalHaber;
    private javax.swing.JPanel pnlAcomodador;
    private javax.swing.JPanel pnlAdvertenciaRangoFechaInvalido;
    private javax.swing.JPanel pnlAsientoNroN;
    private javax.swing.JPanel pnlAsientoNroN1;
    private javax.swing.JPanel pnlConsultaLibroDiario;
    private javax.swing.JPanel pnlResumen;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JScrollPane spnlDetalleAsientoN;
    private javax.swing.JScrollPane spnlDetalleAsientoN1;
    private javax.swing.JScrollPane spnlLibroDiario;
    private javax.swing.JTable tblDetalleAsientoN;
    private javax.swing.JTable tblDetalleAsientoN1;
    private javax.swing.JTextField txtCondicionDebeHaber;
    private javax.swing.JTextField txtEstadoAsientoN;
    private javax.swing.JTextField txtEstadoAsientoN1;
    // End of variables declaration//GEN-END:variables
}
