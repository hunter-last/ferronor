package com.ferronor.sic.ventas.vista;

import com.ferronor.sic.maestros.logica.ClienteService;
import com.ferronor.sic.maestros.modelo.Cliente;

import com.ferronor.sic.procesos.ProcesoCobroCliente;

import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.SesionUsuario;
import com.ferronor.sic.shared.ServiceFactory;

import com.ferronor.sic.tesoreria.logica.TesoreriaService;
import com.ferronor.sic.tesoreria.modelo.Caja;
import com.ferronor.sic.tesoreria.modelo.CuentaBancaria;

import com.ferronor.sic.ventas.logica.VentaService;
import com.ferronor.sic.ventas.modelo.CobroCliente;
import com.ferronor.sic.ventas.modelo.EstadoCuenta;
import com.ferronor.sic.ventas.modelo.dto.CuentaCobrarConsulta;

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
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class FrmCobroCliente extends javax.swing.JDialog {

    // ============================================================
    // SERVICES
    // ============================================================
    private final ClienteService clienteService
            = ServiceFactory.clienteService();

    private final VentaService ventaService
            = ServiceFactory.ventaService();

    private final TesoreriaService tesoreriaService
            = ServiceFactory.tesoreriaService();

    private final ProcesoCobroCliente procesoCobroCliente
            = ServiceFactory.procesoCobroCliente();

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
    private List<CuentaCobrarConsulta> cuentasConsultadas
            = new ArrayList<>();

    private CuentaCobrarConsulta cuentaSeleccionada;

    private Caja cajaAbierta;

    private List<CuentaBancaria> cuentasBancariasActivas
            = new ArrayList<>();

    private CuentaBancaria cuentaBancariaSeleccionada;

    private DefaultTableModel modeloTablaCuentas;

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
    public FrmCobroCliente(java.awt.Frame parent, boolean modal) {
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
                    "VENTA",
                    "CLIENTE",
                    "FECHA VENTA",
                    "VENCIMIENTO",
                    "TOTAL",
                    "COBRADO",
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

        tblDetalleCuentasCobrar.setModel(
                modeloTablaCuentas
        );

        tblDetalleCuentasCobrar.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblDetalleCuentasCobrar.setRowHeight(27);

        tblDetalleCuentasCobrar.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        9
                )
        );

        tblDetalleCuentasCobrar
                .getTableHeader()
                .setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                9
                        )
                );

        tblDetalleCuentasCobrar
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

        // --------------------------------------------------------
        // ESTADO
        // --------------------------------------------------------
        cmbEstado.removeAllItems();

        cmbEstado.addItem("Todos");
        cmbEstado.addItem("Pendiente");
        cmbEstado.addItem("Pagada");
        cmbEstado.addItem("Vencida");

        cmbEstado.setSelectedItem("Todos");

        // --------------------------------------------------------
        // CLIENTES
        // --------------------------------------------------------
        cmbClientes.removeAllItems();

        cmbClientes.setRenderer(
                new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list,
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

                if (value instanceof Cliente cliente) {

                    setText(
                            cliente.getNombreRazonSocial()
                    );
                }

                return this;
            }
        });

        cargarClientes();
    }

    private void cargarClientes() {

        try {

            List<Cliente> clientes
                    = clienteService.listarActivos();

            cmbClientes.removeAllItems();

            for (Cliente cliente : clientes) {
                cmbClientes.addItem(cliente);
            }

            cmbClientes.setSelectedItem(null);

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al cargar clientes",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // ESTADO INICIAL
    // ============================================================
    private void configurarEstadoInicial() {

        cuentaSeleccionada = null;

        cajaAbierta = null;

        cuentaBancariaSeleccionada = null;

        cmbClientes.setSelectedItem(null);

        cmbEstado.setSelectedItem("Todos");

        jdcDesdeFecha.setDate(null);

        jdcHastaFecha.setDate(null);

        txtFechaYHoraCobro.setEditable(false);

        txtFechaYHoraCobro.setText(
                LocalDateTime.now()
                        .format(FORMATO_FECHA_HORA)
        );

        txtEstadoCuentaCobrar.setEditable(false);

        txtEstadoCobro.setEditable(false);

        txtValorMontoACobrar.setText("");

        /*
     * Sin cuenta seleccionada:
     * ningún medio de cobro debe estar visible.
         */
        pnlDatosCaja.setVisible(false);

        pnlSelectorCtaBancaria.setVisible(false);

        btnEfectivo.setEnabled(false);

        btnBanco.setEnabled(false);

        btnRegistrarCobro.setEnabled(false);

        limpiarCuentaSeleccionada();

        actualizarDatosUsuario();

        lblNCuentasCobrarFiltrosSeleccionados.setText(
                "Seleccione los filtros y consulte las cuentas por cobrar"
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

        // --------------------------------------------------------
        // MEDIOS DE COBRO
        // --------------------------------------------------------
        btnEfectivo.addActionListener(
                e -> mostrarMedioEfectivo()
        );

        btnBanco.addActionListener(
                e -> mostrarMedioBanco()
        );

        // --------------------------------------------------------
        // MONTO
        // --------------------------------------------------------
        txtValorMontoACobrar
                .getDocument()
                .addDocumentListener(
                        new DocumentListener() {

                    @Override
                    public void insertUpdate(
                            DocumentEvent e) {

                        actualizarCalculoCobro();
                    }

                    @Override
                    public void removeUpdate(
                            DocumentEvent e) {

                        actualizarCalculoCobro();
                    }

                    @Override
                    public void changedUpdate(
                            DocumentEvent e) {

                        actualizarCalculoCobro();
                    }
                });
    }

    // ============================================================
    // CARGA INICIAL DE DATOS
    // ============================================================
    private void cargarDatosIniciales() {

        cargarCajaAbierta();

        cargarCuentasBancariasActivas();

        consultarCuentas();
    }

    // ============================================================
    // CAJA ABIERTA
    // ============================================================
    private void cargarCajaAbierta() {

        try {

            Optional<Caja> resultado
                    = tesoreriaService.obtenerCajaAbierta();

            if (resultado.isPresent()) {

                cajaAbierta = resultado.get();

                lblCajaAbierta.setText(
                        "● CAJA ABIERTA"
                );

                lblCajaPrincipal.setText(
                        cajaAbierta.getNombre()
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
    }

    // ============================================================
    // PANEL DINÁMICO DE CUENTA BANCARIA
    // ============================================================
    private void configurarSelectorBanco() {

        cmbCuenta = new javax.swing.JComboBox<>();

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
         * El .form ya reservó este panel.
         * Sustituimos solamente su contenido generado vacío.
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

        /*
         * Cómo se mostrará cada CuentaBancaria
         * dentro del combo.
         */
        cmbCuenta.setRenderer(
                new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list,
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
                    = (CuentaBancaria) cmbCuenta
                            .getSelectedItem();

                    cuentaBancariaSeleccionada
                    = cuenta;

                    actualizarDatosCuentaBancaria();

                    actualizarEstadoBotonRegistrar();
                }
        );

        /*
         * Regla explícita del formulario:
         * BANCO permanece oculto hasta pulsar Banco.
         */
        pnlSelectorCtaBancaria.setVisible(false);

        pnlSelectorCtaBancaria.revalidate();
        pnlSelectorCtaBancaria.repaint();
    }

    private void cargarCuentasBancariasActivas() {

        try {

            List<CuentaBancaria> cuentas
                    = tesoreriaService
                            .listarCuentasBancariasActivas();

            if (cuentas == null) {
                cuentas = Collections.emptyList();
            }

            cuentasBancariasActivas
                    = new ArrayList<>(cuentas);

            cuentasBancariasActivas.sort(
                    Comparator.comparing(
                            cuenta -> {

                                String alias
                                = cuenta.getAlias();

                                if (alias == null
                                || alias.isBlank()) {

                                    alias = cuenta.getBanco();
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

            cuentaBancariaSeleccionada
                    = null;

            actualizandoInterfaz = true;

            cmbCuenta.setModel(
                    new DefaultComboBoxModel<>()
            );

            cmbCuenta.setSelectedItem(null);

            actualizandoInterfaz = false;

            actualizarDatosCuentaBancaria();
        }
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

            alias = cuentaBancariaSeleccionada
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
    // CONSULTA DE CUENTAS POR COBRAR
    // ============================================================
    private void consultarCuentas() {

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

            Integer idCliente
                    = obtenerIdClienteSeleccionado();

            List<CuentaCobrarConsulta> resultados
                    = ventaService
                            .consultarCuentasPorCobrar(
                                    estado,
                                    idCliente,
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
                    "Error al consultar cuentas por cobrar",
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

    private Integer obtenerIdClienteSeleccionado() {

        Object seleccionado
                = cmbClientes.getSelectedItem();

        if (seleccionado instanceof Cliente cliente) {

            return cliente.getIdCliente();
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
            List<CuentaCobrarConsulta> cuentas) {

        modeloTablaCuentas.setRowCount(0);

        for (CuentaCobrarConsulta cuenta
                : cuentas) {

            modeloTablaCuentas.addRow(
                    new Object[]{
                        "#" + cuenta.getIdVenta(),
                        valorTexto(
                                cuenta
                                        .getNombreRazonSocialCliente()
                        ),
                        formatearFechaHora(
                                cuenta.getFechaVenta()
                        ),
                        formatearFecha(
                                cuenta
                                        .getFechaVencimiento()
                        ),
                        "S/ "
                        + formatearMonto(
                                cuenta.getMontoTotal()
                        ),
                        "S/ "
                        + formatearMonto(
                                cuenta.getMontoCobrado()
                        ),
                        "S/ "
                        + formatearMonto(
                                cuenta
                                        .getSaldoPendiente()
                        ),
                        formatearEstado(
                                cuenta.getEstado()
                        )
                    }
            );
        }
    }

    private void seleccionarCuentaDesdeTabla() {

        int filaVista
                = tblDetalleCuentasCobrar
                        .getSelectedRow();

        if (filaVista < 0) {

            if (cuentaSeleccionada != null) {
                limpiarCuentaSeleccionada();
            }

            return;
        }

        int filaModelo
                = tblDetalleCuentasCobrar
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
                                .getNombreRazonSocialCliente()
                )
        );

        String documento
                = valorTexto(
                        cuentaSeleccionada
                                .getTipoDocumentoCliente()
                )
                + " "
                + valorTexto(
                        cuentaSeleccionada
                                .getNumeroDocumentoCliente()
                );

        lblRucNroRuc.setText(
                documento.trim()
        );

        lblValorCodigoVenta.setText(
                "#" + cuentaSeleccionada.getIdVenta()
        );

        lblValorFechaVenta.setText(
                formatearFechaHora(
                        cuentaSeleccionada
                                .getFechaVenta()
                )
        );

        lblValorFechaVencimiento.setText(
                formatearFecha(
                        cuentaSeleccionada
                                .getFechaVencimiento()
                )
        );

        txtEstadoCuentaCobrar.setText(
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
                                .getMontoCobrado()
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

        txtValorMontoACobrar.setText("");

        boolean tieneSaldo
                = cuentaSeleccionada
                        .getSaldoPendiente() != null
                && cuentaSeleccionada
                        .getSaldoPendiente()
                        .compareTo(
                                BigDecimal.ZERO
                        ) > 0;

        habilitarComponentesCobro(
                tieneSaldo
        );

        actualizarCalculoCobro();
    }

    private void limpiarCuentaSeleccionada() {

        cuentaSeleccionada = null;

        if (tblDetalleCuentasCobrar
                .getSelectedRow() >= 0) {

            tblDetalleCuentasCobrar
                    .clearSelection();
        }

        lblNombreEmpresaYTipoEmpresa.setText(
                "Sin cuenta seleccionada"
        );

        lblRucNroRuc.setText("");

        lblValorCodigoVenta.setText(
                "—"
        );

        lblValorFechaVenta.setText(
                "—"
        );

        lblValorFechaVencimiento.setText(
                "—"
        );

        txtEstadoCuentaCobrar.setText(
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
                        "Seleccione una cuenta por cobrar"
                );

        txtValorMontoACobrar.setText("");

        txtEstadoCobro.setText(
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

        btnRegistrarCobro.setEnabled(false);

        btnEfectivo.setEnabled(false);

        btnBanco.setEnabled(false);

        /*
     * Sin cuenta seleccionada:
     * ocultamos ambos medios.
         */
        pnlDatosCaja.setVisible(false);

        pnlSelectorCtaBancaria.setVisible(false);

        cuentaBancariaSeleccionada = null;

        actualizarLayoutMedioCobro();
    }

    // ============================================================
    // HABILITACIÓN DEL REGISTRO
    // ============================================================
    private void habilitarComponentesCobro(
            boolean habilitar) {

        txtValorMontoACobrar.setEnabled(
                habilitar
        );

        txtFechaYHoraCobro.setEnabled(
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

        btnRegistrarCobro.setEnabled(false);

        if (!habilitar) {

            pnlDatosCaja.setVisible(false);

            pnlSelectorCtaBancaria.setVisible(false);

            txtValorMontoACobrar.setText("");

            txtEstadoCobro.setText(
                    "SIN CUENTA"
            );

            cuentaBancariaSeleccionada = null;

            actualizarLayoutMedioCobro();

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

        pnlDatosCaja.setVisible(true);

        pnlSelectorCtaBancaria.setVisible(false);

        boolean cajaDisponible
                = cajaAbierta != null;

        boolean bancoDisponible
                = !cuentasBancariasActivas.isEmpty();

        btnEfectivo.setEnabled(
                cajaDisponible
        );

        btnBanco.setEnabled(
                bancoDisponible
        );

        actualizarEstadoBotonRegistrar();

        actualizarLayoutMedioCobro();
    }

    // ============================================================
    // MEDIO BANCO
    // ============================================================
    private void mostrarMedioBanco() {

        if (cuentaSeleccionada == null) {
            return;
        }

        boolean bancoDisponible
                = !cuentasBancariasActivas.isEmpty();

        if (!bancoDisponible) {

            JOptionPane.showMessageDialog(
                    this,
                    "No hay cuentas bancarias activas disponibles.",
                    "Cuenta bancaria no disponible",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

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

        actualizarLayoutMedioCobro();
    }

    private void actualizarLayoutMedioCobro() {

        pnlDatosCaja.revalidate();
        pnlDatosCaja.repaint();

        pnlSelectorCtaBancaria.revalidate();
        pnlSelectorCtaBancaria.repaint();

        pnlRegistrarCobro.revalidate();
        pnlRegistrarCobro.repaint();

        pnlCuentaSeleccionada.revalidate();
        pnlCuentaSeleccionada.repaint();

        spnlAcomodador.revalidate();
        spnlAcomodador.repaint();

        getContentPane().revalidate();
        getContentPane().repaint();
    }

    // ============================================================
    // CÁLCULO DEL COBRO
    // ============================================================
    private void actualizarCalculoCobro() {

        if (cuentaSeleccionada == null) {

            txtEstadoCobro.setText(
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

            txtEstadoCobro.setText(
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

            txtEstadoCobro.setText(
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

            txtEstadoCobro.setText(
                    "COBRO TOTAL"
            );

        } else {

            txtEstadoCobro.setText(
                    "COBRO PARCIAL"
            );
        }

        actualizarEstadoBotonRegistrar();
    }

    private BigDecimal obtenerMontoIngresado() {

        String texto
                = txtValorMontoACobrar
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
    private boolean validarRegistroCobro() {

        if (cuentaSeleccionada == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Debe seleccionar una cuenta por cobrar.",
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

            txtValorMontoACobrar.requestFocus();

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

            txtValorMontoACobrar.requestFocus();

            return false;
        }

        if (monto.compareTo(saldo) > 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "El monto a cobrar no puede exceder "
                    + "el saldo pendiente de S/ "
                    + formatearMonto(saldo)
                    + ".",
                    "Monto excedido",
                    JOptionPane.WARNING_MESSAGE
            );

            txtValorMontoACobrar.requestFocus();

            return false;
        }

        // --------------------------------------------------------
        // EFECTIVO
        // --------------------------------------------------------
        if (pnlDatosCaja.isVisible()) {

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
        if (pnlSelectorCtaBancaria.isVisible()) {

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

        JOptionPane.showMessageDialog(
                this,
                "Seleccione un medio de cobro.",
                "Medio de cobro",
                JOptionPane.WARNING_MESSAGE
        );

        return false;
    }

    // ============================================================
    // ESTADO DEL BOTÓN REGISTRAR
    // ============================================================
    private void actualizarEstadoBotonRegistrar() {

        if (cuentaSeleccionada == null) {

            btnRegistrarCobro.setEnabled(false);

            return;
        }

        BigDecimal monto
                = obtenerMontoIngresado();

        if (monto == null) {

            btnRegistrarCobro.setEnabled(false);

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

            btnRegistrarCobro.setEnabled(false);

            return;
        }

        // --------------------------------------------------------
        // EFECTIVO
        // --------------------------------------------------------
        if (pnlDatosCaja.isVisible()) {

            btnRegistrarCobro.setEnabled(
                    cajaAbierta != null
            );

            return;
        }

        // --------------------------------------------------------
        // BANCO
        // --------------------------------------------------------
        if (pnlSelectorCtaBancaria.isVisible()) {

            btnRegistrarCobro.setEnabled(
                    cuentaBancariaSeleccionada != null
            );

            return;
        }

        btnRegistrarCobro.setEnabled(false);
    }

    // ============================================================
    // REGISTRAR COBRO
    // ============================================================
    private void registrarCobro() {

        if (!validarRegistroCobro()) {
            return;
        }

        BigDecimal monto
                = obtenerMontoIngresado();

        LocalDateTime fechaHora
                = LocalDateTime.now();

        CobroCliente cobro
                = new CobroCliente(
                        cuentaSeleccionada.getIdVenta(),
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
            if (pnlDatosCaja.isVisible()) {

                respuesta
                        = procesoCobroCliente
                                .registrarCobroCaja(
                                        cobro,
                                        cajaAbierta
                                                .getIdCaja(),
                                        idUsuario
                                );

            } else {

                // ------------------------------------------------
                // BANCO
                // ------------------------------------------------
                respuesta
                        = procesoCobroCliente
                                .registrarCobroBanco(
                                        cobro,
                                        cuentaBancariaSeleccionada
                                                .getIdCuentaBancaria(),
                                        idUsuario
                                );
            }

            if (!respuesta.isExito()) {

                JOptionPane.showMessageDialog(
                        this,
                        respuesta.getMensaje(),
                        "No se pudo registrar el cobro",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "El cobro se registró correctamente.",
                    "Cobro registrado",
                    JOptionPane.INFORMATION_MESSAGE
            );

            limpiarDespuesDeCobro();

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al registrar cobro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // LIMPIAR DESPUÉS DE COBRO
    // ============================================================
    private void limpiarDespuesDeCobro() {

        cuentaSeleccionada = null;

        cuentaBancariaSeleccionada = null;

        txtValorMontoACobrar.setText("");

        txtEstadoCobro.setText(
                "SIN CUENTA"
        );

        tblDetalleCuentasCobrar.clearSelection();

        limpiarCuentaSeleccionada();

        cargarCajaAbierta();

        cargarCuentasBancariasActivas();

        txtFechaYHoraCobro.setText(
                LocalDateTime.now()
                        .format(FORMATO_FECHA_HORA)
        );

        consultarCuentas();
    }

    // ============================================================
    // LIMPIAR FILTROS
    // ============================================================
    private void limpiarFiltros() {

        cmbClientes.setSelectedItem(null);

        cmbEstado.setSelectedItem(
                "Todos"
        );

        jdcDesdeFecha.setDate(null);

        jdcHastaFecha.setDate(null);

        consultarCuentas();
    }

    // ============================================================
    // CONTADOR
    // ============================================================
    private void actualizarContadorResultados() {

        int cantidad
                = cuentasConsultadas.size();

        if (cantidad == 0) {

            lblNCuentasCobrarFiltrosSeleccionados
                    .setText(
                            "No hay cuentas por cobrar "
                            + "para los filtros seleccionados"
                    );

        } else if (cantidad == 1) {

            lblNCuentasCobrarFiltrosSeleccionados
                    .setText(
                            "1 cuenta por cobrar "
                            + "para los filtros seleccionados"
                    );

        } else {

            lblNCuentasCobrarFiltrosSeleccionados
                    .setText(
                            cantidad
                            + " cuentas por cobrar "
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
        lblRegistroCobroCliente = new javax.swing.JLabel();
        lblAplicarCobroSobreCuentaCobrarExistente = new javax.swing.JLabel();
        lblNombreApellidoUsuario = new javax.swing.JLabel();
        lblFechaActual = new javax.swing.JLabel();
        lblHoraActual = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        pnlCuentasCobrar = new javax.swing.JPanel();
        lblCliente = new javax.swing.JLabel();
        cmbClientes = new javax.swing.JComboBox<>();
        cmbEstado = new javax.swing.JComboBox<>();
        lblEstado = new javax.swing.JLabel();
        lblDesde = new javax.swing.JLabel();
        jdcDesdeFecha = new com.toedter.calendar.JDateChooser();
        lblHasta = new javax.swing.JLabel();
        jdcHastaFecha = new com.toedter.calendar.JDateChooser();
        btnConsultar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        spnlTblDetalleCuentasCobrar = new javax.swing.JScrollPane();
        tblDetalleCuentasCobrar = new javax.swing.JTable();
        jSeparator1 = new javax.swing.JSeparator();
        lblNCuentasCobrarFiltrosSeleccionados = new javax.swing.JLabel();
        spnlAcomodador = new javax.swing.JScrollPane();
        pnlCuentaSeleccionada = new javax.swing.JPanel();
        lblNombreEmpresaYTipoEmpresa = new javax.swing.JLabel();
        txtEstadoCuentaCobrar = new javax.swing.JTextField();
        lblRucNroRuc = new javax.swing.JLabel();
        lblVenta = new javax.swing.JLabel();
        lblFechaVenta = new javax.swing.JLabel();
        lblVencimiento = new javax.swing.JLabel();
        lblValorCodigoVenta = new javax.swing.JLabel();
        lblValorFechaVenta = new javax.swing.JLabel();
        lblValorFechaVencimiento = new javax.swing.JLabel();
        pnlSaldosCuentaCobrar = new javax.swing.JPanel();
        lblTotal = new javax.swing.JLabel();
        lblCobrado = new javax.swing.JLabel();
        lblSaldoPend = new javax.swing.JLabel();
        lblCaracter1erPnlSNro1 = new javax.swing.JLabel();
        lblCaracter1erPnlSNro2 = new javax.swing.JLabel();
        lblCaracter1erPnlSNro3 = new javax.swing.JLabel();
        lblSaldoTotal = new javax.swing.JLabel();
        lblValorSaldoPend = new javax.swing.JLabel();
        lblSaldoCobrado = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        pnlRegistrarCobro = new javax.swing.JPanel();
        lblMontoACobrar = new javax.swing.JLabel();
        txtValorMontoACobrar = new javax.swing.JTextField();
        lblMensajeNoPuedeExcederSaldoPendienteValorSaldoPendiente = new javax.swing.JLabel();
        pnlSaldosRegistrarCobro = new javax.swing.JPanel();
        lblCaracter2doPnlSNro1 = new javax.swing.JLabel();
        lblCaracter2doPnlSNro2 = new javax.swing.JLabel();
        lblCaracter2doPnlSNro3 = new javax.swing.JLabel();
        lblValorSaldoPendiente = new javax.swing.JLabel();
        lblValorNuevoSaldo = new javax.swing.JLabel();
        lblValorMontoACobrar = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        lblSaldo_Pend = new javax.swing.JLabel();
        lblMontoA_Cobrar = new javax.swing.JLabel();
        lblNuevo_Saldo = new javax.swing.JLabel();
        lblPend_Saldo = new javax.swing.JLabel();
        lblCobrar_MontoA = new javax.swing.JLabel();
        lblSaldo_Nuevo = new javax.swing.JLabel();
        txtEstadoCobro = new javax.swing.JTextField();
        lblMedioCobro = new javax.swing.JLabel();
        btnEfectivo = new javax.swing.JButton();
        btnBanco = new javax.swing.JButton();
        pnlDatosCaja = new javax.swing.JPanel();
        lblCajaAbierta = new javax.swing.JLabel();
        lblCajaPrincipal = new javax.swing.JLabel();
        lblSaldoActual = new javax.swing.JLabel();
        lblValorSaldoActual = new javax.swing.JLabel();
        lblFechaCobro = new javax.swing.JLabel();
        txtFechaYHoraCobro = new javax.swing.JTextField();
        jSeparator6 = new javax.swing.JSeparator();
        btnRegistrarCobro = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        pnlSelectorCtaBancaria = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblRegistroCobroCliente.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblRegistroCobroCliente.setText("REGISTRO DE COBRO A CLIENTE");

        lblAplicarCobroSobreCuentaCobrarExistente.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblAplicarCobroSobreCuentaCobrarExistente.setText("Aplicar un cobro sobre una cuenta por cobrar existente ");

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
                    .addComponent(lblAplicarCobroSobreCuentaCobrarExistente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblRegistroCobroCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(lblRegistroCobroCliente)
                    .addComponent(lblNombreApellidoUsuario)
                    .addComponent(lblUsuario))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAplicarCobroSobreCuentaCobrarExistente, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFechaActual)
                    .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        pnlCuentasCobrar.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. CUENTAS POR COBRAR", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblCliente.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCliente.setText("CLIENTE");

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

        javax.swing.GroupLayout pnlCuentasCobrarLayout = new javax.swing.GroupLayout(pnlCuentasCobrar);
        pnlCuentasCobrar.setLayout(pnlCuentasCobrarLayout);
        pnlCuentasCobrarLayout.setHorizontalGroup(
            pnlCuentasCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCuentasCobrarLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(pnlCuentasCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCliente)
                    .addComponent(cmbClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addGroup(pnlCuentasCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCuentasCobrarLayout.createSequentialGroup()
                        .addComponent(cmbEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jdcDesdeFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                        .addComponent(jdcHastaFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlCuentasCobrarLayout.createSequentialGroup()
                        .addComponent(lblEstado)
                        .addGap(87, 87, 87)
                        .addComponent(lblDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(91, 91, 91)
                        .addComponent(lblHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlCuentasCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnConsultar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnLimpiar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(10, 10, 10))
        );
        pnlCuentasCobrarLayout.setVerticalGroup(
            pnlCuentasCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCuentasCobrarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCuentasCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCuentasCobrarLayout.createSequentialGroup()
                        .addComponent(btnConsultar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLimpiar))
                    .addGroup(pnlCuentasCobrarLayout.createSequentialGroup()
                        .addGroup(pnlCuentasCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblCliente)
                            .addComponent(lblDesde)
                            .addComponent(lblEstado)
                            .addComponent(lblHasta))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlCuentasCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlCuentasCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cmbClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jdcDesdeFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jdcHastaFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tblDetalleCuentasCobrar.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "VENTA", "CLIENTE", "FECHA VENTA", "VENCIMIENTO", "TOTAL", "COBRADO", "SALDO", "ESTADO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlTblDetalleCuentasCobrar.setViewportView(tblDetalleCuentasCobrar);

        lblNCuentasCobrarFiltrosSeleccionados.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblNCuentasCobrarFiltrosSeleccionados.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNCuentasCobrarFiltrosSeleccionados.setText("3 cuentas por cobrar para los filtros seleccionados ");
        lblNCuentasCobrarFiltrosSeleccionados.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        pnlCuentaSeleccionada.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. CUENTA SELECCIONADA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblNombreEmpresaYTipoEmpresa.setText("Corporación Ferretera del Norte S.A.C. ");

        txtEstadoCuentaCobrar.setEditable(false);
        txtEstadoCuentaCobrar.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        txtEstadoCuentaCobrar.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtEstadoCuentaCobrar.setText("• Pendiente");

        lblRucNroRuc.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblRucNroRuc.setText("RUC 20456789123 ");

        lblVenta.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblVenta.setText("Venta");

        lblFechaVenta.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFechaVenta.setText("Fecha de venta");

        lblVencimiento.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblVencimiento.setText("Vencimiento");

        lblValorCodigoVenta.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblValorCodigoVenta.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblValorCodigoVenta.setText("#341");

        lblValorFechaVenta.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblValorFechaVenta.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblValorFechaVenta.setText("05/08/2026");

        lblValorFechaVencimiento.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblValorFechaVencimiento.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblValorFechaVencimiento.setText("04/09/2026");

        pnlSaldosCuentaCobrar.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblTotal.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotal.setText("TOTAL");

        lblCobrado.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCobrado.setText("COBRADO");

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

        javax.swing.GroupLayout pnlSaldosCuentaCobrarLayout = new javax.swing.GroupLayout(pnlSaldosCuentaCobrar);
        pnlSaldosCuentaCobrar.setLayout(pnlSaldosCuentaCobrarLayout);
        pnlSaldosCuentaCobrarLayout.setHorizontalGroup(
            pnlSaldosCuentaCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSaldosCuentaCobrarLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(pnlSaldosCuentaCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSaldosCuentaCobrarLayout.createSequentialGroup()
                        .addComponent(lblSaldoTotal)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSaldosCuentaCobrarLayout.createSequentialGroup()
                        .addGroup(pnlSaldosCuentaCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlSaldosCuentaCobrarLayout.createSequentialGroup()
                                .addGroup(pnlSaldosCuentaCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(lblCaracter1erPnlSNro1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(pnlSaldosCuentaCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnlSaldosCuentaCobrarLayout.createSequentialGroup()
                                        .addGap(62, 62, 62)
                                        .addGroup(pnlSaldosCuentaCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lblSaldoCobrado)
                                            .addComponent(lblCaracter1erPnlSNro2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(35, 35, 35)
                                        .addGroup(pnlSaldosCuentaCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lblCaracter1erPnlSNro3, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lblValorSaldoPend, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSaldosCuentaCobrarLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblCobrado)
                                        .addGap(33, 33, 33)
                                        .addComponent(lblSaldoPend)))))
                        .addGap(17, 17, 17))))
        );
        pnlSaldosCuentaCobrarLayout.setVerticalGroup(
            pnlSaldosCuentaCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSaldosCuentaCobrarLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlSaldosCuentaCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTotal)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSaldosCuentaCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblCobrado)
                        .addComponent(lblSaldoPend)))
                .addGap(2, 2, 2)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(pnlSaldosCuentaCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCaracter1erPnlSNro2)
                    .addComponent(lblCaracter1erPnlSNro1)
                    .addComponent(lblCaracter1erPnlSNro3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSaldosCuentaCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSaldoTotal)
                    .addComponent(lblValorSaldoPend)
                    .addComponent(lblSaldoCobrado))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlRegistrarCobro.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "03. REGISTRAR COBRO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblMontoACobrar.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblMontoACobrar.setText("MONTO A COBRAR");

        txtValorMontoACobrar.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        txtValorMontoACobrar.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtValorMontoACobrar.setText("1,200.00");

        lblMensajeNoPuedeExcederSaldoPendienteValorSaldoPendiente.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblMensajeNoPuedeExcederSaldoPendienteValorSaldoPendiente.setText("No puede exceder el saldo pendiente (S/ 2,750.00) ");

        pnlSaldosRegistrarCobro.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

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

        lblMontoA_Cobrar.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblMontoA_Cobrar.setText("MONTO A");

        lblNuevo_Saldo.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblNuevo_Saldo.setText("NUEVO");

        lblPend_Saldo.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblPend_Saldo.setText("PEND.");

        lblCobrar_MontoA.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCobrar_MontoA.setText("COBRAR");

        lblSaldo_Nuevo.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblSaldo_Nuevo.setText("SALDO");

        javax.swing.GroupLayout pnlSaldosRegistrarCobroLayout = new javax.swing.GroupLayout(pnlSaldosRegistrarCobro);
        pnlSaldosRegistrarCobro.setLayout(pnlSaldosRegistrarCobroLayout);
        pnlSaldosRegistrarCobroLayout.setHorizontalGroup(
            pnlSaldosRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSaldosRegistrarCobroLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(pnlSaldosRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSaldosRegistrarCobroLayout.createSequentialGroup()
                        .addGroup(pnlSaldosRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator5, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlSaldosRegistrarCobroLayout.createSequentialGroup()
                                .addComponent(lblCaracter2doPnlSNro1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(62, 62, 62)
                                .addGroup(pnlSaldosRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblValorMontoACobrar)
                                    .addComponent(lblCaracter2doPnlSNro2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(35, 35, 35)
                                .addGroup(pnlSaldosRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblCaracter2doPnlSNro3, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblValorNuevoSaldo, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(17, 17, 17))
                    .addGroup(pnlSaldosRegistrarCobroLayout.createSequentialGroup()
                        .addGroup(pnlSaldosRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblValorSaldoPendiente)
                            .addGroup(pnlSaldosRegistrarCobroLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(pnlSaldosRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lblPend_Saldo, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblSaldo_Pend, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(36, 36, 36)
                                .addGroup(pnlSaldosRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblMontoA_Cobrar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblCobrar_MontoA, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(43, 43, 43)
                                .addGroup(pnlSaldosRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblNuevo_Saldo)
                                    .addComponent(lblSaldo_Nuevo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(28, 28, 28))))
        );
        pnlSaldosRegistrarCobroLayout.setVerticalGroup(
            pnlSaldosRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSaldosRegistrarCobroLayout.createSequentialGroup()
                .addContainerGap(11, Short.MAX_VALUE)
                .addGroup(pnlSaldosRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSaldo_Pend)
                    .addComponent(lblMontoA_Cobrar)
                    .addComponent(lblNuevo_Saldo))
                .addGap(4, 4, 4)
                .addGroup(pnlSaldosRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPend_Saldo)
                    .addComponent(lblCobrar_MontoA)
                    .addComponent(lblSaldo_Nuevo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(pnlSaldosRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCaracter2doPnlSNro2)
                    .addComponent(lblCaracter2doPnlSNro1)
                    .addComponent(lblCaracter2doPnlSNro3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSaldosRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblValorSaldoPendiente)
                    .addComponent(lblValorNuevoSaldo)
                    .addComponent(lblValorMontoACobrar))
                .addGap(12, 12, 12))
        );

        txtEstadoCobro.setEditable(false);
        txtEstadoCobro.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        txtEstadoCobro.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtEstadoCobro.setText("COBRO PARCIAL");

        lblMedioCobro.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblMedioCobro.setText("MEDIO DE COBRO");

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

        lblFechaCobro.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFechaCobro.setText("FECHA DEL COBRO");

        txtFechaYHoraCobro.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        txtFechaYHoraCobro.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtFechaYHoraCobro.setText("21/08/2026 10:40");

        btnRegistrarCobro.setBackground(new java.awt.Color(153, 51, 0));
        btnRegistrarCobro.setText("Registrar Cobro");
        btnRegistrarCobro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarCobroActionPerformed(evt);
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
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlSelectorCtaBancariaLayout.setVerticalGroup(
            pnlSelectorCtaBancariaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 13, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pnlRegistrarCobroLayout = new javax.swing.GroupLayout(pnlRegistrarCobro);
        pnlRegistrarCobro.setLayout(pnlRegistrarCobroLayout);
        pnlRegistrarCobroLayout.setHorizontalGroup(
            pnlRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlRegistrarCobroLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtEstadoCobro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(102, 102, 102))
            .addGroup(pnlRegistrarCobroLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(pnlRegistrarCobroLayout.createSequentialGroup()
                        .addComponent(btnCancelar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRegistrarCobro))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlRegistrarCobroLayout.createSequentialGroup()
                        .addComponent(lblFechaCobro)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtFechaYHoraCobro))
                    .addComponent(lblMedioCobro, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMensajeNoPuedeExcederSaldoPendienteValorSaldoPendiente, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlRegistrarCobroLayout.createSequentialGroup()
                        .addComponent(lblMontoACobrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtValorMontoACobrar, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlSaldosRegistrarCobro, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlRegistrarCobroLayout.createSequentialGroup()
                        .addComponent(btnEfectivo, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBanco, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlDatosCaja, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator6, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlSelectorCtaBancaria, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        pnlRegistrarCobroLayout.setVerticalGroup(
            pnlRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRegistrarCobroLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtValorMontoACobrar)
                    .addComponent(lblMontoACobrar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMensajeNoPuedeExcederSaldoPendienteValorSaldoPendiente)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlSaldosRegistrarCobro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtEstadoCobro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlRegistrarCobroLayout.createSequentialGroup()
                        .addComponent(lblMedioCobro)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnEfectivo)
                            .addComponent(btnBanco))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlDatosCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pnlSelectorCtaBancaria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addGroup(pnlRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblFechaCobro)
                            .addComponent(txtFechaYHoraCobro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlRegistrarCobroLayout.createSequentialGroup()
                        .addGroup(pnlRegistrarCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnRegistrarCobro)
                            .addComponent(btnCancelar))
                        .addContainerGap())))
        );

        javax.swing.GroupLayout pnlCuentaSeleccionadaLayout = new javax.swing.GroupLayout(pnlCuentaSeleccionada);
        pnlCuentaSeleccionada.setLayout(pnlCuentaSeleccionadaLayout);
        pnlCuentaSeleccionadaLayout.setHorizontalGroup(
            pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCuentaSeleccionadaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlSaldosCuentaCobrar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlRegistrarCobro, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlCuentaSeleccionadaLayout.createSequentialGroup()
                                .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(lblVenta, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblVencimiento, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblFechaVenta, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblValorFechaVenta, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                                    .addComponent(lblValorFechaVencimiento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblValorCodigoVenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(pnlCuentaSeleccionadaLayout.createSequentialGroup()
                                .addComponent(lblNombreEmpresaYTipoEmpresa, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtEstadoCuentaCobrar, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(lblRucNroRuc, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        pnlCuentaSeleccionadaLayout.setVerticalGroup(
            pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCuentaSeleccionadaLayout.createSequentialGroup()
                .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlCuentaSeleccionadaLayout.createSequentialGroup()
                        .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtEstadoCuentaCobrar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblNombreEmpresaYTipoEmpresa))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblRucNroRuc, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblVenta)
                            .addComponent(lblValorCodigoVenta))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblFechaVenta))
                    .addComponent(lblValorFechaVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCuentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVencimiento)
                    .addComponent(lblValorFechaVencimiento, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSaldosCuentaCobrar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlRegistrarCobro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 12, Short.MAX_VALUE))
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
                            .addComponent(spnlTblDetalleCuentasCobrar, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlCuentasCobrar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(lblNCuentasCobrarFiltrosSeleccionados, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnlAcomodador, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlCuentasCobrar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnlTblDetalleCuentasCobrar, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblNCuentasCobrarFiltrosSeleccionados, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(spnlAcomodador, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        // TODO add your handling code here:
        consultarCuentas();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
        limpiarFiltros();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnRegistrarCobroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarCobroActionPerformed
        // TODO add your handling code here:
        registrarCobro();
    }//GEN-LAST:event_btnRegistrarCobroActionPerformed

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
            java.util.logging.Logger.getLogger(FrmCobroCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmCobroCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmCobroCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmCobroCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmCobroCliente dialog = new FrmCobroCliente(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnRegistrarCobro;
    private javax.swing.JComboBox<Cliente> cmbClientes;
    private javax.swing.JComboBox<String> cmbEstado;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private com.toedter.calendar.JDateChooser jdcDesdeFecha;
    private com.toedter.calendar.JDateChooser jdcHastaFecha;
    private javax.swing.JLabel lblAplicarCobroSobreCuentaCobrarExistente;
    private javax.swing.JLabel lblCajaAbierta;
    private javax.swing.JLabel lblCajaPrincipal;
    private javax.swing.JLabel lblCaracter1erPnlSNro1;
    private javax.swing.JLabel lblCaracter1erPnlSNro2;
    private javax.swing.JLabel lblCaracter1erPnlSNro3;
    private javax.swing.JLabel lblCaracter2doPnlSNro1;
    private javax.swing.JLabel lblCaracter2doPnlSNro2;
    private javax.swing.JLabel lblCaracter2doPnlSNro3;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblCobrado;
    private javax.swing.JLabel lblCobrar_MontoA;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblFechaActual;
    private javax.swing.JLabel lblFechaCobro;
    private javax.swing.JLabel lblFechaVenta;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblHoraActual;
    private javax.swing.JLabel lblMedioCobro;
    private javax.swing.JLabel lblMensajeNoPuedeExcederSaldoPendienteValorSaldoPendiente;
    private javax.swing.JLabel lblMontoACobrar;
    private javax.swing.JLabel lblMontoA_Cobrar;
    private javax.swing.JLabel lblNCuentasCobrarFiltrosSeleccionados;
    private javax.swing.JLabel lblNombreApellidoUsuario;
    private javax.swing.JLabel lblNombreEmpresaYTipoEmpresa;
    private javax.swing.JLabel lblNuevo_Saldo;
    private javax.swing.JLabel lblPend_Saldo;
    private javax.swing.JLabel lblRegistroCobroCliente;
    private javax.swing.JLabel lblRucNroRuc;
    private javax.swing.JLabel lblSaldoActual;
    private javax.swing.JLabel lblSaldoCobrado;
    private javax.swing.JLabel lblSaldoPend;
    private javax.swing.JLabel lblSaldoTotal;
    private javax.swing.JLabel lblSaldo_Nuevo;
    private javax.swing.JLabel lblSaldo_Pend;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblValorCodigoVenta;
    private javax.swing.JLabel lblValorFechaVencimiento;
    private javax.swing.JLabel lblValorFechaVenta;
    private javax.swing.JLabel lblValorMontoACobrar;
    private javax.swing.JLabel lblValorNuevoSaldo;
    private javax.swing.JLabel lblValorSaldoActual;
    private javax.swing.JLabel lblValorSaldoPend;
    private javax.swing.JLabel lblValorSaldoPendiente;
    private javax.swing.JLabel lblVencimiento;
    private javax.swing.JLabel lblVenta;
    private javax.swing.JPanel pnlCuentaSeleccionada;
    private javax.swing.JPanel pnlCuentasCobrar;
    private javax.swing.JPanel pnlDatosCaja;
    private javax.swing.JPanel pnlRegistrarCobro;
    private javax.swing.JPanel pnlSaldosCuentaCobrar;
    private javax.swing.JPanel pnlSaldosRegistrarCobro;
    private javax.swing.JPanel pnlSelectorCtaBancaria;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JScrollPane spnlAcomodador;
    private javax.swing.JScrollPane spnlTblDetalleCuentasCobrar;
    private javax.swing.JTable tblDetalleCuentasCobrar;
    private javax.swing.JTextField txtEstadoCobro;
    private javax.swing.JTextField txtEstadoCuentaCobrar;
    private javax.swing.JTextField txtFechaYHoraCobro;
    private javax.swing.JTextField txtValorMontoACobrar;
    // End of variables declaration//GEN-END:variables
}
