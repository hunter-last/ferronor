package com.ferronor.sic.maestros.vista;

import com.ferronor.sic.maestros.logica.ClienteService;
import com.ferronor.sic.maestros.modelo.Cliente;
import com.ferronor.sic.maestros.modelo.TipoDocumento;
import com.ferronor.sic.shared.FrmBase;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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
import java.util.List;

public class FrmGestionClientes extends FrmBase {

    private final ClienteService clienteService
            = ServiceFactory.clienteService();

    private final DefaultTableModel modeloTabla
            = new DefaultTableModel(
                    new Object[]{
                        "ID",
                        "TIPO",
                        "N.º DOCUMENTO",
                        "NOMBRE / RAZÓN SOCIAL",
                        "TELÉFONO",
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

    private final JTable tblClientes
            = new JTable(modeloTabla);

    private final JTextField txtBuscar
            = new JTextField();

    private final JComboBox<TipoDocumento> cmbTipoDocumento
            = new JComboBox<>(TipoDocumento.values());

    private final JTextField txtNumeroDocumento
            = new JTextField();

    private final JTextField txtNombreRazonSocial
            = new JTextField();

    private final JTextField txtTelefono
            = new JTextField();

    private final JTextField txtEstado
            = new JTextField();

    private final JLabel lblModo
            = new JLabel("NUEVO CLIENTE");

    private final JLabel lblMensaje
            = new JLabel(
                    "Complete los datos del cliente y pulse Guardar."
            );

    private final JLabel lblUsuario
            = new JLabel();

    private final JLabel lblFechaHora
            = new JLabel();

    private final DateTimeFormatter formatoFechaHora
            = DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm"
            );

    private Cliente clienteSeleccionado;

    private JButton btnGuardar;
    private JButton btnActivar;
    private JButton btnDesactivar;

    public FrmGestionClientes() {

        super("MAESTROS");

        construirInterfaz();
        configurarComponentes();
        cargarClientes();
        prepararNuevoCliente();
        actualizarCabecera();
    }

    private void construirInterfaz() {

        setTitle("Gestión de Clientes");

        setSize(
                1100,
                700
        );

        setMinimumSize(
                new Dimension(
                        950,
                        620
                )
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
    }

    private JPanel construirCabecera() {

        JPanel panel
                = new JPanel(
                        new BorderLayout(
                                15,
                                2
                        )
                );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        4,
                        8,
                        8,
                        8
                )
        );

        JPanel izquierda
                = new JPanel(
                        new GridLayout(
                                2,
                                1,
                                0,
                                2
                        )
                );

        JLabel lblTitulo
                = new JLabel("CLIENTES");

        lblTitulo.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        24
                )
        );

        JLabel lblSubtitulo
                = new JLabel(
                        "Gestión y mantenimiento del padrón de clientes"
                );

        lblSubtitulo.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        10
                )
        );

        izquierda.add(lblTitulo);
        izquierda.add(lblSubtitulo);

        JPanel derecha
                = new JPanel(
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

    private void actualizarCabecera() {

        SesionUsuario sesion = SesionUsuario.actual();

        lblUsuario.setText(
                sesion.getNombreCompleto()
                + "  |  "
                + sesion.getNombreRol()
        );

        lblFechaHora.setText(
                LocalDateTime.now()
                        .format(formatoFechaHora)
        );
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
                construirPanelDatos(),
                BorderLayout.SOUTH
        );

        return centro;
    }

    private JPanel construirConsulta() {

        JPanel panel
                = crearPanelSeccion(
                        "01. CONSULTA DE CLIENTES"
                );

        panel.setLayout(
                new BorderLayout(
                        8,
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
                        "N.º DOCUMENTO"
                );

        txtBuscar.setPreferredSize(
                new Dimension(
                        320,
                        30
                )
        );

        JButton btnConsultar
                = crearBoton("Consultar");

        JButton btnMostrarTodos
                = crearBoton("Mostrar todos");

        JPanel botones
                = new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                5,
                                0
                        )
                );

        botones.add(btnConsultar);
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

        btnConsultar.addActionListener(
                e -> consultarPorDocumento()
        );

        btnMostrarTodos.addActionListener(
                e -> {

                    txtBuscar.setText("");

                    cargarClientes();

                    prepararNuevoCliente();
                }
        );

        txtBuscar.addActionListener(
                e -> consultarPorDocumento()
        );

        return panel;
    }

    private JScrollPane construirTabla() {

        tblClientes.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblClientes.setAutoCreateRowSorter(true);
        tblClientes.setRowHeight(27);
        tblClientes.setFillsViewportHeight(true);

        tblClientes.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        tblClientes.getTableHeader().setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11
                )
        );

        tblClientes.getTableHeader()
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

        tblClientes.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(55);

        tblClientes.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(80);

        tblClientes.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(140);

        tblClientes.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(330);

        tblClientes.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(130);

        tblClientes.getColumnModel()
                .getColumn(5)
                .setPreferredWidth(100);

        tblClientes.getColumnModel()
                .getColumn(0)
                .setCellRenderer(centrado);

        tblClientes.getColumnModel()
                .getColumn(1)
                .setCellRenderer(centrado);

        tblClientes.getColumnModel()
                .getColumn(5)
                .setCellRenderer(centrado);

        tblClientes.getSelectionModel()
                .addListSelectionListener(
                        e -> {

                            if (!e.getValueIsAdjusting()) {
                                cargarClienteSeleccionado();
                            }
                        }
                );

        tblClientes.addMouseListener(
                new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(
                    java.awt.event.MouseEvent e
            ) {

                if (e.getClickCount() == 2
                        && tblClientes.getSelectedRow() >= 0) {

                    cargarClienteSeleccionado();
                }
            }
        }
        );

        return new JScrollPane(
                tblClientes
        );
    }

    private JPanel construirPanelDatos() {

        JPanel panel
                = crearPanelSeccion(
                        "02. DATOS DEL CLIENTE"
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

        agregarCampo(
                panel,
                gbc,
                0,
                0,
                "TIPO DE DOCUMENTO",
                cmbTipoDocumento
        );

        agregarCampo(
                panel,
                gbc,
                2,
                0,
                "N.º DOCUMENTO",
                txtNumeroDocumento
        );

        agregarCampo(
                panel,
                gbc,
                0,
                1,
                "NOMBRE / RAZÓN SOCIAL",
                txtNombreRazonSocial,
                5
        );

        agregarCampo(
                panel,
                gbc,
                0,
                2,
                "TELÉFONO",
                txtTelefono
        );

        JLabel lblEstado
                = crearEtiqueta("ESTADO");

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;

        panel.add(
                lblEstado,
                gbc
        );

        txtEstado.setEditable(false);
        txtEstado.setFocusable(false);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.25;

        panel.add(
                txtEstado,
                gbc
        );

        lblModo.setFont(
                new Font(
                        "Consolas",
                        Font.BOLD,
                        10
                )
        );

        gbc.gridx = 4;
        gbc.weightx = 0;

        panel.add(
                lblModo,
                gbc
        );

        lblMensaje.setFont(
                new Font(
                        "Segoe UI",
                        Font.ITALIC,
                        11
                )
        );

        gbc.gridx = 5;
        gbc.weightx = 1;
        gbc.gridwidth = 2;

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
                        "Nuevo cliente"
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
                e -> prepararNuevoCliente()
        );

        btnGuardar.addActionListener(
                e -> guardarCliente()
        );

        btnActivar.addActionListener(
                e -> activarCliente()
        );

        btnDesactivar.addActionListener(
                e -> desactivarCliente()
        );

        btnActualizar.addActionListener(
                e -> {

                    cargarClientes();

                    prepararNuevoCliente();
                }
        );

        btnCerrar.addActionListener(
                e -> dispose()
        );

        return panel;
    }

    private void configurarComponentes() {

        cmbTipoDocumento.setRenderer(
                new DefaultListCellRenderer() {

            @Override
            public Component
                    getListCellRendererComponent(
                            javax.swing.JList<?> list,
                            Object value,
                            int index,
                            boolean selected,
                            boolean focus
                    ) {

                super.getListCellRendererComponent(
                        list,
                        value,
                        index,
                        selected,
                        focus
                );

                if (value instanceof TipoDocumento tipo) {
                    setText(tipo.name());
                }

                return this;
            }
        }
        );
    }

    private void cargarClientes() {

        try {

            List<Cliente> clientes
                    = clienteService.listar();

            cargarTabla(clientes);

        } catch (Exception ex) {

            mostrarError(
                    ex.getMessage() == null
                    ? "No se pudieron cargar los clientes."
                    : ex.getMessage()
            );
        }
    }

    private void consultarPorDocumento() {

        String documento
                = txtBuscar.getText() == null
                ? ""
                : txtBuscar.getText().trim();

        if (documento.isBlank()) {

            mostrarAviso(
                    "Ingrese un número de documento."
            );

            txtBuscar.requestFocus();
            return;
        }

        if (!documento.matches("\\d+")) {

            mostrarAviso(
                    "El número de documento debe contener únicamente dígitos."
            );

            txtBuscar.requestFocus();
            return;
        }

        try {

            Cliente cliente
                    = clienteService
                            .buscarPorNumeroDocumento(
                                    documento
                            );

            modeloTabla.setRowCount(0);

            if (cliente == null) {

                prepararNuevoCliente();

                lblMensaje.setText(
                        "No se encontró ningún cliente con ese número de documento."
                );

                mostrarAviso(
                        "No se encontró un cliente con el número de documento indicado."
                );

                return;
            }

            cargarFila(cliente);

            tblClientes.setRowSelectionInterval(
                    0,
                    0
            );

        } catch (Exception ex) {

            mostrarError(
                    ex.getMessage() == null
                    ? "No se pudo realizar la consulta."
                    : ex.getMessage()
            );
        }
    }

    private void cargarTabla(
            List<Cliente> clientes
    ) {

        modeloTabla.setRowCount(0);

        if (clientes == null) {
            return;
        }

        for (Cliente cliente : clientes) {
            cargarFila(cliente);
        }
    }

    private void cargarFila(
            Cliente cliente
    ) {

        modeloTabla.addRow(
                new Object[]{
                    cliente.getIdCliente(),
                    cliente.getTipoDocumento().name(),
                    cliente.getNumeroDocumento(),
                    cliente.getNombreRazonSocial(),
                    cliente.getTelefono() == null
                    ? ""
                    : cliente.getTelefono(),
                    cliente.isActivo()
                    ? "ACTIVO"
                    : "INACTIVO"
                }
        );
    }

    private void cargarClienteSeleccionado() {

        int filaVista
                = tblClientes.getSelectedRow();

        if (filaVista < 0) {
            return;
        }

        int filaModelo
                = tblClientes.convertRowIndexToModel(
                        filaVista
                );

        int idCliente
                = ((Number) modeloTabla.getValueAt(
                        filaModelo,
                        0
                )).intValue();

        Cliente cliente
                = clienteService.buscarPorId(
                        idCliente
                );

        if (cliente == null) {

            mostrarError(
                    "No se pudo recuperar el cliente seleccionado."
            );

            return;
        }

        clienteSeleccionado
                = cliente;

        cmbTipoDocumento.setSelectedItem(
                cliente.getTipoDocumento()
        );

        txtNumeroDocumento.setText(
                cliente.getNumeroDocumento()
        );

        txtNombreRazonSocial.setText(
                cliente.getNombreRazonSocial()
        );

        txtTelefono.setText(
                cliente.getTelefono() == null
                ? ""
                : cliente.getTelefono()
        );

        txtEstado.setText(
                cliente.isActivo()
                ? "ACTIVO"
                : "INACTIVO"
        );

        lblModo.setText(
                "EDITAR CLIENTE"
        );

        lblMensaje.setText(
                "Cliente seleccionado. Modifique los datos y pulse Guardar."
        );

        btnGuardar.setEnabled(true);

        btnActivar.setEnabled(
                !cliente.isActivo()
        );

        btnDesactivar.setEnabled(
                cliente.isActivo()
        );
    }

    private void prepararNuevoCliente() {

        clienteSeleccionado = null;

        tblClientes.clearSelection();

        cmbTipoDocumento.setSelectedItem(
                TipoDocumento.DNI
        );

        txtNumeroDocumento.setText("");
        txtNombreRazonSocial.setText("");
        txtTelefono.setText("");
        txtEstado.setText("NUEVO");

        lblModo.setText(
                "NUEVO CLIENTE"
        );

        lblMensaje.setText(
                "Complete los datos del cliente y pulse Guardar."
        );

        btnGuardar.setEnabled(true);
        btnActivar.setEnabled(false);
        btnDesactivar.setEnabled(false);
    }

    private void guardarCliente() {

        TipoDocumento tipoDocumento
                = (TipoDocumento) cmbTipoDocumento
                        .getSelectedItem();

        if (tipoDocumento == null) {

            mostrarAviso(
                    "Seleccione el tipo de documento."
            );

            return;
        }

        String numeroDocumento
                = txtNumeroDocumento
                        .getText()
                        .trim();

        if (numeroDocumento.isBlank()) {

            mostrarAviso(
                    "El número de documento es obligatorio."
            );

            txtNumeroDocumento.requestFocus();
            return;
        }

        int longitudEsperada
                = tipoDocumento == TipoDocumento.DNI
                        ? 8
                        : 11;

        if (!numeroDocumento.matches(
                "\\d{"
                + longitudEsperada
                + "}"
        )) {

            mostrarAviso(
                    "El "
                    + tipoDocumento
                    + " debe tener "
                    + longitudEsperada
                    + " dígitos numéricos."
            );

            txtNumeroDocumento.requestFocus();
            return;
        }

        String nombre
                = txtNombreRazonSocial
                        .getText()
                        .trim();

        if (nombre.isBlank()) {

            mostrarAviso(
                    "El nombre o razón social es obligatorio."
            );

            txtNombreRazonSocial.requestFocus();
            return;
        }

        String telefono
                = txtTelefono
                        .getText()
                        .trim();

        RespuestaOperacion<Void> respuesta;

        boolean nuevo
                = clienteSeleccionado == null;

        if (nuevo) {

            Cliente cliente
                    = new Cliente(
                            tipoDocumento,
                            numeroDocumento,
                            nombre,
                            telefono.isBlank()
                            ? null
                            : telefono
                    );

            respuesta
                    = clienteService.registrar(
                            cliente
                    );

        } else {

            clienteSeleccionado.setTipoDocumento(
                    tipoDocumento
            );

            clienteSeleccionado.setNumeroDocumento(
                    numeroDocumento
            );

            clienteSeleccionado.setNombreRazonSocial(
                    nombre
            );

            clienteSeleccionado.setTelefono(
                    telefono.isBlank()
                    ? null
                    : telefono
            );

            respuesta
                    = clienteService.actualizar(
                            clienteSeleccionado
                    );
        }

        if (!respuesta.isExito()) {

            mostrarError(
                    respuesta.getMensaje()
            );

            return;
        }

        mostrarInformacion(
                nuevo
                        ? "Cliente registrado correctamente."
                        : "Cliente actualizado correctamente."
        );

        cargarClientes();

        prepararNuevoCliente();
    }

    private void activarCliente() {

        if (clienteSeleccionado == null) {
            return;
        }

        int opcion
                = javax.swing.JOptionPane.showConfirmDialog(
                        this,
                        "¿Desea activar el cliente seleccionado?",
                        "Confirmar activación",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE
                );

        if (opcion
                != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        RespuestaOperacion<Void> respuesta
                = clienteService.activar(
                        clienteSeleccionado
                                .getIdCliente()
                );

        if (!respuesta.isExito()) {

            mostrarError(
                    respuesta.getMensaje()
            );

            return;
        }

        mostrarInformacion(
                "Cliente activado correctamente."
        );

        cargarClientes();

        prepararNuevoCliente();
    }

    private void desactivarCliente() {

        if (clienteSeleccionado == null) {
            return;
        }

        int opcion
                = javax.swing.JOptionPane.showConfirmDialog(
                        this,
                        "¿Desea desactivar el cliente seleccionado?",
                        "Confirmar desactivación",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.WARNING_MESSAGE
                );

        if (opcion
                != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        RespuestaOperacion<Void> respuesta
                = clienteService.desactivar(
                        clienteSeleccionado
                                .getIdCliente()
                );

        if (!respuesta.isExito()) {

            mostrarError(
                    respuesta.getMensaje()
            );

            return;
        }

        mostrarInformacion(
                "Cliente desactivado correctamente."
        );

        cargarClientes();

        prepararNuevoCliente();
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

        JLabel etiqueta
                = new JLabel(texto);

        etiqueta.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        10
                )
        );

        return etiqueta;
    }

    private JButton crearBoton(
            String texto
    ) {

        JButton boton
                = new JButton(texto);

        boton.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        boton.setFocusPainted(false);

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
            int columnas
    ) {

        JLabel etiqueta
                = crearEtiqueta(texto);

        gbc.gridx = columna;
        gbc.gridy = fila;
        gbc.gridwidth = 1;
        gbc.weightx = 0;

        panel.add(
                etiqueta,
                gbc
        );

        gbc.gridx
                = columna + 1;

        gbc.gridwidth
                = columnas;

        gbc.weightx
                = 1;

        componente.setPreferredSize(
                new Dimension(
                        190,
                        29
                )
        );

        panel.add(
                componente,
                gbc
        );
    }

    private void mostrarAviso(
            String mensaje
    ) {

        javax.swing.JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Aviso",
                javax.swing.JOptionPane.WARNING_MESSAGE
        );
    }

    private void mostrarError(
            String mensaje
    ) {

        javax.swing.JOptionPane.showMessageDialog(
                this,
                mensaje == null
                        ? "Ocurrió un error."
                        : mensaje,
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE
        );
    }

    private void mostrarInformacion(
            String mensaje
    ) {

        javax.swing.JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Operación exitosa",
                javax.swing.JOptionPane.INFORMATION_MESSAGE
        );
    }

}
