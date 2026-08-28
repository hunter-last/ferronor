package com.ferronor.sic.contabilidad.vista;

import com.ferronor.sic.contabilidad.logica.ContabilidadService;
import com.ferronor.sic.contabilidad.modelo.dto.LibroMayorItem;
import com.ferronor.sic.maestros.logica.PlanCuentaService;
import com.ferronor.sic.maestros.modelo.PlanCuenta;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class FrmLibroMayor extends javax.swing.JDialog {

    // ============================================================
    // SERVICIOS
    // ============================================================
    private final PlanCuentaService planCuentaService
            = ServiceFactory.planCuentaService();

    private final ContabilidadService contabilidadService
            = ServiceFactory.contabilidadService();

    // ============================================================
    // ESTADO DE LA VISTA
    // ============================================================
    private List<LibroMayorItem> movimientosConsultados = List.of();

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

    private static final Color COLOR_BORDE
            = new Color(216, 220, 218);

    private static final Color COLOR_FONDO_CABECERA_TABLA
            = new Color(245, 246, 245);

    private static final Color COLOR_PETROLEO
            = new Color(53, 84, 92);

    private static final Color COLOR_EXITO
            = new Color(63, 125, 82);

    private static final Color COLOR_ERROR
            = new Color(171, 58, 52);

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    public FrmLibroMayor(
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

        configurarComboCuentas();

        configurarFechas();

        configurarAdvertenciaRango();

        configurarTablaMovimientos();

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
    // COMBO DE CUENTAS
    // ============================================================
    private void configurarComboCuentas() {

        cargarCuentas();

        cmbCuentas.setRenderer(
                new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                Component componente
                        = super.getListCellRendererComponent(
                                list,
                                value,
                                index,
                                isSelected,
                                cellHasFocus
                        );

                if (componente instanceof JLabel label) {

                    if (value instanceof PlanCuenta cuenta) {

                        String codigo
                                = valorTexto(
                                        cuenta.getCodigo()
                                );

                        String nombre
                                = valorTexto(
                                        cuenta.getNombreCuenta()
                                );

                        label.setText(
                                codigo
                                + " - "
                                + nombre
                        );

                    } else {

                        label.setText(
                                "Seleccione una cuenta..."
                        );
                    }

                    label.setFont(
                            new Font(
                                    "Segoe UI",
                                    Font.PLAIN,
                                    12
                            )
                    );
                }

                return componente;
            }
        }
        );

        cmbCuentas.setSelectedItem(null);
    }

    private void cargarCuentas() {

        try {

            List<PlanCuenta> cuentas
                    = planCuentaService.listar();

            DefaultComboBoxModel<PlanCuenta> modelo
                    = new DefaultComboBoxModel<>();

            modelo.addElement(null);

            if (cuentas != null) {

                for (PlanCuenta cuenta : cuentas) {

                    if (cuenta != null) {

                        modelo.addElement(
                                cuenta
                        );
                    }
                }
            }

            cmbCuentas.setModel(
                    modelo
            );

        } catch (RuntimeException ex) {

            cmbCuentas.setModel(
                    new DefaultComboBoxModel<>(
                            new PlanCuenta[]{
                                null
                            }
                    )
            );

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al cargar el plan de cuentas",
                    JOptionPane.ERROR_MESSAGE
            );
        }
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
    }

    // ============================================================
    // ADVERTENCIA DE RANGO
    // ============================================================
    private void configurarAdvertenciaRango() {

        pnlAdvertenciaRangoFechaInvalido.setVisible(
                false
        );
    }

    private void mostrarAdvertenciaRango(
            boolean mostrar) {

        pnlAdvertenciaRangoFechaInvalido.setVisible(
                mostrar
        );

        pnlConsultaLibroMayor.revalidate();
        pnlConsultaLibroMayor.repaint();
    }

    // ============================================================
    // TABLA
    // ============================================================
    private void configurarTablaMovimientos() {

        tblMovimientos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblMovimientos.setRowSelectionAllowed(
                true
        );

        tblMovimientos.setColumnSelectionAllowed(
                false
        );

        tblMovimientos.setCellSelectionEnabled(
                false
        );

        tblMovimientos.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        tblMovimientos.getTableHeader().setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        10
                )
        );

        tblMovimientos.getTableHeader()
                .setReorderingAllowed(
                        false
                );

        tblMovimientos.setRowHeight(
                24
        );

        tblMovimientos.setShowVerticalLines(
                false
        );

        tblMovimientos.setShowHorizontalLines(
                false
        );

        tblMovimientos.setIntercellSpacing(
                new java.awt.Dimension(
                        0,
                        0
                )
        );

        tblMovimientos.setGridColor(
                COLOR_BORDE
        );

        tblMovimientos.getTableHeader()
                .setBackground(
                        COLOR_FONDO_CABECERA_TABLA
                );

        tblMovimientos.getTableHeader()
                .setForeground(
                        COLOR_TEXTO_SECUNDARIO
                );

        configurarColumnasTabla();

        configurarRenderersTabla();
    }

    private void configurarColumnasTabla() {

        if (tblMovimientos.getColumnModel()
                .getColumnCount() < 5) {

            return;
        }

        tblMovimientos.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(
                        110
                );

        tblMovimientos.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(
                        420
                );

        tblMovimientos.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(
                        150
                );

        tblMovimientos.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(
                        150
                );

        tblMovimientos.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(
                        170
                );
    }

    private void configurarRenderersTabla() {

        DefaultTableCellRenderer rendererFecha
                = new DefaultTableCellRenderer();

        rendererFecha.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        rendererFecha.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        11
                )
        );

        rendererFecha.setForeground(
                COLOR_TEXTO_SECUNDARIO
        );

        DefaultTableCellRenderer rendererTexto
                = new DefaultTableCellRenderer();

        rendererTexto.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        rendererTexto.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

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
                COLOR_PETROLEO
        );

        tblMovimientos.getColumnModel()
                .getColumn(0)
                .setCellRenderer(
                        rendererFecha
                );

        tblMovimientos.getColumnModel()
                .getColumn(1)
                .setCellRenderer(
                        rendererTexto
                );

        tblMovimientos.getColumnModel()
                .getColumn(2)
                .setCellRenderer(
                        rendererNumero
                );

        tblMovimientos.getColumnModel()
                .getColumn(3)
                .setCellRenderer(
                        rendererNumero
                );

        tblMovimientos.getColumnModel()
                .getColumn(4)
                .setCellRenderer(
                        rendererSaldo
                );
    }

    // ============================================================
    // ESTADO INICIAL
    // ============================================================
    private void configurarEstadoInicial() {

        movimientosConsultados = List.of();

        limpiarDatosCuenta();

        limpiarTabla();

        limpiarResumen();

        mostrarAdvertenciaRango(
                false
        );
    }

    // ============================================================
    // CONSULTA PRINCIPAL
    // ============================================================
    private void consultarLibroMayor() {

        PlanCuenta cuenta
                = obtenerCuentaSeleccionada();

        if (cuenta == null) {

            mostrarAdvertenciaRango(
                    false
            );

            limpiarResultados();

            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione una cuenta contable.",
                    "Consulta del Libro Mayor",
                    JOptionPane.WARNING_MESSAGE
            );

            cmbCuentas.requestFocusInWindow();

            return;
        }

        LocalDate fechaDesde
                = obtenerFecha(
                        jdcFechaDesde
                );

        LocalDate fechaHasta
                = obtenerFecha(
                        jdcFechaHasta
                );

        if (fechaDesde == null
                || fechaHasta == null) {

            mostrarAdvertenciaRango(
                    false
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione la fecha desde y la fecha hasta.",
                    "Consulta del Libro Mayor",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (fechaDesde.isAfter(
                fechaHasta
        )) {

            mostrarAdvertenciaRango(
                    true
            );

            limpiarResultados();

            return;
        }

        mostrarAdvertenciaRango(
                false
        );

        try {

            List<LibroMayorItem> resultados
                    = contabilidadService.obtenerLibroMayor(
                            cuenta.getIdCuenta(),
                            fechaDesde,
                            fechaHasta
                    );

            movimientosConsultados
                    = resultados == null
                            ? List.of()
                            : resultados;

            mostrarCuentaSeleccionada(
                    cuenta
            );

            cargarMovimientos(
                    movimientosConsultados
            );

            actualizarResumen(
                    movimientosConsultados
            );

            actualizarFechaHora();

        } catch (RuntimeException ex) {

            movimientosConsultados
                    = List.of();

            limpiarTabla();

            limpiarResumen();

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al consultar el Libro Mayor",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // CUENTA SELECCIONADA
    // ============================================================
    private PlanCuenta obtenerCuentaSeleccionada() {

        Object seleccionado
                = cmbCuentas.getSelectedItem();

        if (!(seleccionado instanceof PlanCuenta cuenta)) {

            return null;
        }

        return cuenta;
    }

    private void mostrarCuentaSeleccionada(
            PlanCuenta cuenta) {

        if (cuenta == null) {

            limpiarDatosCuenta();

            return;
        }

        lblValorCodigoCuenta.setText(
                valorTexto(
                        cuenta.getCodigo()
                )
        );

        lblValorNombreCuenta.setText(
                valorTexto(
                        cuenta.getNombreCuenta()
                )
        );

        lblValorIdCuenta.setText(
                String.valueOf(
                        cuenta.getIdCuenta()
                )
        );

        pnlResultadosConsulta.revalidate();
        pnlResultadosConsulta.repaint();
    }

    private void limpiarDatosCuenta() {

        lblValorCodigoCuenta.setText(
                "-"
        );

        lblValorNombreCuenta.setText(
                "-"
        );

        lblValorIdCuenta.setText(
                "-"
        );
    }

    // ============================================================
    // CARGAR MOVIMIENTOS
    // ============================================================
    private void cargarMovimientos(
            List<LibroMayorItem> movimientos) {

        DefaultTableModel modelo
                = obtenerModeloTabla();

        if (movimientos == null
                || movimientos.isEmpty()) {

            tblMovimientos.setModel(
                    modelo
            );

            configurarColumnasTabla();

            configurarRenderersTabla();

            configurarEncabezadoTabla();

            return;
        }

        for (LibroMayorItem movimiento
                : movimientos) {

            if (movimiento == null) {
                continue;
            }

            modelo.addRow(
                    new Object[]{
                        formatearFechaMovimiento(
                                movimiento.getFecha()
                        ),
                        valorTexto(
                                movimiento.getGlosa()
                        ),
                        formatearImporteTabla(
                                movimiento.getDebe()
                        ),
                        formatearImporteTabla(
                                movimiento.getHaber()
                        ),
                        formatearImporteTabla(
                                movimiento.getSaldo()
                        )
                    }
            );
        }

        tblMovimientos.setModel(
                modelo
        );

        configurarColumnasTabla();

        configurarRenderersTabla();

        configurarEncabezadoTabla();

        tblMovimientos.revalidate();
        tblMovimientos.repaint();
    }

    private DefaultTableModel obtenerModeloTabla() {

        return new DefaultTableModel(
                new Object[]{
                    "FECHA",
                    "GLOSA",
                    "DEBE",
                    "HABER",
                    "SALDO"
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
    }

    private void limpiarTabla() {

        tblMovimientos.setModel(
                obtenerModeloTabla()
        );

        configurarColumnasTabla();

        configurarRenderersTabla();

        configurarEncabezadoTabla();

        tblMovimientos.revalidate();
        tblMovimientos.repaint();
    }

    private void configurarEncabezadoTabla() {

        tblMovimientos.getTableHeader()
                .setBackground(
                        COLOR_FONDO_CABECERA_TABLA
                );

        tblMovimientos.getTableHeader()
                .setForeground(
                        COLOR_TEXTO_SECUNDARIO
                );

        tblMovimientos.getTableHeader()
                .setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                10
                        )
                );
    }

    // ============================================================
    // RESUMEN
    // ============================================================
    private void actualizarResumen(
            List<LibroMayorItem> movimientos) {

        if (movimientos == null
                || movimientos.isEmpty()) {

            limpiarResumen();

            return;
        }

        int cantidad = 0;

        BigDecimal totalDebe
                = BigDecimal.ZERO;

        BigDecimal totalHaber
                = BigDecimal.ZERO;

        BigDecimal saldoFinal
                = BigDecimal.ZERO;

        for (LibroMayorItem movimiento
                : movimientos) {

            if (movimiento == null) {
                continue;
            }

            cantidad++;

            totalDebe
                    = totalDebe.add(
                            normalizarMonto(
                                    movimiento.getDebe()
                            )
                    );

            totalHaber
                    = totalHaber.add(
                            normalizarMonto(
                                    movimiento.getHaber()
                            )
                    );

            /*
             * El saldo viene calculado por el backend.
             *
             * NO hacer:
             *
             * totalDebe - totalHaber
             *
             * El saldo final oficial corresponde al último
             * LibroMayorItem.
             */
            saldoFinal
                    = normalizarMonto(
                            movimiento.getSaldo()
                    );
        }

        lblCantMovimientos.setText(
                String.valueOf(
                        cantidad
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

        lblValorSaldoFinal.setText(
                formatearMoneda(
                        saldoFinal
                )
        );
    }

    private void limpiarResumen() {

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

        lblValorSaldoFinal.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );
    }

    // ============================================================
    // LIMPIAR FORMULARIO
    // ============================================================
    private void limpiarFormulario() {

        cmbCuentas.setSelectedItem(
                null
        );

        jdcFechaDesde.setDate(
                null
        );

        jdcFechaHasta.setDate(
                null
        );

        mostrarAdvertenciaRango(
                false
        );

        movimientosConsultados
                = List.of();

        limpiarDatosCuenta();

        limpiarTabla();

        limpiarResumen();

        actualizarFechaHora();

        cmbCuentas.requestFocusInWindow();
    }

    private void limpiarResultados() {

        movimientosConsultados
                = List.of();

        limpiarDatosCuenta();

        limpiarTabla();

        limpiarResumen();
    }

    // ============================================================
    // FECHAS
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

    private String formatearFechaMovimiento(
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
    // IMPORTES
    // ============================================================
    private BigDecimal normalizarMonto(
            BigDecimal valor) {

        if (valor == null) {

            return BigDecimal.ZERO;
        }

        return valor;
    }

    private String formatearImporteTabla(
            BigDecimal valor) {

        if (valor == null
                || valor.compareTo(
                        BigDecimal.ZERO
                ) == 0) {

            return "";
        }

        return formatearMoneda(
                valor
        );
    }

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
    // ERRORES
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
        lblLibroMayor = new javax.swing.JLabel();
        lblMovimientoSaldoAcumuladoCuentaContable = new javax.swing.JLabel();
        lblNombreApellidoUsuario = new javax.swing.JLabel();
        lblFechaActual = new javax.swing.JLabel();
        lblHoraActual = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        pnlConsultaLibroMayor = new javax.swing.JPanel();
        lblFechaDesde = new javax.swing.JLabel();
        jdcFechaDesde = new com.toedter.calendar.JDateChooser();
        lblFechaHasta = new javax.swing.JLabel();
        jdcFechaHasta = new com.toedter.calendar.JDateChooser();
        btnConsultar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        pnlAdvertenciaRangoFechaInvalido = new javax.swing.JPanel();
        lblDiferenciaDeCaja = new javax.swing.JLabel();
        lblCuenta = new javax.swing.JLabel();
        cmbCuentas = new javax.swing.JComboBox<>();
        pnlResultadosConsulta = new javax.swing.JPanel();
        lblCodigoCuenta = new javax.swing.JLabel();
        lblValorCodigoCuenta = new javax.swing.JLabel();
        lblNombreCuenta = new javax.swing.JLabel();
        lblValorNombreCuenta = new javax.swing.JLabel();
        lblIdCuenta = new javax.swing.JLabel();
        lblValorIdCuenta = new javax.swing.JLabel();
        spnlMovimientos = new javax.swing.JScrollPane();
        tblMovimientos = new javax.swing.JTable();
        pnlResumen = new javax.swing.JPanel();
        lblMovimientos = new javax.swing.JLabel();
        lblCantMovimientos = new javax.swing.JLabel();
        lblTotalDebe = new javax.swing.JLabel();
        lblValorTotalDebe = new javax.swing.JLabel();
        lblTotalHaber = new javax.swing.JLabel();
        lblValorTotalHaber = new javax.swing.JLabel();
        lblSaldoFinal = new javax.swing.JLabel();
        lblValorSaldoFinal = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblLibroMayor.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblLibroMayor.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblLibroMayor.setText("LIBRO MAYOR");
        lblLibroMayor.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        lblMovimientoSaldoAcumuladoCuentaContable.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblMovimientoSaldoAcumuladoCuentaContable.setText("Movimiento y saldo acumulado de una cuenta contable");

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
                    .addComponent(lblLibroMayor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblMovimientoSaldoAcumuladoCuentaContable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 385, Short.MAX_VALUE)
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
                            .addComponent(lblMovimientoSaldoAcumuladoCuentaContable, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFechaActual)
                            .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblLibroMayor, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlConsultaLibroMayor.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. CONSULTA DEL LIBRO MAYOR", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

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

        lblCuenta.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCuenta.setText("CUENTA");

        pnlResultadosConsulta.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblCodigoCuenta.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCodigoCuenta.setText("CODIGO");

        lblValorCodigoCuenta.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorCodigoCuenta.setText("1011");

        lblNombreCuenta.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblNombreCuenta.setText("CUENTA");

        lblValorNombreCuenta.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorNombreCuenta.setText("Caja");

        lblIdCuenta.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblIdCuenta.setForeground(new java.awt.Color(153, 153, 153));
        lblIdCuenta.setText("ID CUENTA");

        lblValorIdCuenta.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorIdCuenta.setForeground(new java.awt.Color(153, 153, 153));
        lblValorIdCuenta.setText("1011");

        javax.swing.GroupLayout pnlResultadosConsultaLayout = new javax.swing.GroupLayout(pnlResultadosConsulta);
        pnlResultadosConsulta.setLayout(pnlResultadosConsultaLayout);
        pnlResultadosConsultaLayout.setHorizontalGroup(
            pnlResultadosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResultadosConsultaLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(pnlResultadosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblCodigoCuenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorCodigoCuenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(32, 32, 32)
                .addGroup(pnlResultadosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblNombreCuenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorNombreCuenta, javax.swing.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(pnlResultadosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblIdCuenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorIdCuenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(531, 531, 531))
        );
        pnlResultadosConsultaLayout.setVerticalGroup(
            pnlResultadosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResultadosConsultaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlResultadosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCodigoCuenta)
                    .addComponent(lblNombreCuenta)
                    .addComponent(lblIdCuenta))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlResultadosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblValorCodigoCuenta)
                    .addComponent(lblValorNombreCuenta)
                    .addComponent(lblValorIdCuenta))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlConsultaLibroMayorLayout = new javax.swing.GroupLayout(pnlConsultaLibroMayor);
        pnlConsultaLibroMayor.setLayout(pnlConsultaLibroMayorLayout);
        pnlConsultaLibroMayorLayout.setHorizontalGroup(
            pnlConsultaLibroMayorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsultaLibroMayorLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(pnlConsultaLibroMayorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlAdvertenciaRangoFechaInvalido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlConsultaLibroMayorLayout.createSequentialGroup()
                        .addGroup(pnlConsultaLibroMayorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlConsultaLibroMayorLayout.createSequentialGroup()
                                .addComponent(lblCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(cmbCuentas, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(pnlConsultaLibroMayorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jdcFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(pnlConsultaLibroMayorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jdcFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(142, 142, 142)
                        .addComponent(btnLimpiar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnConsultar))
                    .addComponent(pnlResultadosConsulta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(30, 30, 30))
        );
        pnlConsultaLibroMayorLayout.setVerticalGroup(
            pnlConsultaLibroMayorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsultaLibroMayorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlConsultaLibroMayorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlConsultaLibroMayorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnConsultar)
                        .addComponent(btnLimpiar))
                    .addGroup(pnlConsultaLibroMayorLayout.createSequentialGroup()
                        .addGroup(pnlConsultaLibroMayorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(pnlConsultaLibroMayorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblFechaDesde, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblCuenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(lblFechaHasta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlConsultaLibroMayorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jdcFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jdcFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbCuentas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAdvertenciaRangoFechaInvalido, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlResultadosConsulta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        spnlMovimientos.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. MOVIMIENTOS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        tblMovimientos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "FECHA", "GLOSA", "DEBE", "HABER", "SALDO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlMovimientos.setViewportView(tblMovimientos);

        pnlResumen.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "03. RESUMEN", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblMovimientos.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblMovimientos.setText("MOVIMIENTOS");

        lblCantMovimientos.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblCantMovimientos.setText("4");

        lblTotalDebe.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalDebe.setText("TOTAL DEBE");

        lblValorTotalDebe.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalDebe.setText("S/ 1,700.00");

        lblTotalHaber.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalHaber.setText("TOTAL HABER");

        lblValorTotalHaber.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalHaber.setText("S/ 200.00");

        lblSaldoFinal.setFont(new java.awt.Font("Segoe UI Historic", 1, 10)); // NOI18N
        lblSaldoFinal.setText("SALDO FINAL");

        lblValorSaldoFinal.setFont(new java.awt.Font("Consolas", 1, 18)); // NOI18N
        lblValorSaldoFinal.setText("S/ 2,300.00");

        javax.swing.GroupLayout pnlResumenLayout = new javax.swing.GroupLayout(pnlResumen);
        pnlResumen.setLayout(pnlResumenLayout);
        pnlResumenLayout.setHorizontalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblCantMovimientos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblMovimientos))
                .addGap(65, 65, 65)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblTotalDebe, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorTotalDebe, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(65, 65, 65)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblValorTotalHaber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTotalHaber, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlResumenLayout.createSequentialGroup()
                        .addComponent(lblSaldoFinal, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(lblValorSaldoFinal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlResumenLayout.setVerticalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMovimientos)
                    .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblTotalDebe, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblTotalHaber)
                        .addComponent(lblSaldoFinal)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblCantMovimientos)
                        .addComponent(lblValorTotalDebe))
                    .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblValorTotalHaber)
                        .addComponent(lblValorSaldoFinal)))
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
                    .addComponent(pnlConsultaLibroMayor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(spnlMovimientos)
                    .addComponent(pnlResumen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlConsultaLibroMayor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnlMovimientos, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlResumen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        // TODO add your handling code here:
        consultarLibroMayor();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
        limpiarFormulario();
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
            java.util.logging.Logger.getLogger(FrmLibroMayor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmLibroMayor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmLibroMayor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmLibroMayor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmLibroMayor dialog = new FrmLibroMayor(new javax.swing.JFrame(), true);
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
    private javax.swing.JComboBox<PlanCuenta> cmbCuentas;
    private com.toedter.calendar.JDateChooser jdcFechaDesde;
    private com.toedter.calendar.JDateChooser jdcFechaHasta;
    private javax.swing.JLabel lblCantMovimientos;
    private javax.swing.JLabel lblCodigoCuenta;
    private javax.swing.JLabel lblCuenta;
    private javax.swing.JLabel lblDiferenciaDeCaja;
    private javax.swing.JLabel lblFechaActual;
    private javax.swing.JLabel lblFechaDesde;
    private javax.swing.JLabel lblFechaHasta;
    private javax.swing.JLabel lblHoraActual;
    private javax.swing.JLabel lblIdCuenta;
    private javax.swing.JLabel lblLibroMayor;
    private javax.swing.JLabel lblMovimientoSaldoAcumuladoCuentaContable;
    private javax.swing.JLabel lblMovimientos;
    private javax.swing.JLabel lblNombreApellidoUsuario;
    private javax.swing.JLabel lblNombreCuenta;
    private javax.swing.JLabel lblSaldoFinal;
    private javax.swing.JLabel lblTotalDebe;
    private javax.swing.JLabel lblTotalHaber;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblValorCodigoCuenta;
    private javax.swing.JLabel lblValorIdCuenta;
    private javax.swing.JLabel lblValorNombreCuenta;
    private javax.swing.JLabel lblValorSaldoFinal;
    private javax.swing.JLabel lblValorTotalDebe;
    private javax.swing.JLabel lblValorTotalHaber;
    private javax.swing.JPanel pnlAdvertenciaRangoFechaInvalido;
    private javax.swing.JPanel pnlConsultaLibroMayor;
    private javax.swing.JPanel pnlResultadosConsulta;
    private javax.swing.JPanel pnlResumen;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JScrollPane spnlMovimientos;
    private javax.swing.JTable tblMovimientos;
    // End of variables declaration//GEN-END:variables
}
