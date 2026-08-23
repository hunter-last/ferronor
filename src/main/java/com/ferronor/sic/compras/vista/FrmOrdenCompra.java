package com.ferronor.sic.compras.vista;

import com.ferronor.sic.compras.logica.OrdenCompraService;
import com.ferronor.sic.compras.modelo.DetalleOrdenCompra;
import com.ferronor.sic.compras.modelo.OrdenCompra;
import com.ferronor.sic.maestros.logica.ProductoService;
import com.ferronor.sic.maestros.logica.ProveedorService;
import com.ferronor.sic.maestros.logica.UnidadMedidaService;
import com.ferronor.sic.maestros.modelo.Producto;
import com.ferronor.sic.maestros.modelo.Proveedor;
import com.ferronor.sic.maestros.modelo.UnidadMedida;
import com.ferronor.sic.shared.ui.ComboAutoFiltro;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class FrmOrdenCompra extends javax.swing.JDialog {

    private final OrdenCompraService ordenCompraService = ServiceFactory.ordenCompraService();
    private final ProveedorService proveedorService = ServiceFactory.proveedorService();
    private final ProductoService productoService = ServiceFactory.productoService();
    private final UnidadMedidaService unidadMedidaService = ServiceFactory.unidadMedidaService();
    private final List<DetalleOrdenCompra> detalles = new ArrayList<>();
    private DefaultTableModel modeloDetalle;
    private Timer timerFechaHora;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    public FrmOrdenCompra(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        configurarComponentes();
    }

    private void configurarComponentes() {
        configurarSesion();
        configurarEstadoInicial();
        configurarComboProveedores();
        configurarComboProductos();
        configurarSpinnerCantidad();
        configurarTablaDetalle();
        configurarListeners();
        configurarCamposSoloLectura();
        iniciarReloj();
        actualizarResumen();
    }

    private void configurarSesion() {
        if (!SesionUsuario.haySesion()) {
            JOptionPane.showMessageDialog(this, "No existe una sesión de usuario activa.", "Sesión", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }
        SesionUsuario sesion = SesionUsuario.actual();
        lblNombreApellidoUsuario.setText(sesion.getNombreCompleto());
        LocalDateTime ahora = LocalDateTime.now();
        lblFechaActual.setText(ahora.format(FORMATO_FECHA));
        lblHoraActual.setText(ahora.format(FORMATO_HORA));
    }

    private void configurarEstadoInicial() {
        txtEstadoSuperior.setText("• PENDIENTE");
        txtEstadoSolicitud.setText("Pendiente de Aprobación");
    }

    private void configurarComboProveedores() {
        cmbProveedores.setModel(new DefaultComboBoxModel<>());
        cmbProveedores.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Proveedor proveedor) {
                    setText(proveedor.getRazonSocial() + " — RUC " + proveedor.getRuc());
                }
                return this;
            }
        });
        ComboAutoFiltro.mejorarCombo(cmbProveedores, texto -> {
            if (texto == null || texto.isBlank()) {
                return proveedorService.listarActivos();
            }
            return proveedorService.buscarActivosPorRazonSocialORucParcial(texto);
        });
    }

    private void configurarComboProductos() {
        cmbProductos.setModel(new DefaultComboBoxModel<>());
        cmbProductos.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Producto producto) {
                    setText(producto.getCodigo() + " — " + producto.getNombre());
                }
                return this;
            }
        });
        ComboAutoFiltro.mejorarCombo(cmbProductos, texto -> {
            if (texto == null || texto.isBlank()) {
                return productoService.listarActivos();
            }
            return productoService.buscarActivosPorNombreOCodigoParcial(texto);
        });
    }

    private void configurarSpinnerCantidad() {
        SpinnerNumberModel modeloCantidad = new SpinnerNumberModel(BigDecimal.ONE, new BigDecimal("0.01"), null, BigDecimal.ONE);
        spnCantidad.setModel(modeloCantidad);
    }

    /**
     * * Configuración de JTable.
     */
    private void configurarTablaDetalle() {
        modeloDetalle = new DefaultTableModel(new Object[][]{}, new String[]{"PRODUCTO", "CANTIDAD", "ACCIONES"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblDetalleSolicitud.setModel(modeloDetalle);
        tblDetalleSolicitud.setRowHeight(32);
        tblDetalleSolicitud.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        configurarRendererAcciones();
    }

    /**
     * * Renderer visual para la columna ACCIONES.
     */
    private void configurarRendererAcciones() {
        tblDetalleSolicitud.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel("✕");
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                label.setForeground(new Color(176, 58, 46));
                return label;
            }
        });
    }

    private void configurarListeners() {
        cmbProveedores.addActionListener(e -> proveedorSeleccionado());
        cmbProductos.addActionListener(e -> productoSeleccionado());
        btnAgregarProducto.addActionListener(e -> agregarProducto());
        btnRegistrarSolicitud.addActionListener(e -> registrarSolicitud());
        btnCancelar.addActionListener(e -> cancelar());
        tblDetalleSolicitud.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 1) {
                    int fila = tblDetalleSolicitud.rowAtPoint(evt.getPoint());
                    int columna = tblDetalleSolicitud.columnAtPoint(evt.getPoint());
                    if (fila >= 0 && columna == 2) {
                        eliminarDetalle(fila);
                    }
                }
            }
        });
    }

    private void configurarCamposSoloLectura() {
        txtRuc.setEditable(false);
        txtContacto.setEditable(false);
        txtInicialesEmpresa.setEditable(false);
        txtProductoSeleccionado.setEditable(false);
        txtUnidad.setEditable(false);
        txtCantProdDistintos.setEditable(false);
        txtCantUnidadesTotales.setEditable(false);
        txtEstadoSolicitud.setEditable(false);
        txtEstadoSuperior.setEditable(false);
    }

    private void iniciarReloj() {
        timerFechaHora = new Timer(1000, e -> {
            LocalDateTime ahora = LocalDateTime.now();
            lblFechaActual.setText(ahora.format(FORMATO_FECHA));
            lblHoraActual.setText(ahora.format(FORMATO_HORA));
        });
        timerFechaHora.start();
    }

    private void proveedorSeleccionado() {
        Object seleccionado = cmbProveedores.getSelectedItem();
        if (!(seleccionado instanceof Proveedor proveedor)) {
            limpiarDatosProveedor();
            return;
        }
        txtRuc.setText(proveedor.getRuc() != null ? proveedor.getRuc() : "");
        txtContacto.setText(construirContacto(proveedor));
        lblNombreEmpresaYTipo.setText(proveedor.getRazonSocial());
        lblRucNroRuc.setText("RUC " + proveedor.getRuc());
        lblDireccionSedePrincipal.setText(proveedor.getDireccion() != null ? proveedor.getDireccion() : "");
        txtInicialesEmpresa.setText(obtenerIniciales(proveedor.getRazonSocial()));
    }

    private void limpiarDatosProveedor() {
        txtRuc.setText("");
        txtContacto.setText("");
        lblNombreEmpresaYTipo.setText("");
        lblRucNroRuc.setText("");
        lblDireccionSedePrincipal.setText("");
        txtInicialesEmpresa.setText("");
    }

    private String construirContacto(Proveedor proveedor) {
        String contacto = proveedor.getContacto();
        String telefono = proveedor.getTelefono();
        if (contacto == null || contacto.isBlank()) {
            if (telefono == null || telefono.isBlank()) {
                return "";
            }
            return telefono;
        }
        if (telefono == null || telefono.isBlank()) {
            return contacto;
        }
        return contacto + " — " + telefono;
    }

    private String obtenerIniciales(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        String[] partes = texto.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        }
        return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase();
    }

    private void productoSeleccionado() {
        Object seleccionado = cmbProductos.getSelectedItem();
        if (!(seleccionado instanceof Producto producto)) {
            txtProductoSeleccionado.setText("");
            txtUnidad.setText("");
            return;
        }
        txtProductoSeleccionado.setText("Seleccionado: " + producto.getCodigo() + " — " + producto.getNombre());
        cargarUnidadMedida(producto);
    }

    private void cargarUnidadMedida(Producto producto) {
        UnidadMedida unidad = unidadMedidaService.buscarPorId(producto.getIdUnidadMedida());
        if (unidad == null) {
            txtUnidad.setText("");
            return;
        }
        txtUnidad.setText(unidad.getAbreviatura());
    }

    private void agregarProducto() {
        Object seleccionado = cmbProductos.getSelectedItem();
        if (!(seleccionado instanceof Producto producto)) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un producto.", "Producto",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        BigDecimal cantidad = obtenerCantidad();
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a cero.", "Cantidad",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (productoYaAgregado(producto.getIdProducto())) {
            JOptionPane.showMessageDialog(this, "El producto ya fue agregado a la solicitud.", "Producto duplicado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        DetalleOrdenCompra detalle = new DetalleOrdenCompra(producto.getIdProducto(), cantidad);
        detalles.add(detalle);
        refrescarTablaDetalle();
        limpiarEntradaProducto();
        actualizarResumen();
    }

    private BigDecimal obtenerCantidad() {
        Object valor = spnCantidad.getValue();
        if (valor instanceof BigDecimal cantidad) {
            return cantidad;
        }
        if (valor instanceof Number numero) {
            return BigDecimal.valueOf(numero.doubleValue());
        }
        return null;
    }

    private boolean productoYaAgregado(int idProducto) {
        return detalles.stream().anyMatch(detalle -> detalle.getIdProducto() == idProducto);
    }

    private void refrescarTablaDetalle() {
        modeloDetalle.setRowCount(0);
        for (DetalleOrdenCompra detalle : detalles) {
            Producto producto = productoService.buscarPorId(detalle.getIdProducto());
            if (producto == null) {
                continue;
            }
            UnidadMedida unidad = unidadMedidaService.buscarPorId(producto.getIdUnidadMedida());
            String textoCantidad = detalle.getCantidad().stripTrailingZeros().toPlainString();
            String abreviatura = unidad != null ? unidad.getAbreviatura() : "";
            modeloDetalle.addRow(new Object[]{producto.getCodigo() + " — " + producto.getNombre(), textoCantidad + (abreviatura.isBlank() ? "" : " " + abreviatura), "✕"});
        }
    }

    private void eliminarDetalle(int fila) {
        if (fila < 0 || fila >= detalles.size()) {
            return;
        }
        detalles.remove(fila);
        refrescarTablaDetalle();
        actualizarResumen();
    }

    private void actualizarResumen() {
        txtCantProdDistintos.setText(String.valueOf(detalles.size()));
        BigDecimal totalUnidades = detalles.stream().map(DetalleOrdenCompra::getCantidad).filter(cantidad -> cantidad != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        txtCantUnidadesTotales.setText(totalUnidades.stripTrailingZeros().toPlainString());
        txtEstadoSolicitud.setText("Pendiente de Aprobación");
        txtEstadoSuperior.setText("• PENDIENTE");
        txtCantProduSeleccionados.setText(detalles.size() + (detalles.size() == 1 ? " Producto seleccionado" : " Productos seleccionados"));
    }

    private void limpiarEntradaProducto() {
        cmbProductos.setSelectedItem(null);
        txtProductoSeleccionado.setText("");
        txtUnidad.setText("");
        spnCantidad.setValue(BigDecimal.ONE);
    }

    private void registrarSolicitud() {
        if (!SesionUsuario.haySesion()) {
            JOptionPane.showMessageDialog(this, "No existe una sesión de usuario activa.", "Sesión", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Object seleccionadoProveedor = cmbProveedores.getSelectedItem();
        if (!(seleccionadoProveedor instanceof Proveedor proveedor)) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un proveedor.", "Proveedor", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (detalles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe agregar al menos un producto.", "Solicitud de Orden de Compra", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Desea registrar esta solicitud de orden de compra?", "Confirmar registro", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        OrdenCompra orden = new OrdenCompra(proveedor.getIdProveedor(), SesionUsuario.actual().getIdUsuario());
        for (DetalleOrdenCompra detalle : detalles) {
            orden.agregarDetalle(detalle);
        }
        RespuestaOperacion<Integer> respuesta = ordenCompraService.registrarSolicitud(orden);
        if (!respuesta.isExito()) {
            JOptionPane.showMessageDialog(this, respuesta.getMensaje(), "No se pudo registrar", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "Solicitud de orden de compra registrada correctamente.\n" + "N.º de solicitud: " + respuesta.getResultado(), "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        detalles.clear();
        cmbProveedores.setSelectedItem(null);
        cmbProductos.setSelectedItem(null);
        limpiarDatosProveedor();
        limpiarEntradaProducto();
        refrescarTablaDetalle();
        actualizarResumen();
    }

    private void cancelar() {
        if (!detalles.isEmpty()) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "Hay productos agregados.\n" + "¿Desea cancelar la solicitud?", "Cancelar solicitud", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
        }
        dispose();
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
        lblSolicitudOrdenCompra = new javax.swing.JLabel();
        lblConsultaDeObligacionesConProveedor = new javax.swing.JLabel();
        txtEstadoSuperior = new javax.swing.JTextField();
        lblSolicita = new javax.swing.JLabel();
        lblNombreApellidoUsuario = new javax.swing.JLabel();
        lblFechaActual = new javax.swing.JLabel();
        lblHoraActual = new javax.swing.JLabel();
        pblDatosProveedor = new javax.swing.JPanel();
        lblBuscarProveedor = new javax.swing.JLabel();
        cmbProveedores = new javax.swing.JComboBox<>();
        lblRuc = new javax.swing.JLabel();
        txtRuc = new javax.swing.JTextField();
        lblContacto = new javax.swing.JLabel();
        txtContacto = new javax.swing.JTextField();
        pnlProveedorEncontrado = new javax.swing.JPanel();
        lblNombreEmpresaYTipo = new javax.swing.JLabel();
        lblRucNroRuc = new javax.swing.JLabel();
        lblDireccionSedePrincipal = new javax.swing.JLabel();
        txtInicialesEmpresa = new javax.swing.JTextField();
        pnlProductos = new javax.swing.JPanel();
        lblBuscarProducto = new javax.swing.JLabel();
        cmbProductos = new javax.swing.JComboBox<>();
        lblCantidad = new javax.swing.JLabel();
        spnCantidad = new javax.swing.JSpinner();
        txtUnidad = new javax.swing.JTextField();
        btnAgregarProducto = new javax.swing.JButton();
        txtProductoSeleccionado = new javax.swing.JTextField();
        pnlDetalleSolicitud = new javax.swing.JPanel();
        spnlDetalleSolicitud = new javax.swing.JScrollPane();
        tblDetalleSolicitud = new javax.swing.JTable();
        separadorTablaDetalleSolicitud = new javax.swing.JSeparator();
        txtCantProduSeleccionados = new javax.swing.JLabel();
        pnlResumen = new javax.swing.JPanel();
        lblProductosDistintos = new javax.swing.JLabel();
        txtCantProdDistintos = new javax.swing.JTextField();
        lblUnidadesTotales = new javax.swing.JLabel();
        txtCantUnidadesTotales = new javax.swing.JTextField();
        lblEstado = new javax.swing.JLabel();
        txtEstadoSolicitud = new javax.swing.JTextField();
        pnlInferior = new javax.swing.JPanel();
        txtMensajeSolicitudEstadoPendiente = new javax.swing.JLabel();
        btnCancelar = new javax.swing.JButton();
        btnRegistrarSolicitud = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblSolicitudOrdenCompra.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblSolicitudOrdenCompra.setText("SOLICITUD DE ORDEN DE COMPRA");

        lblConsultaDeObligacionesConProveedor.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblConsultaDeObligacionesConProveedor.setText("Nueva solicitud dirigida a proveedor — sin registrar ");

        txtEstadoSuperior.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtEstadoSuperior.setText("• Pendiente");

        lblSolicita.setText("Solicita:");

        lblNombreApellidoUsuario.setText("Nombre Apellido");

        lblFechaActual.setText("21/08/2026");

        lblHoraActual.setText("10:40");

        javax.swing.GroupLayout pnlSuperiorLayout = new javax.swing.GroupLayout(pnlSuperior);
        pnlSuperior.setLayout(pnlSuperiorLayout);
        pnlSuperiorLayout.setHorizontalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSuperiorLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblConsultaDeObligacionesConProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblSolicitudOrdenCompra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addGap(109, 109, 109)
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(pnlSuperiorLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(lblFechaActual)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblHoraActual)
                                .addGap(6, 6, 6))
                            .addGroup(pnlSuperiorLayout.createSequentialGroup()
                                .addComponent(lblSolicita)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblNombreApellidoUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSuperiorLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtEstadoSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)))
                .addContainerGap())
        );
        pnlSuperiorLayout.setVerticalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSuperiorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblSolicitudOrdenCompra)
                    .addComponent(txtEstadoSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblConsultaDeObligacionesConProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSolicita)
                    .addComponent(lblNombreApellidoUsuario))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFechaActual)
                    .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pblDatosProveedor.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. DATOS DEL PROVEEDOR", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblBuscarProveedor.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblBuscarProveedor.setText("BUSCAR PROVEEDOR");

        cmbProveedores.setToolTipText("");

        lblRuc.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblRuc.setText("RUC");

        lblContacto.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblContacto.setText("CONTACTO");

        txtContacto.setText("Miguel Torres — 074 123456");

        pnlProveedorEncontrado.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblNombreEmpresaYTipo.setText("Nombre de la Empresa (Tipo de empresa)");

        lblRucNroRuc.setText("RUC 0123456789");

        lblDireccionSedePrincipal.setText("Dirección de Sede Principal.");

        javax.swing.GroupLayout pnlProveedorEncontradoLayout = new javax.swing.GroupLayout(pnlProveedorEncontrado);
        pnlProveedorEncontrado.setLayout(pnlProveedorEncontradoLayout);
        pnlProveedorEncontradoLayout.setHorizontalGroup(
            pnlProveedorEncontradoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProveedorEncontradoLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(txtInicialesEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlProveedorEncontradoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblNombreEmpresaYTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlProveedorEncontradoLayout.createSequentialGroup()
                        .addComponent(lblRucNroRuc)
                        .addGap(18, 18, 18)
                        .addComponent(lblDireccionSedePrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(177, Short.MAX_VALUE))
        );
        pnlProveedorEncontradoLayout.setVerticalGroup(
            pnlProveedorEncontradoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProveedorEncontradoLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(pnlProveedorEncontradoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtInicialesEmpresa)
                    .addGroup(pnlProveedorEncontradoLayout.createSequentialGroup()
                        .addComponent(lblNombreEmpresaYTipo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlProveedorEncontradoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblRucNroRuc)
                            .addComponent(lblDireccionSedePrincipal))))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pblDatosProveedorLayout = new javax.swing.GroupLayout(pblDatosProveedor);
        pblDatosProveedor.setLayout(pblDatosProveedorLayout);
        pblDatosProveedorLayout.setHorizontalGroup(
            pblDatosProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pblDatosProveedorLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(pblDatosProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pblDatosProveedorLayout.createSequentialGroup()
                        .addGroup(pblDatosProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pblDatosProveedorLayout.createSequentialGroup()
                                .addComponent(lblBuscarProveedor)
                                .addGap(107, 107, 107)
                                .addComponent(lblRuc, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pblDatosProveedorLayout.createSequentialGroup()
                                .addComponent(cmbProveedores, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(txtRuc, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(26, 26, 26)
                        .addGroup(pblDatosProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pblDatosProveedorLayout.createSequentialGroup()
                                .addComponent(lblContacto)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(txtContacto)))
                    .addComponent(pnlProveedorEncontrado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22))
        );
        pblDatosProveedorLayout.setVerticalGroup(
            pblDatosProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pblDatosProveedorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pblDatosProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pblDatosProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblBuscarProveedor)
                        .addComponent(lblRuc))
                    .addComponent(lblContacto))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pblDatosProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbProveedores, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtRuc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtContacto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlProveedorEncontrado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlProductos.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. PRODUCTOS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblBuscarProducto.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblBuscarProducto.setText("BUSCAR PRODUCTO");

        cmbProductos.setName(""); // NOI18N

        lblCantidad.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCantidad.setText("CANT.");

        txtUnidad.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtUnidad.setText("UND");

        btnAgregarProducto.setText("+ Agregar Producto");
        btnAgregarProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarProductoActionPerformed(evt);
            }
        });

        txtProductoSeleccionado.setText("Seleccionado: P-00231  — Porcelanato Beige 60×60");

        javax.swing.GroupLayout pnlProductosLayout = new javax.swing.GroupLayout(pnlProductos);
        pnlProductos.setLayout(pnlProductosLayout);
        pnlProductosLayout.setHorizontalGroup(
            pnlProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductosLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(pnlProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlProductosLayout.createSequentialGroup()
                        .addComponent(lblBuscarProducto)
                        .addGap(106, 106, 106)
                        .addComponent(lblCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlProductosLayout.createSequentialGroup()
                        .addGroup(pnlProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(pnlProductosLayout.createSequentialGroup()
                                .addComponent(cmbProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(spnCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(txtUnidad, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtProductoSeleccionado))
                        .addGap(18, 18, 18)
                        .addComponent(btnAgregarProducto)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlProductosLayout.setVerticalGroup(
            pnlProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBuscarProducto)
                    .addComponent(lblCantidad))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtUnidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregarProducto))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtProductoSeleccionado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        pnlDetalleSolicitud.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "03. DETALLE DE SOLICITUD", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        tblDetalleSolicitud.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "PRODUCTO", "CANTIDAD", "ACCIONES"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlDetalleSolicitud.setViewportView(tblDetalleSolicitud);

        txtCantProduSeleccionados.setText("3 Productos seleccionados");

        javax.swing.GroupLayout pnlDetalleSolicitudLayout = new javax.swing.GroupLayout(pnlDetalleSolicitud);
        pnlDetalleSolicitud.setLayout(pnlDetalleSolicitudLayout);
        pnlDetalleSolicitudLayout.setHorizontalGroup(
            pnlDetalleSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetalleSolicitudLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDetalleSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spnlDetalleSolicitud)
                    .addComponent(separadorTablaDetalleSolicitud)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDetalleSolicitudLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(txtCantProduSeleccionados)))
                .addContainerGap())
        );
        pnlDetalleSolicitudLayout.setVerticalGroup(
            pnlDetalleSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetalleSolicitudLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlDetalleSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separadorTablaDetalleSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCantProduSeleccionados)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlResumen.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "04. RESUMEN", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblProductosDistintos.setText("Productos Distintos");

        txtCantProdDistintos.setText("N");

        lblUnidadesTotales.setText("Unidades Totales");

        txtCantUnidadesTotales.setText("370");

        lblEstado.setText("Estado");

        txtEstadoSolicitud.setText("Pendiente de Aprobación");

        javax.swing.GroupLayout pnlResumenLayout = new javax.swing.GroupLayout(pnlResumen);
        pnlResumen.setLayout(pnlResumenLayout);
        pnlResumenLayout.setHorizontalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(lblProductosDistintos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCantProdDistintos, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblUnidadesTotales)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCantUnidadesTotales, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtEstadoSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlResumenLayout.setVerticalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProductosDistintos)
                    .addComponent(txtCantProdDistintos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblUnidadesTotales)
                    .addComponent(txtCantUnidadesTotales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEstado)
                    .addComponent(txtEstadoSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        txtMensajeSolicitudEstadoPendiente.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        txtMensajeSolicitudEstadoPendiente.setText("La solicitud quedará en estado PENDIENTE hasta su aprobación. ");

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        btnRegistrarSolicitud.setText("Registrar Solicitud");
        btnRegistrarSolicitud.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarSolicitudActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlInferiorLayout = new javax.swing.GroupLayout(pnlInferior);
        pnlInferior.setLayout(pnlInferiorLayout);
        pnlInferiorLayout.setHorizontalGroup(
            pnlInferiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInferiorLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtMensajeSolicitudEstadoPendiente)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCancelar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnRegistrarSolicitud)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlInferiorLayout.setVerticalGroup(
            pnlInferiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInferiorLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(pnlInferiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMensajeSolicitudEstadoPendiente)
                    .addComponent(btnCancelar)
                    .addComponent(btnRegistrarSolicitud))
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
                    .addComponent(pblDatosProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlProductos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDetalleSolicitud, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlResumen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlInferior, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pblDatosProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDetalleSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResumen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlInferior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAgregarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarProductoActionPerformed
        // TODO add your handling code here:
        agregarProducto();
    }//GEN-LAST:event_btnAgregarProductoActionPerformed

    private void btnRegistrarSolicitudActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarSolicitudActionPerformed
        // TODO add your handling code here:
        registrarSolicitud();
    }//GEN-LAST:event_btnRegistrarSolicitudActionPerformed

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
            java.util.logging.Logger.getLogger(FrmOrdenCompra.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmOrdenCompra.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmOrdenCompra.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmOrdenCompra.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmOrdenCompra dialog = new FrmOrdenCompra(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnAgregarProducto;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnRegistrarSolicitud;
    private javax.swing.JComboBox<Producto> cmbProductos;
    private javax.swing.JComboBox<Proveedor> cmbProveedores;
    private javax.swing.JLabel lblBuscarProducto;
    private javax.swing.JLabel lblBuscarProveedor;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblConsultaDeObligacionesConProveedor;
    private javax.swing.JLabel lblContacto;
    private javax.swing.JLabel lblDireccionSedePrincipal;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblFechaActual;
    private javax.swing.JLabel lblHoraActual;
    private javax.swing.JLabel lblNombreApellidoUsuario;
    private javax.swing.JLabel lblNombreEmpresaYTipo;
    private javax.swing.JLabel lblProductosDistintos;
    private javax.swing.JLabel lblRuc;
    private javax.swing.JLabel lblRucNroRuc;
    private javax.swing.JLabel lblSolicita;
    private javax.swing.JLabel lblSolicitudOrdenCompra;
    private javax.swing.JLabel lblUnidadesTotales;
    private javax.swing.JPanel pblDatosProveedor;
    private javax.swing.JPanel pnlDetalleSolicitud;
    private javax.swing.JPanel pnlInferior;
    private javax.swing.JPanel pnlProductos;
    private javax.swing.JPanel pnlProveedorEncontrado;
    private javax.swing.JPanel pnlResumen;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JSeparator separadorTablaDetalleSolicitud;
    private javax.swing.JSpinner spnCantidad;
    private javax.swing.JScrollPane spnlDetalleSolicitud;
    private javax.swing.JTable tblDetalleSolicitud;
    private javax.swing.JTextField txtCantProdDistintos;
    private javax.swing.JLabel txtCantProduSeleccionados;
    private javax.swing.JTextField txtCantUnidadesTotales;
    private javax.swing.JTextField txtContacto;
    private javax.swing.JTextField txtEstadoSolicitud;
    private javax.swing.JTextField txtEstadoSuperior;
    private javax.swing.JTextField txtInicialesEmpresa;
    private javax.swing.JLabel txtMensajeSolicitudEstadoPendiente;
    private javax.swing.JTextField txtProductoSeleccionado;
    private javax.swing.JTextField txtRuc;
    private javax.swing.JTextField txtUnidad;
    // End of variables declaration//GEN-END:variables
}
