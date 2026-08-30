package com.ferronor.sic.compras.vista;

import com.ferronor.sic.compras.logica.CompraService;
import com.ferronor.sic.compras.modelo.Compra;
import com.ferronor.sic.compras.modelo.DetalleCompra;
import com.ferronor.sic.compras.modelo.EstadoCuenta;
import com.ferronor.sic.compras.modelo.dto.CompraConsulta;
import com.ferronor.sic.maestros.logica.FormaPagoService;
import com.ferronor.sic.maestros.logica.ProductoService;
import com.ferronor.sic.maestros.logica.ProveedorService;
import com.ferronor.sic.maestros.modelo.FormaPago;
import com.ferronor.sic.maestros.modelo.Producto;
import com.ferronor.sic.maestros.modelo.Proveedor;
import com.ferronor.sic.seguridad.logica.UsuarioService;
import com.ferronor.sic.seguridad.modelo.Usuario;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;

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
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class FrmHistorialCompras extends javax.swing.JDialog {

    // ============================================================
    // SERVICES
    // ============================================================
    private final CompraService compraService
            = ServiceFactory.compraService();

    private final ProveedorService proveedorService
            = ServiceFactory.proveedorService();

    private final FormaPagoService formaPagoService
            = ServiceFactory.formaPagoService();

    private final ProductoService productoService
            = ServiceFactory.productoService();

    private final UsuarioService usuarioService
            = ServiceFactory.usuarioService();

    // ============================================================
    // ESTADO
    // ============================================================
    private List<CompraConsulta> comprasConsultadas
            = new ArrayList<>();

    private CompraConsulta compraConsultaSeleccionada;

    private Compra compraSeleccionada;

    private final Map<Integer, Boolean> formasPagoCredito
            = new HashMap<>();

    // ============================================================
    // MODELOS DE TABLA
    // ============================================================
    private DefaultTableModel modeloCompras;

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

    private static final DecimalFormat FORMATO_MONEDA_4
            = new DecimalFormat("#,##0.0000");

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    public FrmHistorialCompras(
            java.awt.Frame parent,
            boolean modal) {

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
    }

    // ============================================================
    // CABECERA
    // ============================================================
    private void actualizarCabecera() {

        SesionUsuario sesion
                = SesionUsuario.actual();

        if (sesion != null) {

            lblNombreApellidoUsuarioActual.setText(
                    sesion.getNombreCompleto()
            );

        } else {

            lblNombreApellidoUsuarioActual.setText(
                    "-"
            );
        }

        LocalDateTime ahora
                = LocalDateTime.now();

        lblValorFecha.setText(
                ahora.format(FORMATO_FECHA)
        );

        lblValorHora.setText(
                ahora.format(FORMATO_HORA)
        );

        lblMontoTotalVendido.setText(
                "TOTAL COMPRADO"
        );
    }

    // ============================================================
    // TABLAS
    // ============================================================
    private void configurarTablas() {

        modeloCompras
                = new DefaultTableModel(
                        new Object[]{
                            "N°. COMPRA",
                            "FECHA",
                            "FACTURA",
                            "PROVEEDOR",
                            "RUC",
                            "FORMA PAGO",
                            "TOTAL",
                            "ORDEN COMPRA"
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

        modeloProductos
                = new DefaultTableModel(
                        new Object[]{
                            "CÓDIGO",
                            "PRODUCTO",
                            "CANTIDAD",
                            "COSTO UNITARIO",
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

        tblComprasEncontradas.setModel(
                modeloCompras
        );

        tlbProductosComprados.setModel(
                modeloProductos
        );

        tblComprasEncontradas.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tlbProductosComprados.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        Font fuenteTabla
                = new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        9
                );

        Font fuenteCabecera
                = new Font(
                        "Segoe UI",
                        Font.BOLD,
                        9
                );

        tblComprasEncontradas.setFont(
                fuenteTabla
        );

        tblComprasEncontradas.setRowHeight(
                23
        );

        tblComprasEncontradas
                .getTableHeader()
                .setFont(
                        fuenteCabecera
                );

        tlbProductosComprados.setFont(
                fuenteTabla
        );

        tlbProductosComprados.setRowHeight(
                23
        );

        tlbProductosComprados
                .getTableHeader()
                .setFont(
                        fuenteCabecera
                );

        configurarAlineacionesTablas();
    }

    private void configurarAlineacionesTablas() {

        DefaultTableCellRenderer centrado
                = new DefaultTableCellRenderer();

        centrado.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        DefaultTableCellRenderer derecha
                = new DefaultTableCellRenderer();

        derecha.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        tblComprasEncontradas
                .getColumnModel()
                .getColumn(0)
                .setCellRenderer(
                        centrado
                );

        tblComprasEncontradas
                .getColumnModel()
                .getColumn(1)
                .setCellRenderer(
                        centrado
                );

        tblComprasEncontradas
                .getColumnModel()
                .getColumn(6)
                .setCellRenderer(
                        derecha
                );

        tlbProductosComprados
                .getColumnModel()
                .getColumn(2)
                .setCellRenderer(
                        derecha
                );

        tlbProductosComprados
                .getColumnModel()
                .getColumn(3)
                .setCellRenderer(
                        derecha
                );

        tlbProductosComprados
                .getColumnModel()
                .getColumn(4)
                .setCellRenderer(
                        derecha
                );
    }

    // ============================================================
    // COMBOS
    // ============================================================
    private void configurarCombos() {

        configurarComboProveedores();

        configurarComboFormaPago();

        configurarComboOrdenCompra();
    }

    private void configurarComboProveedores() {

        DefaultComboBoxModel<Proveedor> modelo
                = new DefaultComboBoxModel<>();

        modelo.addElement(null);

        List<Proveedor> proveedores
                = proveedorService.listarActivos();

        for (Proveedor proveedor
                : proveedores) {

            modelo.addElement(proveedor);
        }

        cmbProveedores.setModel(modelo);

        cmbProveedores.setRenderer(
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

                    setText(
                            "Todos los proveedores"
                    );

                } else if (value instanceof Proveedor proveedor) {

                    setText(
                            proveedor.getRazonSocial()
                    );
                }

                return this;
            }
        });

        cmbProveedores.setSelectedItem(null);
    }

    private void configurarComboFormaPago() {

        DefaultComboBoxModel<FormaPago> modelo
                = new DefaultComboBoxModel<>();

        modelo.addElement(null);

        List<FormaPago> formasPago
                = formaPagoService.listar();

        for (FormaPago formaPago
                : formasPago) {

            modelo.addElement(formaPago);
        }

        cmbFormaPago.setModel(modelo);

        cmbFormaPago.setRenderer(
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

                    setText("Todas");

                } else if (value instanceof FormaPago formaPago) {

                    setText(
                            formaPago.getNombre()
                    );
                }

                return this;
            }
        });

        cmbFormaPago.setSelectedItem(null);
    }

    private void configurarComboOrdenCompra() {

        DefaultComboBoxModel<String> modelo
                = new DefaultComboBoxModel<>();

        modelo.addElement("Todos");
        modelo.addElement("Con Orden de Compra");
        modelo.addElement("Compra Directa");

        cmbOrdenesCompra.setModel(modelo);

        cmbOrdenesCompra.setSelectedItem(
                "Todos"
        );
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

        tblComprasEncontradas
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (e.getValueIsAdjusting()) {
                        return;
                    }

                    seleccionarCompraDesdeTabla();
                });
    }

    private void seleccionarCompraDesdeTabla() {

        int fila
                = tblComprasEncontradas
                        .getSelectedRow();

        if (fila < 0) {

            limpiarDetalle();

            return;
        }

        if (fila >= comprasConsultadas.size()) {

            limpiarDetalle();

            return;
        }

        compraConsultaSeleccionada
                = comprasConsultadas.get(
                        fila
                );

        cargarDetalleCompra(
                compraConsultaSeleccionada
                        .getIdCompra()
        );
    }

    // ============================================================
    // ESTADO INICIAL
    // ============================================================
    private void configurarEstadoInicial() {

        comprasConsultadas.clear();

        compraConsultaSeleccionada
                = null;

        compraSeleccionada
                = null;

        jdcFechaDesde.setDate(null);

        jdcFechaHasta.setDate(null);

        cmbProveedores.setSelectedItem(
                null
        );

        cmbFormaPago.setSelectedItem(
                null
        );

        cmbOrdenesCompra.setSelectedItem(
                "Todos"
        );

        limpiarTablas();

        limpiarDetalle();
    }

    // ============================================================
    // CONSULTA
    // ============================================================
    private void consultarHistorial() {

        LocalDate fechaDesde
                = obtenerFecha(
                        jdcFechaDesde
                );

        LocalDate fechaHasta
                = obtenerFecha(
                        jdcFechaHasta
                );

        if (fechaDesde != null
                && fechaHasta != null
                && fechaDesde.isAfter(
                        fechaHasta
                )) {

            JOptionPane.showMessageDialog(
                    this,
                    "La fecha desde no puede ser "
                    + "posterior a la fecha hasta.",
                    "Filtro de fechas",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        Proveedor proveedor
                = (Proveedor) cmbProveedores
                        .getSelectedItem();

        FormaPago formaPago
                = (FormaPago) cmbFormaPago
                        .getSelectedItem();

        Integer idProveedor
                = proveedor != null
                        ? proveedor.getIdProveedor()
                        : null;

        Integer idFormaPago
                = formaPago != null
                        ? formaPago.getIdFormaPago()
                        : null;

        Boolean conOrdenCompra
                = obtenerFiltroOrdenCompra();

        try {

            comprasConsultadas
                    = compraService
                            .consultarHistorial(
                                    fechaDesde,
                                    fechaHasta,
                                    idProveedor,
                                    idFormaPago,
                                    conOrdenCompra
                            );

            cargarTablaCompras();

            actualizarResumen();

            limpiarDetalle();

        } catch (RuntimeException ex) {

            comprasConsultadas
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
    // FILTRO ORDEN DE COMPRA
    // ============================================================
    private Boolean obtenerFiltroOrdenCompra() {

        String seleccion
                = (String) cmbOrdenesCompra
                        .getSelectedItem();

        if ("Con Orden de Compra".equals(
                seleccion)) {

            return Boolean.TRUE;
        }

        if ("Compra Directa".equals(
                seleccion)) {

            return Boolean.FALSE;
        }

        return null;
    }

    // ============================================================
    // TABLA DE COMPRAS
    // ============================================================
    private void cargarTablaCompras() {

        modeloCompras.setRowCount(0);

        for (CompraConsulta compra
                : comprasConsultadas) {

            modeloCompras.addRow(
                    new Object[]{
                        compra.getIdCompra(),
                        formatearFechaHora(
                                compra.getFecha()
                        ),
                        valorFactura(
                                compra
                        ),
                        compra
                                .getRazonSocialProveedor(),
                        compra
                                .getRucProveedor(),
                        compra
                                .getNombreFormaPago(),
                        "S/ "
                        + formatearMoneda(
                                compra.getTotal()
                        ),
                        obtenerOrigenCompra(
                                compra
                        )
                    }
            );
        }
    }

    private String valorFactura(
            CompraConsulta compra) {

        String factura
                = compra.getNumeroFactura();

        if (factura == null
                || factura.isBlank()) {

            return "-";
        }

        return factura;
    }

    private String obtenerOrigenCompra(
            CompraConsulta compra) {

        return compra.getIdOrdenCompra() != null
                ? "ORDEN DE COMPRA"
                : "DIRECTA";
    }

    // ============================================================
    // DETALLE
    // ============================================================
    private void cargarDetalleCompra(
            int idCompra) {

        try {

            Compra compra
                    = compraService.buscarPorId(
                            idCompra
                    );

            if (compra == null) {

                limpiarDetalle();

                JOptionPane.showMessageDialog(
                        this,
                        "No se encontró la compra seleccionada.",
                        "Compra no encontrada",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            compraSeleccionada
                    = compra;

            actualizarInformacionCompra(
                    compra
            );

            cargarProductosComprados(
                    compra
            );

            actualizarMontosCompra(
                    compra
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

    private void actualizarInformacionCompra(
            Compra compra) {

        lblValorNroCompra.setText(
                String.valueOf(
                        compra.getIdCompra()
                )
        );

        lblValorPlazo.setText(
                formatearPlazo(
                        compra.getPlazoDias()
                )
        );

        lblValorFechaHoraCompra.setText(
                compra.getFecha() != null
                ? compra.getFecha()
                        .format(
                                FORMATO_FECHA_HORA
                        )
                : "-"
        );

        Proveedor proveedor
                = proveedorService.buscarPorId(
                        compra.getIdProveedor()
                );

        if (proveedor != null) {

            lblValorProveedorCompra.setText(
                    proveedor.getRazonSocial()
            );

            lblValorRUC.setText(
                    proveedor.getRuc() != null
                    ? proveedor.getRuc()
                    : "-"
            );

        } else {

            lblValorProveedorCompra.setText(
                    "-"
            );

            lblValorRUC.setText(
                    "-"
            );
        }

        lblValorFactura.setText(
                compra.getNumeroFactura() != null
                && !compra.getNumeroFactura()
                        .isBlank()
                        ? compra.getNumeroFactura()
                        : "-"
        );

        FormaPago formaPago
                = formaPagoService.buscarPorId(
                        compra.getIdFormaPago()
                );

        if (formaPago != null) {

            lblValorFormaPago.setText(
                    formaPago.getNombre()
            );

        } else {

            lblValorFormaPago.setText(
                    "-"
            );
        }

        Usuario usuario
                = usuarioService.buscarPorId(
                        compra.getIdUsuario()
                );

        if (usuario != null) {

            lblValorUsuarioCompra.setText(
                    usuario.getNombreCompleto()
            );

        } else {

            lblValorUsuarioCompra.setText(
                    "-"
            );
        }
    }

    private String formatearPlazo(
            Integer plazoDias) {

        if (plazoDias == null) {
            return "—";
        }

        return plazoDias
                + (plazoDias == 1
                        ? " día"
                        : " días");
    }

    // ============================================================
    // PRODUCTOS
    // ============================================================
    private void cargarProductosComprados(
            Compra compra) {

        modeloProductos.setRowCount(0);

        if (compra.getDetalles() == null) {
            return;
        }

        for (DetalleCompra detalle
                : compra.getDetalles()) {

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
                        + formatearMoneda4(
                                detalle.getCostoUnitario()
                        ),
                        "S/ "
                        + formatearMoneda4(
                                detalle.getSubtotal()
                        )
                    }
            );
        }
    }

    // ============================================================
    // MONTOS
    // ============================================================
    private void actualizarMontosCompra(
            Compra compra) {

        txtValorSubtotal.setText(
                "S/ "
                + formatearMoneda(
                        compra.getSubtotal()
                )
        );

        txtValorIGV.setText(
                "S/ "
                + formatearMoneda(
                        compra.getIgv()
                )
        );

        txtValorTotal.setText(
                "S/ "
                + formatearMoneda(
                        compra.getTotal()
                )
        );
    }

    // ============================================================
    // RESUMEN
    // ============================================================
    private void actualizarResumen() {

        int cantidad
                = comprasConsultadas.size();

        BigDecimal total
                = BigDecimal.ZERO;

        int contado = 0;

        int credito = 0;

        for (CompraConsulta compra
                : comprasConsultadas) {

            if (compra.getTotal() != null) {

                total
                        = total.add(
                                compra.getTotal()
                        );
            }

            Boolean esCredito
                    = formasPagoCredito.get(
                            compra.getIdFormaPago()
                    );

            if (Boolean.TRUE.equals(
                    esCredito
            )) {

                credito++;

            } else {

                contado++;
            }
        }

        lblCantComprasEncontradas.setText(
                String.valueOf(
                        cantidad
                )
        );

        lblValorMontoTotalComprado.setText(
                "S/ "
                + formatearMoneda(
                        total
                )
        );

        lblCantComprasContado.setText(
                String.valueOf(
                        contado
                )
        );

        lblCantComprasCredito.setText(
                String.valueOf(
                        credito
                )
        );
    }

    // ============================================================
    // LIMPIAR
    // ============================================================
    private void limpiarFiltros() {

        jdcFechaDesde.setDate(null);

        jdcFechaHasta.setDate(null);

        cmbProveedores.setSelectedItem(
                null
        );

        cmbFormaPago.setSelectedItem(
                null
        );

        cmbOrdenesCompra.setSelectedItem(
                "Todos"
        );

        consultarHistorial();
    }

    private void limpiarDetalle() {

        compraConsultaSeleccionada
                = null;

        compraSeleccionada
                = null;

        lblValorNroCompra.setText(
                "-"
        );

        lblValorPlazo.setText(
                "-"
        );

        lblValorFechaHoraCompra.setText(
                "-"
        );

        lblValorProveedorCompra.setText(
                "-"
        );

        lblValorFactura.setText(
                "-"
        );

        lblValorRUC.setText(
                "-"
        );

        lblValorFormaPago.setText(
                "-"
        );

        lblValorUsuarioCompra.setText(
                "-"
        );

        txtValorSubtotal.setText(
                "S/ 0.00"
        );

        txtValorIGV.setText(
                "S/ 0.00"
        );

        txtValorTotal.setText(
                "S/ 0.00"
        );

        modeloProductos.setRowCount(0);
    }

    private void limpiarTablas() {

        modeloCompras.setRowCount(0);

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

    private String formatearMoneda4(
            BigDecimal valor) {

        if (valor == null) {
            return "0.0000";
        }

        return FORMATO_MONEDA_4.format(
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

        if (ex.getMessage() == null
                || ex.getMessage().isBlank()) {

            return "Se produjo un error inesperado.";
        }

        return ex.getMessage();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSuperior = new javax.swing.JPanel();
        lblHistorialCompras = new javax.swing.JLabel();
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
        lblProveedor = new javax.swing.JLabel();
        cmbProveedores = new javax.swing.JComboBox<>();
        lblFormaPago = new javax.swing.JLabel();
        cmbFormaPago = new javax.swing.JComboBox<>();
        lblTipoComprobante = new javax.swing.JLabel();
        cmbOrdenesCompra = new javax.swing.JComboBox<>();
        jSeparator2 = new javax.swing.JSeparator();
        btnConsultar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        pnlCantComprasEncontradas = new javax.swing.JPanel();
        lblComprasEncontradas = new javax.swing.JLabel();
        lblCantComprasEncontradas = new javax.swing.JLabel();
        spnlComprasEncontradas = new javax.swing.JScrollPane();
        tblComprasEncontradas = new javax.swing.JTable();
        pnlDetalleCompraSeleccionada = new javax.swing.JPanel();
        jSeparator3 = new javax.swing.JSeparator();
        lblInformacionCompra = new javax.swing.JLabel();
        pnlNro1Informacion = new javax.swing.JPanel();
        lblNroCompra = new javax.swing.JLabel();
        lblValorNroCompra = new javax.swing.JLabel();
        lblPlazo = new javax.swing.JLabel();
        lblValorPlazo = new javax.swing.JLabel();
        pnlNro2Informacion = new javax.swing.JPanel();
        lblFechaCompra = new javax.swing.JLabel();
        lblValorFechaHoraCompra = new javax.swing.JLabel();
        lblProveedorCompra = new javax.swing.JLabel();
        lblValorProveedorCompra = new javax.swing.JLabel();
        pnlNro3Informacion = new javax.swing.JPanel();
        lblFactura = new javax.swing.JLabel();
        lblValorFactura = new javax.swing.JLabel();
        lblRUC = new javax.swing.JLabel();
        lblValorRUC = new javax.swing.JLabel();
        pnlNro4Informacion = new javax.swing.JPanel();
        lblFormaPagoCompra = new javax.swing.JLabel();
        lblValorFormaPago = new javax.swing.JLabel();
        lblUsuarioCompra = new javax.swing.JLabel();
        lblValorUsuarioCompra = new javax.swing.JLabel();
        pnlMontoTotalComprado = new javax.swing.JPanel();
        lblMontoTotalVendido = new javax.swing.JLabel();
        lblValorMontoTotalComprado = new javax.swing.JLabel();
        pnlCantComprasContado = new javax.swing.JPanel();
        lblComprasContado = new javax.swing.JLabel();
        lblCantComprasContado = new javax.swing.JLabel();
        pnlCantComprasCredito = new javax.swing.JPanel();
        lblComprasCredito = new javax.swing.JLabel();
        lblCantComprasCredito = new javax.swing.JLabel();
        spnlProductosComprados = new javax.swing.JScrollPane();
        tlbProductosComprados = new javax.swing.JTable();
        pnlMontosCompra = new javax.swing.JPanel();
        txtSubtotal = new javax.swing.JTextField();
        txtValorSubtotal = new javax.swing.JTextField();
        txtIGV = new javax.swing.JTextField();
        txtValorIGV = new javax.swing.JTextField();
        txtTotal = new javax.swing.JTextField();
        txtValorTotal = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblHistorialCompras.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 24)); // NOI18N
        lblHistorialCompras.setText("HISTORIAL DE COMPRAS");

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
        lblSubtitulo.setText("Consulta y seguimiento del historial de operaciones de abastecimiento ");

        javax.swing.GroupLayout pnlSuperiorLayout = new javax.swing.GroupLayout(pnlSuperior);
        pnlSuperior.setLayout(pnlSuperiorLayout);
        pnlSuperiorLayout.setHorizontalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSuperiorLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblSubtitulo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblHistorialCompras, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                        .addComponent(lblHistorialCompras, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSubtitulo)))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlFiltrosConsulta.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. FILTROS DE CONSULTA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblFechaDesde.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFechaDesde.setText("FECHA DESDE");

        lblFechaHasta.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFechaHasta.setText("FECHA HASTA");

        lblProveedor.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblProveedor.setText("PROVEEDOR");

        lblFormaPago.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFormaPago.setText("FORMA DE PAGO");

        lblTipoComprobante.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTipoComprobante.setText("ORDEN DE COMPRA");

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
                    .addComponent(cmbProveedores, 0, 192, Short.MAX_VALUE)
                    .addComponent(lblProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbFormaPago, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblFormaPago, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFiltrosConsultaLayout.createSequentialGroup()
                        .addComponent(cmbOrdenesCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLimpiar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnConsultar))
                    .addComponent(lblTipoComprobante, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(28, Short.MAX_VALUE))
            .addComponent(jSeparator2)
        );
        pnlFiltrosConsultaLayout.setVerticalGroup(
            pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFiltrosConsultaLayout.createSequentialGroup()
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFechaDesde)
                    .addComponent(lblFechaHasta)
                    .addComponent(lblProveedor)
                    .addComponent(lblFormaPago)
                    .addComponent(lblTipoComprobante))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jdcFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jdcFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlFiltrosConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cmbProveedores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbFormaPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbOrdenesCompra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnConsultar)))
                .addGap(11, 11, 11)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pnlCantComprasEncontradas.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblComprasEncontradas.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblComprasEncontradas.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblComprasEncontradas.setText("COMPRAS ENCONTRADAS");

        lblCantComprasEncontradas.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblCantComprasEncontradas.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCantComprasEncontradas.setText("7");

        javax.swing.GroupLayout pnlCantComprasEncontradasLayout = new javax.swing.GroupLayout(pnlCantComprasEncontradas);
        pnlCantComprasEncontradas.setLayout(pnlCantComprasEncontradasLayout);
        pnlCantComprasEncontradasLayout.setHorizontalGroup(
            pnlCantComprasEncontradasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCantComprasEncontradasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCantComprasEncontradasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblComprasEncontradas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlCantComprasEncontradasLayout.createSequentialGroup()
                        .addComponent(lblCantComprasEncontradas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        pnlCantComprasEncontradasLayout.setVerticalGroup(
            pnlCantComprasEncontradasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCantComprasEncontradasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblComprasEncontradas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCantComprasEncontradas)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        spnlComprasEncontradas.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. COMPRAS ENCONTRADAS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        tblComprasEncontradas.setModel(new javax.swing.table.DefaultTableModel(
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
        spnlComprasEncontradas.setViewportView(tblComprasEncontradas);

        pnlDetalleCompraSeleccionada.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "03. DETALLE DE LA COMPRA SELECCIONADA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblInformacionCompra.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 12)); // NOI18N
        lblInformacionCompra.setText("Información de la Compra");

        pnlNro1Informacion.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblNroCompra.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblNroCompra.setText("N°. COMPRA");

        lblValorNroCompra.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorNroCompra.setText("123");

        lblPlazo.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblPlazo.setText("PLAZO");

        lblValorPlazo.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorPlazo.setText("45 días");

        javax.swing.GroupLayout pnlNro1InformacionLayout = new javax.swing.GroupLayout(pnlNro1Informacion);
        pnlNro1Informacion.setLayout(pnlNro1InformacionLayout);
        pnlNro1InformacionLayout.setHorizontalGroup(
            pnlNro1InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNro1InformacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlNro1InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNroCompra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorNroCompra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblPlazo, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                    .addComponent(lblValorPlazo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlNro1InformacionLayout.setVerticalGroup(
            pnlNro1InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlNro1InformacionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblNroCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorNroCompra)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblPlazo, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorPlazo)
                .addContainerGap())
        );

        pnlNro2Informacion.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblFechaCompra.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblFechaCompra.setText("FECHA");

        lblValorFechaHoraCompra.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorFechaHoraCompra.setText("27/08/2026 16:40 PM");

        lblProveedorCompra.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblProveedorCompra.setText("PROVEEDOR");

        lblValorProveedorCompra.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorProveedorCompra.setText("Importaciones del Norte S.A.C.");

        javax.swing.GroupLayout pnlNro2InformacionLayout = new javax.swing.GroupLayout(pnlNro2Informacion);
        pnlNro2Informacion.setLayout(pnlNro2InformacionLayout);
        pnlNro2InformacionLayout.setHorizontalGroup(
            pnlNro2InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNro2InformacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlNro2InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFechaCompra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorFechaHoraCompra, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                    .addComponent(lblProveedorCompra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorProveedorCompra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlNro2InformacionLayout.setVerticalGroup(
            pnlNro2InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlNro2InformacionLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblFechaCompra)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorFechaHoraCompra)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblProveedorCompra)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorProveedorCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pnlNro3Informacion.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblFactura.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblFactura.setText("FACTURA");

        lblValorFactura.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorFactura.setText("F001-45819");

        lblRUC.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblRUC.setText("RUC");

        lblValorRUC.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorRUC.setText("20587412369");

        javax.swing.GroupLayout pnlNro3InformacionLayout = new javax.swing.GroupLayout(pnlNro3Informacion);
        pnlNro3Informacion.setLayout(pnlNro3InformacionLayout);
        pnlNro3InformacionLayout.setHorizontalGroup(
            pnlNro3InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNro3InformacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlNro3InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFactura, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorFactura, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                    .addComponent(lblRUC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorRUC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlNro3InformacionLayout.setVerticalGroup(
            pnlNro3InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlNro3InformacionLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblFactura, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorFactura)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblRUC, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorRUC)
                .addContainerGap())
        );

        pnlNro4Informacion.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblFormaPagoCompra.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblFormaPagoCompra.setText("FORMA PAGO");

        lblValorFormaPago.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorFormaPago.setText("Crédito");

        lblUsuarioCompra.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblUsuarioCompra.setText("USUARIO");

        lblValorUsuarioCompra.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorUsuarioCompra.setText("Juan Carlos Pérez");

        javax.swing.GroupLayout pnlNro4InformacionLayout = new javax.swing.GroupLayout(pnlNro4Informacion);
        pnlNro4Informacion.setLayout(pnlNro4InformacionLayout);
        pnlNro4InformacionLayout.setHorizontalGroup(
            pnlNro4InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNro4InformacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlNro4InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblValorFormaPago, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblFormaPagoCompra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblUsuarioCompra, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorUsuarioCompra, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlNro4InformacionLayout.setVerticalGroup(
            pnlNro4InformacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNro4InformacionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblFormaPagoCompra)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorFormaPago)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblUsuarioCompra)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorUsuarioCompra)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlDetalleCompraSeleccionadaLayout = new javax.swing.GroupLayout(pnlDetalleCompraSeleccionada);
        pnlDetalleCompraSeleccionada.setLayout(pnlDetalleCompraSeleccionadaLayout);
        pnlDetalleCompraSeleccionadaLayout.setHorizontalGroup(
            pnlDetalleCompraSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator3)
            .addGroup(pnlDetalleCompraSeleccionadaLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(pnlDetalleCompraSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblInformacionCompra)
                    .addGroup(pnlDetalleCompraSeleccionadaLayout.createSequentialGroup()
                        .addComponent(pnlNro1Informacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlNro2Informacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlNro3Informacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlNro4Informacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        pnlDetalleCompraSeleccionadaLayout.setVerticalGroup(
            pnlDetalleCompraSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetalleCompraSeleccionadaLayout.createSequentialGroup()
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblInformacionCompra)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetalleCompraSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlNro4Informacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlNro3Informacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlNro2Informacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlNro1Informacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(12, 12, 12))
        );

        pnlMontoTotalComprado.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblMontoTotalVendido.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblMontoTotalVendido.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMontoTotalVendido.setText("TOTAL COMPRADO");

        lblValorMontoTotalComprado.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblValorMontoTotalComprado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblValorMontoTotalComprado.setText("S/ 13,646.40");

        javax.swing.GroupLayout pnlMontoTotalCompradoLayout = new javax.swing.GroupLayout(pnlMontoTotalComprado);
        pnlMontoTotalComprado.setLayout(pnlMontoTotalCompradoLayout);
        pnlMontoTotalCompradoLayout.setHorizontalGroup(
            pnlMontoTotalCompradoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMontoTotalCompradoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMontoTotalVendido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(lblValorMontoTotalComprado, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
        );
        pnlMontoTotalCompradoLayout.setVerticalGroup(
            pnlMontoTotalCompradoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMontoTotalCompradoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMontoTotalVendido, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorMontoTotalComprado)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlCantComprasContado.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblComprasContado.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblComprasContado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblComprasContado.setText("COMPRAS CONTADO");

        lblCantComprasContado.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblCantComprasContado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCantComprasContado.setText("5");

        javax.swing.GroupLayout pnlCantComprasContadoLayout = new javax.swing.GroupLayout(pnlCantComprasContado);
        pnlCantComprasContado.setLayout(pnlCantComprasContadoLayout);
        pnlCantComprasContadoLayout.setHorizontalGroup(
            pnlCantComprasContadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblComprasContado, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
            .addComponent(lblCantComprasContado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlCantComprasContadoLayout.setVerticalGroup(
            pnlCantComprasContadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCantComprasContadoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblComprasContado)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCantComprasContado)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlCantComprasCredito.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblComprasCredito.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblComprasCredito.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblComprasCredito.setText("COMPRAS CRÉDITO");

        lblCantComprasCredito.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblCantComprasCredito.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCantComprasCredito.setText("2");

        javax.swing.GroupLayout pnlCantComprasCreditoLayout = new javax.swing.GroupLayout(pnlCantComprasCredito);
        pnlCantComprasCredito.setLayout(pnlCantComprasCreditoLayout);
        pnlCantComprasCreditoLayout.setHorizontalGroup(
            pnlCantComprasCreditoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblComprasCredito, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
            .addComponent(lblCantComprasCredito, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlCantComprasCreditoLayout.setVerticalGroup(
            pnlCantComprasCreditoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCantComprasCreditoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblComprasCredito)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCantComprasCredito)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        spnlProductosComprados.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "03. PRODUCTOS COMPRADOS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        tlbProductosComprados.setModel(new javax.swing.table.DefaultTableModel(
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
        spnlProductosComprados.setViewportView(tlbProductosComprados);

        pnlMontosCompra.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

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

        javax.swing.GroupLayout pnlMontosCompraLayout = new javax.swing.GroupLayout(pnlMontosCompra);
        pnlMontosCompra.setLayout(pnlMontosCompraLayout);
        pnlMontosCompraLayout.setHorizontalGroup(
            pnlMontosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMontosCompraLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMontosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMontosCompraLayout.createSequentialGroup()
                        .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorSubtotal))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMontosCompraLayout.createSequentialGroup()
                        .addComponent(txtIGV, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorIGV, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMontosCompraLayout.createSequentialGroup()
                        .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorTotal, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlMontosCompraLayout.setVerticalGroup(
            pnlMontosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMontosCompraLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlMontosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtValorSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMontosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtIGV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtValorIGV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMontosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                    .addComponent(spnlComprasEncontradas)
                    .addComponent(pnlSuperior, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDetalleCompraSeleccionada, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(pnlCantComprasEncontradas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlMontoTotalComprado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlCantComprasContado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlCantComprasCredito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(spnlProductosComprados)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlMontosCompra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                    .addComponent(pnlMontoTotalComprado, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(pnlCantComprasContado, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlCantComprasCredito, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlCantComprasEncontradas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnlComprasEncontradas, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDetalleCompraSeleccionada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlMontosCompra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(spnlProductosComprados, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
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
            java.util.logging.Logger.getLogger(FrmHistorialCompras.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmHistorialCompras.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmHistorialCompras.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmHistorialCompras.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmHistorialCompras dialog = new FrmHistorialCompras(new javax.swing.JFrame(), true);
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
    private javax.swing.JComboBox<FormaPago> cmbFormaPago;
    private javax.swing.JComboBox<String> cmbOrdenesCompra;
    private javax.swing.JComboBox<Proveedor> cmbProveedores;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private com.toedter.calendar.JDateChooser jdcFechaDesde;
    private com.toedter.calendar.JDateChooser jdcFechaHasta;
    private javax.swing.JLabel lblCantComprasContado;
    private javax.swing.JLabel lblCantComprasCredito;
    private javax.swing.JLabel lblCantComprasEncontradas;
    private javax.swing.JLabel lblComprasContado;
    private javax.swing.JLabel lblComprasCredito;
    private javax.swing.JLabel lblComprasEncontradas;
    private javax.swing.JLabel lblFactura;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblFechaCompra;
    private javax.swing.JLabel lblFechaDesde;
    private javax.swing.JLabel lblFechaHasta;
    private javax.swing.JLabel lblFormaPago;
    private javax.swing.JLabel lblFormaPagoCompra;
    private javax.swing.JLabel lblHistorialCompras;
    private javax.swing.JLabel lblHora;
    private javax.swing.JLabel lblInformacionCompra;
    private javax.swing.JLabel lblMontoTotalVendido;
    private javax.swing.JLabel lblNombreApellidoUsuarioActual;
    private javax.swing.JLabel lblNroCompra;
    private javax.swing.JLabel lblPlazo;
    private javax.swing.JLabel lblProveedor;
    private javax.swing.JLabel lblProveedorCompra;
    private javax.swing.JLabel lblRUC;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTipoComprobante;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblUsuarioCompra;
    private javax.swing.JLabel lblValorFactura;
    private javax.swing.JLabel lblValorFecha;
    private javax.swing.JLabel lblValorFechaHoraCompra;
    private javax.swing.JLabel lblValorFormaPago;
    private javax.swing.JLabel lblValorHora;
    private javax.swing.JLabel lblValorMontoTotalComprado;
    private javax.swing.JLabel lblValorNroCompra;
    private javax.swing.JLabel lblValorPlazo;
    private javax.swing.JLabel lblValorProveedorCompra;
    private javax.swing.JLabel lblValorRUC;
    private javax.swing.JLabel lblValorUsuarioCompra;
    private javax.swing.JPanel pnlCantComprasContado;
    private javax.swing.JPanel pnlCantComprasCredito;
    private javax.swing.JPanel pnlCantComprasEncontradas;
    private javax.swing.JPanel pnlDetalleCompraSeleccionada;
    private javax.swing.JPanel pnlFiltrosConsulta;
    private javax.swing.JPanel pnlMontoTotalComprado;
    private javax.swing.JPanel pnlMontosCompra;
    private javax.swing.JPanel pnlNro1Informacion;
    private javax.swing.JPanel pnlNro2Informacion;
    private javax.swing.JPanel pnlNro3Informacion;
    private javax.swing.JPanel pnlNro4Informacion;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JScrollPane spnlComprasEncontradas;
    private javax.swing.JScrollPane spnlProductosComprados;
    private javax.swing.JTable tblComprasEncontradas;
    private javax.swing.JTable tlbProductosComprados;
    private javax.swing.JTextField txtIGV;
    private javax.swing.JTextField txtSubtotal;
    private javax.swing.JTextField txtTotal;
    private javax.swing.JTextField txtValorIGV;
    private javax.swing.JTextField txtValorSubtotal;
    private javax.swing.JTextField txtValorTotal;
    // End of variables declaration//GEN-END:variables
}
