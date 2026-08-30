package com.ferronor.sic.tesoreria.vista;

import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;
import com.ferronor.sic.tesoreria.logica.TesoreriaService;
import com.ferronor.sic.tesoreria.modelo.Caja;
import com.ferronor.sic.tesoreria.modelo.MovimientoCaja;
import com.ferronor.sic.tesoreria.modelo.OrigenMovimientoCaja;
import com.ferronor.sic.tesoreria.modelo.TipoMovimientoCaja;

import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class FrmMovsCaja extends javax.swing.JDialog {

    // ============================================================
    // SERVICE
    // ============================================================
    private final TesoreriaService tesoreriaService
            = ServiceFactory.tesoreriaService();

    // ============================================================
    // FORMATOS
    // ============================================================
    private final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DateTimeFormatter FORMATO_HORA
            = DateTimeFormatter.ofPattern("HH:mm");

    // ============================================================
    // ESTADO DE LA VISTA
    // ============================================================
    private List<MovimientoCaja> movimientosConsultados
            = new ArrayList<>();

    private DefaultTableModel modeloTablaMovimientos;

    /**
     * Mapa auxiliar para representar el nombre de las cajas disponibles en el
     * combo.
     *
     * La clave es idCaja. El valor es el nombre conocido de la caja.
     */
    private final Map<Integer, String> nombresCajas
            = new HashMap<>();

    /**
     * Evita que cambios programáticos del combo provoquen lógica adicional
     * mientras se carga o reconstruye su modelo.
     */
    private boolean actualizandoInterfaz;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    public FrmMovsCaja(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        configurarFormulario();
    }

    // ============================================================
    // CONFIGURACIÓN GENERAL
    // ============================================================
    private void configurarFormulario() {

        configurarCabecera();

        configurarTabla();

        configurarComboCajas();

        configurarEstadoInicial();

        cargarDatosIniciales();

        setLocationRelativeTo(getParent());
    }

    // ============================================================
    // CABECERA
    // ============================================================
    private void configurarCabecera() {

        SesionUsuario sesion
                = SesionUsuario.actual();

        if (sesion != null) {

            lblNombreApellidoUsuario.setText(
                    valorTexto(
                            sesion.getNombreCompleto()
                    )
            );

        } else {

            lblNombreApellidoUsuario.setText(
                    "Usuario actual"
            );
        }

        actualizarFechaHoraCabecera();
    }

    private void actualizarFechaHoraCabecera() {

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
    // TABLA
    // ============================================================
    private void configurarTabla() {

        modeloTablaMovimientos
                = new DefaultTableModel(
                        new Object[]{
                            "FECHA",
                            "CAJA",
                            "N.° OPERACION",
                            "TIPO",
                            "ORIGEN",
                            "DOCUMENTO",
                            "DESCRIPCION",
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

        tblMovimientosDeCaja.setModel(
                modeloTablaMovimientos
        );

        tblMovimientosDeCaja.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        /*
         * El .form deja la tabla en una zona de aproximadamente
         * 197 px. Una fuente pequeña permite visualizar todas
         * las columnas sin sacrificar legibilidad.
         */
        tblMovimientosDeCaja.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        9
                )
        );

        tblMovimientosDeCaja
                .getTableHeader()
                .setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                9
                        )
                );

        tblMovimientosDeCaja.setRowHeight(24);

        /*
         * Anchos iniciales orientativos.
         * JTable puede reajustarlos conforme cambie el tamaño.
         */
        tblMovimientosDeCaja
                .getColumnModel()
                .getColumn(0)
                .setPreferredWidth(125);

        tblMovimientosDeCaja
                .getColumnModel()
                .getColumn(1)
                .setPreferredWidth(115);

        tblMovimientosDeCaja
                .getColumnModel()
                .getColumn(2)
                .setPreferredWidth(100);

        tblMovimientosDeCaja
                .getColumnModel()
                .getColumn(3)
                .setPreferredWidth(75);

        tblMovimientosDeCaja
                .getColumnModel()
                .getColumn(4)
                .setPreferredWidth(120);

        tblMovimientosDeCaja
                .getColumnModel()
                .getColumn(5)
                .setPreferredWidth(85);

        tblMovimientosDeCaja
                .getColumnModel()
                .getColumn(6)
                .setPreferredWidth(220);

        tblMovimientosDeCaja
                .getColumnModel()
                .getColumn(7)
                .setPreferredWidth(100);
    }

    // ============================================================
    // COMBO CAJAS
    // ============================================================
    private void configurarComboCajas() {

        cmbCajas.setRenderer(
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

                if (value instanceof Caja caja) {

                    setText(
                            obtenerTextoCaja(caja)
                    );

                } else if (value == null) {

                    setText(
                            "Todas las cajas"
                    );
                }

                return this;
            }
        }
        );
    }

    private String obtenerTextoCaja(
            Caja caja) {

        if (caja == null) {
            return "Todas las cajas";
        }

        String nombre
                = caja.getNombre();

        if (nombre != null
                && !nombre.isBlank()) {

            return nombre;
        }

        return "Caja #" + caja.getIdCaja();
    }

    /**
     * Construye la lista de cajas utilizando exclusivamente la información
     * disponible desde TesoreriaService.
     *
     * Como actualmente la fachada pública no expone listarCajas(), los
     * identificadores de las cajas se obtienen desde el historial.
     */
    private void reconstruirCajasDisponibles(
            List<MovimientoCaja> movimientos) {

        nombresCajas.clear();

        /*
         * La caja abierta es la única caja cuyo nombre conocemos
         * directamente mediante el contrato público actual.
         */
        try {

            var cajaAbierta
                    = tesoreriaService.obtenerCajaAbierta();

            if (cajaAbierta.isPresent()) {

                Caja caja
                        = cajaAbierta.get();

                nombresCajas.put(
                        caja.getIdCaja(),
                        obtenerTextoCaja(caja)
                );
            }

        } catch (RuntimeException ignored) {
            /*
             * La falta de caja abierta no impide consultar
             * el historial.
             */
        }

        /*
         * Para las demás cajas históricas se conserva su id.
         * Posteriormente, cuando el contrato incorpore un
         * listado general de cajas, este método puede sustituirse
         * sin afectar el resto del formulario.
         */
        if (movimientos != null) {

            for (MovimientoCaja movimiento : movimientos) {

                if (movimiento == null) {
                    continue;
                }

                int idCaja
                        = movimiento.getIdCaja();

                nombresCajas.putIfAbsent(
                        idCaja,
                        "Caja #" + idCaja
                );
            }
        }

        /*
         * Construimos los objetos Caja que utilizará el combo.
         * Son representaciones de selección, no objetos destinados
         * a persistencia.
         */
        List<Caja> cajas
                = new ArrayList<>();

        for (Integer idCaja
                : nombresCajas.keySet()) {

            Caja caja
                    = new Caja();

            caja.setIdCaja(idCaja);

            caja.setNombre(
                    nombresCajas.get(idCaja)
            );

            cajas.add(caja);
        }

        cajas.sort(
                Comparator.comparing(
                        Caja::getNombre,
                        Comparator.nullsLast(
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
        );

        actualizandoInterfaz = true;

        DefaultComboBoxModel<Caja> modelo
                = new DefaultComboBoxModel<>();

        /*
         * Primera opción:
         * todas las cajas.
         *
         * null representa esta opción porque el combo es
         * parametrizado como JComboBox<Caja>.
         */
        modelo.addElement(null);

        for (Caja caja : cajas) {
            modelo.addElement(caja);
        }

        cmbCajas.setModel(modelo);

        cmbCajas.setSelectedItem(null);

        actualizandoInterfaz = false;
    }

    // ============================================================
    // ESTADO INICIAL
    // ============================================================
    private void configurarEstadoInicial() {

        actualizandoInterfaz = true;

        cmbCajas.setSelectedItem(null);

        actualizandoInterfaz = false;

        movimientosConsultados
                = new ArrayList<>();

        limpiarTabla();

        actualizarResumen(
                movimientosConsultados
        );
    }

    // ============================================================
    // CARGA INICIAL
    // ============================================================
    private void cargarDatosIniciales() {

        consultarMovimientos();
    }

    // ============================================================
    // CONSULTA
    // ============================================================
    private void consultarMovimientos() {

        try {

            Caja cajaSeleccionada
                    = obtenerCajaSeleccionada();

            List<MovimientoCaja> resultados;

            if (cajaSeleccionada == null) {

                resultados
                        = tesoreriaService
                                .listarMovimientosCaja();

            } else {

                resultados
                        = tesoreriaService
                                .listarMovimientosCajaPorCaja(
                                        cajaSeleccionada.getIdCaja()
                                );
            }

            movimientosConsultados
                    = resultados == null
                            ? new ArrayList<>()
                            : new ArrayList<>(resultados);

            /*
             * Orden descendente:
             * movimiento más reciente primero.
             *
             * Esto se realiza en memoria para no alterar el
             * contrato del DAO ni imponer una nueva consulta SQL.
             */
            movimientosConsultados.sort(
                    Comparator.comparing(
                            MovimientoCaja::getFecha,
                            Comparator.nullsLast(
                                    Comparator.reverseOrder()
                            )
                    )
            );

            reconstruirCajasDisponibles(
                    movimientosConsultados
            );

            restaurarSeleccionCaja(
                    cajaSeleccionada
            );

            cargarTabla(
                    movimientosConsultados
            );

            actualizarResumen(
                    movimientosConsultados
            );

            actualizarFechaHoraCabecera();

        } catch (RuntimeException ex) {

            movimientosConsultados
                    = new ArrayList<>();

            limpiarTabla();

            actualizarResumen(
                    movimientosConsultados
            );

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al consultar movimientos de caja",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // CAJA SELECCIONADA
    // ============================================================
    private Caja obtenerCajaSeleccionada() {

        Object seleccionado
                = cmbCajas.getSelectedItem();

        if (seleccionado instanceof Caja caja) {
            return caja;
        }

        return null;
    }

    private void restaurarSeleccionCaja(
            Caja cajaAnterior) {

        if (cajaAnterior == null) {

            actualizandoInterfaz = true;

            cmbCajas.setSelectedItem(null);

            actualizandoInterfaz = false;

            return;
        }

        for (int i = 0;
                i < cmbCajas.getItemCount();
                i++) {

            Caja caja
                    = cmbCajas.getItemAt(i);

            if (caja != null
                    && caja.getIdCaja()
                    == cajaAnterior.getIdCaja()) {

                actualizandoInterfaz = true;

                cmbCajas.setSelectedIndex(i);

                actualizandoInterfaz = false;

                return;
            }
        }

        actualizandoInterfaz = true;

        cmbCajas.setSelectedItem(null);

        actualizandoInterfaz = false;
    }

    // ============================================================
    // TABLA
    // ============================================================
    private void cargarTabla(
            List<MovimientoCaja> movimientos) {

        limpiarTabla();

        if (movimientos == null
                || movimientos.isEmpty()) {

            return;
        }

        for (MovimientoCaja movimiento
                : movimientos) {

            modeloTablaMovimientos.addRow(
                    new Object[]{
                        formatearFechaHora(
                                movimiento.getFecha()
                        ),
                        obtenerNombreCaja(
                                movimiento.getIdCaja()
                        ),
                        obtenerNumeroOperacion(
                                movimiento
                        ),
                        formatearTipo(
                                movimiento.getTipo()
                        ),
                        formatearOrigen(
                                movimiento.getOrigen()
                        ),
                        formatearDocumento(
                                movimiento
                                        .getIdDocumentoOrigen()
                        ),
                        valorTexto(
                                movimiento.getDescripcion()
                        ),
                        formatearMontoConSigno(
                                movimiento.getTipo(),
                                movimiento.getMonto()
                        )
                    }
            );
        }

        /*
         * Siempre trabajamos con 8 o 9 px en esta tabla porque
         * existen 8 columnas y la descripción puede ocupar
         * bastante espacio.
         */
        tblMovimientosDeCaja.revalidate();
        tblMovimientosDeCaja.repaint();
    }

    private void limpiarTabla() {

        if (modeloTablaMovimientos != null) {

            modeloTablaMovimientos.setRowCount(0);
        }
    }

    // ============================================================
    // RESUMEN
    // ============================================================
    private void actualizarResumen(
            List<MovimientoCaja> movimientos) {

        if (movimientos == null
                || movimientos.isEmpty()) {

            lblCantMovimientos.setText("0");
            lblValorIngresos.setText("S/ 0.00");
            lblValorEgresos.setText("S/ 0.00");
            lblValorSaldoNeto.setText("S/ 0.00");

            return;
        }

        BigDecimal ingresos
                = BigDecimal.ZERO;

        BigDecimal egresos
                = BigDecimal.ZERO;

        for (MovimientoCaja movimiento
                : movimientos) {

            if (movimiento == null) {
                continue;
            }

            BigDecimal monto
                    = valorSeguro(
                            movimiento.getMonto()
                    );

            if (movimiento.getTipo()
                    == TipoMovimientoCaja.INGRESO) {

                ingresos = ingresos.add(monto);

            } else if (movimiento.getTipo()
                    == TipoMovimientoCaja.EGRESO) {

                egresos = egresos.add(monto);
            }
        }

        BigDecimal saldoNeto
                = ingresos.subtract(egresos);

        lblCantMovimientos.setText(
                String.valueOf(
                        movimientos.size()
                )
        );

        lblValorIngresos.setText(
                "S/ "
                + formatearMonto(ingresos)
        );

        lblValorEgresos.setText(
                "S/ "
                + formatearMonto(egresos)
        );

        lblValorSaldoNeto.setText(
                "S/ "
                + formatearMonto(saldoNeto)
        );
    }

    // ============================================================
    // NOMBRE DE CAJA
    // ============================================================
    private String obtenerNombreCaja(
            int idCaja) {

        String nombre
                = nombresCajas.get(idCaja);

        if (nombre != null
                && !nombre.isBlank()) {

            return nombre;
        }

        return "Caja #" + idCaja;
    }

    // ============================================================
    // DOCUMENTO
    // ============================================================
    private String formatearDocumento(
            Integer idDocumento) {

        if (idDocumento == null) {
            return "—";
        }

        return "#" + idDocumento;
    }

    // ============================================================
    // NÚMERO DE OPERACIÓN
    // ============================================================
    /**
     * MovimientoCaja no dispone de un campo independiente "numero de
     * operación".
     *
     * Por eso no debemos inventarlo.
     */
    private String obtenerNumeroOperacion(
            MovimientoCaja movimiento) {

        return "—";
    }

    // ============================================================
    // TIPO
    // ============================================================
    private String formatearTipo(
            TipoMovimientoCaja tipo) {

        if (tipo == null) {
            return "—";
        }

        return switch (tipo) {

            case INGRESO ->
                "INGRESO";

            case EGRESO ->
                "EGRESO";
        };
    }

    // ============================================================
    // ORIGEN
    // ============================================================
    private String formatearOrigen(
            OrigenMovimientoCaja origen) {

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

            case GASTO_OPERATIVO ->
                "Gasto operativo";

            case DEPOSITO_CAJA ->
                "Depósito a banco";
        };
    }

    // ============================================================
    // MONTO
    // ============================================================
    private String formatearMontoConSigno(
            TipoMovimientoCaja tipo,
            BigDecimal monto) {

        String valor
                = "S/ "
                + formatearMonto(monto);

        if (tipo == TipoMovimientoCaja.EGRESO) {

            return "- " + valor;
        }

        return "+ " + valor;
    }

    private String formatearMonto(
            BigDecimal monto) {

        if (monto == null) {
            return "0.00";
        }

        return monto
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                .toPlainString();
    }

    // ============================================================
    // FECHAS
    // ============================================================
    private String formatearFechaHora(
            LocalDateTime fechaHora) {

        if (fechaHora == null) {
            return "—";
        }

        return fechaHora.format(
                FORMATO_FECHA_HORA
        );
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

    private String valorTexto(
            String valor) {

        if (valor == null
                || valor.isBlank()) {

            return "—";
        }

        return valor;
    }

    private String obtenerMensajeError(
            RuntimeException ex) {

        if (ex == null
                || ex.getMessage() == null
                || ex.getMessage().isBlank()) {

            return "Ocurrió un error al consultar los movimientos de caja.";
        }

        return ex.getMessage();
    }

    // ============================================================
    // LIMPIAR FILTRO
    // ============================================================
    private void limpiarFiltro() {

        actualizandoInterfaz = true;

        cmbCajas.setSelectedItem(null);

        actualizandoInterfaz = false;

        consultarMovimientos();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSuperior = new javax.swing.JPanel();
        lblMovimientosDeCaja = new javax.swing.JLabel();
        lblConsultaHistorialMovimientosCajas = new javax.swing.JLabel();
        lblNombreApellidoUsuario = new javax.swing.JLabel();
        lblFechaActual = new javax.swing.JLabel();
        lblHoraActual = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        pnlConsulta = new javax.swing.JPanel();
        lblCuentaBancaria = new javax.swing.JLabel();
        cmbCajas = new javax.swing.JComboBox<>();
        btnConsultar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        pnlResumen = new javax.swing.JPanel();
        lblMovimientos = new javax.swing.JLabel();
        lblCantMovimientos = new javax.swing.JLabel();
        lblIngresos = new javax.swing.JLabel();
        lblValorIngresos = new javax.swing.JLabel();
        lblEgresos = new javax.swing.JLabel();
        lblValorEgresos = new javax.swing.JLabel();
        lblSaldoNeto = new javax.swing.JLabel();
        lblValorSaldoNeto = new javax.swing.JLabel();
        pnlMovimientos = new javax.swing.JPanel();
        spnlMovimientosDeCaja = new javax.swing.JScrollPane();
        tblMovimientosDeCaja = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblMovimientosDeCaja.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblMovimientosDeCaja.setText("MOVIMIENTOS DE CAJA");

        lblConsultaHistorialMovimientosCajas.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblConsultaHistorialMovimientosCajas.setText("Consulta e historial de movimientos de cajas  ");

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
                    .addComponent(lblConsultaHistorialMovimientosCajas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblMovimientosDeCaja, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(lblMovimientosDeCaja)
                    .addComponent(lblNombreApellidoUsuario)
                    .addComponent(lblUsuario))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblConsultaHistorialMovimientosCajas, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFechaActual)
                    .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        pnlConsulta.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. CONSULTA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblCuentaBancaria.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCuentaBancaria.setText("CAJA");

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
                        .addComponent(cmbCajas, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                    .addComponent(cmbCajas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpiar)
                    .addComponent(btnConsultar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlResumen.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. RESUMEN", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblMovimientos.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblMovimientos.setText("MOVIMIENTOS");

        lblCantMovimientos.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblCantMovimientos.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCantMovimientos.setText("7");

        lblIngresos.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblIngresos.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblIngresos.setText("INGRESOS");

        lblValorIngresos.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorIngresos.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblValorIngresos.setText("S/ 4,220.00");

        lblEgresos.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblEgresos.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblEgresos.setText("EGRESOS");

        lblValorEgresos.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorEgresos.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblValorEgresos.setText("S/ 2,160.00");

        lblSaldoNeto.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblSaldoNeto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblSaldoNeto.setText("SALDO NETO");

        lblValorSaldoNeto.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorSaldoNeto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblValorSaldoNeto.setText("S/ 2,060.00");

        javax.swing.GroupLayout pnlResumenLayout = new javax.swing.GroupLayout(pnlResumen);
        pnlResumen.setLayout(pnlResumenLayout);
        pnlResumenLayout.setHorizontalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addGap(90, 90, 90)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblMovimientos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCantMovimientos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(40, 40, 40)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblValorIngresos, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblIngresos, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblValorEgresos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblEgresos, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSaldoNeto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorSaldoNeto, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE))
                .addGap(84, 84, 84))
        );
        pnlResumenLayout.setVerticalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlResumenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlResumenLayout.createSequentialGroup()
                        .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlResumenLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblEgresos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(lblSaldoNeto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblValorEgresos)
                            .addComponent(lblValorSaldoNeto)))
                    .addGroup(pnlResumenLayout.createSequentialGroup()
                        .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblMovimientos, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblIngresos))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblCantMovimientos)
                            .addComponent(lblValorIngresos))))
                .addContainerGap())
        );

        pnlMovimientos.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "03. MOVIMIENTOS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        tblMovimientosDeCaja.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "FECHA", "CUENTA", "TIPO", "ORIGEN", "DOCUMENTO", "DESCRIPCION", "MONTO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlMovimientosDeCaja.setViewportView(tblMovimientosDeCaja);

        javax.swing.GroupLayout pnlMovimientosLayout = new javax.swing.GroupLayout(pnlMovimientos);
        pnlMovimientos.setLayout(pnlMovimientosLayout);
        pnlMovimientosLayout.setHorizontalGroup(
            pnlMovimientosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMovimientosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlMovimientosDeCaja)
                .addContainerGap())
        );
        pnlMovimientosLayout.setVerticalGroup(
            pnlMovimientosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMovimientosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlMovimientosDeCaja, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
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
        consultarMovimientos();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
        limpiarFiltro();
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
            java.util.logging.Logger.getLogger(FrmMovsCaja.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmMovsCaja.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmMovsCaja.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmMovsCaja.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmMovsCaja dialog = new FrmMovsCaja(new javax.swing.JFrame(), true);
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
    private javax.swing.JComboBox<Caja> cmbCajas;
    private javax.swing.JLabel lblCantMovimientos;
    private javax.swing.JLabel lblConsultaHistorialMovimientosCajas;
    private javax.swing.JLabel lblCuentaBancaria;
    private javax.swing.JLabel lblEgresos;
    private javax.swing.JLabel lblFechaActual;
    private javax.swing.JLabel lblHoraActual;
    private javax.swing.JLabel lblIngresos;
    private javax.swing.JLabel lblMovimientos;
    private javax.swing.JLabel lblMovimientosDeCaja;
    private javax.swing.JLabel lblNombreApellidoUsuario;
    private javax.swing.JLabel lblSaldoNeto;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblValorEgresos;
    private javax.swing.JLabel lblValorIngresos;
    private javax.swing.JLabel lblValorSaldoNeto;
    private javax.swing.JPanel pnlConsulta;
    private javax.swing.JPanel pnlMovimientos;
    private javax.swing.JPanel pnlResumen;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JScrollPane spnlMovimientosDeCaja;
    private javax.swing.JTable tblMovimientosDeCaja;
    // End of variables declaration//GEN-END:variables
}
