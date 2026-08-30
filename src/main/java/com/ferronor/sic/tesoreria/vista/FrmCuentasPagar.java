package com.ferronor.sic.tesoreria.vista;

import com.ferronor.sic.compras.vista.*;
import com.ferronor.sic.compras.logica.CompraService;
import com.ferronor.sic.compras.modelo.Compra;
import com.ferronor.sic.compras.modelo.CuentaPagar;
import com.ferronor.sic.compras.modelo.EstadoCuenta;
import com.ferronor.sic.compras.modelo.dto.CuentaPagarConsulta;
import com.ferronor.sic.maestros.logica.ProveedorService;
import com.ferronor.sic.maestros.modelo.Proveedor;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;
import com.ferronor.sic.shared.ui.ComboAutoFiltro;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Usuario
 */
public class FrmCuentasPagar extends javax.swing.JDialog {

    private final CompraService compraService = ServiceFactory.compraService();
    private final ProveedorService proveedorService = ServiceFactory.proveedorService();

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yy - hh:mm a");

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DecimalFormat FORMATO_MONEDA
            = new DecimalFormat("#,##0.00");

    private final DefaultTableModel modeloCuentasPorPagar
            = new DefaultTableModel(
                    new Object[]{
                        "ID",
                        "PROVEEDOR",
                        "RUC",
                        "COMPRA",
                        "FECHA COMPRA",
                        "FECHA VENC.",
                        "MONTO TOTAL",
                        "MONTO PAGADO",
                        "SALDO PENDIENTE",
                        "ESTADO"
                    },
                    0
            ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public FrmCuentasPagar(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        configurarComponentes();
    }

    private void configurarComponentes() {
        configurarInformacionSuperior();
        configurarComboEstados();
        configurarComboProveedor();
        configurarTabla();
        configurarListeners();

        consultarCuentasPorPagar();
        setLocationRelativeTo(getParent());
    }

    private void configurarInformacionSuperior() {
        SesionUsuario sesion = SesionUsuario.actual();

        lblNombreVend.setText(sesion.getNombreCompleto());
        lblFechaHora.setText(
                LocalDateTime.now().format(FORMATO_FECHA_HORA)
        );
    }

    private void configurarComboEstados() {
        cmbEstados.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "Todos",
                            "Pendiente",
                            "Pagada",
                            "Vencida"
                        }
                )
        );

        cmbEstados.setSelectedIndex(0);
    }

    private EstadoCuenta obtenerEstadoSeleccionado() {
        String seleccionado = (String) cmbEstados.getSelectedItem();

        if (seleccionado == null || "Todos".equals(seleccionado)) {
            return null;
        }

        return switch (seleccionado) {
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void configurarComboProveedor() {

        JComboBox combo = (JComboBox) cmbProveedor;

        combo.setModel(
                new DefaultComboBoxModel<>(
                        new Object[]{"Todos"}
                )
        );

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                Object texto = value;

                if (value instanceof Proveedor proveedor) {
                    texto = proveedor.getRazonSocial()
                            + " - RUC " + proveedor.getRuc();
                }

                return super.getListCellRendererComponent(
                        list,
                        texto,
                        index,
                        isSelected,
                        cellHasFocus
                );
            }
        });

        Function<String, List<Proveedor>> buscador
                = proveedorService::buscarActivosPorRazonSocialORucParcial;

        ComboAutoFiltro.mejorarCombo(
                combo,
                buscador
        );

        combo.setSelectedItem("Todos");
    }

    private Integer obtenerIdProveedorSeleccionado() {
        Object seleccionado = cmbProveedor.getSelectedItem();

        if (seleccionado instanceof Proveedor proveedor) {
            return proveedor.getIdProveedor();
        }

        return null;
    }

    private void configurarTabla() {
        tblCuentasPorPagar.setModel(modeloCuentasPorPagar);

        tblCuentasPorPagar.setAutoCreateRowSorter(true);

        tblCuentasPorPagar.setSelectionMode(
                javax.swing.ListSelectionModel.SINGLE_SELECTION
        );
    }

    private void configurarListeners() {
        btnConsultar.addActionListener(
                e -> consultarCuentasPorPagar()
        );

        btnLimpiar.addActionListener(
                e -> limpiarFiltros()
        );

        btnVerDetalle.addActionListener(
                e -> verDetalle()
        );
    }

    private LocalDate convertirFecha(
            com.toedter.calendar.JDateChooser selector) {

        Date fecha = selector.getDate();

        if (fecha == null) {
            return null;
        }

        return fecha.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private void consultarCuentasPorPagar() {

        EstadoCuenta estado = obtenerEstadoSeleccionado();
        Integer idProveedor = obtenerIdProveedorSeleccionado();

        LocalDate fechaDesde = convertirFecha(jdcFechaDesde);
        LocalDate fechaHasta = convertirFecha(jdcFechaHasta);

        if (fechaDesde != null
                && fechaHasta != null
                && fechaDesde.isAfter(fechaHasta)) {

            JOptionPane.showMessageDialog(
                    this,
                    "La fecha Desde no puede ser posterior a la fecha Hasta.",
                    "Rango de fechas inválido",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            List<CuentaPagarConsulta> cuentas
                    = compraService.consultarCuentasPorPagar(
                            estado,
                            idProveedor,
                            fechaDesde,
                            fechaHasta
                    );

            cargarTabla(cuentas);
            actualizarResumen(cuentas);

        } catch (Exception ex) {

            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "No fue posible consultar las cuentas por pagar.\n"
                    + "Detalle: " + ex.getMessage(),
                    "Error de consulta",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cargarTabla(List<CuentaPagarConsulta> cuentas) {

        modeloCuentasPorPagar.setRowCount(0);

        if (cuentas == null) {
            return;
        }

        for (CuentaPagarConsulta cuenta : cuentas) {

            String fechaCompra = cuenta.getFechaCompra() != null
                    ? cuenta.getFechaCompra().format(FORMATO_FECHA_HORA)
                    : "";

            String fechaVencimiento = cuenta.getFechaVencimiento() != null
                    ? cuenta.getFechaVencimiento().format(FORMATO_FECHA)
                    : "";

            modeloCuentasPorPagar.addRow(
                    new Object[]{
                        cuenta.getIdCuentaPagar(),
                        cuenta.getRazonSocialProveedor(),
                        cuenta.getRucProveedor(),
                        cuenta.getIdCompra(),
                        fechaCompra,
                        fechaVencimiento,
                        formatearMoneda(cuenta.getMontoTotal()),
                        formatearMoneda(cuenta.getMontoPagado()),
                        formatearMoneda(cuenta.getSaldoPendiente()),
                        cuenta.getEstado()
                    }
            );
        }
    }

    private String formatearMoneda(BigDecimal valor) {

        if (valor == null) {
            return "S/ 0.00";
        }

        return "S/ " + FORMATO_MONEDA.format(valor);
    }

    private void actualizarResumen(List<CuentaPagarConsulta> cuentas) {

        int cantidad = cuentas == null
                ? 0
                : cuentas.size();

        BigDecimal totalPendiente = BigDecimal.ZERO;

        if (cuentas != null) {

            for (CuentaPagarConsulta cuenta : cuentas) {

                if (cuenta.getSaldoPendiente() != null) {

                    totalPendiente = totalPendiente.add(
                            cuenta.getSaldoPendiente()
                    );
                }
            }
        }

        jLabel8.setText(
                String.valueOf(cantidad)
        );

        lblMontoTotalPendiente.setText(
                formatearMoneda(totalPendiente)
        );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void limpiarFiltros() {

        cmbEstados.setSelectedIndex(0);

        JComboBox combo = (JComboBox) cmbProveedor;

        combo.setModel(
                new DefaultComboBoxModel<>(
                        new Object[]{"Todos"}
                )
        );

        combo.setSelectedItem("Todos");

        jdcFechaDesde.setDate(null);
        jdcFechaHasta.setDate(null);

        consultarCuentasPorPagar();
    }

    private void verDetalle() {

        int filaSeleccionada
                = tblCuentasPorPagar.getSelectedRow();

        if (filaSeleccionada < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona una cuenta por pagar para ver su detalle.",
                    "Sin selección",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int filaModelo
                = tblCuentasPorPagar.convertRowIndexToModel(
                        filaSeleccionada
                );

        Object valorIdCompra
                = modeloCuentasPorPagar.getValueAt(
                        filaModelo,
                        3
                );

        if (!(valorIdCompra instanceof Number numero)) {

            JOptionPane.showMessageDialog(
                    this,
                    "No fue posible obtener el ID de la compra seleccionada.",
                    "Datos inválidos",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        int idCompra = numero.intValue();

        try {

            Compra compra
                    = compraService.buscarPorId(idCompra);

            CuentaPagar cuentaPagar
                    = compraService.buscarCuentaPagarPorCompra(idCompra);

            if (compra == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "No se encontró la compra seleccionada.",
                        "Compra no encontrada",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            if (cuentaPagar == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "La compra seleccionada no tiene una cuenta por pagar asociada.",
                        "Cuenta no encontrada",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Datos de detalle recuperados para la compra N.º "
                    + idCompra
                    + ".\nLa vista específica de detalle todavía no existe.",
                    "Detalle",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {

            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "No fue posible obtener el detalle de la compra.\n"
                    + "Detalle: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSuperior = new javax.swing.JPanel();
        lblCuentasPorPagar = new javax.swing.JLabel();
        lblConsultaDeObligacionesConProveedor = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        lblNombreVend = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        lblFechaHora = new javax.swing.JLabel();
        pnlFiltrosDeConsulta = new javax.swing.JPanel();
        lblEstado = new javax.swing.JLabel();
        cmbEstados = new javax.swing.JComboBox<>();
        lblProveedor = new javax.swing.JLabel();
        cmbProveedor = new javax.swing.JComboBox<>();
        lblDesde = new javax.swing.JLabel();
        jdcFechaDesde = new com.toedter.calendar.JDateChooser();
        lblHasta = new javax.swing.JLabel();
        jdcFechaHasta = new com.toedter.calendar.JDateChooser();
        btnLimpiar = new javax.swing.JButton();
        btnConsultar = new javax.swing.JButton();
        pnlTotalObligacionesEncontradas = new javax.swing.JPanel();
        lblTxtTotalObligacionesEncontradas = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        pnlMontoTotalPendiente = new javax.swing.JPanel();
        lblTxtMontoTotalPendiente = new javax.swing.JLabel();
        lblMontoTotalPendiente = new javax.swing.JLabel();
        spnlCuentasPorPagar = new javax.swing.JScrollPane();
        tblCuentasPorPagar = new javax.swing.JTable();
        pnlBtnVerDetalle = new javax.swing.JPanel();
        btnVerDetalle = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblCuentasPorPagar.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblCuentasPorPagar.setText("CUENTAS POR PAGAR");

        lblConsultaDeObligacionesConProveedor.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblConsultaDeObligacionesConProveedor.setText("Consulta de obligaciones con proveedores");

        lblUsuario.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblUsuario.setText("USUARIO:");

        lblNombreVend.setText("Nombre A. (sesión activa)");

        lblFecha.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblFecha.setText("FECHA:");

        lblFechaHora.setText("dd/mm/yy - 19:18 p.m.");

        javax.swing.GroupLayout pnlSuperiorLayout = new javax.swing.GroupLayout(pnlSuperior);
        pnlSuperior.setLayout(pnlSuperiorLayout);
        pnlSuperiorLayout.setHorizontalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSuperiorLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblCuentasPorPagar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblConsultaDeObligacionesConProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblUsuario)
                            .addComponent(lblFecha))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNombreVend)
                            .addComponent(lblFechaHora)))
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblCuentasPorPagar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblConsultaDeObligacionesConProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlFiltrosDeConsulta.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "FILTROS DE CONSULTA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblEstado.setText("Estado");

        lblProveedor.setText("Proveedor");

        lblDesde.setText("Desde");

        lblHasta.setText("Hasta");

        btnLimpiar.setBackground(new java.awt.Color(51, 51, 51));
        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        btnConsultar.setBackground(new java.awt.Color(0, 102, 204));
        btnConsultar.setText("Consultar");
        btnConsultar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConsultarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlFiltrosDeConsultaLayout = new javax.swing.GroupLayout(pnlFiltrosDeConsulta);
        pnlFiltrosDeConsulta.setLayout(pnlFiltrosDeConsultaLayout);
        pnlFiltrosDeConsultaLayout.setHorizontalGroup(
            pnlFiltrosDeConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFiltrosDeConsultaLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlFiltrosDeConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbEstados, 0, 97, Short.MAX_VALUE)
                    .addComponent(lblEstado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(32, 32, 32)
                .addGroup(pnlFiltrosDeConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbProveedor, 0, 123, Short.MAX_VALUE)
                    .addComponent(lblProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(pnlFiltrosDeConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jdcFechaDesde, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                    .addComponent(lblDesde, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(pnlFiltrosDeConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlFiltrosDeConsultaLayout.createSequentialGroup()
                        .addComponent(jdcFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnLimpiar)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnConsultar)
                .addContainerGap())
        );
        pnlFiltrosDeConsultaLayout.setVerticalGroup(
            pnlFiltrosDeConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFiltrosDeConsultaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFiltrosDeConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlFiltrosDeConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnConsultar)
                        .addComponent(btnLimpiar))
                    .addGroup(pnlFiltrosDeConsultaLayout.createSequentialGroup()
                        .addGroup(pnlFiltrosDeConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblEstado)
                            .addComponent(lblProveedor)
                            .addComponent(lblDesde)
                            .addComponent(lblHasta))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFiltrosDeConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlFiltrosDeConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cmbEstados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jdcFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jdcFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlTotalObligacionesEncontradas.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblTxtTotalObligacionesEncontradas.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblTxtTotalObligacionesEncontradas.setText("TOTAL DE OBLIGACIONES ENCONTRADAS");

        jLabel8.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        jLabel8.setText("N");

        javax.swing.GroupLayout pnlTotalObligacionesEncontradasLayout = new javax.swing.GroupLayout(pnlTotalObligacionesEncontradas);
        pnlTotalObligacionesEncontradas.setLayout(pnlTotalObligacionesEncontradasLayout);
        pnlTotalObligacionesEncontradasLayout.setHorizontalGroup(
            pnlTotalObligacionesEncontradasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTotalObligacionesEncontradasLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblTxtTotalObligacionesEncontradas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlTotalObligacionesEncontradasLayout.setVerticalGroup(
            pnlTotalObligacionesEncontradasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTotalObligacionesEncontradasLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlTotalObligacionesEncontradasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(lblTxtTotalObligacionesEncontradas)))
        );

        pnlMontoTotalPendiente.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblTxtMontoTotalPendiente.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblTxtMontoTotalPendiente.setText("MONTO TOTAL PENDIENTE");

        lblMontoTotalPendiente.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblMontoTotalPendiente.setText("S/ 3,300");

        javax.swing.GroupLayout pnlMontoTotalPendienteLayout = new javax.swing.GroupLayout(pnlMontoTotalPendiente);
        pnlMontoTotalPendiente.setLayout(pnlMontoTotalPendienteLayout);
        pnlMontoTotalPendienteLayout.setHorizontalGroup(
            pnlMontoTotalPendienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMontoTotalPendienteLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(lblTxtMontoTotalPendiente)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 97, Short.MAX_VALUE)
                .addComponent(lblMontoTotalPendiente)
                .addContainerGap())
        );
        pnlMontoTotalPendienteLayout.setVerticalGroup(
            pnlMontoTotalPendienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMontoTotalPendienteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMontoTotalPendienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTxtMontoTotalPendiente)
                    .addComponent(lblMontoTotalPendiente))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        tblCuentasPorPagar.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "PROVEEDOR", "RUC", "COMPRA", "FECHA COMPRA", "FECHA VENC.", "MONTO TOTAL", "MONTO PAGADO", "SALDO PENDIENTE", "ESTADO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlCuentasPorPagar.setViewportView(tblCuentasPorPagar);

        pnlBtnVerDetalle.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        btnVerDetalle.setBackground(new java.awt.Color(51, 51, 51));
        btnVerDetalle.setText("👁️ Ver Detalle");
        btnVerDetalle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerDetalleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlBtnVerDetalleLayout = new javax.swing.GroupLayout(pnlBtnVerDetalle);
        pnlBtnVerDetalle.setLayout(pnlBtnVerDetalleLayout);
        pnlBtnVerDetalleLayout.setHorizontalGroup(
            pnlBtnVerDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBtnVerDetalleLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnVerDetalle, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlBtnVerDetalleLayout.setVerticalGroup(
            pnlBtnVerDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnVerDetalle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFiltrosDeConsulta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlSuperior, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlTotalObligacionesEncontradas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pnlMontoTotalPendiente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pnlBtnVerDetalle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(spnlCuentasPorPagar))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlFiltrosDeConsulta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnlCuentasPorPagar, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlTotalObligacionesEncontradas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlMontoTotalPendiente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlBtnVerDetalle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
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

    private void btnVerDetalleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerDetalleActionPerformed
        // TODO add your handling code here:
        verDetalle();
    }//GEN-LAST:event_btnVerDetalleActionPerformed

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
            java.util.logging.Logger.getLogger(FrmCuentasPagar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmCuentasPagar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmCuentasPagar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmCuentasPagar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
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
                FrmCuentasPagar dialog = new FrmCuentasPagar(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnVerDetalle;
    private javax.swing.JComboBox<String> cmbEstados;
    private javax.swing.JComboBox<String> cmbProveedor;
    private javax.swing.JLabel jLabel8;
    private com.toedter.calendar.JDateChooser jdcFechaDesde;
    private com.toedter.calendar.JDateChooser jdcFechaHasta;
    private javax.swing.JLabel lblConsultaDeObligacionesConProveedor;
    private javax.swing.JLabel lblCuentasPorPagar;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblFechaHora;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblMontoTotalPendiente;
    private javax.swing.JLabel lblNombreVend;
    private javax.swing.JLabel lblProveedor;
    private javax.swing.JLabel lblTxtMontoTotalPendiente;
    private javax.swing.JLabel lblTxtTotalObligacionesEncontradas;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPanel pnlBtnVerDetalle;
    private javax.swing.JPanel pnlFiltrosDeConsulta;
    private javax.swing.JPanel pnlMontoTotalPendiente;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JPanel pnlTotalObligacionesEncontradas;
    private javax.swing.JScrollPane spnlCuentasPorPagar;
    private javax.swing.JTable tblCuentasPorPagar;
    // End of variables declaration//GEN-END:variables
}
