package com.ferronor.sic.compras.vista;

import com.ferronor.sic.compras.logica.CompraService;
import com.ferronor.sic.compras.modelo.Compra;
import com.ferronor.sic.compras.modelo.DetalleCompra;
import com.ferronor.sic.compras.modelo.DevolucionCompra;
import com.ferronor.sic.maestros.logica.ProductoService;
import com.ferronor.sic.maestros.logica.ProveedorService;
import com.ferronor.sic.maestros.modelo.Producto;
import com.ferronor.sic.maestros.modelo.Proveedor;
import com.ferronor.sic.procesos.ProcesoDevolucionCompra;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FrmDevolucionProveedor extends javax.swing.JDialog {

    // Services y Proceso obtenidos vía ServiceFactory — nunca instanciados a mano
// ni accedidos a través de DAO directo.
    private final CompraService compraService = ServiceFactory.compraService();
    private final ProveedorService proveedorService = ServiceFactory.proveedorService();
    private final ProductoService productoService = ServiceFactory.productoService();
    private final ProcesoDevolucionCompra procesoDevolucionCompra = ServiceFactory.procesoDevolucionCompra();

// Compra actualmente seleccionada en cmbCompras (fuente de verdad de sus detalles).
    private Compra compraSeleccionada;

// Colección temporal: lo que el usuario ha preparado para devolver, todavía no registrado.
    private final List<DevolucionCompra> devolucionesPendientes = new ArrayList<>();

    private final DefaultTableModel modeloDetalleDevolucion = new DefaultTableModel(
            new Object[]{"PRODUCTO", "CANT. COMPRADA", "CANT. DEVUELTA", "MOTIVO"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public FrmDevolucionProveedor(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        configurarComponentes();
        setLocationRelativeTo(getParent());
    }

    private void configurarComponentes() {
        lblNombreVend.setText(SesionUsuario.actual().getNombreCompleto());
        lblFechaHora.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy - hh:mm a")));

        configurarTabla();
        configurarSpinners();
        configurarComboCompras();
        configurarComboProductosCompra();
        limpiarValidaciones();

        txtValEj1.setEditable(false);
        txtValEj2.setEditable(false);
        txtValEj3.setEditable(false);

    }

    private void configurarTabla() {
        tblDetalleDevolucion.setModel(modeloDetalleDevolucion);
    }

// spnCantComprada es de solo lectura: el usuario no debe poder tocarlo.
// spnCantADevolver sí es editable.
    private void configurarSpinners() {
        spnCantVendida.setModel(new SpinnerNumberModel(0.0, 0.0, 999999.0, 1.0));
        spnCantVendida.setEnabled(false);

        spnCantADevolver.setModel(new SpinnerNumberModel(1.0, 0.0, 999999.0, 1.0));
    }

// cmbCompras representa objetos Compra reales, sin ComboAutoFiltro (es una lista
// acotada, no una búsqueda de catálogo). Compra no tiene toString(), así que se
// instala un renderer propio en vez de depender de él.
    private void configurarComboCompras() {
        List<Compra> compras = compraService.listar();
        cmbCompras.setModel(new DefaultComboBoxModel<>(compras.toArray(new Compra[0])));
        cmbCompras.setSelectedItem(null);

        cmbCompras.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                String texto = "";
                if (value instanceof Compra compra) {
                    String fecha = compra.getFecha() == null ? ""
                            : compra.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    texto = compra.getNumeroFactura() + " — " + fecha;
                }
                return super.getListCellRendererComponent(list, texto, index, isSelected, cellHasFocus);
            }
        });

        cmbCompras.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                if (e.getItem() instanceof Compra compra) {
                    seleccionarCompra(compra);
                }
            }
        });
    }

// cmbProductosCompra representa DetalleCompra (no Producto suelto): así ya trae
// consigo la cantidad comprada real de esa línea, sin inventar un wrapper nuevo.
// Se recarga cada vez que cambia la compra seleccionada — nunca con el catálogo completo.
    private void configurarComboProductosCompra() {
        cmbProductosCompra.setModel(new DefaultComboBoxModel<>());
        cmbProductosCompra.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                String texto = "";
                if (value instanceof DetalleCompra detalle) {
                    Producto producto = productoService.buscarPorId(detalle.getIdProducto());
                    texto = (producto != null)
                            ? producto.getCodigo() + " — " + producto.getNombre()
                            : "Producto #" + detalle.getIdProducto();
                }
                return super.getListCellRendererComponent(list, texto, index, isSelected, cellHasFocus);
            }
        });

        cmbProductosCompra.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                if (e.getItem() instanceof DetalleCompra detalle) {
                    mostrarCantidadComprada(detalle);
                }
            }
        });
    }

    private void seleccionarCompra(Compra compra) {
        compraSeleccionada = compra;

        Proveedor proveedor = proveedorService.buscarPorId(compra.getIdProveedor());
        lblNombreProveedor.setText(proveedor != null ? proveedor.getRazonSocial() : "—");
        lblRuc.setText(proveedor != null ? proveedor.getRuc() : "—");
        lblFechaCompra.setText(compra.getFecha() == null ? "—"
                : compra.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        DefaultComboBoxModel<DetalleCompra> modeloProductos = new DefaultComboBoxModel<>();
        for (DetalleCompra detalle : compra.getDetalles()) {
            modeloProductos.addElement(detalle);
        }
        cmbProductosCompra.setModel(modeloProductos);
        cmbProductosCompra.setSelectedItem(null);

        spnCantVendida.setValue(0.0);
        limpiarValidaciones();
    }

    private void mostrarCantidadComprada(DetalleCompra detalle) {
        spnCantVendida.setValue(detalle.getCantidad().doubleValue());
    }

    private void agregarProductoADevolucion() {
        limpiarValidaciones();

        if (compraSeleccionada == null) {
            mostrarValidacion("Seleccione una compra.");
            return;
        }

        Object productoSel = cmbProductosCompra.getSelectedItem();
        if (!(productoSel instanceof DetalleCompra detalle)) {
            mostrarValidacion("Seleccione un producto.");
            return;
        }

        BigDecimal cantidadDevolver = BigDecimal.valueOf((Double) spnCantADevolver.getValue());
        if (cantidadDevolver.compareTo(BigDecimal.ZERO) <= 0) {
            mostrarValidacion("La cantidad a devolver debe ser mayor que cero.");
            return;
        }
        if (cantidadDevolver.compareTo(detalle.getCantidad()) > 0) {
            mostrarValidacion("La cantidad a devolver no puede superar la cantidad comprada.");
            return;
        }

        String motivo = txtaMotivo.getText() == null ? "" : txtaMotivo.getText().trim();
        if (motivo.isEmpty()) {
            mostrarValidacion("El motivo es obligatorio.");
            return;
        }

        boolean yaExiste = devolucionesPendientes.stream()
                .anyMatch(d -> d.getIdCompra() == compraSeleccionada.getIdCompra()
                && d.getIdProducto() == detalle.getIdProducto());
        if (yaExiste) {
            mostrarValidacion("Este producto ya fue agregado a la devolución.");
            return;
        }

        DevolucionCompra devolucion = new DevolucionCompra(
                compraSeleccionada.getIdCompra(),
                detalle.getIdProducto(),
                cantidadDevolver,
                motivo,
                SesionUsuario.actual().getIdUsuario());

        devolucionesPendientes.add(devolucion);

        Producto producto = productoService.buscarPorId(detalle.getIdProducto());
        String nombreProducto = (producto != null)
                ? producto.getCodigo() + " — " + producto.getNombre()
                : "Producto #" + detalle.getIdProducto();

        modeloDetalleDevolucion.addRow(new Object[]{
            nombreProducto,
            detalle.getCantidad().toPlainString(),
            cantidadDevolver.toPlainString(),
            motivo
        });

        cmbProductosCompra.setSelectedItem(null);
        spnCantVendida.setValue(0.0);
        spnCantADevolver.setValue(1.0);
        txtaMotivo.setText("");
    }

// Cada elemento temporal es una fila de devolucion_compra; no existe un método
// batch en el contrato real, así que se invoca una vez por cada pendiente.
    private void registrarDevolucion() {
        limpiarValidaciones();

        if (compraSeleccionada == null) {
            mostrarValidacion("Seleccione una compra.");
            return;
        }
        if (devolucionesPendientes.isEmpty()) {
            mostrarValidacion("Agregue al menos un producto a la devolución.");
            return;
        }

        for (DevolucionCompra devolucion : devolucionesPendientes) {
            RespuestaOperacion<Void> respuesta = procesoDevolucionCompra.registrarDevolucion(devolucion);
            if (!respuesta.isExito()) {
                mostrarValidacion(respuesta.getMensaje());
                return; // no se limpia el formulario: el usuario ve qué falló
            }
        }

        JOptionPane.showMessageDialog(this, "Devolución registrada correctamente.");
        limpiarFormulario();
        dispose();
    }

    private void limpiarFormulario() {
        devolucionesPendientes.clear();
        modeloDetalleDevolucion.setRowCount(0);
        compraSeleccionada = null;
        cmbCompras.setSelectedItem(null);
        cmbProductosCompra.setModel(new DefaultComboBoxModel<>());
        lblNombreProveedor.setText("—");
        lblRuc.setText("—");
        lblFechaCompra.setText("—");
        spnCantVendida.setValue(0.0);
        spnCantADevolver.setValue(1.0);
        txtaMotivo.setText("");
        limpiarValidaciones();
    }

    private void mostrarValidacion(String mensaje) {
        txtValEj1.setText(mensaje);
        txtValEj2.setText("");
        txtValEj3.setText("");
    }

    private void limpiarValidaciones() {
        txtValEj1.setText("");
        txtValEj2.setText("");
        txtValEj3.setText("");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSuperior = new javax.swing.JPanel();
        lblUsuario = new javax.swing.JLabel();
        lblNombreVend = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        lblFechaHora = new javax.swing.JLabel();
        lblDevolucionAProveedor = new javax.swing.JLabel();
        pnlCompra = new javax.swing.JPanel();
        cmbCompras = new javax.swing.JComboBox<>();
        lblCompraAProveedor = new javax.swing.JLabel();
        pnlProveedor = new javax.swing.JPanel();
        lblTextoNombre = new javax.swing.JLabel();
        lblTextoRuc = new javax.swing.JLabel();
        lblTextoFechaCompra = new javax.swing.JLabel();
        lblNombreProveedor = new javax.swing.JLabel();
        lblRuc = new javax.swing.JLabel();
        lblFechaCompra = new javax.swing.JLabel();
        pnlProducto = new javax.swing.JPanel();
        cmbProductosCompra = new javax.swing.JComboBox<>();
        lblProductoDeEstaCompra = new javax.swing.JLabel();
        lblMotivo = new javax.swing.JLabel();
        spnlTxtaMotivo = new javax.swing.JScrollPane();
        txtaMotivo = new javax.swing.JTextArea();
        lblCantComprada = new javax.swing.JLabel();
        spnCantVendida = new javax.swing.JSpinner();
        lblCantADevolver = new javax.swing.JLabel();
        spnCantADevolver = new javax.swing.JSpinner();
        btnAgregarProducto = new javax.swing.JButton();
        pnlDetalleDevolucion = new javax.swing.JPanel();
        spnlTblDetalleDevolucion = new javax.swing.JScrollPane();
        tblDetalleDevolucion = new javax.swing.JTable();
        pnlValidaciones = new javax.swing.JPanel();
        txtValEj1 = new javax.swing.JTextField();
        txtValEj2 = new javax.swing.JTextField();
        txtValEj3 = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        btnRegistrarDevolucion = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblUsuario.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblUsuario.setText("USUARIO:");

        lblNombreVend.setText("Nombre A. (sesión activa)");

        lblFecha.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFecha.setText("FECHA:");

        lblFechaHora.setText("dd/mm/yy - 19:18 p.m.");

        lblDevolucionAProveedor.setFont(new java.awt.Font("Yu Gothic UI", 0, 14)); // NOI18N
        lblDevolucionAProveedor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDevolucionAProveedor.setText("DEVOLUCIÓN A PROVEEDOR");

        javax.swing.GroupLayout pnlSuperiorLayout = new javax.swing.GroupLayout(pnlSuperior);
        pnlSuperior.setLayout(pnlSuperiorLayout);
        pnlSuperiorLayout.setHorizontalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSuperiorLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblDevolucionAProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUsuario)
                    .addComponent(lblNombreVend))
                .addGap(23, 23, 23)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFecha)
                    .addComponent(lblFechaHora))
                .addGap(15, 15, 15))
        );
        pnlSuperiorLayout.setVerticalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSuperiorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUsuario)
                    .addComponent(lblFecha))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNombreVend)
                    .addComponent(lblFechaHora))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(lblDevolucionAProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pnlCompra.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. COMPRA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        cmbCompras.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbComprasItemStateChanged(evt);
            }
        });

        lblCompraAProveedor.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblCompraAProveedor.setText("COMPRA A DEVOLVER");

        pnlProveedor.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "PROVEEDOR", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblTextoNombre.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblTextoNombre.setText("NOMBRE");

        lblTextoRuc.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblTextoRuc.setText("RUC");

        lblTextoFechaCompra.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblTextoFechaCompra.setText("FECHA COMPRA");

        lblNombreProveedor.setText("Nombre del Cliente (Persona o Empresa)");

        lblRuc.setText("20603381124 ");

        lblFechaCompra.setText("09/08/2026 ");

        javax.swing.GroupLayout pnlProveedorLayout = new javax.swing.GroupLayout(pnlProveedor);
        pnlProveedor.setLayout(pnlProveedorLayout);
        pnlProveedorLayout.setHorizontalGroup(
            pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProveedorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(lblTextoFechaCompra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblTextoNombre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lblTextoRuc, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFechaCompra, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(lblRuc, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblNombreProveedor, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, 0))
        );
        pnlProveedorLayout.setVerticalGroup(
            pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProveedorLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTextoNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNombreProveedor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRuc)
                    .addComponent(lblTextoRuc))
                .addGap(6, 6, 6)
                .addGroup(pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTextoFechaCompra)
                    .addComponent(lblFechaCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlCompraLayout = new javax.swing.GroupLayout(pnlCompra);
        pnlCompra.setLayout(pnlCompraLayout);
        pnlCompraLayout.setHorizontalGroup(
            pnlCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCompraLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCompraAProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbCompras, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlCompraLayout.setVerticalGroup(
            pnlCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlCompraLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCompraAProveedor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cmbCompras, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlCompraLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(pnlProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pnlProducto.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. PRODUCTO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        cmbProductosCompra.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbProductosCompraItemStateChanged(evt);
            }
        });

        lblProductoDeEstaCompra.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblProductoDeEstaCompra.setText("PRODUCTO DE ESTA COMPRA");

        lblMotivo.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblMotivo.setText("MOTIVO");

        txtaMotivo.setColumns(20);
        txtaMotivo.setRows(5);
        spnlTxtaMotivo.setViewportView(txtaMotivo);

        lblCantComprada.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblCantComprada.setText("Cant. Comprada");

        lblCantADevolver.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblCantADevolver.setText("Cant. a Devolver");

        btnAgregarProducto.setBackground(new java.awt.Color(102, 51, 0));
        btnAgregarProducto.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAgregarProducto.setForeground(new java.awt.Color(255, 153, 51));
        btnAgregarProducto.setText("+ Agregar Producto");
        btnAgregarProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarProductoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlProductoLayout = new javax.swing.GroupLayout(pnlProducto);
        pnlProducto.setLayout(pnlProductoLayout);
        pnlProductoLayout.setHorizontalGroup(
            pnlProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spnlTxtaMotivo)
                    .addGroup(pnlProductoLayout.createSequentialGroup()
                        .addGroup(pnlProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cmbProductosCompra, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblProductoDeEstaCompra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblMotivo, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(pnlProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblCantComprada, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(spnCantVendida))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblCantADevolver, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                            .addComponent(spnCantADevolver))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAgregarProducto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlProductoLayout.setVerticalGroup(
            pnlProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductoLayout.createSequentialGroup()
                .addGroup(pnlProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlProductoLayout.createSequentialGroup()
                        .addGroup(pnlProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCantADevolver, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblProductoDeEstaCompra)
                                .addComponent(lblCantComprada)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbProductosCompra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spnCantVendida, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spnCantADevolver, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblMotivo))
                    .addGroup(pnlProductoLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnAgregarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnlTxtaMotivo, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        pnlDetalleDevolucion.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "03. DETALLE DE DEVOLUCIÓN", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        tblDetalleDevolucion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "PRODUCTO", "CANT. COMPRADA", "CANT. DEVUELTA", "MOTIVO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlTblDetalleDevolucion.setViewportView(tblDetalleDevolucion);

        javax.swing.GroupLayout pnlDetalleDevolucionLayout = new javax.swing.GroupLayout(pnlDetalleDevolucion);
        pnlDetalleDevolucion.setLayout(pnlDetalleDevolucionLayout);
        pnlDetalleDevolucionLayout.setHorizontalGroup(
            pnlDetalleDevolucionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetalleDevolucionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlTblDetalleDevolucion)
                .addContainerGap())
        );
        pnlDetalleDevolucionLayout.setVerticalGroup(
            pnlDetalleDevolucionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetalleDevolucionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlTblDetalleDevolucion, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                .addContainerGap())
        );

        pnlValidaciones.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "04. VALIDACIONES", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        javax.swing.GroupLayout pnlValidacionesLayout = new javax.swing.GroupLayout(pnlValidaciones);
        pnlValidaciones.setLayout(pnlValidacionesLayout);
        pnlValidacionesLayout.setHorizontalGroup(
            pnlValidacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlValidacionesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlValidacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtValEj1)
                    .addComponent(txtValEj2)
                    .addComponent(txtValEj3))
                .addContainerGap())
        );
        pnlValidacionesLayout.setVerticalGroup(
            pnlValidacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlValidacionesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtValEj1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtValEj2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtValEj3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        btnRegistrarDevolucion.setBackground(new java.awt.Color(153, 51, 0));
        btnRegistrarDevolucion.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        btnRegistrarDevolucion.setText("REGISTRAR DEVOLUCION");
        btnRegistrarDevolucion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarDevolucionActionPerformed(evt);
            }
        });

        btnCancelar.setBackground(new java.awt.Color(51, 51, 51));
        btnCancelar.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        btnCancelar.setText("CANCELAR");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCancelar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRegistrarDevolucion)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRegistrarDevolucion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(pnlSuperior, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDetalleDevolucion, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlValidaciones, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlCompra, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlProducto, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlCompra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDetalleDevolucion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlValidaciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbComprasItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbComprasItemStateChanged
        // TODO add your handling code here:
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED && evt.getItem() instanceof Compra compra) {
            seleccionarCompra(compra);
        }
    }//GEN-LAST:event_cmbComprasItemStateChanged

    private void cmbProductosCompraItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbProductosCompraItemStateChanged
        // TODO add your handling code here:
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED && evt.getItem() instanceof DetalleCompra detalle) {
            mostrarCantidadComprada(detalle);
        }
    }//GEN-LAST:event_cmbProductosCompraItemStateChanged

    private void btnAgregarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarProductoActionPerformed
        // TODO add your handling code here:
        agregarProductoADevolucion();

    }//GEN-LAST:event_btnAgregarProductoActionPerformed

    private void btnRegistrarDevolucionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarDevolucionActionPerformed
        // TODO add your handling code here:
        registrarDevolucion();

    }//GEN-LAST:event_btnRegistrarDevolucionActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        // TODO add your handling code here:
        dispose();

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
            java.util.logging.Logger.getLogger(FrmDevolucionProveedor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmDevolucionProveedor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmDevolucionProveedor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmDevolucionProveedor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmDevolucionProveedor dialog = new FrmDevolucionProveedor(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnRegistrarDevolucion;
    private javax.swing.JComboBox<Compra> cmbCompras;
    private javax.swing.JComboBox<DetalleCompra> cmbProductosCompra;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblCantADevolver;
    private javax.swing.JLabel lblCantComprada;
    private javax.swing.JLabel lblCompraAProveedor;
    private javax.swing.JLabel lblDevolucionAProveedor;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblFechaCompra;
    private javax.swing.JLabel lblFechaHora;
    private javax.swing.JLabel lblMotivo;
    private javax.swing.JLabel lblNombreProveedor;
    private javax.swing.JLabel lblNombreVend;
    private javax.swing.JLabel lblProductoDeEstaCompra;
    private javax.swing.JLabel lblRuc;
    private javax.swing.JLabel lblTextoFechaCompra;
    private javax.swing.JLabel lblTextoNombre;
    private javax.swing.JLabel lblTextoRuc;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPanel pnlCompra;
    private javax.swing.JPanel pnlDetalleDevolucion;
    private javax.swing.JPanel pnlProducto;
    private javax.swing.JPanel pnlProveedor;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JPanel pnlValidaciones;
    private javax.swing.JSpinner spnCantADevolver;
    private javax.swing.JSpinner spnCantVendida;
    private javax.swing.JScrollPane spnlTblDetalleDevolucion;
    private javax.swing.JScrollPane spnlTxtaMotivo;
    private javax.swing.JTable tblDetalleDevolucion;
    private javax.swing.JTextField txtValEj1;
    private javax.swing.JTextField txtValEj2;
    private javax.swing.JTextField txtValEj3;
    private javax.swing.JTextArea txtaMotivo;
    // End of variables declaration//GEN-END:variables
}
