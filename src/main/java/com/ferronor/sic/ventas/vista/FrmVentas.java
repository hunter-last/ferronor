/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.ferronor.sic.ventas.vista;

import com.ferronor.sic.maestros.logica.ClienteService;
import com.ferronor.sic.maestros.logica.FormaPagoService;
import com.ferronor.sic.maestros.logica.ProductoService;
import com.ferronor.sic.maestros.logica.TipoComprobanteService;
import com.ferronor.sic.maestros.modelo.Cliente;
import com.ferronor.sic.maestros.modelo.FormaPago;
import com.ferronor.sic.maestros.modelo.Producto;
import com.ferronor.sic.maestros.modelo.TipoComprobante;
import com.ferronor.sic.procesos.ProcesoVenta;
import com.ferronor.sic.shared.FrmBase;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;
import com.ferronor.sic.shared.ui.ComboAutoFiltro;
import com.ferronor.sic.tesoreria.logica.TesoreriaService;
import com.ferronor.sic.tesoreria.modelo.Caja;
import com.ferronor.sic.tesoreria.modelo.CuentaBancaria;
import com.ferronor.sic.util.CalculadoraImpuestos;
import com.ferronor.sic.ventas.modelo.DetalleVenta;
import com.ferronor.sic.ventas.modelo.Venta;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Usuario
 */
public class FrmVentas extends FrmBase {

    // Services y Proceso obtenidos vía ServiceFactory — nunca instanciados a mano
    // ni accedidos a través de DAO directo, según ARQUITECTURA.md 3.1.
    private final ClienteService clienteService = ServiceFactory.clienteService();
    private final ProductoService productoService = ServiceFactory.productoService();
    private final FormaPagoService formaPagoService = ServiceFactory.formaPagoService();
    private final TipoComprobanteService tipoComprobanteService = ServiceFactory.tipoComprobanteService();
    private final TesoreriaService tesoreriaService = ServiceFactory.tesoreriaService();
    private final ProcesoVenta procesoVenta = ServiceFactory.procesoVenta();

    // Colección de DetalleVenta en memoria: se va llenando con "Agregar producto"
    // y recién se envía completa al confirmar la venta.
    private final List<DetalleVenta> detalles = new ArrayList<>();
    private final DefaultTableModel modeloDetalle = new DefaultTableModel(
            new Object[]{"PRODUCTO", "CANTIDAD", "P. UNIT (CON IGV)", "SUBTOTAL"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private Caja cajaAbierta;
    private CuentaBancaria cuentaBancariaActiva;

    private static final DecimalFormat FORMATO_MONEDA = new DecimalFormat("#,##0.00");

    /**
     * Creates new form FrmVentas
     */
    public FrmVentas() {
        super("VENTAS");
        initComponents();
        setDefaultCloseOperation(
                javax.swing.WindowConstants.DISPOSE_ON_CLOSE
        );

        configurarComponentes();
    }

    // ============================================================
    // Conexión del formulario con Service/ProcesoVenta.
    // Nada de lo siguiente accede a DAO ni a PostgreSQL directamente.
    // ============================================================
    private void configurarComponentes() {
        lblNombreVend.setText(SesionUsuario.actual().getNombreCompleto());
        lblFechaHora.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy - hh:mm a")));

        configurarCaja();
        configurarTabla();
        configurarSpinnerCantidad();
        configurarComboClientes();
        configurarComboProductos();
        configurarComboFormasPago();
        configurarComboTipoComprobante();
        limpiarValidaciones();
        recalcularTotales();

        txtValEj1.setEditable(false);
        txtValEj2.setEditable(false);
        txtValEj3.setEditable(false);

        txtPrecioUnitario.setEditable(false);
        lblNroComprobante.setText("Se genera automáticamente al confirmar");

    }

    // Caja abierta y cuenta bancaria activa se obtienen una sola vez al abrir el
    // formulario, exclusivamente vía TesoreriaService (nunca CajaDAO/BancoDAO).
    private void configurarCaja() {
        Optional<Caja> caja = tesoreriaService.obtenerCajaAbierta();
        if (caja.isPresent()) {
            cajaAbierta = caja.get();
            lblCaja.setText(cajaAbierta.getNombre() + " - Abierta");
            rbtnCaja.setText("Caja - " + cajaAbierta.getNombre());
            rbtnCaja.setEnabled(true);
        } else {
            cajaAbierta = null;
            lblCaja.setText("Sin caja abierta");
            rbtnCaja.setText("Caja (no hay ninguna abierta)");
            rbtnCaja.setEnabled(false);
        }

        List<CuentaBancaria> cuentas = tesoreriaService.listarCuentasBancariasActivas();
        if (!cuentas.isEmpty()) {
            // El formulario solo tiene un radio para "Cuenta Bancaria": se usa la
            // primera cuenta activa. Si existiera más de una, este mockup no
            // permite elegir entre varias — se deja constancia en el resumen.
            cuentaBancariaActiva = cuentas.get(0);
            rbtnCtaBancaria.setText("Cuenta Bancaria - " + cuentaBancariaActiva.getBanco()
                    + " " + cuentaBancariaActiva.getAlias());
            rbtnCtaBancaria.setEnabled(true);
        } else {
            cuentaBancariaActiva = null;
            rbtnCtaBancaria.setText("Cuenta Bancaria (no hay ninguna activa)");
            rbtnCtaBancaria.setEnabled(false);
        }

        if (cajaAbierta != null) {
            rbtnCaja.setSelected(true);
        } else if (cuentaBancariaActiva != null) {
            rbtnCtaBancaria.setSelected(true);
        }
    }

    private void configurarTabla() {
        tblDetProducto.setModel(modeloDetalle);
    }

    private void configurarSpinnerCantidad() {
        spnCantidad.setModel(new SpinnerNumberModel(1, 1, 99999, 1));
    }

    // cmbClientes: editable + ComboAutoFiltro sobre
    // ClienteService.buscarActivosPorNombreODocumentoParcial(texto).
    private void configurarComboClientes() {
        cmbClientes.setModel(new DefaultComboBoxModel<>());
        ComboAutoFiltro.mejorarCombo(cmbClientes, clienteService::buscarActivosPorNombreODocumentoParcial);
        cmbClientes.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Cliente cliente) {
                mostrarClienteSeleccionado(cliente);
            }
        });
        limpiarClienteMostrado();
    }

    private void mostrarClienteSeleccionado(Cliente cliente) {
        lblDocumento.setText(cliente.getTipoDocumento() + " " + cliente.getNumeroDocumento());
        lblNombreCliente.setText(cliente.getNombreRazonSocial());
        tblTelefono.setText("Tel. " + (cliente.getTelefono() == null ? "-" : cliente.getTelefono()));
    }

    private void limpiarClienteMostrado() {
        lblDocumento.setText("RUC/DNI —");
        lblNombreCliente.setText("Selecciona un cliente");
        tblTelefono.setText("Tel. —");
    }

    // cmbProductos: editable + ComboAutoFiltro sobre
    // ProductoService.buscarActivosPorNombreOCodigoParcial(texto).
    // txtPrecioUnitario se autocompleta desde Producto.precioVenta y es de solo lectura.
    private void configurarComboProductos() {
        cmbProductos.setModel(new DefaultComboBoxModel<>());
        ComboAutoFiltro.mejorarCombo(cmbProductos, productoService::buscarActivosPorNombreOCodigoParcial);
        cmbProductos.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Producto producto) {
                txtPrecioUnitario.setText(FORMATO_MONEDA.format(producto.getPrecioVenta()));
            }
        });
    }

    // cmbFormasPago: lista fija, sin ComboAutoFiltro — se carga completa con
    // formaPagoService.listar(). Nunca se compara el nombre: crédito/contado se
    // decide siempre con FormaPago.isEsCredito().
    private void configurarComboFormasPago() {
        cmbFormasPago.setModel(new DefaultComboBoxModel<>(formaPagoService.listar().toArray(new FormaPago[0])));
        cmbFormasPago.setSelectedItem(null);
        cmbFormasPago.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                actualizarVisibilidadPago();
            }
        });
        actualizarVisibilidadPago();
    }

    private void configurarComboTipoComprobante() {
        cmbTipoComprobante.setModel(
                new DefaultComboBoxModel<>(tipoComprobanteService.listar().toArray(new TipoComprobante[0])));
        cmbTipoComprobante.setSelectedItem(null);
    }

    // Si la forma de pago es crédito: se oculta el destino de pago (caja/banco)
    // y se muestra el aviso de cuenta por cobrar. Si es contado: al revés.
    private void actualizarVisibilidadPago() {
        Object seleccion = cmbFormasPago.getSelectedItem();
        boolean esCredito = seleccion instanceof FormaPago fp && fp.isEsCredito();

        pnlDestinoPago.setVisible(!esCredito);
        pnlAvisoCtaCobrar.setVisible(esCredito);

        if (esCredito && pnlAvisoCtaCobrar.getComponentCount() == 0) {
            pnlAvisoCtaCobrar.setLayout(new BorderLayout());
            javax.swing.JLabel lblAviso = new javax.swing.JLabel(
                    "Se generará una cuenta por cobrar. Sin movimiento de caja/banco.");
            lblAviso.setFont(lblAviso.getFont().deriveFont(java.awt.Font.ITALIC, 11f));
            pnlAvisoCtaCobrar.add(lblAviso, BorderLayout.CENTER);
        }
        pnlDestinoPago.getParent().revalidate();
        pnlDestinoPago.getParent().repaint();
    }

    // ------------------------------------------------------------------
    // Detalle de productos en memoria
    // ------------------------------------------------------------------
    private void agregarProducto() {
        limpiarValidaciones();

        Object seleccion = cmbProductos.getSelectedItem();
        if (!(seleccion instanceof Producto producto)) {
            mostrarValidacion("Selecciona un producto del listado.");
            return;
        }

        Object valorSpinner = spnCantidad.getValue();
        BigDecimal cantidad = new BigDecimal(valorSpinner.toString());
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            mostrarValidacion("La cantidad debe ser mayor a cero.");
            return;
        }

        DetalleVenta detalle = new DetalleVenta(producto.getIdProducto(), cantidad, producto.getPrecioVenta());
        detalles.add(detalle);
        modeloDetalle.addRow(new Object[]{
            producto,
            cantidad.toPlainString(),
            "S/ " + FORMATO_MONEDA.format(producto.getPrecioVenta()),
            "S/ " + FORMATO_MONEDA.format(detalle.getSubtotal())
        });

        recalcularTotales();

        cmbProductos.setSelectedItem(null);
        txtPrecioUnitario.setText("");
        spnCantidad.setValue(1);
    }

    // El total acumulado ya incluye IGV (precio_unitario lo incluye); subtotal e
    // IGV se derivan con CalculadoraImpuestos — misma fórmula centralizada que
    // usa VentaServiceImpl, no se repite el cálculo aquí.
    private void recalcularTotales() {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleVenta d : detalles) {
            total = total.add(d.getSubtotal());
        }
        total = total.setScale(2, RoundingMode.HALF_UP);

        BigDecimal subtotal = total.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : CalculadoraImpuestos.calcularValorVenta(total);
        BigDecimal igv = total.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : CalculadoraImpuestos.calcularIGV(total);

        lblCantSubTotal.setText("S/ " + FORMATO_MONEDA.format(subtotal));
        lblCantIGV.setText("S/ " + FORMATO_MONEDA.format(igv));
        lblCantTotal.setText("S/ " + FORMATO_MONEDA.format(total));
    }

    // ------------------------------------------------------------------
    // Registrar venta — decide la variante de ProcesoVenta según forma de pago
    // ------------------------------------------------------------------
    private void registrarVenta() {
        limpiarValidaciones();

        Object clienteSel = cmbClientes.getSelectedItem();
        if (!(clienteSel instanceof Cliente cliente)) {
            mostrarValidacion("Selecciona un cliente.");
            return;
        }

        if (detalles.isEmpty()) {
            mostrarValidacion("La venta debe tener al menos un producto.");
            return;
        }

        Object formaPagoSel = cmbFormasPago.getSelectedItem();
        if (!(formaPagoSel instanceof FormaPago formaPago)) {
            mostrarValidacion("Selecciona una forma de pago.");
            return;
        }

        Object tipoComprobanteSel = cmbTipoComprobante.getSelectedItem();
        if (!(tipoComprobanteSel instanceof TipoComprobante tipoComprobante)) {
            mostrarValidacion("Selecciona el tipo de comprobante.");
            return;
        }

        Venta venta = new Venta(cliente.getIdCliente(), formaPago.getIdFormaPago(),
                SesionUsuario.actual().getIdUsuario());
        for (DetalleVenta d : detalles) {
            venta.agregarDetalle(d);
        }

        RespuestaOperacion<Integer> resultado;
        if (formaPago.isEsCredito()) {
            resultado = procesoVenta.registrarVentaCredito(venta, tipoComprobante.getIdTipoComprobante());
        } else if (rbtnCaja.isSelected()) {
            if (cajaAbierta == null) {
                mostrarValidacion("No hay una caja abierta para cobrar al contado.");
                return;
            }
            resultado = procesoVenta.registrarVentaContadoCaja(venta, tipoComprobante.getIdTipoComprobante(),
                    cajaAbierta.getIdCaja());
        } else if (rbtnCtaBancaria.isSelected()) {
            if (cuentaBancariaActiva == null) {
                mostrarValidacion("No hay una cuenta bancaria activa para cobrar.");
                return;
            }
            resultado = procesoVenta.registrarVentaContadoBanco(venta, tipoComprobante.getIdTipoComprobante(),
                    cuentaBancariaActiva.getIdCuentaBancaria());
        } else {
            mostrarValidacion("Selecciona caja o cuenta bancaria para el cobro al contado.");
            return;
        }

        if (!resultado.isExito()) {
            mostrarValidacion(resultado.getMensaje());
            return;
        }

        JOptionPane.showMessageDialog(this, "Venta N° " + resultado.getResultado() + " registrada correctamente.");
        limpiarFormularioTrasRegistro();
    }

    private void limpiarFormularioTrasRegistro() {
        detalles.clear();
        modeloDetalle.setRowCount(0);
        recalcularTotales();
        cmbClientes.setSelectedItem(null);
        limpiarClienteMostrado();
        cmbFormasPago.setSelectedItem(null);
        cmbTipoComprobante.setSelectedItem(null);
        actualizarVisibilidadPago();
    }

    // ------------------------------------------------------------------
    // pnlValidaciones: los errores de RespuestaOperacion (y las validaciones
    // propias del formulario) se muestran acá, nunca con JOptionPane.
    // ------------------------------------------------------------------
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpBtnFormaPago = new javax.swing.ButtonGroup();
        pnlSuperior = new javax.swing.JPanel();
        lblVendedor = new javax.swing.JLabel();
        lblNombreVend = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        lblFechaHora = new javax.swing.JLabel();
        lblCaja = new javax.swing.JLabel();
        pnlCliente = new javax.swing.JPanel();
        pnlClienteBuscado = new javax.swing.JPanel();
        lblDocumento = new javax.swing.JLabel();
        lblNombreCliente = new javax.swing.JLabel();
        tblTelefono = new javax.swing.JLabel();
        cmbClientes = new javax.swing.JComboBox<>();
        pnlDetalleProducto = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDetProducto = new javax.swing.JTable();
        pnlAgregarProducto = new javax.swing.JPanel();
        txtPrecioUnitario = new javax.swing.JTextField();
        btnAgregarProducto = new javax.swing.JButton();
        cmbProductos = new javax.swing.JComboBox<>();
        spnCantidad = new javax.swing.JSpinner();
        pnlValidaciones = new javax.swing.JPanel();
        txtValEj1 = new javax.swing.JTextField();
        txtValEj2 = new javax.swing.JTextField();
        txtValEj3 = new javax.swing.JTextField();
        pnlComprobante = new javax.swing.JPanel();
        cmbTipoComprobante = new javax.swing.JComboBox<>();
        lblTipo = new javax.swing.JLabel();
        lblSerie_Nro = new javax.swing.JLabel();
        lblNroComprobante = new javax.swing.JLabel();
        pnlFormaPago = new javax.swing.JPanel();
        cmbFormasPago = new javax.swing.JComboBox<>();
        pnlDestinoPago = new javax.swing.JPanel();
        rbtnCaja = new javax.swing.JRadioButton();
        rbtnCtaBancaria = new javax.swing.JRadioButton();
        pnlTotales = new javax.swing.JPanel();
        lblSubtotal = new javax.swing.JLabel();
        lblCantSubTotal = new javax.swing.JLabel();
        lblIGV = new javax.swing.JLabel();
        lblCantIGV = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        lblTotal = new javax.swing.JLabel();
        lblCantTotal = new javax.swing.JLabel();
        btnRegistrarVenta = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        pnlAvisoCtaCobrar = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblVendedor.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblVendedor.setText("VENDEDOR:");

        lblNombreVend.setText("Nombre A. (sesión activa)");

        lblFecha.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFecha.setText("FECHA:");

        lblFechaHora.setText("dd/mm/yy - 19:18 p.m.");

        lblCaja.setText("Caja Principal - Abierta");

        javax.swing.GroupLayout pnlSuperiorLayout = new javax.swing.GroupLayout(pnlSuperior);
        pnlSuperior.setLayout(pnlSuperiorLayout);
        pnlSuperiorLayout.setHorizontalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSuperiorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblVendedor)
                    .addComponent(lblNombreVend))
                .addGap(23, 23, 23)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFecha)
                    .addComponent(lblFechaHora))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblCaja)
                .addGap(15, 15, 15))
        );
        pnlSuperiorLayout.setVerticalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSuperiorLayout.createSequentialGroup()
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblVendedor)
                            .addComponent(lblFecha))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNombreVend)
                            .addComponent(lblFechaHora)))
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(lblCaja)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlCliente.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. CLIENTE", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        pnlClienteBuscado.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblDocumento.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblDocumento.setText("RUC/DNI 0123456789");

        lblNombreCliente.setText("Nombre del Cliente (Persona o Empresa)");

        tblTelefono.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        tblTelefono.setText("Tel. 985 466 883");

        javax.swing.GroupLayout pnlClienteBuscadoLayout = new javax.swing.GroupLayout(pnlClienteBuscado);
        pnlClienteBuscado.setLayout(pnlClienteBuscadoLayout);
        pnlClienteBuscadoLayout.setHorizontalGroup(
            pnlClienteBuscadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlClienteBuscadoLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlClienteBuscadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblNombreCliente)
                    .addGroup(pnlClienteBuscadoLayout.createSequentialGroup()
                        .addComponent(lblDocumento)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tblTelefono)))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        pnlClienteBuscadoLayout.setVerticalGroup(
            pnlClienteBuscadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlClienteBuscadoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlClienteBuscadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDocumento)
                    .addComponent(tblTelefono))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblNombreCliente)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlClienteLayout = new javax.swing.GroupLayout(pnlCliente);
        pnlCliente.setLayout(pnlClienteLayout);
        pnlClienteLayout.setHorizontalGroup(
            pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmbClientes, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlClienteBuscado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlClienteLayout.setVerticalGroup(
            pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmbClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnlClienteLayout.createSequentialGroup()
                .addComponent(pnlClienteBuscado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pnlDetalleProducto.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. DETALLE DEL PRODUCTO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        tblDetProducto.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        tblDetProducto.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "PRODUCTO", "CANTIDAD", "P. UNIT (CON IGV)", "SUBTOTAL"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblDetProducto);

        javax.swing.GroupLayout pnlDetalleProductoLayout = new javax.swing.GroupLayout(pnlDetalleProducto);
        pnlDetalleProducto.setLayout(pnlDetalleProductoLayout);
        pnlDetalleProductoLayout.setHorizontalGroup(
            pnlDetalleProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetalleProductoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlDetalleProductoLayout.setVerticalGroup(
            pnlDetalleProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetalleProductoLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 6, Short.MAX_VALUE))
        );

        pnlAgregarProducto.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        btnAgregarProducto.setBackground(new java.awt.Color(51, 102, 0));
        btnAgregarProducto.setText("Agregar Producto");
        btnAgregarProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarProductoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlAgregarProductoLayout = new javax.swing.GroupLayout(pnlAgregarProducto);
        pnlAgregarProducto.setLayout(pnlAgregarProductoLayout);
        pnlAgregarProductoLayout.setHorizontalGroup(
            pnlAgregarProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAgregarProductoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmbProductos, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(txtPrecioUnitario, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAgregarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlAgregarProductoLayout.setVerticalGroup(
            pnlAgregarProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAgregarProductoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAgregarProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPrecioUnitario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregarProducto)
                    .addComponent(cmbProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlValidaciones.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "03. VALIDACIONES", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

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

        pnlComprobante.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "COMPROBANTE", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblTipo.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblTipo.setText("TIPO:");

        lblSerie_Nro.setText("Serie-Número:");

        lblNroComprobante.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblNroComprobante.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNroComprobante.setText("B00-00124");

        pnlFormaPago.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "FORMA DE PAGO", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        javax.swing.GroupLayout pnlFormaPagoLayout = new javax.swing.GroupLayout(pnlFormaPago);
        pnlFormaPago.setLayout(pnlFormaPagoLayout);
        pnlFormaPagoLayout.setHorizontalGroup(
            pnlFormaPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormaPagoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmbFormasPago, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlFormaPagoLayout.setVerticalGroup(
            pnlFormaPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormaPagoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmbFormasPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlDestinoPago.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        grpBtnFormaPago.add(rbtnCaja);
        rbtnCaja.setText("Caja - Caja Principal");

        grpBtnFormaPago.add(rbtnCtaBancaria);
        rbtnCtaBancaria.setText("Cuenta Bancaria - BCP Cta. Corriente");

        javax.swing.GroupLayout pnlDestinoPagoLayout = new javax.swing.GroupLayout(pnlDestinoPago);
        pnlDestinoPago.setLayout(pnlDestinoPagoLayout);
        pnlDestinoPagoLayout.setHorizontalGroup(
            pnlDestinoPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDestinoPagoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDestinoPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbtnCtaBancaria, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlDestinoPagoLayout.createSequentialGroup()
                        .addComponent(rbtnCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlDestinoPagoLayout.setVerticalGroup(
            pnlDestinoPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDestinoPagoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rbtnCaja)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbtnCtaBancaria)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlTotales.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "TOTALES", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblSubtotal.setText("Subtotal:");

        lblCantSubTotal.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblCantSubTotal.setText("S/ 913.98");

        lblIGV.setText("IGV;");

        lblCantIGV.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblCantIGV.setText("S/ 164.52");

        lblTotal.setFont(new java.awt.Font("Consolas", 0, 16)); // NOI18N
        lblTotal.setText("TOTAL");

        lblCantTotal.setFont(new java.awt.Font("Consolas", 0, 16)); // NOI18N
        lblCantTotal.setText("S/ 1,078.50 ");

        javax.swing.GroupLayout pnlTotalesLayout = new javax.swing.GroupLayout(pnlTotales);
        pnlTotales.setLayout(pnlTotalesLayout);
        pnlTotalesLayout.setHorizontalGroup(
            pnlTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(pnlTotalesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlTotalesLayout.createSequentialGroup()
                        .addComponent(lblTotal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblCantTotal))
                    .addGroup(pnlTotalesLayout.createSequentialGroup()
                        .addGroup(pnlTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblIGV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblSubtotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(pnlTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCantSubTotal, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblCantIGV, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        pnlTotalesLayout.setVerticalGroup(
            pnlTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTotalesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSubtotal)
                    .addComponent(lblCantSubTotal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblIGV)
                    .addComponent(lblCantIGV))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotal)
                    .addComponent(lblCantTotal))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnRegistrarVenta.setBackground(new java.awt.Color(204, 51, 0));
        btnRegistrarVenta.setText("REGISTRAR VENTA");
        btnRegistrarVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarVentaActionPerformed(evt);
            }
        });

        btnCancelar.setBackground(new java.awt.Color(102, 51, 0));
        btnCancelar.setText("CANCELAR");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlAvisoCtaCobrarLayout = new javax.swing.GroupLayout(pnlAvisoCtaCobrar);
        pnlAvisoCtaCobrar.setLayout(pnlAvisoCtaCobrarLayout);
        pnlAvisoCtaCobrarLayout.setHorizontalGroup(
            pnlAvisoCtaCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlAvisoCtaCobrarLayout.setVerticalGroup(
            pnlAvisoCtaCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pnlComprobanteLayout = new javax.swing.GroupLayout(pnlComprobante);
        pnlComprobante.setLayout(pnlComprobanteLayout);
        pnlComprobanteLayout.setHorizontalGroup(
            pnlComprobanteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlComprobanteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlComprobanteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFormaPago, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbTipoComprobante, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDestinoPago, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlTotales, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRegistrarVenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlComprobanteLayout.createSequentialGroup()
                        .addComponent(lblSerie_Nro)
                        .addGap(45, 45, 45)
                        .addComponent(lblNroComprobante, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlComprobanteLayout.createSequentialGroup()
                        .addComponent(lblTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(pnlAvisoCtaCobrar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlComprobanteLayout.setVerticalGroup(
            pnlComprobanteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlComprobanteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTipo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbTipoComprobante, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlComprobanteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSerie_Nro)
                    .addComponent(lblNroComprobante))
                .addGap(14, 14, 14)
                .addComponent(pnlFormaPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlDestinoPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAvisoCtaCobrar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlTotales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnRegistrarVenta)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCancelar)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlSuperior, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlDetalleProducto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlAgregarProducto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlValidaciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlComprobante, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlDetalleProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlAgregarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlValidaciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlComprobante, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRegistrarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarVentaActionPerformed
        // TODO add your handling code here:
        registrarVenta();
    }//GEN-LAST:event_btnRegistrarVentaActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnAgregarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarProductoActionPerformed
        // TODO add your handling code here:
        agregarProducto();
    }//GEN-LAST:event_btnAgregarProductoActionPerformed

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
            java.util.logging.Logger.getLogger(FrmVentas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmVentas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmVentas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmVentas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrmVentas().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarProducto;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnRegistrarVenta;
    private javax.swing.JComboBox<Cliente> cmbClientes;
    private javax.swing.JComboBox<FormaPago> cmbFormasPago;
    private javax.swing.JComboBox<Producto> cmbProductos;
    private javax.swing.JComboBox<TipoComprobante> cmbTipoComprobante;
    private javax.swing.ButtonGroup grpBtnFormaPago;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblCaja;
    private javax.swing.JLabel lblCantIGV;
    private javax.swing.JLabel lblCantSubTotal;
    private javax.swing.JLabel lblCantTotal;
    private javax.swing.JLabel lblDocumento;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblFechaHora;
    private javax.swing.JLabel lblIGV;
    private javax.swing.JLabel lblNombreCliente;
    private javax.swing.JLabel lblNombreVend;
    private javax.swing.JLabel lblNroComprobante;
    private javax.swing.JLabel lblSerie_Nro;
    private javax.swing.JLabel lblSubtotal;
    private javax.swing.JLabel lblTipo;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblVendedor;
    private javax.swing.JPanel pnlAgregarProducto;
    private javax.swing.JPanel pnlAvisoCtaCobrar;
    private javax.swing.JPanel pnlCliente;
    private javax.swing.JPanel pnlClienteBuscado;
    private javax.swing.JPanel pnlComprobante;
    private javax.swing.JPanel pnlDestinoPago;
    private javax.swing.JPanel pnlDetalleProducto;
    private javax.swing.JPanel pnlFormaPago;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JPanel pnlTotales;
    private javax.swing.JPanel pnlValidaciones;
    private javax.swing.JRadioButton rbtnCaja;
    private javax.swing.JRadioButton rbtnCtaBancaria;
    private javax.swing.JSpinner spnCantidad;
    private javax.swing.JTable tblDetProducto;
    private javax.swing.JLabel tblTelefono;
    private javax.swing.JTextField txtPrecioUnitario;
    private javax.swing.JTextField txtValEj1;
    private javax.swing.JTextField txtValEj2;
    private javax.swing.JTextField txtValEj3;
    // End of variables declaration//GEN-END:variables
}
