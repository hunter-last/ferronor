package com.ferronor.sic.compras.vista;

import com.ferronor.sic.compras.logica.CompraService;
import com.ferronor.sic.compras.modelo.EstadoCuenta;
import com.ferronor.sic.compras.modelo.PagoProveedor;
import com.ferronor.sic.compras.modelo.dto.CuentaPagarConsulta;
import com.ferronor.sic.maestros.logica.ProveedorService;
import com.ferronor.sic.maestros.modelo.Proveedor;
import com.ferronor.sic.procesos.ProcesoPagoProveedor;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;
import com.ferronor.sic.tesoreria.logica.TesoreriaService;
import com.ferronor.sic.tesoreria.modelo.Caja;
import com.ferronor.sic.tesoreria.modelo.CuentaBancaria;
import com.ferronor.sic.shared.ui.ComboAutoFiltro;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class FrmPagoProveedor extends javax.swing.JDialog {

    // ============================================================
    // SERVICES
    // ============================================================
    private final CompraService compraService
            = ServiceFactory.compraService();

    private final ProveedorService proveedorService
            = ServiceFactory.proveedorService();

    private final TesoreriaService tesoreriaService
            = ServiceFactory.tesoreriaService();

    private final ProcesoPagoProveedor procesoPagoProveedor
            = ServiceFactory.procesoPagoProveedor();

    // ============================================================
    // FORMATOS
    // ============================================================
    private final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DateTimeFormatter FORMATO_HORA
            = DateTimeFormatter.ofPattern("HH:mm");

    // ============================================================
    // ESTADO DE LA VISTA
    // ============================================================
    private List<CuentaPagarConsulta> cuentasConsultadas
            = new ArrayList<>();

    private CuentaPagarConsulta cuentaSeleccionada;

    private Caja cajaAbierta;

    private List<CuentaBancaria> cuentasBancariasActivas
            = new ArrayList<>();

    private CuentaBancaria cuentaBancariaSeleccionada;

    private DefaultTableModel modeloTablaCuentas;

    /**
     * true = Banco false = Efectivo
     */
    private boolean medioBanco;

    /**
     * Evita que cambios programáticos del combo bancario disparen lógica de
     * selección.
     */
    private boolean actualizandoInterfaz;

    // ============================================================
    // COMPONENTES DINÁMICOS DEL PANEL BANCO
    // ============================================================
    private javax.swing.JComboBox<CuentaBancaria> cmbCuenta;

    private javax.swing.JLabel lblTituloCuentaBancaria;

    private javax.swing.JLabel lblCuentaBancaria;

    private javax.swing.JLabel lblMonedaCuentaBancaria;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    public FrmPagoProveedor(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        initComponents();

        configurarFormulario();
    }

    // ============================================================
    // CONFIGURACIÓN GENERAL
    // ============================================================
    private void configurarFormulario() {

        configurarTabla();

        configurarCombos();

        configurarSelectorBanco();

        configurarEventosManual();

        configurarEstadoInicial();

        cargarDatosIniciales();
    }

    // ============================================================
    // TABLA
    // ============================================================
    private void configurarTabla() {

        modeloTablaCuentas = new DefaultTableModel(
                new Object[]{
                    "COMPRA",
                    "PROVEEDOR",
                    "FECHA COMPRA",
                    "VENCIMIENTO",
                    "TOTAL",
                    "PAGADO",
                    "SALDO",
                    "ESTADO"
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

        tblDetalleCuentasPagar.setModel(
                modeloTablaCuentas
        );

        tblDetalleCuentasPagar.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblDetalleCuentasPagar.setRowHeight(27);

        tblDetalleCuentasPagar.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        9
                )
        );

        tblDetalleCuentasPagar
                .getTableHeader()
                .setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                9
                        )
                );

        tblDetalleCuentasPagar
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (e.getValueIsAdjusting()) {
                        return;
                    }

                    seleccionarCuentaDesdeTabla();
                });
    }

    // ============================================================
    // COMBOS
    // ============================================================
    private void configurarCombos() {

        configurarComboEstados();

        configurarComboProveedores();
    }

    private void configurarComboEstados() {

        cmbEstado.removeAllItems();

        cmbEstado.addItem("Todos");
        cmbEstado.addItem("Pendiente");
        cmbEstado.addItem("Vencida");
        cmbEstado.addItem("Pagada");

        /*
         * En esta pantalla resulta más útil iniciar con
         * cuentas que realmente pueden recibir un pago.
         */
        cmbEstado.setSelectedItem("Pendiente");
    }

    private void configurarComboProveedores() {

        cmbProveedores.removeAllItems();

        cmbProveedores.setRenderer(
                new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list,
                        value,
                        index,
                        isSelected,
                        cellHasFocus
                );

                if (value instanceof Proveedor proveedor) {

                    setText(
                            valorTexto(
                                    proveedor.getRazonSocial()
                            )
                    );
                }

                return this;
            }
        });

        /*
     * Búsqueda incremental:
     *
     * texto escrito
     *      ↓
     * ProveedorService.buscarActivosPorRazonSocialORucParcial(...)
     *      ↓
     * ComboAutoFiltro
     *      ↓
     * JComboBox<Proveedor>
         */
        ComboAutoFiltro.mejorarCombo(
                cmbProveedores,
                proveedorService::buscarActivosPorRazonSocialORucParcial
        );

        /*
     * Al iniciar no queremos un texto ni una selección
     * accidental.
         */
        cmbProveedores.setSelectedItem(null);

        javax.swing.JTextField editor
                = (javax.swing.JTextField) cmbProveedores
                        .getEditor()
                        .getEditorComponent();

        editor.setText("");
    }

    // ============================================================
    // ESTADO INICIAL
    // ============================================================
    private void configurarEstadoInicial() {

        cuentaSeleccionada = null;

        cajaAbierta = null;

        cuentaBancariaSeleccionada = null;

        medioBanco = false;

        cmbProveedores.setSelectedItem(null);

        cmbEstado.setSelectedItem("Pendiente");

        jdcDesdeFecha.setDate(null);

        jdcHastaFecha.setDate(null);

        txtFechaYHoraPago.setEditable(false);

        txtFechaYHoraPago.setText(
                LocalDateTime.now()
                        .format(FORMATO_FECHA_HORA)
        );

        txtEstadoCuentaPagar.setEditable(false);

        txtEstadoPago.setEditable(false);

        txtValorMontoAPagar.setText("");

        txtValorMontoAPagar.setEnabled(false);

        btnEfectivo.setEnabled(false);

        btnBanco.setEnabled(false);

        btnRegistrarPago.setEnabled(false);

        pnlDatosCaja.setVisible(false);

        pnlSelectorCtaBancaria.setVisible(false);

        limpiarCuentaSeleccionada();

        actualizarDatosUsuario();

        lblNCuentasPagarFiltrosSeleccionados.setText(
                "Seleccione los filtros y consulte las cuentas por pagar"
        );
    }

    // ============================================================
    // CABECERA
    // ============================================================
    private void actualizarDatosUsuario() {

        SesionUsuario sesion
                = SesionUsuario.actual();

        lblNombreApellidoUsuario.setText(
                sesion.getNombreCompleto()
        );

        LocalDateTime ahora
                = LocalDateTime.now();

        lblFechaActual.setText(
                ahora.format(FORMATO_FECHA)
        );

        lblHoraActual.setText(
                ahora.format(FORMATO_HORA)
        );
    }

    // ============================================================
    // EVENTOS MANUALES
    // ============================================================
    private void configurarEventosManual() {

        /*
         * Estos botones no tienen actionPerformed generado
         * por NetBeans.
         */
        btnEfectivo.addActionListener(
                e -> mostrarMedioEfectivo()
        );

        btnBanco.addActionListener(
                e -> mostrarMedioBanco()
        );

        txtValorMontoAPagar
                .getDocument()
                .addDocumentListener(
                        new DocumentListener() {

                    @Override
                    public void insertUpdate(
                            DocumentEvent e) {

                        actualizarCalculoPago();
                    }

                    @Override
                    public void removeUpdate(
                            DocumentEvent e) {

                        actualizarCalculoPago();
                    }

                    @Override
                    public void changedUpdate(
                            DocumentEvent e) {

                        actualizarCalculoPago();
                    }
                });
    }

    // ============================================================
    // CARGA INICIAL
    // ============================================================
    private void cargarDatosIniciales() {

        cargarCajaAbierta();

        cargarCuentasBancariasActivas();

        consultarCuentasPorPagar();
    }

    // ============================================================
    // CAJA ABIERTA
    // ============================================================
    private void cargarCajaAbierta() {

        try {

            Optional<Caja> resultado
                    = tesoreriaService
                            .obtenerCajaAbierta();

            if (resultado.isPresent()) {

                cajaAbierta = resultado.get();

                lblCajaAbierta.setText(
                        "● CAJA ABIERTA"
                );

                lblCajaPrincipal.setText(
                        valorTexto(
                                cajaAbierta.getNombre()
                        )
                );

                lblValorSaldoActual.setText(
                        "S/ "
                        + formatearMonto(
                                cajaAbierta.getSaldoActual()
                        )
                );

            } else {

                cajaAbierta = null;

                lblCajaAbierta.setText(
                        "● SIN CAJA ABIERTA"
                );

                lblCajaPrincipal.setText(
                        "No disponible"
                );

                lblValorSaldoActual.setText(
                        "S/ 0.00"
                );
            }

        } catch (RuntimeException ex) {

            cajaAbierta = null;

            lblCajaAbierta.setText(
                    "● SIN CAJA ABIERTA"
            );

            lblCajaPrincipal.setText(
                    "No disponible"
            );

            lblValorSaldoActual.setText(
                    "S/ 0.00"
            );
        }

        actualizarEstadoBotonRegistrar();
    }

    // ============================================================
    // PANEL DINÁMICO DE CUENTA BANCARIA
    // ============================================================
    private void configurarSelectorBanco() {

        cmbCuenta
                = new javax.swing.JComboBox<>();

        lblTituloCuentaBancaria
                = new javax.swing.JLabel(
                        "CUENTA BANCARIA"
                );

        lblCuentaBancaria
                = new javax.swing.JLabel(
                        "CUENTA"
                );

        lblMonedaCuentaBancaria
                = new javax.swing.JLabel(
                        "MONEDA"
                );

        lblTituloCuentaBancaria.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        10
                )
        );

        lblCuentaBancaria.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        10
                )
        );

        lblMonedaCuentaBancaria.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        10
                )
        );

        /*
         * pnlSelectorCtaBancaria ya está dentro de
         * pnlRegistrarPago en el .form definitivo.
         *
         * Aquí solamente reemplazamos el contenido
         * reservado por NetBeans.
         */
        pnlSelectorCtaBancaria.removeAll();

        pnlSelectorCtaBancaria.setLayout(
                new BorderLayout()
        );

        javax.swing.JPanel contenido
                = new javax.swing.JPanel();

        javax.swing.GroupLayout layout
                = new javax.swing.GroupLayout(
                        contenido
                );

        contenido.setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(
                        javax.swing.GroupLayout.Alignment.LEADING
                )
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addGap(10)
                                        .addGroup(
                                                layout.createParallelGroup(
                                                        javax.swing.GroupLayout.Alignment.LEADING
                                                )
                                                        .addComponent(
                                                                lblTituloCuentaBancaria
                                                        )
                                                        .addGroup(
                                                                layout.createSequentialGroup()
                                                                        .addComponent(
                                                                                lblCuentaBancaria,
                                                                                65,
                                                                                65,
                                                                                65
                                                                        )
                                                                        .addGap(8)
                                                                        .addComponent(
                                                                                cmbCuenta,
                                                                                0,
                                                                                180,
                                                                                Short.MAX_VALUE
                                                                        )
                                                                        .addGap(8)
                                                                        .addComponent(
                                                                                lblMonedaCuentaBancaria,
                                                                                55,
                                                                                55,
                                                                                55
                                                                        )
                                                        )
                                        )
                                        .addGap(10)
                        )
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGap(7)
                        .addComponent(
                                lblTituloCuentaBancaria
                        )
                        .addGap(4)
                        .addGroup(
                                layout.createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.CENTER
                                )
                                        .addComponent(
                                                lblCuentaBancaria
                                        )
                                        .addComponent(
                                                cmbCuenta
                                        )
                                        .addComponent(
                                                lblMonedaCuentaBancaria
                                        )
                        )
                        .addGap(7)
        );

        pnlSelectorCtaBancaria.add(
                contenido,
                BorderLayout.CENTER
        );

        cmbCuenta.setRenderer(
                new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list,
                        value,
                        index,
                        isSelected,
                        cellHasFocus
                );

                if (value instanceof CuentaBancaria cuenta) {

                    String alias
                            = cuenta.getAlias();

                    if (alias == null
                            || alias.isBlank()) {

                        alias = cuenta.getBanco();
                    }

                    String numero
                            = cuenta.getNumeroCuenta();

                    setText(
                            valorTexto(alias)
                            + " — "
                            + valorTexto(numero)
                    );
                }

                return this;
            }
        });

        cmbCuenta.addActionListener(
                e -> {

                    if (actualizandoInterfaz) {
                        return;
                    }

                    CuentaBancaria cuenta
                    = (CuentaBancaria) cmbCuenta.getSelectedItem();

                    cuentaBancariaSeleccionada
                    = cuenta;

                    actualizarDatosCuentaBancaria();

                    actualizarEstadoBotonRegistrar();
                }
        );

        pnlSelectorCtaBancaria.setVisible(false);

        pnlSelectorCtaBancaria.revalidate();

        pnlSelectorCtaBancaria.repaint();
    }

    // ============================================================
    // CUENTAS BANCARIAS ACTIVAS
    // ============================================================
    private void cargarCuentasBancariasActivas() {

        try {

            List<CuentaBancaria> cuentas
                    = tesoreriaService
                            .listarCuentasBancariasActivas();

            if (cuentas == null) {

                cuentas
                        = Collections.emptyList();
            }

            cuentasBancariasActivas
                    = new ArrayList<>(
                            cuentas
                    );

            cuentasBancariasActivas.sort(
                    Comparator.comparing(
                            cuenta -> {

                                String alias
                                = cuenta.getAlias();

                                if (alias == null
                                || alias.isBlank()) {

                                    alias
                                    = cuenta.getBanco();
                                }

                                return alias == null
                                        ? ""
                                        : alias.toUpperCase();
                            }
                    )
            );

            actualizandoInterfaz = true;

            cmbCuenta.setModel(
                    new DefaultComboBoxModel<>(
                            cuentasBancariasActivas
                                    .toArray(
                                            new CuentaBancaria[0]
                                    )
                    )
            );

            cmbCuenta.setSelectedItem(null);

            cuentaBancariaSeleccionada = null;

            actualizandoInterfaz = false;

            actualizarDatosCuentaBancaria();

        } catch (RuntimeException ex) {

            cuentasBancariasActivas
                    = new ArrayList<>();

            cuentaBancariaSeleccionada = null;

            actualizandoInterfaz = true;

            cmbCuenta.setModel(
                    new DefaultComboBoxModel<>()
            );

            cmbCuenta.setSelectedItem(null);

            actualizandoInterfaz = false;

            actualizarDatosCuentaBancaria();
        }

        actualizarEstadoBotonRegistrar();
    }

    private void actualizarDatosCuentaBancaria() {

        if (cuentaBancariaSeleccionada == null) {

            lblTituloCuentaBancaria.setText(
                    "CUENTA BANCARIA"
            );

            lblMonedaCuentaBancaria.setText("");

            return;
        }

        String alias
                = cuentaBancariaSeleccionada
                        .getAlias();

        if (alias == null
                || alias.isBlank()) {

            alias
                    = cuentaBancariaSeleccionada
                            .getBanco();
        }

        lblTituloCuentaBancaria.setText(
                "CUENTA BANCARIA · "
                + valorTexto(alias)
        );

        if (cuentaBancariaSeleccionada
                .getMoneda() != null) {

            lblMonedaCuentaBancaria.setText(
                    cuentaBancariaSeleccionada
                            .getMoneda()
                            .name()
            );

        } else {

            lblMonedaCuentaBancaria.setText("");
        }
    }

    // ============================================================
    // CONSULTA
    // ============================================================
    private void consultarCuentasPorPagar() {

        try {

            LocalDate fechaDesde
                    = obtenerFecha(
                            jdcDesdeFecha
                    );

            LocalDate fechaHasta
                    = obtenerFecha(
                            jdcHastaFecha
                    );

            if (fechaDesde != null
                    && fechaHasta != null
                    && fechaDesde.isAfter(
                            fechaHasta
                    )) {

                JOptionPane.showMessageDialog(
                        this,
                        "La fecha DESDE no puede ser "
                        + "posterior a la fecha HASTA.",
                        "Rango de fechas inválido",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            EstadoCuenta estado
                    = obtenerEstadoSeleccionado();

            Integer idProveedor
                    = obtenerIdProveedorSeleccionado();

            List<CuentaPagarConsulta> resultados
                    = compraService
                            .consultarCuentasPorPagar(
                                    estado,
                                    idProveedor,
                                    fechaDesde,
                                    fechaHasta
                            );

            cuentasConsultadas
                    = resultados != null
                            ? new ArrayList<>(resultados)
                            : new ArrayList<>();

            cargarTablaCuentas(
                    cuentasConsultadas
            );

            limpiarCuentaSeleccionada();

            actualizarContadorResultados();

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al consultar cuentas por pagar",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private EstadoCuenta obtenerEstadoSeleccionado() {

        Object seleccionado
                = cmbEstado.getSelectedItem();

        if (seleccionado == null) {
            return null;
        }

        return switch (seleccionado.toString()) {

            case "Pendiente" ->
                EstadoCuenta.PENDIENTE;

            case "Pagada" ->
                EstadoCuenta.PAGADA;

            case "Vencida" ->
                EstadoCuenta.VENCIDA;

            default ->
                null;
        };
    }

    private Integer obtenerIdProveedorSeleccionado() {

        Object seleccionado
                = cmbProveedores.getSelectedItem();

        if (seleccionado instanceof Proveedor proveedor) {

            return proveedor.getIdProveedor();
        }

        return null;
    }

    private LocalDate obtenerFecha(
            com.toedter.calendar.JDateChooser chooser) {

        Date fecha
                = chooser.getDate();

        if (fecha == null) {
            return null;
        }

        return fecha.toInstant()
                .atZone(
                        ZoneId.systemDefault()
                )
                .toLocalDate();
    }

    // ============================================================
    // TABLA
    // ============================================================
    private void cargarTablaCuentas(
            List<CuentaPagarConsulta> cuentas) {

        modeloTablaCuentas.setRowCount(0);

        for (CuentaPagarConsulta cuenta
                : cuentas) {

            modeloTablaCuentas.addRow(
                    new Object[]{
                        "#" + cuenta.getIdCompra(),
                        valorTexto(
                                cuenta
                                        .getRazonSocialProveedor()
                        ),
                        formatearFechaHora(
                                cuenta
                                        .getFechaCompra()
                        ),
                        formatearFecha(
                                cuenta
                                        .getFechaVencimiento()
                        ),
                        "S/ "
                        + formatearMonto(
                                cuenta
                                        .getMontoTotal()
                        ),
                        "S/ "
                        + formatearMonto(
                                cuenta
                                        .getMontoPagado()
                        ),
                        "S/ "
                        + formatearMonto(
                                cuenta
                                        .getSaldoPendiente()
                        ),
                        formatearEstado(
                                cuenta
                                        .getEstado()
                        )
                    }
            );
        }
    }

    private void seleccionarCuentaDesdeTabla() {

        int filaVista
                = tblDetalleCuentasPagar
                        .getSelectedRow();

        if (filaVista < 0) {

            if (cuentaSeleccionada != null) {

                limpiarCuentaSeleccionada();
            }

            return;
        }

        int filaModelo
                = tblDetalleCuentasPagar
                        .convertRowIndexToModel(
                                filaVista
                        );

        if (filaModelo < 0
                || filaModelo
                >= cuentasConsultadas.size()) {

            limpiarCuentaSeleccionada();

            return;
        }

        cuentaSeleccionada
                = cuentasConsultadas.get(
                        filaModelo
                );

        mostrarCuentaSeleccionada();
    }

    // ============================================================
    // CUENTA SELECCIONADA
    // ============================================================
    private void mostrarCuentaSeleccionada() {

        if (cuentaSeleccionada == null) {

            limpiarCuentaSeleccionada();

            return;
        }

        lblNombreEmpresaYTipoEmpresa.setText(
                valorTexto(
                        cuentaSeleccionada
                                .getRazonSocialProveedor()
                )
        );

        lblRucNroRuc.setText(
                "RUC "
                + valorTexto(
                        cuentaSeleccionada
                                .getRucProveedor()
                )
        );

        lblValorCodigoCompra.setText(
                "#"
                + cuentaSeleccionada
                        .getIdCompra()
        );

        lblValorFechaCompra.setText(
                formatearFechaHora(
                        cuentaSeleccionada
                                .getFechaCompra()
                )
        );

        lblValorFechaVencimiento.setText(
                formatearFecha(
                        cuentaSeleccionada
                                .getFechaVencimiento()
                )
        );

        txtEstadoCuentaPagar.setText(
                "• "
                + formatearEstado(
                        cuentaSeleccionada
                                .getEstado()
                )
        );

        lblSaldoTotal.setText(
                formatearMonto(
                        cuentaSeleccionada
                                .getMontoTotal()
                )
        );

        lblSaldoCobrado.setText(
                formatearMonto(
                        cuentaSeleccionada
                                .getMontoPagado()
                )
        );

        lblValorSaldoPend.setText(
                formatearMonto(
                        cuentaSeleccionada
                                .getSaldoPendiente()
                )
        );

        lblMensajeNoPuedeExcederSaldoPendienteValorSaldoPendiente
                .setText(
                        "No puede exceder el saldo pendiente (S/ "
                        + formatearMonto(
                                cuentaSeleccionada
                                        .getSaldoPendiente()
                        )
                        + ")"
                );

        txtValorMontoAPagar.setText("");

        boolean tieneSaldo
                = cuentaSeleccionada
                        .getSaldoPendiente() != null
                && cuentaSeleccionada
                        .getSaldoPendiente()
                        .compareTo(
                                BigDecimal.ZERO
                        ) > 0;

        habilitarComponentesPago(
                tieneSaldo
        );

        actualizarCalculoPago();
    }

    private void limpiarCuentaSeleccionada() {

        cuentaSeleccionada = null;

        if (tblDetalleCuentasPagar
                .getSelectedRow() >= 0) {

            tblDetalleCuentasPagar
                    .clearSelection();
        }

        lblNombreEmpresaYTipoEmpresa.setText(
                "Sin cuenta seleccionada"
        );

        lblRucNroRuc.setText("");

        lblValorCodigoCompra.setText(
                "—"
        );

        lblValorFechaCompra.setText(
                "—"
        );

        lblValorFechaVencimiento.setText(
                "—"
        );

        txtEstadoCuentaPagar.setText(
                "• Sin selección"
        );

        lblSaldoTotal.setText(
                "0.00"
        );

        lblSaldoCobrado.setText(
                "0.00"
        );

        lblValorSaldoPend.setText(
                "0.00"
        );

        lblMensajeNoPuedeExcederSaldoPendienteValorSaldoPendiente
                .setText(
                        "Seleccione una cuenta por pagar"
                );

        txtValorMontoAPagar.setText("");

        txtEstadoPago.setText(
                "SIN CUENTA"
        );

        lblValorSaldoPendiente.setText(
                "0.00"
        );

        lblValorMontoACobrar.setText(
                "0.00"
        );

        lblValorNuevoSaldo.setText(
                "0.00"
        );

        txtValorMontoAPagar.setEnabled(false);

        btnRegistrarPago.setEnabled(false);

        btnEfectivo.setEnabled(false);

        btnBanco.setEnabled(false);

        pnlDatosCaja.setVisible(false);

        pnlSelectorCtaBancaria.setVisible(false);

        medioBanco = false;

        cuentaBancariaSeleccionada = null;

        actualizarLayoutMedioPago();
    }

    // ============================================================
    // HABILITACIÓN DEL REGISTRO
    // ============================================================
    private void habilitarComponentesPago(
            boolean habilitar) {

        txtValorMontoAPagar.setEnabled(
                habilitar
        );

        txtFechaYHoraPago.setEnabled(
                true
        );

        btnEfectivo.setEnabled(
                habilitar
                && cajaAbierta != null
        );

        btnBanco.setEnabled(
                habilitar
                && !cuentasBancariasActivas.isEmpty()
        );

        btnRegistrarPago.setEnabled(false);

        if (!habilitar) {

            pnlDatosCaja.setVisible(false);

            pnlSelectorCtaBancaria.setVisible(false);

            txtValorMontoAPagar.setText("");

            txtEstadoPago.setText(
                    "SIN CUENTA"
            );

            medioBanco = false;

            cuentaBancariaSeleccionada = null;

            return;
        }

        /*
         * Al seleccionar una cuenta:
         * Efectivo es el medio inicial.
         */
        mostrarMedioEfectivo();
    }

    // ============================================================
    // MEDIO EFECTIVO
    // ============================================================
    private void mostrarMedioEfectivo() {

        if (cuentaSeleccionada == null) {
            return;
        }

        medioBanco = false;

        pnlDatosCaja.setVisible(true);

        pnlSelectorCtaBancaria.setVisible(false);

        btnEfectivo.setEnabled(
                cajaAbierta != null
        );

        btnBanco.setEnabled(
                !cuentasBancariasActivas.isEmpty()
        );

        actualizarEstadoBotonRegistrar();

        actualizarLayoutMedioPago();
    }

    // ============================================================
    // MEDIO BANCO
    // ============================================================
    private void mostrarMedioBanco() {

        if (cuentaSeleccionada == null) {
            return;
        }

        if (cuentasBancariasActivas.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "No hay cuentas bancarias activas disponibles.",
                    "Cuenta bancaria no disponible",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        medioBanco = true;

        pnlDatosCaja.setVisible(false);

        pnlSelectorCtaBancaria.setVisible(true);

        btnBanco.setEnabled(true);

        btnEfectivo.setEnabled(
                cajaAbierta != null
        );

        cmbCuenta.setEnabled(true);

        if (cuentaBancariaSeleccionada == null) {

            actualizandoInterfaz = true;

            cmbCuenta.setSelectedIndex(0);

            cuentaBancariaSeleccionada
                    = (CuentaBancaria) cmbCuenta
                            .getSelectedItem();

            actualizandoInterfaz = false;

            actualizarDatosCuentaBancaria();
        }

        actualizarEstadoBotonRegistrar();

        actualizarLayoutMedioPago();
    }

    private void actualizarLayoutMedioPago() {

        pnlDatosCaja.revalidate();
        pnlDatosCaja.repaint();

        pnlSelectorCtaBancaria.revalidate();
        pnlSelectorCtaBancaria.repaint();

        pnlRegistrarPago.revalidate();
        pnlRegistrarPago.repaint();

        pnlCuentaSeleccionada.revalidate();
        pnlCuentaSeleccionada.repaint();

        spnlAcomodador.revalidate();
        spnlAcomodador.repaint();

        getContentPane().revalidate();
        getContentPane().repaint();
    }

    // ============================================================
    // CÁLCULO DEL PAGO
    // ============================================================
    private void actualizarCalculoPago() {

        if (cuentaSeleccionada == null) {

            txtEstadoPago.setText(
                    "SIN CUENTA"
            );

            actualizarEstadoBotonRegistrar();

            return;
        }

        BigDecimal saldo
                = valorSeguro(
                        cuentaSeleccionada
                                .getSaldoPendiente()
                );

        BigDecimal monto
                = obtenerMontoIngresado();

        lblValorSaldoPendiente.setText(
                formatearMonto(saldo)
        );

        if (monto == null) {

            lblValorMontoACobrar.setText(
                    "0.00"
            );

            lblValorNuevoSaldo.setText(
                    formatearMonto(saldo)
            );

            txtEstadoPago.setText(
                    "SIN MONTO"
            );

            actualizarEstadoBotonRegistrar();

            return;
        }

        BigDecimal nuevoSaldo
                = saldo.subtract(monto);

        lblValorMontoACobrar.setText(
                formatearMonto(monto)
        );

        if (monto.compareTo(saldo) > 0) {

            lblValorNuevoSaldo.setText(
                    "—"
            );

            txtEstadoPago.setText(
                    "MONTO INVÁLIDO"
            );

            actualizarEstadoBotonRegistrar();

            return;
        }

        lblValorNuevoSaldo.setText(
                formatearMonto(nuevoSaldo)
        );

        if (nuevoSaldo.compareTo(
                BigDecimal.ZERO
        ) == 0) {

            txtEstadoPago.setText(
                    "PAGO TOTAL"
            );

        } else {

            txtEstadoPago.setText(
                    "PAGO PARCIAL"
            );
        }

        actualizarEstadoBotonRegistrar();
    }

    private BigDecimal obtenerMontoIngresado() {

        String texto
                = txtValorMontoAPagar
                        .getText()
                        .trim()
                        .replace(",", "");

        if (texto.isBlank()) {
            return null;
        }

        try {

            BigDecimal monto
                    = new BigDecimal(texto)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            if (monto.compareTo(
                    BigDecimal.ZERO
            ) <= 0) {

                return null;
            }

            return monto;

        } catch (NumberFormatException ex) {

            return null;
        }
    }

    // ============================================================
    // VALIDACIÓN
    // ============================================================
    private boolean validarRegistroPago() {

        if (cuentaSeleccionada == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Debe seleccionar una cuenta por pagar.",
                    "Cuenta obligatoria",
                    JOptionPane.WARNING_MESSAGE
            );

            return false;
        }

        BigDecimal saldo
                = valorSeguro(
                        cuentaSeleccionada
                                .getSaldoPendiente()
                );

        BigDecimal monto
                = obtenerMontoIngresado();

        if (monto == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Ingrese un monto válido mayor a cero.",
                    "Monto inválido",
                    JOptionPane.WARNING_MESSAGE
            );

            txtValorMontoAPagar.requestFocus();

            return false;
        }

        if (monto.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "El monto debe ser mayor a cero.",
                    "Monto inválido",
                    JOptionPane.WARNING_MESSAGE
            );

            txtValorMontoAPagar.requestFocus();

            return false;
        }

        if (monto.compareTo(saldo) > 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "El monto a pagar no puede exceder "
                    + "el saldo pendiente de S/ "
                    + formatearMonto(saldo)
                    + ".",
                    "Monto excedido",
                    JOptionPane.WARNING_MESSAGE
            );

            txtValorMontoAPagar.requestFocus();

            return false;
        }

        // --------------------------------------------------------
        // EFECTIVO
        // --------------------------------------------------------
        if (!medioBanco) {

            if (cajaAbierta == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "No hay una caja abierta disponible.",
                        "Caja no disponible",
                        JOptionPane.WARNING_MESSAGE
                );

                return false;
            }

            return true;
        }

        // --------------------------------------------------------
        // BANCO
        // --------------------------------------------------------
        if (cuentasBancariasActivas.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "No hay cuentas bancarias activas disponibles.",
                    "Cuenta bancaria no disponible",
                    JOptionPane.WARNING_MESSAGE
            );

            return false;
        }

        if (cuentaBancariaSeleccionada == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione una cuenta bancaria.",
                    "Cuenta bancaria obligatoria",
                    JOptionPane.WARNING_MESSAGE
            );

            cmbCuenta.requestFocus();

            return false;
        }

        return true;
    }

    // ============================================================
    // ESTADO DEL BOTÓN REGISTRAR
    // ============================================================
    private void actualizarEstadoBotonRegistrar() {

        if (cuentaSeleccionada == null) {

            btnRegistrarPago.setEnabled(false);

            return;
        }

        BigDecimal monto
                = obtenerMontoIngresado();

        if (monto == null) {

            btnRegistrarPago.setEnabled(false);

            return;
        }

        BigDecimal saldo
                = valorSeguro(
                        cuentaSeleccionada
                                .getSaldoPendiente()
                );

        if (monto.compareTo(
                BigDecimal.ZERO
        ) <= 0
                || monto.compareTo(saldo) > 0) {

            btnRegistrarPago.setEnabled(false);

            return;
        }

        // --------------------------------------------------------
        // EFECTIVO
        // --------------------------------------------------------
        if (!medioBanco) {

            btnRegistrarPago.setEnabled(
                    cajaAbierta != null
            );

            return;
        }

        // --------------------------------------------------------
        // BANCO
        // --------------------------------------------------------
        btnRegistrarPago.setEnabled(
                cuentaBancariaSeleccionada != null
        );
    }

    // ============================================================
    // REGISTRAR PAGO
    // ============================================================
    private void registrarPago() {

        if (!validarRegistroPago()) {
            return;
        }

        BigDecimal monto
                = obtenerMontoIngresado();

        LocalDateTime fechaHora
                = LocalDateTime.now();

        PagoProveedor pago
                = new PagoProveedor(
                        cuentaSeleccionada
                                .getIdCompra(),
                        monto,
                        fechaHora
                );

        int idUsuario
                = SesionUsuario.actual()
                        .getIdUsuario();

        try {

            RespuestaOperacion<Void> respuesta;

            // ----------------------------------------------------
            // EFECTIVO
            // ----------------------------------------------------
            if (!medioBanco) {

                respuesta
                        = procesoPagoProveedor
                                .registrarPagoCaja(
                                        pago,
                                        cajaAbierta
                                                .getIdCaja(),
                                        idUsuario
                                );

            } else {

                // ------------------------------------------------
                // BANCO
                // ------------------------------------------------
                respuesta
                        = procesoPagoProveedor
                                .registrarPagoBanco(
                                        pago,
                                        cuentaBancariaSeleccionada
                                                .getIdCuentaBancaria(),
                                        idUsuario
                                );
            }

            if (!respuesta.isExito()) {

                JOptionPane.showMessageDialog(
                        this,
                        respuesta.getMensaje(),
                        "No se pudo registrar el pago",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "El pago se registró correctamente.",
                    "Pago registrado",
                    JOptionPane.INFORMATION_MESSAGE
            );

            limpiarDespuesDePago();

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al registrar pago",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // LIMPIAR DESPUÉS DE PAGO
    // ============================================================
    private void limpiarDespuesDePago() {

        cuentaSeleccionada = null;

        cuentaBancariaSeleccionada = null;

        medioBanco = false;

        txtValorMontoAPagar.setText("");

        txtEstadoPago.setText(
                "SIN CUENTA"
        );

        tblDetalleCuentasPagar.clearSelection();

        limpiarCuentaSeleccionada();

        cargarCajaAbierta();

        cargarCuentasBancariasActivas();

        txtFechaYHoraPago.setText(
                LocalDateTime.now()
                        .format(FORMATO_FECHA_HORA)
        );

        consultarCuentasPorPagar();
    }

    // ============================================================
    // LIMPIAR FILTROS
    // ============================================================
    private void limpiarFiltros() {

        cmbProveedores.setSelectedItem(null);

        javax.swing.JTextField editor
                = (javax.swing.JTextField) cmbProveedores
                        .getEditor()
                        .getEditorComponent();

        editor.setText("");

        cmbEstado.setSelectedItem(
                "Pendiente"
        );

        jdcDesdeFecha.setDate(null);

        jdcHastaFecha.setDate(null);

        consultarCuentasPorPagar();
    }

    // ============================================================
    // CONTADOR
    // ============================================================
    private void actualizarContadorResultados() {

        int cantidad
                = cuentasConsultadas.size();

        if (cantidad == 0) {

            lblNCuentasPagarFiltrosSeleccionados
                    .setText(
                            "No hay cuentas por pagar "
                            + "para los filtros seleccionados"
                    );

        } else if (cantidad == 1) {

            lblNCuentasPagarFiltrosSeleccionados
                    .setText(
                            "1 cuenta por pagar "
                            + "para los filtros seleccionados"
                    );

        } else {

            lblNCuentasPagarFiltrosSeleccionados
                    .setText(
                            cantidad
                            + " cuentas por pagar "
                            + "para los filtros seleccionados"
                    );
        }
    }

    // ============================================================
    // FORMATEADORES
    // ============================================================
    private String formatearFechaHora(
            LocalDateTime fecha) {

        return fecha == null
                ? "—"
                : fecha.format(
                        FORMATO_FECHA_HORA
                );
    }

    private String formatearFecha(
            LocalDate fecha) {

        return fecha == null
                ? "—"
                : fecha.format(
                        FORMATO_FECHA
                );
    }

    private String formatearMonto(
            BigDecimal monto) {

        if (monto == null) {
            return "0.00";
        }

        return monto
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                .toPlainString();
    }

    private String formatearEstado(
            EstadoCuenta estado) {

        if (estado == null) {
            return "—";
        }

        return switch (estado) {

            case PENDIENTE ->
                "Pendiente";

            case PAGADA ->
                "Pagada";

            case VENCIDA ->
                "Vencida";
        };
    }

    private BigDecimal valorSeguro(
            BigDecimal valor) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private String valorTexto(
            String valor) {

        return valor == null
                ? ""
                : valor;
    }

    private String obtenerMensajeError(
            RuntimeException ex) {

        if (ex == null
                || ex.getMessage() == null
                || ex.getMessage().isBlank()) {

            return "Ocurrió un error al procesar la operación.";
        }

        return ex.getMessage();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSuperior = new javax.swing.JPanel();
        lblRegistroPagoProveedor = new javax.swing.JLabel();
        lblAplicarCobroSobreCuentaPagarExistente = new javax.swing.JLabel();
        lblNombreApellidoUsuario = new javax.swing.JLabel();
        lblFechaActual = new javax.swing.JLabel();
        lblHoraActual = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        pnlCuentasPagar = new javax.swing.JPanel();
        lblProveedor = new javax.swing.JLabel();
        cmbProveedores = new javax.swing.JComboBox<>();
        cmbEstado = new javax.swing.JComboBox<>();
        lblEstado = new javax.swing.JLabel();
        lblDesde = new javax.swing.JLabel();
        jdcDesdeFecha = new com.toedter.calendar.JDateChooser();
        lblHasta = new javax.swing.JLabel();
        jdcHastaFecha = new com.toedter.calendar.JDateChooser();
        btnConsultar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        spnlTblDetalleCuentasPagar = new javax.swing.JScrollPane();
        tblDetalleCuentasPagar = new javax.swing.JTable();
        jSeparator1 = new javax.swing.JSeparator();
        lblNCuentasPagarFiltrosSeleccionados = new javax.swing.JLabel();
        spnlAcomodador = new javax.swing.JScrollPane();
        pnlCuentaSeleccionada = new javax.swing.JPanel();
        lblNombreEmpresaYTipoEmpresa = new javax.swing.JLabel();
        txtEstadoCuentaPagar = new javax.swing.JTextField();
        lblRucNroRuc = new javax.swing.JLabel();
        lblCompra = new javax.swing.JLabel();
        lblFechaCompra = new javax.swing.JLabel();
        lblVencimiento = new javax.swing.JLabel();
        lblValorCodigoCompra = new javax.swing.JLabel();
        lblValorFechaCompra = new javax.swing.JLabel();
        lblValorFechaVencimiento = new javax.swing.JLabel();
        pnlSaldosCuentaPagar = new javax.swing.JPanel();
        lblTotal = new javax.swing.JLabel();
        lblPagado = new javax.swing.JLabel();
        lblSaldoPend = new javax.swing.JLabel();
        lblCaracter1erPnlSNro1 = new javax.swing.JLabel();
        lblCaracter1erPnlSNro2 = new javax.swing.JLabel();
        lblCaracter1erPnlSNro3 = new javax.swing.JLabel();
        lblSaldoTotal = new javax.swing.JLabel();
        lblValorSaldoPend = new javax.swing.JLabel();
        lblSaldoCobrado = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        pnlRegistrarPago = new javax.swing.JPanel();
        lblMontoAPagar = new javax.swing.JLabel();
        txtValorMontoAPagar = new javax.swing.JTextField();
        lblMensajeNoPuedeExcederSaldoPendienteValorSaldoPendiente = new javax.swing.JLabel();
        pnlSaldosRegistrarPago = new javax.swing.JPanel();
        lblCaracter2doPnlSNro1 = new javax.swing.JLabel();
        lblCaracter2doPnlSNro2 = new javax.swing.JLabel();
        lblCaracter2doPnlSNro3 = new javax.swing.JLabel();
        lblValorSaldoPendiente = new javax.swing.JLabel();
        lblValorNuevoSaldo = new javax.swing.JLabel();
        lblValorMontoACobrar = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        lblSaldo_Pend = new javax.swing.JLabel();
        lblMontoA_Pagar = new javax.swing.JLabel();
        lblNuevo_Saldo = new javax.swing.JLabel();
        lblPend_Saldo = new javax.swing.JLabel();
        lblPagar_MontoA = new javax.swing.JLabel();
        lblSaldo_Nuevo = new javax.swing.JLabel();
        txtEstadoPago = new javax.swing.JTextField();
        lblMedioPago = new javax.swing.JLabel();
        btnEfectivo = new javax.swing.JButton();
        btnBanco = new javax.swing.JButton();
        pnlDatosCaja = new javax.swing.JPanel();
        lblCajaAbierta = new javax.swing.JLabel();
        lblCajaPrincipal = new javax.swing.JLabel();
        lblSaldoActual = new javax.swing.JLabel();
        lblValorSaldoActual = new javax.swing.JLabel();
        lblFechaPago = new javax.swing.JLabel();
        txtFechaYHoraPago = new javax.swing.JTextField();
        jSeparator6 = new javax.swing.JSeparator();
        btnRegistrarPago = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        pnlSelectorCtaBancaria = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblRegistroPagoProveedor.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblRegistroPagoProveedor.setText("REGISTRO DE PAGO A PROVEEDOR");

        lblAplicarCobroSobreCuentaPagarExistente.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblAplicarCobroSobreCuentaPagarExistente.setText("Aplicar un pago sobre una cuenta por pagar existente ");

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
                    .addComponent(lblAplicarCobroSobreCuentaPagarExistente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblRegistroPagoProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblNombreApellidoUsuario))
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblFechaActual)
                        .addGap(18, 18, 18)
                        .addComponent(lblHoraActual)))
                .addGap(20, 20, 20))
        );
        pnlSuperiorLayout.setVerticalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSuperiorLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRegistroPagoProveedor)
                    .addComponent(lblNombreApellidoUsuario)
                    .addComponent(lblUsuario))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAplicarCobroSobreCuentaPagarExistente, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFechaActual)
                    .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        pnlCuentasPagar.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(41, 43, 45)), "01. CUENTAS POR PAGAR", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblProveedor.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblProveedor.setText("PROVEEDOR");

        lblEstado.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblEstado.setText("ESTADO");

        lblDesde.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblDesde.setText("DESDE");

        lblHasta.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblHasta.setText("HASTA");

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

        javax.swing.GroupLayout pnlCuentasPagarLayout = new javax.swing.GroupLayout(pnlCuentasPagar);
        pnlCuentasPagar.setLayout(pnlCuentasPagarLayout);
        pnlCuentasPagarLayout.setHorizontalGroup(
            pnlCuentasPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCuentasPagarLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(pnlCuentasPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblProveedor)
                    .addComponent(cmbProveedores, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlCuentasPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblEstado)
                    .addComponent(cmbEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlCuentasPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jdcDesdeFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlCuentasPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlCuentasPagarLayout.createSequentialGroup()
                        .addComponent(jdcHastaFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnConsultar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlCuentasPagarLayout.setVerticalGroup(
            pnlCuentasPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCuentasPagarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCuentasPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlCuentasPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnConsultar)
                        .addComponent(btnLimpiar))
                    .addGroup(pnlCuentasPagarLayout.createSequentialGroup()
                        .addGroup(pnlCuentasPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblEstado, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(pnlCuentasPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblProveedor)
                                .addComponent(lblDesde)
                                .addComponent(lblHasta)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlCuentasPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlCuentasPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cmbProveedores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jdcHastaFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jdcDesdeFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        tblDetalleCuentasPagar.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "COMPRA", "PROVEEDOR", "FECHA COMPRA", "VENCIMIENTO", "TOTAL", "PAGADO", "SALDO", "ESTADO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlTblDetalleCuentasPagar.setViewportView(tblDetalleCuentasPagar);

        lblNCuentasPagarFiltrosSeleccionados.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblNCuentasPagarFiltrosSeleccionados.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNCuentasPagarFiltrosSeleccionados.setText("3 cuentas por pagar para los filtros seleccionados ");
        lblNCuentasPagarFiltrosSeleccionados.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        pnlCuentaSeleccionada.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(41, 43, 45)), "02. CUENTA SELECCIONADA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblNombreEmpresaYTipoEmpresa.setText("Corporación Ferretera del Norte S.A.C. ");

        txtEstadoCuentaPagar.setEditable(false);
        txtEstadoCuentaPagar.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        txtEstadoCuentaPagar.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtEstadoCuentaPagar.setText("• Pendiente");

        lblRucNroRuc.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblRucNroRuc.setText("RUC 20456789123 ");

        lblCompra.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCompra.setText("Compra");

        lblFechaCompra.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFechaCompra.setText("Fecha de compra");

        lblVencimiento.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblVencimiento.setText("Vencimiento");

        lblValorCodigoCompra.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblValorCodigoCompra.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblValorCodigoCompra.setText("#341");

        lblValorFechaCompra.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblValorFechaCompra.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblValorFechaCompra.setText("05/08/2026");

        lblValorFechaVencimiento.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblValorFechaVencimiento.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblValorFechaVencimiento.setText("04/09/2026");

        pnlSaldosCuentaPagar.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblTotal.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotal.setText("TOTAL");

        lblPagado.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblPagado.setText("PAGADO");

        lblSaldoPend.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        lblSaldoPend.setForeground(new java.awt.Color(255, 153, 51));
        lblSaldoPend.setText("SALDO PEND.");

        lblCaracter1erPnlSNro1.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblCaracter1erPnlSNro1.setText("S/");

        lblCaracter1erPnlSNro2.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblCaracter1erPnlSNro2.setText("S/");

        lblCaracter1erPnlSNro3.setFont(new java.awt.Font("Consolas", 1, 14)); // NOI18N
        lblCaracter1erPnlSNro3.setForeground(new java.awt.Color(255, 153, 51));
        lblCaracter1erPnlSNro3.setText("S/");

        lblSaldoTotal.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblSaldoTotal.setText("4,250.00");

        lblValorSaldoPend.setFont(new java.awt.Font("Consolas", 1, 12)); // NOI18N
        lblValorSaldoPend.setForeground(new java.awt.Color(255, 153, 51));
        lblValorSaldoPend.setText("2,750.00");

        lblSaldoCobrado.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblSaldoCobrado.setText("1,500.00");

        javax.swing.GroupLayout pnlSaldosCuentaPagarLayout = new javax.swing.GroupLayout(pnlSaldosCuentaPagar);
        pnlSaldosCuentaPagar.setLayout(pnlSaldosCuentaPagarLayout);
        pnlSaldosCuentaPagarLayout.setHorizontalGroup(
            pnlSaldosCuentaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSaldosCuentaPagarLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(pnlSaldosCuentaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSaldosCuentaPagarLayout.createSequentialGroup()
                        .addComponent(lblSaldoTotal)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSaldosCuentaPagarLayout.createSequentialGroup()
                        .addGroup(pnlSaldosCuentaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlSaldosCuentaPagarLayout.createSequentialGroup()
                                .addGroup(pnlSaldosCuentaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(lblCaracter1erPnlSNro1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(pnlSaldosCuentaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnlSaldosCuentaPagarLayout.createSequentialGroup()
                                        .addGap(62, 62, 62)
                                        .addGroup(pnlSaldosCuentaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lblSaldoCobrado)
                                            .addComponent(lblCaracter1erPnlSNro2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(35, 35, 35)
                                        .addGroup(pnlSaldosCuentaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lblCaracter1erPnlSNro3, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lblValorSaldoPend, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(0, 3, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSaldosCuentaPagarLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblPagado)
                                        .addGap(33, 33, 33)
                                        .addComponent(lblSaldoPend)))))
                        .addGap(17, 17, 17))))
        );
        pnlSaldosCuentaPagarLayout.setVerticalGroup(
            pnlSaldosCuentaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSaldosCuentaPagarLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlSaldosCuentaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTotal)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSaldosCuentaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPagado)
                        .addComponent(lblSaldoPend)))
                .addGap(2, 2, 2)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(pnlSaldosCuentaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCaracter1erPnlSNro2)
                    .addComponent(lblCaracter1erPnlSNro1)
                    .addComponent(lblCaracter1erPnlSNro3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSaldosCuentaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSaldoTotal)
                    .addComponent(lblValorSaldoPend)
                    .addComponent(lblSaldoCobrado))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlRegistrarPago.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(41, 43, 45)), "03. REGISTRAR PAGO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblMontoAPagar.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblMontoAPagar.setText("MONTO A PAGAR");

        txtValorMontoAPagar.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        txtValorMontoAPagar.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtValorMontoAPagar.setText("1,200.00");

        lblMensajeNoPuedeExcederSaldoPendienteValorSaldoPendiente.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblMensajeNoPuedeExcederSaldoPendienteValorSaldoPendiente.setText("No puede exceder el saldo pendiente (S/ 2,750.00) ");

        pnlSaldosRegistrarPago.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblCaracter2doPnlSNro1.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblCaracter2doPnlSNro1.setText("S/");

        lblCaracter2doPnlSNro2.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblCaracter2doPnlSNro2.setText("S/");

        lblCaracter2doPnlSNro3.setFont(new java.awt.Font("Consolas", 1, 14)); // NOI18N
        lblCaracter2doPnlSNro3.setForeground(new java.awt.Color(255, 153, 51));
        lblCaracter2doPnlSNro3.setText("S/");

        lblValorSaldoPendiente.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorSaldoPendiente.setText("2,750.00");

        lblValorNuevoSaldo.setFont(new java.awt.Font("Consolas", 1, 12)); // NOI18N
        lblValorNuevoSaldo.setForeground(new java.awt.Color(255, 153, 51));
        lblValorNuevoSaldo.setText("1,550,00");

        lblValorMontoACobrar.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorMontoACobrar.setText("1,200.00");

        lblSaldo_Pend.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblSaldo_Pend.setText("SALDO");

        lblMontoA_Pagar.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblMontoA_Pagar.setText("MONTO A");

        lblNuevo_Saldo.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblNuevo_Saldo.setText("NUEVO");

        lblPend_Saldo.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblPend_Saldo.setText("PEND.");

        lblPagar_MontoA.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblPagar_MontoA.setText("PAGAR");

        lblSaldo_Nuevo.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblSaldo_Nuevo.setText("SALDO");

        javax.swing.GroupLayout pnlSaldosRegistrarPagoLayout = new javax.swing.GroupLayout(pnlSaldosRegistrarPago);
        pnlSaldosRegistrarPago.setLayout(pnlSaldosRegistrarPagoLayout);
        pnlSaldosRegistrarPagoLayout.setHorizontalGroup(
            pnlSaldosRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSaldosRegistrarPagoLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(pnlSaldosRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSaldosRegistrarPagoLayout.createSequentialGroup()
                        .addGroup(pnlSaldosRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator5, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlSaldosRegistrarPagoLayout.createSequentialGroup()
                                .addComponent(lblCaracter2doPnlSNro1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(62, 62, 62)
                                .addGroup(pnlSaldosRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblValorMontoACobrar)
                                    .addComponent(lblCaracter2doPnlSNro2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(35, 35, 35)
                                .addGroup(pnlSaldosRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblCaracter2doPnlSNro3, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblValorNuevoSaldo, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(17, 17, 17))
                    .addGroup(pnlSaldosRegistrarPagoLayout.createSequentialGroup()
                        .addGroup(pnlSaldosRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblValorSaldoPendiente)
                            .addGroup(pnlSaldosRegistrarPagoLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(pnlSaldosRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lblPend_Saldo, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblSaldo_Pend, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(36, 36, 36)
                                .addGroup(pnlSaldosRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblMontoA_Pagar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblPagar_MontoA, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(43, 43, 43)
                                .addGroup(pnlSaldosRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblNuevo_Saldo)
                                    .addComponent(lblSaldo_Nuevo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(28, 28, 28))))
        );
        pnlSaldosRegistrarPagoLayout.setVerticalGroup(
            pnlSaldosRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSaldosRegistrarPagoLayout.createSequentialGroup()
                .addContainerGap(11, Short.MAX_VALUE)
                .addGroup(pnlSaldosRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSaldo_Pend)
                    .addComponent(lblMontoA_Pagar)
                    .addComponent(lblNuevo_Saldo))
                .addGap(4, 4, 4)
                .addGroup(pnlSaldosRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPend_Saldo)
                    .addComponent(lblPagar_MontoA)
                    .addComponent(lblSaldo_Nuevo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(pnlSaldosRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCaracter2doPnlSNro2)
                    .addComponent(lblCaracter2doPnlSNro1)
                    .addComponent(lblCaracter2doPnlSNro3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSaldosRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblValorSaldoPendiente)
                    .addComponent(lblValorNuevoSaldo)
                    .addComponent(lblValorMontoACobrar))
                .addGap(12, 12, 12))
        );

        txtEstadoPago.setEditable(false);
        txtEstadoPago.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        txtEstadoPago.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtEstadoPago.setText("PAGO PARCIAL");

        lblMedioPago.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblMedioPago.setText("MEDIO DE PAGO");

        btnEfectivo.setText("💵 Efectivo");

        btnBanco.setText("🏦 Banco");

        pnlDatosCaja.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblCajaAbierta.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCajaAbierta.setText("CAJA ABIERTA");

        lblCajaPrincipal.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCajaPrincipal.setText("Caja Principal");

        lblSaldoActual.setText("Saldo actual:");

        lblValorSaldoActual.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorSaldoActual.setText("S/ 3,420.00");

        javax.swing.GroupLayout pnlDatosCajaLayout = new javax.swing.GroupLayout(pnlDatosCaja);
        pnlDatosCaja.setLayout(pnlDatosCajaLayout);
        pnlDatosCajaLayout.setHorizontalGroup(
            pnlDatosCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatosCajaLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(pnlDatosCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDatosCajaLayout.createSequentialGroup()
                        .addComponent(lblCajaPrincipal)
                        .addGap(18, 18, 18)
                        .addComponent(lblSaldoActual)
                        .addGap(18, 18, 18)
                        .addComponent(lblValorSaldoActual))
                    .addComponent(lblCajaAbierta))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlDatosCajaLayout.setVerticalGroup(
            pnlDatosCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatosCajaLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblCajaAbierta)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatosCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCajaPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSaldoActual)
                    .addComponent(lblValorSaldoActual))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        lblFechaPago.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFechaPago.setText("FECHA DEL PAGO");

        txtFechaYHoraPago.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        txtFechaYHoraPago.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtFechaYHoraPago.setText("21/08/2026 10:40");

        btnRegistrarPago.setBackground(new java.awt.Color(153, 51, 0));
        btnRegistrarPago.setText("Registrar Pago");
        btnRegistrarPago.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarPagoActionPerformed(evt);
            }
        });

        btnCancelar.setBackground(new java.awt.Color(51, 51, 51));
        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlSelectorCtaBancariaLayout = new javax.swing.GroupLayout(pnlSelectorCtaBancaria);
        pnlSelectorCtaBancaria.setLayout(pnlSelectorCtaBancariaLayout);
        pnlSelectorCtaBancariaLayout.setHorizontalGroup(
            pnlSelectorCtaBancariaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 276, Short.MAX_VALUE)
        );
        pnlSelectorCtaBancariaLayout.setVerticalGroup(
            pnlSelectorCtaBancariaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pnlRegistrarPagoLayout = new javax.swing.GroupLayout(pnlRegistrarPago);
        pnlRegistrarPago.setLayout(pnlRegistrarPagoLayout);
        pnlRegistrarPagoLayout.setHorizontalGroup(
            pnlRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlRegistrarPagoLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtEstadoPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(102, 102, 102))
            .addGroup(pnlRegistrarPagoLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlSelectorCtaBancaria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlRegistrarPagoLayout.createSequentialGroup()
                        .addComponent(btnCancelar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRegistrarPago))
                    .addGroup(pnlRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(pnlRegistrarPagoLayout.createSequentialGroup()
                            .addComponent(lblFechaPago)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(txtFechaYHoraPago))
                        .addComponent(lblMedioPago)
                        .addComponent(lblMensajeNoPuedeExcederSaldoPendienteValorSaldoPendiente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(pnlRegistrarPagoLayout.createSequentialGroup()
                            .addComponent(lblMontoAPagar)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(txtValorMontoAPagar, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(pnlSaldosRegistrarPago, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGroup(pnlRegistrarPagoLayout.createSequentialGroup()
                            .addComponent(btnEfectivo, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnBanco, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(pnlDatosCaja, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSeparator6)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlRegistrarPagoLayout.setVerticalGroup(
            pnlRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRegistrarPagoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtValorMontoAPagar)
                    .addComponent(lblMontoAPagar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMensajeNoPuedeExcederSaldoPendienteValorSaldoPendiente)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlSaldosRegistrarPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtEstadoPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMedioPago)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEfectivo)
                    .addComponent(btnBanco))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDatosCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSelectorCtaBancaria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFechaPago)
                    .addComponent(txtFechaYHoraPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlRegistrarPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRegistrarPago)
                    .addComponent(btnCancelar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlCuentaSeleccionadaLayout = new javax.swing.GroupLayout(pnlCuentaSeleccionada);
        pnlCuentaSeleccionada.setLayout(pnlCuentaSeleccionadaLayout);
        pnlCuentaSeleccionadaLayout.setHorizontalGroup(
            pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCuentaSeleccionadaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlCuentaSeleccionadaLayout.createSequentialGroup()
                        .addGap(0, 18, Short.MAX_VALUE)
                        .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(pnlCuentaSeleccionadaLayout.createSequentialGroup()
                                    .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(lblCompra, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblVencimiento, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblFechaCompra, javax.swing.GroupLayout.Alignment.LEADING))
                                    .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(pnlCuentaSeleccionadaLayout.createSequentialGroup()
                                            .addGap(12, 12, 12)
                                            .addComponent(lblValorFechaCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(pnlCuentaSeleccionadaLayout.createSequentialGroup()
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(lblValorCodigoCompra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addGap(12, 12, 12))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlCuentaSeleccionadaLayout.createSequentialGroup()
                                    .addComponent(lblNombreEmpresaYTipoEmpresa)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(txtEstadoCuentaPagar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(14, 14, 14)))
                            .addGroup(pnlCuentaSeleccionadaLayout.createSequentialGroup()
                                .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lblValorFechaVencimiento, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(lblRucNroRuc)
                                        .addComponent(pnlSaldosCuentaPagar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())))
                    .addComponent(pnlRegistrarPago, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        pnlCuentaSeleccionadaLayout.setVerticalGroup(
            pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCuentaSeleccionadaLayout.createSequentialGroup()
                .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlCuentaSeleccionadaLayout.createSequentialGroup()
                        .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtEstadoCuentaPagar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblNombreEmpresaYTipoEmpresa))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblRucNroRuc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblCompra)
                            .addComponent(lblValorCodigoCompra))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblFechaCompra))
                    .addComponent(lblValorFechaCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVencimiento)
                    .addComponent(lblValorFechaVencimiento, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSaldosCuentaPagar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlRegistrarPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        spnlAcomodador.setViewportView(pnlCuentaSeleccionada);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlSuperior, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jSeparator1)
                                .addComponent(pnlCuentasPagar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(spnlTblDetalleCuentasPagar, javax.swing.GroupLayout.Alignment.LEADING))
                            .addComponent(lblNCuentasPagarFiltrosSeleccionados, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(spnlAcomodador, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(pnlCuentasPagar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnlTblDetalleCuentasPagar, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblNCuentasPagarFiltrosSeleccionados, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnlAcomodador, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        // TODO add your handling code here:
        consultarCuentasPorPagar();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
        limpiarFiltros();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnRegistrarPagoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarPagoActionPerformed
        // TODO add your handling code here:
        registrarPago();
    }//GEN-LAST:event_btnRegistrarPagoActionPerformed

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
            java.util.logging.Logger.getLogger(FrmPagoProveedor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmPagoProveedor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmPagoProveedor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmPagoProveedor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmPagoProveedor dialog = new FrmPagoProveedor(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnBanco;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnEfectivo;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnRegistrarPago;
    private javax.swing.JComboBox<String> cmbEstado;
    private javax.swing.JComboBox<Proveedor> cmbProveedores;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private com.toedter.calendar.JDateChooser jdcDesdeFecha;
    private com.toedter.calendar.JDateChooser jdcHastaFecha;
    private javax.swing.JLabel lblAplicarCobroSobreCuentaPagarExistente;
    private javax.swing.JLabel lblCajaAbierta;
    private javax.swing.JLabel lblCajaPrincipal;
    private javax.swing.JLabel lblCaracter1erPnlSNro1;
    private javax.swing.JLabel lblCaracter1erPnlSNro2;
    private javax.swing.JLabel lblCaracter1erPnlSNro3;
    private javax.swing.JLabel lblCaracter2doPnlSNro1;
    private javax.swing.JLabel lblCaracter2doPnlSNro2;
    private javax.swing.JLabel lblCaracter2doPnlSNro3;
    private javax.swing.JLabel lblCompra;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblFechaActual;
    private javax.swing.JLabel lblFechaCompra;
    private javax.swing.JLabel lblFechaPago;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblHoraActual;
    private javax.swing.JLabel lblMedioPago;
    private javax.swing.JLabel lblMensajeNoPuedeExcederSaldoPendienteValorSaldoPendiente;
    private javax.swing.JLabel lblMontoAPagar;
    private javax.swing.JLabel lblMontoA_Pagar;
    private javax.swing.JLabel lblNCuentasPagarFiltrosSeleccionados;
    private javax.swing.JLabel lblNombreApellidoUsuario;
    private javax.swing.JLabel lblNombreEmpresaYTipoEmpresa;
    private javax.swing.JLabel lblNuevo_Saldo;
    private javax.swing.JLabel lblPagado;
    private javax.swing.JLabel lblPagar_MontoA;
    private javax.swing.JLabel lblPend_Saldo;
    private javax.swing.JLabel lblProveedor;
    private javax.swing.JLabel lblRegistroPagoProveedor;
    private javax.swing.JLabel lblRucNroRuc;
    private javax.swing.JLabel lblSaldoActual;
    private javax.swing.JLabel lblSaldoCobrado;
    private javax.swing.JLabel lblSaldoPend;
    private javax.swing.JLabel lblSaldoTotal;
    private javax.swing.JLabel lblSaldo_Nuevo;
    private javax.swing.JLabel lblSaldo_Pend;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblValorCodigoCompra;
    private javax.swing.JLabel lblValorFechaCompra;
    private javax.swing.JLabel lblValorFechaVencimiento;
    private javax.swing.JLabel lblValorMontoACobrar;
    private javax.swing.JLabel lblValorNuevoSaldo;
    private javax.swing.JLabel lblValorSaldoActual;
    private javax.swing.JLabel lblValorSaldoPend;
    private javax.swing.JLabel lblValorSaldoPendiente;
    private javax.swing.JLabel lblVencimiento;
    private javax.swing.JPanel pnlCuentaSeleccionada;
    private javax.swing.JPanel pnlCuentasPagar;
    private javax.swing.JPanel pnlDatosCaja;
    private javax.swing.JPanel pnlRegistrarPago;
    private javax.swing.JPanel pnlSaldosCuentaPagar;
    private javax.swing.JPanel pnlSaldosRegistrarPago;
    private javax.swing.JPanel pnlSelectorCtaBancaria;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JScrollPane spnlAcomodador;
    private javax.swing.JScrollPane spnlTblDetalleCuentasPagar;
    private javax.swing.JTable tblDetalleCuentasPagar;
    private javax.swing.JTextField txtEstadoCuentaPagar;
    private javax.swing.JTextField txtEstadoPago;
    private javax.swing.JTextField txtFechaYHoraPago;
    private javax.swing.JTextField txtValorMontoAPagar;
    // End of variables declaration//GEN-END:variables
}
