package com.ferronor.sic.tesoreria.vista;

import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.tesoreria.logica.TesoreriaService;
import com.ferronor.sic.tesoreria.modelo.CuentaBancaria;
import com.ferronor.sic.tesoreria.modelo.Moneda;
import com.ferronor.sic.tesoreria.modelo.MovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.OrigenMovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.TipoMovimientoBanco;
import com.ferronor.sic.shared.SesionUsuario;

import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class FrmMovsBancarios extends javax.swing.JDialog {

    // ============================================================
    // SERVICE
    // ============================================================
    private final TesoreriaService tesoreriaService
            = ServiceFactory.tesoreriaService();

    // ============================================================
    // FORMATOS
    // ============================================================
    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_HORA
            = DateTimeFormatter.ofPattern("HH:mm");

    // ============================================================
    // ESTADO DE LA VISTA
    // ============================================================
    private List<CuentaBancaria> cuentasBancarias
            = new ArrayList<>();

    private List<MovimientoBanco> movimientosConsultados
            = new ArrayList<>();

    private CuentaBancaria cuentaSeleccionada;

    private DefaultTableModel modeloMovimientos;

    /**
     * Permite evitar reacciones innecesarias del combo cuando se modifica
     * programáticamente durante la carga inicial.
     */
    private boolean actualizandoInterfaz;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    public FrmMovsBancarios(
            java.awt.Frame parent,
            boolean modal) {

        super(parent, modal);

        initComponents();

        configurarFormulario();

        setLocationRelativeTo(getParent());

    }

    // ============================================================
    // CONFIGURACIÓN GENERAL
    // ============================================================
    private void configurarFormulario() {

        configurarTabla();

        configurarComboCuentas();

        configurarEstadoInicial();

        actualizarDatosUsuario();

        cargarCuentasBancarias();

        consultarMovimientos();
    }

    // ============================================================
    // TABLA
    // ============================================================
    private void configurarTabla() {

        modeloMovimientos
                = new DefaultTableModel(
                        new Object[]{
                            "FECHA",
                            "CUENTA",
                            "N.° OPERACIÓN",
                            "TIPO",
                            "ORIGEN",
                            "DOCUMENTO",
                            "MONTO"
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

        tblMovimientosBancarios.setModel(
                modeloMovimientos
        );

        tblMovimientosBancarios.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblMovimientosBancarios.setRowHeight(27);
        tblMovimientosBancarios.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        9
                )
        );

        tblMovimientosBancarios
                .getTableHeader()
                .setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                9
                        )
                );

        // --------------------------------------------------------
        // ANCHOS
        // --------------------------------------------------------
        tblMovimientosBancarios
                .getColumnModel()
                .getColumn(0)
                .setPreferredWidth(125);

        tblMovimientosBancarios
                .getColumnModel()
                .getColumn(1)
                .setPreferredWidth(190);

        tblMovimientosBancarios
                .getColumnModel()
                .getColumn(2)
                .setPreferredWidth(120);

        tblMovimientosBancarios
                .getColumnModel()
                .getColumn(3)
                .setPreferredWidth(110);

        tblMovimientosBancarios
                .getColumnModel()
                .getColumn(4)
                .setPreferredWidth(150);

        tblMovimientosBancarios
                .getColumnModel()
                .getColumn(5)
                .setPreferredWidth(90);

        tblMovimientosBancarios
                .getColumnModel()
                .getColumn(6)
                .setPreferredWidth(125);
    }

    // ============================================================
    // COMBO CUENTAS
    // ============================================================
    private void configurarComboCuentas() {

        cmbCuentasBancarias.setRenderer(
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

                if (value == null) {

                    setText(
                            "Todas las cuentas"
                    );

                    return this;
                }

                if (value instanceof CuentaBancaria cuenta) {

                    setText(
                            construirTextoCuentaCombo(
                                    cuenta
                            )
                    );
                }

                return this;
            }
        }
        );
    }

    private String construirTextoCuentaCombo(
            CuentaBancaria cuenta) {

        if (cuenta == null) {
            return "—";
        }

        String identificador
                = valorTexto(cuenta.getAlias());

        if (identificador.isBlank()) {

            identificador
                    = valorTexto(cuenta.getBanco());
        }

        String numero
                = valorTexto(
                        cuenta.getNumeroCuenta()
                );

        String moneda
                = cuenta.getMoneda() == null
                ? ""
                : cuenta.getMoneda().name();

        String texto
                = identificador;

        if (!numero.isBlank()) {

            texto += " — " + numero;
        }

        if (!moneda.isBlank()) {

            texto += " — " + moneda;
        }

        return texto;
    }

    private void cargarCuentasBancarias() {

        try {

            List<CuentaBancaria> resultado
                    = tesoreriaService
                            .listarCuentasBancariasActivas();

            if (resultado == null) {

                resultado
                        = Collections.emptyList();
            }

            cuentasBancarias
                    = new ArrayList<>(resultado);

            cuentasBancarias.sort(
                    Comparator.comparing(
                            this::obtenerNombreOrdenCuenta,
                            String.CASE_INSENSITIVE_ORDER
                    )
            );

            actualizandoInterfaz = true;

            DefaultComboBoxModel<CuentaBancaria> modelo
                    = new DefaultComboBoxModel<>();

            // null representa TODAS LAS CUENTAS
            modelo.addElement(null);

            for (CuentaBancaria cuenta
                    : cuentasBancarias) {

                modelo.addElement(cuenta);
            }

            cmbCuentasBancarias.setModel(
                    modelo
            );

            cmbCuentasBancarias
                    .setSelectedItem(null);

            cuentaSeleccionada = null;

            actualizandoInterfaz = false;

        } catch (RuntimeException ex) {

            cuentasBancarias
                    = new ArrayList<>();

            cuentaSeleccionada = null;

            actualizandoInterfaz = true;

            DefaultComboBoxModel<CuentaBancaria> modelo
                    = new DefaultComboBoxModel<>();

            modelo.addElement(null);

            cmbCuentasBancarias.setModel(
                    modelo
            );

            cmbCuentasBancarias
                    .setSelectedItem(null);

            actualizandoInterfaz = false;

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al cargar cuentas bancarias",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String obtenerNombreOrdenCuenta(
            CuentaBancaria cuenta) {

        if (cuenta == null) {
            return "";
        }

        String nombre
                = cuenta.getAlias();

        if (nombre == null
                || nombre.isBlank()) {

            nombre = cuenta.getBanco();
        }

        return nombre == null
                ? ""
                : nombre;
    }

    // ============================================================
    // ESTADO INICIAL
    // ============================================================
    private void configurarEstadoInicial() {

        cuentaSeleccionada = null;

        movimientosConsultados
                = new ArrayList<>();

        cmbCuentasBancarias
                .setSelectedItem(null);

        lblNombreApellidoUsuario.setText(
                ""
        );

        lblFechaActual.setText(
                ""
        );

        lblHoraActual.setText(
                ""
        );

        lblCantMovimientos.setText(
                "0"
        );

        lblValorTotalDepositos.setText(
                "PEN 0.00"
        );

        lblValorTotalDepositosUSD.setText(
                "USD 0.00"
        );

        lblValorTotalRetiros.setText(
                "PEN 0.00"
        );

        lblValorTotalRetirosUSD.setText(
                "USD 0.00"
        );

        modeloMovimientos.setRowCount(0);
    }

    // ============================================================
    // CABECERA
    // ============================================================
    private void actualizarDatosUsuario() {

        SesionUsuario sesion
                = SesionUsuario.actual();

        if (sesion != null) {

            lblNombreApellidoUsuario.setText(
                    sesion.getNombreCompleto()
            );
        }

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
    // CONSULTA
    // ============================================================
    private void consultarMovimientos() {

        try {

            Object seleccionado
                    = cmbCuentasBancarias
                            .getSelectedItem();

            if (seleccionado == null) {

                cuentaSeleccionada = null;

                List<MovimientoBanco> resultados
                        = tesoreriaService
                                .listarMovimientosBancarios();

                movimientosConsultados
                        = resultados != null
                                ? new ArrayList<>(resultados)
                                : new ArrayList<>();

            } else if (seleccionado instanceof CuentaBancaria cuenta) {

                cuentaSeleccionada = cuenta;

                List<MovimientoBanco> resultados
                        = tesoreriaService
                                .listarMovimientosBancariosPorCuenta(
                                        cuenta.getIdCuentaBancaria()
                                );

                movimientosConsultados
                        = resultados != null
                                ? new ArrayList<>(resultados)
                                : new ArrayList<>();

            } else {

                cuentaSeleccionada = null;

                movimientosConsultados
                        = new ArrayList<>();
            }

            cargarTablaMovimientos();

            actualizarResumen();

        } catch (RuntimeException ex) {

            movimientosConsultados
                    = new ArrayList<>();

            modeloMovimientos
                    .setRowCount(0);

            actualizarResumen();

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al consultar movimientos bancarios",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // TABLA
    // ============================================================
    private void cargarTablaMovimientos() {

        modeloMovimientos.setRowCount(0);

        for (MovimientoBanco movimiento
                : movimientosConsultados) {

            CuentaBancaria cuenta
                    = buscarCuenta(
                            movimiento.getIdCuentaBancaria()
                    );

            modeloMovimientos.addRow(
                    new Object[]{
                        formatearFechaHora(
                                movimiento.getFecha()
                        ),
                        construirTextoCuentaTabla(
                                cuenta
                        ),
                        formatearNumeroOperacion(
                                movimiento.getNumeroOperacion()
                        ),
                        formatearTipoMovimiento(
                                movimiento.getTipo()
                        ),
                        formatearOrigenMovimiento(
                                movimiento.getOrigen()
                        ),
                        formatearDocumento(
                                movimiento.getIdDocumentoOrigen()
                        ),
                        formatearMontoMovimiento(
                                movimiento,
                                cuenta
                        )
                    }
            );
        }
    }

    private String construirTextoCuentaTabla(
            CuentaBancaria cuenta) {

        if (cuenta == null) {
            return "Cuenta #" + "-";
        }

        String alias
                = cuenta.getAlias();

        if (alias == null
                || alias.isBlank()) {

            alias = cuenta.getBanco();
        }

        return valorTexto(alias);
    }

    private CuentaBancaria buscarCuenta(
            int idCuentaBancaria) {

        for (CuentaBancaria cuenta
                : cuentasBancarias) {

            if (cuenta.getIdCuentaBancaria()
                    == idCuentaBancaria) {

                return cuenta;
            }
        }

        return null;
    }

    // ============================================================
    // RESUMEN
    // ============================================================
    private void actualizarResumen() {

        lblCantMovimientos.setText(
                String.valueOf(
                        movimientosConsultados.size()
                )
        );

        EnumMap<Moneda, BigDecimal> depositos
                = crearMapaMontos();

        EnumMap<Moneda, BigDecimal> retiros
                = crearMapaMontos();

        for (MovimientoBanco movimiento
                : movimientosConsultados) {

            CuentaBancaria cuenta
                    = buscarCuenta(
                            movimiento.getIdCuentaBancaria()
                    );

            if (cuenta == null
                    || cuenta.getMoneda() == null) {

                continue;
            }

            Moneda moneda
                    = cuenta.getMoneda();

            BigDecimal monto
                    = valorSeguro(
                            movimiento.getMonto()
                    );

            if (movimiento.getTipo()
                    == TipoMovimientoBanco.DEPOSITO) {

                depositos.put(
                        moneda,
                        depositos.get(moneda)
                                .add(monto)
                );
            }

            if (movimiento.getTipo()
                    == TipoMovimientoBanco.RETIRO) {

                retiros.put(
                        moneda,
                        retiros.get(moneda)
                                .add(monto)
                );
            }
        }

        actualizarResumenMoneda(
                depositos.get(Moneda.PEN),
                lblValorTotalDepositos,
                "PEN"
        );

        actualizarResumenMoneda(
                depositos.get(Moneda.USD),
                lblValorTotalDepositosUSD,
                "USD"
        );

        actualizarResumenMoneda(
                retiros.get(Moneda.PEN),
                lblValorTotalRetiros,
                "PEN"
        );

        actualizarResumenMoneda(
                retiros.get(Moneda.USD),
                lblValorTotalRetirosUSD,
                "USD"
        );
    }

    private EnumMap<Moneda, BigDecimal> crearMapaMontos() {

        EnumMap<Moneda, BigDecimal> mapa
                = new EnumMap<>(Moneda.class);

        mapa.put(
                Moneda.PEN,
                BigDecimal.ZERO
        );

        mapa.put(
                Moneda.USD,
                BigDecimal.ZERO
        );

        return mapa;
    }

    private void actualizarResumenMoneda(
            BigDecimal monto,
            javax.swing.JLabel label,
            String moneda) {

        label.setText(
                moneda
                + " "
                + formatearMonto(
                        monto
                )
        );
    }

    // ============================================================
    // FORMATOS DE TABLA
    // ============================================================
    private String formatearFechaHora(
            LocalDateTime fecha) {

        return fecha == null
                ? "—"
                : fecha.format(
                        FORMATO_FECHA_HORA
                );
    }

    private String formatearNumeroOperacion(
            String numeroOperacion) {

        if (numeroOperacion == null
                || numeroOperacion.isBlank()) {

            return "—";
        }

        return numeroOperacion;
    }

    private String formatearDocumento(
            Integer idDocumento) {

        if (idDocumento == null) {
            return "—";
        }

        return "#" + idDocumento;
    }

    private String formatearTipoMovimiento(
            TipoMovimientoBanco tipo) {

        if (tipo == null) {
            return "—";
        }

        return switch (tipo) {

            case DEPOSITO ->
                "Depósito";

            case RETIRO ->
                "Retiro";

            case TRANSFERENCIA ->
                "Transferencia";
        };
    }

    private String formatearOrigenMovimiento(
            OrigenMovimientoBanco origen) {

        if (origen == null) {
            return "—";
        }

        return switch (origen) {

            case VENTA_CONTADO ->
                "Venta al contado";

            case COBRO_CLIENTE ->
                "Cobro a cliente";

            case COMPRA_CONTADO ->
                "Compra al contado";

            case PAGO_PROVEEDOR ->
                "Pago a proveedor";

            case DEPOSITO_CAJA ->
                "Depósito desde caja";
        };
    }

    private String formatearMontoMovimiento(
            MovimientoBanco movimiento,
            CuentaBancaria cuenta) {

        BigDecimal monto
                = valorSeguro(
                        movimiento.getMonto()
                );

        String prefijo
                = obtenerSimboloMoneda(
                        cuenta == null
                                ? null
                                : cuenta.getMoneda()
                );

        return prefijo
                + " "
                + formatearMonto(monto);
    }

    private String obtenerSimboloMoneda(
            Moneda moneda) {

        if (moneda == null) {
            return "";
        }

        return switch (moneda) {

            case PEN ->
                "S/";

            case USD ->
                "US$";
        };
    }

    // ============================================================
    // LIMPIAR
    // ============================================================
    private void limpiarFiltros() {

        actualizandoInterfaz = true;

        cmbCuentasBancarias
                .setSelectedItem(null);

        actualizandoInterfaz = false;

        cuentaSeleccionada = null;

        consultarMovimientos();
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    private BigDecimal valorSeguro(
            BigDecimal valor) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private String formatearMonto(
            BigDecimal monto) {

        if (monto == null) {
            monto = BigDecimal.ZERO;
        }

        return monto
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                .toPlainString();
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
        lblMovimientosBancarios = new javax.swing.JLabel();
        lblConsultaHistorialMovimientosCtasBancarias = new javax.swing.JLabel();
        lblNombreApellidoUsuario = new javax.swing.JLabel();
        lblFechaActual = new javax.swing.JLabel();
        lblHoraActual = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        pnlConsulta = new javax.swing.JPanel();
        lblCuentaBancaria = new javax.swing.JLabel();
        cmbCuentasBancarias = new javax.swing.JComboBox<>();
        btnConsultar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        pnlResumen = new javax.swing.JPanel();
        lblMovimientos = new javax.swing.JLabel();
        lblCantMovimientos = new javax.swing.JLabel();
        lblTotalDepositos = new javax.swing.JLabel();
        lblValorTotalDepositos = new javax.swing.JLabel();
        lblTotalRetiros = new javax.swing.JLabel();
        lblValorTotalRetiros = new javax.swing.JLabel();
        lblValorTotalDepositosUSD = new javax.swing.JLabel();
        lblValorTotalRetirosUSD = new javax.swing.JLabel();
        pnlMovimientos = new javax.swing.JPanel();
        spnlMovimientosBancarios = new javax.swing.JScrollPane();
        tblMovimientosBancarios = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblMovimientosBancarios.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblMovimientosBancarios.setText("MOVIMIENTOS BANCARIOS");

        lblConsultaHistorialMovimientosCtasBancarias.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblConsultaHistorialMovimientosCtasBancarias.setText("Consulta e historial de movimientos de cuentas bancarias ");

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
                    .addComponent(lblConsultaHistorialMovimientosCtasBancarias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblMovimientosBancarios, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(lblMovimientosBancarios)
                    .addComponent(lblNombreApellidoUsuario)
                    .addComponent(lblUsuario))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblConsultaHistorialMovimientosCtasBancarias, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFechaActual)
                    .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        pnlConsulta.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. CONSULTA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblCuentaBancaria.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCuentaBancaria.setText("CUENTA BANCARIA");

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

        javax.swing.GroupLayout pnlConsultaLayout = new javax.swing.GroupLayout(pnlConsulta);
        pnlConsulta.setLayout(pnlConsultaLayout);
        pnlConsultaLayout.setHorizontalGroup(
            pnlConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsultaLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(pnlConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCuentaBancaria)
                    .addGroup(pnlConsultaLayout.createSequentialGroup()
                        .addComponent(cmbCuentasBancarias, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnConsultar)))
                .addContainerGap())
        );
        pnlConsultaLayout.setVerticalGroup(
            pnlConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsultaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCuentaBancaria)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbCuentasBancarias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpiar)
                    .addComponent(btnConsultar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlResumen.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. RESUMEN", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblMovimientos.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblMovimientos.setText("MOVIMIENTOS");

        lblCantMovimientos.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblCantMovimientos.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCantMovimientos.setText("5");

        lblTotalDepositos.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalDepositos.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTotalDepositos.setText("TOTAL DÉPOSITOS");

        lblValorTotalDepositos.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalDepositos.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblValorTotalDepositos.setText("S/ 4,100.00");

        lblTotalRetiros.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalRetiros.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTotalRetiros.setText("TOTAL RETIROS");

        lblValorTotalRetiros.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalRetiros.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblValorTotalRetiros.setText("S/ 1,200.00");

        lblValorTotalDepositosUSD.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalDepositosUSD.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblValorTotalDepositosUSD.setText("USD 0.00");

        lblValorTotalRetirosUSD.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalRetirosUSD.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblValorTotalRetirosUSD.setText("USD 0.00");

        javax.swing.GroupLayout pnlResumenLayout = new javax.swing.GroupLayout(pnlResumen);
        pnlResumen.setLayout(pnlResumenLayout);
        pnlResumenLayout.setHorizontalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblMovimientos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCantMovimientos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(36, 36, 36)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlResumenLayout.createSequentialGroup()
                        .addComponent(lblValorTotalDepositos, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblValorTotalDepositosUSD, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblTotalDepositos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlResumenLayout.createSequentialGroup()
                        .addComponent(lblValorTotalRetiros, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblValorTotalRetirosUSD, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblTotalRetiros, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlResumenLayout.setVerticalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTotalRetiros, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblMovimientos, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblTotalDepositos)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCantMovimientos)
                    .addComponent(lblValorTotalDepositos)
                    .addComponent(lblValorTotalRetiros)
                    .addComponent(lblValorTotalDepositosUSD)
                    .addComponent(lblValorTotalRetirosUSD))
                .addContainerGap())
        );

        pnlMovimientos.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "03. MOVIMIENTOS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        tblMovimientosBancarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "FECHA", "CUENTA", "N.° OPERACION", "TIPO", "ORIGEN", "DOCUMENTO", "MONTO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlMovimientosBancarios.setViewportView(tblMovimientosBancarios);

        javax.swing.GroupLayout pnlMovimientosLayout = new javax.swing.GroupLayout(pnlMovimientos);
        pnlMovimientos.setLayout(pnlMovimientosLayout);
        pnlMovimientosLayout.setHorizontalGroup(
            pnlMovimientosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMovimientosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlMovimientosBancarios)
                .addContainerGap())
        );
        pnlMovimientosLayout.setVerticalGroup(
            pnlMovimientosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMovimientosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlMovimientosBancarios, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
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
                    .addComponent(pnlMovimientos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlResumen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlConsulta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlConsulta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResumen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlMovimientos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
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
            java.util.logging.Logger.getLogger(FrmMovsBancarios.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmMovsBancarios.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmMovsBancarios.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmMovsBancarios.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmMovsBancarios dialog = new FrmMovsBancarios(new javax.swing.JFrame(), true);
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
    private javax.swing.JComboBox<CuentaBancaria> cmbCuentasBancarias;
    private javax.swing.JLabel lblCantMovimientos;
    private javax.swing.JLabel lblConsultaHistorialMovimientosCtasBancarias;
    private javax.swing.JLabel lblCuentaBancaria;
    private javax.swing.JLabel lblFechaActual;
    private javax.swing.JLabel lblHoraActual;
    private javax.swing.JLabel lblMovimientos;
    private javax.swing.JLabel lblMovimientosBancarios;
    private javax.swing.JLabel lblNombreApellidoUsuario;
    private javax.swing.JLabel lblTotalDepositos;
    private javax.swing.JLabel lblTotalRetiros;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblValorTotalDepositos;
    private javax.swing.JLabel lblValorTotalDepositosUSD;
    private javax.swing.JLabel lblValorTotalRetiros;
    private javax.swing.JLabel lblValorTotalRetirosUSD;
    private javax.swing.JPanel pnlConsulta;
    private javax.swing.JPanel pnlMovimientos;
    private javax.swing.JPanel pnlResumen;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JScrollPane spnlMovimientosBancarios;
    private javax.swing.JTable tblMovimientosBancarios;
    // End of variables declaration//GEN-END:variables
}
