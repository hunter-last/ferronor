package com.ferronor.sic.inventario.vista;

import com.ferronor.sic.inventario.logica.KardexService;
import com.ferronor.sic.inventario.modelo.OrigenMovimiento;
import com.ferronor.sic.inventario.modelo.TipoMovimiento;
import com.ferronor.sic.inventario.modelo.dto.KardexItem;
import com.ferronor.sic.maestros.logica.CategoriaService;
import com.ferronor.sic.maestros.logica.ProductoService;
import com.ferronor.sic.maestros.logica.UnidadMedidaService;
import com.ferronor.sic.maestros.modelo.Categoria;
import com.ferronor.sic.maestros.modelo.Producto;
import com.ferronor.sic.maestros.modelo.UnidadMedida;
import com.ferronor.sic.shared.ui.ComboAutoFiltro;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
 * Consulta de Kardex de un producto en un rango de fechas.
 */
public class FrmKardex extends javax.swing.JDialog {

    private final KardexService kardexService
            = ServiceFactory.kardexService();

    private final ProductoService productoService
            = ServiceFactory.productoService();

    private final CategoriaService categoriaService
            = ServiceFactory.categoriaService();

    private final UnidadMedidaService unidadMedidaService
            = ServiceFactory.unidadMedidaService();

    private Producto productoSeleccionado;

    private List<KardexItem> kardexActual
            = new ArrayList<>();

    private DefaultTableModel modeloKardex;

    private Timer timerFechaHora;

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter FORMATO_HORA
            = DateTimeFormatter.ofPattern("HH:mm:ss");

    public FrmKardex(
            java.awt.Frame parent,
            boolean modal) {

        super(parent, modal);

        initComponents();

        configurarComponentes();
    }

    // ============================================================
    // CONFIGURACIÓN GENERAL
    // ============================================================
    private void configurarComponentes() {

        configurarSesion();

        configurarComboProductos();

        configurarTablaKardex();

        configurarCamposIniciales();

        configurarListeners();

        iniciarReloj();
    }

    // ============================================================
    // SESIÓN
    // ============================================================
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
                            .buscarActivosPorNombreOCodigoParcial(
                                    texto
                            );
                }
        );
    }

    // ============================================================
    // TABLA KARDEX
    // ============================================================
    private void configurarTablaKardex() {

        modeloKardex
                = new DefaultTableModel(
                        new Object[][]{},
                        new String[]{
                            "FECHA",
                            "TIPO",
                            "ORIGEN",
                            "DOCUMENTO",
                            "ENTRADA",
                            "SALIDA",
                            "SALDO",
                            "COSTO UNIT.",
                            "SALDO VALORIZADO"
                        }
                ) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };

        tblKardex.setModel(
                modeloKardex
        );

        /*
         * Ajuste visual solicitado:
         * tabla compacta para evitar saturación.
         */
        tblKardex.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        9
                )
        );

        tblKardex.setRowHeight(27);

        tblKardex.setAutoResizeMode(
                JTable.AUTO_RESIZE_LAST_COLUMN
        );

        tblKardex.setSelectionMode(
                javax.swing.ListSelectionModel.SINGLE_SELECTION
        );

        configurarAnchosColumnas();

        configurarRenderers();
    }

    private void configurarAnchosColumnas() {

        TableColumnModel columnas
                = tblKardex.getColumnModel();

        columnas.getColumn(0).setPreferredWidth(125);
        columnas.getColumn(0).setMinWidth(105);

        columnas.getColumn(1).setPreferredWidth(82);
        columnas.getColumn(1).setMinWidth(70);

        columnas.getColumn(2).setPreferredWidth(135);
        columnas.getColumn(2).setMinWidth(110);

        columnas.getColumn(3).setPreferredWidth(80);
        columnas.getColumn(3).setMinWidth(65);

        columnas.getColumn(4).setPreferredWidth(90);
        columnas.getColumn(4).setMinWidth(75);

        columnas.getColumn(5).setPreferredWidth(90);
        columnas.getColumn(5).setMinWidth(75);

        columnas.getColumn(6).setPreferredWidth(90);
        columnas.getColumn(6).setMinWidth(75);

        columnas.getColumn(7).setPreferredWidth(105);
        columnas.getColumn(7).setMinWidth(90);

        columnas.getColumn(8).setPreferredWidth(130);
        columnas.getColumn(8).setMinWidth(110);
    }

    private void configurarRenderers() {

        tblKardex
                .getColumnModel()
                .getColumn(0)
                .setCellRenderer(
                        crearRendererFecha()
                );

        tblKardex
                .getColumnModel()
                .getColumn(1)
                .setCellRenderer(
                        new RendererTipoMovimiento()
                );

        tblKardex
                .getColumnModel()
                .getColumn(2)
                .setCellRenderer(
                        crearRendererTexto()
                );

        tblKardex
                .getColumnModel()
                .getColumn(3)
                .setCellRenderer(
                        crearRendererDocumento()
                );

        tblKardex
                .getColumnModel()
                .getColumn(4)
                .setCellRenderer(
                        crearRendererNumerico()
                );

        tblKardex
                .getColumnModel()
                .getColumn(5)
                .setCellRenderer(
                        crearRendererNumerico()
                );

        tblKardex
                .getColumnModel()
                .getColumn(6)
                .setCellRenderer(
                        crearRendererSaldo()
                );

        tblKardex
                .getColumnModel()
                .getColumn(7)
                .setCellRenderer(
                        crearRendererNumerico()
                );

        tblKardex
                .getColumnModel()
                .getColumn(8)
                .setCellRenderer(
                        crearRendererNumerico()
                );
    }

    private DefaultTableCellRenderer crearRendererFecha() {

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
                        JLabel.LEFT
                );

                return this;
            }
        };
    }

    private DefaultTableCellRenderer crearRendererTexto() {

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
                        JLabel.LEFT
                );

                return this;
            }
        };
    }

    private DefaultTableCellRenderer crearRendererDocumento() {

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
                        JLabel.CENTER
                );

                return this;
            }
        };
    }

    private DefaultTableCellRenderer crearRendererNumerico() {

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
                        JLabel.RIGHT
                );

                return this;
            }
        };
    }

    private DefaultTableCellRenderer crearRendererSaldo() {

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
                                Font.BOLD,
                                9
                        )
                );

                setHorizontalAlignment(
                        JLabel.RIGHT
                );

                return this;
            }
        };
    }

    // ============================================================
    // CAMPOS INICIALES
    // ============================================================
    private void configurarCamposIniciales() {

        mostrarProductoNoSeleccionado();

        limpiarFechas();

        limpiarEstadisticas();

        lblCantMovimientosOrdenAscendente.setText(
                "Seleccione un producto y un rango de fechas "
                + "para consultar el Kardex"
        );
    }

    private void mostrarProductoNoSeleccionado() {

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
    }

    private void limpiarFechas() {

        jdcDesdeFecha.setDate(null);
        jdcHastaFecha.setDate(null);
    }

    // ============================================================
    // LISTENERS
    // ============================================================
    private void configurarListeners() {

        cmbProductos.addActionListener(
                e -> mostrarProductoSeleccionado()
        );
    }

    // ============================================================
    // PRODUCTO SELECCIONADO
    // ============================================================
    private void mostrarProductoSeleccionado() {

        Object seleccionado
                = cmbProductos.getSelectedItem();

        if (!(seleccionado instanceof Producto producto)) {

            productoSeleccionado = null;

            mostrarProductoNoSeleccionado();

            return;
        }

        productoSeleccionado
                = producto;

        mostrarInformacionProducto(
                producto
        );
    }

    private void mostrarInformacionProducto(
            Producto producto) {

        txtInicialesProducto.setText(
                obtenerIniciales(
                        producto.getNombre()
                )
        );

        lblNombreProducto.setText(
                producto.getNombre()
        );

        lblIdProducto.setText(
                producto.getCodigo()
        );

        lblUnidad.setText(
                "Unidad: "
                + valorTexto(
                        obtenerUnidadMedida(
                                producto
                        )
                )
        );

        lblCategoria.setText(
                "Categoría: "
                + valorTexto(
                        obtenerCategoria(
                                producto
                        )
                )
        );

        txtEstadoProducto.setText(
                producto.isActivo()
                ? "• ACTIVO"
                : "• INACTIVO"
        );
    }

    private String obtenerUnidadMedida(
            Producto producto) {

        UnidadMedida unidad
                = unidadMedidaService.buscarPorId(
                        producto.getIdUnidadMedida()
                );

        if (unidad == null) {
            return "";
        }

        return unidad.getAbreviatura();
    }

    private String obtenerCategoria(
            Producto producto) {

        Categoria categoria
                = categoriaService.buscarPorId(
                        producto.getIdCategoria()
                );

        if (categoria == null) {
            return "";
        }

        return categoria.getNombre();
    }

    // ============================================================
    // CONSULTA KARDEX
    // ============================================================
    private void consultarKardex() {

        if (!(cmbProductos.getSelectedItem() instanceof Producto producto)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Debe seleccionar un producto.",
                    "Consulta de Kardex",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        LocalDate desde
                = convertirFecha(
                        jdcDesdeFecha.getDate()
                );

        if (desde == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Debe seleccionar la fecha Desde.",
                    "Consulta de Kardex",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        LocalDate hasta
                = convertirFecha(
                        jdcHastaFecha.getDate()
                );

        if (hasta == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Debe seleccionar la fecha Hasta.",
                    "Consulta de Kardex",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (desde.isAfter(hasta)) {

            JOptionPane.showMessageDialog(
                    this,
                    "La fecha Desde no puede ser posterior "
                    + "a la fecha Hasta.",
                    "Rango de fechas",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            List<KardexItem> resultado
                    = kardexService.obtenerKardex(
                            producto.getIdProducto(),
                            desde,
                            hasta
                    );

            kardexActual
                    = resultado != null
                            ? resultado
                            : new ArrayList<>();

            cargarTablaKardex();

            actualizarEstadisticas();

            actualizarMensajeMovimientos();

        } catch (RuntimeException ex) {

            kardexActual
                    = new ArrayList<>();

            limpiarTablaKardex();

            limpiarEstadisticas();

            lblCantMovimientosOrdenAscendente.setText(
                    "No se pudo consultar el Kardex"
            );

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo consultar el Kardex.\n"
                    + obtenerMensajeError(ex),
                    "Error de consulta",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // CARGAR TABLA
    // ============================================================
    private void cargarTablaKardex() {

        modeloKardex.setRowCount(0);

        for (KardexItem item : kardexActual) {

            modeloKardex.addRow(
                    new Object[]{
                        formatearFechaHora(
                                item.getFecha()
                        ),
                        formatearTipo(
                                item.getTipoMovimiento()
                        ),
                        formatearOrigen(
                                item.getOrigen()
                        ),
                        item.getIdDocumentoOrigen() == null
                        ? "—"
                        : String.valueOf(
                                item.getIdDocumentoOrigen()
                        ),
                        formatearCantidadTabla(
                                item.getEntrada()
                        ),
                        formatearCantidadTabla(
                                item.getSalida()
                        ),
                        formatearCantidadTabla(
                                item.getSaldoCantidad()
                        ),
                        formatearMoneda(
                                item.getCostoUnitario()
                        ),
                        formatearMoneda(
                                item.getSaldoValor()
                        )
                    }
            );
        }
    }

    private void limpiarTablaKardex() {

        modeloKardex.setRowCount(0);
    }

    // ============================================================
    // ESTADÍSTICAS
    // ============================================================
    private void actualizarEstadisticas() {

        if (kardexActual.isEmpty()) {

            limpiarEstadisticas();

            return;
        }

        BigDecimal totalEntradas
                = BigDecimal.ZERO;

        BigDecimal totalSalidas
                = BigDecimal.ZERO;

        for (KardexItem item : kardexActual) {

            if (item.getEntrada() != null) {

                totalEntradas
                        = totalEntradas.add(
                                item.getEntrada()
                        );
            }

            if (item.getSalida() != null) {

                totalSalidas
                        = totalSalidas.add(
                                item.getSalida()
                        );
            }
        }

        KardexItem ultimo
                = kardexActual.get(
                        kardexActual.size() - 1
                );

        BigDecimal saldoFinal
                = ultimo.getSaldoCantidad();

        lblValorTotalMovimientos.setText(
                String.valueOf(
                        kardexActual.size()
                )
        );

        lblValorTotalEntradas.setText(
                formatearCantidad(
                        totalEntradas
                )
        );

        lblValorTotalSalidas.setText(
                formatearCantidad(
                        totalSalidas
                )
        );

        lblValorSaldoFinal.setText(
                formatearCantidad(
                        saldoFinal
                )
        );

        String unidad
                = obtenerUnidadMedida(
                        productoSeleccionado
                );

        lblUnidadTotalEntradas.setText(
                valorTexto(unidad)
        );

        lblUnidadTotalSalidas.setText(
                valorTexto(unidad)
        );

        lblUnidadSaldoFinal.setText(
                valorTexto(unidad)
        );
    }

    private void limpiarEstadisticas() {

        lblValorTotalMovimientos.setText(
                "—"
        );

        lblValorTotalEntradas.setText(
                "—"
        );

        lblValorTotalSalidas.setText(
                "—"
        );

        lblValorSaldoFinal.setText(
                "—"
        );

        lblUnidadTotalEntradas.setText(
                ""
        );

        lblUnidadTotalSalidas.setText(
                ""
        );

        lblUnidadSaldoFinal.setText(
                ""
        );
    }

    // ============================================================
    // MENSAJE DE MOVIMIENTOS
    // ============================================================
    private void actualizarMensajeMovimientos() {

        int cantidad
                = kardexActual.size();

        if (cantidad == 0) {

            lblCantMovimientosOrdenAscendente.setText(
                    "No hay movimientos en el período seleccionado"
            );

            return;
        }

        String palabraMovimiento
                = cantidad == 1
                        ? "movimiento"
                        : "movimientos";

        lblCantMovimientosOrdenAscendente.setText(
                cantidad
                + " "
                + palabraMovimiento
                + " en el período seleccionado · "
                + "orden cronológico ascendente"
        );
    }

    // ============================================================
    // LIMPIAR
    // ============================================================
    private void limpiarConsulta() {

        productoSeleccionado
                = null;

        kardexActual
                = new ArrayList<>();

        cmbProductos.setSelectedItem(
                null
        );

        limpiarTextoCombo();

        limpiarFechas();

        limpiarTablaKardex();

        limpiarEstadisticas();

        mostrarProductoNoSeleccionado();

        lblCantMovimientosOrdenAscendente.setText(
                "Seleccione un producto y un rango de fechas "
                + "para consultar el Kardex"
        );
    }

    private void limpiarTextoCombo() {

        if (!cmbProductos.isEditable()) {
            return;
        }

        Component editor
                = cmbProductos
                        .getEditor()
                        .getEditorComponent();

        if (editor instanceof javax.swing.text.JTextComponent textComponent) {

            textComponent.setText("");
        }
    }

    // ============================================================
    // FECHAS
    // ============================================================
    private LocalDate convertirFecha(
            java.util.Date fecha) {

        if (fecha == null) {
            return null;
        }

        return fecha.toInstant()
                .atZone(
                        ZoneId.systemDefault()
                )
                .toLocalDate();
    }

    // ============================================================
    // FORMATEO KARDEX
    // ============================================================
    private String formatearTipo(
            TipoMovimiento tipo) {

        if (tipo == null) {
            return "—";
        }

        return switch (tipo) {

            case ENTRADA ->
                "ENTRADA";

            case SALIDA ->
                "SALIDA";

            case AJUSTE ->
                "AJUSTE";
        };
    }

    private String formatearOrigen(
            OrigenMovimiento origen) {

        if (origen == null) {
            return "—";
        }

        return switch (origen) {

            case COMPRA ->
                "Compra";

            case VENTA ->
                "Venta";

            case AJUSTE_INVENTARIO ->
                "Ajuste de inventario";

            case DEVOLUCION_COMPRA ->
                "Devolución de compra";

            case DEVOLUCION_VENTA ->
                "Devolución de venta";
        };
    }

    private String formatearCantidadTabla(
            BigDecimal cantidad) {

        if (cantidad == null
                || cantidad.compareTo(
                        BigDecimal.ZERO
                ) == 0) {

            return "—";
        }

        return formatearCantidad(
                cantidad
        );
    }

    private String formatearCantidad(
            BigDecimal valor) {

        if (valor == null) {
            return "—";
        }

        return valor
                .stripTrailingZeros()
                .toPlainString();
    }

    private String formatearMoneda(
            BigDecimal valor) {

        if (valor == null) {
            return "—";
        }

        return "S/ "
                + valor
                        .setScale(
                                2,
                                java.math.RoundingMode.HALF_UP
                        )
                        .toPlainString();
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

        return (partes[0]
                .substring(0, 1)
                + partes[1]
                        .substring(0, 1)).toUpperCase();
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
    // RENDERER DE TIPO
    // ============================================================
    private static class RendererTipoMovimiento
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

                String tipo
                        = value != null
                                ? value.toString()
                                : "";

                switch (tipo) {

                    case "ENTRADA" -> {

                        label.setForeground(
                                new Color(
                                        43,
                                        76,
                                        92
                                )
                        );
                    }

                    case "SALIDA" -> {

                        label.setForeground(
                                new Color(
                                        168,
                                        89,
                                        15
                                )
                        );
                    }

                    case "AJUSTE" -> {

                        label.setForeground(
                                new Color(
                                        91,
                                        100,
                                        114
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
        lblConsultaKardex = new javax.swing.JLabel();
        lblHistorialMovimientosInventario = new javax.swing.JLabel();
        lblNombreApellidoUsuario = new javax.swing.JLabel();
        lblFechaActual = new javax.swing.JLabel();
        lblHoraActual = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        pnlConsulta = new javax.swing.JPanel();
        lblProducto = new javax.swing.JLabel();
        cmbProductos = new javax.swing.JComboBox<>();
        lblDesde = new javax.swing.JLabel();
        jdcDesdeFecha = new com.toedter.calendar.JDateChooser();
        jdcHastaFecha = new com.toedter.calendar.JDateChooser();
        lblHasta = new javax.swing.JLabel();
        btnConsultar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        pnlEstadisticasProdSeleccionado = new javax.swing.JPanel();
        lblNombreProducto = new javax.swing.JLabel();
        lblIdProducto = new javax.swing.JLabel();
        lblUnidad = new javax.swing.JLabel();
        lblCategoria = new javax.swing.JLabel();
        txtEstadoProducto = new javax.swing.JTextField();
        pnlIniciales = new javax.swing.JPanel();
        txtInicialesProducto = new javax.swing.JTextField();
        pnlKardex = new javax.swing.JPanel();
        spnlTblKardex = new javax.swing.JScrollPane();
        tblKardex = new javax.swing.JTable();
        lblCantMovimientosOrdenAscendente = new javax.swing.JLabel();
        pnlEstadisticas = new javax.swing.JPanel();
        lblTotalMovimientos = new javax.swing.JLabel();
        lblValorTotalMovimientos = new javax.swing.JLabel();
        lblTotalEntradas = new javax.swing.JLabel();
        lblValorTotalEntradas = new javax.swing.JLabel();
        lblUnidadTotalEntradas = new javax.swing.JLabel();
        lblTotalSalidas = new javax.swing.JLabel();
        lblValorTotalSalidas = new javax.swing.JLabel();
        lblUnidadTotalSalidas = new javax.swing.JLabel();
        lblSaldoFinal = new javax.swing.JLabel();
        lblValorSaldoFinal = new javax.swing.JLabel();
        lblUnidadSaldoFinal = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblConsultaKardex.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblConsultaKardex.setText("CONSULTA DE KARDEX");

        lblHistorialMovimientosInventario.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblHistorialMovimientosInventario.setText("Historial de movimientos de inventario");

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
                    .addComponent(lblHistorialMovimientosInventario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblConsultaKardex, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(lblConsultaKardex)
                    .addComponent(lblNombreApellidoUsuario)
                    .addComponent(lblUsuario))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblHistorialMovimientosInventario, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFechaActual)
                    .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        pnlConsulta.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. CONSULTA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblProducto.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblProducto.setText("PRODUCTO");

        lblDesde.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblDesde.setText("DESDE");

        lblHasta.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblHasta.setText("HASTA");

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

        pnlEstadisticasProdSeleccionado.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblNombreProducto.setText("Porcelanato Beige 60×60 ");

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
                    .addComponent(lblNombreProducto)
                    .addGroup(pnlEstadisticasProdSeleccionadoLayout.createSequentialGroup()
                        .addGroup(pnlEstadisticasProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlEstadisticasProdSeleccionadoLayout.createSequentialGroup()
                                .addComponent(lblUnidad, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblCategoria))
                            .addComponent(lblIdProducto))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtEstadoProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlEstadisticasProdSeleccionadoLayout.setVerticalGroup(
            pnlEstadisticasProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEstadisticasProdSeleccionadoLayout.createSequentialGroup()
                .addGroup(pnlEstadisticasProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEstadisticasProdSeleccionadoLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlEstadisticasProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEstadisticasProdSeleccionadoLayout.createSequentialGroup()
                                .addComponent(lblNombreProducto)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblIdProducto)
                                .addGap(6, 6, 6)
                                .addGroup(pnlEstadisticasProdSeleccionadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblCategoria)
                                    .addComponent(lblUnidad))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEstadisticasProdSeleccionadoLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(txtEstadoProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(pnlIniciales, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlConsultaLayout = new javax.swing.GroupLayout(pnlConsulta);
        pnlConsulta.setLayout(pnlConsultaLayout);
        pnlConsultaLayout.setHorizontalGroup(
            pnlConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsultaLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlEstadisticasProdSeleccionado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlConsultaLayout.createSequentialGroup()
                        .addGroup(pnlConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblProducto)
                            .addComponent(cmbProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(41, 41, 41)
                        .addGroup(pnlConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jdcDesdeFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDesde))
                        .addGap(18, 18, 18)
                        .addGroup(pnlConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlConsultaLayout.createSequentialGroup()
                                .addComponent(jdcHastaFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnConsultar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlConsultaLayout.createSequentialGroup()
                                .addComponent(lblHasta)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        pnlConsultaLayout.setVerticalGroup(
            pnlConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsultaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnConsultar)
                        .addComponent(btnLimpiar))
                    .addGroup(pnlConsultaLayout.createSequentialGroup()
                        .addGroup(pnlConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblProducto)
                            .addComponent(lblHasta)
                            .addComponent(lblDesde))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlConsultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jdcDesdeFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jdcHastaFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlEstadisticasProdSeleccionado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlKardex.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. KARDEX", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        tblKardex.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "FECHA", "TIPO", "ORIGEN", "DOCUMENTO", "ENTRADA", "SALIDA", "SALDO", "COSTO UNIT.", "SALDO VALORIZADO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spnlTblKardex.setViewportView(tblKardex);

        lblCantMovimientosOrdenAscendente.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCantMovimientosOrdenAscendente.setText("5 movimientos en el período seleccionado · orden cronológico ascendente ");

        javax.swing.GroupLayout pnlKardexLayout = new javax.swing.GroupLayout(pnlKardex);
        pnlKardex.setLayout(pnlKardexLayout);
        pnlKardexLayout.setHorizontalGroup(
            pnlKardexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKardexLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlKardexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spnlTblKardex, javax.swing.GroupLayout.DEFAULT_SIZE, 844, Short.MAX_VALUE)
                    .addGroup(pnlKardexLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(lblCantMovimientosOrdenAscendente)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlKardexLayout.setVerticalGroup(
            pnlKardexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKardexLayout.createSequentialGroup()
                .addComponent(spnlTblKardex, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCantMovimientosOrdenAscendente)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlEstadisticas.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblTotalMovimientos.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalMovimientos.setText("TOTAL MOVIMIENTOS");

        lblValorTotalMovimientos.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalMovimientos.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblValorTotalMovimientos.setText("5");

        lblTotalEntradas.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalEntradas.setText("TOTAL DE ENTRADAS");

        lblValorTotalEntradas.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalEntradas.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblValorTotalEntradas.setText("125.00");

        lblUnidadTotalEntradas.setText("CAJA");

        lblTotalSalidas.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblTotalSalidas.setText("TOTAL DE SALIDAS");

        lblValorTotalSalidas.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorTotalSalidas.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblValorTotalSalidas.setText("105.00");

        lblUnidadTotalSalidas.setText("CAJA");

        lblSaldoFinal.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblSaldoFinal.setText("SALDO FINAL");

        lblValorSaldoFinal.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblValorSaldoFinal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblValorSaldoFinal.setText("370.00");

        lblUnidadSaldoFinal.setText("CAJA");

        javax.swing.GroupLayout pnlEstadisticasLayout = new javax.swing.GroupLayout(pnlEstadisticas);
        pnlEstadisticas.setLayout(pnlEstadisticasLayout);
        pnlEstadisticasLayout.setHorizontalGroup(
            pnlEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEstadisticasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator3))
            .addGroup(pnlEstadisticasLayout.createSequentialGroup()
                .addGroup(pnlEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlEstadisticasLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEstadisticasLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(pnlEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblSaldoFinal, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEstadisticasLayout.createSequentialGroup()
                                        .addComponent(lblValorSaldoFinal)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblUnidadSaldoFinal))))
                            .addComponent(jSeparator1)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEstadisticasLayout.createSequentialGroup()
                        .addGap(0, 11, Short.MAX_VALUE)
                        .addGroup(pnlEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTotalSalidas, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEstadisticasLayout.createSequentialGroup()
                                .addComponent(lblValorTotalSalidas)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblUnidadTotalSalidas))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(pnlEstadisticasLayout.createSequentialGroup()
                                    .addComponent(lblValorTotalEntradas)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(lblUnidadTotalEntradas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(lblTotalEntradas))
                            .addComponent(lblValorTotalMovimientos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTotalMovimientos, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        pnlEstadisticasLayout.setVerticalGroup(
            pnlEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEstadisticasLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lblTotalMovimientos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorTotalMovimientos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblTotalEntradas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblValorTotalEntradas)
                    .addComponent(lblUnidadTotalEntradas))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotalSalidas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblValorTotalSalidas)
                    .addComponent(lblUnidadTotalSalidas))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSaldoFinal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblValorSaldoFinal)
                    .addComponent(lblUnidadSaldoFinal))
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
                    .addComponent(pnlConsulta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlKardex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlEstadisticas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlKardex, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlEstadisticas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        // TODO add your handling code here:
        consultarKardex();
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
            java.util.logging.Logger.getLogger(FrmKardex.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmKardex.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmKardex.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmKardex.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmKardex dialog = new FrmKardex(new javax.swing.JFrame(), true);
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
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private com.toedter.calendar.JDateChooser jdcDesdeFecha;
    private com.toedter.calendar.JDateChooser jdcHastaFecha;
    private javax.swing.JLabel lblCantMovimientosOrdenAscendente;
    private javax.swing.JLabel lblCategoria;
    private javax.swing.JLabel lblConsultaKardex;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblFechaActual;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblHistorialMovimientosInventario;
    private javax.swing.JLabel lblHoraActual;
    private javax.swing.JLabel lblIdProducto;
    private javax.swing.JLabel lblNombreApellidoUsuario;
    private javax.swing.JLabel lblNombreProducto;
    private javax.swing.JLabel lblProducto;
    private javax.swing.JLabel lblSaldoFinal;
    private javax.swing.JLabel lblTotalEntradas;
    private javax.swing.JLabel lblTotalMovimientos;
    private javax.swing.JLabel lblTotalSalidas;
    private javax.swing.JLabel lblUnidad;
    private javax.swing.JLabel lblUnidadSaldoFinal;
    private javax.swing.JLabel lblUnidadTotalEntradas;
    private javax.swing.JLabel lblUnidadTotalSalidas;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblValorSaldoFinal;
    private javax.swing.JLabel lblValorTotalEntradas;
    private javax.swing.JLabel lblValorTotalMovimientos;
    private javax.swing.JLabel lblValorTotalSalidas;
    private javax.swing.JPanel pnlConsulta;
    private javax.swing.JPanel pnlEstadisticas;
    private javax.swing.JPanel pnlEstadisticasProdSeleccionado;
    private javax.swing.JPanel pnlIniciales;
    private javax.swing.JPanel pnlKardex;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JScrollPane spnlTblKardex;
    private javax.swing.JTable tblKardex;
    private javax.swing.JTextField txtEstadoProducto;
    private javax.swing.JTextField txtInicialesProducto;
    // End of variables declaration//GEN-END:variables
}
