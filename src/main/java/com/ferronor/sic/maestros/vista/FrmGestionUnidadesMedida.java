package com.ferronor.sic.maestros.vista;

import com.ferronor.sic.maestros.logica.UnidadMedidaService;
import com.ferronor.sic.maestros.modelo.UnidadMedida;
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
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FrmGestionUnidadesMedida extends FrmBase {

    private final UnidadMedidaService unidadMedidaService =
            ServiceFactory.unidadMedidaService();

    private UnidadMedida unidadMedidaSeleccionada;

    private final DefaultTableModel modeloTabla =
            new DefaultTableModel(
                    new Object[]{
                        "ID",
                        "NOMBRE",
                        "ABREVIATURA"
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

    private final JTable tblUnidades =
            new JTable(modeloTabla);

    private final JTextField txtBuscar =
            new JTextField();

    private final JTextField txtNombre =
            new JTextField();

    private final JTextField txtAbreviatura =
            new JTextField();

    private final JLabel lblModo =
            new JLabel("NUEVA UNIDAD");

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

    public FrmGestionUnidadesMedida() {

        super("MAESTROS");

        construirInterfaz();
        cargarUnidades();
        prepararNuevaUnidad();
        actualizarCabecera();
    }

    private void construirInterfaz() {

        setTitle(
                "Gestión de Unidades de Medida"
        );

        setMinimumSize(
                new Dimension(
                        900,
                        600
                )
        );

        setSize(
                1000,
                680
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
                        "UNIDADES DE MEDIDA"
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
                        "Gestión y mantenimiento de unidades utilizadas en los productos"
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
                        "01. CONSULTA DE UNIDADES"
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
                        "BUSCAR UNIDAD"
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
                e -> buscarUnidades()
        );

        btnMostrarTodas.addActionListener(
                e -> {

                    txtBuscar.setText("");

                    cargarUnidades();

                    prepararNuevaUnidad();
                }
        );

        txtBuscar.addActionListener(
                e -> buscarUnidades()
        );

        return panel;
    }

    private JScrollPane construirTabla() {

        tblUnidades.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblUnidades.setRowHeight(
                27
        );

        tblUnidades.setAutoCreateRowSorter(
                true
        );

        tblUnidades.setFillsViewportHeight(
                true
        );

        tblUnidades.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        tblUnidades.getTableHeader()
                .setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                11
                        )
                );

        tblUnidades.getTableHeader()
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

        tblUnidades.getColumnModel()
                .getColumn(0)
                .setCellRenderer(
                        centrado
                );

        tblUnidades.getColumnModel()
                .getColumn(2)
                .setCellRenderer(
                        centrado
                );

        tblUnidades.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(
                        80
                );

        tblUnidades.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(
                        350
                );

        tblUnidades.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(
                        150
                );

        tblUnidades.getSelectionModel()
                .addListSelectionListener(
                        e -> {

                            if (!e.getValueIsAdjusting()) {
                                cargarUnidadSeleccionada();
                            }
                        }
                );

        tblUnidades.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent e
                    ) {

                        if (e.getClickCount() == 2
                                && tblUnidades
                                .getSelectedRow() >= 0) {

                            cargarUnidadSeleccionada();
                        }
                    }
                }
        );

        return new JScrollPane(
                tblUnidades
        );
    }

    private JPanel construirGestion() {

        JPanel panel =
                crearPanelSeccion(
                        "02. DATOS DE LA UNIDAD DE MEDIDA"
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
        // FILA 0
        // =====================================================

        agregarCampo(
                panel,
                gbc,
                0,
                0,
                "NOMBRE",
                txtNombre,
                3
        );

        agregarCampo(
                panel,
                gbc,
                4,
                0,
                "ABREVIATURA",
                txtAbreviatura
        );

        // =====================================================
        // FILA 1
        // =====================================================

        gbc.gridx = 0;
        gbc.gridy = 1;
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
        // FILA 2
        // =====================================================

        gbc.gridx = 0;
        gbc.gridy = 2;
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
        gbc.gridy = 2;
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
                        "Nueva unidad"
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
                e -> prepararNuevaUnidad()
        );

        btnGuardar.addActionListener(
                e -> guardarUnidad()
        );

        btnActualizar.addActionListener(
                e -> {

                    cargarUnidades();

                    prepararNuevaUnidad();
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

    private void cargarUnidades() {

        try {

            List<UnidadMedida> unidades =
                    unidadMedidaService.listar();

            cargarTabla(
                    unidades
            );

        } catch (Exception ex) {

            mostrarError(
                    ex.getMessage() == null
                            ? "No se pudieron cargar las unidades de medida."
                            : ex.getMessage()
            );
        }
    }

    private void buscarUnidades() {

        String texto =
                txtBuscar.getText() == null
                        ? ""
                        : txtBuscar.getText()
                                .trim();

        if (texto.isBlank()) {

            cargarUnidades();

            return;
        }

        try {

            List<UnidadMedida> resultado =
                    new java.util.ArrayList<>();

            for (UnidadMedida unidad :
                    unidadMedidaService.listar()) {

                boolean coincideNombre =
                        unidad.getNombre() != null
                        && unidad.getNombre()
                                .toLowerCase()
                                .contains(
                                        texto.toLowerCase()
                                );

                boolean coincideAbreviatura =
                        unidad.getAbreviatura() != null
                        && unidad.getAbreviatura()
                                .toLowerCase()
                                .contains(
                                        texto.toLowerCase()
                                );

                if (coincideNombre
                        || coincideAbreviatura) {

                    resultado.add(
                            unidad
                    );
                }
            }

            cargarTabla(
                    resultado
            );

            if (resultado.isEmpty()) {

                mostrarInformacion(
                        "No se encontraron unidades de medida."
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
            List<UnidadMedida> unidades
    ) {

        modeloTabla.setRowCount(
                0
        );

        if (unidades == null) {
            return;
        }

        for (UnidadMedida unidad :
                unidades) {

            modeloTabla.addRow(
                    new Object[]{
                        unidad.getIdUnidadMedida(),
                        unidad.getNombre(),
                        unidad.getAbreviatura()
                    }
            );
        }
    }

    private void cargarUnidadSeleccionada() {

        int filaVista =
                tblUnidades
                        .getSelectedRow();

        if (filaVista < 0) {
            return;
        }

        int filaModelo =
                tblUnidades
                        .convertRowIndexToModel(
                                filaVista
                        );

        int idUnidad =
                ((Number)
                        modeloTabla.getValueAt(
                                filaModelo,
                                0
                        )).intValue();

        try {

            UnidadMedida unidad =
                    unidadMedidaService
                            .buscarPorId(
                                    idUnidad
                            );

            if (unidad == null) {

                mostrarError(
                        "No se pudo recuperar la unidad seleccionada."
                );

                return;
            }

            unidadMedidaSeleccionada =
                    unidad;

            txtNombre.setText(
                    unidad.getNombre()
            );

            txtAbreviatura.setText(
                    unidad.getAbreviatura()
            );

            lblModo.setText(
                    "EDITAR UNIDAD"
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
                            ? "No se pudo cargar la unidad de medida."
                            : ex.getMessage()
            );
        }
    }

    private void prepararNuevaUnidad() {

        unidadMedidaSeleccionada =
                null;

        tblUnidades.clearSelection();

        txtNombre.setText(
                ""
        );

        txtAbreviatura.setText(
                ""
        );

        lblModo.setText(
                "NUEVA UNIDAD"
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

        txtAbreviatura.setEditable(
                true
        );

        txtNombre.requestFocus();
    }

    private void guardarUnidad() {

        String nombre =
                txtNombre.getText()
                        .trim();

        String abreviatura =
                txtAbreviatura.getText()
                        .trim();

        // =====================================================
        // VALIDACIONES BÁSICAS
        // =====================================================

        if (nombre.isBlank()) {

            mostrarAviso(
                    "El nombre de la unidad de medida es obligatorio."
            );

            txtNombre.requestFocus();

            return;
        }

        if (abreviatura.isBlank()) {

            mostrarAviso(
                    "La abreviatura es obligatoria."
            );

            txtAbreviatura.requestFocus();

            return;
        }

        if (nombre.length() > 30) {

            mostrarAviso(
                    "El nombre no puede superar los 30 caracteres."
            );

            txtNombre.requestFocus();

            return;
        }

        if (abreviatura.length() > 10) {

            mostrarAviso(
                    "La abreviatura no puede superar los 10 caracteres."
            );

            txtAbreviatura.requestFocus();

            return;
        }

        boolean nueva =
                unidadMedidaSeleccionada
                        == null;

        RespuestaOperacion<Void> respuesta;

        if (nueva) {

            UnidadMedida unidad =
                    new UnidadMedida(
                            nombre,
                            abreviatura
                    );

            respuesta =
                    unidadMedidaService
                            .registrar(
                                    unidad
                            );

        } else {

            unidadMedidaSeleccionada
                    .setNombre(
                            nombre
                    );

            unidadMedidaSeleccionada
                    .setAbreviatura(
                            abreviatura
                    );

            respuesta =
                    unidadMedidaService
                            .actualizar(
                                    unidadMedidaSeleccionada
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
                        ? "Unidad de medida registrada correctamente."
                        : "Unidad de medida actualizada correctamente."
        );

        cargarUnidades();

        prepararNuevaUnidad();
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

                    FrmGestionUnidadesMedida frm =
                            new FrmGestionUnidadesMedida();

                    frm.setVisible(
                            true
                    );
                }
        );
    }
}
