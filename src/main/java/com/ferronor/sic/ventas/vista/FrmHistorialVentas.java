package com.ferronor.sic.ventas.vista;

import com.ferronor.sic.maestros.logica.ClienteService;
import com.ferronor.sic.maestros.logica.FormaPagoService;
import com.ferronor.sic.maestros.logica.ProductoService;
import com.ferronor.sic.maestros.logica.TipoComprobanteService;
import com.ferronor.sic.maestros.modelo.Cliente;
import com.ferronor.sic.maestros.modelo.FormaPago;
import com.ferronor.sic.maestros.modelo.Producto;
import com.ferronor.sic.maestros.modelo.TipoComprobante;
import com.ferronor.sic.seguridad.logica.UsuarioService;
import com.ferronor.sic.seguridad.modelo.Usuario;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;
import com.ferronor.sic.ventas.logica.VentaService;
import com.ferronor.sic.ventas.modelo.DetalleVenta;
import com.ferronor.sic.ventas.modelo.EstadoVenta;
import com.ferronor.sic.ventas.modelo.Venta;
import com.ferronor.sic.ventas.modelo.dto.VentaConsulta;

import java.awt.Font;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class FrmHistorialVentas extends javax.swing.JDialog {

    // ============================================================
    // SERVICES
    // ============================================================
    private final VentaService ventaService
            = ServiceFactory.ventaService();

    private final ClienteService clienteService
            = ServiceFactory.clienteService();

    private final FormaPagoService formaPagoService
            = ServiceFactory.formaPagoService();

    private final TipoComprobanteService tipoComprobanteService
            = ServiceFactory.tipoComprobanteService();

    private final ProductoService productoService
            = ServiceFactory.productoService();

    private final UsuarioService usuarioService
            = ServiceFactory.usuarioService();

    // ============================================================
    // DATOS DE LA CONSULTA
    // ============================================================
    private List<VentaConsulta> ventasConsultadas
            = new ArrayList<>();

    private VentaConsulta ventaConsultaSeleccionada;

    private Venta ventaSeleccionada;

    private final Map<Integer, Boolean> formasPagoCredito
            = new HashMap<>();

    // ============================================================
    // MODELOS DE TABLA
    // ============================================================
    private DefaultTableModel modeloVentas;

    private DefaultTableModel modeloProductos;

    // ============================================================
    // FORMATOS
    // ============================================================
    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_HORA
            = DateTimeFormatter.ofPattern("HH:mm");

    private static final DecimalFormat FORMATO_MONEDA
            = new DecimalFormat("#,##0.00");

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    public FrmHistorialVentas(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        initComponents();

        setDefaultCloseOperation(
                javax.swing.WindowConstants.DISPOSE_ON_CLOSE
        );

        configurarFormulario();
    }

    // ============================================================
    // CONFIGURACIÓN GENERAL
    // ============================================================
    private void configurarFormulario() {

        actualizarCabecera();

        configurarTablas();

        configurarCombos();

        configurarEventosTabla();

        configurarEstadoInicial();

        cargarFormasPago();

        consultarHistorial();

        setLocationRelativeTo(getParent());

    }

    // ============================================================
    // CABECERA
    // ============================================================
    private void actualizarCabecera() {

        SesionUsuario sesion = SesionUsuario.actual();

        if (sesion != null) {
            lblNombreApellidoUsuarioActual.setText(
                    sesion.getNombreCompleto()
            );
        } else {
            lblNombreApellidoUsuarioActual.setText(
                    "-"
            );
        }

        LocalDateTime ahora = LocalDateTime.now();

        lblValorFecha.setText(
                ahora.format(FORMATO_FECHA)
        );

        lblValorHora.setText(
                ahora.format(FORMATO_HORA)
        );
    }

    // ============================================================
    // TABLAS
    // ============================================================
    private void configurarTablas() {

        modeloVentas = new DefaultTableModel(
                new Object[]{
                    "N°. VENTA",
                    "FECHA",
                    "COMPROBANTE",
                    "CLIENTE",
                    "DOCUMENTO",
                    "FORMA PAGO",
                    "TOTAL",
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

        modeloProductos = new DefaultTableModel(
                new Object[]{
                    "CÓDIGO",
                    "PRODUCTO",
                    "CANTIDAD",
                    "P. UNITARIO",
                    "SUBTOTAL"
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

        tblVentasEncontradas.setModel(
                modeloVentas
        );

        tlbProductosVendidos.setModel(
                modeloProductos
        );

        tblVentasEncontradas.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tlbProductosVendidos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        Font fuenteTabla = new Font(
                "Segoe UI",
                Font.PLAIN,
                9
        );

        Font fuenteCabecera = new Font(
                "Segoe UI",
                Font.BOLD,
                9
        );

        tblVentasEncontradas.setFont(
                fuenteTabla
        );

        tblVentasEncontradas.setRowHeight(23);

        tblVentasEncontradas
                .getTableHeader()
                .setFont(fuenteCabecera);

        tlbProductosVendidos.setFont(
                fuenteTabla
        );

        tlbProductosVendidos.setRowHeight(23);

        tlbProductosVendidos
                .getTableHeader()
                .setFont(fuenteCabecera);

        configurarAlineacionesTablas();
    }

    private void configurarAlineacionesTablas() {

        DefaultTableCellRenderer centrado
                = new DefaultTableCellRenderer();

        centrado.setHorizontalAlignment(
                javax.swing.SwingConstants.CENTER
        );

        DefaultTableCellRenderer derecha
                = new DefaultTableCellRenderer();

        derecha.setHorizontalAlignment(
                javax.swing.SwingConstants.RIGHT
        );

        tblVentasEncontradas
                .getColumnModel()
                .getColumn(0)
                .setCellRenderer(centrado);

        tblVentasEncontradas
                .getColumnModel()
                .getColumn(1)
                .setCellRenderer(centrado);

        tblVentasEncontradas
                .getColumnModel()
                .getColumn(6)
                .setCellRenderer(derecha);

        tlbProductosVendidos
                .getColumnModel()
                .getColumn(2)
                .setCellRenderer(derecha);

        tlbProductosVendidos
                .getColumnModel()
                .getColumn(3)
                .setCellRenderer(derecha);

        tlbProductosVendidos
                .getColumnModel()
                .getColumn(4)
                .setCellRenderer(derecha);
    }

    // ============================================================
    // COMBOS
    // ============================================================
    private void configurarCombos() {

        configurarComboClientes();

        configurarComboEstados();

        configurarComboTiposComprobante();
    }

    private void configurarComboClientes() {

        DefaultComboBoxModel<Cliente> modelo
                = new DefaultComboBoxModel<>();

        modelo.addElement(null);

        List<Cliente> clientes
                = clienteService.listarActivos();

        for (Cliente cliente : clientes) {
            modelo.addElement(cliente);
        }

        cmbClientes.setModel(modelo);

        cmbClientes.setRenderer(
                new DefaultListCellRenderer() {

            @Override
            public java.awt.Component
                    getListCellRendererComponent(
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

                if (value == null) {

                    setText("Todos los clientes");

                } else if (value instanceof Cliente cliente) {

                    setText(
                            cliente.getNombreRazonSocial()
                    );
                }

                return this;
            }
        });

        cmbClientes.setSelectedItem(null);
    }

    private void configurarComboEstados() {

        DefaultComboBoxModel<EstadoVenta> modelo
                = new DefaultComboBoxModel<>();

        modelo.addElement(null);

        for (EstadoVenta estado
                : EstadoVenta.values()) {

            modelo.addElement(estado);
        }

        cmbEstados.setModel(modelo);

        cmbEstados.setRenderer(
                new DefaultListCellRenderer() {

            @Override
            public java.awt.Component
                    getListCellRendererComponent(
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

                if (value == null) {

                    setText("Todos");

                } else if (value instanceof EstadoVenta estado) {

                    setText(
                            estado.name()
                    );
                }

                return this;
            }
        });

        cmbEstados.setSelectedItem(null);
    }

    private void configurarComboTiposComprobante() {

        DefaultComboBoxModel<TipoComprobante> modelo
                = new DefaultComboBoxModel<>();

        modelo.addElement(null);

        List<TipoComprobante> tipos
                = tipoComprobanteService.listar();

        for (TipoComprobante tipo : tipos) {
            modelo.addElement(tipo);
        }

        cmbTiposComprobante.setModel(modelo);

        cmbTiposComprobante.setRenderer(
                new DefaultListCellRenderer() {

            @Override
            public java.awt.Component
                    getListCellRendererComponent(
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

                if (value == null) {

                    setText("Todos");

                } else if (value instanceof TipoComprobante tipo) {

                    String serie = tipo.getSerie();

                    if (serie == null
                            || serie.isBlank()) {

                        setText(
                                tipo.getNombre()
                        );

                    } else {

                        setText(
                                tipo.getNombre()
                                + " - "
                                + serie
                        );
                    }
                }

                return this;
            }
        });

        cmbTiposComprobante.setSelectedItem(null);
    }

    // ============================================================
    // FORMAS DE PAGO
    // ============================================================
    private void cargarFormasPago() {

        formasPagoCredito.clear();

        List<FormaPago> formasPago
                = formaPagoService.listar();

        for (FormaPago formaPago
                : formasPago) {

            formasPagoCredito.put(
                    formaPago.getIdFormaPago(),
                    formaPago.isEsCredito()
            );
        }
    }

    // ============================================================
    // EVENTOS DE TABLA
    // ============================================================
    private void configurarEventosTabla() {

        tblVentasEncontradas
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (e.getValueIsAdjusting()) {
                        return;
                    }

                    seleccionarVentaDesdeTabla();
                });
    }

    private void seleccionarVentaDesdeTabla() {

        int fila
                = tblVentasEncontradas
                        .getSelectedRow();

        if (fila < 0) {

            limpiarDetalle();

            return;
        }

        if (fila >= ventasConsultadas.size()) {

            limpiarDetalle();

            return;
        }

        ventaConsultaSeleccionada
                = ventasConsultadas.get(fila);

        cargarDetalleVenta(
                ventaConsultaSeleccionada
                        .getIdVenta()
        );
    }

    // ============================================================
    // ESTADO INICIAL
    // ============================================================
    private void configurarEstadoInicial() {

        ventasConsultadas.clear();

        ventaConsultaSeleccionada = null;

        ventaSeleccionada = null;

        jdcFechaDesde.setDate(null);

        jdcFechaHasta.setDate(null);

        cmbClientes.setSelectedItem(null);

        cmbEstados.setSelectedItem(null);

        cmbTiposComprobante.setSelectedItem(null);

        limpiarTablas();

        limpiarDetalle();
    }

    // ============================================================
    // CONSULTA
    // ============================================================
    private void consultarHistorial() {

        LocalDate fechaDesde
                = obtenerFecha(jdcFechaDesde);

        LocalDate fechaHasta
                = obtenerFecha(jdcFechaHasta);

        if (fechaDesde != null
                && fechaHasta != null
                && fechaDesde.isAfter(fechaHasta)) {

            JOptionPane.showMessageDialog(
                    this,
                    "La fecha desde no puede ser posterior "
                    + "a la fecha hasta.",
                    "Filtro de fechas",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        Cliente cliente
                = (Cliente) cmbClientes.getSelectedItem();

        EstadoVenta estado
                = (EstadoVenta) cmbEstados.getSelectedItem();

        TipoComprobante tipoComprobante
                = (TipoComprobante) cmbTiposComprobante
                        .getSelectedItem();

        Integer idCliente
                = cliente != null
                        ? cliente.getIdCliente()
                        : null;

        Integer idTipoComprobante
                = tipoComprobante != null
                        ? tipoComprobante
                                .getIdTipoComprobante()
                        : null;

        try {

            ventasConsultadas
                    = ventaService.consultarHistorial(
                            fechaDesde,
                            fechaHasta,
                            idCliente,
                            estado,
                            idTipoComprobante
                    );

            cargarTablaVentas();

            actualizarResumen();

            limpiarDetalle();

        } catch (RuntimeException ex) {

            ventasConsultadas
                    = new ArrayList<>();

            limpiarTablas();

            actualizarResumen();

            limpiarDetalle();

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al consultar historial",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // TABLA PRINCIPAL
    // ============================================================
    private void cargarTablaVentas() {

        modeloVentas.setRowCount(0);

        for (VentaConsulta venta
                : ventasConsultadas) {

            modeloVentas.addRow(
                    new Object[]{
                        venta.getIdVenta(),
                        formatearFechaHora(
                                venta.getFecha()
                        ),
                        construirComprobante(
                                venta
                        ),
                        venta.getNombreRazonSocialCliente(),
                        construirDocumento(
                                venta
                        ),
                        venta.getNombreFormaPago(),
                        "S/ " + formatearMoneda(
                                venta.getTotal()
                        ),
                        formatearEstado(
                                venta.getEstado()
                        )
                    }
            );
        }
    }

    private String construirComprobante(
            VentaConsulta venta) {

        String serie = venta.getSerie();

        String numero = venta.getNumero();

        if (serie == null
                || serie.isBlank()) {

            return numero != null
                    && !numero.isBlank()
                    ? numero
                    : "-";
        }

        if (numero == null
                || numero.isBlank()) {

            return serie;
        }

        return serie + "-" + numero;
    }

    private String construirDocumento(
            VentaConsulta venta) {

        String tipo
                = venta.getTipoDocumentoCliente();

        String numero
                = venta.getNumeroDocumentoCliente();

        if (numero == null
                || numero.isBlank()) {

            return "-";
        }

        if (tipo == null
                || tipo.isBlank()) {

            return numero;
        }

        return tipo + " " + numero;
    }

    private String formatearEstado(
            EstadoVenta estado) {

        if (estado == null) {
            return "";
        }

        return estado.name()
                .replace("_", " ");
    }

    // ============================================================
    // DETALLE DE VENTA
    // ============================================================
    private void cargarDetalleVenta(
            int idVenta) {

        try {

            Venta venta
                    = ventaService.buscarPorId(
                            idVenta
                    );

            if (venta == null) {

                limpiarDetalle();

                JOptionPane.showMessageDialog(
                        this,
                        "No se encontró la venta seleccionada.",
                        "Venta no encontrada",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            ventaSeleccionada = venta;

            actualizarInformacionVenta(
                    venta
            );

            cargarProductosVendidos(
                    venta
            );

            actualizarMontosVenta(
                    venta
            );

        } catch (RuntimeException ex) {

            limpiarDetalle();

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al cargar detalle",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void actualizarInformacionVenta(
            Venta venta) {

        /*
         * IMPORTANTE:
         * El N°. VENTA es solamente el idVenta numérico.
         * No se agrega prefijo V-.
         */
        lblValorNroVenta.setText(
                String.valueOf(
                        venta.getIdVenta()
                )
        );

        lblValorFechaHoraVenta.setText(
                venta.getFecha() != null
                ? venta.getFecha()
                        .format(
                                FORMATO_FECHA_HORA
                        )
                : "-"
        );

        lblValorEstadoVenta.setText(
                formatearEstado(
                        venta.getEstado()
                )
        );

        Cliente cliente
                = clienteService.buscarPorId(
                        venta.getIdCliente()
                );

        if (cliente != null) {

            lblValorClienteVenta.setText(
                    cliente.getNombreRazonSocial()
            );

            lblValorDocumentoCliente.setText(
                    construirDocumentoCliente(
                            cliente
                    )
            );

        } else {

            lblValorClienteVenta.setText("-");

            lblValorDocumentoCliente.setText("-");
        }

        FormaPago formaPago
                = formaPagoService.buscarPorId(
                        venta.getIdFormaPago()
                );

        lblValorFormaPago.setText(
                formaPago != null
                        ? formaPago.getNombre()
                        : "-"
        );

        Usuario usuario
                = usuarioService.buscarPorId(
                        venta.getIdUsuario()
                );

        lblValorUsuarioVenta.setText(
                usuario != null
                        ? usuario.getNombreCompleto()
                        : "-"
        );
    }

    private String construirDocumentoCliente(
            Cliente cliente) {

        if (cliente == null) {
            return "-";
        }

        String tipo
                = cliente.getTipoDocumento() != null
                ? cliente.getTipoDocumento().name()
                : null;

        String numero
                = cliente.getNumeroDocumento();

        if (numero == null
                || numero.isBlank()) {

            return "-";
        }

        if (tipo == null
                || tipo.isBlank()) {

            return numero;
        }

        return tipo + " " + numero;
    }

    // ============================================================
    // PRODUCTOS
    // ============================================================
    private void cargarProductosVendidos(
            Venta venta) {

        modeloProductos.setRowCount(0);

        for (DetalleVenta detalle
                : venta.getDetalles()) {

            Producto producto
                    = productoService.buscarPorId(
                            detalle.getIdProducto()
                    );

            String codigo
                    = producto != null
                            ? producto.getCodigo()
                            : String.valueOf(
                                    detalle.getIdProducto()
                            );

            String nombre
                    = producto != null
                            ? producto.getNombre()
                            : "-";

            modeloProductos.addRow(
                    new Object[]{
                        codigo,
                        nombre,
                        formatearCantidad(
                                detalle.getCantidad()
                        ),
                        "S/ "
                        + formatearMoneda(
                                detalle.getPrecioUnitario()
                        ),
                        "S/ "
                        + formatearMoneda(
                                detalle.getSubtotal()
                        )
                    }
            );
        }
    }

    // ============================================================
    // MONTOS
    // ============================================================
    private void actualizarMontosVenta(
            Venta venta) {

        txtValorSubtotal.setText(
                "S/ "
                + formatearMoneda(
                        venta.getSubtotal()
                )
        );

        txtValorIGV.setText(
                "S/ "
                + formatearMoneda(
                        venta.getIgv()
                )
        );

        txtValorTotal.setText(
                "S/ "
                + formatearMoneda(
                        venta.getTotal()
                )
        );
    }

    // ============================================================
    // RESUMEN
    // ============================================================
    private void actualizarResumen() {

        int cantidad
                = ventasConsultadas.size();

        BigDecimal montoTotal
                = BigDecimal.ZERO;

        int contado = 0;

        int credito = 0;

        for (VentaConsulta venta
                : ventasConsultadas) {

            if (venta.getTotal() != null) {

                montoTotal
                        = montoTotal.add(
                                venta.getTotal()
                        );
            }

            Boolean esCredito
                    = formasPagoCredito.get(
                            venta.getIdFormaPago()
                    );

            if (Boolean.TRUE.equals(
                    esCredito
            )) {

                credito++;

            } else {

                contado++;
            }
        }

        lblCantVentasEncontradas.setText(
                String.valueOf(cantidad)
        );

        lblValorMontoTotalVendido.setText(
                "S/ "
                + formatearMoneda(
                        montoTotal
                )
        );

        lblCantVentasContado.setText(
                String.valueOf(contado)
        );

        lblCantVentasCredito.setText(
                String.valueOf(credito)
        );
    }

    // ============================================================
    // LIMPIAR
    // ============================================================
    private void limpiarFiltros() {

        jdcFechaDesde.setDate(null);

        jdcFechaHasta.setDate(null);

        cmbClientes.setSelectedItem(null);

        cmbEstados.setSelectedItem(null);

        cmbTiposComprobante.setSelectedItem(null);

        consultarHistorial();
    }

    private void limpiarDetalle() {

        ventaConsultaSeleccionada = null;

        ventaSeleccionada = null;

        lblValorNroVenta.setText("-");

        lblValorFormaPago.setText("-");

        lblValorFechaHoraVenta.setText("-");

        lblValorEstadoVenta.setText("-");

        lblValorClienteVenta.setText("-");

        lblValorUsuarioVenta.setText("-");

        lblValorDocumentoCliente.setText("-");

        txtValorSubtotal.setText("S/ 0.00");

        txtValorIGV.setText("S/ 0.00");

        txtValorTotal.setText("S/ 0.00");

        modeloProductos.setRowCount(0);

        tblVentasEncontradas.clearSelection();
    }

    private void limpiarTablas() {

        modeloVentas.setRowCount(0);

        modeloProductos.setRowCount(0);
    }

    // ============================================================
    // FECHAS
    // ============================================================
    private LocalDate obtenerFecha(
            com.toedter.calendar.JDateChooser selector) {

        if (selector == null
                || selector.getDate() == null) {

            return null;
        }

        return selector.getDate()
                .toInstant()
                .atZone(
                        ZoneId.systemDefault()
                )
                .toLocalDate();
    }

    // ============================================================
    // FORMATOS
    // ============================================================
    private String formatearFechaHora(
            LocalDateTime fechaHora) {

        if (fechaHora == null) {
            return "-";
        }

        return fechaHora.format(
                FORMATO_FECHA_HORA
        );
    }

    private String formatearMoneda(
            BigDecimal valor) {

        if (valor == null) {
            return "0.00";
        }

        return FORMATO_MONEDA.format(
                valor
        );
    }

    private String formatearCantidad(
            BigDecimal valor) {

        if (valor == null) {
            return "0";
        }

        return valor
                .stripTrailingZeros()
                .toPlainString();
    }

    private String obtenerMensajeError(
            RuntimeException ex) {

        if (ex == null) {
            return "Se produjo un error inesperado.";
        }

        String mensaje = ex.getMessage();

        if (mensaje == null
                || mensaje.isBlank()) {

            return "Se produjo un error inesperado.";
        }

        return mensaje;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSuperior = new javax.swing.JPanel();
        lblHistorialVentas = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        lblNombreApellidoUsuarioActual = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        lblValorFecha = new javax.swing.JLabel();
        lblHora = new javax.swing.JLabel();
        lblValorHora = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        pnlFiltrosConsulta = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        lblFechaDesde = new javax.swing.JLabel();
        jdcFechaDesde = new com.toedter.calendar.JDateChooser();
        lblFechaHasta = new javax.swing.JLabel();
        jdcFechaHasta = new com.toedter.calendar.JDateChooser();
        lblCliente = new javax.swing.JLabel();
        cmbClientes = new javax.swing.JComboBox<>();
        lblEstado = new javax.swing.JLabel();
        cmbEstados = new javax.swing.JComboBox<>();
        lblTipoComprobante = new javax.swing.JLabel();
        cmbTiposComprobante = new javax.swing.JComboBox<>();
        jSeparator2 = new javax.swing.JSeparator();
        btnConsultar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        pnlCantVentasEncontradas = new javax.swing.JPanel();
        lblVentasEncontradas = new javax.swing.JLabel();
        lblCantVentasEncontradas = new javax.swing.JLabel();
        spnlVentasEncontradas = new javax.swing.JScrollPane();
        tblVentasEncontradas = new javax.swing.JTable();
        pnlDetalleVentaSeleccionada = new javax.swing.JPanel();
        jSeparator3 = new javax.swing.JSeparator();
        lblInformacionVenta = new javax.swing.JLabel();
        pnlNro1Informacion = new javax.swing.JPanel();
        lblNroVenta = new javax.swing.JLabel();
        lblValorNroVenta = new javax.swing.JLabel();
        lblFormaPago = new javax.swing.JLabel();
        lblValorFormaPago = new javax.swing.JLabel();
        pnlNro2Informacion = new javax.swing.JPanel();
        lblFechaVenta = new javax.swing.JLabel();
        lblValorFechaHoraVenta = new javax.swing.JLabel();
        lblEstadoVenta = new javax.swing.JLabel();
        lblValorEstadoVenta = new javax.swing.JLabel();
        pnlNro3Informacion = new javax.swing.JPanel();
        lblClienteVenta = new javax.swing.JLabel();
        lblValorClienteVenta = new javax.swing.JLabel();
        lblUsuarioVenta = new javax.swing.JLabel();
        lblValorUsuarioVenta = new javax.swing.JLabel();
        pnlNro4Informacion = new javax.swing.JPanel();
        lblDocumento = new javax.swing.JLabel();
        lblValorDocumentoCliente = new javax.swing.JLabel();
        pnlMontoTotalVendido = new javax.swing.JPanel();
        lblMontoTotalVendido = new javax.swing.JLabel();
        lblValorMontoTotalVendido = new javax.swing.JLabel();
        pnlCantVentasContado = new javax.swing.JPanel();
        lblVentasContado = new javax.swing.JLabel();
        lblCantVentasContado = new javax.swing.JLabel();
        pnlCantVentasCredito = new javax.swing.JPanel();
        lblVentasCredito = new javax.swing.JLabel();
        lblCantVentasCredito = new javax.swing.JLabel();
        spnlProductosVendidos = new javax.swing.JScrollPane();
        tlbProductosVendidos = new javax.swing.JTable();
        pnlMontosVenta = new javax.swing.JPanel();
        txtSubtotal = new javax.swing.JTextField();
        txtValorSubtotal = new javax.swing.JTextField();
        txtIGV = new javax.swing.JTextField();
        txtValorIGV = new javax.swing.JTextField();
        txtTotal = new javax.swing.JTextField();
        txtValorTotal = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblHistorialVentas.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 24)); // NOI18N
        lblHistorialVentas.setText("HISTORIAL DE VENTAS");

        lblUsuario.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblUsuario.setText("USUARIO:");

        lblNombreApellidoUsuarioActual.setText("Nombre Ap. (Sesion activa)");

        lblFecha.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFecha.setText("FECHA:");

        lblValorFecha.setText("dd/mm/yy");

        lblHora.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblHora.setText("HORA:");

        lblValorHora.setText("19:18 p.m.");

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblSubtitulo.setText("Consulta y seguimiento del historial de operaciones comerciales  ");

        javax.swing.GroupLayout pnlSuperiorLayout = new javax.swing.GroupLayout(pnlSuperior);
        pnlSuperior.setLayout(pnlSuperiorLayout);
        pnlSuperiorLayout.setHorizontalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSuperiorLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblSubtitulo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblHistorialVentas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNombreApellidoUsuarioActual)
                    .addComponent(lblUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblValorFecha, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblHora, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorHora, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))
                .addGap(12, 12, 12))
        );
        pnlSuperiorLayout.setVerticalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSuperiorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblUsuario)
                            .addComponent(lblFecha)
                            .addComponent(lblHora))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNombreApellidoUsuarioActual)
                            .addComponent(lblValorFecha)
                            .addComponent(lblValorHora)))
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblHistorialVentas, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSubtitulo)))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlFiltrosConsulta.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. FILTROS DE CONSULTA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblFechaDesde.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFechaDesde.setText("FECHA DESDE");

        lblFechaHasta.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFechaHasta.setText("FECHA HASTA");

        lblCliente.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCliente.setText("CLIENTE");

        lblEstado.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblEstado.setText("ESTADO");

        lblTipoComprobante.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTipoComprobante.setText("TIPO DE COMPROBANTE");

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

        javax.swing.GroupLayout pnlFiltrosConsultaLayout = new javax.swing.GroupLayout(pnlFiltrosConsulta);
        pnlFiltrosConsulta.setLayout(pnlFiltrosConsultaLayout);
        pnlFiltrosConsultaLayout.setHorizontalGroup(
            pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(pnlFiltrosConsultaLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jdcFechaDesde, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                    .addComponent(lblFechaDesde, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jdcFechaHasta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbClientes, 0, 192, Short.MAX_VALUE)
                    .addComponent(lblCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbEstados, 0, 100, Short.MAX_VALUE)
                    .addComponent(lblEstado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblTipoComprobante, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                    .addComponent(cmbTiposComprobante, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnConsultar, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jSeparator2)
        );
        pnlFiltrosConsultaLayout.setVerticalGroup(
            pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFiltrosConsultaLayout.createSequentialGroup()
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFiltrosConsultaLayout.createSequentialGroup()
                        .addGroup(pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblFechaDesde)
                            .addComponent(lblFechaHasta)
                            .addComponent(lblCliente)
                            .addComponent(lblEstado)
                            .addComponent(lblTipoComprobante))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jdcFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jdcFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cmbClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbEstados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbTiposComprobante, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(pnlFiltrosConsultaLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnConsultar)
                            .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(12, 12, 12)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pnlCantVentasEncontradas.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblVentasEncontradas.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblVentasEncontradas.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblVentasEncontradas.setText("VENTAS ENCONTRADAS");

        lblCantVentasEncontradas.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblCantVentasEncontradas.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCantVentasEncontradas.setText("7");

        javax.swing.GroupLayout pnlCantVentasEncontradasLayout = new javax.swing.GroupLayout(pnlCantVentasEncontradas);
        pnlCantVentasEncontradas.setLayout(pnlCantVentasEncontradasLayout);
        pnlCantVentasEncontradasLayout.setHorizontalGroup(
            pnlCantVentasEncontradasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCantVentasEncontradasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCantVentasEncontradasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblVentasEncontradas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlCantVentasEncontradasLayout.createSequentialGroup()
                        .addComponent(lblCantVentasEncontradas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        pnlCantVentasEncontradasLayout.setVerticalGroup(
            pnlCantVentasEncontradasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCantVentasEncontradasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblVentasEncontradas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCantVentasEncontradas)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        spnlVentasEncontradas.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. VENTAS ENCONTRADAS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        tblVentasEncontradas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "N°. VENTA", "FECHA", "COMPROBANTE", "CLIENTE", "DOCUMENTO", "FORMA PAGO", "TOTAL", "ESTADO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlVentasEncontradas.setViewportView(tblVentasEncontradas);

        pnlDetalleVentaSeleccionada.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "03. DETALLE DE LA VENTA SELECCIONADA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblInformacionVenta.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 12)); // NOI18N
        lblInformacionVenta.setText("Información de la Venta");

        pnlNro1Informacion.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblNroVenta.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblNroVenta.setText("N°. VENTA");

        lblValorNroVenta.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorNroVenta.setText("V-000128");

        lblFormaPago.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblFormaPago.setText("FORMA PAGO");

        lblValorFormaPago.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorFormaPago.setText("Contado");

        javax.swing.GroupLayout pnlNro1InformacionLayout = new javax.swing.GroupLayout(pnlNro1Informacion);
        pnlNro1Informacion.setLayout(pnlNro1InformacionLayout);
        pnlNro1InformacionLayout.setHorizontalGroup(
            pnlNro1InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNro1InformacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlNro1InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNroVenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorNroVenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblFormaPago, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                    .addComponent(lblValorFormaPago, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlNro1InformacionLayout.setVerticalGroup(
            pnlNro1InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlNro1InformacionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblNroVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorNroVenta)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblFormaPago, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorFormaPago)
                .addContainerGap())
        );

        pnlNro2Informacion.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblFechaVenta.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblFechaVenta.setText("FECHA");

        lblValorFechaHoraVenta.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorFechaHoraVenta.setText("28/08/2026 10:42 AM ");

        lblEstadoVenta.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblEstadoVenta.setText("ESTADO");

        lblValorEstadoVenta.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorEstadoVenta.setText("DESPACHADA");

        javax.swing.GroupLayout pnlNro2InformacionLayout = new javax.swing.GroupLayout(pnlNro2Informacion);
        pnlNro2Informacion.setLayout(pnlNro2InformacionLayout);
        pnlNro2InformacionLayout.setHorizontalGroup(
            pnlNro2InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNro2InformacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlNro2InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFechaVenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorFechaHoraVenta, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                    .addComponent(lblEstadoVenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorEstadoVenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlNro2InformacionLayout.setVerticalGroup(
            pnlNro2InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlNro2InformacionLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblFechaVenta)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorFechaHoraVenta)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblEstadoVenta)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorEstadoVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pnlNro3Informacion.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblClienteVenta.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblClienteVenta.setText("CLIENTE");

        lblValorClienteVenta.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorClienteVenta.setText("Comercial ABC S.A.C.");

        lblUsuarioVenta.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblUsuarioVenta.setText("USUARIO");

        lblValorUsuarioVenta.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorUsuarioVenta.setText("Juan Carlos Pérez");

        javax.swing.GroupLayout pnlNro3InformacionLayout = new javax.swing.GroupLayout(pnlNro3Informacion);
        pnlNro3Informacion.setLayout(pnlNro3InformacionLayout);
        pnlNro3InformacionLayout.setHorizontalGroup(
            pnlNro3InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNro3InformacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlNro3InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblClienteVenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorClienteVenta, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                    .addComponent(lblUsuarioVenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorUsuarioVenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlNro3InformacionLayout.setVerticalGroup(
            pnlNro3InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlNro3InformacionLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblClienteVenta)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorClienteVenta)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblUsuarioVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorUsuarioVenta)
                .addContainerGap())
        );

        pnlNro4Informacion.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblDocumento.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblDocumento.setText("DOCUMENTO");

        lblValorDocumentoCliente.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorDocumentoCliente.setText("RUC 20601234567");

        javax.swing.GroupLayout pnlNro4InformacionLayout = new javax.swing.GroupLayout(pnlNro4Informacion);
        pnlNro4Informacion.setLayout(pnlNro4InformacionLayout);
        pnlNro4InformacionLayout.setHorizontalGroup(
            pnlNro4InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNro4InformacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlNro4InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblValorDocumentoCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlNro4InformacionLayout.setVerticalGroup(
            pnlNro4InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNro4InformacionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblDocumento)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorDocumentoCliente)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlDetalleVentaSeleccionadaLayout = new javax.swing.GroupLayout(pnlDetalleVentaSeleccionada);
        pnlDetalleVentaSeleccionada.setLayout(pnlDetalleVentaSeleccionadaLayout);
        pnlDetalleVentaSeleccionadaLayout.setHorizontalGroup(
            pnlDetalleVentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator3)
            .addGroup(pnlDetalleVentaSeleccionadaLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(pnlDetalleVentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblInformacionVenta)
                    .addGroup(pnlDetalleVentaSeleccionadaLayout.createSequentialGroup()
                        .addComponent(pnlNro1Informacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlNro2Informacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlNro3Informacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlNro4Informacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        pnlDetalleVentaSeleccionadaLayout.setVerticalGroup(
            pnlDetalleVentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetalleVentaSeleccionadaLayout.createSequentialGroup()
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblInformacionVenta)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetalleVentaSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlNro4Informacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlNro3Informacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlNro2Informacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlNro1Informacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(12, 12, 12))
        );

        pnlMontoTotalVendido.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblMontoTotalVendido.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblMontoTotalVendido.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMontoTotalVendido.setText("TOTAL VENDIDO");

        lblValorMontoTotalVendido.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblValorMontoTotalVendido.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblValorMontoTotalVendido.setText("S/ 13,646.40");

        javax.swing.GroupLayout pnlMontoTotalVendidoLayout = new javax.swing.GroupLayout(pnlMontoTotalVendido);
        pnlMontoTotalVendido.setLayout(pnlMontoTotalVendidoLayout);
        pnlMontoTotalVendidoLayout.setHorizontalGroup(
            pnlMontoTotalVendidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMontoTotalVendidoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMontoTotalVendido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(lblValorMontoTotalVendido, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
        );
        pnlMontoTotalVendidoLayout.setVerticalGroup(
            pnlMontoTotalVendidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMontoTotalVendidoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMontoTotalVendido, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorMontoTotalVendido)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlCantVentasContado.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblVentasContado.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblVentasContado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblVentasContado.setText("VENTAS CONTADO");

        lblCantVentasContado.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblCantVentasContado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCantVentasContado.setText("5");

        javax.swing.GroupLayout pnlCantVentasContadoLayout = new javax.swing.GroupLayout(pnlCantVentasContado);
        pnlCantVentasContado.setLayout(pnlCantVentasContadoLayout);
        pnlCantVentasContadoLayout.setHorizontalGroup(
            pnlCantVentasContadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblVentasContado, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
            .addComponent(lblCantVentasContado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlCantVentasContadoLayout.setVerticalGroup(
            pnlCantVentasContadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCantVentasContadoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblVentasContado)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCantVentasContado)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlCantVentasCredito.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblVentasCredito.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblVentasCredito.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblVentasCredito.setText("VENTAS CRÉDITO");

        lblCantVentasCredito.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblCantVentasCredito.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCantVentasCredito.setText("2");

        javax.swing.GroupLayout pnlCantVentasCreditoLayout = new javax.swing.GroupLayout(pnlCantVentasCredito);
        pnlCantVentasCredito.setLayout(pnlCantVentasCreditoLayout);
        pnlCantVentasCreditoLayout.setHorizontalGroup(
            pnlCantVentasCreditoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblVentasCredito, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
            .addComponent(lblCantVentasCredito, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlCantVentasCreditoLayout.setVerticalGroup(
            pnlCantVentasCreditoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCantVentasCreditoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblVentasCredito)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCantVentasCredito)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        spnlProductosVendidos.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "03. PRODUCTOS VENDIDOS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        tlbProductosVendidos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "CÓDIGO", "PRODUCTO", "CANTIDAD", "P. UNITARIO", "SUBTOTAL"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlProductosVendidos.setViewportView(tlbProductosVendidos);

        pnlMontosVenta.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        txtSubtotal.setEditable(false);
        txtSubtotal.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 12)); // NOI18N
        txtSubtotal.setText("Subtotal");

        txtValorSubtotal.setEditable(false);
        txtValorSubtotal.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtValorSubtotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtValorSubtotal.setText("S/ 123.64");

        txtIGV.setEditable(false);
        txtIGV.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 12)); // NOI18N
        txtIGV.setText("IGV");

        txtValorIGV.setEditable(false);
        txtValorIGV.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtValorIGV.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtValorIGV.setText("S/ 22.26");

        txtTotal.setEditable(false);
        txtTotal.setBackground(new java.awt.Color(153, 51, 0));
        txtTotal.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 12)); // NOI18N
        txtTotal.setText("Total");

        txtValorTotal.setEditable(false);
        txtValorTotal.setBackground(new java.awt.Color(153, 51, 0));
        txtValorTotal.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtValorTotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtValorTotal.setText("S/ 145.90");

        javax.swing.GroupLayout pnlMontosVentaLayout = new javax.swing.GroupLayout(pnlMontosVenta);
        pnlMontosVenta.setLayout(pnlMontosVentaLayout);
        pnlMontosVentaLayout.setHorizontalGroup(
            pnlMontosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMontosVentaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMontosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMontosVentaLayout.createSequentialGroup()
                        .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorSubtotal))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMontosVentaLayout.createSequentialGroup()
                        .addComponent(txtIGV, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorIGV, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMontosVentaLayout.createSequentialGroup()
                        .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorTotal, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlMontosVentaLayout.setVerticalGroup(
            pnlMontosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMontosVentaLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlMontosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtValorSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMontosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtIGV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtValorIGV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMontosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtValorTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFiltrosConsulta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(spnlVentasEncontradas)
                    .addComponent(pnlSuperior, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDetalleVentaSeleccionada, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(pnlCantVentasEncontradas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlMontoTotalVendido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlCantVentasContado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlCantVentasCredito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(spnlProductosVendidos)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlMontosVenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlFiltrosConsulta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(pnlMontoTotalVendido, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(pnlCantVentasContado, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlCantVentasCredito, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlCantVentasEncontradas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnlVentasEncontradas, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDetalleVentaSeleccionada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlMontosVenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(spnlProductosVendidos, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        // TODO add your handling code here:
        consultarHistorial();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
        limpiarFiltros();
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
            java.util.logging.Logger.getLogger(FrmHistorialVentas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmHistorialVentas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmHistorialVentas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmHistorialVentas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmHistorialVentas dialog = new FrmHistorialVentas(new javax.swing.JFrame(), true);
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
    private javax.swing.JComboBox<Cliente> cmbClientes;
    private javax.swing.JComboBox<EstadoVenta> cmbEstados;
    private javax.swing.JComboBox<TipoComprobante> cmbTiposComprobante;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private com.toedter.calendar.JDateChooser jdcFechaDesde;
    private com.toedter.calendar.JDateChooser jdcFechaHasta;
    private javax.swing.JLabel lblCantVentasContado;
    private javax.swing.JLabel lblCantVentasCredito;
    private javax.swing.JLabel lblCantVentasEncontradas;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblClienteVenta;
    private javax.swing.JLabel lblDocumento;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblEstadoVenta;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblFechaDesde;
    private javax.swing.JLabel lblFechaHasta;
    private javax.swing.JLabel lblFechaVenta;
    private javax.swing.JLabel lblFormaPago;
    private javax.swing.JLabel lblHistorialVentas;
    private javax.swing.JLabel lblHora;
    private javax.swing.JLabel lblInformacionVenta;
    private javax.swing.JLabel lblMontoTotalVendido;
    private javax.swing.JLabel lblNombreApellidoUsuarioActual;
    private javax.swing.JLabel lblNroVenta;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTipoComprobante;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblUsuarioVenta;
    private javax.swing.JLabel lblValorClienteVenta;
    private javax.swing.JLabel lblValorDocumentoCliente;
    private javax.swing.JLabel lblValorEstadoVenta;
    private javax.swing.JLabel lblValorFecha;
    private javax.swing.JLabel lblValorFechaHoraVenta;
    private javax.swing.JLabel lblValorFormaPago;
    private javax.swing.JLabel lblValorHora;
    private javax.swing.JLabel lblValorMontoTotalVendido;
    private javax.swing.JLabel lblValorNroVenta;
    private javax.swing.JLabel lblValorUsuarioVenta;
    private javax.swing.JLabel lblVentasContado;
    private javax.swing.JLabel lblVentasCredito;
    private javax.swing.JLabel lblVentasEncontradas;
    private javax.swing.JPanel pnlCantVentasContado;
    private javax.swing.JPanel pnlCantVentasCredito;
    private javax.swing.JPanel pnlCantVentasEncontradas;
    private javax.swing.JPanel pnlDetalleVentaSeleccionada;
    private javax.swing.JPanel pnlFiltrosConsulta;
    private javax.swing.JPanel pnlMontoTotalVendido;
    private javax.swing.JPanel pnlMontosVenta;
    private javax.swing.JPanel pnlNro1Informacion;
    private javax.swing.JPanel pnlNro2Informacion;
    private javax.swing.JPanel pnlNro3Informacion;
    private javax.swing.JPanel pnlNro4Informacion;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JScrollPane spnlProductosVendidos;
    private javax.swing.JScrollPane spnlVentasEncontradas;
    private javax.swing.JTable tblVentasEncontradas;
    private javax.swing.JTable tlbProductosVendidos;
    private javax.swing.JTextField txtIGV;
    private javax.swing.JTextField txtSubtotal;
    private javax.swing.JTextField txtTotal;
    private javax.swing.JTextField txtValorIGV;
    private javax.swing.JTextField txtValorSubtotal;
    private javax.swing.JTextField txtValorTotal;
    // End of variables declaration//GEN-END:variables
}
