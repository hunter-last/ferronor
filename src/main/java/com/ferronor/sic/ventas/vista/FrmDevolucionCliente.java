package com.ferronor.sic.ventas.vista;

import com.ferronor.sic.maestros.logica.ClienteService;
import com.ferronor.sic.maestros.logica.ProductoService;
import com.ferronor.sic.maestros.modelo.Cliente;
import com.ferronor.sic.maestros.modelo.Producto;
import com.ferronor.sic.procesos.ProcesoDevolucionVenta;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;
import com.ferronor.sic.ventas.logica.VentaService;
import com.ferronor.sic.ventas.modelo.DetalleVenta;
import com.ferronor.sic.ventas.modelo.DevolucionVenta;
import com.ferronor.sic.ventas.modelo.Venta;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrmDevolucionCliente extends javax.swing.JDialog {

    private final VentaService ventaService = ServiceFactory.ventaService();
    private final ClienteService clienteService = ServiceFactory.clienteService();
    private final ProductoService productoService = ServiceFactory.productoService();
    private final ProcesoDevolucionVenta procesoDevolucionVenta = ServiceFactory.procesoDevolucionVenta();

// Venta actualmente seleccionada en cmbVentas.
    private Venta ventaSeleccionada;

// Productos de la venta seleccionada, resueltos UNA sola vez al elegir la venta
// (no en el renderer) — idProducto -> Producto, para no repetir buscarPorId().
    private final Map<Integer, Producto> productosDeLaVenta = new HashMap<>();

// Colección temporal: lo que el usuario ha preparado para devolver, aún no registrado.
    private final List<DevolucionVenta> devolucionesPendientes = new ArrayList<>();

    private final DefaultTableModel modeloDetalleDevolucion = new DefaultTableModel(
            new Object[]{"PRODUCTO", "CANT. COMPRADA", "CANT. DEVUELTA", "MOTIVO"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public FrmDevolucionCliente(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        configurarComponentes();
        setLocationRelativeTo(getParent());

    }

    private void configurarComponentes() {
        lblNombreUsuario.setText(SesionUsuario.actual().getNombreCompleto());
        lblFechaHora.setText(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy - hh:mm a")));

        configurarTabla();
        configurarSpinners();
        cargarVentas();
        limpiarValidaciones();

        txtValEj1.setEditable(false);
        txtValEj2.setEditable(false);
        txtValEj3.setEditable(false);
    }

    private void configurarTabla() {
        tblDetalleDevolucion.setModel(modeloDetalleDevolucion);
    }

// spnCantVendida es de solo lectura; spnCantADevolver es el único editable.
    private void configurarSpinners() {
        spnCantVendida.setModel(new SpinnerNumberModel(0.0, 0.0, 999999.0, 1.0));
        spnCantVendida.setEnabled(false);

        spnCantADevolver.setModel(new SpinnerNumberModel(1.0, 0.0, 999999.0, 1.0));
    }

// cmbVentas representa objetos Venta. Venta no tiene toString() garantizado,
// así que se instala un renderer propio en vez de depender de él.
// La única representación legible disponible con los datos reales de Venta es
// su número de venta (idVenta) — no existe un campo de comprobante en el modelo.
    private void cargarVentas() {
        List<Venta> ventas = ventaService.listar();
        cmbVentas.setModel(new DefaultComboBoxModel<>(ventas.toArray(new Venta[0])));
        cmbVentas.setSelectedItem(null);

        cmbVentas.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                String texto = "";
                if (value instanceof Venta venta) {
                    String fecha = venta.getFecha() == null ? ""
                            : venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    texto = "Venta N° " + venta.getIdVenta() + " — " + fecha;
                }
                return super.getListCellRendererComponent(list, texto, index, isSelected, cellHasFocus);
            }
        });
    }

    // cmbProductosVenta representa DetalleVenta (no Producto suelto): ya trae consigo
    // la cantidad vendida real de esa línea. El nombre/código del producto se resuelve
    // una sola vez aquí (vía productosDeLaVenta), no repetidamente en el renderer.
    private void cargarProductosDeVenta(Venta venta) {
        productosDeLaVenta.clear();
        DefaultComboBoxModel<DetalleVenta> modeloProductos = new DefaultComboBoxModel<>();

        for (DetalleVenta detalle : venta.getDetalles()) {
            productosDeLaVenta.computeIfAbsent(detalle.getIdProducto(), productoService::buscarPorId);
            modeloProductos.addElement(detalle);
        }

        cmbProductosVenta.setModel(modeloProductos);
        cmbProductosVenta.setSelectedItem(null);

        cmbProductosVenta.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                String texto = "";
                if (value instanceof DetalleVenta detalle) {
                    Producto producto = productosDeLaVenta.get(detalle.getIdProducto());
                    texto = (producto != null)
                            ? producto.getCodigo() + " — " + producto.getNombre()
                            : "Producto #" + detalle.getIdProducto();
                }
                return super.getListCellRendererComponent(list, texto, index, isSelected, cellHasFocus);
            }
        });
    }

    private void agregarFilaTabla(DetalleVenta detalle, BigDecimal cantidadDevolver, String motivo) {
        Producto producto = productosDeLaVenta.get(detalle.getIdProducto());
        String nombreProducto = (producto != null)
                ? producto.getCodigo() + " — " + producto.getNombre()
                : "Producto #" + detalle.getIdProducto();

        modeloDetalleDevolucion.addRow(new Object[]{
            nombreProducto,
            detalle.getCantidad().toPlainString(),
            cantidadDevolver.toPlainString(),
            motivo
        });
    }

    private void limpiarFormulario() {
        devolucionesPendientes.clear();
        modeloDetalleDevolucion.setRowCount(0);
        ventaSeleccionada = null;
        productosDeLaVenta.clear();
        cmbVentas.setSelectedItem(null);
        cmbProductosVenta.setModel(new DefaultComboBoxModel<>());
        lblNombreCliente.setText("—");
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

    private void seleccionarVenta(Venta venta) {
        ventaSeleccionada = venta;

        Cliente cliente = clienteService.buscarPorId(venta.getIdCliente());

        lblNombreCliente.setText(
                cliente != null ? cliente.getNombreRazonSocial() : "—"
        );

        lblRuc.setText(
                cliente != null
                        ? cliente.getTipoDocumento() + " " + cliente.getNumeroDocumento()
                        : "—"
        );

        lblFechaCompra.setText(
                venta.getFecha() == null
                ? "—"
                : venta.getFecha().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                )
        );

        cargarProductosDeVenta(venta);

        spnCantVendida.setValue(0.0);
        spnCantADevolver.setValue(1.0);
        txtaMotivo.setText("");
        limpiarValidaciones();
    }

    private void mostrarDetalleSeleccionado(DetalleVenta detalle) {
        spnCantVendida.setValue(
                detalle.getCantidad().doubleValue()
        );
    }

    private void agregarProducto() {
        limpiarValidaciones();

        if (ventaSeleccionada == null) {
            mostrarValidacion("Selecciona una venta.");
            return;
        }

        Object productoSel = cmbProductosVenta.getSelectedItem();

        if (!(productoSel instanceof DetalleVenta detalle)) {
            mostrarValidacion("Selecciona un producto.");
            return;
        }

        BigDecimal cantidadDevolver
                = BigDecimal.valueOf((Double) spnCantADevolver.getValue());

        if (cantidadDevolver.compareTo(BigDecimal.ZERO) <= 0) {
            mostrarValidacion("La cantidad a devolver debe ser mayor a cero.");
            return;
        }

        if (cantidadDevolver.compareTo(detalle.getCantidad()) > 0) {
            mostrarValidacion(
                    "La cantidad a devolver no puede superar la cantidad vendida."
            );
            return;
        }

        String motivo = txtaMotivo.getText() == null
                ? ""
                : txtaMotivo.getText().trim();

        if (motivo.isEmpty()) {
            mostrarValidacion("El motivo de la devolución es obligatorio.");
            return;
        }

        boolean yaExiste = devolucionesPendientes.stream()
                .anyMatch(d
                        -> d.getIdVenta() == ventaSeleccionada.getIdVenta()
                && d.getIdProducto() == detalle.getIdProducto());

        if (yaExiste) {
            mostrarValidacion(
                    "Este producto ya fue agregado a la devolución."
            );
            return;
        }

        DevolucionVenta devolucion = new DevolucionVenta(
                ventaSeleccionada.getIdVenta(),
                detalle.getIdProducto(),
                cantidadDevolver,
                motivo,
                SesionUsuario.actual().getIdUsuario()
        );

        devolucionesPendientes.add(devolucion);

        agregarFilaTabla(detalle, cantidadDevolver, motivo);

        cmbProductosVenta.setSelectedItem(null);
        spnCantVendida.setValue(0.0);
        spnCantADevolver.setValue(1.0);
        txtaMotivo.setText("");
    }

    private void registrarDevolucion() {
        limpiarValidaciones();

        if (ventaSeleccionada == null) {
            mostrarValidacion("Selecciona una venta.");
            return;
        }

        if (devolucionesPendientes.isEmpty()) {
            mostrarValidacion("Agrega al menos un producto a la devolución.");
            return;
        }

        for (DevolucionVenta devolucion : devolucionesPendientes) {
            RespuestaOperacion<Void> resultado
                    = procesoDevolucionVenta.registrarDevolucion(devolucion);

            if (!resultado.isExito()) {
                mostrarValidacion(resultado.getMensaje());
                return;
            }
        }

        JOptionPane.showMessageDialog(
                this,
                "Devolución registrada correctamente."
        );

        limpiarFormulario();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSuperior = new javax.swing.JPanel();
        lblUsuario = new javax.swing.JLabel();
        lblNombreUsuario = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        lblFechaHora = new javax.swing.JLabel();
        lblDevolucionAProveedor = new javax.swing.JLabel();
        pnlCompra = new javax.swing.JPanel();
        cmbVentas = new javax.swing.JComboBox<>();
        lblCompraAProveedor = new javax.swing.JLabel();
        pnlCliente = new javax.swing.JPanel();
        lblTextoNombre = new javax.swing.JLabel();
        lblTextoDNI = new javax.swing.JLabel();
        lblTextoFechaCompra = new javax.swing.JLabel();
        lblNombreCliente = new javax.swing.JLabel();
        lblRuc = new javax.swing.JLabel();
        lblFechaCompra = new javax.swing.JLabel();
        pnlProducto = new javax.swing.JPanel();
        cmbProductosVenta = new javax.swing.JComboBox<>();
        lblProductoDeEstaVenta = new javax.swing.JLabel();
        lblMotivo = new javax.swing.JLabel();
        spnlTxtaMotivo = new javax.swing.JScrollPane();
        txtaMotivo = new javax.swing.JTextArea();
        lblCantVendida = new javax.swing.JLabel();
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

        lblNombreUsuario.setText("Nombre A. (sesión activa)");

        lblFecha.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFecha.setText("FECHA:");

        lblFechaHora.setText("dd/mm/yy - 19:18 p.m.");

        lblDevolucionAProveedor.setFont(new java.awt.Font("Yu Gothic UI", 0, 14)); // NOI18N
        lblDevolucionAProveedor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDevolucionAProveedor.setText("DEVOLUCIÓN A CLIENTE");

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
                    .addComponent(lblNombreUsuario))
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
                    .addComponent(lblNombreUsuario)
                    .addComponent(lblFechaHora))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(lblDevolucionAProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pnlCompra.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. VENTA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        cmbVentas.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbVentasItemStateChanged(evt);
            }
        });

        lblCompraAProveedor.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblCompraAProveedor.setText("VENTA");

        pnlCliente.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "CLIENTE", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblTextoNombre.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblTextoNombre.setText("NOMBRE");

        lblTextoDNI.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblTextoDNI.setText("DNI");

        lblTextoFechaCompra.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblTextoFechaCompra.setText("FECHA VENTA");

        lblNombreCliente.setText("Nombre del Cliente (Persona o Empresa)");

        lblRuc.setText("20603381124 ");

        lblFechaCompra.setText("09/08/2026 ");

        javax.swing.GroupLayout pnlClienteLayout = new javax.swing.GroupLayout(pnlCliente);
        pnlCliente.setLayout(pnlClienteLayout);
        pnlClienteLayout.setHorizontalGroup(
            pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(lblTextoFechaCompra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblTextoNombre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lblTextoDNI, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFechaCompra, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(lblRuc, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblNombreCliente, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, 0))
        );
        pnlClienteLayout.setVerticalGroup(
            pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlClienteLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTextoNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNombreCliente))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRuc)
                    .addComponent(lblTextoDNI))
                .addGap(6, 6, 6)
                .addGroup(pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
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
                    .addComponent(cmbVentas, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlCompraLayout.setVerticalGroup(
            pnlCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlCompraLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCompraAProveedor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cmbVentas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlCompraLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(pnlCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pnlProducto.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. PRODUCTO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        cmbProductosVenta.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbProductosVentaItemStateChanged(evt);
            }
        });

        lblProductoDeEstaVenta.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblProductoDeEstaVenta.setText("PRODUCTO DE ESTA VENTA");

        lblMotivo.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblMotivo.setText("MOTIVO");

        txtaMotivo.setColumns(20);
        txtaMotivo.setRows(5);
        spnlTxtaMotivo.setViewportView(txtaMotivo);

        lblCantVendida.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblCantVendida.setText("Cant. Vendida");

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
                            .addComponent(cmbProductosVenta, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblProductoDeEstaVenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblMotivo, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(pnlProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblCantVendida, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
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
                                .addComponent(lblProductoDeEstaVenta)
                                .addComponent(lblCantVendida)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbProductosVenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    private void cmbVentasItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbVentasItemStateChanged
        // TODO add your handling code here:
        if (evt.getStateChange() != ItemEvent.SELECTED) {
            return;
        }

        if (evt.getItem() instanceof Venta venta) {
            seleccionarVenta(venta);
        }
    }//GEN-LAST:event_cmbVentasItemStateChanged

    private void cmbProductosVentaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbProductosVentaItemStateChanged
        // TODO add your handling code here:
        if (evt.getStateChange() != ItemEvent.SELECTED) {
            return;
        }

        if (evt.getItem() instanceof DetalleVenta detalle) {
            mostrarDetalleSeleccionado(detalle);
        }
    }//GEN-LAST:event_cmbProductosVentaItemStateChanged

    private void btnAgregarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarProductoActionPerformed
        // TODO add your handling code here:
        agregarProducto();
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
            java.util.logging.Logger.getLogger(FrmDevolucionCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmDevolucionCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmDevolucionCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmDevolucionCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmDevolucionCliente dialog = new FrmDevolucionCliente(new javax.swing.JFrame(), true);
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
    private javax.swing.JComboBox<DetalleVenta> cmbProductosVenta;
    private javax.swing.JComboBox<Venta> cmbVentas;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblCantADevolver;
    private javax.swing.JLabel lblCantVendida;
    private javax.swing.JLabel lblCompraAProveedor;
    private javax.swing.JLabel lblDevolucionAProveedor;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblFechaCompra;
    private javax.swing.JLabel lblFechaHora;
    private javax.swing.JLabel lblMotivo;
    private javax.swing.JLabel lblNombreCliente;
    private javax.swing.JLabel lblNombreUsuario;
    private javax.swing.JLabel lblProductoDeEstaVenta;
    private javax.swing.JLabel lblRuc;
    private javax.swing.JLabel lblTextoDNI;
    private javax.swing.JLabel lblTextoFechaCompra;
    private javax.swing.JLabel lblTextoNombre;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPanel pnlCliente;
    private javax.swing.JPanel pnlCompra;
    private javax.swing.JPanel pnlDetalleDevolucion;
    private javax.swing.JPanel pnlProducto;
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
