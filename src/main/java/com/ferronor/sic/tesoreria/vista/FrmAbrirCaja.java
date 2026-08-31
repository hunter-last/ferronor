package com.ferronor.sic.tesoreria.vista;

import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;
import com.ferronor.sic.tesoreria.logica.TesoreriaService;
import com.ferronor.sic.tesoreria.modelo.Caja;
import com.ferronor.sic.tesoreria.modelo.EstadoCaja;

import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JOptionPane;

/**
 * Formulario para apertura de caja.
 *
 * Flujo:
 *
 * Vista -> ServiceFactory -> TesoreriaService -> lógica existente
 *
 * La vista no accede a DAO, SQL ni TransactionContext.
 *
 * IMPORTANTE: El método tesoreriaService.listarCajas() requiere la pequeña
 * extensión del contrato público de TesoreriaService indicada previamente.
 * CajaDAO ya dispone internamente de listar().
 */
public class FrmAbrirCaja extends javax.swing.JDialog {

    // ============================================================
    // SERVICIO
    // ============================================================
    private final TesoreriaService tesoreriaService
            = ServiceFactory.tesoreriaService();

    // ============================================================
    // ESTADO DE LA VISTA
    // ============================================================
    /**
     * Caja que actualmente está abierta en el sistema.
     *
     * Puede ser null cuando no existe ninguna caja abierta.
     */
    private Caja cajaAbierta;

    /**
     * Evita ejecutar lógica de selección mientras se reconstruye
     * programáticamente el combo.
     */
    private boolean actualizandoCombo;

    /**
     * Indica si la vista ya terminó de inicializarse.
     */
    private boolean formularioInicializado;

    // ============================================================
    // FORMATOS
    // ============================================================
    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter FORMATO_HORA
            = DateTimeFormatter.ofPattern("HH:mm");

    private static final DecimalFormat FORMATO_MONEDA;

    static {

        DecimalFormatSymbols symbols
                = DecimalFormatSymbols.getInstance(Locale.US);

        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');

        FORMATO_MONEDA = new DecimalFormat(
                "#,##0.00",
                symbols
        );

        FORMATO_MONEDA.setGroupingUsed(true);
    }

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    public FrmAbrirCaja(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        initComponents();

        configurarFormulario();

        setLocationRelativeTo(getParent());

    }

    // ============================================================
    // CONFIGURACIÓN GENERAL
    // ============================================================
    private void configurarFormulario() {

        configurarCabecera();

        configurarComponentesInformativos();

        configurarComboCajas();

        configurarEventos();

        configurarEstadoInicial();

        formularioInicializado = true;

        cargarEstadoActual();
    }

    // ============================================================
    // CABECERA
    // ============================================================
    private void configurarCabecera() {

        actualizarUsuarioCabecera();

        actualizarFechaHoraCabecera();
    }

    private void actualizarUsuarioCabecera() {

        try {

            SesionUsuario sesion
                    = SesionUsuario.actual();

            lblNombreApellidoUsuario.setText(
                    valorTexto(
                            sesion.getNombreCompleto()
                    )
            );

        } catch (RuntimeException ex) {

            lblNombreApellidoUsuario.setText(
                    "Usuario actual"
            );
        }
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
    // COMPONENTES INFORMATIVOS
    // ============================================================
    private void configurarComponentesInformativos() {

        /*
         * Estos componentes son exclusivamente informativos.
         * El usuario nunca debe editarlos.
         */
        txtEstadoCaja.setEditable(false);
        txtEstadoCaja.setFocusable(false);

        /*
         * txtMensajeVariable se utiliza como indicador dinámico
         * del estado actual del formulario.
         */
        txtMensajeVariable.setEditable(false);
        txtMensajeVariable.setFocusable(false);

        /*
         * Mantener tipografía monoespaciada para valores operativos.
         */
        txtEstadoCaja.setFont(
                new Font(
                        "Consolas",
                        Font.BOLD,
                        10
                )
        );

        txtMensajeVariable.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        14
                )
        );

        lblValorSaldoActual.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        14
                )
        );

        lblFechaYHoraUltimaApertura.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        14
                )
        );
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

                } else {

                    setText(
                            value == null
                                    ? "Seleccione una caja..."
                                    : value.toString()
                    );
                }

                return this;
            }
        }
        );

        cmbCajas.setToolTipText(
                "Seleccione la caja que desea abrir"
        );
    }

    private String obtenerTextoCaja(Caja caja) {

        if (caja == null) {
            return "Seleccione una caja...";
        }

        String nombre
                = caja.getNombre();

        if (nombre != null
                && !nombre.isBlank()) {

            return nombre;
        }

        return "Caja #" + caja.getIdCaja();
    }

    // ============================================================
    // EVENTOS
    // ============================================================
    private void configurarEventos() {

        cmbCajas.addActionListener(
                evt -> {

                    if (!formularioInicializado
                    || actualizandoCombo) {
                        return;
                    }

                    cajaSeleccionada();
                }
        );

        btnAbrirCaja.addActionListener(
                evt -> abrirCaja()
        );

        btnCancelar.addActionListener(
                evt -> dispose()
        );
    }

    // ============================================================
    // ESTADO INICIAL
    // ============================================================
    private void configurarEstadoInicial() {

        actualizandoCombo = true;

        DefaultComboBoxModel<Caja> modelo
                = new DefaultComboBoxModel<>();

        modelo.addElement(null);

        cmbCajas.setModel(modelo);

        cmbCajas.setSelectedItem(null);

        actualizandoCombo = false;

        cmbCajas.setEnabled(true);

        btnAbrirCaja.setEnabled(false);

        limpiarInformacionCaja();

        mostrarMensaje(
                "Seleccione una caja cerrada para continuar."
        );
    }

    // ============================================================
    // CARGA DEL ESTADO ACTUAL
    // ============================================================
    private void cargarEstadoActual() {

        try {

            Optional<Caja> resultado
                    = tesoreriaService.obtenerCajaAbierta();

            if (resultado.isPresent()) {

                cajaAbierta
                        = resultado.get();

                mostrarCajaYaAbierta(
                        cajaAbierta
                );

                return;
            }

            cajaAbierta = null;

            cargarCajasDisponibles();

        } catch (RuntimeException ex) {

            cajaAbierta = null;

            limpiarInformacionCaja();

            cmbCajas.setEnabled(false);

            btnAbrirCaja.setEnabled(false);

            mostrarMensaje(
                    obtenerMensajeError(ex)
            );

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al cargar cajas",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // CARGAR CAJAS
    // ============================================================
    private void cargarCajasDisponibles() {

        try {

            List<Caja> cajas
                    = tesoreriaService.listarCajas();

            if (cajas == null) {

                cajas
                        = new ArrayList<>();
            }

            /*
             * Para la operación de apertura solo tienen sentido
             * las cajas CERRADAS cuando no existe una caja abierta.
             */
            List<Caja> cajasCerradas
                    = cajas.stream()
                            .filter(caja -> caja != null)
                            .filter(caja
                                    -> caja.getEstado()
                            == EstadoCaja.CERRADA
                            )
                            .sorted(
                                    Comparator.comparing(
                                            Caja::getNombre,
                                            Comparator.nullsLast(
                                                    String.CASE_INSENSITIVE_ORDER
                                            )
                                    )
                            )
                            .toList();

            reconstruirCombo(
                    cajasCerradas
            );

            if (cajasCerradas.isEmpty()) {

                limpiarInformacionCaja();

                cmbCajas.setEnabled(false);

                btnAbrirCaja.setEnabled(false);

                mostrarMensaje(
                        "No existen cajas cerradas disponibles para apertura."
                );

                return;
            }

            cmbCajas.setEnabled(true);

            btnAbrirCaja.setEnabled(false);

            limpiarInformacionCaja();

            mostrarMensaje(
                    "Seleccione una caja cerrada para continuar."
            );

        } catch (RuntimeException ex) {

            limpiarInformacionCaja();

            cmbCajas.setEnabled(false);

            btnAbrirCaja.setEnabled(false);

            mostrarMensaje(
                    obtenerMensajeError(ex)
            );

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al cargar cajas",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void reconstruirCombo(
            List<Caja> cajas) {

        actualizandoCombo = true;

        DefaultComboBoxModel<Caja> modelo
                = new DefaultComboBoxModel<>();

        /*
         * Primera opción: ninguna caja seleccionada.
         */
        modelo.addElement(null);

        if (cajas != null) {

            for (Caja caja : cajas) {

                modelo.addElement(
                        caja
                );
            }
        }

        cmbCajas.setModel(
                modelo
        );

        cmbCajas.setSelectedItem(
                null
        );

        actualizandoCombo = false;
    }

    // ============================================================
    // SELECCIÓN DE CAJA
    // ============================================================
    private void cajaSeleccionada() {

        Caja caja
                = obtenerCajaSeleccionada();

        if (caja == null) {

            limpiarInformacionCaja();

            btnAbrirCaja.setEnabled(false);

            mostrarMensaje(
                    "Seleccione una caja cerrada para continuar."
            );

            return;
        }

        /*
         * Protección adicional de la interfaz.
         * La validación real sigue estando en el backend.
         */
        if (caja.getEstado() == EstadoCaja.ABIERTA) {

            mostrarCajaYaAbierta(
                    caja
            );

            return;
        }

        mostrarInformacionCaja(
                caja
        );

        btnAbrirCaja.setEnabled(true);

        mostrarMensaje(
                "La caja está cerrada y puede ser abierta."
        );
    }

    private Caja obtenerCajaSeleccionada() {

        Object seleccionado
                = cmbCajas.getSelectedItem();

        if (seleccionado instanceof Caja caja) {

            return caja;
        }

        return null;
    }

    // ============================================================
    // INFORMACIÓN DE CAJA
    // ============================================================
    private void mostrarInformacionCaja(
            Caja caja) {

        if (caja == null) {

            limpiarInformacionCaja();

            return;
        }

        lblNombreCajaSeleccionada.setText(
                valorTexto(
                        caja.getNombre()
                )
        );

        EstadoCaja estado
                = caja.getEstado();

        txtEstadoCaja.setText(
                estado != null
                        ? estado.name()
                        : "—"
        );

        lblValorSaldoActual.setText(
                formatearMoneda(
                        caja.getSaldoActual()
                )
        );

        lblFechaYHoraUltimaApertura.setText(
                formatearFechaHora(
                        caja.getFechaApertura()
                )
        );

        lblNombreApellidoResponsable.setText(
                obtenerNombreResponsable(
                        caja
                )
        );

        /*
         * Si la caja está cerrada, no existe responsable actual.
         */
        if (estado == EstadoCaja.CERRADA) {

            lblNombreApellidoResponsable.setText(
                    "No asignado"
            );
        }
    }

    private void limpiarInformacionCaja() {

        lblNombreCajaSeleccionada.setText(
                "—"
        );

        txtEstadoCaja.setText(
                "—"
        );

        lblValorSaldoActual.setText(
                "S/ 0.00"
        );

        lblFechaYHoraUltimaApertura.setText(
                "—"
        );

        lblNombreApellidoResponsable.setText(
                "No asignado"
        );
    }

    // ============================================================
    // CAJA YA ABIERTA
    // ============================================================
    private void mostrarCajaYaAbierta(
            Caja caja) {

        if (caja == null) {

            return;
        }

        actualizandoCombo = true;

        DefaultComboBoxModel<Caja> modelo
                = new DefaultComboBoxModel<>();

        modelo.addElement(
                caja
        );

        cmbCajas.setModel(
                modelo
        );

        cmbCajas.setSelectedItem(
                caja
        );

        actualizandoCombo = false;

        cmbCajas.setEnabled(false);

        btnAbrirCaja.setEnabled(false);

        mostrarInformacionCaja(
                caja
        );

        /*
         * Para este escenario el responsable actual debe mostrarse
         * como el usuario asociado a idUsuarioActual.
         *
         * Como Caja solo conserva el ID del usuario actual,
         * intentamos obtener su nombre a partir de la sesión
         * cuando corresponde.
         */
        actualizarResponsableCajaAbierta(
                caja
        );

        mostrarMensaje(
                "Existe una caja abierta actualmente. Debe cerrarla antes de abrir otra caja."
        );
    }

    private void actualizarResponsableCajaAbierta(
            Caja caja) {

        if (caja == null
                || caja.getIdUsuarioActual() == null) {

            lblNombreApellidoResponsable.setText(
                    "No asignado"
            );

            return;
        }

        try {

            SesionUsuario sesion
                    = SesionUsuario.actual();

            if (sesion.getIdUsuario()
                    == caja.getIdUsuarioActual()) {

                lblNombreApellidoResponsable.setText(
                        valorTexto(
                                sesion.getNombreCompleto()
                        )
                );

                return;
            }

        } catch (RuntimeException ignored) {

            // La información seguirá siendo segura aunque no
            // pueda resolverse el nombre desde la sesión actual.
        }

        lblNombreApellidoResponsable.setText(
                "Usuario #" + caja.getIdUsuarioActual()
        );
    }

    // ============================================================
    // APERTURA
    // ============================================================
    private void abrirCaja() {

        Caja caja
                = obtenerCajaSeleccionada();

        if (caja == null) {

            mostrarMensaje(
                    "Seleccione una caja cerrada para continuar."
            );

            return;
        }

        if (caja.getEstado() != EstadoCaja.CERRADA) {

            mostrarCajaYaAbierta(
                    caja
            );

            return;
        }

        int idUsuario;

        try {

            idUsuario
                    = SesionUsuario.actual()
                            .getIdUsuario();

        } catch (RuntimeException ex) {

            mostrarMensaje(
                    "No existe una sesión de usuario activa."
            );

            JOptionPane.showMessageDialog(
                    this,
                    "No existe una sesión de usuario activa.",
                    "No se puede abrir la caja",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        /*
         * Bloquear temporalmente para impedir doble clic.
         */
        btnAbrirCaja.setEnabled(false);

        cmbCajas.setEnabled(false);

        try {

            RespuestaOperacion<Void> resultado
                    = tesoreriaService.abrirCaja(
                            caja.getIdCaja(),
                            idUsuario
                    );

            if (!resultado.isExito()) {

                btnAbrirCaja.setEnabled(true);

                cmbCajas.setEnabled(true);

                mostrarMensaje(
                        valorMensajeRespuesta(
                                resultado
                        )
                );

                JOptionPane.showMessageDialog(
                        this,
                        valorMensajeRespuesta(
                                resultado
                        ),
                        "No se pudo abrir la caja",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            /*
             * Volvemos a consultar el backend.
             *
             * No construimos manualmente una Caja abierta.
             * El backend es la fuente de verdad.
             */
            cargarCajaAbiertaDespuesDeApertura();

        } catch (RuntimeException ex) {

            btnAbrirCaja.setEnabled(true);

            cmbCajas.setEnabled(true);

            mostrarMensaje(
                    obtenerMensajeError(ex)
            );

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al abrir la caja",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // VERIFICAR APERTURA EXITOSA
    // ============================================================
    private void cargarCajaAbiertaDespuesDeApertura() {

        try {

            Optional<Caja> resultado
                    = tesoreriaService.obtenerCajaAbierta();

            if (resultado.isEmpty()) {

                /*
                 * Situación anómala: la operación dijo éxito pero
                 * inmediatamente no aparece una caja abierta.
                 */
                btnAbrirCaja.setEnabled(false);

                cmbCajas.setEnabled(false);

                mostrarMensaje(
                        "La operación finalizó correctamente, pero no fue posible recuperar la caja abierta."
                );

                return;
            }

            cajaAbierta
                    = resultado.get();

            mostrarCajaAbiertaDespuesDeOperacion(
                    cajaAbierta
            );

        } catch (RuntimeException ex) {

            btnAbrirCaja.setEnabled(false);

            cmbCajas.setEnabled(false);

            mostrarMensaje(
                    "La caja fue abierta, pero no se pudo actualizar la información visual."
            );

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void mostrarCajaAbiertaDespuesDeOperacion(
            Caja caja) {

        if (caja == null) {

            return;
        }

        actualizandoCombo = true;

        DefaultComboBoxModel<Caja> modelo
                = new DefaultComboBoxModel<>();

        modelo.addElement(
                caja
        );

        cmbCajas.setModel(
                modelo
        );

        cmbCajas.setSelectedItem(
                caja
        );

        actualizandoCombo = false;

        cmbCajas.setEnabled(false);

        btnAbrirCaja.setEnabled(false);

        mostrarInformacionCaja(
                caja
        );

        actualizarResponsableCajaAbierta(
                caja
        );

        txtEstadoCaja.setText(
                EstadoCaja.ABIERTA.name()
        );

        mostrarMensaje(
                "La caja fue abierta correctamente."
        );
    }

    // ============================================================
    // MENSAJES
    // ============================================================
    private void mostrarMensaje(
            String mensaje) {

        txtMensajeVariable.setText(
                valorTexto(
                        mensaje
                )
        );
    }

    // ============================================================
    // RESPONSABLE
    // ============================================================
    private String obtenerNombreResponsable(
            Caja caja) {

        if (caja == null
                || caja.getIdUsuarioActual() == null) {

            return "No asignado";
        }

        try {

            SesionUsuario sesion
                    = SesionUsuario.actual();

            if (sesion.getIdUsuario()
                    == caja.getIdUsuarioActual()) {

                return valorTexto(
                        sesion.getNombreCompleto()
                );
            }

        } catch (RuntimeException ignored) {

            // Se utilizará el identificador como respaldo.
        }

        return "Usuario #"
                + caja.getIdUsuarioActual();
    }

    // ============================================================
    // FORMATEO
    // ============================================================
    private String formatearMoneda(
            BigDecimal valor) {

        if (valor == null) {

            valor
                    = BigDecimal.ZERO;
        }

        return "S/ "
                + FORMATO_MONEDA.format(
                        valor
                );
    }

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
    private String valorTexto(
            String texto) {

        if (texto == null
                || texto.isBlank()) {

            return "—";
        }

        return texto;
    }

    private String valorMensajeRespuesta(
            RespuestaOperacion<?> resultado) {

        if (resultado == null) {

            return "No fue posible completar la operación.";
        }

        String mensaje
                = resultado.getMensaje();

        if (mensaje == null
                || mensaje.isBlank()) {

            return "No fue posible completar la operación.";
        }

        return mensaje;
    }

    private String obtenerMensajeError(
            RuntimeException ex) {

        if (ex == null) {

            return "Ocurrió un error inesperado.";
        }

        String mensaje
                = ex.getMessage();

        if (mensaje == null
                || mensaje.isBlank()) {

            return "Ocurrió un error inesperado.";
        }

        return mensaje;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSuperior = new javax.swing.JPanel();
        lblAbrirCaja = new javax.swing.JLabel();
        lblAperturaHabilitacionCajaOperacionesDelTurno = new javax.swing.JLabel();
        lblNombreApellidoUsuario = new javax.swing.JLabel();
        lblFechaActual = new javax.swing.JLabel();
        lblHoraActual = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        pnlSeleccionCaja = new javax.swing.JPanel();
        lblCaja = new javax.swing.JLabel();
        cmbCajas = new javax.swing.JComboBox<>();
        pnlInformacionDeLaCaja = new javax.swing.JPanel();
        pnlCaja = new javax.swing.JPanel();
        lblCajaSeleccionada = new javax.swing.JLabel();
        lblNombreCajaSeleccionada = new javax.swing.JLabel();
        pnlEstado = new javax.swing.JPanel();
        lblEstadoCajaSeleccionada = new javax.swing.JLabel();
        txtEstadoCaja = new javax.swing.JTextField();
        pnlUltimaApertura = new javax.swing.JPanel();
        lblUltimaAperturaCajaSeleccionada = new javax.swing.JLabel();
        lblFechaYHoraUltimaApertura = new javax.swing.JLabel();
        pnlSaldoActual = new javax.swing.JPanel();
        lblSaldoActualCajaSeleccionada = new javax.swing.JLabel();
        lblValorSaldoActual = new javax.swing.JLabel();
        pnlResponsableActual = new javax.swing.JPanel();
        lblReponsableActualCajaSeleccionada = new javax.swing.JLabel();
        lblNombreApellidoResponsable = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        txtMensajeVariable = new javax.swing.JTextField();
        pnlBotones = new javax.swing.JPanel();
        btnAbrirCaja = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblAbrirCaja.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblAbrirCaja.setText("ABRIR CAJA");

        lblAperturaHabilitacionCajaOperacionesDelTurno.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblAperturaHabilitacionCajaOperacionesDelTurno.setText("Apertura y habilitación de la caja para operaciones del turno ");

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
                    .addComponent(lblAperturaHabilitacionCajaOperacionesDelTurno, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblAbrirCaja, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblNombreApellidoUsuario))
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblFechaActual)
                        .addGap(18, 18, 18)
                        .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20))
        );
        pnlSuperiorLayout.setVerticalGroup(
            pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSuperiorLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNombreApellidoUsuario)
                            .addComponent(lblUsuario))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblAperturaHabilitacionCajaOperacionesDelTurno, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFechaActual)
                            .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblAbrirCaja, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlSeleccionCaja.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. SELECCIÓN DE CAJA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblCaja.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCaja.setText("CAJA");

        javax.swing.GroupLayout pnlSeleccionCajaLayout = new javax.swing.GroupLayout(pnlSeleccionCaja);
        pnlSeleccionCaja.setLayout(pnlSeleccionCajaLayout);
        pnlSeleccionCajaLayout.setHorizontalGroup(
            pnlSeleccionCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSeleccionCajaLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(lblCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbCajas, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlSeleccionCajaLayout.setVerticalGroup(
            pnlSeleccionCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSeleccionCajaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSeleccionCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCaja)
                    .addComponent(cmbCajas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlInformacionDeLaCaja.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. INFORMACIÓN DE LA CAJA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        pnlCaja.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblCajaSeleccionada.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCajaSeleccionada.setText("CAJA");

        lblNombreCajaSeleccionada.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblNombreCajaSeleccionada.setText("Caja Principal");

        javax.swing.GroupLayout pnlCajaLayout = new javax.swing.GroupLayout(pnlCaja);
        pnlCaja.setLayout(pnlCajaLayout);
        pnlCajaLayout.setHorizontalGroup(
            pnlCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCajaLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(pnlCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblNombreCajaSeleccionada, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCajaSeleccionada, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(90, Short.MAX_VALUE))
        );
        pnlCajaLayout.setVerticalGroup(
            pnlCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCajaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCajaSeleccionada)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblNombreCajaSeleccionada, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pnlEstado.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblEstadoCajaSeleccionada.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblEstadoCajaSeleccionada.setText("ESTADO");

        txtEstadoCaja.setFont(new java.awt.Font("Consolas", 1, 10)); // NOI18N
        txtEstadoCaja.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtEstadoCaja.setText("ABIERTA");

        javax.swing.GroupLayout pnlEstadoLayout = new javax.swing.GroupLayout(pnlEstado);
        pnlEstado.setLayout(pnlEstadoLayout);
        pnlEstadoLayout.setHorizontalGroup(
            pnlEstadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEstadoLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlEstadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtEstadoCaja, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                    .addComponent(lblEstadoCajaSeleccionada, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(85, Short.MAX_VALUE))
        );
        pnlEstadoLayout.setVerticalGroup(
            pnlEstadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEstadoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblEstadoCajaSeleccionada)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtEstadoCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(8, Short.MAX_VALUE))
        );

        pnlUltimaApertura.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblUltimaAperturaCajaSeleccionada.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblUltimaAperturaCajaSeleccionada.setText("ULTIMA APERTURA");

        lblFechaYHoraUltimaApertura.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblFechaYHoraUltimaApertura.setText("28/08/2026 08:02");

        javax.swing.GroupLayout pnlUltimaAperturaLayout = new javax.swing.GroupLayout(pnlUltimaApertura);
        pnlUltimaApertura.setLayout(pnlUltimaAperturaLayout);
        pnlUltimaAperturaLayout.setHorizontalGroup(
            pnlUltimaAperturaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlUltimaAperturaLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlUltimaAperturaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUltimaAperturaCajaSeleccionada)
                    .addComponent(lblFechaYHoraUltimaApertura, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        pnlUltimaAperturaLayout.setVerticalGroup(
            pnlUltimaAperturaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlUltimaAperturaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblUltimaAperturaCajaSeleccionada)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblFechaYHoraUltimaApertura, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                .addContainerGap())
        );

        pnlSaldoActual.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblSaldoActualCajaSeleccionada.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblSaldoActualCajaSeleccionada.setText("SALDO ACTUAL");

        lblValorSaldoActual.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblValorSaldoActual.setText("S/ 2,450.00");

        javax.swing.GroupLayout pnlSaldoActualLayout = new javax.swing.GroupLayout(pnlSaldoActual);
        pnlSaldoActual.setLayout(pnlSaldoActualLayout);
        pnlSaldoActualLayout.setHorizontalGroup(
            pnlSaldoActualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSaldoActualLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlSaldoActualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblValorSaldoActual, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                    .addComponent(lblSaldoActualCajaSeleccionada, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        pnlSaldoActualLayout.setVerticalGroup(
            pnlSaldoActualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSaldoActualLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSaldoActualCajaSeleccionada)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorSaldoActual, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                .addContainerGap())
        );

        pnlResponsableActual.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblReponsableActualCajaSeleccionada.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblReponsableActualCajaSeleccionada.setText("RESPONSABLE ACTUAL");

        lblNombreApellidoResponsable.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblNombreApellidoResponsable.setText("Juan Pérez");

        javax.swing.GroupLayout pnlResponsableActualLayout = new javax.swing.GroupLayout(pnlResponsableActual);
        pnlResponsableActual.setLayout(pnlResponsableActualLayout);
        pnlResponsableActualLayout.setHorizontalGroup(
            pnlResponsableActualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResponsableActualLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlResponsableActualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblReponsableActualCajaSeleccionada, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblNombreApellidoResponsable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlResponsableActualLayout.setVerticalGroup(
            pnlResponsableActualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResponsableActualLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblReponsableActualCajaSeleccionada)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblNombreApellidoResponsable)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        txtMensajeVariable.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        txtMensajeVariable.setText("Seleccione una caja cerrada para continuar. ");

        javax.swing.GroupLayout pnlInformacionDeLaCajaLayout = new javax.swing.GroupLayout(pnlInformacionDeLaCaja);
        pnlInformacionDeLaCaja.setLayout(pnlInformacionDeLaCajaLayout);
        pnlInformacionDeLaCajaLayout.setHorizontalGroup(
            pnlInformacionDeLaCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(pnlInformacionDeLaCajaLayout.createSequentialGroup()
                .addGroup(pnlInformacionDeLaCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlInformacionDeLaCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(pnlUltimaApertura, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(pnlInformacionDeLaCajaLayout.createSequentialGroup()
                            .addGap(29, 29, 29)
                            .addGroup(pnlInformacionDeLaCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(pnlInformacionDeLaCajaLayout.createSequentialGroup()
                                    .addComponent(pnlCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(pnlEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(pnlSaldoActual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(pnlResponsableActual, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(pnlInformacionDeLaCajaLayout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(txtMensajeVariable, javax.swing.GroupLayout.PREFERRED_SIZE, 483, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlInformacionDeLaCajaLayout.setVerticalGroup(
            pnlInformacionDeLaCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInformacionDeLaCajaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlInformacionDeLaCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlInformacionDeLaCajaLayout.createSequentialGroup()
                        .addComponent(pnlEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlUltimaApertura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlInformacionDeLaCajaLayout.createSequentialGroup()
                        .addComponent(pnlCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlSaldoActual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResponsableActual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMensajeVariable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlBotones.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        btnAbrirCaja.setBackground(new java.awt.Color(51, 102, 0));
        btnAbrirCaja.setText("Abrir Caja");
        btnAbrirCaja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAbrirCajaActionPerformed(evt);
            }
        });

        btnCancelar.setBackground(new java.awt.Color(51, 51, 51));
        btnCancelar.setText("Cancelar");
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
                .addComponent(btnAbrirCaja)
                .addContainerGap())
        );
        pnlBotonesLayout.setVerticalGroup(
            pnlBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBotonesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAbrirCaja)
                    .addComponent(btnCancelar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlSuperior, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlSeleccionCaja, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlInformacionDeLaCaja, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlBotones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSeleccionCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlInformacionDeLaCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlBotones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAbrirCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbrirCajaActionPerformed
        // TODO add your handling code here:
        abrirCaja();
    }//GEN-LAST:event_btnAbrirCajaActionPerformed

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
            java.util.logging.Logger.getLogger(FrmAbrirCaja.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmAbrirCaja.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmAbrirCaja.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmAbrirCaja.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmAbrirCaja dialog = new FrmAbrirCaja(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnAbrirCaja;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JComboBox<Caja> cmbCajas;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblAbrirCaja;
    private javax.swing.JLabel lblAperturaHabilitacionCajaOperacionesDelTurno;
    private javax.swing.JLabel lblCaja;
    private javax.swing.JLabel lblCajaSeleccionada;
    private javax.swing.JLabel lblEstadoCajaSeleccionada;
    private javax.swing.JLabel lblFechaActual;
    private javax.swing.JLabel lblFechaYHoraUltimaApertura;
    private javax.swing.JLabel lblHoraActual;
    private javax.swing.JLabel lblNombreApellidoResponsable;
    private javax.swing.JLabel lblNombreApellidoUsuario;
    private javax.swing.JLabel lblNombreCajaSeleccionada;
    private javax.swing.JLabel lblReponsableActualCajaSeleccionada;
    private javax.swing.JLabel lblSaldoActualCajaSeleccionada;
    private javax.swing.JLabel lblUltimaAperturaCajaSeleccionada;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblValorSaldoActual;
    private javax.swing.JPanel pnlBotones;
    private javax.swing.JPanel pnlCaja;
    private javax.swing.JPanel pnlEstado;
    private javax.swing.JPanel pnlInformacionDeLaCaja;
    private javax.swing.JPanel pnlResponsableActual;
    private javax.swing.JPanel pnlSaldoActual;
    private javax.swing.JPanel pnlSeleccionCaja;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JPanel pnlUltimaApertura;
    private javax.swing.JTextField txtEstadoCaja;
    private javax.swing.JTextField txtMensajeVariable;
    // End of variables declaration//GEN-END:variables
}
