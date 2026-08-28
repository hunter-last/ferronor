package com.ferronor.sic.contabilidad.vista;

import com.ferronor.sic.contabilidad.logica.ContabilidadService;
import com.ferronor.sic.contabilidad.modelo.dto.BalanceGeneralDTO;
import com.ferronor.sic.contabilidad.modelo.dto.BalanceGeneralItem;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;

import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class FrmBalanceGeneral extends javax.swing.JDialog {

    // ============================================================
    // SERVICIO
    // ============================================================
    private final ContabilidadService contabilidadService
            = ServiceFactory.contabilidadService();

    // ============================================================
    // ESTADO
    // ============================================================
    private BalanceGeneralDTO balanceActual;

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
                = DecimalFormatSymbols.getInstance(Locale.US);

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
    private static final Color COLOR_TEXTO
            = new Color(43, 47, 46);

    private static final Color COLOR_TEXTO_SECUNDARIO
            = new Color(107, 113, 110);

    private static final Color COLOR_ACTIVO
            = new Color(53, 84, 92);

    private static final Color COLOR_PASIVO
            = new Color(181, 80, 46);

    private static final Color COLOR_PATRIMONIO
            = new Color(181, 80, 46);

    private static final Color COLOR_VERDE
            = new Color(63, 125, 82);

    private static final Color COLOR_ROJO
            = new Color(171, 58, 52);

    private static final Color COLOR_BORDE
            = new Color(216, 220, 218);

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    public FrmBalanceGeneral(
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

        configurarFechaCorte();

        configurarMensajes();

        configurarTablas();

        configurarResumen();

        configurarEstadoInicial();
    }

    // ============================================================
    // CABECERA
    // ============================================================
    private void configurarCabecera() {

        try {

            SesionUsuario sesion
                    = SesionUsuario.actual();

            if (sesion != null) {

                lblNombreApellidoUsuario.setText(
                        sesion.getNombreCompleto()
                );
            }

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
    // FECHA DE CORTE
    // ============================================================
    private void configurarFechaCorte() {

        jdcFechaCorte.setDateFormatString(
                "dd/MM/yyyy"
        );

        jdcFechaCorte.setDate(
                null
        );
    }

    private LocalDate obtenerFechaCorte() {

        if (jdcFechaCorte.getDate() == null) {

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
    // MENSAJES
    // ============================================================
    private void configurarMensajes() {

        pnlMensajeFechaCorte.setVisible(
                false
        );

        pnlAdvertenciaFechaCorteInvalida.setVisible(
                false
        );

        lblMensajeFechaCorte.setText(
                ""
        );

        lblMensajeFechaCorteInvalida.setText(
                "• Seleccione la fecha de corte."
        );
    }

    private void mostrarMensajeInformativo(
            LocalDate fechaCorte) {

        if (fechaCorte == null) {

            pnlMensajeFechaCorte.setVisible(
                    false
            );

            lblMensajeFechaCorte.setText(
                    ""
            );

            return;
        }

        pnlAdvertenciaFechaCorteInvalida.setVisible(
                false
        );

        lblMensajeFechaCorte.setText(
                "Movimientos activos acumulados hasta: "
                + fechaCorte.format(
                        FORMATO_FECHA
                )
        );

        pnlMensajeFechaCorte.setVisible(
                true
        );

        pnlConsultaBalanceGeneral.revalidate();
        pnlConsultaBalanceGeneral.repaint();
    }

    private void mostrarAdvertencia(
            String mensaje) {

        pnlMensajeFechaCorte.setVisible(
                false
        );

        lblMensajeFechaCorte.setText(
                ""
        );

        lblMensajeFechaCorteInvalida.setText(
                "• " + mensaje
        );

        pnlAdvertenciaFechaCorteInvalida.setVisible(
                true
        );

        pnlConsultaBalanceGeneral.revalidate();
        pnlConsultaBalanceGeneral.repaint();
    }

    private void limpiarMensajes() {

        pnlMensajeFechaCorte.setVisible(
                false
        );

        pnlAdvertenciaFechaCorteInvalida.setVisible(
                false
        );

        lblMensajeFechaCorte.setText(
                ""
        );

        lblMensajeFechaCorteInvalida.setText(
                ""
        );
    }

    // ============================================================
    // TABLAS
    // ============================================================
    private void configurarTablas() {

        configurarTabla(tblActivo);
        configurarTabla(tblPasivo);
        configurarTabla(tblPatrimonio);

        configurarColumnas(tblActivo);
        configurarColumnas(tblPasivo);
        configurarColumnas(tblPatrimonio);
    }

    private void configurarTabla(
            JTable tabla) {

        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tabla.setRowSelectionAllowed(
                true
        );

        tabla.setColumnSelectionAllowed(
                false
        );

        tabla.setCellSelectionEnabled(
                false
        );

        tabla.getTableHeader()
                .setReorderingAllowed(
                        false
                );

        tabla.setRowHeight(
                24
        );

        tabla.setShowVerticalLines(
                false
        );

        tabla.setShowHorizontalLines(
                false
        );

        tabla.setIntercellSpacing(
                new java.awt.Dimension(
                        0,
                        0
                )
        );

        tabla.setGridColor(
                COLOR_BORDE
        );

        tabla.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        tabla.getTableHeader()
                .setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                10
                        )
                );
    }

    private void configurarColumnas(
            JTable tabla) {

        if (tabla.getColumnModel()
                .getColumnCount() < 3) {

            return;
        }

        tabla.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(
                        90
                );

        tabla.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(
                        430
                );

        tabla.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(
                        150
                );

        tabla.getColumnModel()
                .getColumn(0)
                .setCellRenderer(
                        crearRendererCodigo()
                );

        tabla.getColumnModel()
                .getColumn(1)
                .setCellRenderer(
                        crearRendererCuenta()
                );

        tabla.getColumnModel()
                .getColumn(2)
                .setCellRenderer(
                        crearRendererSaldo()
                );
    }

    private DefaultTableCellRenderer crearRendererCodigo() {

        DefaultTableCellRenderer renderer
                = new DefaultTableCellRenderer();

        renderer.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        renderer.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        11
                )
        );

        renderer.setForeground(
                COLOR_TEXTO_SECUNDARIO
        );

        return renderer;
    }

    private DefaultTableCellRenderer crearRendererCuenta() {

        DefaultTableCellRenderer renderer
                = new DefaultTableCellRenderer();

        renderer.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        renderer.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        renderer.setForeground(
                COLOR_TEXTO
        );

        return renderer;
    }

    private DefaultTableCellRenderer crearRendererSaldo() {

        DefaultTableCellRenderer renderer
                = new DefaultTableCellRenderer();

        renderer.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        renderer.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        11
                )
        );

        renderer.setForeground(
                COLOR_TEXTO
        );

        return renderer;
    }

    // ============================================================
    // MODELO DE TABLA
    // ============================================================
    private DefaultTableModel crearModeloTabla() {

        return new DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "CODIGO",
                    "CUENTA",
                    "SALDO"
                }
        ) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };
    }

    private void cargarTabla(
            JTable tabla,
            List<BalanceGeneralItem> items) {

        DefaultTableModel modelo
                = crearModeloTabla();

        if (items != null) {

            for (BalanceGeneralItem item
                    : items) {

                if (item == null) {
                    continue;
                }

                modelo.addRow(
                        new Object[]{
                            valorTexto(
                                    item.getCodigo()
                            ),
                            valorTexto(
                                    item.getNombreCuenta()
                            ),
                            formatearMoneda(
                                    item.getSaldo()
                            )
                        }
                );
            }
        }

        tabla.setModel(
                modelo
        );

        configurarTabla(
                tabla
        );

        configurarColumnas(
                tabla
        );

        tabla.clearSelection();
    }

    // ============================================================
    // CANTIDAD DE CUENTAS
    // ============================================================
    private void actualizarCantidadCuentas(
            BalanceGeneralDTO balance) {

        int cantidadActivo
                = obtenerCantidad(
                        balance.getActivo()
                );

        int cantidadPasivo
                = obtenerCantidad(
                        balance.getPasivo()
                );

        int cantidadPatrimonio
                = obtenerCantidad(
                        balance.getPatrimonio()
                );

        lblNroCuentasActivo.setText(
                cantidadActivo
                + (cantidadActivo == 1
                        ? " cuenta"
                        : " cuentas")
        );

        lblNroCuentasPasivo.setText(
                cantidadPasivo
                + (cantidadPasivo == 1
                        ? " cuenta"
                        : " cuentas")
        );

        lblNroCuentasPatrimonio.setText(
                cantidadPatrimonio
                + (cantidadPatrimonio == 1
                        ? " cuenta"
                        : " cuentas")
        );
    }

    private int obtenerCantidad(
            List<BalanceGeneralItem> items) {

        return items == null
                ? 0
                : items.size();
    }

    // ============================================================
    // RESUMEN
    // ============================================================
    private void configurarResumen() {

        lblValorTotalActivo.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        lblValorTotalPasivo.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        lblValorTotalPatrimonio.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        lblValorPasivoMasPatrimonio.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        txtEstadoBalance.setEditable(
                false
        );

        txtEstadoBalance.setFocusable(
                false
        );

        txtEstadoBalance.setText(
                ""
        );
    }

    private void actualizarResumen(
            BalanceGeneralDTO balance) {

        BigDecimal totalActivo
                = valorMonetario(
                        balance.getTotalActivo()
                );

        BigDecimal totalPasivo
                = valorMonetario(
                        balance.getTotalPasivo()
                );

        BigDecimal totalPatrimonio
                = valorMonetario(
                        balance.getTotalPatrimonio()
                );

        BigDecimal pasivoMasPatrimonio
                = totalPasivo.add(
                        totalPatrimonio
                );

        lblValorTotalActivo.setText(
                formatearMoneda(
                        totalActivo
                )
        );

        lblValorTotalPasivo.setText(
                formatearMoneda(
                        totalPasivo
                )
        );

        lblValorTotalPatrimonio.setText(
                formatearMoneda(
                        totalPatrimonio
                )
        );

        lblValorPasivoMasPatrimonio.setText(
                formatearMoneda(
                        pasivoMasPatrimonio
                )
        );

        txtEstadoBalance.setText(
                "Balance Cuadrado"
        );

        txtEstadoBalance.setForeground(
                COLOR_VERDE
        );

        txtEstadoBalance.setFont(
                new Font(
                        "Consolas",
                        Font.BOLD,
                        12
                )
        );
    }

    private void mostrarEstadoBalanceNoCuadrado(
            String mensaje) {

        txtEstadoBalance.setText(
                "Balance No Cuadrado"
        );

        txtEstadoBalance.setForeground(
                COLOR_ROJO
        );

        txtEstadoBalance.setFont(
                new Font(
                        "Consolas",
                        Font.BOLD,
                        12
                )
        );

        if (mensaje != null
                && !mensaje.isBlank()) {

            JOptionPane.showMessageDialog(
                    this,
                    mensaje,
                    "Balance General",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // CONSULTA PRINCIPAL
    // ============================================================
    private void consultarBalanceGeneral() {

        limpiarMensajes();

        LocalDate fechaCorte
                = obtenerFechaCorte();

        if (fechaCorte == null) {

            limpiarResultados();

            mostrarAdvertencia(
                    "Seleccione la fecha de corte."
            );

            jdcFechaCorte.requestFocusInWindow();

            return;
        }

        try {

            BalanceGeneralDTO balance
                    = contabilidadService
                            .obtenerBalanceGeneral(
                                    fechaCorte
                            );

            if (balance == null) {

                limpiarResultados();

                mostrarAdvertencia(
                        "No se obtuvo información del Balance General."
                );

                return;
            }

            balanceActual = balance;

            cargarTabla(
                    tblActivo,
                    balance.getActivo()
            );

            cargarTabla(
                    tblPasivo,
                    balance.getPasivo()
            );

            cargarTabla(
                    tblPatrimonio,
                    balance.getPatrimonio()
            );

            actualizarCantidadCuentas(
                    balance
            );

            actualizarResumen(
                    balance
            );

            mostrarMensajeInformativo(
                    balance.getFechaCorte()
            );

            actualizarFechaHora();

        } catch (RuntimeException ex) {

            balanceActual = null;

            limpiarResultados();

            if (esErrorCuadratura(ex)) {

                mostrarEstadoBalanceNoCuadrado(
                        obtenerMensajeError(ex)
                );

                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al consultar el Balance General",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private boolean esErrorCuadratura(
            RuntimeException ex) {

        if (ex == null
                || ex.getMessage() == null) {

            return false;
        }

        return ex.getMessage()
                .toLowerCase(Locale.ROOT)
                .contains(
                        "balance general no cuadra"
                );
    }

    // ============================================================
    // LIMPIAR
    // ============================================================
    private void limpiar() {

        jdcFechaCorte.setDate(
                null
        );

        balanceActual = null;

        limpiarMensajes();

        limpiarResultados();

        actualizarFechaHora();

        jdcFechaCorte.requestFocusInWindow();
    }

    private void limpiarResultados() {

        limpiarTabla(
                tblActivo
        );

        limpiarTabla(
                tblPasivo
        );

        limpiarTabla(
                tblPatrimonio
        );

        lblNroCuentasActivo.setText(
                "0 cuentas"
        );

        lblNroCuentasPasivo.setText(
                "0 cuentas"
        );

        lblNroCuentasPatrimonio.setText(
                "0 cuentas"
        );

        lblValorTotalActivo.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        lblValorTotalPasivo.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        lblValorTotalPatrimonio.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        lblValorPasivoMasPatrimonio.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        txtEstadoBalance.setText(
                ""
        );

        txtEstadoBalance.setForeground(
                COLOR_TEXTO
        );
    }

    private void limpiarTabla(
            JTable tabla) {

        tabla.setModel(
                crearModeloTabla()
        );

        configurarTabla(
                tabla
        );

        configurarColumnas(
                tabla
        );

        tabla.clearSelection();
    }

    // ============================================================
    // ESTADO INICIAL
    // ============================================================
    private void configurarEstadoInicial() {

        balanceActual = null;

        limpiarResultados();

        limpiarMensajes();

        jdcFechaCorte.setDate(
                null
        );
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

    private BigDecimal valorMonetario(
            BigDecimal valor) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private String valorTexto(
            String valor) {

        if (valor == null
                || valor.isBlank()) {

            return "-";
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

            return "Ocurrió un error inesperado al consultar el Balance General.";
        }

        return mensaje;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSuperior = new javax.swing.JPanel();
        lblBalanceGeneral = new javax.swing.JLabel();
        lblSistemaGestionComercialYContable = new javax.swing.JLabel();
        lblNombreApellidoUsuario = new javax.swing.JLabel();
        lblFechaActual = new javax.swing.JLabel();
        lblHoraActual = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        lblHora = new javax.swing.JLabel();
        pnlConsultaBalanceGeneral = new javax.swing.JPanel();
        lblFechaDeCorte = new javax.swing.JLabel();
        jdcFechaCorte = new com.toedter.calendar.JDateChooser();
        btnLimpiar = new javax.swing.JButton();
        btnConsultar = new javax.swing.JButton();
        pnlMensajeFechaCorte = new javax.swing.JPanel();
        lblMensajeFechaCorte = new javax.swing.JLabel();
        pnlAdvertenciaFechaCorteInvalida = new javax.swing.JPanel();
        lblMensajeFechaCorteInvalida = new javax.swing.JLabel();
        spnlBalanceGeneral = new javax.swing.JScrollPane();
        pnlAcomodador = new javax.swing.JPanel();
        pnlActivos = new javax.swing.JPanel();
        spnlTblActivo = new javax.swing.JScrollPane();
        tblActivo = new javax.swing.JTable();
        jSeparator1 = new javax.swing.JSeparator();
        lblActivo = new javax.swing.JLabel();
        lblNroCuentasActivo = new javax.swing.JLabel();
        pnlPasivos = new javax.swing.JPanel();
        spnlTblPasivo = new javax.swing.JScrollPane();
        tblPasivo = new javax.swing.JTable();
        jSeparator2 = new javax.swing.JSeparator();
        lblPasivo = new javax.swing.JLabel();
        lblNroCuentasPasivo = new javax.swing.JLabel();
        pnlPatrimonio = new javax.swing.JPanel();
        spnlTblPatrimonio = new javax.swing.JScrollPane();
        tblPatrimonio = new javax.swing.JTable();
        jSeparator3 = new javax.swing.JSeparator();
        lblPatrimonio = new javax.swing.JLabel();
        lblNroCuentasPatrimonio = new javax.swing.JLabel();
        pnlResumen = new javax.swing.JPanel();
        lblTotalActivo = new javax.swing.JLabel();
        lblValorTotalActivo = new javax.swing.JLabel();
        lblTotalPasivo = new javax.swing.JLabel();
        lblValorTotalPasivo = new javax.swing.JLabel();
        lblTotalPatrimonio = new javax.swing.JLabel();
        lblValorTotalPatrimonio = new javax.swing.JLabel();
        lblTotalPasivoMasPatrimonio = new javax.swing.JLabel();
        lblValorPasivoMasPatrimonio = new javax.swing.JLabel();
        txtEstadoBalance = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblBalanceGeneral.setFont(new java.awt.Font("Segoe UI Historic", 1, 24)); // NOI18N
        lblBalanceGeneral.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblBalanceGeneral.setText("BALANCE GENERAL");
        lblBalanceGeneral.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        lblSistemaGestionComercialYContable.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblSistemaGestionComercialYContable.setText("Sistema de Gestión Comercial y Contable ");

        lblNombreApellidoUsuario.setText("Nombre Apellido");

        lblFechaActual.setText("21/08/2026");

        lblHoraActual.setText("10:40");

        lblUsuario.setText("Usuario:");

        lblFecha.setText("Fecha:");

        lblHora.setText("Hora:");

        javax.swing.GroupLayout pnlSuperiorLayout = new javax.swing.GroupLayout(pnlSuperior);
        pnlSuperior.setLayout(pnlSuperiorLayout);
        pnlSuperiorLayout.setHorizontalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSuperiorLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblSistemaGestionComercialYContable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblBalanceGeneral, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(355, 355, 355)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblHora, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblFecha, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblUsuario, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblNombreApellidoUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblFechaActual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblHoraActual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        pnlSuperiorLayout.setVerticalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSuperiorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNombreApellidoUsuario)
                            .addComponent(lblUsuario))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblFechaActual)
                            .addComponent(lblFecha)))
                    .addComponent(lblBalanceGeneral, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblHora)
                        .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblSistemaGestionComercialYContable, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlConsultaBalanceGeneral.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. CONSULTA DEL BALANCE GENERAL", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblFechaDeCorte.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFechaDeCorte.setText("FECHA DE CORTE");

        btnLimpiar.setBackground(new java.awt.Color(51, 51, 51));
        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        btnConsultar.setBackground(new java.awt.Color(153, 51, 0));
        btnConsultar.setText("Consultar");
        btnConsultar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConsultarActionPerformed(evt);
            }
        });

        pnlMensajeFechaCorte.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblMensajeFechaCorte.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblMensajeFechaCorte.setText("Movimientos activos acumulados hasta: 25/08/2026");

        javax.swing.GroupLayout pnlMensajeFechaCorteLayout = new javax.swing.GroupLayout(pnlMensajeFechaCorte);
        pnlMensajeFechaCorte.setLayout(pnlMensajeFechaCorteLayout);
        pnlMensajeFechaCorteLayout.setHorizontalGroup(
            pnlMensajeFechaCorteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMensajeFechaCorteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMensajeFechaCorte)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlMensajeFechaCorteLayout.setVerticalGroup(
            pnlMensajeFechaCorteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMensajeFechaCorteLayout.createSequentialGroup()
                .addContainerGap(7, Short.MAX_VALUE)
                .addComponent(lblMensajeFechaCorte)
                .addContainerGap())
        );

        pnlAdvertenciaFechaCorteInvalida.setBackground(new java.awt.Color(102, 0, 0));

        lblMensajeFechaCorteInvalida.setFont(new java.awt.Font("Consolas", 1, 14)); // NOI18N
        lblMensajeFechaCorteInvalida.setForeground(new java.awt.Color(255, 0, 0));
        lblMensajeFechaCorteInvalida.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblMensajeFechaCorteInvalida.setText("• Seleccione la fecha de corte. ");

        javax.swing.GroupLayout pnlAdvertenciaFechaCorteInvalidaLayout = new javax.swing.GroupLayout(pnlAdvertenciaFechaCorteInvalida);
        pnlAdvertenciaFechaCorteInvalida.setLayout(pnlAdvertenciaFechaCorteInvalidaLayout);
        pnlAdvertenciaFechaCorteInvalidaLayout.setHorizontalGroup(
            pnlAdvertenciaFechaCorteInvalidaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdvertenciaFechaCorteInvalidaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMensajeFechaCorteInvalida, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlAdvertenciaFechaCorteInvalidaLayout.setVerticalGroup(
            pnlAdvertenciaFechaCorteInvalidaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblMensajeFechaCorteInvalida, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pnlConsultaBalanceGeneralLayout = new javax.swing.GroupLayout(pnlConsultaBalanceGeneral);
        pnlConsultaBalanceGeneral.setLayout(pnlConsultaBalanceGeneralLayout);
        pnlConsultaBalanceGeneralLayout.setHorizontalGroup(
            pnlConsultaBalanceGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsultaBalanceGeneralLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(pnlConsultaBalanceGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlConsultaBalanceGeneralLayout.createSequentialGroup()
                        .addComponent(lblFechaDeCorte, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlConsultaBalanceGeneralLayout.createSequentialGroup()
                        .addGroup(pnlConsultaBalanceGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(pnlAdvertenciaFechaCorteInvalida, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlMensajeFechaCorte, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlConsultaBalanceGeneralLayout.createSequentialGroup()
                                .addComponent(jdcFechaCorte, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnLimpiar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnConsultar)))
                        .addGap(20, 20, 20))))
        );
        pnlConsultaBalanceGeneralLayout.setVerticalGroup(
            pnlConsultaBalanceGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsultaBalanceGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlConsultaBalanceGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlConsultaBalanceGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnConsultar)
                        .addComponent(btnLimpiar))
                    .addGroup(pnlConsultaBalanceGeneralLayout.createSequentialGroup()
                        .addComponent(lblFechaDeCorte)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jdcFechaCorte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlMensajeFechaCorte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlAdvertenciaFechaCorteInvalida, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        spnlBalanceGeneral.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. BALANCE GENERAL", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        pnlActivos.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        tblActivo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "CODIGO", "CUENTA", "SALDO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlTblActivo.setViewportView(tblActivo);

        lblActivo.setFont(new java.awt.Font("CourierThai", 1, 14)); // NOI18N
        lblActivo.setText("ACTIVO");

        lblNroCuentasActivo.setFont(new java.awt.Font("CourierThai", 0, 12)); // NOI18N
        lblNroCuentasActivo.setText("4 cuentas");

        javax.swing.GroupLayout pnlActivosLayout = new javax.swing.GroupLayout(pnlActivos);
        pnlActivos.setLayout(pnlActivosLayout);
        pnlActivosLayout.setHorizontalGroup(
            pnlActivosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spnlTblActivo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 809, Short.MAX_VALUE)
            .addGroup(pnlActivosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlActivosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(pnlActivosLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(lblActivo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblNroCuentasActivo)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlActivosLayout.setVerticalGroup(
            pnlActivosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlActivosLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(pnlActivosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblActivo)
                    .addGroup(pnlActivosLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(lblNroCuentasActivo, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnlTblActivo, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlPasivos.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        tblPasivo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "CODIGO", "CUENTA", "SALDO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlTblPasivo.setViewportView(tblPasivo);

        lblPasivo.setFont(new java.awt.Font("CourierThai", 1, 14)); // NOI18N
        lblPasivo.setText("PASIVO");

        lblNroCuentasPasivo.setFont(new java.awt.Font("CourierThai", 0, 12)); // NOI18N
        lblNroCuentasPasivo.setText("2 cuentas");

        javax.swing.GroupLayout pnlPasivosLayout = new javax.swing.GroupLayout(pnlPasivos);
        pnlPasivos.setLayout(pnlPasivosLayout);
        pnlPasivosLayout.setHorizontalGroup(
            pnlPasivosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spnlTblPasivo, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(pnlPasivosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlPasivosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2)
                    .addGroup(pnlPasivosLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(lblPasivo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblNroCuentasPasivo)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlPasivosLayout.setVerticalGroup(
            pnlPasivosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlPasivosLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(pnlPasivosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPasivo)
                    .addGroup(pnlPasivosLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(lblNroCuentasPasivo, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnlTblPasivo, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                .addContainerGap())
        );

        pnlPatrimonio.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        tblPatrimonio.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "CODIGO", "CUENTA", "SALDO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlTblPatrimonio.setViewportView(tblPatrimonio);

        lblPatrimonio.setFont(new java.awt.Font("CourierThai", 1, 14)); // NOI18N
        lblPatrimonio.setText("PATRIMONIO");

        lblNroCuentasPatrimonio.setFont(new java.awt.Font("CourierThai", 0, 12)); // NOI18N
        lblNroCuentasPatrimonio.setText("2 cuentas");

        javax.swing.GroupLayout pnlPatrimonioLayout = new javax.swing.GroupLayout(pnlPatrimonio);
        pnlPatrimonio.setLayout(pnlPatrimonioLayout);
        pnlPatrimonioLayout.setHorizontalGroup(
            pnlPatrimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spnlTblPatrimonio, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(pnlPatrimonioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlPatrimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator3)
                    .addGroup(pnlPatrimonioLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(lblPatrimonio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblNroCuentasPatrimonio)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlPatrimonioLayout.setVerticalGroup(
            pnlPatrimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlPatrimonioLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(pnlPatrimonioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPatrimonio)
                    .addGroup(pnlPatrimonioLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(lblNroCuentasPatrimonio, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnlTblPatrimonio, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlAcomodadorLayout = new javax.swing.GroupLayout(pnlAcomodador);
        pnlAcomodador.setLayout(pnlAcomodadorLayout);
        pnlAcomodadorLayout.setHorizontalGroup(
            pnlAcomodadorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAcomodadorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAcomodadorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlActivos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlPasivos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlPatrimonio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlAcomodadorLayout.setVerticalGroup(
            pnlAcomodadorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAcomodadorLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlActivos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlPasivos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlPatrimonio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        spnlBalanceGeneral.setViewportView(pnlAcomodador);

        pnlResumen.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "03. RESUMEN", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblTotalActivo.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalActivo.setText("TOTAL ACTIVO");

        lblValorTotalActivo.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalActivo.setForeground(new java.awt.Color(255, 255, 255));
        lblValorTotalActivo.setText("S/ 60,000.00");

        lblTotalPasivo.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalPasivo.setText("TOTAL PASIVO");

        lblValorTotalPasivo.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalPasivo.setForeground(new java.awt.Color(255, 102, 102));
        lblValorTotalPasivo.setText("S/ 15,000.00");

        lblTotalPatrimonio.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalPatrimonio.setText("TOTAL PATRIMONIO");

        lblValorTotalPatrimonio.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalPatrimonio.setForeground(new java.awt.Color(255, 153, 51));
        lblValorTotalPatrimonio.setText("S/ 45,000.00");

        lblTotalPasivoMasPatrimonio.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalPasivoMasPatrimonio.setText("TOTAL PASIVO + PATRIMONIO");

        lblValorPasivoMasPatrimonio.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorPasivoMasPatrimonio.setText("S/ 60,000.00");

        txtEstadoBalance.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtEstadoBalance.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtEstadoBalance.setText("Balance Cuadrado");

        javax.swing.GroupLayout pnlResumenLayout = new javax.swing.GroupLayout(pnlResumen);
        pnlResumen.setLayout(pnlResumenLayout);
        pnlResumenLayout.setHorizontalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblValorTotalActivo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTotalActivo, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblValorTotalPasivo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTotalPasivo, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblValorTotalPatrimonio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTotalPatrimonio, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblTotalPasivoMasPatrimonio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorPasivoMasPatrimonio, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtEstadoBalance, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlResumenLayout.setVerticalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlResumenLayout.createSequentialGroup()
                            .addComponent(lblTotalPatrimonio, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(lblValorTotalPatrimonio))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlResumenLayout.createSequentialGroup()
                            .addComponent(lblTotalPasivoMasPatrimonio, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblValorPasivoMasPatrimonio)
                                .addComponent(txtEstadoBalance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlResumenLayout.createSequentialGroup()
                            .addComponent(lblTotalPasivo, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(lblValorTotalPasivo))
                        .addGroup(pnlResumenLayout.createSequentialGroup()
                            .addComponent(lblTotalActivo, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(lblValorTotalActivo))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlSuperior, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spnlBalanceGeneral)
                            .addComponent(pnlConsultaBalanceGeneral, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlResumen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlConsultaBalanceGeneral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnlBalanceGeneral, javax.swing.GroupLayout.PREFERRED_SIZE, 324, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResumen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
        limpiar();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        // TODO add your handling code here:
        consultarBalanceGeneral();
    }//GEN-LAST:event_btnConsultarActionPerformed

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
            java.util.logging.Logger.getLogger(FrmBalanceGeneral.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmBalanceGeneral.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmBalanceGeneral.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmBalanceGeneral.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmBalanceGeneral dialog = new FrmBalanceGeneral(new javax.swing.JFrame(), true);
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
    private javax.swing.JSeparator jSeparator3;
    private com.toedter.calendar.JDateChooser jdcFechaCorte;
    private javax.swing.JLabel lblActivo;
    private javax.swing.JLabel lblBalanceGeneral;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblFechaActual;
    private javax.swing.JLabel lblFechaDeCorte;
    private javax.swing.JLabel lblHora;
    private javax.swing.JLabel lblHoraActual;
    private javax.swing.JLabel lblMensajeFechaCorte;
    private javax.swing.JLabel lblMensajeFechaCorteInvalida;
    private javax.swing.JLabel lblNombreApellidoUsuario;
    private javax.swing.JLabel lblNroCuentasActivo;
    private javax.swing.JLabel lblNroCuentasPasivo;
    private javax.swing.JLabel lblNroCuentasPatrimonio;
    private javax.swing.JLabel lblPasivo;
    private javax.swing.JLabel lblPatrimonio;
    private javax.swing.JLabel lblSistemaGestionComercialYContable;
    private javax.swing.JLabel lblTotalActivo;
    private javax.swing.JLabel lblTotalPasivo;
    private javax.swing.JLabel lblTotalPasivoMasPatrimonio;
    private javax.swing.JLabel lblTotalPatrimonio;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblValorPasivoMasPatrimonio;
    private javax.swing.JLabel lblValorTotalActivo;
    private javax.swing.JLabel lblValorTotalPasivo;
    private javax.swing.JLabel lblValorTotalPatrimonio;
    private javax.swing.JPanel pnlAcomodador;
    private javax.swing.JPanel pnlActivos;
    private javax.swing.JPanel pnlAdvertenciaFechaCorteInvalida;
    private javax.swing.JPanel pnlConsultaBalanceGeneral;
    private javax.swing.JPanel pnlMensajeFechaCorte;
    private javax.swing.JPanel pnlPasivos;
    private javax.swing.JPanel pnlPatrimonio;
    private javax.swing.JPanel pnlResumen;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JScrollPane spnlBalanceGeneral;
    private javax.swing.JScrollPane spnlTblActivo;
    private javax.swing.JScrollPane spnlTblPasivo;
    private javax.swing.JScrollPane spnlTblPatrimonio;
    private javax.swing.JTable tblActivo;
    private javax.swing.JTable tblPasivo;
    private javax.swing.JTable tblPatrimonio;
    private javax.swing.JTextField txtEstadoBalance;
    // End of variables declaration//GEN-END:variables
}
