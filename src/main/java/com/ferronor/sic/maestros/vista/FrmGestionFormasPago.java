package com.ferronor.sic.maestros.vista;

import com.ferronor.sic.maestros.logica.FormaPagoService;
import com.ferronor.sic.maestros.modelo.FormaPago;
import com.ferronor.sic.shared.FrmBase;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FrmGestionFormasPago extends FrmBase {

    private final FormaPagoService formaPagoService =
            ServiceFactory.formaPagoService();

    private FormaPago formaPagoSeleccionada;

    private final DefaultTableModel modeloTabla =
            new DefaultTableModel(
                    new Object[]{
                        "ID",
                        "NOMBRE",
                        "ES CRÉDITO"
                    },
                    0
            ) {
        @Override
        public boolean isCellEditable(
                int row,
                int column
        ) {
            return false;
        }
    };

    private final JTable tblFormasPago =
            new JTable(modeloTabla);

    private final JTextField txtBuscar =
            new JTextField();

    private final JTextField txtNombre =
            new JTextField();

    private final JCheckBox chkEsCredito =
            new JCheckBox(
                    "Sí, esta forma de pago permite crédito"
            );

    private final JLabel lblModo =
            new JLabel(
                    "NUEVA FORMA DE PAGO"
            );

    private final JLabel lblMensaje =
            new JLabel(
                    "Complete los datos y pulse Guardar."
            );

    private final JLabel lblUsuario =
            new JLabel();

    private final JLabel lblFechaHora =
            new JLabel();

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm"
            );

    private JButton btnGuardar;

    public FrmGestionFormasPago() {

        super("MAESTROS");

        construirInterfaz();
        cargarFormasPago();
        prepararNuevaFormaPago();
        actualizarCabecera();
    }

    private void construirInterfaz() {

        setTitle(
                "Gestión de Formas de Pago"
        );

        setMinimumSize(
                new Dimension(
                        900,
                        620
                )
        );

        setSize(
                1000,
                700
        );

        setLocationRelativeTo(null);

        setDefaultCloseOperation(
                javax.swing.WindowConstants.DISPOSE_ON_CLOSE
        );

        JPanel contenedor =
                new JPanel(
                        new BorderLayout(
                                12,
                                12
                        )
                );

        contenedor.setBorder(
                BorderFactory.createEmptyBorder(
                        12,
                        14,
                        14,
                        14
                )
        );

        setContentPane(
                contenedor
        );

        contenedor.add(
                construirCabecera(),
                BorderLayout.NORTH
        );

        contenedor.add(
                construirCentro(),
                BorderLayout.CENTER
        );

        contenedor.add(
                construirAcciones(),
                BorderLayout.SOUTH
        );

        configurarTabla();
    }

    private JPanel construirCabecera() {

        JPanel panel =
                new JPanel(
                        new BorderLayout(
                                15,
                                4
                        )
                );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        6,
                        8,
                        8,
                        8
                )
        );

        JPanel izquierda =
                new JPanel(
                        new GridLayout(
                                2,
                                1,
                                0,
                                2
                        )
                );

        JLabel titulo =
                new JLabel(
                        "FORMAS DE PAGO"
                );

        titulo.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        24
                )
        );

        JLabel subtitulo =
                new JLabel(
                        "Gestión y mantenimiento de las formas de pago"
                );

        subtitulo.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        10
                )
        );

        izquierda.add(
                titulo
        );

        izquierda.add(
                subtitulo
        );

        JPanel derecha =
                new JPanel(
                        new GridLayout(
                                2,
                                1,
                                0,
                                2
                        )
                );

        lblUsuario.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        lblFechaHora.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        lblUsuario.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        lblFechaHora.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        10
                )
        );

        derecha.add(
                lblUsuario
        );

        derecha.add(
                lblFechaHora
        );

        panel.add(
                izquierda,
                BorderLayout.WEST
        );

        panel.add(
                derecha,
                BorderLayout.EAST
        );

        return panel;
    }

    private JPanel construirCentro() {

        JPanel centro =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        centro.add(
                construirConsulta(),
                BorderLayout.NORTH
        );

        centro.add(
                construirTabla(),
                BorderLayout.CENTER
        );

        centro.add(
                construirGestion(),
                BorderLayout.SOUTH
        );

        return centro;
    }

    private JPanel construirConsulta() {

        JPanel panel =
                crearPanelSeccion(
                        "01. CONSULTA DE FORMAS DE PAGO"
                );

        panel.setLayout(
                new BorderLayout(
                        10,
                        8
                )
        );

        JPanel fila =
                new JPanel(
                        new BorderLayout(
                                8,
                                0
                        )
                );

        JLabel lblBuscar =
                crearEtiqueta(
                        "BUSCAR FORMA DE PAGO"
                );

        txtBuscar.setPreferredSize(
                new Dimension(
                        350,
                        30
                )
        );

        JButton btnBuscar =
                crearBoton(
                        "Buscar"
                );

        JButton btnMostrarTodas =
                crearBoton(
                        "Mostrar todas"
                );

        JPanel botones =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                5,
                                0
                        )
                );

        botones.add(
                btnBuscar
        );

        botones.add(
                btnMostrarTodas
        );

        fila.add(
                lblBuscar,
                BorderLayout.WEST
        );

        fila.add(
                txtBuscar,
                BorderLayout.CENTER
        );

        fila.add(
                botones,
                BorderLayout.EAST
        );

        panel.add(
                fila,
                BorderLayout.CENTER
        );

        btnBuscar.addActionListener(
                e -> buscarFormasPago()
        );

        btnMostrarTodas.addActionListener(
                e -> {

                    txtBuscar.setText("");

                    cargarFormasPago();

                    prepararNuevaFormaPago();
                }
        );

        txtBuscar.addActionListener(
                e -> buscarFormasPago()
        );

        return panel;
    }

    private JScrollPane construirTabla() {

        tblFormasPago.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblFormasPago.setRowHeight(
                27
        );

        tblFormasPago.setAutoCreateRowSorter(
                true
        );

        tblFormasPago.setFillsViewportHeight(
                true
        );

        tblFormasPago.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        tblFormasPago.getTableHeader()
                .setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                11
                        )
                );

        tblFormasPago.getTableHeader()
                .setPreferredSize(
                        new Dimension(
                                0,
                                30
                        )
                );

        DefaultTableCellRenderer centrado =
                new DefaultTableCellRenderer();

        centrado.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        tblFormasPago.getColumnModel()
                .getColumn(0)
                .setCellRenderer(
                        centrado
                );

        tblFormasPago.getColumnModel()
                .getColumn(2)
                .setCellRenderer(
                        centrado
                );

        tblFormasPago.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(
                        80
                );

        tblFormasPago.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(
                        350
                );

        tblFormasPago.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(
                        130
                );

        tblFormasPago.getSelectionModel()
                .addListSelectionListener(
                        e -> {

                            if (!e.getValueIsAdjusting()) {
                                cargarFormaPagoSeleccionada();
                            }
                        }
                );

        tblFormasPago.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent e
                    ) {

                        if (e.getClickCount() == 2
                                && tblFormasPago
                                .getSelectedRow() >= 0) {

                            cargarFormaPagoSeleccionada();
                        }
                    }
                }
        );

        return new JScrollPane(
                tblFormasPago
        );
    }

    private JPanel construirGestion() {

        JPanel panel =
                crearPanelSeccion(
                        "02. DATOS DE LA FORMA DE PAGO"
                );

        panel.setLayout(
                new GridBagLayout()
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(
                        5,
                        6,
                        5,
                        6
                );

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        gbc.anchor =
                GridBagConstraints.WEST;

        // =====================================================
        // FILA 0 - NOMBRE
        // =====================================================

        agregarCampo(
                panel,
                gbc,
                0,
                0,
                "NOMBRE",
                txtNombre,
                5
        );

        // =====================================================
        // FILA 1 - CRÉDITO
        // =====================================================

        JLabel lblCredito =
                crearEtiqueta(
                        "TIPO DE PAGO"
                );

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;

        panel.add(
                lblCredito,
                gbc
        );

        gbc.gridx = 1;
        gbc.gridwidth = 5;
        gbc.weightx = 1;

        chkEsCredito.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        panel.add(
                chkEsCredito,
                gbc
        );

        // =====================================================
        // FILA 2 - MODO / ESTADO
        // =====================================================

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;

        JLabel lblModoTitulo =
                crearEtiqueta(
                        "MODO"
                );

        panel.add(
                lblModoTitulo,
                gbc
        );

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.3;

        lblModo.setFont(
                new Font(
                        "Consolas",
                        Font.BOLD,
                        11
                )
        );

        panel.add(
                lblModo,
                gbc
        );

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;

        JLabel lblEstadoTitulo =
                crearEtiqueta(
                        "ESTADO"
                );

        panel.add(
                lblEstadoTitulo,
                gbc
        );

        gbc.gridx = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 0.3;

        JLabel lblEstado =
                new JLabel(
                        "DISPONIBLE"
                );

        lblEstado.setFont(
                new Font(
                        "Consolas",
                        Font.BOLD,
                        11
                )
        );

        panel.add(
                lblEstado,
                gbc
        );

        // =====================================================
        // FILA 3 - MENSAJE
        // =====================================================

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;

        JLabel lblMensajeTitulo =
                crearEtiqueta(
                        "MENSAJE"
                );

        panel.add(
                lblMensajeTitulo,
                gbc
        );

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 5;
        gbc.weightx = 1;

        lblMensaje.setFont(
                new Font(
                        "Segoe UI",
                        Font.ITALIC,
                        11
                )
        );

        panel.add(
                lblMensaje,
                gbc
        );

        return panel;
    }

    private JPanel construirAcciones() {

        JPanel panel =
                crearPanelSeccion(
                        "03. ACCIONES"
                );

        panel.setLayout(
                new FlowLayout(
                        FlowLayout.CENTER,
                        8,
                        7
                )
        );

        JButton btnNueva =
                crearBoton(
                        "Nueva forma de pago"
                );

        btnGuardar =
                crearBoton(
                        "Guardar"
                );

        JButton btnActualizar =
                crearBoton(
                        "Actualizar"
                );

        JButton btnCerrar =
                crearBoton(
                        "Cerrar"
                );

        panel.add(
                btnNueva
        );

        panel.add(
                btnGuardar
        );

        panel.add(
                btnActualizar
        );

        panel.add(
                btnCerrar
        );

        btnNueva.addActionListener(
                e -> prepararNuevaFormaPago()
        );

        btnGuardar.addActionListener(
                e -> guardarFormaPago()
        );

        btnActualizar.addActionListener(
                e -> {

                    cargarFormasPago();

                    prepararNuevaFormaPago();
                }
        );

        btnCerrar.addActionListener(
                e -> dispose()
        );

        return panel;
    }

    private JPanel crearPanelSeccion(
            String titulo
    ) {

        JPanel panel =
                new JPanel();

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(
                                new Color(
                                        190,
                                        190,
                                        190
                                )
                        ),
                        titulo
                )
        );

        return panel;
    }

    private JLabel crearEtiqueta(
            String texto
    ) {

        JLabel label =
                new JLabel(
                        texto
                );

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        10
                )
        );

        return label;
    }

    private JButton crearBoton(
            String texto
    ) {

        JButton boton =
                new JButton(
                        texto
                );

        boton.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        boton.setFocusPainted(
                false
        );

        boton.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        boton.setMargin(
                new Insets(
                        6,
                        14,
                        6,
                        14
                )
        );

        return boton;
    }

    private void agregarCampo(
            JPanel panel,
            GridBagConstraints gbc,
            int columna,
            int fila,
            String texto,
            Component componente
    ) {

        agregarCampo(
                panel,
                gbc,
                columna,
                fila,
                texto,
                componente,
                1
        );
    }

    private void agregarCampo(
            JPanel panel,
            GridBagConstraints gbc,
            int columna,
            int fila,
            String texto,
            Component componente,
            int ancho
    ) {

        JLabel etiqueta =
                crearEtiqueta(
                        texto
                );

        gbc.gridx =
                columna;

        gbc.gridy =
                fila;

        gbc.gridwidth =
                1;

        gbc.weightx =
                0;

        panel.add(
                etiqueta,
                gbc
        );

        gbc.gridx =
                columna + 1;

        gbc.gridwidth =
                ancho;

        gbc.weightx =
                1;

        componente.setPreferredSize(
                new Dimension(
                        180,
                        29
                )
        );

        panel.add(
                componente,
                gbc
        );
    }

    private void configurarTabla() {

        txtNombre.setPreferredSize(
                new Dimension(
                        250,
                        29
                )
        );
    }

    private void actualizarCabecera() {

        if (!SesionUsuario.haySesion()) {
            return;
        }

        lblUsuario.setText(
                "Usuario: "
                + SesionUsuario.actual()
                        .getNombreCompleto()
        );

        lblFechaHora.setText(
                LocalDateTime.now()
                        .format(
                                formatoFecha
                        )
        );
    }

    private void cargarFormasPago() {

        try {

            List<FormaPago> formasPago =
                    formaPagoService.listar();

            cargarTabla(
                    formasPago
            );

        } catch (Exception ex) {

            mostrarError(
                    ex.getMessage() == null
                            ? "No se pudieron cargar las formas de pago."
                            : ex.getMessage()
            );
        }
    }

    private void buscarFormasPago() {

        String texto =
                txtBuscar.getText() == null
                        ? ""
                        : txtBuscar.getText()
                                .trim();

        if (texto.isBlank()) {

            cargarFormasPago();

            return;
        }

        try {

            /*
             * FormaPagoService no posee una búsqueda parcial.
             * Por lo tanto, se utiliza listar() y se filtra
             * únicamente en la vista.
             */

            List<FormaPago> formasEncontradas =
                    new ArrayList<>();

            for (FormaPago formaPago :
                    formaPagoService.listar()) {

                if (formaPago.getNombre() != null
                        && formaPago.getNombre()
                        .toLowerCase()
                        .contains(
                                texto.toLowerCase()
                        )) {

                    formasEncontradas.add(
                            formaPago
                    );
                }
            }

            cargarTabla(
                    formasEncontradas
            );

            if (formasEncontradas.isEmpty()) {

                mostrarInformacion(
                        "No se encontraron formas de pago."
                );
            }

        } catch (Exception ex) {

            mostrarError(
                    ex.getMessage() == null
                            ? "No se pudo realizar la búsqueda."
                            : ex.getMessage()
            );
        }
    }

    private void cargarTabla(
            List<FormaPago> formasPago
    ) {

        modeloTabla.setRowCount(
                0
        );

        if (formasPago == null) {
            return;
        }

        for (FormaPago formaPago :
                formasPago) {

            modeloTabla.addRow(
                    new Object[]{
                        formaPago.getIdFormaPago(),
                        formaPago.getNombre(),
                        formaPago.isEsCredito()
                                ? "SÍ"
                                : "NO"
                    }
            );
        }
    }

    private void cargarFormaPagoSeleccionada() {

        int filaVista =
                tblFormasPago
                        .getSelectedRow();

        if (filaVista < 0) {
            return;
        }

        int filaModelo =
                tblFormasPago
                        .convertRowIndexToModel(
                                filaVista
                        );

        int idFormaPago =
                ((Number)
                        modeloTabla.getValueAt(
                                filaModelo,
                                0
                        )).intValue();

        try {

            FormaPago formaPago =
                    formaPagoService
                            .buscarPorId(
                                    idFormaPago
                            );

            if (formaPago == null) {

                mostrarError(
                        "No se pudo recuperar la forma de pago seleccionada."
                );

                return;
            }

            formaPagoSeleccionada =
                    formaPago;

            txtNombre.setText(
                    formaPago.getNombre()
            );

            chkEsCredito.setSelected(
                    formaPago.isEsCredito()
            );

            lblModo.setText(
                    "EDITAR FORMA DE PAGO"
            );

            lblMensaje.setText(
                    "Modifique los datos y pulse Guardar."
            );

            btnGuardar.setEnabled(
                    true
            );

        } catch (Exception ex) {

            mostrarError(
                    ex.getMessage() == null
                            ? "No se pudo cargar la forma de pago."
                            : ex.getMessage()
            );
        }
    }

    private void prepararNuevaFormaPago() {

        formaPagoSeleccionada =
                null;

        tblFormasPago.clearSelection();

        txtNombre.setText(
                ""
        );

        chkEsCredito.setSelected(
                false
        );

        lblModo.setText(
                "NUEVA FORMA DE PAGO"
        );

        lblMensaje.setText(
                "Complete los datos y pulse Guardar."
        );

        btnGuardar.setEnabled(
                true
        );

        txtNombre.setEditable(
                true
        );

        chkEsCredito.setEnabled(
                true
        );

        txtNombre.requestFocus();
    }

    private void guardarFormaPago() {

        String nombre =
                txtNombre.getText()
                        .trim();

        boolean esCredito =
                chkEsCredito.isSelected();

        // =====================================================
        // VALIDACIONES BÁSICAS
        // =====================================================

        if (nombre.isBlank()) {

            mostrarAviso(
                    "El nombre de la forma de pago es obligatorio."
            );

            txtNombre.requestFocus();

            return;
        }

        if (nombre.length() > 30) {

            mostrarAviso(
                    "El nombre no puede superar los 30 caracteres."
            );

            txtNombre.requestFocus();

            return;
        }

        boolean nueva =
                formaPagoSeleccionada
                        == null;

        RespuestaOperacion<Void> respuesta;

        if (nueva) {

            FormaPago formaPago =
                    new FormaPago(
                            nombre,
                            esCredito
                    );

            respuesta =
                    formaPagoService
                            .registrar(
                                    formaPago
                            );

        } else {

            formaPagoSeleccionada
                    .setNombre(
                            nombre
                    );

            formaPagoSeleccionada
                    .setEsCredito(
                            esCredito
                    );

            respuesta =
                    formaPagoService
                            .actualizar(
                                    formaPagoSeleccionada
                            );
        }

        if (!respuesta.isExito()) {

            mostrarError(
                    respuesta.getMensaje()
            );

            return;
        }

        mostrarInformacion(
                nueva
                        ? "Forma de pago registrada correctamente."
                        : "Forma de pago actualizada correctamente."
        );

        cargarFormasPago();

        prepararNuevaFormaPago();
    }

    private void mostrarAviso(
            String mensaje
    ) {

        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Validación",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void mostrarError(
            String mensaje
    ) {

        JOptionPane.showMessageDialog(
                this,
                mensaje == null
                        ? "Ocurrió un error."
                        : mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void mostrarInformacion(
            String mensaje
    ) {

        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Información",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(
            String[] args
    ) {

        javax.swing.SwingUtilities.invokeLater(
                () -> {

                    try {

                        javax.swing.UIManager
                                .setLookAndFeel(
                                        javax.swing.UIManager
                                                .getSystemLookAndFeelClassName()
                                );

                    } catch (Exception ignored) {
                    }

                    FrmGestionFormasPago frm =
                            new FrmGestionFormasPago();

                    frm.setVisible(
                            true
                    );
                }
        );
    }
}
