package com.ferronor.sic.maestros.vista;

import com.ferronor.sic.maestros.logica.ProveedorService;
import com.ferronor.sic.maestros.modelo.Proveedor;
import com.ferronor.sic.shared.FrmBase;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FrmGestionProveedores extends FrmBase {

    private final ProveedorService proveedorService
            = ServiceFactory.proveedorService();

    private Proveedor proveedorSeleccionado;

    private final DefaultTableModel modeloTabla
            = new DefaultTableModel(
                    new Object[]{
                        "ID",
                        "RUC",
                        "RAZÓN SOCIAL",
                        "DIRECCIÓN",
                        "TELÉFONO",
                        "CONTACTO",
                        "ESTADO"
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

    private final JTable tblProveedores
            = new JTable(modeloTabla);

    private final JTextField txtBuscar
            = new JTextField();

    private final JTextField txtRuc
            = new JTextField();

    private final JTextField txtRazonSocial
            = new JTextField();

    private final JTextField txtDireccion
            = new JTextField();

    private final JTextField txtTelefono
            = new JTextField();

    private final JTextField txtContacto
            = new JTextField();

    private final JTextField txtEstado
            = new JTextField();

    private final JLabel lblModo
            = new JLabel("NUEVO PROVEEDOR");

    private final JLabel lblMensaje
            = new JLabel(
                    "Complete los datos y pulse Guardar."
            );

    private final JLabel lblUsuario
            = new JLabel();

    private final JLabel lblFechaHora
            = new JLabel();

    private final DateTimeFormatter formatoFecha
            = DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm"
            );

    private JButton btnGuardar;
    private JButton btnActivar;
    private JButton btnDesactivar;

    public FrmGestionProveedores() {

        super("MAESTROS");

        construirInterfaz();
        cargarProveedores();
        prepararNuevoProveedor();
        actualizarCabecera();
    }

    private void construirInterfaz() {

        setTitle("Gestión de Proveedores");

        setMinimumSize(
                new Dimension(1050, 680)
        );

        setSize(
                1180,
                760
        );

        setLocationRelativeTo(null);

        setDefaultCloseOperation(
                javax.swing.WindowConstants.DISPOSE_ON_CLOSE
        );

        JPanel contenedor
                = new JPanel(
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

        setContentPane(contenedor);

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

        JPanel panel
                = new JPanel(
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

        JPanel izquierda
                = new JPanel();

        izquierda.setLayout(
                new javax.swing.BoxLayout(
                        izquierda,
                        javax.swing.BoxLayout.Y_AXIS
                )
        );

        JLabel titulo
                = new JLabel(
                        "PROVEEDORES"
                );

        titulo.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        24
                )
        );

        JLabel subtitulo
                = new JLabel(
                        "Gestión y mantenimiento de proveedores"
                );

        subtitulo.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        10
                )
        );

        izquierda.add(titulo);
        izquierda.add(subtitulo);

        JPanel derecha
                = new JPanel(
                        new java.awt.GridLayout(
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

        derecha.add(lblUsuario);
        derecha.add(lblFechaHora);

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

        JPanel centro
                = new JPanel(
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

        JPanel panel
                = crearPanelSeccion(
                        "01. CONSULTA DE PROVEEDORES"
                );

        panel.setLayout(
                new BorderLayout(
                        10,
                        8
                )
        );

        JPanel fila
                = new JPanel(
                        new BorderLayout(
                                8,
                                0
                        )
                );

        JLabel lblBuscar
                = crearEtiqueta(
                        "BUSCAR POR RUC"
                );

        txtBuscar.setPreferredSize(
                new Dimension(
                        350,
                        30
                )
        );

        JButton btnBuscar
                = crearBoton(
                        "Buscar"
                );

        JButton btnMostrarTodos
                = crearBoton(
                        "Mostrar todos"
                );

        JPanel botones
                = new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                5,
                                0
                        )
                );

        botones.add(btnBuscar);
        botones.add(btnMostrarTodos);

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
                e -> buscarProveedorPorRuc()
        );

        btnMostrarTodos.addActionListener(
                e -> {
                    txtBuscar.setText("");
                    cargarProveedores();
                    prepararNuevoProveedor();
                }
        );

        txtBuscar.addActionListener(
                e -> buscarProveedorPorRuc()
        );

        return panel;
    }

    private JScrollPane construirTabla() {

        tblProveedores.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblProveedores.setRowHeight(27);

        tblProveedores.setAutoCreateRowSorter(
                true
        );

        tblProveedores.setFillsViewportHeight(
                true
        );

        tblProveedores.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        tblProveedores.getTableHeader()
                .setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                11
                        )
                );

        tblProveedores.getTableHeader()
                .setPreferredSize(
                        new Dimension(
                                0,
                                30
                        )
                );

        DefaultTableCellRenderer centrado
                = new DefaultTableCellRenderer();

        centrado.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        tblProveedores.getColumnModel()
                .getColumn(0)
                .setCellRenderer(
                        centrado
                );

        tblProveedores.getColumnModel()
                .getColumn(1)
                .setCellRenderer(
                        centrado
                );

        tblProveedores.getColumnModel()
                .getColumn(6)
                .setCellRenderer(
                        centrado
                );

        tblProveedores.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(60);

        tblProveedores.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(110);

        tblProveedores.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(220);

        tblProveedores.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(230);

        tblProveedores.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(110);

        tblProveedores.getColumnModel()
                .getColumn(5)
                .setPreferredWidth(140);

        tblProveedores.getColumnModel()
                .getColumn(6)
                .setPreferredWidth(100);

        tblProveedores.getSelectionModel()
                .addListSelectionListener(
                        e -> {

                            if (!e.getValueIsAdjusting()) {
                                cargarProveedorSeleccionado();
                            }
                        }
                );

        tblProveedores.addMouseListener(
                new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(
                    java.awt.event.MouseEvent e
            ) {

                if (e.getClickCount() == 2
                        && tblProveedores
                                .getSelectedRow() >= 0) {

                    cargarProveedorSeleccionado();
                }
            }
        }
        );

        return new JScrollPane(
                tblProveedores
        );
    }

    private JPanel construirGestion() {

        JPanel panel
                = crearPanelSeccion(
                        "02. DATOS DEL PROVEEDOR"
                );

        panel.setLayout(
                new GridBagLayout()
        );

        GridBagConstraints gbc
                = new GridBagConstraints();

        gbc.insets
                = new Insets(
                        5,
                        6,
                        5,
                        6
                );

        gbc.fill
                = GridBagConstraints.HORIZONTAL;

        gbc.anchor
                = GridBagConstraints.WEST;

        // =====================================================
        // FILA 0
        // =====================================================
        agregarCampo(
                panel,
                gbc,
                0,
                0,
                "RUC",
                txtRuc
        );

        agregarCampo(
                panel,
                gbc,
                2,
                0,
                "RAZÓN SOCIAL",
                txtRazonSocial,
                3
        );

        // =====================================================
        // FILA 1
        // =====================================================
        agregarCampo(
                panel,
                gbc,
                0,
                1,
                "DIRECCIÓN",
                txtDireccion,
                5
        );

        // =====================================================
        // FILA 2
        // =====================================================
        agregarCampo(
                panel,
                gbc,
                0,
                2,
                "TELÉFONO",
                txtTelefono
        );

        agregarCampo(
                panel,
                gbc,
                2,
                2,
                "CONTACTO",
                txtContacto,
                3
        );

        // =====================================================
        // FILA 3
        // =====================================================
        JLabel lblEstado
                = crearEtiqueta(
                        "ESTADO"
                );

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;

        panel.add(
                lblEstado,
                gbc
        );

        txtEstado.setEditable(
                false
        );

        txtEstado.setFocusable(
                false
        );

        txtEstado.setPreferredSize(
                new Dimension(
                        180,
                        29
                )
        );

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;

        panel.add(
                txtEstado,
                gbc
        );

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;

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
        gbc.gridwidth = 3;
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

        JPanel panel
                = crearPanelSeccion(
                        "03. ACCIONES"
                );

        panel.setLayout(
                new FlowLayout(
                        FlowLayout.CENTER,
                        8,
                        7
                )
        );

        JButton btnNuevo
                = crearBoton(
                        "Nuevo proveedor"
                );

        btnGuardar
                = crearBoton(
                        "Guardar"
                );

        btnActivar
                = crearBoton(
                        "Activar"
                );

        btnDesactivar
                = crearBoton(
                        "Desactivar"
                );

        JButton btnActualizar
                = crearBoton(
                        "Actualizar"
                );

        JButton btnCerrar
                = crearBoton(
                        "Cerrar"
                );

        panel.add(btnNuevo);
        panel.add(btnGuardar);
        panel.add(btnActivar);
        panel.add(btnDesactivar);
        panel.add(btnActualizar);
        panel.add(btnCerrar);

        btnNuevo.addActionListener(
                e -> prepararNuevoProveedor()
        );

        btnGuardar.addActionListener(
                e -> guardarProveedor()
        );

        btnActivar.addActionListener(
                e -> activarProveedor()
        );

        btnDesactivar.addActionListener(
                e -> desactivarProveedor()
        );

        btnActualizar.addActionListener(
                e -> {
                    cargarProveedores();
                    prepararNuevoProveedor();
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

        JPanel panel
                = new JPanel();

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

        JLabel label
                = new JLabel(
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

        JButton boton
                = new JButton(
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

        JLabel etiqueta
                = crearEtiqueta(
                        texto
                );

        gbc.gridx
                = columna;

        gbc.gridy
                = fila;

        gbc.gridwidth
                = 1;

        gbc.weightx
                = 0;

        panel.add(
                etiqueta,
                gbc
        );

        gbc.gridx
                = columna + 1;

        gbc.gridwidth
                = ancho;

        gbc.weightx
                = 1;

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
        txtEstado.setText(
                "SIN SELECCIÓN"
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

    private void cargarProveedores() {

        try {

            List<Proveedor> proveedores
                    = proveedorService.listar();

            cargarTabla(
                    proveedores
            );

        } catch (Exception ex) {

            mostrarError(
                    ex.getMessage() == null
                    ? "No se pudieron cargar los proveedores."
                    : ex.getMessage()
            );
        }
    }

    private void cargarTabla(
            List<Proveedor> proveedores
    ) {

        modeloTabla.setRowCount(
                0
        );

        if (proveedores == null) {
            return;
        }

        for (Proveedor proveedor
                : proveedores) {

            modeloTabla.addRow(
                    new Object[]{
                        proveedor.getIdProveedor(),
                        proveedor.getRuc(),
                        proveedor.getRazonSocial(),
                        proveedor.getDireccion(),
                        proveedor.getTelefono(),
                        proveedor.getContacto(),
                        proveedor.isActivo()
                        ? "ACTIVO"
                        : "INACTIVO"
                    }
            );
        }
    }

    private void buscarProveedorPorRuc() {

        String ruc
                = txtBuscar.getText() == null
                ? ""
                : txtBuscar.getText()
                        .trim();

        if (ruc.isBlank()) {

            cargarProveedores();

            return;
        }

        if (!ruc.matches(
                "\\d{11}"
        )) {

            mostrarAviso(
                    "El RUC debe tener exactamente 11 dígitos numéricos."
            );

            txtBuscar.requestFocus();

            return;
        }

        try {

            Proveedor proveedor
                    = proveedorService
                            .buscarPorRuc(
                                    ruc
                            );

            modeloTabla.setRowCount(
                    0
            );

            if (proveedor == null) {

                mostrarInformacion(
                        "No se encontró un proveedor con el RUC indicado."
                );

                return;
            }

            modeloTabla.addRow(
                    new Object[]{
                        proveedor.getIdProveedor(),
                        proveedor.getRuc(),
                        proveedor.getRazonSocial(),
                        proveedor.getDireccion(),
                        proveedor.getTelefono(),
                        proveedor.getContacto(),
                        proveedor.isActivo()
                        ? "ACTIVO"
                        : "INACTIVO"
                    }
            );

        } catch (Exception ex) {

            mostrarError(
                    ex.getMessage() == null
                    ? "No se pudo realizar la búsqueda."
                    : ex.getMessage()
            );
        }
    }

    private void cargarProveedorSeleccionado() {

        int filaVista
                = tblProveedores
                        .getSelectedRow();

        if (filaVista < 0) {
            return;
        }

        int filaModelo
                = tblProveedores
                        .convertRowIndexToModel(
                                filaVista
                        );

        int idProveedor
                = ((Number) modeloTabla.getValueAt(
                        filaModelo,
                        0
                )).intValue();

        try {

            Proveedor proveedor
                    = proveedorService
                            .buscarPorId(
                                    idProveedor
                            );

            if (proveedor == null) {

                mostrarError(
                        "No se pudo recuperar el proveedor seleccionado."
                );

                return;
            }

            proveedorSeleccionado
                    = proveedor;

            cargarDatosProveedor(
                    proveedor
            );

            lblModo.setText(
                    "EDITAR PROVEEDOR"
            );

            lblMensaje.setText(
                    "Modifique los datos y pulse Guardar."
            );

            btnGuardar.setEnabled(
                    true
            );

            btnActivar.setEnabled(
                    !proveedor.isActivo()
            );

            btnDesactivar.setEnabled(
                    proveedor.isActivo()
            );

        } catch (Exception ex) {

            mostrarError(
                    ex.getMessage() == null
                    ? "No se pudo cargar el proveedor."
                    : ex.getMessage()
            );
        }
    }

    private void cargarDatosProveedor(
            Proveedor proveedor
    ) {

        txtRuc.setText(
                proveedor.getRuc()
        );

        txtRazonSocial.setText(
                proveedor.getRazonSocial()
        );

        txtDireccion.setText(
                proveedor.getDireccion() == null
                ? ""
                : proveedor.getDireccion()
        );

        txtTelefono.setText(
                proveedor.getTelefono() == null
                ? ""
                : proveedor.getTelefono()
        );

        txtContacto.setText(
                proveedor.getContacto() == null
                ? ""
                : proveedor.getContacto()
        );

        txtEstado.setText(
                proveedor.isActivo()
                ? "ACTIVO"
                : "INACTIVO"
        );
    }

    private void prepararNuevoProveedor() {

        proveedorSeleccionado
                = null;

        tblProveedores.clearSelection();

        txtRuc.setText("");
        txtRazonSocial.setText("");
        txtDireccion.setText("");
        txtTelefono.setText("");
        txtContacto.setText("");

        txtEstado.setText(
                "NUEVO"
        );

        lblModo.setText(
                "NUEVO PROVEEDOR"
        );

        lblMensaje.setText(
                "Complete los datos y pulse Guardar."
        );

        btnGuardar.setEnabled(
                true
        );

        btnActivar.setEnabled(
                false
        );

        btnDesactivar.setEnabled(
                false
        );

        txtRuc.setEditable(
                true
        );

        txtRazonSocial.setEditable(
                true
        );

        txtDireccion.setEditable(
                true
        );

        txtTelefono.setEditable(
                true
        );

        txtContacto.setEditable(
                true
        );
    }

    private void guardarProveedor() {

        String ruc
                = txtRuc.getText()
                        .trim();

        String razonSocial
                = txtRazonSocial.getText()
                        .trim();

        String direccion
                = txtDireccion.getText()
                        .trim();

        String telefono
                = txtTelefono.getText()
                        .trim();

        String contacto
                = txtContacto.getText()
                        .trim();

        // =====================================================
        // VALIDACIONES BÁSICAS
        // =====================================================
        if (razonSocial.isBlank()) {

            mostrarAviso(
                    "La razón social es obligatoria."
            );

            txtRazonSocial.requestFocus();

            return;
        }

        if (ruc.isBlank()) {

            mostrarAviso(
                    "El RUC es obligatorio."
            );

            txtRuc.requestFocus();

            return;
        }

        if (!ruc.matches(
                "\\d{11}"
        )) {

            mostrarAviso(
                    "El RUC debe tener exactamente 11 dígitos numéricos."
            );

            txtRuc.requestFocus();

            return;
        }

        // =====================================================
        // REGISTRAR
        // =====================================================
        boolean nuevo
                = proveedorSeleccionado
                == null;

        RespuestaOperacion<Void> respuesta;

        if (nuevo) {

            Proveedor proveedor
                    = new Proveedor(
                            razonSocial,
                            ruc,
                            direccion.isBlank()
                            ? null
                            : direccion,
                            telefono.isBlank()
                            ? null
                            : telefono,
                            contacto.isBlank()
                            ? null
                            : contacto
                    );

            respuesta
                    = proveedorService
                            .registrar(
                                    proveedor
                            );

        } else {

            proveedorSeleccionado
                    .setRazonSocial(
                            razonSocial
                    );

            proveedorSeleccionado
                    .setRuc(
                            ruc
                    );

            proveedorSeleccionado
                    .setDireccion(
                            direccion.isBlank()
                            ? null
                            : direccion
                    );

            proveedorSeleccionado
                    .setTelefono(
                            telefono.isBlank()
                            ? null
                            : telefono
                    );

            proveedorSeleccionado
                    .setContacto(
                            contacto.isBlank()
                            ? null
                            : contacto
                    );

            respuesta
                    = proveedorService
                            .actualizar(
                                    proveedorSeleccionado
                            );
        }

        // =====================================================
        // RESPUESTA DEL SERVICE
        // =====================================================
        if (!respuesta.isExito()) {

            mostrarError(
                    respuesta.getMensaje()
            );

            return;
        }

        mostrarInformacion(
                nuevo
                        ? "Proveedor registrado correctamente."
                        : "Proveedor actualizado correctamente."
        );

        cargarProveedores();

        prepararNuevoProveedor();
    }

    private void activarProveedor() {

        if (proveedorSeleccionado == null) {
            return;
        }

        int opcion
                = JOptionPane.showConfirmDialog(
                        this,
                        "¿Desea activar el proveedor seleccionado?",
                        "Confirmar activación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (opcion
                != JOptionPane.YES_OPTION) {

            return;
        }

        RespuestaOperacion<Void> respuesta
                = proveedorService
                        .activar(
                                proveedorSeleccionado
                                        .getIdProveedor()
                        );

        if (!respuesta.isExito()) {

            mostrarError(
                    respuesta.getMensaje()
            );

            return;
        }

        mostrarInformacion(
                "Proveedor activado correctamente."
        );

        cargarProveedores();

        prepararNuevoProveedor();
    }

    private void desactivarProveedor() {

        if (proveedorSeleccionado == null) {
            return;
        }

        int opcion
                = JOptionPane.showConfirmDialog(
                        this,
                        "¿Desea desactivar el proveedor seleccionado?",
                        "Confirmar desactivación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (opcion
                != JOptionPane.YES_OPTION) {

            return;
        }

        RespuestaOperacion<Void> respuesta
                = proveedorService
                        .desactivar(
                                proveedorSeleccionado
                                        .getIdProveedor()
                        );

        if (!respuesta.isExito()) {

            mostrarError(
                    respuesta.getMensaje()
            );

            return;
        }

        mostrarInformacion(
                "Proveedor desactivado correctamente."
        );

        cargarProveedores();

        prepararNuevoProveedor();
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

                    FrmGestionProveedores frm
                    = new FrmGestionProveedores();

                    frm.setVisible(
                            true
                    );
                }
        );
    }
}
