/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.ferronor.sic.contabilidad.vista;

import com.ferronor.sic.contabilidad.logica.ContabilidadService;
import com.ferronor.sic.contabilidad.modelo.dto.EstadoResultadosDTO;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;

import com.toedter.calendar.JDateChooser;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.math.BigDecimal;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class FrmEstadoDeResultados extends javax.swing.JDialog {

    // ============================================================
    // SERVICIO
    // ============================================================
    private final ContabilidadService contabilidadService
            = ServiceFactory.contabilidadService();

    // ============================================================
    // COMPONENTE CREADO FUERA DEL .FORM
    // ============================================================
    private JDateChooser jdcFechaCorte;

    private javax.swing.JLabel lblFechaCorte;

    // ============================================================
    // ESTADO
    // ============================================================
    private EstadoResultadosDTO resultadoActual;

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
    // COLORES
    // ============================================================
    private static final Color COLOR_UTILIDAD
            = new Color(63, 125, 82);

    private static final Color COLOR_PERDIDA
            = new Color(171, 58, 52);

    private static final Color COLOR_TEXTO
            = new Color(43, 47, 46);

    private static final Color COLOR_TEXTO_SECUNDARIO
            = new Color(107, 113, 110);

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    public FrmEstadoDeResultados(
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

        configurarModo();

        configurarPanelModoConsulta();

        configurarFechaCorte();

        configurarCombosPeriodo();

        configurarMensajes();

        configurarEstadoResultado();

        configurarEstadoInicial();
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

        actualizarFechaHora();
    }

    private void actualizarFechaHora() {

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
    // MODO DE CONSULTA
    // ============================================================
    private void configurarModo() {

        /*
         * El .form ya contiene ButtonGroup.
         */
        gbtnPeriodo.setSelected(true);

        gbtnFechaCorte.setSelected(false);

        gbtnPeriodo.addActionListener(
                e -> cambiarModoConsulta(false)
        );

        gbtnFechaCorte.addActionListener(
                e -> cambiarModoConsulta(true)
        );
    }

    private boolean modoFechaCorte() {

        return gbtnFechaCorte.isSelected();
    }

    private void cambiarModoConsulta(
            boolean fechaCorte) {

        limpiarMensajes();

        limpiarResultado();

        if (fechaCorte) {

            mostrarModoFechaCorte();

        } else {

            mostrarModoPeriodo();
        }
    }

    // ============================================================
    // PANEL DINÁMICO DEL MODO
    // ============================================================
    private void configurarPanelModoConsulta() {

        /*
         * El .form fue diseñado inicialmente pensando en
         * "Por Periodo".
         *
         * En Fecha de corte reemplazamos visualmente su
         * contenido por el JDateChooser creado en código.
         */
        prepararPanelModoConsulta();

        mostrarModoPeriodo();
    }

    private void prepararPanelModoConsulta() {

        pnlModoConsulta.setLayout(
                new GridBagLayout()
        );
    }

    private void mostrarModoPeriodo() {

        pnlModoConsulta.removeAll();

        cambiarTituloPanelModo(
                "POR PERIODO"
        );

        GridBagConstraints gbc
                = new GridBagConstraints();

        gbc.insets
                = new Insets(
                        4,
                        8,
                        4,
                        8
                );

        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.anchor
                = GridBagConstraints.WEST;

        pnlModoConsulta.add(
                lblAnnio,
                gbc
        );

        gbc.gridx = 1;

        pnlModoConsulta.add(
                cmbAnniosPeriodo,
                gbc
        );

        gbc.gridx = 2;

        pnlModoConsulta.add(
                lblMes,
                gbc
        );

        gbc.gridx = 3;

        pnlModoConsulta.add(
                cmbMesesPeriodo,
                gbc
        );

        pnlModoConsulta.revalidate();
        pnlModoConsulta.repaint();
    }

    private void mostrarModoFechaCorte() {

        pnlModoConsulta.removeAll();

        cambiarTituloPanelModo(
                "POR FECHA DE CORTE"
        );

        // ========================================================
        // CREAR COMPONENTES DINÁMICOS DEL MODO FECHA DE CORTE
        // ========================================================
        lblFechaCorte = new javax.swing.JLabel();

        lblFechaCorte.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        10
                )
        );

        lblFechaCorte.setText(
                "FECHA DE CORTE"
        );

        jdcFechaCorte = new JDateChooser();

        jdcFechaCorte.setDateFormatString(
                "dd/MM/yyyy"
        );

        jdcFechaCorte.setPreferredSize(
                new Dimension(
                        132,
                        28
                )
        );

        // ========================================================
        // POSICIONAR COMPONENTES
        // ========================================================
        GridBagConstraints gbc
                = new GridBagConstraints();

        gbc.insets
                = new Insets(
                        4,
                        8,
                        4,
                        8
                );

        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.anchor
                = GridBagConstraints.WEST;

        pnlModoConsulta.add(
                lblFechaCorte,
                gbc
        );

        gbc.gridx = 1;

        pnlModoConsulta.add(
                jdcFechaCorte,
                gbc
        );

        pnlModoConsulta.revalidate();
        pnlModoConsulta.repaint();
    }

    private void cambiarTituloPanelModo(
            String titulo) {

        if (pnlModoConsulta.getBorder() instanceof javax.swing.border.TitledBorder border) {

            border.setTitle(titulo);

            pnlModoConsulta.setBorder(
                    border
            );
        }
    }

    // ============================================================
    // FECHA DE CORTE
    // ============================================================
    private void configurarFechaCorte() {

        jdcFechaCorte = null;

        lblFechaCorte = null;
    }

    private LocalDate obtenerFechaCorte() {

        if (jdcFechaCorte == null
                || jdcFechaCorte.getDate() == null) {

            return null;
        }

        return jdcFechaCorte.getDate()
                .toInstant()
                .atZone(
                        ZoneId.systemDefault()
                )
                .toLocalDate();
    }

    // ============================================================
    // COMBOS DE PERÍODO
    // ============================================================
    private void configurarCombosPeriodo() {

        cargarAnios();

        cargarMeses();

        cmbAnniosPeriodo.setSelectedItem(
                String.valueOf(
                        LocalDate.now().getYear()
                )
        );

        cmbMesesPeriodo.setSelectedItem(
                obtenerNombreMes(
                        LocalDate.now().getMonth()
                )
        );
    }

    private void cargarAnios() {

        cmbAnniosPeriodo.removeAllItems();

        int anioActual
                = LocalDate.now().getYear();

        /*
         * Mostramos años recientes.
         * El período real se determina por la selección.
         */
        for (int anio = anioActual;
                anio >= anioActual - 5;
                anio--) {

            cmbAnniosPeriodo.addItem(
                    String.valueOf(anio)
            );
        }
    }

    private void cargarMeses() {

        cmbMesesPeriodo.removeAllItems();

        for (Month mes : Month.values()) {

            cmbMesesPeriodo.addItem(
                    obtenerNombreMes(mes)
            );
        }
    }

    private String obtenerNombreMes(
            Month mes) {

        String nombre
                = mes.getDisplayName(
                        java.time.format.TextStyle.FULL,
                        new Locale("es", "PE")
                );

        return nombre.substring(0, 1).toUpperCase()
                + nombre.substring(1);
    }

    // ============================================================
    // CONVERTIR PERÍODO
    // ============================================================
    private LocalDate[] obtenerPeriodoSeleccionado() {

        Object valorAnio
                = cmbAnniosPeriodo.getSelectedItem();

        Object valorMes
                = cmbMesesPeriodo.getSelectedItem();

        if (valorAnio == null
                || valorMes == null) {

            return null;
        }

        int anio;

        try {

            anio = Integer.parseInt(
                    valorAnio.toString()
            );

        } catch (NumberFormatException ex) {

            return null;
        }

        Month mes
                = obtenerMesPorNombre(
                        valorMes.toString()
                );

        if (mes == null) {

            return null;
        }

        LocalDate desde
                = LocalDate.of(
                        anio,
                        mes,
                        1
                );

        LocalDate hasta
                = desde.withDayOfMonth(
                        mes.length(
                                desde.isLeapYear()
                        )
                );

        return new LocalDate[]{
            desde,
            hasta
        };
    }

    private Month obtenerMesPorNombre(
            String nombre) {

        for (Month mes : Month.values()) {

            if (obtenerNombreMes(mes)
                    .equalsIgnoreCase(
                            nombre
                    )) {

                return mes;
            }
        }

        return null;
    }

    // ============================================================
    // MENSAJES
    // ============================================================
    private void configurarMensajes() {

        pnlAdvertenciaFechaCorteInvalida
                .setVisible(false);

        pnlMensajeFechaCorte
                .setVisible(false);

        lblMensajeFechaCorteInvalida.setText(
                "• Seleccione la fecha de corte."
        );

        lblMensajeFechaCorte.setText(
                ""
        );
    }

    private void mostrarAdvertencia(
            String mensaje) {

        lblMensajeFechaCorteInvalida.setText(
                "• " + mensaje
        );

        pnlMensajeFechaCorte.setVisible(
                false
        );

        pnlAdvertenciaFechaCorteInvalida
                .setVisible(true);

        pnlConsultaFechaBalanzaComprobacion.revalidate();
        pnlConsultaFechaBalanzaComprobacion.repaint();
    }

    private void mostrarMensajeInformativo(
            String mensaje) {

        pnlAdvertenciaFechaCorteInvalida
                .setVisible(false);

        lblMensajeFechaCorte.setText(
                mensaje
        );

        pnlMensajeFechaCorte.setVisible(
                true
        );

        pnlConsultaFechaBalanzaComprobacion.revalidate();
        pnlConsultaFechaBalanzaComprobacion.repaint();
    }

    private void limpiarMensajes() {

        pnlAdvertenciaFechaCorteInvalida
                .setVisible(false);

        pnlMensajeFechaCorte
                .setVisible(false);

        lblMensajeFechaCorteInvalida.setText(
                ""
        );

        lblMensajeFechaCorte.setText(
                ""
        );
    }

    // ============================================================
    // ESTADO DE RESULTADOS
    // ============================================================
    private void configurarEstadoResultado() {

        txtUtilidadOPerdida.setEditable(
                false
        );

        txtUtilidadOPerdida.setFocusable(
                false
        );

        limpiarResultado();
    }

    private void limpiarResultado() {

        resultadoActual = null;

        lblValorIngresos.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        lblValorGastos.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        lblValorResultadoEjercicio.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        lblValorTotalIngresos.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        lblValorTotalGastos.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        lblValorResultadoDelEjercicioUtilidadOPerdida
                .setText(
                        formatearMoneda(
                                BigDecimal.ZERO
                        )
                );

        txtUtilidadOPerdida.setText(
                ""
        );

        lblResultadoDelEjercicioUtilidadOPerdida
                .setText(
                        "RESULTADO DEL EJERCICIO"
                );

        aplicarColorResultado(
                COLOR_TEXTO,
                COLOR_TEXTO
        );
    }

    private void mostrarResultado(
            EstadoResultadosDTO resultado) {

        resultadoActual = resultado;

        if (resultado == null) {

            limpiarResultado();

            return;
        }

        BigDecimal ingresos
                = valorMonetario(
                        resultado.getTotalIngresos()
                );

        BigDecimal gastos
                = valorMonetario(
                        resultado.getTotalGastos()
                );

        BigDecimal utilidad
                = valorMonetario(
                        resultado.getUtilidadNeta()
                );

        lblValorIngresos.setText(
                formatearMoneda(
                        ingresos
                )
        );

        lblValorGastos.setText(
                formatearMoneda(
                        gastos
                )
        );

        lblValorResultadoEjercicio.setText(
                formatearResultado(
                        utilidad
                )
        );

        lblValorTotalIngresos.setText(
                formatearMoneda(
                        ingresos
                )
        );

        lblValorTotalGastos.setText(
                formatearMoneda(
                        gastos
                )
        );

        lblValorResultadoDelEjercicioUtilidadOPerdida
                .setText(
                        formatearResultado(
                                utilidad
                        )
                );

        actualizarEstadoUtilidadPerdida(
                resultado
        );
    }

    // ============================================================
    // UTILIDAD / PÉRDIDA
    // ============================================================
    private void actualizarEstadoUtilidadPerdida(
            EstadoResultadosDTO resultado) {

        if (resultado.tieneUtilidad()) {

            Color color = COLOR_UTILIDAD;

            txtUtilidadOPerdida.setText(
                    "• Utilidad"
            );

            lblResultadoDelEjercicioUtilidadOPerdida
                    .setText(
                            "RESULTADO DEL EJERCICIO — UTILIDAD"
                    );

            aplicarColorResultado(
                    color,
                    color
            );

            return;
        }

        Color color = COLOR_PERDIDA;

        txtUtilidadOPerdida.setText(
                "• Pérdida"
        );

        lblResultadoDelEjercicioUtilidadOPerdida
                .setText(
                        "RESULTADO DEL EJERCICIO — PÉRDIDA"
                );

        aplicarColorResultado(
                color,
                color
        );
    }

    private void aplicarColorResultado(
            Color colorIndicador,
            Color colorResultado) {

        txtUtilidadOPerdida.setForeground(
                colorIndicador
        );

        lblResultadoDelEjercicioUtilidadOPerdida
                .setForeground(
                        colorIndicador
                );

        lblValorResultadoDelEjercicioUtilidadOPerdida
                .setForeground(
                        colorResultado
                );

        lblValorResultadoEjercicio.setForeground(
                colorResultado
        );
    }

    // ============================================================
    // CONSULTA
    // ============================================================
    private void consultar() {

        limpiarMensajes();

        if (modoFechaCorte()) {

            consultarPorFechaCorte();

        } else {

            consultarPorPeriodo();
        }
    }

    private void consultarPorFechaCorte() {

        LocalDate fechaCorte
                = obtenerFechaCorte();

        if (fechaCorte == null) {

            limpiarResultado();

            mostrarAdvertencia(
                    "Seleccione la fecha de corte."
            );

            jdcFechaCorte.requestFocusInWindow();

            return;
        }

        try {

            EstadoResultadosDTO resultado
                    = contabilidadService
                            .obtenerEstadoResultados(
                                    fechaCorte
                            );

            mostrarResultado(
                    resultado
            );

            mostrarMensajeInformativo(
                    "Movimientos activos acumulados hasta: "
                    + fechaCorte.format(
                            FORMATO_FECHA
                    )
            );

        } catch (RuntimeException ex) {

            limpiarResultado();

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al consultar el Estado de Resultados",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void consultarPorPeriodo() {

        LocalDate[] periodo
                = obtenerPeriodoSeleccionado();

        if (periodo == null) {

            limpiarResultado();

            mostrarAdvertencia(
                    "Seleccione el año y el mes del período."
            );

            return;
        }

        LocalDate desde
                = periodo[0];

        LocalDate hasta
                = periodo[1];

        try {

            EstadoResultadosDTO resultado
                    = contabilidadService
                            .obtenerEstadoResultados(
                                    desde,
                                    hasta
                            );

            mostrarResultado(
                    resultado
            );

            mostrarMensajeInformativo(
                    "Movimientos activos del período: "
                    + desde.format(
                            FORMATO_FECHA
                    )
                    + " al "
                    + hasta.format(
                            FORMATO_FECHA
                    )
            );

        } catch (RuntimeException ex) {

            limpiarResultado();

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al consultar el Estado de Resultados",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // LIMPIAR
    // ============================================================
    private void limpiar() {

        limpiarMensajes();

        limpiarResultado();

        if (modoFechaCorte()) {

            if (jdcFechaCorte != null) {

                jdcFechaCorte.setDate(
                        null
                );
            }

        } else {

            cmbAnniosPeriodo.setSelectedItem(
                    String.valueOf(
                            LocalDate.now().getYear()
                    )
            );

            cmbMesesPeriodo.setSelectedItem(
                    obtenerNombreMes(
                            LocalDate.now().getMonth()
                    )
            );
        }

        actualizarFechaHora();
    }

    // ============================================================
    // ESTADO INICIAL
    // ============================================================
    private void configurarEstadoInicial() {

        /*
         * El .form fue construido inicialmente con el modo
         * período como referencia visual.
         */
        gbtnPeriodo.setSelected(true);

        mostrarModoPeriodo();

        limpiarMensajes();

        limpiarResultado();
    }

    // ============================================================
    // FORMATEO
    // ============================================================
    private String formatearMoneda(
            BigDecimal valor) {

        return "S/ "
                + FORMATO_MONEDA.format(
                        valorMonetario(valor)
                );
    }

    private String formatearResultado(
            BigDecimal valor) {

        BigDecimal importe
                = valorMonetario(valor);

        if (importe.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            return "- S/ "
                    + FORMATO_MONEDA.format(
                            importe.abs()
                    );
        }

        return "S/ "
                + FORMATO_MONEDA.format(
                        importe
                );
    }

    private BigDecimal valorMonetario(
            BigDecimal valor) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    // ============================================================
    // TEXTO
    // ============================================================
    private String valorTexto(
            String valor) {

        if (valor == null
                || valor.isBlank()) {

            return "Usuario actual";
        }

        return valor;
    }

    // ============================================================
    // ERRORES
    // ============================================================
    private String obtenerMensajeError(
            RuntimeException ex) {

        if (ex == null) {

            return "Ocurrió un error inesperado.";
        }

        String mensaje
                = ex.getMessage();

        if (mensaje == null
                || mensaje.isBlank()) {

            return "Ocurrió un error inesperado al consultar la información.";
        }

        return mensaje;
    }

    // ============================================================
    // EVENTOS GENERADOS POR NETBEANS
    // ============================================================
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpBotonesModo = new javax.swing.ButtonGroup();
        pnlSuperior = new javax.swing.JPanel();
        lblEstadoDeResultados = new javax.swing.JLabel();
        lblSistemaGestionComercialContable = new javax.swing.JLabel();
        lblNombreApellidoUsuario = new javax.swing.JLabel();
        lblFechaActual = new javax.swing.JLabel();
        lblHoraActual = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        pnlConsultaFechaBalanzaComprobacion = new javax.swing.JPanel();
        btnConsultar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        pnlAdvertenciaFechaCorteInvalida = new javax.swing.JPanel();
        lblMensajeFechaCorteInvalida = new javax.swing.JLabel();
        pnlMensajeFechaCorte = new javax.swing.JPanel();
        lblMensajeFechaCorte = new javax.swing.JLabel();
        pnlModoConsulta = new javax.swing.JPanel();
        lblAnnio = new javax.swing.JLabel();
        cmbAnniosPeriodo = new javax.swing.JComboBox<>();
        lblMes = new javax.swing.JLabel();
        cmbMesesPeriodo = new javax.swing.JComboBox<>();
        lblModo = new javax.swing.JLabel();
        gbtnFechaCorte = new javax.swing.JRadioButton();
        gbtnPeriodo = new javax.swing.JRadioButton();
        pnlEstadoDeResultados = new javax.swing.JPanel();
        pnlDetalleEstadoDeResultados = new javax.swing.JPanel();
        lblTituloEstadoResultados = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        lblIngresos = new javax.swing.JLabel();
        lblValorIngresos = new javax.swing.JLabel();
        lblGastos = new javax.swing.JLabel();
        lblValorGastos = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        lblResultadoDelEjercicio = new javax.swing.JLabel();
        lblValorResultadoEjercicio = new javax.swing.JLabel();
        txtUtilidadOPerdida = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        lblTotalIngresos = new javax.swing.JLabel();
        lblValorTotalIngresos = new javax.swing.JLabel();
        lblTotalGastos = new javax.swing.JLabel();
        lblValorTotalGastos = new javax.swing.JLabel();
        lblResultadoDelEjercicioUtilidadOPerdida = new javax.swing.JLabel();
        lblValorResultadoDelEjercicioUtilidadOPerdida = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblEstadoDeResultados.setFont(new java.awt.Font("Segoe UI Historic", 1, 24)); // NOI18N
        lblEstadoDeResultados.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblEstadoDeResultados.setText("ESTADO DE RESULTADOS");
        lblEstadoDeResultados.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        lblSistemaGestionComercialContable.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblSistemaGestionComercialContable.setText("Sistema de Gestión Comercial y Contable");

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
                    .addComponent(lblSistemaGestionComercialContable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblEstadoDeResultados, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 148, Short.MAX_VALUE)
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
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblEstadoDeResultados, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSistemaGestionComercialContable, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblUsuario, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblNombreApellidoUsuario))
                        .addGap(18, 18, 18)
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblHoraActual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblFechaActual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlConsultaFechaBalanzaComprobacion.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. CONSULTA DEL ESTADO DE RESULTADOS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

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

        pnlAdvertenciaFechaCorteInvalida.setBackground(new java.awt.Color(102, 0, 0));

        lblMensajeFechaCorteInvalida.setFont(new java.awt.Font("Consolas", 1, 14)); // NOI18N
        lblMensajeFechaCorteInvalida.setForeground(new java.awt.Color(255, 0, 0));
        lblMensajeFechaCorteInvalida.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblMensajeFechaCorteInvalida.setText("• Seleccione la fecha de corte. ");

        javax.swing.GroupLayout pnlAdvertenciaFechaCorteInvalidaLayout = new javax.swing.GroupLayout(pnlAdvertenciaFechaCorteInvalida);
        pnlAdvertenciaFechaCorteInvalida.setLayout(pnlAdvertenciaFechaCorteInvalidaLayout);
        pnlAdvertenciaFechaCorteInvalidaLayout.setHorizontalGroup(
            pnlAdvertenciaFechaCorteInvalidaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblMensajeFechaCorteInvalida, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
        );
        pnlAdvertenciaFechaCorteInvalidaLayout.setVerticalGroup(
            pnlAdvertenciaFechaCorteInvalidaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdvertenciaFechaCorteInvalidaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMensajeFechaCorteInvalida, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlMensajeFechaCorte.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblMensajeFechaCorte.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblMensajeFechaCorte.setText("Movimientos activos acumulados hasta: 25/08/2026");

        javax.swing.GroupLayout pnlMensajeFechaCorteLayout = new javax.swing.GroupLayout(pnlMensajeFechaCorte);
        pnlMensajeFechaCorte.setLayout(pnlMensajeFechaCorteLayout);
        pnlMensajeFechaCorteLayout.setHorizontalGroup(
            pnlMensajeFechaCorteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMensajeFechaCorteLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblMensajeFechaCorte)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlMensajeFechaCorteLayout.setVerticalGroup(
            pnlMensajeFechaCorteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMensajeFechaCorteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMensajeFechaCorte)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlModoConsulta.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "POR PERIODO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblAnnio.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblAnnio.setText("AÑO:");

        lblMes.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblMes.setText("MES:");

        javax.swing.GroupLayout pnlModoConsultaLayout = new javax.swing.GroupLayout(pnlModoConsulta);
        pnlModoConsulta.setLayout(pnlModoConsultaLayout);
        pnlModoConsultaLayout.setHorizontalGroup(
            pnlModoConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlModoConsultaLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(pnlModoConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbAnniosPeriodo, 0, 102, Short.MAX_VALUE)
                    .addComponent(lblAnnio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(pnlModoConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbMesesPeriodo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(21, 21, 21))
        );
        pnlModoConsultaLayout.setVerticalGroup(
            pnlModoConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlModoConsultaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlModoConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAnnio, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMes, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlModoConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbAnniosPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbMesesPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        lblModo.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblModo.setText("MODO:");

        grpBotonesModo.add(gbtnFechaCorte);
        gbtnFechaCorte.setText("Fecha de corte");

        grpBotonesModo.add(gbtnPeriodo);
        gbtnPeriodo.setText("Periodo");

        javax.swing.GroupLayout pnlConsultaFechaBalanzaComprobacionLayout = new javax.swing.GroupLayout(pnlConsultaFechaBalanzaComprobacion);
        pnlConsultaFechaBalanzaComprobacion.setLayout(pnlConsultaFechaBalanzaComprobacionLayout);
        pnlConsultaFechaBalanzaComprobacionLayout.setHorizontalGroup(
            pnlConsultaFechaBalanzaComprobacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlConsultaFechaBalanzaComprobacionLayout.createSequentialGroup()
                .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pnlAdvertenciaFechaCorteInvalida, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlMensajeFechaCorte, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createSequentialGroup()
                                .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblModo, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(gbtnPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(gbtnFechaCorte))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(pnlModoConsulta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnConsultar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnLimpiar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
                .addGap(28, 28, 28))
        );
        pnlConsultaFechaBalanzaComprobacionLayout.setVerticalGroup(
            pnlConsultaFechaBalanzaComprobacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createSequentialGroup()
                .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(btnConsultar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnLimpiar))
                    .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblModo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gbtnPeriodo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gbtnFechaCorte))
                    .addComponent(pnlModoConsulta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(pnlAdvertenciaFechaCorteInvalida, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlMensajeFechaCorte, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pnlEstadoDeResultados.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. ESTAOD DE RESULTADOS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        pnlDetalleEstadoDeResultados.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblTituloEstadoResultados.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblTituloEstadoResultados.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTituloEstadoResultados.setText("ESTADO DE RESULTADOS");

        lblIngresos.setFont(new java.awt.Font("CourierThai", 1, 12)); // NOI18N
        lblIngresos.setText("INGRESOS");

        lblValorIngresos.setFont(new java.awt.Font("CourierThai", 1, 12)); // NOI18N
        lblValorIngresos.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblValorIngresos.setText("S/ 50,000.00");

        lblGastos.setFont(new java.awt.Font("CourierThai", 1, 12)); // NOI18N
        lblGastos.setText("(-)GASTOS");

        lblValorGastos.setFont(new java.awt.Font("CourierThai", 1, 12)); // NOI18N
        lblValorGastos.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblValorGastos.setText("S/ 32,500.00");

        lblResultadoDelEjercicio.setFont(new java.awt.Font("CourierThai", 1, 12)); // NOI18N
        lblResultadoDelEjercicio.setText("RESULTADO DEL EJERCICIO ");

        lblValorResultadoEjercicio.setFont(new java.awt.Font("CourierThai", 1, 18)); // NOI18N
        lblValorResultadoEjercicio.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblValorResultadoEjercicio.setText("S/ 17,500.00");
        lblValorResultadoEjercicio.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        txtUtilidadOPerdida.setFont(new java.awt.Font("CourierThai", 1, 12)); // NOI18N
        txtUtilidadOPerdida.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtUtilidadOPerdida.setText("• Utilidad");

        javax.swing.GroupLayout pnlDetalleEstadoDeResultadosLayout = new javax.swing.GroupLayout(pnlDetalleEstadoDeResultados);
        pnlDetalleEstadoDeResultados.setLayout(pnlDetalleEstadoDeResultadosLayout);
        pnlDetalleEstadoDeResultadosLayout.setHorizontalGroup(
            pnlDetalleEstadoDeResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTituloEstadoResultados, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(pnlDetalleEstadoDeResultadosLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlDetalleEstadoDeResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblGastos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblIngresos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(pnlDetalleEstadoDeResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDetalleEstadoDeResultadosLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(lblValorIngresos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlDetalleEstadoDeResultadosLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblValorGastos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(12, 12, 12))
            .addGroup(pnlDetalleEstadoDeResultadosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator1)
                .addContainerGap())
            .addGroup(pnlDetalleEstadoDeResultadosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator2)
                .addContainerGap())
            .addGroup(pnlDetalleEstadoDeResultadosLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlDetalleEstadoDeResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtUtilidadOPerdida, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblResultadoDelEjercicio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorResultadoEjercicio, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );
        pnlDetalleEstadoDeResultadosLayout.setVerticalGroup(
            pnlDetalleEstadoDeResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetalleEstadoDeResultadosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloEstadoResultados)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetalleEstadoDeResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIngresos)
                    .addComponent(lblValorIngresos))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetalleEstadoDeResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGastos)
                    .addComponent(lblValorGastos))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetalleEstadoDeResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlDetalleEstadoDeResultadosLayout.createSequentialGroup()
                        .addComponent(lblResultadoDelEjercicio, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtUtilidadOPerdida, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblValorResultadoEjercicio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlEstadoDeResultadosLayout = new javax.swing.GroupLayout(pnlEstadoDeResultados);
        pnlEstadoDeResultados.setLayout(pnlEstadoDeResultadosLayout);
        pnlEstadoDeResultadosLayout.setHorizontalGroup(
            pnlEstadoDeResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEstadoDeResultadosLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlDetalleEstadoDeResultados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(120, 120, 120))
        );
        pnlEstadoDeResultadosLayout.setVerticalGroup(
            pnlEstadoDeResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEstadoDeResultadosLayout.createSequentialGroup()
                .addComponent(pnlDetalleEstadoDeResultados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 8, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "03. RESUMEN", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblTotalIngresos.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalIngresos.setText("TOTAL INGRESOS");

        lblValorTotalIngresos.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalIngresos.setText("S/ 50,000.00");

        lblTotalGastos.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalGastos.setText("TOTAL GASTOS");

        lblValorTotalGastos.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalGastos.setText("S/ 32,500.00");

        lblResultadoDelEjercicioUtilidadOPerdida.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        lblResultadoDelEjercicioUtilidadOPerdida.setForeground(new java.awt.Color(102, 153, 0));
        lblResultadoDelEjercicioUtilidadOPerdida.setText("RESULTADO DEL EJERCICIO — UTILIDAD ");

        lblValorResultadoDelEjercicioUtilidadOPerdida.setFont(new java.awt.Font("Consolas", 1, 18)); // NOI18N
        lblValorResultadoDelEjercicioUtilidadOPerdida.setForeground(new java.awt.Color(102, 153, 0));
        lblValorResultadoDelEjercicioUtilidadOPerdida.setText("S/ 32,800.00 ");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(44, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblValorTotalIngresos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTotalIngresos, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblValorTotalGastos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTotalGastos, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblResultadoDelEjercicioUtilidadOPerdida, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorResultadoDelEjercicioUtilidadOPerdida, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(22, 22, 22))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(lblResultadoDelEjercicioUtilidadOPerdida, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblValorResultadoDelEjercicioUtilidadOPerdida))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTotalIngresos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTotalGastos, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblValorTotalGastos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblValorTotalIngresos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(pnlSuperior, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlEstadoDeResultados, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlConsultaFechaBalanzaComprobacion, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlConsultaFechaBalanzaComprobacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlEstadoDeResultados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        // TODO add your handling code here:
        consultar();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
        limpiar();
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
            java.util.logging.Logger.getLogger(FrmEstadoDeResultados.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmEstadoDeResultados.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmEstadoDeResultados.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmEstadoDeResultados.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmEstadoDeResultados dialog = new FrmEstadoDeResultados(new javax.swing.JFrame(), true);
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
    private javax.swing.JComboBox<String> cmbAnniosPeriodo;
    private javax.swing.JComboBox<String> cmbMesesPeriodo;
    private javax.swing.JRadioButton gbtnFechaCorte;
    private javax.swing.JRadioButton gbtnPeriodo;
    private javax.swing.ButtonGroup grpBotonesModo;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblAnnio;
    private javax.swing.JLabel lblEstadoDeResultados;
    private javax.swing.JLabel lblFechaActual;
    private javax.swing.JLabel lblGastos;
    private javax.swing.JLabel lblHoraActual;
    private javax.swing.JLabel lblIngresos;
    private javax.swing.JLabel lblMensajeFechaCorte;
    private javax.swing.JLabel lblMensajeFechaCorteInvalida;
    private javax.swing.JLabel lblMes;
    private javax.swing.JLabel lblModo;
    private javax.swing.JLabel lblNombreApellidoUsuario;
    private javax.swing.JLabel lblResultadoDelEjercicio;
    private javax.swing.JLabel lblResultadoDelEjercicioUtilidadOPerdida;
    private javax.swing.JLabel lblSistemaGestionComercialContable;
    private javax.swing.JLabel lblTituloEstadoResultados;
    private javax.swing.JLabel lblTotalGastos;
    private javax.swing.JLabel lblTotalIngresos;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblValorGastos;
    private javax.swing.JLabel lblValorIngresos;
    private javax.swing.JLabel lblValorResultadoDelEjercicioUtilidadOPerdida;
    private javax.swing.JLabel lblValorResultadoEjercicio;
    private javax.swing.JLabel lblValorTotalGastos;
    private javax.swing.JLabel lblValorTotalIngresos;
    private javax.swing.JPanel pnlAdvertenciaFechaCorteInvalida;
    private javax.swing.JPanel pnlConsultaFechaBalanzaComprobacion;
    private javax.swing.JPanel pnlDetalleEstadoDeResultados;
    private javax.swing.JPanel pnlEstadoDeResultados;
    private javax.swing.JPanel pnlMensajeFechaCorte;
    private javax.swing.JPanel pnlModoConsulta;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JTextField txtUtilidadOPerdida;
    // End of variables declaration//GEN-END:variables
}
