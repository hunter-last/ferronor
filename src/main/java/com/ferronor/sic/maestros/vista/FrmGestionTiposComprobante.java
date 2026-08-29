package com.ferronor.sic.maestros.vista;

import com.ferronor.sic.maestros.logica.TipoComprobanteService;
import com.ferronor.sic.maestros.modelo.TipoComprobante;
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
import java.util.ArrayList;
import java.util.List;

public class FrmGestionTiposComprobante extends FrmBase {

    private final TipoComprobanteService tipoComprobanteService =
            ServiceFactory.tipoComprobanteService();

    private TipoComprobante tipoComprobanteSeleccionado;

    private final DefaultTableModel modeloTabla =
            new DefaultTableModel(
                    new Object[]{
                        "ID",
                        "NOMBRE",
                        "SERIE"
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

    private final JTable tblTiposComprobante =
            new JTable(modeloTabla);

    private final JTextField txtBuscar =
            new JTextField();

    private final JTextField txtNombre =
            new JTextField();

    private final JTextField txtSerie =
            new JTextField();

    private final JLabel lblModo =
            new JLabel(
                    "NUEVO TIPO DE COMPROBANTE"
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

    public FrmGestionTiposComprobante() {

        super("MAESTROS");

        construirInterfaz();
        cargarTiposComprobante();
        prepararNuevoTipoComprobante();
        actualizarCabecera();
    }

    private void construirInterfaz() {

        setTitle(
                "Gestión de Tipos de Comprobante"
        );

        setMinimumSize(
                new Dimension(
                        950,
                        620
                )
        );

        setSize(
                1050,
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
                        "TIPOS DE COMPROBANTE"
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
                        "Gestión y mantenimiento de tipos de comprobante"
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
                        "01. CONSULTA DE TIPOS DE COMPROBANTE"
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
                        "BUSCAR COMPROBANTE"
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

        JButton btnMostrarTodos =
                crearBoton(
                        "Mostrar todos"
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
                btnMostrarTodos
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
                e -> buscarTiposComprobante()
        );

        btnMostrarTodos.addActionListener(
                e -> {

                    txtBuscar.setText("");

                    cargarTiposComprobante();

                    prepararNuevoTipoComprobante();
                }
        );

        txtBuscar.addActionListener(
                e -> buscarTiposComprobante()
        );

        return panel;
    }

    private JScrollPane construirTabla() {

        tblTiposComprobante.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblTiposComprobante.setRowHeight(
                27
        );

        tblTiposComprobante.setAutoCreateRowSorter(
                true
        );

        tblTiposComprobante.setFillsViewportHeight(
                true
        );

        tblTiposComprobante.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        tblTiposComprobante.getTableHeader()
                .setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                11
                        )
                );

        tblTiposComprobante.getTableHeader()
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

        tblTiposComprobante.getColumnModel()
                .getColumn(0)
                .setCellRenderer(
                        centrado
                );

        tblTiposComprobante.getColumnModel()
                .getColumn(2)
                .setCellRenderer(
                        centrado
                );

        tblTiposComprobante.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(
                        80
                );

        tblTiposComprobante.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(
                        350
                );

        tblTiposComprobante.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(
                        150
                );

        tblTiposComprobante.getSelectionModel()
                .addListSelectionListener(
                        e -> {

                            if (!e.getValueIsAdjusting()) {
                                cargarTipoComprobanteSeleccionado();
                            }
                        }
                );

        tblTiposComprobante.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent e
                    ) {

                        if (e.getClickCount() == 2
                                && tblTiposComprobante
                                .getSelectedRow() >= 0) {

                            cargarTipoComprobanteSeleccionado();
                        }
                    }
                }
        );

        return new JScrollPane(
                tblTiposComprobante
        );
    }

    private JPanel construirGestion() {

        JPanel panel =
                crearPanelSeccion(
                        "02. DATOS DEL TIPO DE COMPROBANTE"
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
                "SERIE",
                txtSerie
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

        JButton btnNuevo =
                crearBoton(
                        "Nuevo tipo"
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
                btnNuevo
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

        btnNuevo.addActionListener(
                e -> prepararNuevoTipoComprobante()
        );

        btnGuardar.addActionListener(
                e -> guardarTipoComprobante()
        );

        btnActualizar.addActionListener(
                e -> {

                    cargarTiposComprobante();

                    prepararNuevoTipoComprobante();
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

    private void cargarTiposComprobante() {

        try {

            List<TipoComprobante> tipos =
                    tipoComprobanteService
                            .listar();

            cargarTabla(
                    tipos
            );

        } catch (Exception ex) {

            mostrarError(
                    ex.getMessage() == null
                            ? "No se pudieron cargar los tipos de comprobante."
                            : ex.getMessage()
            );
        }
    }

    private void buscarTiposComprobante() {

        String texto =
                txtBuscar.getText() == null
                        ? ""
                        : txtBuscar.getText()
                                .trim();

        if (texto.isBlank()) {

            cargarTiposComprobante();

            return;
        }

        try {

            /*
             * TipoComprobanteService no posee búsqueda parcial.
             * Se utiliza listar() y se filtra localmente por
             * nombre o serie.
             */

            List<TipoComprobante> encontrados =
                    new ArrayList<>();

            String textoNormalizado =
                    texto.toLowerCase();

            for (TipoComprobante tipo :
                    tipoComprobanteService.listar()) {

                boolean coincideNombre =
                        tipo.getNombre() != null
                        && tipo.getNombre()
                                .toLowerCase()
                                .contains(
                                        textoNormalizado
                                );

                boolean coincideSerie =
                        tipo.getSerie() != null
                        && tipo.getSerie()
                                .toLowerCase()
                                .contains(
                                        textoNormalizado
                                );

                if (coincideNombre
                        || coincideSerie) {

                    encontrados.add(
                            tipo
                    );
                }
            }

            cargarTabla(
                    encontrados
            );

            if (encontrados.isEmpty()) {

                mostrarInformacion(
                        "No se encontraron tipos de comprobante."
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
            List<TipoComprobante> tipos
    ) {

        modeloTabla.setRowCount(
                0
        );

        if (tipos == null) {
            return;
        }

        for (TipoComprobante tipo :
                tipos) {

            modeloTabla.addRow(
                    new Object[]{
                        tipo.getIdTipoComprobante(),
                        tipo.getNombre(),
                        tipo.getSerie() == null
                                || tipo.getSerie().isBlank()
                                ? "-"
                                : tipo.getSerie()
                    }
            );
        }
    }

    private void cargarTipoComprobanteSeleccionado() {

        int filaVista =
                tblTiposComprobante
                        .getSelectedRow();

        if (filaVista < 0) {
            return;
        }

        int filaModelo =
                tblTiposComprobante
                        .convertRowIndexToModel(
                                filaVista
                        );

        int idTipo =
                ((Number)
                        modeloTabla.getValueAt(
                                filaModelo,
                                0
                        )).intValue();

        try {

            TipoComprobante tipo =
                    tipoComprobanteService
                            .buscarPorId(
                                    idTipo
                            );

            if (tipo == null) {

                mostrarError(
                        "No se pudo recuperar el tipo de comprobante seleccionado."
                );

                return;
            }

            tipoComprobanteSeleccionado =
                    tipo;

            txtNombre.setText(
                    tipo.getNombre()
            );

            txtSerie.setText(
                    tipo.getSerie() == null
                            ? ""
                            : tipo.getSerie()
            );

            lblModo.setText(
                    "EDITAR TIPO DE COMPROBANTE"
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
                            ? "No se pudo cargar el tipo de comprobante."
                            : ex.getMessage()
            );
        }
    }

    private void prepararNuevoTipoComprobante() {

        tipoComprobanteSeleccionado =
                null;

        tblTiposComprobante.clearSelection();

        txtNombre.setText(
                ""
        );

        txtSerie.setText(
                ""
        );

        lblModo.setText(
                "NUEVO TIPO DE COMPROBANTE"
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

        txtSerie.setEditable(
                true
        );

        txtNombre.requestFocus();
    }

    private void guardarTipoComprobante() {

        String nombre =
                txtNombre.getText()
                        .trim();

        String serie =
                txtSerie.getText()
                        .trim();

        // =====================================================
        // VALIDACIONES BÁSICAS DE LA VISTA
        // =====================================================

        if (nombre.isBlank()) {

            mostrarAviso(
                    "El nombre del tipo de comprobante es obligatorio."
            );

            txtNombre.requestFocus();

            return;
        }

        if (nombre.length() > 20) {

            mostrarAviso(
                    "El nombre no puede superar los 20 caracteres."
            );

            txtNombre.requestFocus();

            return;
        }

        if (serie.length() > 10) {

            mostrarAviso(
                    "La serie no puede superar los 10 caracteres."
            );

            txtSerie.requestFocus();

            return;
        }

        boolean nuevo =
                tipoComprobanteSeleccionado
                        == null;

        RespuestaOperacion<Void> respuesta;

        if (nuevo) {

            TipoComprobante tipo =
                    new TipoComprobante(
                            nombre,
                            serie.isBlank()
                                    ? null
                                    : serie
                    );

            respuesta =
                    tipoComprobanteService
                            .registrar(
                                    tipo
                            );

        } else {

            tipoComprobanteSeleccionado
                    .setNombre(
                            nombre
                    );

            tipoComprobanteSeleccionado
                    .setSerie(
                            serie.isBlank()
                                    ? null
                                    : serie
                    );

            respuesta =
                    tipoComprobanteService
                            .actualizar(
                                    tipoComprobanteSeleccionado
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
                        ? "Tipo de comprobante registrado correctamente."
                        : "Tipo de comprobante actualizado correctamente."
        );

        cargarTiposComprobante();

        prepararNuevoTipoComprobante();
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

                    FrmGestionTiposComprobante frm =
                            new FrmGestionTiposComprobante();

                    frm.setVisible(
                            true
                    );
                }
        );
    }
}
