package com.ferronor.sic.inventario.vista;

import com.ferronor.sic.inventario.logica.InventarioService;
import com.ferronor.sic.inventario.modelo.dto.StockConsulta;
import com.ferronor.sic.maestros.logica.ProductoService;
import com.ferronor.sic.maestros.modelo.Producto;
import com.ferronor.sic.shared.ui.ComboAutoFiltro;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

/**
 * Consulta de existencias actuales del inventario.
 */
public class FrmConsultarStock extends javax.swing.JDialog {

    private final InventarioService inventarioService
            = ServiceFactory.inventarioService();

    private final ProductoService productoService
            = ServiceFactory.productoService();

    private DefaultTableModel modeloExistencias;

    private Timer timerFechaHora;

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter FORMATO_HORA
            = DateTimeFormatter.ofPattern("HH:mm:ss");

    public FrmConsultarStock(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        configurarComponentes();
    }

    // ============================================================
    // CONFIGURACIÓN
    // ============================================================
    private void configurarComponentes() {

        configurarSesion();
        configurarComboProductos();
        configurarTablaExistencias();
        configurarCamposSoloLectura();
        configurarEstadoInicial();
        configurarListeners();
        iniciarReloj();

        cargarExistencias();

        setLocationRelativeTo(getParent());

    }

    private void configurarSesion() {

        if (!SesionUsuario.haySesion()) {

            JOptionPane.showMessageDialog(
                    this,
                    "No existe una sesión de usuario activa.",
                    "Sesión",
                    JOptionPane.WARNING_MESSAGE
            );

            dispose();
            return;
        }

        actualizarInformacionSesion();
    }

    private void actualizarInformacionSesion() {

        SesionUsuario sesion
                = SesionUsuario.actual();

        lblNombreApellidoUsuario.setText(
                sesion.getNombreCompleto()
        );

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
    // COMBO PRODUCTOS
    // ============================================================
    private void configurarComboProductos() {

        cmbProductos.setModel(
                new DefaultComboBoxModel<>()
        );

        cmbProductos.setRenderer(
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

                if (value instanceof Producto producto) {

                    setText(
                            producto.getCodigo()
                            + " — "
                            + producto.getNombre()
                    );
                }

                return this;
            }
        }
        );

        ComboAutoFiltro.mejorarCombo(
                cmbProductos,
                texto -> {

                    if (texto == null || texto.isBlank()) {
                        return productoService.listarActivos();
                    }

                    return productoService
                            .buscarActivosPorNombreOCodigoParcial(texto);
                }
        );
    }

    // ============================================================
    // TABLA
    // ============================================================
    private void configurarTablaExistencias() {

        modeloExistencias
                = new DefaultTableModel(
                        new Object[][]{},
                        new String[]{
                            "CÓDIGO",
                            "PRODUCTO",
                            "UNIDAD",
                            "STOCK ACT.",
                            "STOCK MÍN.",
                            "COSTO PROM.",
                            "ÚLTIMA ACTUALIZACIÓN",
                            "ESTADO"
                        }
                ) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };

        tblExistencias.setModel(
                modeloExistencias
        );

        tblExistencias.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        9
                )
        );

        tblExistencias.setRowHeight(28);

        tblExistencias.setAutoResizeMode(
                JTable.AUTO_RESIZE_LAST_COLUMN
        );

        tblExistencias.setSelectionMode(
                javax.swing.ListSelectionModel.SINGLE_SELECTION
        );

        configurarAnchosColumnas();

        configurarRenderersTabla();
    }

    private void configurarAnchosColumnas() {

        TableColumnModel columnas
                = tblExistencias.getColumnModel();

        columnas.getColumn(0).setPreferredWidth(80);
        columnas.getColumn(0).setMinWidth(65);

        columnas.getColumn(1).setPreferredWidth(220);
        columnas.getColumn(1).setMinWidth(160);

        columnas.getColumn(2).setPreferredWidth(70);
        columnas.getColumn(2).setMinWidth(55);

        columnas.getColumn(3).setPreferredWidth(85);
        columnas.getColumn(3).setMinWidth(75);

        columnas.getColumn(4).setPreferredWidth(85);
        columnas.getColumn(4).setMinWidth(75);

        columnas.getColumn(5).setPreferredWidth(95);
        columnas.getColumn(5).setMinWidth(80);

        columnas.getColumn(6).setPreferredWidth(145);
        columnas.getColumn(6).setMinWidth(125);

        columnas.getColumn(7).setPreferredWidth(90);
        columnas.getColumn(7).setMinWidth(85);
    }

    private void configurarRenderersTabla() {

        DefaultTableCellRenderer rendererGeneral
                = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {

                Component componente
                        = super.getTableCellRendererComponent(
                                table,
                                value,
                                isSelected,
                                hasFocus,
                                row,
                                column
                        );

                setFont(
                        new Font(
                                "Segoe UI",
                                Font.PLAIN,
                                9
                        )
                );

                return componente;
            }
        };

        for (int i = 0;
                i < tblExistencias.getColumnCount();
                i++) {

            tblExistencias
                    .getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(rendererGeneral);
        }

        // Código
        tblExistencias
                .getColumnModel()
                .getColumn(0)
                .setCellRenderer(
                        crearRendererMonoespaciado(false)
                );

        // Producto
        tblExistencias
                .getColumnModel()
                .getColumn(1)
                .setCellRenderer(
                        crearRendererProducto()
                );

        // Unidad
        tblExistencias
                .getColumnModel()
                .getColumn(2)
                .setCellRenderer(
                        crearRendererCentrado()
                );

        // Cantidades
        tblExistencias
                .getColumnModel()
                .getColumn(3)
                .setCellRenderer(
                        crearRendererMonoespaciado(true)
                );

        tblExistencias
                .getColumnModel()
                .getColumn(4)
                .setCellRenderer(
                        crearRendererMonoespaciado(true)
                );

        // Costo
        tblExistencias
                .getColumnModel()
                .getColumn(5)
                .setCellRenderer(
                        crearRendererMonoespaciado(true)
                );

        // Fecha
        tblExistencias
                .getColumnModel()
                .getColumn(6)
                .setCellRenderer(
                        crearRendererMonoespaciado(false)
                );

        // Estado
        tblExistencias
                .getColumnModel()
                .getColumn(7)
                .setCellRenderer(
                        new RendererEstado()
                );
    }

    private DefaultTableCellRenderer crearRendererMonoespaciado(
            boolean derecha) {

        return new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {

                super.getTableCellRendererComponent(
                        table,
                        value,
                        isSelected,
                        hasFocus,
                        row,
                        column
                );

                setFont(
                        new Font(
                                "Consolas",
                                Font.PLAIN,
                                9
                        )
                );

                setHorizontalAlignment(
                        derecha
                                ? JLabel.RIGHT
                                : JLabel.LEFT
                );

                return this;
            }
        };
    }

    private DefaultTableCellRenderer crearRendererCentrado() {

        return new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {

                super.getTableCellRendererComponent(
                        table,
                        value,
                        isSelected,
                        hasFocus,
                        row,
                        column
                );

                setFont(
                        new Font(
                                "Segoe UI",
                                Font.PLAIN,
                                9
                        )
                );

                setHorizontalAlignment(
                        JLabel.CENTER
                );

                return this;
            }
        };
    }

    private DefaultTableCellRenderer crearRendererProducto() {

        return new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {

                super.getTableCellRendererComponent(
                        table,
                        value,
                        isSelected,
                        hasFocus,
                        row,
                        column
                );

                setFont(
                        new Font(
                                "Segoe UI",
                                Font.PLAIN,
                                9
                        )
                );

                return this;
            }
        };
    }

    // ============================================================
    // ESTADO INICIAL DEL PANEL
    // ============================================================
    private void configurarEstadoInicial() {

        mostrarProductoNoConsultado();
    }

    private void mostrarProductoNoConsultado() {

        txtInicialesProducto.setText("—");

        lblNombreProducto.setText(
                "Seleccione un producto"
        );

        lblIdProducto.setText(
                "—"
        );

        lblUnidad.setText(
                "Unidad: —"
        );

        lblCategoria.setText(
                "Categoría: —"
        );

        txtEstadoProducto.setText(
                "• Sin consulta"
        );

        lblCantidadStockProdSeleccionado.setText(
                "—"
        );

        lblUnidadProdSeleccionado.setText(
                ""
        );

        lblCantStockActual.setText(
                "—"
        );

        lblUnidadStockActual.setText(
                ""
        );

        lblValorCostoPromedioActual.setText(
                "—"
        );

        lblFechaYHoraUltimaActualizacion.setText(
                "—"
        );
    }

    // ============================================================
    // LISTENERS
    // ============================================================
    private void configurarListeners() {

        btnConsultar.addActionListener(
                e -> consultarProducto()
        );

        btnLimpiar.addActionListener(
                e -> limpiarConsulta()
        );
    }

    // ============================================================
    // CONSULTA GENERAL
    // ============================================================
    private void cargarExistencias() {

        try {

            List<StockConsulta> existencias
                    = inventarioService.consultarStock();

            cargarTabla(existencias);

        } catch (RuntimeException ex) {

            limpiarTabla();

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudieron consultar las existencias.\n"
                    + obtenerMensajeError(ex),
                    "Error de consulta",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cargarTabla(
            List<StockConsulta> existencias) {

        modeloExistencias.setRowCount(0);

        for (StockConsulta stock : existencias) {

            modeloExistencias.addRow(
                    new Object[]{
                        stock.getCodigoProducto(),
                        stock.getNombreProducto(),
                        stock.getAbreviaturaUnidad(),
                        formatearCantidad(
                                stock.getCantidadActual()
                        ),
                        formatearCantidad(
                                stock.getStockMinimo()
                        ),
                        formatearMoneda(
                                stock.getCostoPromedioActual()
                        ),
                        formatearFechaHora(
                                stock.getFechaUltimaActualizacion()
                        ),
                        obtenerEstadoStock(stock)
                    }
            );
        }
    }

    private void limpiarTabla() {

        modeloExistencias.setRowCount(0);
    }

    // ============================================================
    // CONSULTA DE PRODUCTO
    // ============================================================
    private void consultarProducto() {

        Object seleccionado
                = cmbProductos.getSelectedItem();

        if (!(seleccionado instanceof Producto producto)) {

            mostrarProductoNoEncontrado();
            return;
        }

        try {

            StockConsulta consulta
                    = inventarioService
                            .consultarStockPorProducto(
                                    producto.getIdProducto()
                            );

            if (consulta == null) {

                mostrarProductoNoEncontrado();
                return;
            }

            mostrarProductoEncontrado(
                    consulta
            );

            cargarTablaProducto(
                    consulta
            );

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo consultar el stock del producto.\n"
                    + obtenerMensajeError(ex),
                    "Error de consulta",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cargarTablaProducto(
            StockConsulta consulta) {

        modeloExistencias.setRowCount(0);

        modeloExistencias.addRow(
                new Object[]{
                    consulta.getCodigoProducto(),
                    consulta.getNombreProducto(),
                    consulta.getAbreviaturaUnidad(),
                    formatearCantidad(
                            consulta.getCantidadActual()
                    ),
                    formatearCantidad(
                            consulta.getStockMinimo()
                    ),
                    formatearMoneda(
                            consulta.getCostoPromedioActual()
                    ),
                    formatearFechaHora(
                            consulta.getFechaUltimaActualizacion()
                    ),
                    obtenerEstadoStock(consulta)
                }
        );
    }

    // ============================================================
    // PRODUCTO ENCONTRADO
    // ============================================================
    private void mostrarProductoEncontrado(
            StockConsulta consulta) {

        txtInicialesProducto.setText(
                obtenerIniciales(
                        consulta.getNombreProducto()
                )
        );

        lblNombreProducto.setText(
                consulta.getNombreProducto()
        );

        lblIdProducto.setText(
                consulta.getCodigoProducto()
        );

        lblUnidad.setText(
                "Unidad: "
                + valorTexto(
                        consulta.getAbreviaturaUnidad()
                )
        );

        lblCategoria.setText(
                "Categoría: "
                + valorTexto(
                        consulta.getNombreCategoria()
                )
        );

        txtEstadoProducto.setText(
                "• ACTIVO"
        );

        String cantidad
                = formatearCantidad(
                        consulta.getCantidadActual()
                );

        String unidad
                = valorTexto(
                        consulta.getAbreviaturaUnidad()
                );

        lblCantidadStockProdSeleccionado.setText(
                cantidad
        );

        lblUnidadProdSeleccionado.setText(
                unidad
        );

        lblCantStockActual.setText(
                cantidad
        );

        lblUnidadStockActual.setText(
                unidad
        );

        lblValorCostoPromedioActual.setText(
                formatearMoneda(
                        consulta.getCostoPromedioActual()
                )
        );

        lblFechaYHoraUltimaActualizacion.setText(
                formatearFechaHora(
                        consulta.getFechaUltimaActualizacion()
                )
        );
    }

    // ============================================================
    // PRODUCTO NO ENCONTRADO / SIN CONSULTA
    // ============================================================
    private void mostrarProductoNoEncontrado() {

        txtInicialesProducto.setText(
                "?"
        );

        lblNombreProducto.setText(
                "Producto no encontrado"
        );

        lblIdProducto.setText(
                "—"
        );

        lblUnidad.setText(
                "Unidad: —"
        );

        lblCategoria.setText(
                "Categoría: —"
        );

        txtEstadoProducto.setText(
                "• No encontrado"
        );

        lblCantidadStockProdSeleccionado.setText(
                "—"
        );

        lblUnidadProdSeleccionado.setText(
                ""
        );

        lblCantStockActual.setText(
                "—"
        );

        lblUnidadStockActual.setText(
                ""
        );

        lblValorCostoPromedioActual.setText(
                "—"
        );

        lblFechaYHoraUltimaActualizacion.setText(
                "—"
        );

        limpiarTabla();
    }

    // ============================================================
    // LIMPIAR
    // ============================================================
    private void limpiarConsulta() {

        cmbProductos.setSelectedItem(null);

        if (cmbProductos.isEditable()) {

            Component editor
                    = cmbProductos.getEditor()
                            .getEditorComponent();

            if (editor instanceof javax.swing.text.JTextComponent textComponent) {
                textComponent.setText("");
            }
        }

        mostrarProductoNoConsultado();

        cargarExistencias();
    }

    // ============================================================
    // ESTADO DE STOCK
    // ============================================================
    private String obtenerEstadoStock(
            StockConsulta stock) {

        BigDecimal actual
                = stock.getCantidadActual();

        BigDecimal minimo
                = stock.getStockMinimo();

        if (actual == null
                || actual.compareTo(BigDecimal.ZERO) == 0) {

            return "SIN STOCK";
        }

        if (minimo != null
                && actual.compareTo(minimo) <= 0) {

            return "BAJO";
        }

        return "NORMAL";
    }

    // ============================================================
    // FORMATEOS
    // ============================================================
    private String formatearCantidad(
            BigDecimal cantidad) {

        if (cantidad == null) {
            return "—";
        }

        return cantidad
                .stripTrailingZeros()
                .toPlainString();
    }

    private String formatearMoneda(
            BigDecimal valor) {

        if (valor == null) {
            return "—";
        }

        return "S/ "
                + valor.setScale(
                        2,
                        java.math.RoundingMode.HALF_UP
                ).toPlainString();
    }

    private String formatearFechaHora(
            LocalDateTime fecha) {

        if (fecha == null) {
            return "—";
        }

        return fecha.format(
                FORMATO_FECHA_HORA
        );
    }

    private String valorTexto(
            String texto) {

        return texto == null
                || texto.isBlank()
                ? "—"
                : texto;
    }

    private String obtenerIniciales(
            String nombre) {

        if (nombre == null
                || nombre.isBlank()) {

            return "?";
        }

        String[] partes
                = nombre.trim()
                        .split("\\s+");

        if (partes.length == 1) {

            return partes[0]
                    .substring(
                            0,
                            Math.min(
                                    2,
                                    partes[0].length()
                            )
                    )
                    .toUpperCase();
        }

        return (partes[0].substring(0, 1)
                + partes[1].substring(0, 1)).toUpperCase();
    }

    private String obtenerMensajeError(
            RuntimeException ex) {

        if (ex.getMessage() == null
                || ex.getMessage().isBlank()) {

            return "Ocurrió un error inesperado.";
        }

        return ex.getMessage();
    }

    // ============================================================
    // CAMPOS SOLO LECTURA
    // ============================================================
    private void configurarCamposSoloLectura() {

        txtInicialesProducto.setEditable(false);

        txtEstadoProducto.setEditable(false);
    }

    // ============================================================
    // RELOJ
    // ============================================================
    private void iniciarReloj() {

        timerFechaHora
                = new Timer(
                        1000,
                        e -> actualizarInformacionSesion()
                );

        timerFechaHora.start();
    }

    // ============================================================
    // RENDERER DE ESTADO
    // ============================================================
    private static class RendererEstado
            extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            JLabel label
                    = (JLabel) super.getTableCellRendererComponent(
                            table,
                            value,
                            isSelected,
                            hasFocus,
                            row,
                            column
                    );

            label.setHorizontalAlignment(
                    JLabel.CENTER
            );

            label.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            9
                    )
            );

            if (!isSelected) {

                String estado
                        = value != null
                                ? value.toString()
                                : "";

                switch (estado) {

                    case "SIN STOCK" -> {
                        label.setForeground(
                                new Color(
                                        176,
                                        58,
                                        46
                                )
                        );
                    }

                    case "BAJO" -> {
                        label.setForeground(
                                new Color(
                                        138,
                                        90,
                                        18
                                )
                        );
                    }

                    case "NORMAL" -> {
                        label.setForeground(
                                new Color(
                                        60,
                                        107,
                                        58
                                )
                        );
                    }

                    default -> {
                        label.setForeground(
                                Color.DARK_GRAY
                        );
                    }
                }
            }

            return label;
        }
    }

    // ============================================================
    // DISPOSE
    // ============================================================
    @Override
    public void dispose() {

        if (timerFechaHora != null) {
            timerFechaHora.stop();
        }

        super.dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSuperior = new javax.swing.JPanel();
        lblConsultaDeStock = new javax.swing.JLabel();
        lblConsultaExistenciasActualesInventario = new javax.swing.JLabel();
        lblNombreApellidoUsuario = new javax.swing.JLabel();
        lblFechaActual = new javax.swing.JLabel();
        lblHoraActual = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        pnlConsultaProducto = new javax.swing.JPanel();
        lblBuscarProducto = new javax.swing.JLabel();
        cmbProductos = new javax.swing.JComboBox<>();
        btnConsultar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        pnlProductoSeleccionado = new javax.swing.JPanel();
        pnlEstadisticasProdSeleccionado = new javax.swing.JPanel();
        lblNombreProducto = new javax.swing.JLabel();
        lblStockActualProdSeleccionado = new javax.swing.JLabel();
        lblCantidadStockProdSeleccionado = new javax.swing.JLabel();
        lblUnidadProdSeleccionado = new javax.swing.JLabel();
        lblIdProducto = new javax.swing.JLabel();
        lblUnidad = new javax.swing.JLabel();
        lblCategoria = new javax.swing.JLabel();
        txtEstadoProducto = new javax.swing.JTextField();
        pnlIniciales = new javax.swing.JPanel();
        txtInicialesProducto = new javax.swing.JTextField();
        pnlResumenProdSeleccionado = new javax.swing.JPanel();
        lblStockActual = new javax.swing.JLabel();
        lblCantStockActual = new javax.swing.JLabel();
        lblUnidadStockActual = new javax.swing.JLabel();
        lblCostoPromedioActual = new javax.swing.JLabel();
        lblValorCostoPromedioActual = new javax.swing.JLabel();
        lblUltimaActualizacion = new javax.swing.JLabel();
        lblFechaYHoraUltimaActualizacion = new javax.swing.JLabel();
        pnlExistencias = new javax.swing.JPanel();
        spnlTblExistencias = new javax.swing.JScrollPane();
        tblExistencias = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblConsultaDeStock.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblConsultaDeStock.setText("CONSULTA DE STOCK");

        lblConsultaExistenciasActualesInventario.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblConsultaExistenciasActualesInventario.setText("Consulta de existencias actuales del inventario ");

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
                    .addComponent(lblConsultaExistenciasActualesInventario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblConsultaDeStock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(lblConsultaDeStock)
                    .addComponent(lblNombreApellidoUsuario)
                    .addComponent(lblUsuario))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblConsultaExistenciasActualesInventario, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFechaActual)
                    .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        pnlConsultaProducto.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(41, 43, 45)), "01. CONSULTA DE PRODUCTO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblBuscarProducto.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblBuscarProducto.setText("BUSCAR PRODUCTO");

        cmbProductos.setName(""); // NOI18N

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

        javax.swing.GroupLayout pnlConsultaProductoLayout = new javax.swing.GroupLayout(pnlConsultaProducto);
        pnlConsultaProducto.setLayout(pnlConsultaProductoLayout);
        pnlConsultaProductoLayout.setHorizontalGroup(
            pnlConsultaProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsultaProductoLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(pnlConsultaProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlConsultaProductoLayout.createSequentialGroup()
                        .addComponent(lblBuscarProducto)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(pnlConsultaProductoLayout.createSequentialGroup()
                        .addComponent(cmbProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnConsultar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnLimpiar)))
                .addContainerGap())
        );
        pnlConsultaProductoLayout.setVerticalGroup(
            pnlConsultaProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsultaProductoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlConsultaProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlConsultaProductoLayout.createSequentialGroup()
                        .addComponent(lblBuscarProducto)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlConsultaProductoLayout.createSequentialGroup()
                        .addGroup(pnlConsultaProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnConsultar)
                            .addComponent(btnLimpiar))
                        .addGap(1, 1, 1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlProductoSeleccionado.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(41, 43, 45)), "  PRODUCTO SELECCIONADO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        pnlEstadisticasProdSeleccionado.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblNombreProducto.setText("Porcelanato Beige 60×60 ");

        lblStockActualProdSeleccionado.setText("STOCK ACTUAL:");

        lblCantidadStockProdSeleccionado.setFont(new java.awt.Font("Consolas", 0, 24)); // NOI18N
        lblCantidadStockProdSeleccionado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCantidadStockProdSeleccionado.setText("350");
        lblCantidadStockProdSeleccionado.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        lblUnidadProdSeleccionado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblUnidadProdSeleccionado.setText("CAJA");

        lblIdProducto.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblIdProducto.setText("P-00231");

        lblUnidad.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblUnidad.setText("Unidad: CAJA");

        lblCategoria.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCategoria.setText("Categoría: Pisos y revestimientos ");

        txtEstadoProducto.setEditable(false);
        txtEstadoProducto.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        txtEstadoProducto.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtEstadoProducto.setText("• Activo");

        txtInicialesProducto.setEditable(false);
        txtInicialesProducto.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtInicialesProducto.setText("PB");

        javax.swing.GroupLayout pnlInicialesLayout = new javax.swing.GroupLayout(pnlIniciales);
        pnlIniciales.setLayout(pnlInicialesLayout);
        pnlInicialesLayout.setHorizontalGroup(
            pnlInicialesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInicialesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtInicialesProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlInicialesLayout.setVerticalGroup(
            pnlInicialesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInicialesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtInicialesProducto))
        );

        javax.swing.GroupLayout pnlEstadisticasProdSeleccionadoLayout = new javax.swing.GroupLayout(pnlEstadisticasProdSeleccionado);
        pnlEstadisticasProdSeleccionado.setLayout(pnlEstadisticasProdSeleccionadoLayout);
        pnlEstadisticasProdSeleccionadoLayout.setHorizontalGroup(
            pnlEstadisticasProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEstadisticasProdSeleccionadoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlIniciales, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEstadisticasProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlEstadisticasProdSeleccionadoLayout.createSequentialGroup()
                        .addComponent(lblNombreProducto)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblStockActualProdSeleccionado))
                    .addGroup(pnlEstadisticasProdSeleccionadoLayout.createSequentialGroup()
                        .addGroup(pnlEstadisticasProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlEstadisticasProdSeleccionadoLayout.createSequentialGroup()
                                .addComponent(lblUnidad, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblCategoria))
                            .addComponent(lblIdProducto))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtEstadoProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addComponent(lblCantidadStockProdSeleccionado, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(lblUnidadProdSeleccionado, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(26, 26, 26))
        );
        pnlEstadisticasProdSeleccionadoLayout.setVerticalGroup(
            pnlEstadisticasProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEstadisticasProdSeleccionadoLayout.createSequentialGroup()
                .addGroup(pnlEstadisticasProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEstadisticasProdSeleccionadoLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlEstadisticasProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEstadisticasProdSeleccionadoLayout.createSequentialGroup()
                                .addGroup(pnlEstadisticasProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblNombreProducto)
                                    .addComponent(lblStockActualProdSeleccionado))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlEstadisticasProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnlEstadisticasProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblCantidadStockProdSeleccionado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblUnidadProdSeleccionado))
                                    .addGroup(pnlEstadisticasProdSeleccionadoLayout.createSequentialGroup()
                                        .addComponent(lblIdProducto)
                                        .addGap(6, 6, 6)
                                        .addGroup(pnlEstadisticasProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(lblCategoria)
                                            .addComponent(lblUnidad))
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEstadisticasProdSeleccionadoLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(txtEstadoProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(pnlIniciales, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pnlResumenProdSeleccionado.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblStockActual.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblStockActual.setText("STOCK ACTUAL");

        lblCantStockActual.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblCantStockActual.setText("350.00");

        lblUnidadStockActual.setText("CAJA");

        lblCostoPromedioActual.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCostoPromedioActual.setText("COSTO PROMEDIO ACTUAL");

        lblValorCostoPromedioActual.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorCostoPromedioActual.setText("S/ 48.50");

        lblUltimaActualizacion.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblUltimaActualizacion.setText("ULTIMA ACTUALIZACIÓN");

        lblFechaYHoraUltimaActualizacion.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        lblFechaYHoraUltimaActualizacion.setText("21/08/2026 — 10:42 ");

        javax.swing.GroupLayout pnlResumenProdSeleccionadoLayout = new javax.swing.GroupLayout(pnlResumenProdSeleccionado);
        pnlResumenProdSeleccionado.setLayout(pnlResumenProdSeleccionadoLayout);
        pnlResumenProdSeleccionadoLayout.setHorizontalGroup(
            pnlResumenProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenProdSeleccionadoLayout.createSequentialGroup()
                .addGap(75, 75, 75)
                .addGroup(pnlResumenProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblCantStockActual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblStockActual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblUnidadStockActual)
                .addGap(63, 63, 63)
                .addGroup(pnlResumenProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblCostoPromedioActual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblValorCostoPromedioActual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(63, 63, 63)
                .addGroup(pnlResumenProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUltimaActualizacion)
                    .addComponent(lblFechaYHoraUltimaActualizacion))
                .addContainerGap(75, Short.MAX_VALUE))
        );
        pnlResumenProdSeleccionadoLayout.setVerticalGroup(
            pnlResumenProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenProdSeleccionadoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlResumenProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUltimaActualizacion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlResumenProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblStockActual)
                        .addComponent(lblCostoPromedioActual)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlResumenProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCantStockActual, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblUnidadStockActual)
                    .addComponent(lblValorCostoPromedioActual)
                    .addComponent(lblFechaYHoraUltimaActualizacion))
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlProductoSeleccionadoLayout = new javax.swing.GroupLayout(pnlProductoSeleccionado);
        pnlProductoSeleccionado.setLayout(pnlProductoSeleccionadoLayout);
        pnlProductoSeleccionadoLayout.setHorizontalGroup(
            pnlProductoSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlProductoSeleccionadoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProductoSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlResumenProdSeleccionado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlEstadisticasProdSeleccionado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlProductoSeleccionadoLayout.setVerticalGroup(
            pnlProductoSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductoSeleccionadoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlEstadisticasProdSeleccionado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResumenProdSeleccionado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlExistencias.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(41, 43, 45)), "02. EXISTENCIAS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        tblExistencias.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        tblExistencias.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "CÓDIGO", "PRODUCTO", "UNIDAD", "STOCK ACT.", "STOCK MÍN.", "COSTO PROM.", "ULTIMA ACTUALIZACION", "ESTADO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlTblExistencias.setViewportView(tblExistencias);

        javax.swing.GroupLayout pnlExistenciasLayout = new javax.swing.GroupLayout(pnlExistencias);
        pnlExistencias.setLayout(pnlExistenciasLayout);
        pnlExistenciasLayout.setHorizontalGroup(
            pnlExistenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExistenciasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlTblExistencias)
                .addContainerGap())
        );
        pnlExistenciasLayout.setVerticalGroup(
            pnlExistenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExistenciasLayout.createSequentialGroup()
                .addComponent(spnlTblExistencias, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 6, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSuperior, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlConsultaProducto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlProductoSeleccionado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlExistencias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlConsultaProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlProductoSeleccionado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlExistencias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        // TODO add your handling code here:
        consultarProducto();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
        limpiarConsulta();
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
            java.util.logging.Logger.getLogger(FrmConsultarStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmConsultarStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmConsultarStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmConsultarStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmConsultarStock dialog = new FrmConsultarStock(new javax.swing.JFrame(), true);
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
    private javax.swing.JComboBox<Producto> cmbProductos;
    private javax.swing.JLabel lblBuscarProducto;
    private javax.swing.JLabel lblCantStockActual;
    private javax.swing.JLabel lblCantidadStockProdSeleccionado;
    private javax.swing.JLabel lblCategoria;
    private javax.swing.JLabel lblConsultaDeStock;
    private javax.swing.JLabel lblConsultaExistenciasActualesInventario;
    private javax.swing.JLabel lblCostoPromedioActual;
    private javax.swing.JLabel lblFechaActual;
    private javax.swing.JLabel lblFechaYHoraUltimaActualizacion;
    private javax.swing.JLabel lblHoraActual;
    private javax.swing.JLabel lblIdProducto;
    private javax.swing.JLabel lblNombreApellidoUsuario;
    private javax.swing.JLabel lblNombreProducto;
    private javax.swing.JLabel lblStockActual;
    private javax.swing.JLabel lblStockActualProdSeleccionado;
    private javax.swing.JLabel lblUltimaActualizacion;
    private javax.swing.JLabel lblUnidad;
    private javax.swing.JLabel lblUnidadProdSeleccionado;
    private javax.swing.JLabel lblUnidadStockActual;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblValorCostoPromedioActual;
    private javax.swing.JPanel pnlConsultaProducto;
    private javax.swing.JPanel pnlEstadisticasProdSeleccionado;
    private javax.swing.JPanel pnlExistencias;
    private javax.swing.JPanel pnlIniciales;
    private javax.swing.JPanel pnlProductoSeleccionado;
    private javax.swing.JPanel pnlResumenProdSeleccionado;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JScrollPane spnlTblExistencias;
    private javax.swing.JTable tblExistencias;
    private javax.swing.JTextField txtEstadoProducto;
    private javax.swing.JTextField txtInicialesProducto;
    // End of variables declaration//GEN-END:variables
}
