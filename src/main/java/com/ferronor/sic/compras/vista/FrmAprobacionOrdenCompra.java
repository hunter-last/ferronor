package com.ferronor.sic.compras.vista;


import com.ferronor.sic.compras.logica.OrdenCompraService;
import com.ferronor.sic.compras.modelo.DetalleOrdenCompra;
import com.ferronor.sic.compras.modelo.EstadoOrdenCompra;
import com.ferronor.sic.compras.modelo.OrdenCompra;
import com.ferronor.sic.maestros.logica.ProductoService;
import com.ferronor.sic.maestros.logica.ProveedorService;
import com.ferronor.sic.maestros.logica.UnidadMedidaService;
import com.ferronor.sic.maestros.modelo.Producto;
import com.ferronor.sic.maestros.modelo.Proveedor;
import com.ferronor.sic.maestros.modelo.UnidadMedida;
import com.ferronor.sic.seguridad.logica.UsuarioService;
import com.ferronor.sic.seguridad.modelo.Usuario;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;

import java.awt.Component;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class FrmAprobacionOrdenCompra extends javax.swing.JDialog {

    private final OrdenCompraService ordenCompraService
            = ServiceFactory.ordenCompraService();

    private final ProveedorService proveedorService
            = ServiceFactory.proveedorService();

    private final ProductoService productoService
            = ServiceFactory.productoService();

    private final UnidadMedidaService unidadMedidaService
            = ServiceFactory.unidadMedidaService();

    private final UsuarioService usuarioService
            = ServiceFactory.usuarioService();

    private DefaultTableModel modeloOrdenesPendientes;
    private DefaultTableModel modeloDetalleSolicitud;

    private OrdenCompra ordenSeleccionada;
    private Timer timerFechaHora;

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter FORMATO_HORA
            = DateTimeFormatter.ofPattern("HH:mm:ss");

    public FrmAprobacionOrdenCompra(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        configurarComponentes();
    }

    private void configurarComponentes() {
        configurarSesion();
        configurarTablaOrdenes();
        configurarTablaDetalle();
        configurarCamposSoloLectura();
        configurarListeners();
        iniciarReloj();

        cargarOrdenesPendientes();
        limpiarDetalleSeleccion();
        actualizarBotones();
    }

    private void configurarSesion() {
        if (!SesionUsuario.haySesion()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No existe una sesión de usuario activa.",
                    "Sesión",
                    JOptionPane.WARNING_MESSAGE
            );
            dispose();
            return;
        }

        SesionUsuario sesion = SesionUsuario.actual();

        lblNombreApellidoUsuario.setText(
                valorSeguro(sesion.getNombreCompleto())
        );

        lblValorRolUsuarioActual.setText(
                valorSeguro(sesion.getNombreRol())
        );

        LocalDateTime ahora = LocalDateTime.now();

        lblFechaActual.setText(
                ahora.format(FORMATO_FECHA)
        );

        lblHoraActual.setText(
                ahora.format(FORMATO_HORA)
        );
    }

    private void configurarTablaOrdenes() {

        modeloOrdenesPendientes = new DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "N° ORDEN",
                    "FECHA",
                    "PROVEEDOR",
                    "RUC",
                    "SOLICITANTE",
                    "PRODUCTOS",
                    "ESTADO"
                }
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblOrdenesCompraPendientes.setModel(modeloOrdenesPendientes);
        tblOrdenesCompraPendientes.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblOrdenesCompraPendientes.setRowHeight(30);

        configurarRenderersTablaOrdenes();
    }

    private void configurarRenderersTablaOrdenes() {

        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);

        tblOrdenesCompraPendientes
                .getColumnModel()
                .getColumn(0)
                .setCellRenderer(centrado);

        tblOrdenesCompraPendientes
                .getColumnModel()
                .getColumn(1)
                .setCellRenderer(centrado);

        tblOrdenesCompraPendientes
                .getColumnModel()
                .getColumn(3)
                .setCellRenderer(centrado);

        tblOrdenesCompraPendientes
                .getColumnModel()
                .getColumn(5)
                .setCellRenderer(centrado);

        tblOrdenesCompraPendientes
                .getColumnModel()
                .getColumn(6)
                .setCellRenderer(new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column) {

                        Component componente
                                = super.getTableCellRendererComponent(
                                        table,
                                        value,
                                        isSelected,
                                        hasFocus,
                                        row,
                                        column
                                );

                        if (componente instanceof javax.swing.JLabel label) {
                            label.setHorizontalAlignment(
                                    javax.swing.JLabel.CENTER
                            );
                            label.setFont(
                                    label.getFont().deriveFont(
                                            Font.BOLD
                                    )
                            );
                        }

                        return componente;
                    }
                });
    }

    private void configurarTablaDetalle() {

        modeloDetalleSolicitud = new DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "CODIGO",
                    "PRODUCTO",
                    "CANTIDAD",
                    "UNIDAD"
                }
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblDetalleSolicitud.setModel(modeloDetalleSolicitud);
        tblDetalleSolicitud.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblDetalleSolicitud.setRowHeight(30);

        configurarRenderersTablaDetalle();
    }

    private void configurarRenderersTablaDetalle() {

        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);

        tblDetalleSolicitud
                .getColumnModel()
                .getColumn(0)
                .setCellRenderer(centrado);

        tblDetalleSolicitud
                .getColumnModel()
                .getColumn(2)
                .setCellRenderer(centrado);

        tblDetalleSolicitud
                .getColumnModel()
                .getColumn(3)
                .setCellRenderer(centrado);
    }

    private void configurarCamposSoloLectura() {

        txtNroOrden.setEditable(false);
        txtFechaSolicitud.setEditable(false);
        txtEstado.setEditable(false);
        txtSolicitante.setEditable(false);

        txtRazonSocial.setEditable(false);
        txtRuc.setEditable(false);
        txtDireccion.setEditable(false);
        txtContacto.setEditable(false);
    }

    private void configurarListeners() {

        tblOrdenesCompraPendientes
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (e.getValueIsAdjusting()) {
                        return;
                    }

                    seleccionarOrdenDesdeTabla();
                });

        btnAprobar.addActionListener(
                e -> aprobarOrden()
        );

        btnRechazar.addActionListener(
                e -> rechazarOrden()
        );

        btnCancelar.addActionListener(
                e -> cancelar()
        );
    }

    private void iniciarReloj() {

        timerFechaHora = new Timer(
                1000,
                e -> actualizarFechaHora()
        );

        timerFechaHora.start();
    }

    private void actualizarFechaHora() {

        LocalDateTime ahora = LocalDateTime.now();

        lblFechaActual.setText(
                ahora.format(FORMATO_FECHA)
        );

        lblHoraActual.setText(
                ahora.format(FORMATO_HORA)
        );
    }

    private void cargarOrdenesPendientes() {

        try {

            List<OrdenCompra> ordenes
                    = ordenCompraService.listarPorEstado(
                            EstadoOrdenCompra.PENDIENTE
                    );

            modeloOrdenesPendientes.setRowCount(0);

            if (ordenes == null || ordenes.isEmpty()) {
                limpiarDetalleSeleccion();
                actualizarBotones();
                return;
            }

            for (OrdenCompra orden : ordenes) {

                Proveedor proveedor
                        = proveedorService.buscarPorId(
                                orden.getIdProveedor()
                        );

                Usuario solicitante
                        = usuarioService.buscarPorId(
                                orden.getIdUsuarioSolicita()
                        );

                String razonSocial = proveedor != null
                        ? valorSeguro(proveedor.getRazonSocial())
                        : "Proveedor no disponible";

                String ruc = proveedor != null
                        ? valorSeguro(proveedor.getRuc())
                        : "";

                String nombreSolicitante
                        = solicitante != null
                        ? valorSeguro(
                                solicitante.getNombreCompleto()
                        )
                        : "Usuario no disponible";

                int cantidadProductos
                        = orden.getDetalles() != null
                        ? orden.getDetalles().size()
                        : 0;

                modeloOrdenesPendientes.addRow(
                        new Object[]{
                            formatearNumeroOrden(
                                    orden.getIdOrdenCompra()
                            ),
                            formatearFecha(
                                    orden.getFecha()
                            ),
                            razonSocial,
                            ruc,
                            nombreSolicitante,
                            cantidadProductos,
                            valorSeguro(
                                    orden.getEstado()
                                            != null
                                            ? orden.getEstado().name()
                                            : ""
                            )
                        }
                );
            }

        } catch (RuntimeException ex) {

            mostrarError(
                    "No se pudieron cargar las órdenes pendientes.",
                    ex
            );

            modeloOrdenesPendientes.setRowCount(0);
            limpiarDetalleSeleccion();
            actualizarBotones();
        }
    }

    private void seleccionarOrdenDesdeTabla() {

        int fila = tblOrdenesCompraPendientes
                .getSelectedRow();

        if (fila < 0) {
            ordenSeleccionada = null;
            limpiarDetalleSeleccion();
            actualizarBotones();
            return;
        }

        int modeloFila
                = tblOrdenesCompraPendientes
                        .convertRowIndexToModel(fila);

        Object valorId
                = modeloOrdenesPendientes
                        .getValueAt(modeloFila, 0);

        int idOrdenCompra
                = extraerIdOrden(valorId);

        cargarOrdenSeleccionada(idOrdenCompra);
    }

    private void cargarOrdenSeleccionada(int idOrdenCompra) {

        try {

            OrdenCompra orden
                    = ordenCompraService.buscarPorId(
                            idOrdenCompra
                    );

            if (orden == null) {

                ordenSeleccionada = null;

                limpiarDetalleSeleccion();
                actualizarBotones();

                JOptionPane.showMessageDialog(
                        this,
                        "La orden de compra ya no está disponible.",
                        "Orden no encontrada",
                        JOptionPane.WARNING_MESSAGE
                );

                cargarOrdenesPendientes();
                return;
            }

            ordenSeleccionada = orden;

            mostrarDatosOrden();
            cargarDetalleSolicitud();

            actualizarBotones();

        } catch (RuntimeException ex) {

            ordenSeleccionada = null;

            limpiarDetalleSeleccion();
            actualizarBotones();

            mostrarError(
                    "No se pudo consultar la orden de compra seleccionada.",
                    ex
            );
        }
    }

    private void mostrarDatosOrden() {

        if (ordenSeleccionada == null) {
            limpiarDetalleSeleccion();
            return;
        }

        txtNroOrden.setText(
                formatearNumeroOrden(
                        ordenSeleccionada.getIdOrdenCompra()
                )
        );

        txtFechaSolicitud.setText(
                formatearFechaHora(
                        ordenSeleccionada.getFecha()
                )
        );

        txtEstado.setText(
                ordenSeleccionada.getEstado() != null
                        ? ordenSeleccionada
                                .getEstado()
                                .name()
                        : ""
        );

        Usuario solicitante
                = usuarioService.buscarPorId(
                        ordenSeleccionada.getIdUsuarioSolicita()
                );

        txtSolicitante.setText(
                solicitante != null
                        ? solicitante.getNombreCompleto()
                        : "Usuario no disponible"
        );

        Proveedor proveedor
                = proveedorService.buscarPorId(
                        ordenSeleccionada.getIdProveedor()
                );

        if (proveedor == null) {
            txtRazonSocial.setText("");
            txtRuc.setText("");
            txtDireccion.setText("");
            txtContacto.setText("");
            return;
        }

        txtRazonSocial.setText(
                valorSeguro(proveedor.getRazonSocial())
        );

        txtRuc.setText(
                valorSeguro(proveedor.getRuc())
        );

        txtDireccion.setText(
                valorSeguro(proveedor.getDireccion())
        );

        txtContacto.setText(
                construirContacto(proveedor)
        );
    }

    private void cargarDetalleSolicitud() {

        modeloDetalleSolicitud.setRowCount(0);

        if (ordenSeleccionada == null
                || ordenSeleccionada.getDetalles() == null) {

            actualizarResumenDetalle();
            return;
        }

        for (DetalleOrdenCompra detalle
                : ordenSeleccionada.getDetalles()) {

            Producto producto
                    = productoService.buscarPorId(
                            detalle.getIdProducto()
                    );

            String codigo = "";
            String nombre = "Producto no disponible";
            String unidad = "";

            if (producto != null) {

                codigo = valorSeguro(
                        producto.getCodigo()
                );

                nombre = valorSeguro(
                        producto.getNombre()
                );

                UnidadMedida unidadMedida
                        = unidadMedidaService.buscarPorId(
                                producto.getIdUnidadMedida()
                        );

                if (unidadMedida != null) {
                    unidad = valorSeguro(
                            unidadMedida.getAbreviatura()
                    );
                }
            }

            String cantidad
                    = detalle.getCantidad() != null
                    ? detalle.getCantidad()
                            .stripTrailingZeros()
                            .toPlainString()
                    : "";

            modeloDetalleSolicitud.addRow(
                    new Object[]{
                        codigo,
                        nombre,
                        cantidad,
                        unidad
                    }
            );
        }

        actualizarResumenDetalle();
    }

    private void actualizarResumenDetalle() {

        if (ordenSeleccionada == null
                || ordenSeleccionada.getDetalles() == null) {

            lblValorProductosDistintos.setText("0");
            lblValorUnidadesSolicitadas.setText("0");
            return;
        }

        int productosDistintos
                = ordenSeleccionada
                        .getDetalles()
                        .size();

        java.math.BigDecimal unidadesSolicitadas
                = ordenSeleccionada
                        .getDetalles()
                        .stream()
                        .map(DetalleOrdenCompra::getCantidad)
                        .filter(java.util.Objects::nonNull)
                        .reduce(
                                java.math.BigDecimal.ZERO,
                                java.math.BigDecimal::add
                        );

        lblValorProductosDistintos.setText(
                String.valueOf(productosDistintos)
        );

        lblValorUnidadesSolicitadas.setText(
                unidadesSolicitadas
                        .stripTrailingZeros()
                        .toPlainString()
        );
    }

    private void actualizarBotones() {

        boolean puedeProcesar
                = ordenSeleccionada != null
                && ordenSeleccionada.getEstado()
                == EstadoOrdenCompra.PENDIENTE;

        btnAprobar.setEnabled(puedeProcesar);
        btnRechazar.setEnabled(puedeProcesar);
    }

    private void aprobarOrden() {

        if (!ordenPuedeProcesarse()) {
            return;
        }

        String numero
                = formatearNumeroOrden(
                        ordenSeleccionada.getIdOrdenCompra()
                );

        String proveedor
                = txtRazonSocial.getText();

        int confirmacion
                = JOptionPane.showConfirmDialog(
                        this,
                        "¿Está seguro de aprobar esta orden de compra?\n\n"
                        + "Orden: " + numero + "\n"
                        + "Proveedor: " + proveedor + "\n"
                        + "Solicitante: " + txtSolicitante.getText(),
                        "Confirmar aprobación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        procesarAprobacion();
    }

    private void procesarAprobacion() {

        btnAprobar.setEnabled(false);
        btnRechazar.setEnabled(false);

        try {

            int idUsuario
                    = SesionUsuario.actual()
                            .getIdUsuario();

            RespuestaOperacion<Void> respuesta
                    = ordenCompraService.aprobar(
                            ordenSeleccionada.getIdOrdenCompra(),
                            idUsuario
                    );

            if (respuesta == null
                    || !respuesta.isExito()) {

                String mensaje
                        = respuesta != null
                        ? respuesta.getMensaje()
                        : "No se recibió respuesta del servicio.";

                mostrarMensajeError(
                        mensaje,
                        "No se pudo aprobar la orden"
                );

                actualizarBotones();
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "La orden de compra "
                    + formatearNumeroOrden(
                            ordenSeleccionada.getIdOrdenCompra()
                    )
                    + " fue aprobada correctamente.",
                    "Aprobación registrada",
                    JOptionPane.INFORMATION_MESSAGE
            );

            recargarDespuesDeProcesar();

        } catch (RuntimeException ex) {

            mostrarError(
                    "Ocurrió un error al aprobar la orden de compra.",
                    ex
            );

            actualizarBotones();
        }
    }

    private void rechazarOrden() {

        if (!ordenPuedeProcesarse()) {
            return;
        }

        String numero
                = formatearNumeroOrden(
                        ordenSeleccionada.getIdOrdenCompra()
                );

        String proveedor
                = txtRazonSocial.getText();

        int confirmacion
                = JOptionPane.showConfirmDialog(
                        this,
                        "¿Está seguro de rechazar esta orden de compra?\n\n"
                        + "Orden: " + numero + "\n"
                        + "Proveedor: " + proveedor + "\n"
                        + "Solicitante: " + txtSolicitante.getText(),
                        "Confirmar rechazo",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        procesarRechazo();
    }

    private void procesarRechazo() {

        btnAprobar.setEnabled(false);
        btnRechazar.setEnabled(false);

        try {

            int idUsuario
                    = SesionUsuario.actual()
                            .getIdUsuario();

            RespuestaOperacion<Void> respuesta
                    = ordenCompraService.rechazar(
                            ordenSeleccionada.getIdOrdenCompra(),
                            idUsuario
                    );

            if (respuesta == null
                    || !respuesta.isExito()) {

                String mensaje
                        = respuesta != null
                        ? respuesta.getMensaje()
                        : "No se recibió respuesta del servicio.";

                mostrarMensajeError(
                        mensaje,
                        "No se pudo rechazar la orden"
                );

                actualizarBotones();
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "La orden de compra "
                    + formatearNumeroOrden(
                            ordenSeleccionada.getIdOrdenCompra()
                    )
                    + " fue rechazada correctamente.",
                    "Rechazo registrado",
                    JOptionPane.INFORMATION_MESSAGE
            );

            recargarDespuesDeProcesar();

        } catch (RuntimeException ex) {

            mostrarError(
                    "Ocurrió un error al rechazar la orden de compra.",
                    ex
            );

            actualizarBotones();
        }
    }

    private void recargarDespuesDeProcesar() {

        ordenSeleccionada = null;

        limpiarDetalleSeleccion();

        cargarOrdenesPendientes();

        actualizarBotones();
    }

    private boolean ordenPuedeProcesarse() {

        if (!SesionUsuario.haySesion()) {

            JOptionPane.showMessageDialog(
                    this,
                    "No existe una sesión de usuario activa.",
                    "Sesión",
                    JOptionPane.WARNING_MESSAGE
            );

            return false;
        }

        if (ordenSeleccionada == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Debe seleccionar una orden de compra pendiente.",
                    "Orden de compra",
                    JOptionPane.WARNING_MESSAGE
            );

            return false;
        }

        if (ordenSeleccionada.getEstado()
                != EstadoOrdenCompra.PENDIENTE) {

            JOptionPane.showMessageDialog(
                    this,
                    "La orden seleccionada ya no se encuentra pendiente de aprobación.",
                    "Orden ya procesada",
                    JOptionPane.WARNING_MESSAGE
            );

            cargarOrdenesPendientes();
            limpiarDetalleSeleccion();
            actualizarBotones();

            return false;
        }

        return true;
    }

    private void limpiarDetalleSeleccion() {

        ordenSeleccionada = null;

        txtNroOrden.setText("");
        txtFechaSolicitud.setText("");
        txtEstado.setText("");
        txtSolicitante.setText("");

        txtRazonSocial.setText("");
        txtRuc.setText("");
        txtDireccion.setText("");
        txtContacto.setText("");

        modeloDetalleSolicitud.setRowCount(0);

        lblValorProductosDistintos.setText("0");
        lblValorUnidadesSolicitadas.setText("0");

        if (tblOrdenesCompraPendientes.getSelectedRow() >= 0) {
            tblOrdenesCompraPendientes.clearSelection();
        }
    }

    private void cancelar() {

        dispose();
    }

    private String construirContacto(Proveedor proveedor) {

        String contacto
                = valorSeguro(proveedor.getContacto());

        String telefono
                = valorSeguro(proveedor.getTelefono());

        if (!contacto.isBlank()
                && !telefono.isBlank()) {

            return contacto + " — " + telefono;
        }

        if (!contacto.isBlank()) {
            return contacto;
        }

        return telefono;
    }

    private String formatearNumeroOrden(int idOrdenCompra) {

        return String.format(
                "OC-%04d",
                idOrdenCompra
        );
    }

    private int extraerIdOrden(Object valor) {

        if (valor == null) {
            return -1;
        }

        String texto = valor.toString()
                .trim()
                .toUpperCase();

        if (texto.startsWith("OC-")) {

            texto = texto.substring(3);
        }

        try {
            return Integer.parseInt(texto);

        } catch (NumberFormatException ex) {

            return -1;
        }
    }

    private String formatearFecha(LocalDateTime fecha) {

        if (fecha == null) {
            return "";
        }

        return fecha.format(FORMATO_FECHA);
    }

    private String formatearFechaHora(LocalDateTime fecha) {

        if (fecha == null) {
            return "";
        }

        return fecha.format(FORMATO_FECHA_HORA);
    }

    private String valorSeguro(Object valor) {

        return valor != null
                ? valor.toString()
                : "";
    }

    private void mostrarMensajeError(
            String mensaje,
            String titulo) {

        JOptionPane.showMessageDialog(
                this,
                valorSeguro(mensaje),
                titulo,
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void mostrarError(
            String contexto,
            RuntimeException ex) {

        String mensaje = ex.getMessage();

        if (mensaje == null || mensaje.isBlank()) {
            mensaje = "Error no especificado.";
        }

        JOptionPane.showMessageDialog(
                this,
                contexto + "\n\n" + mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    @Override
    public void dispose() {

        if (timerFechaHora != null) {
            timerFechaHora.stop();
        }

        super.dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSuperior = new javax.swing.JPanel();
        lblAprobarOrdenCompra = new javax.swing.JLabel();
        lblRevisionDecisionSolicitudesPendientes = new javax.swing.JLabel();
        lblNombreApellidoUsuario = new javax.swing.JLabel();
        lblFechaActual = new javax.swing.JLabel();
        lblHoraActual = new javax.swing.JLabel();
        lblRol = new javax.swing.JLabel();
        lblValorRolUsuarioActual = new javax.swing.JLabel();
        pnlOrdenesPendientesAprobacion = new javax.swing.JPanel();
        spnlOrdenesPendientes = new javax.swing.JScrollPane();
        tblOrdenesCompraPendientes = new javax.swing.JTable();
        jSeparator2 = new javax.swing.JSeparator();
        pnlDatosOrdenSeleccionada = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        lblSolicitud = new javax.swing.JLabel();
        lblNroOrden = new javax.swing.JLabel();
        txtNroOrden = new javax.swing.JTextField();
        lblFechaSolicitud = new javax.swing.JLabel();
        txtFechaSolicitud = new javax.swing.JTextField();
        lblEstado = new javax.swing.JLabel();
        txtEstado = new javax.swing.JTextField();
        lblSolicitante = new javax.swing.JLabel();
        txtSolicitante = new javax.swing.JTextField();
        lblRazonSocial = new javax.swing.JLabel();
        txtRazonSocial = new javax.swing.JTextField();
        lblRuc = new javax.swing.JLabel();
        txtRuc = new javax.swing.JTextField();
        lblDireccion = new javax.swing.JLabel();
        txtDireccion = new javax.swing.JTextField();
        txtContacto = new javax.swing.JTextField();
        lblContacto = new javax.swing.JLabel();
        pnlSeparadorVertical = new javax.swing.JPanel();
        lblProveedor = new javax.swing.JLabel();
        pnlDetalleSolicitud = new javax.swing.JPanel();
        jSeparator3 = new javax.swing.JSeparator();
        spnlDetalleSolicitud = new javax.swing.JScrollPane();
        tblDetalleSolicitud = new javax.swing.JTable();
        jSeparator4 = new javax.swing.JSeparator();
        lblProductoDistintos = new javax.swing.JLabel();
        lblValorProductosDistintos = new javax.swing.JLabel();
        lblUnidadesSolicitadas = new javax.swing.JLabel();
        lblValorUnidadesSolicitadas = new javax.swing.JLabel();
        pnlBotones = new javax.swing.JPanel();
        btnAprobar = new javax.swing.JButton();
        btnRechazar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblAprobarOrdenCompra.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblAprobarOrdenCompra.setText("APROBACIÓN DE ORDEN DE COMPRA");

        lblRevisionDecisionSolicitudesPendientes.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblRevisionDecisionSolicitudesPendientes.setText("Revisión y decisión de solicitudes pendientes  ");

        lblNombreApellidoUsuario.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNombreApellidoUsuario.setText("Nombre Apellido");

        lblFechaActual.setText("21/08/2026");

        lblHoraActual.setText("10:40");

        lblRol.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRol.setText("Rol:");

        lblValorRolUsuarioActual.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblValorRolUsuarioActual.setText("Gerencia");

        javax.swing.GroupLayout pnlSuperiorLayout = new javax.swing.GroupLayout(pnlSuperior);
        pnlSuperior.setLayout(pnlSuperiorLayout);
        pnlSuperiorLayout.setHorizontalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSuperiorLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblAprobarOrdenCompra)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblRevisionDecisionSolicitudesPendientes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(250, 250, 250)))
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblNombreApellidoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(pnlSuperiorLayout.createSequentialGroup()
                            .addGap(12, 12, 12)
                            .addComponent(lblRol, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(lblValorRolUsuarioActual, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblFechaActual)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHoraActual)))
                .addGap(20, 20, 20))
        );
        pnlSuperiorLayout.setVerticalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSuperiorLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblNombreApellidoUsuario)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblRol)
                            .addComponent(lblValorRolUsuarioActual)))
                    .addComponent(lblAprobarOrdenCompra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblFechaActual)
                        .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblRevisionDecisionSolicitudesPendientes, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlOrdenesPendientesAprobacion.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. ÓRDENES PENDIENTES DE APROBACIÓN", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        tblOrdenesCompraPendientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "N° ORDEN", "FECHA", "PROVEEDOR", "RUC", "SOLICITANTE", "PRODUCTOS", "ESTADO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlOrdenesPendientes.setViewportView(tblOrdenesCompraPendientes);

        javax.swing.GroupLayout pnlOrdenesPendientesAprobacionLayout = new javax.swing.GroupLayout(pnlOrdenesPendientesAprobacion);
        pnlOrdenesPendientesAprobacion.setLayout(pnlOrdenesPendientesAprobacionLayout);
        pnlOrdenesPendientesAprobacionLayout.setHorizontalGroup(
            pnlOrdenesPendientesAprobacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOrdenesPendientesAprobacionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlOrdenesPendientes)
                .addContainerGap())
            .addComponent(jSeparator2)
        );
        pnlOrdenesPendientesAprobacionLayout.setVerticalGroup(
            pnlOrdenesPendientesAprobacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOrdenesPendientesAprobacionLayout.createSequentialGroup()
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnlOrdenesPendientes, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                .addContainerGap())
        );

        pnlDatosOrdenSeleccionada.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. DATOS DE LA ORDEN SELECCIONADA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblSolicitud.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblSolicitud.setText("SOLICITUD");

        lblNroOrden.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblNroOrden.setText("N.° ORDEN");

        lblFechaSolicitud.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblFechaSolicitud.setText("FECHA DE SOLICITUD");

        lblEstado.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblEstado.setText("ESTADO");

        lblSolicitante.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblSolicitante.setText("SOLICITANTE");

        lblRazonSocial.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblRazonSocial.setText("RAZÓN SOCIAL");

        lblRuc.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblRuc.setText("RUC");

        lblDireccion.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblDireccion.setText("DIRECCIÓN");

        lblContacto.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblContacto.setText("CONTACTO");

        pnlSeparadorVertical.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        javax.swing.GroupLayout pnlSeparadorVerticalLayout = new javax.swing.GroupLayout(pnlSeparadorVertical);
        pnlSeparadorVertical.setLayout(pnlSeparadorVerticalLayout);
        pnlSeparadorVerticalLayout.setHorizontalGroup(
            pnlSeparadorVerticalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlSeparadorVerticalLayout.setVerticalGroup(
            pnlSeparadorVerticalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 245, Short.MAX_VALUE)
        );

        lblProveedor.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblProveedor.setText("PROVEEDOR");

        javax.swing.GroupLayout pnlDatosOrdenSeleccionadaLayout = new javax.swing.GroupLayout(pnlDatosOrdenSeleccionada);
        pnlDatosOrdenSeleccionada.setLayout(pnlDatosOrdenSeleccionadaLayout);
        pnlDatosOrdenSeleccionadaLayout.setHorizontalGroup(
            pnlDatosOrdenSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(pnlDatosOrdenSeleccionadaLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(pnlDatosOrdenSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSolicitud, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlDatosOrdenSeleccionadaLayout.createSequentialGroup()
                        .addGroup(pnlDatosOrdenSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtFechaSolicitud, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtSolicitante, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                            .addComponent(lblSolicitante, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtNroOrden, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtEstado, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblEstado, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblFechaSolicitud, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblNroOrden, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlSeparadorVertical, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlDatosOrdenSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblProveedor, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblRuc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblRazonSocial, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtRuc, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDireccion, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtDireccion, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblContacto, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtRazonSocial)
                    .addComponent(txtContacto, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE))
                .addGap(28, 28, 28))
        );
        pnlDatosOrdenSeleccionadaLayout.setVerticalGroup(
            pnlDatosOrdenSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatosOrdenSeleccionadaLayout.createSequentialGroup()
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatosOrdenSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDatosOrdenSeleccionadaLayout.createSequentialGroup()
                        .addGroup(pnlDatosOrdenSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSolicitud)
                            .addComponent(lblProveedor, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlDatosOrdenSeleccionadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlDatosOrdenSeleccionadaLayout.createSequentialGroup()
                                .addComponent(lblNroOrden)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtNroOrden, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblFechaSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtFechaSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblSolicitante, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtSolicitante, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlDatosOrdenSeleccionadaLayout.createSequentialGroup()
                                .addComponent(lblRazonSocial)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtRazonSocial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblRuc)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtRuc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblDireccion)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblContacto)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtContacto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(pnlSeparadorVertical, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlDetalleSolicitud.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "03. DETALLE DE LA SOLICITUD", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        tblDetalleSolicitud.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "CODIGO", "PRODUCTO", "CANTIDAD", "UNIDAD"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlDetalleSolicitud.setViewportView(tblDetalleSolicitud);

        lblProductoDistintos.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblProductoDistintos.setText("Productos distintos:");

        lblValorProductosDistintos.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorProductosDistintos.setText("4");

        lblUnidadesSolicitadas.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblUnidadesSolicitadas.setText("Unidades solicitadas:");

        lblValorUnidadesSolicitadas.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblValorUnidadesSolicitadas.setText("212");

        javax.swing.GroupLayout pnlDetalleSolicitudLayout = new javax.swing.GroupLayout(pnlDetalleSolicitud);
        pnlDetalleSolicitud.setLayout(pnlDetalleSolicitudLayout);
        pnlDetalleSolicitudLayout.setHorizontalGroup(
            pnlDetalleSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator3)
            .addComponent(jSeparator4)
            .addGroup(pnlDetalleSolicitudLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDetalleSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spnlDetalleSolicitud)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDetalleSolicitudLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lblProductoDistintos)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblValorProductosDistintos, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblUnidadesSolicitadas)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblValorUnidadesSolicitadas, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        pnlDetalleSolicitudLayout.setVerticalGroup(
            pnlDetalleSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetalleSolicitudLayout.createSequentialGroup()
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnlDetalleSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetalleSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProductoDistintos)
                    .addComponent(lblValorProductosDistintos)
                    .addComponent(lblUnidadesSolicitadas)
                    .addComponent(lblValorUnidadesSolicitadas))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlBotones.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        btnAprobar.setBackground(new java.awt.Color(51, 102, 0));
        btnAprobar.setText("APROBAR");
        btnAprobar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAprobarActionPerformed(evt);
            }
        });

        btnRechazar.setBackground(new java.awt.Color(153, 51, 0));
        btnRechazar.setText("RECHAZAR");
        btnRechazar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRechazarActionPerformed(evt);
            }
        });

        btnCancelar.setBackground(new java.awt.Color(51, 51, 51));
        btnCancelar.setText("CANCELAR");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlBotonesLayout = new javax.swing.GroupLayout(pnlBotones);
        pnlBotones.setLayout(pnlBotonesLayout);
        pnlBotonesLayout.setHorizontalGroup(
            pnlBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlBotonesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCancelar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRechazar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAprobar)
                .addContainerGap())
        );
        pnlBotonesLayout.setVerticalGroup(
            pnlBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBotonesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAprobar)
                    .addComponent(btnRechazar)
                    .addComponent(btnCancelar))
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
                    .addComponent(pnlOrdenesPendientesAprobacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDatosOrdenSeleccionada, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDetalleSolicitud, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlBotones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlOrdenesPendientesAprobacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDatosOrdenSeleccionada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDetalleSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlBotones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAprobarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAprobarActionPerformed
        // TODO add your handling code here:
        aprobarOrden();
    }//GEN-LAST:event_btnAprobarActionPerformed

    private void btnRechazarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRechazarActionPerformed
        // TODO add your handling code here:
        rechazarOrden();
    }//GEN-LAST:event_btnRechazarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        // TODO add your handling code here:
        cancelar();
    }//GEN-LAST:event_btnCancelarActionPerformed

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
            java.util.logging.Logger.getLogger(FrmAprobacionOrdenCompra.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmAprobacionOrdenCompra.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmAprobacionOrdenCompra.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmAprobacionOrdenCompra.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmAprobacionOrdenCompra dialog = new FrmAprobacionOrdenCompra(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnAprobar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnRechazar;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel lblAprobarOrdenCompra;
    private javax.swing.JLabel lblContacto;
    private javax.swing.JLabel lblDireccion;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblFechaActual;
    private javax.swing.JLabel lblFechaSolicitud;
    private javax.swing.JLabel lblHoraActual;
    private javax.swing.JLabel lblNombreApellidoUsuario;
    private javax.swing.JLabel lblNroOrden;
    private javax.swing.JLabel lblProductoDistintos;
    private javax.swing.JLabel lblProveedor;
    private javax.swing.JLabel lblRazonSocial;
    private javax.swing.JLabel lblRevisionDecisionSolicitudesPendientes;
    private javax.swing.JLabel lblRol;
    private javax.swing.JLabel lblRuc;
    private javax.swing.JLabel lblSolicitante;
    private javax.swing.JLabel lblSolicitud;
    private javax.swing.JLabel lblUnidadesSolicitadas;
    private javax.swing.JLabel lblValorProductosDistintos;
    private javax.swing.JLabel lblValorRolUsuarioActual;
    private javax.swing.JLabel lblValorUnidadesSolicitadas;
    private javax.swing.JPanel pnlBotones;
    private javax.swing.JPanel pnlDatosOrdenSeleccionada;
    private javax.swing.JPanel pnlDetalleSolicitud;
    private javax.swing.JPanel pnlOrdenesPendientesAprobacion;
    private javax.swing.JPanel pnlSeparadorVertical;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JScrollPane spnlDetalleSolicitud;
    private javax.swing.JScrollPane spnlOrdenesPendientes;
    private javax.swing.JTable tblDetalleSolicitud;
    private javax.swing.JTable tblOrdenesCompraPendientes;
    private javax.swing.JTextField txtContacto;
    private javax.swing.JTextField txtDireccion;
    private javax.swing.JTextField txtEstado;
    private javax.swing.JTextField txtFechaSolicitud;
    private javax.swing.JTextField txtNroOrden;
    private javax.swing.JTextField txtRazonSocial;
    private javax.swing.JTextField txtRuc;
    private javax.swing.JTextField txtSolicitante;
    // End of variables declaration//GEN-END:variables
}
