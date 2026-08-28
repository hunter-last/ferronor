package com.ferronor.sic.maestros.vista;

import com.ferronor.sic.maestros.logica.CategoriaService;
import com.ferronor.sic.maestros.logica.ProductoService;
import com.ferronor.sic.maestros.logica.UnidadMedidaService;
import com.ferronor.sic.maestros.modelo.Categoria;
import com.ferronor.sic.maestros.modelo.Producto;
import com.ferronor.sic.maestros.modelo.UnidadMedida;
import com.ferronor.sic.shared.FrmBase;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class FrmGestionProductos extends FrmBase {

private final ProductoService productoService =
        ServiceFactory.productoService();

private final CategoriaService categoriaService =
        ServiceFactory.categoriaService();

private final UnidadMedidaService unidadMedidaService =
        ServiceFactory.unidadMedidaService();

private Producto productoSeleccionado;

private final DefaultTableModel modeloTabla =
        new DefaultTableModel(
                new Object[]{
                    "ID",
                    "CÓDIGO",
                    "PRODUCTO",
                    "CATEGORÍA",
                    "UNIDAD",
                    "STOCK MÍN.",
                    "PRECIO VENTA",
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

private final JTable tblProductos =
        new JTable(modeloTabla);

private final JTextField txtBuscar =
        new JTextField();

private final JTextField txtCodigo =
        new JTextField();

private final JTextField txtNombre =
        new JTextField();

private final JComboBox<Categoria> cmbCategoria =
        new JComboBox<>();

private final JComboBox<UnidadMedida> cmbUnidadMedida =
        new JComboBox<>();

private final JTextField txtStockMinimo =
        new JTextField();

private final JTextField txtPrecioVenta =
        new JTextField();

private final JTextField txtEstado =
        new JTextField();

private final JLabel lblModo =
        new JLabel("NUEVO PRODUCTO");

private final JLabel lblMensaje =
        new JLabel(
                "Complete los datos y pulse Guardar."
        );

private final JLabel lblUsuario =
        new JLabel();

private final JLabel lblFechaHora =
        new JLabel();

private final DecimalFormat formatoMoneda =
        new DecimalFormat(
                "#,##0.00",
                DecimalFormatSymbols.getInstance(
                        Locale.US
                )
        );

private final DateTimeFormatter formatoFecha =
        DateTimeFormatter.ofPattern(
                "dd/MM/yyyy HH:mm"
        );

private JButton btnGuardar;
private JButton btnActivar;
private JButton btnDesactivar;

public FrmGestionProductos() {

    super("MAESTROS");

    construirInterfaz();
    cargarCatalogos();
    cargarProductos();
    prepararNuevoProducto();
    actualizarCabecera();
}

private void construirInterfaz() {

    setTitle("Gestión de Productos");
    setMinimumSize(new Dimension(1050, 680));
    setSize(1180, 760);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(
            javax.swing.WindowConstants.DISPOSE_ON_CLOSE
    );

    JPanel contenedor =
            new JPanel(new BorderLayout(12, 12));

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
    configurarCombos();
}

private JPanel construirCabecera() {

    JPanel panel =
            new JPanel(new BorderLayout(15, 4));

    panel.setBorder(
            BorderFactory.createEmptyBorder(
                    6,
                    8,
                    8,
                    8
            )
    );

    JPanel izquierda =
            new JPanel();

    izquierda.setLayout(
            new GridLayout(2, 1, 0, 2)
    );

    JLabel titulo =
            new JLabel("PRODUCTOS");

    titulo.setFont(
            new Font(
                    "Segoe UI",
                    Font.BOLD,
                    24
            )
    );

    JLabel subtitulo =
            new JLabel(
                    "Gestión y mantenimiento del catálogo de productos"
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

    JPanel derecha =
            new JPanel(
                    new GridLayout(2, 1, 0, 2)
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

    JPanel centro =
            new JPanel(
                    new BorderLayout(10, 10)
            );

    centro.add(
            construirConsulta(),
            BorderLayout.NORTH
    );

    centro.add(
            construirGestion(),
            BorderLayout.SOUTH
    );

    centro.add(
            construirTabla(),
            BorderLayout.CENTER
    );

    return centro;
}

private JPanel construirConsulta() {

    JPanel panel =
            crearPanelSeccion(
                    "01. CONSULTA DE PRODUCTOS"
            );

    panel.setLayout(
            new BorderLayout(10, 8)
    );

    JPanel fila =
            new JPanel(
                    new BorderLayout(8, 0)
            );

    JLabel lblBuscar =
            new JLabel("BUSCAR PRODUCTO");

    lblBuscar.setFont(
            new Font(
                    "Segoe UI",
                    Font.BOLD,
                    11
            )
    );

    txtBuscar.setPreferredSize(
            new Dimension(400, 30)
    );

    JButton btnBuscar =
            crearBoton(
                    "Buscar"
            );

    JButton btnMostrarTodos =
            crearBoton(
                    "Mostrar activos"
            );

    JPanel botones =
            new JPanel(
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
            e -> buscarProductos()
    );

    btnMostrarTodos.addActionListener(
            e -> {
                txtBuscar.setText("");
                cargarProductos();
                prepararNuevoProducto();
            }
    );

    txtBuscar.addActionListener(
            e -> buscarProductos()
    );

    return panel;
}

private JScrollPane construirTabla() {

    tblProductos.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
    );

    tblProductos.setRowHeight(27);
    tblProductos.setAutoCreateRowSorter(true);

    tblProductos.setFillsViewportHeight(true);

    tblProductos.setFont(
            new Font(
                    "Segoe UI",
                    Font.PLAIN,
                    12
            )
    );

    tblProductos.getTableHeader().setFont(
            new Font(
                    "Segoe UI",
                    Font.BOLD,
                    11
            )
    );

    tblProductos.getTableHeader()
            .setPreferredSize(
                    new Dimension(0, 30)
            );

    DefaultTableCellRenderer centrado =
            new DefaultTableCellRenderer();

    centrado.setHorizontalAlignment(
            SwingConstants.CENTER
    );

    tblProductos.getColumnModel()
            .getColumn(0)
            .setCellRenderer(centrado);

    tblProductos.getColumnModel()
            .getColumn(1)
            .setPreferredWidth(110);

    tblProductos.getColumnModel()
            .getColumn(2)
            .setPreferredWidth(240);

    tblProductos.getColumnModel()
            .getColumn(3)
            .setPreferredWidth(150);

    tblProductos.getColumnModel()
            .getColumn(4)
            .setPreferredWidth(100);

    tblProductos.getColumnModel()
            .getColumn(5)
            .setPreferredWidth(100);

    tblProductos.getColumnModel()
            .getColumn(6)
            .setPreferredWidth(125);

    tblProductos.getColumnModel()
            .getColumn(7)
            .setPreferredWidth(100);

    tblProductos.getSelectionModel()
            .addListSelectionListener(
                    e -> {
                        if (!e.getValueIsAdjusting()) {
                            cargarProductoSeleccionado();
                        }
                    }
            );

    tblProductos.addMouseListener(
            new java.awt.event.MouseAdapter() {

                @Override
                public void mouseClicked(
                        java.awt.event.MouseEvent e
                ) {

                    if (e.getClickCount() == 2
                            && tblProductos.getSelectedRow() >= 0) {

                        cargarProductoSeleccionado();
                    }
                }
            }
    );

    return new JScrollPane(tblProductos);
}

private JPanel construirGestion() {

    JPanel panel =
            crearPanelSeccion(
                    "02. DATOS DEL PRODUCTO"
            );

    panel.setLayout(
            new GridBagLayout()
    );

    GridBagConstraints gbc =
            new GridBagConstraints();

    gbc.insets =
            new Insets(5, 6, 5, 6);

    gbc.fill =
            GridBagConstraints.HORIZONTAL;

    gbc.anchor =
            GridBagConstraints.WEST;

    // Fila 0
    agregarCampo(
            panel,
            gbc,
            0,
            0,
            "CÓDIGO",
            txtCodigo
    );

    agregarCampo(
            panel,
            gbc,
            2,
            0,
            "NOMBRE",
            txtNombre,
            3
    );

    // Fila 1
    agregarCampo(
            panel,
            gbc,
            0,
            1,
            "CATEGORÍA",
            cmbCategoria
    );

    agregarCampo(
            panel,
            gbc,
            2,
            1,
            "UNIDAD DE MEDIDA",
            cmbUnidadMedida,
            3
    );

    // Fila 2
    agregarCampo(
            panel,
            gbc,
            0,
            2,
            "STOCK MÍNIMO",
            txtStockMinimo
    );

    agregarCampo(
            panel,
            gbc,
            2,
            2,
            "PRECIO DE VENTA",
            txtPrecioVenta
    );

    // Estado
    JLabel lblEstado =
            crearEtiqueta(
                    "ESTADO"
            );

    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.weightx = 0;

    panel.add(
            lblEstado,
            gbc
    );

    txtEstado.setEditable(false);
    txtEstado.setFocusable(false);

    gbc.gridx = 1;
    gbc.weightx = 0.2;
    gbc.gridwidth = 1;

    panel.add(
            txtEstado,
            gbc
    );

    gbc.gridx = 2;
    gbc.weightx = 0;

    panel.add(
            lblModo,
            gbc
    );

    lblModo.setFont(
            new Font(
                    "Consolas",
                    Font.BOLD,
                    11
            )
    );

    gbc.gridx = 3;
    gbc.weightx = 1;
    gbc.gridwidth = 3;

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

    FlowLayout layout =
            new FlowLayout(
                    FlowLayout.CENTER,
                    8,
                    7
            );

    panel.setLayout(layout);

    JButton btnNuevo =
            crearBoton(
                    "Nuevo producto"
            );

    btnGuardar =
            crearBoton(
                    "Guardar"
            );

    btnActivar =
            crearBoton(
                    "Activar"
            );

    btnDesactivar =
            crearBoton(
                    "Desactivar"
            );

    JButton btnActualizar =
            crearBoton(
                    "Actualizar"
            );

    JButton btnCerrar =
            crearBoton(
                    "Cerrar"
            );

    panel.add(btnNuevo);
    panel.add(btnGuardar);
    panel.add(btnActivar);
    panel.add(btnDesactivar);
    panel.add(btnActualizar);
    panel.add(btnCerrar);

    btnNuevo.addActionListener(
            e -> prepararNuevoProducto()
    );

    btnGuardar.addActionListener(
            e -> guardarProducto()
    );

    btnActivar.addActionListener(
            e -> activarProducto()
    );

    btnDesactivar.addActionListener(
            e -> desactivarProducto()
    );

    btnActualizar.addActionListener(
            e -> {
                cargarProductos();
                prepararNuevoProducto();
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
            new JLabel(texto);

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
            new JButton(texto);

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
        int ancho
) {

    JLabel etiqueta =
            crearEtiqueta(texto);

    gbc.gridx = columna;
    gbc.gridy = fila;
    gbc.gridwidth = 1;
    gbc.weightx = 0;

    panel.add(
            etiqueta,
            gbc
    );

    gbc.gridx = columna + 1;
    gbc.gridwidth = ancho;
    gbc.weightx = 1;

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
    txtEstado.setText("SIN SELECCIÓN");
}

private void configurarCombos() {

    cmbCategoria.setRenderer(
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

                    if (value instanceof Categoria categoria) {
                        setText(
                                categoria.getNombre()
                        );
                    }

                    return this;
                }
            }
    );

    cmbUnidadMedida.setRenderer(
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

                    if (value instanceof UnidadMedida unidad) {

                        setText(
                                unidad.getNombre()
                                + " ("
                                + unidad.getAbreviatura()
                                + ")"
                        );
                    }

                    return this;
                }
            }
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
                    .format(formatoFecha)
    );
}

private void cargarCatalogos() {

    try {

        cmbCategoria.setModel(
                new DefaultComboBoxModel<>(
                        categoriaService
                                .listar()
                                .toArray(
                                        new Categoria[0]
                                )
                )
        );

        cmbUnidadMedida.setModel(
                new DefaultComboBoxModel<>(
                        unidadMedidaService
                                .listar()
                                .toArray(
                                        new UnidadMedida[0]
                                )
                )
        );

    } catch (Exception ex) {

        mostrarError(
                ex.getMessage() == null
                        ? "No se pudieron cargar los catálogos."
                        : ex.getMessage()
        );
    }
}

private void cargarProductos() {

    try {

        List<Producto> productos =
                productoService.listarActivos();

        cargarTabla(productos);

    } catch (Exception ex) {

        mostrarError(
                ex.getMessage() == null
                        ? "No se pudieron cargar los productos."
                        : ex.getMessage()
        );
    }
}

private void buscarProductos() {

    String texto =
            txtBuscar.getText() == null
                    ? ""
                    : txtBuscar.getText().trim();

    if (texto.isBlank()) {

        cargarProductos();
        return;
    }

    try {

        List<Producto> productos =
                productoService
                        .buscarActivosPorNombreOCodigoParcial(
                                texto
                        );

        if (productos.isEmpty()) {

            Producto producto =
                    productoService
                            .buscarPorCodigo(texto);

            if (producto != null) {

                productos =
                        java.util.Collections.singletonList(
                                producto
                        );
            }
        }

        cargarTabla(productos);

    } catch (Exception ex) {

        mostrarError(
                ex.getMessage() == null
                        ? "No se pudo realizar la búsqueda."
                        : ex.getMessage()
        );
    }
}

private void cargarTabla(
        List<Producto> productos
) {

    modeloTabla.setRowCount(0);

    if (productos == null) {
        return;
    }

    for (Producto producto : productos) {

        modeloTabla.addRow(
                new Object[]{
                    producto.getIdProducto(),
                    producto.getCodigo(),
                    producto.getNombre(),
                    obtenerNombreCategoria(
                            producto.getIdCategoria()
                    ),
                    obtenerNombreUnidad(
                            producto.getIdUnidadMedida()
                    ),
                    producto.getStockMinimo(),
                    formatearMoneda(
                            producto.getPrecioVenta()
                    ),
                    producto.isActivo()
                            ? "ACTIVO"
                            : "INACTIVO"
                }
        );
    }
}

private String obtenerNombreCategoria(
        int idCategoria
) {

    Categoria categoria =
            categoriaService.buscarPorId(
                    idCategoria
            );

    return categoria == null
            ? ""
            : categoria.getNombre();
}

private String obtenerNombreUnidad(
        int idUnidadMedida
) {

    UnidadMedida unidad =
            unidadMedidaService.buscarPorId(
                    idUnidadMedida
            );

    if (unidad == null) {
        return "";
    }

    return unidad.getNombre()
            + " ("
            + unidad.getAbreviatura()
            + ")";
}

private String formatearMoneda(
        BigDecimal valor
) {

    if (valor == null) {
        return "S/ 0.00";
    }

    return "S/ "
            + formatoMoneda.format(valor);
}

private void cargarProductoSeleccionado() {

    int filaVista =
            tblProductos.getSelectedRow();

    if (filaVista < 0) {
        return;
    }

    int filaModelo =
            tblProductos.convertRowIndexToModel(
                    filaVista
            );

    int idProducto =
            ((Number) modeloTabla.getValueAt(
                    filaModelo,
                    0
            )).intValue();

    Producto producto =
            productoService.buscarPorId(
                    idProducto
            );

    if (producto == null) {

        mostrarError(
                "No se pudo recuperar el producto seleccionado."
        );

        return;
    }

    productoSeleccionado = producto;

    txtCodigo.setText(
            producto.getCodigo()
    );

    txtNombre.setText(
            producto.getNombre()
    );

    seleccionarCategoria(
            producto.getIdCategoria()
    );

    seleccionarUnidad(
            producto.getIdUnidadMedida()
    );

    txtStockMinimo.setText(
            producto.getStockMinimo() == null
                    ? ""
                    : producto.getStockMinimo()
                            .toPlainString()
    );

    txtPrecioVenta.setText(
            producto.getPrecioVenta() == null
                    ? ""
                    : producto.getPrecioVenta()
                            .toPlainString()
    );

    txtEstado.setText(
            producto.isActivo()
                    ? "ACTIVO"
                    : "INACTIVO"
    );

    lblModo.setText(
            "EDITAR PRODUCTO"
    );

    lblMensaje.setText(
            "Modifique los datos y pulse Guardar."
    );

    txtCodigo.setEditable(true);

    btnGuardar.setEnabled(true);

    btnActivar.setEnabled(
            !producto.isActivo()
    );

    btnDesactivar.setEnabled(
            producto.isActivo()
    );
}

private void seleccionarCategoria(
        int idCategoria
) {

    for (int i = 0;
            i < cmbCategoria.getItemCount();
            i++) {

        Categoria categoria =
                cmbCategoria.getItemAt(i);

        if (categoria.getIdCategoria()
                == idCategoria) {

            cmbCategoria.setSelectedIndex(i);
            return;
        }
    }

    cmbCategoria.setSelectedItem(null);
}

private void seleccionarUnidad(
        int idUnidad
) {

    for (int i = 0;
            i < cmbUnidadMedida.getItemCount();
            i++) {

        UnidadMedida unidad =
                cmbUnidadMedida.getItemAt(i);

        if (unidad.getIdUnidadMedida()
                == idUnidad) {

            cmbUnidadMedida.setSelectedIndex(i);
            return;
        }
    }

    cmbUnidadMedida.setSelectedItem(null);
}

private void prepararNuevoProducto() {

    productoSeleccionado = null;

    tblProductos.clearSelection();

    txtCodigo.setText("");
    txtNombre.setText("");
    cmbCategoria.setSelectedItem(null);
    cmbUnidadMedida.setSelectedItem(null);
    txtStockMinimo.setText("");
    txtPrecioVenta.setText("");

    txtEstado.setText(
            "NUEVO"
    );

    lblModo.setText(
            "NUEVO PRODUCTO"
    );

    lblMensaje.setText(
            "Complete los datos y pulse Guardar."
    );

    txtCodigo.setEditable(true);

    btnGuardar.setEnabled(true);
    btnActivar.setEnabled(false);
    btnDesactivar.setEnabled(false);
}

private void guardarProducto() {

    String codigo =
            txtCodigo.getText()
                    .trim();

    String nombre =
            txtNombre.getText()
                    .trim();

    if (codigo.isBlank()) {

        mostrarAviso(
                "El código del producto es obligatorio."
        );

        txtCodigo.requestFocus();
        return;
    }

    if (nombre.isBlank()) {

        mostrarAviso(
                "El nombre del producto es obligatorio."
        );

        txtNombre.requestFocus();
        return;
    }

    Categoria categoria =
            (Categoria) cmbCategoria
                    .getSelectedItem();

    if (categoria == null) {

        mostrarAviso(
                "Seleccione una categoría."
        );

        cmbCategoria.requestFocus();
        return;
    }

    UnidadMedida unidad =
            (UnidadMedida) cmbUnidadMedida
                    .getSelectedItem();

    if (unidad == null) {

        mostrarAviso(
                "Seleccione una unidad de medida."
        );

        cmbUnidadMedida.requestFocus();
        return;
    }

    BigDecimal stockMinimo;

    try {

        stockMinimo =
                new BigDecimal(
                        txtStockMinimo
                                .getText()
                                .trim()
                );

    } catch (NumberFormatException ex) {

        mostrarAviso(
                "Ingrese un stock mínimo válido."
        );

        txtStockMinimo.requestFocus();
        return;
    }

    BigDecimal precioVenta;

    try {

        precioVenta =
                new BigDecimal(
                        txtPrecioVenta
                                .getText()
                                .trim()
                );

    } catch (NumberFormatException ex) {

        mostrarAviso(
                "Ingrese un precio de venta válido."
        );

        txtPrecioVenta.requestFocus();
        return;
    }

    if (stockMinimo.compareTo(
            BigDecimal.ZERO
    ) < 0) {

        mostrarAviso(
                "El stock mínimo no puede ser negativo."
        );

        return;
    }

    if (precioVenta.compareTo(
            BigDecimal.ZERO
    ) < 0) {

        mostrarAviso(
                "El precio de venta no puede ser negativo."
        );

        return;
    }

    RespuestaOperacion<Void> respuesta;

    boolean nuevo =
            productoSeleccionado == null;

    if (nuevo) {

        Producto producto =
                new Producto(
                        codigo,
                        nombre,
                        categoria.getIdCategoria(),
                        unidad.getIdUnidadMedida(),
                        stockMinimo,
                        precioVenta
                );

        respuesta =
                productoService.registrar(
                        producto
                );

    } else {

        productoSeleccionado.setCodigo(
                codigo
        );

        productoSeleccionado.setNombre(
                nombre
        );

        productoSeleccionado.setIdCategoria(
                categoria.getIdCategoria()
        );

        productoSeleccionado.setIdUnidadMedida(
                unidad.getIdUnidadMedida()
        );

        productoSeleccionado.setStockMinimo(
                stockMinimo
        );

        productoSeleccionado.setPrecioVenta(
                precioVenta
        );

        respuesta =
                productoService.actualizar(
                        productoSeleccionado
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
                    ? "Producto registrado correctamente."
                    : "Producto actualizado correctamente."
    );

    cargarProductos();
    prepararNuevoProducto();
}

private void activarProducto() {

    if (productoSeleccionado == null) {
        return;
    }

    int opcion =
            JOptionPane.showConfirmDialog(
                    this,
                    "¿Desea activar el producto seleccionado?",
                    "Confirmar activación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

    if (opcion != JOptionPane.YES_OPTION) {
        return;
    }

    RespuestaOperacion<Void> respuesta =
            productoService.activar(
                    productoSeleccionado
                            .getIdProducto()
            );

    if (!respuesta.isExito()) {

        mostrarError(
                respuesta.getMensaje()
        );

        return;
    }

    mostrarInformacion(
            "Producto activado correctamente."
    );

    cargarProductos();
    prepararNuevoProducto();
}

private void desactivarProducto() {

    if (productoSeleccionado == null) {
        return;
    }

    int opcion =
            JOptionPane.showConfirmDialog(
                    this,
                    "¿Desea desactivar el producto seleccionado?",
                    "Confirmar desactivación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

    if (opcion != JOptionPane.YES_OPTION) {
        return;
    }

    RespuestaOperacion<Void> respuesta =
            productoService.desactivar(
                    productoSeleccionado
                            .getIdProducto()
            );

    if (!respuesta.isExito()) {

        mostrarError(
                respuesta.getMensaje()
        );

        return;
    }

    mostrarInformacion(
            "Producto desactivado correctamente."
    );

    cargarProductos();
    prepararNuevoProducto();
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
            "Operación exitosa",
            JOptionPane.INFORMATION_MESSAGE
    );
}

public static void main(
        String[] args
) {

    javax.swing.SwingUtilities.invokeLater(
            () -> {

                try {

                    javax.swing.UIManager.setLookAndFeel(
                            javax.swing.UIManager
                                    .getSystemLookAndFeelClassName()
                    );

                } catch (Exception ignored) {
                }

                FrmGestionProductos frm =
                        new FrmGestionProductos();

                frm.setVisible(true);
            }
    );
}

}
