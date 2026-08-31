package com.ferronor.sic.tesoreria.vista;

import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;
import com.ferronor.sic.tesoreria.logica.TesoreriaService;
import com.ferronor.sic.tesoreria.modelo.Caja;

import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class FrmCierreCaja extends javax.swing.JDialog {

    // ============================================================
    // SERVICIO
    // ============================================================
    private final TesoreriaService tesoreriaService
            = ServiceFactory.tesoreriaService();

    // ============================================================
    // ESTADO DE LA VISTA
    // ============================================================
    private Caja cajaAbierta;

    private BigDecimal saldoSistema = BigDecimal.ZERO;

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
    public FrmCierreCaja(java.awt.Frame parent, boolean modal) {
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

        configurarCampos();

        configurarListenerSaldoFinalReal();

        configurarEventos();

        cargarCajaAbierta();
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
    // CONFIGURACIÓN DE CAMPOS
    // ============================================================
    private void configurarCampos() {

        txtUnidadMoneda.setEditable(false);

        txtResumenSaldoDelSistema.setEditable(false);
        txtResumenValorSaldoDelSistema.setEditable(false);

        txtResumenSaldoFinalReal.setEditable(false);
        txtResumenValorSaldoFinalReal.setEditable(false);

        txtResumenDiferencia.setEditable(false);
        txtResumenValorDiferencia.setEditable(false);

        /*
         * El .form definitivo ya contiene el panel visual
         * correspondiente a un faltante.
         *
         * Debe permanecer oculto inicialmente.
         */
        pnlAdvertencia.setVisible(false);

        /*
         * Mantener el estilo definido en el .form.
         */
        txtSaldoFinalReal.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        18
                )
        );
    }

    // ============================================================
    // EVENTOS
    // ============================================================
    private void configurarEventos() {

        /*
         * Estos listeners complementan los eventos generados
         * por NetBeans.
         */
    }

    // ============================================================
    // LISTENER DEL SALDO FINAL REAL
    // ============================================================
    private void configurarListenerSaldoFinalReal() {

        txtSaldoFinalReal.getDocument().addDocumentListener(
                new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarArqueo();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarArqueo();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarArqueo();
            }
        });
    }

    // ============================================================
    // CARGAR CAJA ABIERTA
    // ============================================================
    private void cargarCajaAbierta() {

        try {

            Optional<Caja> resultado
                    = tesoreriaService.obtenerCajaAbierta();

            if (resultado.isEmpty()) {

                manejarSinCajaAbierta();

                return;
            }

            cajaAbierta = resultado.get();

            mostrarCajaAbierta();

        } catch (RuntimeException ex) {

            manejarErrorCargaCaja(ex);
        }
    }

    // ============================================================
    // MOSTRAR CAJA ABIERTA
    // ============================================================
    private void mostrarCajaAbierta() {

        if (cajaAbierta == null) {

            manejarSinCajaAbierta();

            return;
        }

        // --------------------------------------------------------
        // Caja
        // --------------------------------------------------------
        lblNombreCaja.setText(
                valorTexto(
                        cajaAbierta.getNombre()
                )
        );

        // --------------------------------------------------------
        // Estado
        // --------------------------------------------------------
        lblEstadoCaja.setText(
                cajaAbierta.getEstado() == null
                ? "ABIERTA"
                : cajaAbierta.getEstado().name()
        );

        // --------------------------------------------------------
        // Usuario actual
        // --------------------------------------------------------
        try {

            SesionUsuario sesion
                    = SesionUsuario.actual();

            lblNombreApellidoUsuarioPnlCajaAbierta.setText(
                    valorTexto(
                            sesion.getNombreCompleto()
                    )
            );

        } catch (RuntimeException ex) {

            lblNombreApellidoUsuarioPnlCajaAbierta.setText(
                    "Usuario actual"
            );
        }

        // --------------------------------------------------------
        // Fecha y hora de apertura
        // --------------------------------------------------------
        if (cajaAbierta.getFechaApertura() != null) {

            lblFechaHoraApertura.setText(
                    cajaAbierta
                            .getFechaApertura()
                            .format(FORMATO_FECHA_HORA)
            );

        } else {

            lblFechaHoraApertura.setText("-");
        }

        // --------------------------------------------------------
        // Saldo del sistema
        // --------------------------------------------------------
        saldoSistema = normalizarMonto(
                cajaAbierta.getSaldoActual()
        );

        lblValorSaldoDelSistema.setText(
                formatearMoneda(
                        saldoSistema
                )
        );

        txtResumenValorSaldoDelSistema.setText(
                formatearMoneda(
                        saldoSistema
                )
        );

        // --------------------------------------------------------
        // Mostrar paneles
        // --------------------------------------------------------
        pnlCajaAbierta.setVisible(true);
        pnlArqueoCaja.setVisible(true);
        pnlBotones.setVisible(true);

        btnCerrarCaja.setEnabled(true);

        /*
         * Usamos el valor que ya existe en el .form:
         * 2,050.00
         */
        if (txtSaldoFinalReal.getText() == null
                || txtSaldoFinalReal.getText().isBlank()) {

            txtSaldoFinalReal.setText(
                    "0.00"
            );
        }

        actualizarArqueo();

        revalidate();
        repaint();
    }

    // ============================================================
    // SIN CAJA ABIERTA
    // ============================================================
    private void manejarSinCajaAbierta() {

        cajaAbierta = null;
        saldoSistema = BigDecimal.ZERO.setScale(
                2,
                RoundingMode.HALF_UP
        );

        /*
         * El .form definitivo no contiene un panel separado
         * para "sin caja abierta", por lo que no se inventa.
         *
         * Se informa el estado mediante los componentes existentes.
         */
        lblNombreCaja.setText(
                "No hay una caja abierta"
        );

        lblEstadoCaja.setText(
                "SIN CAJA"
        );

        lblNombreApellidoUsuarioPnlCajaAbierta.setText(
                obtenerNombreUsuarioActual()
        );

        lblFechaHoraApertura.setText("-");

        lblValorSaldoDelSistema.setText(
                formatearMoneda(
                        saldoSistema
                )
        );

        txtResumenValorSaldoDelSistema.setText(
                formatearMoneda(
                        saldoSistema
                )
        );

        txtSaldoFinalReal.setText("");

        txtResumenValorSaldoFinalReal.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        txtResumenValorDiferencia.setText(
                formatearMoneda(
                        BigDecimal.ZERO
                )
        );

        pnlAdvertencia.setVisible(false);

        pnlArqueoCaja.setVisible(false);

        btnCerrarCaja.setEnabled(false);

        btnCancelar.setEnabled(true);

        revalidate();
        repaint();

        JOptionPane.showMessageDialog(
                this,
                "No existe una caja abierta actualmente.",
                "Cierre de caja",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ============================================================
    // ERROR AL CARGAR CAJA
    // ============================================================
    private void manejarErrorCargaCaja(
            RuntimeException ex) {

        cajaAbierta = null;
        saldoSistema = BigDecimal.ZERO;

        pnlCajaAbierta.setVisible(false);
        pnlArqueoCaja.setVisible(false);

        btnCerrarCaja.setEnabled(false);

        JOptionPane.showMessageDialog(
                this,
                obtenerMensajeError(ex),
                "Error al cargar la caja abierta",
                JOptionPane.ERROR_MESSAGE
        );

        revalidate();
        repaint();
    }

    // ============================================================
    // ACTUALIZAR ARQUEO
    // ============================================================
    private void actualizarArqueo() {

        BigDecimal saldoFinalReal
                = obtenerSaldoFinalReal();

        BigDecimal diferencia
                = saldoFinalReal.subtract(
                        saldoSistema
                );

        // --------------------------------------------------------
        // Saldo del sistema
        // --------------------------------------------------------
        txtResumenValorSaldoDelSistema.setText(
                formatearMoneda(
                        saldoSistema
                )
        );

        // --------------------------------------------------------
        // Saldo final real
        // --------------------------------------------------------
        txtResumenValorSaldoFinalReal.setText(
                formatearMoneda(
                        saldoFinalReal
                )
        );

        // --------------------------------------------------------
        // Diferencia
        // --------------------------------------------------------
        txtResumenValorDiferencia.setText(
                formatearDiferencia(
                        diferencia
                )
        );

        /*
         * REGLA DEFINITIVA DEL FORMULARIO:
         *
         * pnlAdvertencia SOLO se muestra cuando:
         *
         * saldoFinalReal < saldoSistema
         *
         * Por tanto:
         *
         * diferencia < 0 → visible
         * diferencia = 0 → oculto
         * diferencia > 0 → oculto
         */
        boolean mostrarAdvertencia
                = diferencia.compareTo(
                        BigDecimal.ZERO
                ) < 0;

        pnlAdvertencia.setVisible(
                mostrarAdvertencia
        );

        if (mostrarAdvertencia) {

            lblDiferenciaDeCaja.setText(
                    "⚠ DIFERENCIA DE CAJA"
            );

            lblMensajeAdvertencia.setText(
                    "El saldo real es menor que el saldo del sistema."
            );
        }

        /*
         * GroupLayout redistribuye automáticamente el espacio
         * cuando pnlAdvertencia cambia de visibilidad.
         */
        pnlArqueoCaja.revalidate();
        pnlArqueoCaja.repaint();
    }

    // ============================================================
    // OBTENER SALDO FINAL REAL
    // ============================================================
    private BigDecimal obtenerSaldoFinalReal() {

        String texto
                = txtSaldoFinalReal.getText();

        if (texto == null
                || texto.isBlank()) {

            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        texto = texto
                .trim()
                .replace(",", "");

        try {

            return new BigDecimal(
                    texto
            ).setScale(
                    2,
                    RoundingMode.HALF_UP
            );

        } catch (NumberFormatException ex) {

            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }
    }

    // ============================================================
    // CERRAR CAJA
    // ============================================================
    private void cerrarCaja() {

        if (cajaAbierta == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "No existe una caja abierta para cerrar.",
                    "Cierre de caja",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String textoSaldo
                = txtSaldoFinalReal.getText();

        if (textoSaldo == null
                || textoSaldo.isBlank()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Ingrese el saldo final real.",
                    "Cierre de caja",
                    JOptionPane.WARNING_MESSAGE
            );

            txtSaldoFinalReal.requestFocusInWindow();

            return;
        }

        BigDecimal saldoFinalReal
                = obtenerSaldoFinalReal();

        /*
         * Si el texto tenía un formato inválido,
         * obtenemos una indicación explícita.
         */
        String textoNormalizado
                = textoSaldo
                        .trim()
                        .replace(",", "");

        try {

            new BigDecimal(textoNormalizado);

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Ingrese un saldo final real válido.",
                    "Cierre de caja",
                    JOptionPane.WARNING_MESSAGE
            );

            txtSaldoFinalReal.requestFocusInWindow();

            return;
        }

        if (saldoFinalReal.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "El saldo final real no puede ser negativo.",
                    "Cierre de caja",
                    JOptionPane.WARNING_MESSAGE
            );

            txtSaldoFinalReal.requestFocusInWindow();

            return;
        }

        int idUsuario;

        try {

            idUsuario
                    = SesionUsuario.actual()
                            .getIdUsuario();

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "No existe una sesión de usuario activa.",
                    "Cierre de caja",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        BigDecimal diferencia
                = saldoFinalReal.subtract(
                        saldoSistema
                );

        // --------------------------------------------------------
        // Confirmación
        // --------------------------------------------------------
        int confirmacion
                = JOptionPane.showConfirmDialog(
                        this,
                        construirMensajeConfirmacion(
                                saldoFinalReal,
                                diferencia
                        ),
                        "Confirmar cierre de caja",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (confirmacion
                != JOptionPane.YES_OPTION) {

            return;
        }

        // --------------------------------------------------------
        // Deshabilitar mientras se ejecuta la operación
        // --------------------------------------------------------
        btnCerrarCaja.setEnabled(false);
        btnCancelar.setEnabled(false);

        try {

            RespuestaOperacion<Void> respuesta
                    = tesoreriaService.cerrarCaja(
                            cajaAbierta.getIdCaja(),
                            saldoFinalReal,
                            idUsuario
                    );

            if (respuesta == null) {

                btnCerrarCaja.setEnabled(true);
                btnCancelar.setEnabled(true);

                JOptionPane.showMessageDialog(
                        this,
                        "No se recibió respuesta del servicio.",
                        "Cierre de caja",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            if (!respuesta.isExito()) {

                btnCerrarCaja.setEnabled(true);
                btnCancelar.setEnabled(true);

                JOptionPane.showMessageDialog(
                        this,
                        valorTexto(
                                respuesta.getMensaje()
                        ),
                        "No se pudo cerrar la caja",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            // ----------------------------------------------------
            // ÉXITO
            // ----------------------------------------------------
            JOptionPane.showMessageDialog(
                    this,
                    construirMensajeExito(
                            saldoFinalReal,
                            diferencia
                    ),
                    "Cierre de caja",
                    JOptionPane.INFORMATION_MESSAGE
            );

            /*
             * TesoreriaServiceImpl actualmente expone únicamente
             * RespuestaOperacion<Void>, por lo que no existe un
             * CierreCaja retornado directamente a la vista.
             *
             * La operación exitosa es suficiente para cerrar
             * esta ventana.
             */
            dispose();

        } catch (RuntimeException ex) {

            btnCerrarCaja.setEnabled(true);
            btnCancelar.setEnabled(true);

            JOptionPane.showMessageDialog(
                    this,
                    obtenerMensajeError(ex),
                    "Error al cerrar la caja",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // CANCELAR
    // ============================================================
    private void cancelar() {

        dispose();
    }

    // ============================================================
    // MENSAJE DE CONFIRMACIÓN
    // ============================================================
    private String construirMensajeConfirmacion(
            BigDecimal saldoFinalReal,
            BigDecimal diferencia) {

        StringBuilder mensaje
                = new StringBuilder();

        mensaje.append(
                "¿Está seguro de cerrar la caja?\n\n"
        );

        mensaje.append(
                "Saldo del sistema: "
        );

        mensaje.append(
                formatearMoneda(
                        saldoSistema
                )
        );

        mensaje.append('\n');

        mensaje.append(
                "Saldo final real: "
        );

        mensaje.append(
                formatearMoneda(
                        saldoFinalReal
                )
        );

        mensaje.append('\n');

        mensaje.append(
                "Diferencia: "
        );

        mensaje.append(
                formatearDiferencia(
                        diferencia
                )
        );

        return mensaje.toString();
    }

    // ============================================================
    // MENSAJE DE ÉXITO
    // ============================================================
    private String construirMensajeExito(
            BigDecimal saldoFinalReal,
            BigDecimal diferencia) {

        StringBuilder mensaje
                = new StringBuilder();

        mensaje.append(
                "La caja fue cerrada correctamente.\n\n"
        );

        mensaje.append(
                "Saldo del sistema: "
        );

        mensaje.append(
                formatearMoneda(
                        saldoSistema
                )
        );

        mensaje.append('\n');

        mensaje.append(
                "Saldo final real: "
        );

        mensaje.append(
                formatearMoneda(
                        saldoFinalReal
                )
        );

        mensaje.append('\n');

        mensaje.append(
                "Diferencia: "
        );

        mensaje.append(
                formatearDiferencia(
                        diferencia
                )
        );

        return mensaje.toString();
    }

    // ============================================================
    // FORMATO MONETARIO
    // ============================================================
    private String formatearMoneda(
            BigDecimal valor) {

        BigDecimal monto
                = normalizarMonto(
                        valor
                );

        return "S/ "
                + FORMATO_MONEDA.format(
                        monto
                );
    }

    private String formatearDiferencia(
            BigDecimal diferencia) {

        BigDecimal valor
                = normalizarMonto(
                        diferencia
                );

        if (valor.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            return "-S/ "
                    + FORMATO_MONEDA.format(
                            valor.abs()
                    );
        }

        return "S/ "
                + FORMATO_MONEDA.format(
                        valor
                );
    }

    private BigDecimal normalizarMonto(
            BigDecimal valor) {

        if (valor == null) {

            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        return valor.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    // ============================================================
    // USUARIO
    // ============================================================
    private String obtenerNombreUsuarioActual() {

        try {

            return valorTexto(
                    SesionUsuario.actual()
                            .getNombreCompleto()
            );

        } catch (RuntimeException ex) {

            return "Usuario actual";
        }
    }

    // ============================================================
    // TEXTO
    // ============================================================
    private String valorTexto(
            String texto) {

        if (texto == null
                || texto.isBlank()) {

            return "-";
        }

        return texto;
    }

    // ============================================================
    // ERROR
    // ============================================================
    private String obtenerMensajeError(
            RuntimeException ex) {

        if (ex == null
                || ex.getMessage() == null
                || ex.getMessage().isBlank()) {

            return "Ocurrió un error inesperado.";
        }

        return ex.getMessage();
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
        pnlCajaAbierta = new javax.swing.JPanel();
        lblCaja = new javax.swing.JLabel();
        lblNombreCaja = new javax.swing.JLabel();
        lblEstado = new javax.swing.JLabel();
        lblEstadoCaja = new javax.swing.JLabel();
        lblUsuarioPnlCajaAbierta = new javax.swing.JLabel();
        lblNombreApellidoUsuarioPnlCajaAbierta = new javax.swing.JLabel();
        lblApertura = new javax.swing.JLabel();
        lblFechaHoraApertura = new javax.swing.JLabel();
        pnlSaldoDelSistema = new javax.swing.JPanel();
        lblSaldoDelSistema = new javax.swing.JLabel();
        lblValorSaldoDelSistema = new javax.swing.JLabel();
        pnlArqueoCaja = new javax.swing.JPanel();
        lblSaldoFinalReal = new javax.swing.JLabel();
        txtSaldoFinalReal = new javax.swing.JTextField();
        txtUnidadMoneda = new javax.swing.JTextField();
        txtResumenSaldoDelSistema = new javax.swing.JTextField();
        txtResumenValorSaldoDelSistema = new javax.swing.JTextField();
        txtResumenSaldoFinalReal = new javax.swing.JTextField();
        txtResumenDiferencia = new javax.swing.JTextField();
        txtResumenValorSaldoFinalReal = new javax.swing.JTextField();
        txtResumenValorDiferencia = new javax.swing.JTextField();
        pnlAdvertencia = new javax.swing.JPanel();
        lblDiferenciaDeCaja = new javax.swing.JLabel();
        lblMensajeAdvertencia = new javax.swing.JLabel();
        pnlBotones = new javax.swing.JPanel();
        btnCerrarCaja = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblMovimientosDeCaja.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblMovimientosDeCaja.setText("CIERRE DE CAJA");

        lblConsultaHistorialMovimientosCajas.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblConsultaHistorialMovimientosCajas.setText("Cierre y arqueo de la caja actualmente abierta ");

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
                .addGap(276, 276, 276)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblNombreApellidoUsuario))
                    .addGroup(pnlSuperiorLayout.createSequentialGroup()
                        .addComponent(lblFechaActual)
                        .addGap(18, 18, 18)
                        .addComponent(lblHoraActual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(12, 12, 12))
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
                            .addComponent(lblConsultaHistorialMovimientosCajas, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFechaActual)
                            .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblMovimientosDeCaja, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlCajaAbierta.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. CAJA ABIERTA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblCaja.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCaja.setText("CAJA");

        lblNombreCaja.setText("Caja Principal");

        lblEstado.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblEstado.setText("ESTADO");

        lblEstadoCaja.setText("ABIERTA");

        lblUsuarioPnlCajaAbierta.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblUsuarioPnlCajaAbierta.setText("USUARIO");

        lblNombreApellidoUsuarioPnlCajaAbierta.setText("Nombre Apellido");

        lblApertura.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblApertura.setText("APERTURA");

        lblFechaHoraApertura.setText("21/08/2026 08:15");

        pnlSaldoDelSistema.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblSaldoDelSistema.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblSaldoDelSistema.setText("SALDO DEL SISTEMA");

        lblValorSaldoDelSistema.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorSaldoDelSistema.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblValorSaldoDelSistema.setText("S/ 2,060.00");

        javax.swing.GroupLayout pnlSaldoDelSistemaLayout = new javax.swing.GroupLayout(pnlSaldoDelSistema);
        pnlSaldoDelSistema.setLayout(pnlSaldoDelSistemaLayout);
        pnlSaldoDelSistemaLayout.setHorizontalGroup(
            pnlSaldoDelSistemaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSaldoDelSistemaLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblSaldoDelSistema)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorSaldoDelSistema, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlSaldoDelSistemaLayout.setVerticalGroup(
            pnlSaldoDelSistemaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSaldoDelSistemaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSaldoDelSistemaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSaldoDelSistema, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorSaldoDelSistema, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlCajaAbiertaLayout = new javax.swing.GroupLayout(pnlCajaAbierta);
        pnlCajaAbierta.setLayout(pnlCajaAbiertaLayout);
        pnlCajaAbiertaLayout.setHorizontalGroup(
            pnlCajaAbiertaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCajaAbiertaLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(pnlCajaAbiertaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblNombreCaja, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCaja, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(pnlCajaAbiertaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblEstadoCaja, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                    .addComponent(lblEstado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(pnlCajaAbiertaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblNombreApellidoUsuarioPnlCajaAbierta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblUsuarioPnlCajaAbierta, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlCajaAbiertaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblFechaHoraApertura, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblApertura, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(pnlSaldoDelSistema, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlCajaAbiertaLayout.setVerticalGroup(
            pnlCajaAbiertaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCajaAbiertaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCajaAbiertaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlCajaAbiertaLayout.createSequentialGroup()
                        .addComponent(lblApertura)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblFechaHoraApertura))
                    .addGroup(pnlCajaAbiertaLayout.createSequentialGroup()
                        .addComponent(lblUsuarioPnlCajaAbierta)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblNombreApellidoUsuarioPnlCajaAbierta))
                    .addGroup(pnlCajaAbiertaLayout.createSequentialGroup()
                        .addGroup(pnlCajaAbiertaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblCaja)
                            .addComponent(lblEstado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlCajaAbiertaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNombreCaja)
                            .addComponent(lblEstadoCaja))))
                .addContainerGap(12, Short.MAX_VALUE))
            .addGroup(pnlCajaAbiertaLayout.createSequentialGroup()
                .addComponent(pnlSaldoDelSistema, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pnlArqueoCaja.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. ARQUEO DE CAJA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblSaldoFinalReal.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblSaldoFinalReal.setText("SALDO FINAL REAL");

        txtSaldoFinalReal.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        txtSaldoFinalReal.setText("2,050.00");

        txtUnidadMoneda.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        txtUnidadMoneda.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtUnidadMoneda.setText("S/");

        txtResumenSaldoDelSistema.setEditable(false);
        txtResumenSaldoDelSistema.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtResumenSaldoDelSistema.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txtResumenSaldoDelSistema.setText("Saldo del Sistema");

        txtResumenValorSaldoDelSistema.setEditable(false);
        txtResumenValorSaldoDelSistema.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        txtResumenValorSaldoDelSistema.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtResumenValorSaldoDelSistema.setText("S/ 2,060.00");

        txtResumenSaldoFinalReal.setEditable(false);
        txtResumenSaldoFinalReal.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtResumenSaldoFinalReal.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txtResumenSaldoFinalReal.setText("Saldo Final Real");

        txtResumenDiferencia.setEditable(false);
        txtResumenDiferencia.setFont(new java.awt.Font("Consolas", 1, 12)); // NOI18N
        txtResumenDiferencia.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txtResumenDiferencia.setText("Diferencia");

        txtResumenValorSaldoFinalReal.setEditable(false);
        txtResumenValorSaldoFinalReal.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        txtResumenValorSaldoFinalReal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtResumenValorSaldoFinalReal.setText("S/ 2,050.00");

        txtResumenValorDiferencia.setEditable(false);
        txtResumenValorDiferencia.setFont(new java.awt.Font("Consolas", 1, 14)); // NOI18N
        txtResumenValorDiferencia.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtResumenValorDiferencia.setText("-S/ 10.00");

        pnlAdvertencia.setBackground(new java.awt.Color(102, 0, 0));
        pnlAdvertencia.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(255, 51, 51))); // NOI18N
        pnlAdvertencia.setForeground(new java.awt.Color(255, 51, 51));

        lblDiferenciaDeCaja.setFont(new java.awt.Font("Consolas", 1, 14)); // NOI18N
        lblDiferenciaDeCaja.setForeground(new java.awt.Color(204, 0, 0));
        lblDiferenciaDeCaja.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblDiferenciaDeCaja.setText("️ DIFERENCIA DE CAJA");

        lblMensajeAdvertencia.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        lblMensajeAdvertencia.setText("El saldo real es menor que el saldo del sistema. ");

        javax.swing.GroupLayout pnlAdvertenciaLayout = new javax.swing.GroupLayout(pnlAdvertencia);
        pnlAdvertencia.setLayout(pnlAdvertenciaLayout);
        pnlAdvertenciaLayout.setHorizontalGroup(
            pnlAdvertenciaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdvertenciaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblDiferenciaDeCaja)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMensajeAdvertencia, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlAdvertenciaLayout.setVerticalGroup(
            pnlAdvertenciaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdvertenciaLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlAdvertenciaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDiferenciaDeCaja)
                    .addComponent(lblMensajeAdvertencia))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlArqueoCajaLayout = new javax.swing.GroupLayout(pnlArqueoCaja);
        pnlArqueoCaja.setLayout(pnlArqueoCajaLayout);
        pnlArqueoCajaLayout.setHorizontalGroup(
            pnlArqueoCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlArqueoCajaLayout.createSequentialGroup()
                .addGroup(pnlArqueoCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlArqueoCajaLayout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(lblSaldoFinalReal))
                    .addGroup(pnlArqueoCajaLayout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(txtUnidadMoneda, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSaldoFinalReal, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(pnlArqueoCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlArqueoCajaLayout.createSequentialGroup()
                        .addComponent(txtResumenSaldoDelSistema, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtResumenValorSaldoDelSistema))
                    .addGroup(pnlArqueoCajaLayout.createSequentialGroup()
                        .addComponent(txtResumenDiferencia, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtResumenValorDiferencia))
                    .addGroup(pnlArqueoCajaLayout.createSequentialGroup()
                        .addComponent(txtResumenSaldoFinalReal, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtResumenValorSaldoFinalReal))
                    .addComponent(pnlAdvertencia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlArqueoCajaLayout.setVerticalGroup(
            pnlArqueoCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlArqueoCajaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlArqueoCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlArqueoCajaLayout.createSequentialGroup()
                        .addGroup(pnlArqueoCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtResumenSaldoDelSistema, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtResumenValorSaldoDelSistema, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlArqueoCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtResumenSaldoFinalReal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtResumenValorSaldoFinalReal, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlArqueoCajaLayout.createSequentialGroup()
                        .addComponent(lblSaldoFinalReal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlArqueoCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSaldoFinalReal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtUnidadMoneda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlArqueoCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtResumenDiferencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtResumenValorDiferencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAdvertencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlBotones.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        btnCerrarCaja.setBackground(new java.awt.Color(153, 51, 0));
        btnCerrarCaja.setText("CERRAR CAJA");
        btnCerrarCaja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarCajaActionPerformed(evt);
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
                .addComponent(btnCerrarCaja)
                .addContainerGap())
        );
        pnlBotonesLayout.setVerticalGroup(
            pnlBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBotonesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCerrarCaja)
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
                    .addComponent(pnlCajaAbierta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlArqueoCaja, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlBotones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlCajaAbierta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlArqueoCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlBotones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCerrarCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarCajaActionPerformed
        // TODO add your handling code here:
        cerrarCaja();
    }//GEN-LAST:event_btnCerrarCajaActionPerformed

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
            java.util.logging.Logger.getLogger(FrmCierreCaja.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmCierreCaja.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmCierreCaja.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmCierreCaja.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmCierreCaja dialog = new FrmCierreCaja(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCerrarCaja;
    private javax.swing.JLabel lblApertura;
    private javax.swing.JLabel lblCaja;
    private javax.swing.JLabel lblConsultaHistorialMovimientosCajas;
    private javax.swing.JLabel lblDiferenciaDeCaja;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblEstadoCaja;
    private javax.swing.JLabel lblFechaActual;
    private javax.swing.JLabel lblFechaHoraApertura;
    private javax.swing.JLabel lblHoraActual;
    private javax.swing.JLabel lblMensajeAdvertencia;
    private javax.swing.JLabel lblMovimientosDeCaja;
    private javax.swing.JLabel lblNombreApellidoUsuario;
    private javax.swing.JLabel lblNombreApellidoUsuarioPnlCajaAbierta;
    private javax.swing.JLabel lblNombreCaja;
    private javax.swing.JLabel lblSaldoDelSistema;
    private javax.swing.JLabel lblSaldoFinalReal;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblUsuarioPnlCajaAbierta;
    private javax.swing.JLabel lblValorSaldoDelSistema;
    private javax.swing.JPanel pnlAdvertencia;
    private javax.swing.JPanel pnlArqueoCaja;
    private javax.swing.JPanel pnlBotones;
    private javax.swing.JPanel pnlCajaAbierta;
    private javax.swing.JPanel pnlSaldoDelSistema;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JTextField txtResumenDiferencia;
    private javax.swing.JTextField txtResumenSaldoDelSistema;
    private javax.swing.JTextField txtResumenSaldoFinalReal;
    private javax.swing.JTextField txtResumenValorDiferencia;
    private javax.swing.JTextField txtResumenValorSaldoDelSistema;
    private javax.swing.JTextField txtResumenValorSaldoFinalReal;
    private javax.swing.JTextField txtSaldoFinalReal;
    private javax.swing.JTextField txtUnidadMoneda;
    // End of variables declaration//GEN-END:variables
}
