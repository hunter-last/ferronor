package com.ferronor.sic.contabilidad.vista;

import com.ferronor.sic.contabilidad.logica.ContabilidadService;
import com.ferronor.sic.contabilidad.modelo.dto.BalanceComprobacionItem;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;
import com.ferronor.sic.util.ExportadorCSV;
import com.ferronor.sic.util.ExportadorPDF;
import com.ferronor.sic.util.TablaExportUtil;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * FrmBalanceComprobacion
 *
 * Reporte contable de Balance de Comprobación.
 *
 * Arquitectura:
 *
 * FrmBalanceComprobacion ↓ ServiceFactory.contabilidadService() ↓
 * ContabilidadService ↓ obtenerBalanceComprobacion(LocalDate hasta)
 *
 * La vista NO accede directamente a: - DAO - SQL - PostgreSQL -
 * BalanceComprobacionService - PlanCuentaService
 */
public class FrmBalanzaComprobacion extends javax.swing.JDialog {

    // ============================================================
    // SERVICIO
    // ============================================================
    private final ContabilidadService contabilidadService
            = ServiceFactory.contabilidadService();

    // ============================================================
    // ESTADO DE LA VISTA
    // ============================================================
    private List<BalanceComprobacionItem> balanceConsultado
            = List.of();

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
    // COLORES / ESTILO
    // ============================================================
    private static final Color COLOR_TEXTO
            = new Color(43, 47, 46);

    private static final Color COLOR_TEXTO_SECUNDARIO
            = new Color(107, 113, 110);

    private static final Color COLOR_TEXTO_SUAVE
            = new Color(154, 160, 157);

    private static final Color COLOR_BORDE
            = new Color(216, 220, 218);

    private static final Color COLOR_FONDO_CABECERA_TABLA
            = new Color(245, 246, 245);

    private static final Color COLOR_PETROLEO
            = new Color(53, 84, 92);

    private static final Color COLOR_ERROR
            = new Color(171, 58, 52);

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    public FrmBalanzaComprobacion(
            java.awt.Frame parent,
            boolean modal) {

        super(parent, modal);

        initComponents();

        configurarFormulario();
        rbtnPDF.setSelected(true);
    }

    // ============================================================
    // CONFIGURACIÓN GENERAL
    // ============================================================
    private void configurarFormulario() {

        configurarCabecera();

        configurarFechaCorte();

        configurarAdvertenciaFecha();

        configurarMensajeFechaCorte();

        configurarTabla();

        configurarResumenInicial();

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
    // FECHA DE CORTE
    // ============================================================
    private void configurarFechaCorte() {

        jdcFechaCorte.setDateFormatString(
                "dd/MM/yyyy"
        );

        jdcFechaCorte.setFocusable(true);
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
    // ADVERTENCIA DE FECHA
    // ============================================================
    private void configurarAdvertenciaFecha() {

        pnlAdvertenciaFechaCorteInvalida.setVisible(false);

        lblMensajeFechaCorteInvalida.setText(
                "• Seleccione la fecha de corte."
        );
    }

    private void mostrarAdvertenciaFecha(
            boolean mostrar) {

        pnlAdvertenciaFechaCorteInvalida.setVisible(
                mostrar
        );

        if (mostrar) {
            pnlMensajeFechaCorte.setVisible(false);
        }

        pnlConsultaFechaBalanzaComprobacion.revalidate();
        pnlConsultaFechaBalanzaComprobacion.repaint();
    }

    private void mostrarMensajeAdvertencia(
            String mensaje) {

        lblMensajeFechaCorteInvalida.setText(
                "• " + mensaje
        );

        pnlMensajeFechaCorte.setVisible(false);

        pnlAdvertenciaFechaCorteInvalida.setVisible(
                true
        );

        pnlConsultaFechaBalanzaComprobacion.revalidate();
        pnlConsultaFechaBalanzaComprobacion.repaint();
    }

    // ============================================================
    // MENSAJE DE FECHA DE CORTE
    // ============================================================
    private void configurarMensajeFechaCorte() {

        pnlMensajeFechaCorte.setVisible(
                false
        );

        lblMensajeFechaCorte.setText(
                ""
        );
    }

    private void actualizarMensajeFechaCorte(
            LocalDate fechaCorte) {

        if (fechaCorte == null) {

            pnlMensajeFechaCorte.setVisible(false);

            lblMensajeFechaCorte.setText("");

            return;
        }

        lblMensajeFechaCorte.setText(
                "Movimientos activos acumulados hasta: "
                + fechaCorte.format(FORMATO_FECHA)
        );

        pnlAdvertenciaFechaCorteInvalida.setVisible(false);
        pnlMensajeFechaCorte.setVisible(true);

        pnlConsultaFechaBalanzaComprobacion.revalidate();
        pnlConsultaFechaBalanzaComprobacion.repaint();
    }

    // ============================================================
    // TABLA
    // ============================================================
    private void configurarTabla() {

        tblBalanzaComprobacion.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblBalanzaComprobacion.setRowSelectionAllowed(
                true
        );

        tblBalanzaComprobacion.setColumnSelectionAllowed(
                false
        );

        tblBalanzaComprobacion.setCellSelectionEnabled(
                false
        );

        tblBalanzaComprobacion.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        tblBalanzaComprobacion.getTableHeader()
                .setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                10
                        )
                );

        tblBalanzaComprobacion.getTableHeader()
                .setReorderingAllowed(
                        false
                );

        tblBalanzaComprobacion.setRowHeight(
                24
        );

        tblBalanzaComprobacion.setShowVerticalLines(
                false
        );

        tblBalanzaComprobacion.setShowHorizontalLines(
                false
        );

        tblBalanzaComprobacion.setIntercellSpacing(
                new java.awt.Dimension(
                        0,
                        0
                )
        );

        tblBalanzaComprobacion.setGridColor(
                COLOR_BORDE
        );

        tblBalanzaComprobacion.getTableHeader()
                .setBackground(
                        COLOR_FONDO_CABECERA_TABLA
                );

        tblBalanzaComprobacion.getTableHeader()
                .setForeground(
                        COLOR_TEXTO_SECUNDARIO
                );

        configurarColumnas();

        configurarRenderers();
    }

    private void configurarColumnas() {

        if (tblBalanzaComprobacion.getColumnModel()
                .getColumnCount() < 6) {

            return;
        }

        tblBalanzaComprobacion.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(100);

        tblBalanzaComprobacion.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(300);

        tblBalanzaComprobacion.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(145);

        tblBalanzaComprobacion.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(145);

        tblBalanzaComprobacion.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(165);

        tblBalanzaComprobacion.getColumnModel()
                .getColumn(5)
                .setPreferredWidth(165);
    }

    private void configurarRenderers() {

        // --------------------------------------------------------
        // CÓDIGO
        // --------------------------------------------------------
        DefaultTableCellRenderer rendererCodigo
                = new DefaultTableCellRenderer();

        rendererCodigo.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        rendererCodigo.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        11
                )
        );

        rendererCodigo.setForeground(
                COLOR_FONDO_CABECERA_TABLA
        );

        // --------------------------------------------------------
        // CUENTA
        // --------------------------------------------------------
        DefaultTableCellRenderer rendererCuenta
                = new DefaultTableCellRenderer();

        rendererCuenta.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        rendererCuenta.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        rendererCuenta.setForeground(
                COLOR_FONDO_CABECERA_TABLA
        );

        // --------------------------------------------------------
        // IMPORTES
        // --------------------------------------------------------
        DefaultTableCellRenderer rendererNumero
                = new DefaultTableCellRenderer();

        rendererNumero.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        rendererNumero.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        11
                )
        );

        // --------------------------------------------------------
        // SALDOS
        // --------------------------------------------------------
        DefaultTableCellRenderer rendererSaldo
                = new DefaultTableCellRenderer();

        rendererSaldo.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        rendererSaldo.setFont(
                new Font(
                        "Consolas",
                        Font.BOLD,
                        11
                )
        );

        rendererSaldo.setForeground(
                COLOR_FONDO_CABECERA_TABLA
        );

        // --------------------------------------------------------
        // ASIGNAR RENDERERS
        // --------------------------------------------------------
        if (tblBalanzaComprobacion.getColumnModel()
                .getColumnCount() >= 6) {

            tblBalanzaComprobacion.getColumnModel()
                    .getColumn(0)
                    .setCellRenderer(
                            rendererCodigo
                    );

            tblBalanzaComprobacion.getColumnModel()
                    .getColumn(1)
                    .setCellRenderer(
                            rendererCuenta
                    );

            tblBalanzaComprobacion.getColumnModel()
                    .getColumn(2)
                    .setCellRenderer(
                            rendererNumero
                    );

            tblBalanzaComprobacion.getColumnModel()
                    .getColumn(3)
                    .setCellRenderer(
                            rendererNumero
                    );

            tblBalanzaComprobacion.getColumnModel()
                    .getColumn(4)
                    .setCellRenderer(
                            rendererSaldo
                    );

            tblBalanzaComprobacion.getColumnModel()
                    .getColumn(5)
                    .setCellRenderer(
                            rendererSaldo
                    );
        }
    }

    // ============================================================
    // ESTADO INICIAL DEL RESUMEN
    // ============================================================
    private void configurarResumenInicial() {

        lblMovimientos.setText(
                "CUENTAS CON MOVIMIENTO"
        );

        lblCantMovimientos.setText(
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

        lblValorSaldoDeudor.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        lblValorSaldoAcreedor.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );
    }

    // ============================================================
    // ESTADO INICIAL
    // ============================================================
    private void configurarEstadoInicial() {

        balanceConsultado = List.of();

        limpiarTabla();

        limpiarResumen();

        mostrarAdvertenciaFecha(false);

        actualizarMensajeFechaCorte(null);
    }

    // ============================================================
    // CONSULTA PRINCIPAL
    // ============================================================
    private void consultarBalance() {

        LocalDate fechaCorte
                = obtenerFechaCorte();

        // --------------------------------------------------------
        // VALIDAR FECHA
        // --------------------------------------------------------
        if (fechaCorte == null) {

            mostrarMensajeAdvertencia(
                    "Seleccione la fecha de corte."
            );

            limpiarResultados();

            jdcFechaCorte.requestFocusInWindow();

            return;
        }

        // --------------------------------------------------------
        // FECHA CORRECTA
        // --------------------------------------------------------
        mostrarAdvertenciaFecha(
                false
        );

        actualizarMensajeFechaCorte(
                fechaCorte
        );

        try {

            List<BalanceComprobacionItem> resultados
                    = contabilidadService
                            .obtenerBalanceComprobacion(
                                    fechaCorte
                            );

            balanceConsultado
                    = resultados == null
                            ? List.of()
                            : resultados;

            cargarTabla(
                    balanceConsultado
            );

            actualizarResumen(
                    balanceConsultado
            );

            actualizarFechaHoraCabecera();

        } catch (RuntimeException ex) {

            balanceConsultado
                    = List.of();

            limpiarTabla();

            limpiarResumen();

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al consultar la Balanza de Comprobación",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // CARGAR TABLA
    // ============================================================
    private void cargarTabla(
            List<BalanceComprobacionItem> lista) {

        DefaultTableModel modelo
                = new DefaultTableModel(
                        new Object[][]{},
                        new String[]{
                            "CODIGO",
                            "CUENTA",
                            "DEBE",
                            "HABER",
                            "SALDO DEUDOR",
                            "SALDO ACREEDOR"
                        }
                ) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };

        if (lista != null) {

            for (BalanceComprobacionItem item
                    : lista) {

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
                                    item.getTotalDebe()
                            ),
                            formatearMoneda(
                                    item.getTotalHaber()
                            ),
                            formatearSaldo(
                                    item.getSaldoDeudor()
                            ),
                            formatearSaldo(
                                    item.getSaldoAcreedor()
                            )
                        }
                );
            }
        }

        tblBalanzaComprobacion.setModel(
                modelo
        );

        configurarColumnas();

        configurarRenderers();
    }

    // ============================================================
    // ACTUALIZAR RESUMEN
    // ============================================================
    private void actualizarResumen(
            List<BalanceComprobacionItem> lista) {

        int cantidadCuentas = 0;

        BigDecimal totalDebe
                = BigDecimal.ZERO;

        BigDecimal totalHaber
                = BigDecimal.ZERO;

        BigDecimal totalSaldoDeudor
                = BigDecimal.ZERO;

        BigDecimal totalSaldoAcreedor
                = BigDecimal.ZERO;

        if (lista != null) {

            for (BalanceComprobacionItem item
                    : lista) {

                if (item == null) {
                    continue;
                }

                cantidadCuentas++;

                totalDebe
                        = totalDebe.add(
                                valorMonetario(
                                        item.getTotalDebe()
                                )
                        );

                totalHaber
                        = totalHaber.add(
                                valorMonetario(
                                        item.getTotalHaber()
                                )
                        );

                totalSaldoDeudor
                        = totalSaldoDeudor.add(
                                valorMonetario(
                                        item.getSaldoDeudor()
                                )
                        );

                totalSaldoAcreedor
                        = totalSaldoAcreedor.add(
                                valorMonetario(
                                        item.getSaldoAcreedor()
                                )
                        );
            }
        }

        lblMovimientos.setText(
                "CUENTAS CON MOVIMIENTO"
        );

        lblCantMovimientos.setText(
                String.valueOf(
                        cantidadCuentas
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

        lblValorSaldoDeudor.setText(
                formatearMoneda(
                        totalSaldoDeudor
                )
        );

        lblValorSaldoAcreedor.setText(
                formatearMoneda(
                        totalSaldoAcreedor
                )
        );
    }

    // ============================================================
    // LIMPIAR FILTROS
    // ============================================================
    private void limpiarFiltros() {

        jdcFechaCorte.setDate(
                null
        );

        mostrarAdvertenciaFecha(
                false
        );

        actualizarMensajeFechaCorte(
                null
        );

        balanceConsultado
                = List.of();

        limpiarResultados();

        actualizarFechaHoraCabecera();

        jdcFechaCorte.requestFocusInWindow();
    }

    // ============================================================
    // LIMPIAR RESULTADOS
    // ============================================================
    private void limpiarResultados() {

        limpiarTabla();

        limpiarResumen();
    }

    // ============================================================
    // LIMPIAR TABLA
    // ============================================================
    private void limpiarTabla() {

        DefaultTableModel modelo
                = new DefaultTableModel(
                        new Object[][]{},
                        new String[]{
                            "CODIGO",
                            "CUENTA",
                            "DEBE",
                            "HABER",
                            "SALDO DEUDOR",
                            "SALDO ACREEDOR"
                        }
                ) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };

        tblBalanzaComprobacion.setModel(
                modelo
        );

        configurarColumnas();

        configurarRenderers();

        tblBalanzaComprobacion.clearSelection();
    }

    // ============================================================
    // LIMPIAR RESUMEN
    // ============================================================
    private void limpiarResumen() {

        lblMovimientos.setText(
                "CUENTAS CON MOVIMIENTO"
        );

        lblCantMovimientos.setText(
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

        lblValorSaldoDeudor.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        lblValorSaldoAcreedor.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );
    }

    // ============================================================
    // FORMATEAR MONEDA
    // ============================================================
    private String formatearMoneda(
            BigDecimal valor) {

        BigDecimal importe
                = valorMonetario(valor);

        return "S/ "
                + FORMATO_MONEDA.format(
                        importe
                );
    }

    private String formatearSaldo(
            BigDecimal valor) {

        BigDecimal importe
                = valorMonetario(valor);

        if (importe.compareTo(
                BigDecimal.ZERO
        ) == 0) {

            return "";
        }

        return formatearMoneda(
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

            return "-";
        }

        return valor;
    }

    // ============================================================
    // MENSAJE DE ERROR
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

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        pnlSuperior = new javax.swing.JPanel();
        lblBalanzaComprobacion = new javax.swing.JLabel();
        lblSistemaGestionComercialYContable = new javax.swing.JLabel();
        lblNombreApellidoUsuario = new javax.swing.JLabel();
        lblFechaActual = new javax.swing.JLabel();
        lblHoraActual = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        pnlConsultaFechaBalanzaComprobacion = new javax.swing.JPanel();
        lblFechaDeCorte = new javax.swing.JLabel();
        jdcFechaCorte = new com.toedter.calendar.JDateChooser();
        btnConsultar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        pnlAdvertenciaFechaCorteInvalida = new javax.swing.JPanel();
        lblMensajeFechaCorteInvalida = new javax.swing.JLabel();
        pnlMensajeFechaCorte = new javax.swing.JPanel();
        lblMensajeFechaCorte = new javax.swing.JLabel();
        pnlExportarReporte = new javax.swing.JPanel();
        rbtnCSV = new javax.swing.JRadioButton();
        rbtnPDF = new javax.swing.JRadioButton();
        btnExportarReporte = new javax.swing.JButton();
        pnlBalanzaComprobacion = new javax.swing.JScrollPane();
        tblBalanzaComprobacion = new javax.swing.JTable();
        pnlResumen = new javax.swing.JPanel();
        lblMovimientos = new javax.swing.JLabel();
        lblCantMovimientos = new javax.swing.JLabel();
        lblTotalDebe = new javax.swing.JLabel();
        lblValorTotalDebe = new javax.swing.JLabel();
        lblTotalHaber = new javax.swing.JLabel();
        lblValorTotalHaber = new javax.swing.JLabel();
        lblSaldoDeudor = new javax.swing.JLabel();
        lblValorSaldoDeudor = new javax.swing.JLabel();
        lblSaldoAcreedor = new javax.swing.JLabel();
        lblValorSaldoAcreedor = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblBalanzaComprobacion.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblBalanzaComprobacion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblBalanzaComprobacion.setText("BALANZA DE COMPROBACIÓN");
        lblBalanzaComprobacion.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        lblSistemaGestionComercialYContable.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblSistemaGestionComercialYContable.setText("Sistema de Gestión Comercial y Contable ");

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
                    .addComponent(lblSistemaGestionComercialYContable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblBalanzaComprobacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblNombreApellidoUsuario))
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblFechaActual)
                        .addGap(18, 18, 18)
                        .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20))
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
                            .addComponent(lblSistemaGestionComercialYContable, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFechaActual)
                            .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblBalanzaComprobacion, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlConsultaFechaBalanzaComprobacion.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(41, 43, 45)), "01. CONSULTA DEL BALANCE DE COMPROBACIÓN ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblFechaDeCorte.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFechaDeCorte.setText("FECHA DE CORTE");

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
            .addComponent(lblMensajeFechaCorteInvalida, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 707, Short.MAX_VALUE)
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

        pnlExportarReporte.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(41, 43, 45)), "Exportar:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 14))); // NOI18N
        pnlExportarReporte.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N

        buttonGroup1.add(rbtnCSV);
        rbtnCSV.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        rbtnCSV.setText("CSV");

        buttonGroup1.add(rbtnPDF);
        rbtnPDF.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        rbtnPDF.setText("PDF");
        rbtnPDF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbtnPDFActionPerformed(evt);
            }
        });

        btnExportarReporte.setBackground(new java.awt.Color(255, 153, 51));
        btnExportarReporte.setForeground(new java.awt.Color(255, 255, 255));
        btnExportarReporte.setText("Generar Reporte");
        btnExportarReporte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportarReporteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlExportarReporteLayout = new javax.swing.GroupLayout(pnlExportarReporte);
        pnlExportarReporte.setLayout(pnlExportarReporteLayout);
        pnlExportarReporteLayout.setHorizontalGroup(
            pnlExportarReporteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExportarReporteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlExportarReporteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnExportarReporte, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlExportarReporteLayout.createSequentialGroup()
                        .addGroup(pnlExportarReporteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rbtnPDF, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rbtnCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 45, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlExportarReporteLayout.setVerticalGroup(
            pnlExportarReporteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExportarReporteLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(rbtnCSV)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbtnPDF)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnExportarReporte)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlConsultaFechaBalanzaComprobacionLayout = new javax.swing.GroupLayout(pnlConsultaFechaBalanzaComprobacion);
        pnlConsultaFechaBalanzaComprobacion.setLayout(pnlConsultaFechaBalanzaComprobacionLayout);
        pnlConsultaFechaBalanzaComprobacionLayout.setHorizontalGroup(
            pnlConsultaFechaBalanzaComprobacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlAdvertenciaFechaCorteInvalida, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createSequentialGroup()
                        .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFechaDeCorte, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createSequentialGroup()
                                .addComponent(jdcFechaCorte, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnLimpiar)))
                        .addGap(18, 18, 18)
                        .addComponent(btnConsultar))
                    .addComponent(pnlMensajeFechaCorte, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlExportarReporte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pnlConsultaFechaBalanzaComprobacionLayout.setVerticalGroup(
            pnlConsultaFechaBalanzaComprobacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createSequentialGroup()
                        .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createSequentialGroup()
                                .addComponent(lblFechaDeCorte)
                                .addGap(7, 7, 7)
                                .addComponent(jdcFechaCorte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlConsultaFechaBalanzaComprobacionLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(pnlConsultaFechaBalanzaComprobacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(btnConsultar)
                                    .addComponent(btnLimpiar))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pnlMensajeFechaCorte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(56, 56, 56)
                        .addComponent(pnlAdvertenciaFechaCorteInvalida, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(pnlExportarReporte, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pnlBalanzaComprobacion.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(41, 43, 45)), "02. BALANZA DE COMPROBACIÓN", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        tblBalanzaComprobacion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "CODIGO", "CUENTA", "DEBE", "HABER", "SALDO DEUDOR", "SALDO ACREEDOR"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        pnlBalanzaComprobacion.setViewportView(tblBalanzaComprobacion);

        pnlResumen.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(41, 43, 45)), "03. RESUMEN", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblMovimientos.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblMovimientos.setText("MOVIMIENTOS");

        lblCantMovimientos.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblCantMovimientos.setText("4");

        lblTotalDebe.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalDebe.setText("TOTAL DEBE");

        lblValorTotalDebe.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalDebe.setText("S/ 44,800.00 ");

        lblTotalHaber.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalHaber.setText("TOTAL HABER");

        lblValorTotalHaber.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalHaber.setText("S/ 32,300.00 ");

        lblSaldoDeudor.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblSaldoDeudor.setForeground(new java.awt.Color(204, 102, 0));
        lblSaldoDeudor.setText("SALDO DEUDOR");

        lblValorSaldoDeudor.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorSaldoDeudor.setForeground(new java.awt.Color(204, 102, 0));
        lblValorSaldoDeudor.setText("S/ 32,800.00 ");

        lblSaldoAcreedor.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblSaldoAcreedor.setForeground(new java.awt.Color(204, 102, 0));
        lblSaldoAcreedor.setText("SALDO ACREEDOR");

        lblValorSaldoAcreedor.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorSaldoAcreedor.setForeground(new java.awt.Color(204, 102, 0));
        lblValorSaldoAcreedor.setText("S/ 20,300.00 ");

        javax.swing.GroupLayout pnlResumenLayout = new javax.swing.GroupLayout(pnlResumen);
        pnlResumen.setLayout(pnlResumenLayout);
        pnlResumenLayout.setHorizontalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblCantMovimientos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblMovimientos))
                .addGap(30, 30, 30)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblValorTotalDebe, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                    .addComponent(lblTotalDebe, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblValorTotalHaber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTotalHaber, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblValorSaldoDeudor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblSaldoDeudor, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblValorSaldoAcreedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblSaldoAcreedor, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(89, Short.MAX_VALUE))
        );
        pnlResumenLayout.setVerticalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlResumenLayout.createSequentialGroup()
                        .addComponent(lblSaldoAcreedor, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblValorSaldoAcreedor))
                    .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(pnlResumenLayout.createSequentialGroup()
                            .addComponent(lblSaldoDeudor, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(lblValorSaldoDeudor))
                        .addGroup(pnlResumenLayout.createSequentialGroup()
                            .addComponent(lblTotalHaber, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(lblValorTotalHaber))
                        .addGroup(pnlResumenLayout.createSequentialGroup()
                            .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblMovimientos)
                                .addComponent(lblTotalDebe, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblValorTotalDebe)
                                .addComponent(lblCantMovimientos)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlSuperior, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlConsultaFechaBalanzaComprobacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlBalanzaComprobacion)
                    .addComponent(pnlResumen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlConsultaFechaBalanzaComprobacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlBalanzaComprobacion, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResumen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        // TODO add your handling code here:
        consultarBalance();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
        limpiarFiltros();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void rbtnPDFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtnPDFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rbtnPDFActionPerformed

    private void btnExportarReporteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportarReporteActionPerformed
        // TODO add your handling code here:
        if (balanceConsultado == null || balanceConsultado.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Realiza una consulta con movimientos antes de exportar.",
                    "Exportar Balanza de Comprobación",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        LocalDate fechaCorte = obtenerFechaCorte();

        List<String> encabezados = TablaExportUtil.obtenerEncabezados(tblBalanzaComprobacion);
        List<List<String>> filas = TablaExportUtil.obtenerFilas(tblBalanzaComprobacion);

        String nombreBase = "balanza_comprobacion"
                + (fechaCorte != null ? "_" + fechaCorte : "");

        JFileChooser selector = new JFileChooser();
        boolean exportarComoCsv = rbtnCSV.isSelected();

        selector.setSelectedFile(new File(nombreBase + (exportarComoCsv ? ".csv" : ".pdf")));

        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String ruta = selector.getSelectedFile().getAbsolutePath();
        String titulo = "BALANZA DE COMPROBACIÓN"
                + (fechaCorte != null ? " — al " + fechaCorte : "");

        try {
            if (exportarComoCsv) {
                ExportadorCSV.exportar(ruta, encabezados, filas);
            } else {
                ExportadorPDF.exportarTabla(ruta, titulo, encabezados, filas);
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Reporte exportado correctamente.",
                    "Exportar Balanza de Comprobación",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Falló la exportación: " + ex.getMessage(),
                    "Exportar Balanza de Comprobación",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_btnExportarReporteActionPerformed

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
            java.util.logging.Logger.getLogger(FrmBalanzaComprobacion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmBalanzaComprobacion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmBalanzaComprobacion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmBalanzaComprobacion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmBalanzaComprobacion dialog = new FrmBalanzaComprobacion(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnExportarReporte;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.ButtonGroup buttonGroup1;
    private com.toedter.calendar.JDateChooser jdcFechaCorte;
    private javax.swing.JLabel lblBalanzaComprobacion;
    private javax.swing.JLabel lblCantMovimientos;
    private javax.swing.JLabel lblFechaActual;
    private javax.swing.JLabel lblFechaDeCorte;
    private javax.swing.JLabel lblHoraActual;
    private javax.swing.JLabel lblMensajeFechaCorte;
    private javax.swing.JLabel lblMensajeFechaCorteInvalida;
    private javax.swing.JLabel lblMovimientos;
    private javax.swing.JLabel lblNombreApellidoUsuario;
    private javax.swing.JLabel lblSaldoAcreedor;
    private javax.swing.JLabel lblSaldoDeudor;
    private javax.swing.JLabel lblSistemaGestionComercialYContable;
    private javax.swing.JLabel lblTotalDebe;
    private javax.swing.JLabel lblTotalHaber;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblValorSaldoAcreedor;
    private javax.swing.JLabel lblValorSaldoDeudor;
    private javax.swing.JLabel lblValorTotalDebe;
    private javax.swing.JLabel lblValorTotalHaber;
    private javax.swing.JPanel pnlAdvertenciaFechaCorteInvalida;
    private javax.swing.JScrollPane pnlBalanzaComprobacion;
    private javax.swing.JPanel pnlConsultaFechaBalanzaComprobacion;
    private javax.swing.JPanel pnlExportarReporte;
    private javax.swing.JPanel pnlMensajeFechaCorte;
    private javax.swing.JPanel pnlResumen;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JRadioButton rbtnCSV;
    private javax.swing.JRadioButton rbtnPDF;
    private javax.swing.JTable tblBalanzaComprobacion;
    // End of variables declaration//GEN-END:variables
}
