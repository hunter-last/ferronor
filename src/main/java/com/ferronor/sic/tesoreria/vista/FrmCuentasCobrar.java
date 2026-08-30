package com.ferronor.sic.tesoreria.vista;

import com.ferronor.sic.maestros.logica.ClienteService;
import com.ferronor.sic.maestros.modelo.Cliente;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;
import com.ferronor.sic.shared.ui.ComboAutoFiltro;
import com.ferronor.sic.ventas.logica.VentaService;
import com.ferronor.sic.ventas.modelo.CuentaCobrar;
import com.ferronor.sic.ventas.modelo.EstadoCuenta;
import com.ferronor.sic.ventas.modelo.Venta;
import com.ferronor.sic.ventas.modelo.dto.CuentaCobrarConsulta;

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
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Usuario
 */
public class FrmCuentasCobrar extends javax.swing.JDialog {

    private final VentaService ventaService
            = ServiceFactory.ventaService();

    private final ClienteService clienteService
            = ServiceFactory.clienteService();

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yy - hh:mm a");

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DecimalFormat FORMATO_MONEDA
            = new DecimalFormat("#,##0.00");

    private final DefaultTableModel modeloCuentasPorCobrar
            = new DefaultTableModel(
                    new Object[]{
                        "ID",
                        "CLIENTE",
                        "DOCUMENTO",
                        "VENTA",
                        "FECHA VENTA",
                        "FECHA VENC.",
                        "MONTO TOTAL",
                        "MONTO COBRADO",
                        "SALDO PEND.",
                        "ESTADO"
                    },
                    0
            ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public FrmCuentasCobrar(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        configurarComponentes();
    }

    private void configurarComponentes() {
        configurarInformacionSuperior();
        configurarComboEstados();
        configurarComboClientes();
        configurarTabla();
        configurarListeners();

        consultarCuentasPorCobrar();
    }

    private void configurarInformacionSuperior() {

        SesionUsuario sesion = SesionUsuario.actual();
        lblNombreUsuario.setText(
                sesion.getNombreCompleto()
        );
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

        if (seleccionado == null
                || "Todos".equals(seleccionado)) {
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
    private void configurarComboClientes() {

        JComboBox combo
                = (JComboBox) cmbClientes;

        combo.setModel(
                new DefaultComboBoxModel<>(
                        new Object[]{"Todos"}
                )
        );

        combo.setRenderer(
                new DefaultListCellRenderer() {

            @Override
            public java.awt.Component
                    getListCellRendererComponent(
                            javax.swing.JList<?> list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus) {

                Object texto = value;

                if (value instanceof Cliente cliente) {

                    texto
                            = cliente.getNombreRazonSocial()
                            + " - "
                            + cliente.getTipoDocumento()
                            + " "
                            + cliente.getNumeroDocumento();
                }

                return super.getListCellRendererComponent(
                        list,
                        texto,
                        index,
                        isSelected,
                        cellHasFocus
                );
            }
        }
        );

        Function<String, List<Cliente>> buscador
                = clienteService::buscarActivosPorNombreODocumentoParcial;

        ComboAutoFiltro.mejorarCombo(
                combo,
                buscador
        );

        combo.setSelectedItem("Todos");
    }

    private Integer obtenerIdClienteSeleccionado() {

        Object seleccionado = cmbClientes.getSelectedItem();

        if (seleccionado instanceof Cliente cliente) {
            return cliente.getIdCliente();
        }

        return null;
    }

    private void configurarTabla() {

        tblCuentasPorCobrar.setModel(modeloCuentasPorCobrar);

        tblCuentasPorCobrar.setAutoCreateRowSorter(true);
        tblCuentasPorCobrar.setSelectionMode(
                javax.swing.ListSelectionModel.SINGLE_SELECTION
        );
    }

    private void configurarListeners() {
   
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

    private void consultarCuentasPorCobrar() {

        EstadoCuenta estado
                = obtenerEstadoSeleccionado();

        Integer idCliente
                = obtenerIdClienteSeleccionado();

        LocalDate fechaDesde
                = convertirFecha(jdcFechaDesde);

        LocalDate fechaHasta
                = convertirFecha(jdcFechaHasta);

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

            List<CuentaCobrarConsulta> cuentas
                    = ventaService.consultarCuentasPorCobrar(
                            estado,
                            idCliente,
                            fechaDesde,
                            fechaHasta
                    );

            cargarTabla(cuentas);

            actualizarResumen(cuentas);

            actualizarProximosVencimientos(cuentas);

        } catch (Exception ex) {

            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "No fue posible consultar las cuentas por cobrar.\n"
                    + "Detalle: " + ex.getMessage(),
                    "Error de consulta",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cargarTabla(
            List<CuentaCobrarConsulta> cuentas) {

        modeloCuentasPorCobrar.setRowCount(0);

        if (cuentas == null) {
            return;
        }

        for (CuentaCobrarConsulta cuenta : cuentas) {

            String documento
                    = cuenta.getTipoDocumentoCliente()
                    + " "
                    + cuenta.getNumeroDocumentoCliente();

            String fechaVenta
                    = cuenta.getFechaVenta() != null
                    ? cuenta.getFechaVenta()
                            .format(FORMATO_FECHA_HORA)
                    : "—";

            String fechaVencimiento
                    = cuenta.getFechaVencimiento() != null
                    ? cuenta.getFechaVencimiento()
                            .format(FORMATO_FECHA)
                    : "—";

            modeloCuentasPorCobrar.addRow(
                    new Object[]{
                        cuenta.getIdCuentaCobrar(),
                        cuenta.getNombreRazonSocialCliente(),
                        documento,
                        cuenta.getIdVenta(),
                        fechaVenta,
                        fechaVencimiento,
                        formatearMoneda(
                                cuenta.getMontoTotal()
                        ),
                        formatearMoneda(
                                cuenta.getMontoCobrado()
                        ),
                        formatearMoneda(
                                cuenta.getSaldoPendiente()
                        ),
                        cuenta.getEstado()
                    }
            );
        }
    }

    private String formatearMoneda(
            BigDecimal valor) {

        if (valor == null) {
            return "S/ 0.00";
        }

        return "S/ "
                + FORMATO_MONEDA.format(valor);
    }

    private void actualizarResumen(
            List<CuentaCobrarConsulta> cuentas) {

        int cantidad
                = cuentas == null
                        ? 0
                        : cuentas.size();

        BigDecimal saldoTotal
                = BigDecimal.ZERO;

        if (cuentas != null) {

            for (CuentaCobrarConsulta cuenta : cuentas) {

                if (cuenta.getSaldoPendiente() != null) {

                    saldoTotal
                            = saldoTotal.add(
                                    cuenta.getSaldoPendiente()
                            );
                }
            }
        }

        lblCuentasEncontradasN.setText(
                String.valueOf(cantidad)
        );

        lblSaldoTotalPendiente.setText(
                formatearMoneda(saldoTotal)
        );
    }

    private void actualizarProximosVencimientos(
            List<CuentaCobrarConsulta> cuentas) {

        int vencenHoy = 0;
        int vencenDentroDe7Dias = 0;

        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(7);

        if (cuentas != null) {

            for (CuentaCobrarConsulta cuenta : cuentas) {

                if (cuenta.getEstado() != EstadoCuenta.PENDIENTE) {
                    continue;
                }

                LocalDate fechaVencimiento
                        = cuenta.getFechaVencimiento();

                if (fechaVencimiento == null) {
                    continue;
                }

                if (fechaVencimiento.isEqual(hoy)) {

                    vencenHoy++;

                } else if (fechaVencimiento.isAfter(hoy)
                        && !fechaVencimiento.isAfter(limite)) {

                    vencenDentroDe7Dias++;
                }
            }
        }

        lblDiasHoyN.setText(
                String.valueOf(vencenHoy)
        );

        lblDiasDentrode7DiasN.setText(
                String.valueOf(vencenDentroDe7Dias)
        );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void limpiarFiltros() {

        cmbEstados.setSelectedIndex(0);

        JComboBox combo
                = (JComboBox) cmbClientes;

        combo.setModel(
                new DefaultComboBoxModel<>(
                        new Object[]{"Todos"}
                )
        );

        combo.setSelectedItem("Todos");

        jdcFechaDesde.setDate(null);
        jdcFechaHasta.setDate(null);

        consultarCuentasPorCobrar();
    }

    private void verDetalle() {

        int filaSeleccionada
                = tblCuentasPorCobrar.getSelectedRow();

        if (filaSeleccionada < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona una cuenta por cobrar para ver su detalle.",
                    "Sin selección",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int filaModelo
                = tblCuentasPorCobrar.convertRowIndexToModel(
                        filaSeleccionada
                );

        Object valorIdVenta
                = modeloCuentasPorCobrar.getValueAt(
                        filaModelo,
                        3
                );

        if (!(valorIdVenta instanceof Number numero)) {

            JOptionPane.showMessageDialog(
                    this,
                    "No fue posible obtener el ID de la venta seleccionada.",
                    "Datos inválidos",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        int idVenta
                = numero.intValue();

        try {

            Venta venta
                    = ventaService.buscarPorId(idVenta);

            CuentaCobrar cuentaCobrar
                    = ventaService.buscarCuentaCobrarPorVenta(
                            idVenta
                    );

            if (venta == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "No se encontró la venta seleccionada.",
                        "Venta no encontrada",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            if (cuentaCobrar == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "La venta seleccionada no tiene una cuenta por cobrar asociada.",
                        "Cuenta no encontrada",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            /*
         * La vista de detalle se conectará aquí cuando
         * definamos su contrato.
         *
         * Ya disponemos de:
         *
         * Venta venta
         * CuentaCobrar cuentaCobrar
         *
         * obtenidos exclusivamente mediante VentaService.
             */
            JOptionPane.showMessageDialog(
                    this,
                    "Detalle de la venta N.º "
                    + idVenta
                    + " recuperado correctamente.",
                    "Detalle",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {

            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "No fue posible obtener el detalle de la venta.\n"
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
        lblConsultaYSeguimientoObligacionesDeClientes = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        lblNombreUsuario = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        lblFechaHora = new javax.swing.JLabel();
        pnlFiltrosDeConsulta = new javax.swing.JPanel();
        lblEstado = new javax.swing.JLabel();
        cmbEstados = new javax.swing.JComboBox<>();
        lblCliente = new javax.swing.JLabel();
        cmbClientes = new javax.swing.JComboBox<>();
        lblDesde = new javax.swing.JLabel();
        jdcFechaDesde = new com.toedter.calendar.JDateChooser();
        lblHasta = new javax.swing.JLabel();
        jdcFechaHasta = new com.toedter.calendar.JDateChooser();
        btnLimpiar = new javax.swing.JButton();
        btnConsultar = new javax.swing.JButton();
        pnlTotalObligacionesEncontradas = new javax.swing.JPanel();
        lblTxtCuentasEncontradas = new javax.swing.JLabel();
        lblCuentasEncontradasN = new javax.swing.JLabel();
        pnlMontoTotalPendiente = new javax.swing.JPanel();
        lblTxtSaldoTotalPendiente = new javax.swing.JLabel();
        lblSaldoTotalPendiente = new javax.swing.JLabel();
        spnlCuentasPorCobrar = new javax.swing.JScrollPane();
        tblCuentasPorCobrar = new javax.swing.JTable();
        pnlBtnVerDetalle = new javax.swing.JPanel();
        btnVerDetalle = new javax.swing.JButton();
        pnlProximosVencimientos = new javax.swing.JPanel();
        lblProximosVencimientos = new javax.swing.JLabel();
        lblHoy = new javax.swing.JLabel();
        lblDiasHoyN = new javax.swing.JLabel();
        lblDentroDe7Dias = new javax.swing.JLabel();
        lblDiasDentrode7DiasN = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblCuentasPorPagar.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblCuentasPorPagar.setText("CUENTAS POR COBRAR");

        lblConsultaYSeguimientoObligacionesDeClientes.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblConsultaYSeguimientoObligacionesDeClientes.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblConsultaYSeguimientoObligacionesDeClientes.setText("Consulta y seguimiento de obligaciones pendientes de clientes");

        lblUsuario.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblUsuario.setText("USUARIO:");

        lblNombreUsuario.setText("Nombre A. (sesión activa)");

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
                    .addComponent(lblConsultaYSeguimientoObligacionesDeClientes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 225, Short.MAX_VALUE)
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
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblUsuario)
                            .addComponent(lblFecha))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNombreUsuario)
                            .addComponent(lblFechaHora)))
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblCuentasPorPagar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblConsultaYSeguimientoObligacionesDeClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlFiltrosDeConsulta.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "FILTROS DE CONSULTA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblEstado.setText("Estado");

        lblCliente.setText("Cliente");

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
                    .addComponent(cmbClientes, 0, 123, Short.MAX_VALUE)
                    .addComponent(lblCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                            .addComponent(lblCliente)
                            .addComponent(lblDesde)
                            .addComponent(lblHasta))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFiltrosDeConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlFiltrosDeConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cmbEstados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jdcFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jdcFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlTotalObligacionesEncontradas.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblTxtCuentasEncontradas.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblTxtCuentasEncontradas.setText("CUENTAS ENCONTRADAS:");

        lblCuentasEncontradasN.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblCuentasEncontradasN.setText("N");

        javax.swing.GroupLayout pnlTotalObligacionesEncontradasLayout = new javax.swing.GroupLayout(pnlTotalObligacionesEncontradas);
        pnlTotalObligacionesEncontradas.setLayout(pnlTotalObligacionesEncontradasLayout);
        pnlTotalObligacionesEncontradasLayout.setHorizontalGroup(
            pnlTotalObligacionesEncontradasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTotalObligacionesEncontradasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTxtCuentasEncontradas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblCuentasEncontradasN, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlTotalObligacionesEncontradasLayout.setVerticalGroup(
            pnlTotalObligacionesEncontradasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTotalObligacionesEncontradasLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlTotalObligacionesEncontradasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTxtCuentasEncontradas)
                    .addComponent(lblCuentasEncontradasN))
                .addGap(11, 11, 11))
        );

        pnlMontoTotalPendiente.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblTxtSaldoTotalPendiente.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblTxtSaldoTotalPendiente.setText("SALDO TOTAL PENDIENTE");

        lblSaldoTotalPendiente.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblSaldoTotalPendiente.setText("S/ 3,300");

        javax.swing.GroupLayout pnlMontoTotalPendienteLayout = new javax.swing.GroupLayout(pnlMontoTotalPendiente);
        pnlMontoTotalPendiente.setLayout(pnlMontoTotalPendienteLayout);
        pnlMontoTotalPendienteLayout.setHorizontalGroup(
            pnlMontoTotalPendienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMontoTotalPendienteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTxtSaldoTotalPendiente)
                .addGap(18, 18, 18)
                .addComponent(lblSaldoTotalPendiente)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        pnlMontoTotalPendienteLayout.setVerticalGroup(
            pnlMontoTotalPendienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMontoTotalPendienteLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlMontoTotalPendienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTxtSaldoTotalPendiente)
                    .addComponent(lblSaldoTotalPendiente))
                .addGap(12, 12, 12))
        );

        tblCuentasPorCobrar.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "CLIENTE", "DOCUMENTO", "VENTA", "FECHA VENTA", "FECHA VENC.", "MONTO TOTAL", "MONTO COBRADO", "SALDO PEND.", "ESTADO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblCuentasPorCobrar.setToolTipText("");
        spnlCuentasPorCobrar.setViewportView(tblCuentasPorCobrar);

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
            .addComponent(btnVerDetalle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlBtnVerDetalleLayout.setVerticalGroup(
            pnlBtnVerDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBtnVerDetalleLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnVerDetalle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pnlProximosVencimientos.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblProximosVencimientos.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblProximosVencimientos.setText("PRÓXIMOS VENCIMIENTOS:");

        lblHoy.setText("Hoy:");

        lblDiasHoyN.setText("n");

        lblDentroDe7Dias.setText("Dentro de 7 días:");

        lblDiasDentrode7DiasN.setText("n");

        javax.swing.GroupLayout pnlProximosVencimientosLayout = new javax.swing.GroupLayout(pnlProximosVencimientos);
        pnlProximosVencimientos.setLayout(pnlProximosVencimientosLayout);
        pnlProximosVencimientosLayout.setHorizontalGroup(
            pnlProximosVencimientosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProximosVencimientosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblProximosVencimientos)
                .addGroup(pnlProximosVencimientosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlProximosVencimientosLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblDentroDe7Dias)
                        .addGap(18, 18, 18)
                        .addComponent(lblDiasDentrode7DiasN, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 19, 19))
                    .addGroup(pnlProximosVencimientosLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(lblHoy, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDiasHoyN, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        pnlProximosVencimientosLayout.setVerticalGroup(
            pnlProximosVencimientosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProximosVencimientosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProximosVencimientosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblHoy)
                    .addComponent(lblDiasHoyN))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlProximosVencimientosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDentroDe7Dias)
                    .addComponent(lblDiasDentrode7DiasN))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlProximosVencimientosLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblProximosVencimientos)
                .addGap(16, 16, 16))
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
                    .addComponent(spnlCuentasPorCobrar)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlTotalObligacionesEncontradas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlMontoTotalPendiente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlProximosVencimientos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlBtnVerDetalle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                .addComponent(spnlCuentasPorCobrar, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(pnlMontoTotalPendiente, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlProximosVencimientos, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlBtnVerDetalle, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlTotalObligacionesEncontradas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        // TODO add your handling code here:
        consultarCuentasPorCobrar();
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
            java.util.logging.Logger.getLogger(FrmCuentasCobrar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmCuentasCobrar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmCuentasCobrar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmCuentasCobrar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmCuentasCobrar dialog = new FrmCuentasCobrar(new javax.swing.JFrame(), true);
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
    private javax.swing.JComboBox<String> cmbClientes;
    private javax.swing.JComboBox<String> cmbEstados;
    private com.toedter.calendar.JDateChooser jdcFechaDesde;
    private com.toedter.calendar.JDateChooser jdcFechaHasta;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblConsultaYSeguimientoObligacionesDeClientes;
    private javax.swing.JLabel lblCuentasEncontradasN;
    private javax.swing.JLabel lblCuentasPorPagar;
    private javax.swing.JLabel lblDentroDe7Dias;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblDiasDentrode7DiasN;
    private javax.swing.JLabel lblDiasHoyN;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblFechaHora;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblHoy;
    private javax.swing.JLabel lblNombreUsuario;
    private javax.swing.JLabel lblProximosVencimientos;
    private javax.swing.JLabel lblSaldoTotalPendiente;
    private javax.swing.JLabel lblTxtCuentasEncontradas;
    private javax.swing.JLabel lblTxtSaldoTotalPendiente;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPanel pnlBtnVerDetalle;
    private javax.swing.JPanel pnlFiltrosDeConsulta;
    private javax.swing.JPanel pnlMontoTotalPendiente;
    private javax.swing.JPanel pnlProximosVencimientos;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JPanel pnlTotalObligacionesEncontradas;
    private javax.swing.JScrollPane spnlCuentasPorCobrar;
    private javax.swing.JTable tblCuentasPorCobrar;
    // End of variables declaration//GEN-END:variables
}
