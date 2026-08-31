package com.ferronor.sic.inventario.vista;

import com.ferronor.sic.inventario.logica.AjusteInventarioService;
import com.ferronor.sic.inventario.logica.InventarioService;
import com.ferronor.sic.inventario.modelo.dto.StockConsulta;
import com.ferronor.sic.maestros.logica.ProductoService;
import com.ferronor.sic.maestros.logica.UnidadMedidaService;
import com.ferronor.sic.maestros.modelo.Producto;
import com.ferronor.sic.maestros.modelo.UnidadMedida;
import com.ferronor.sic.shared.ui.ComboAutoFiltro;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;
import com.ferronor.sic.maestros.logica.CategoriaService;
import com.ferronor.sic.maestros.modelo.Categoria;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.Timer;

/**
 * Formulario para registrar ajustes de inventario.
 *
 * La vista: - selecciona un producto; - consulta su stock actual; - captura el
 * conteo físico; - calcula la diferencia; - solicita el motivo; - delega el
 * registro al AjusteInventarioService.
 *
 * No accede a DAO ni a SQL.
 */
public class FrmAjusteInventario extends javax.swing.JDialog {

    private final AjusteInventarioService ajusteInventarioService
            = ServiceFactory.ajusteInventarioService();

    private final InventarioService inventarioService
            = ServiceFactory.inventarioService();

    private final ProductoService productoService
            = ServiceFactory.productoService();

    private final CategoriaService categoriaService
            = ServiceFactory.categoriaService();

    private final UnidadMedidaService unidadMedidaService
            = ServiceFactory.unidadMedidaService();

    private Producto productoSeleccionado;

    private BigDecimal cantidadSistema
            = BigDecimal.ZERO;

    private BigDecimal cantidadFisica
            = BigDecimal.ZERO;

    private BigDecimal diferencia
            = BigDecimal.ZERO;

    private Timer timerFechaHora;

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_HORA
            = DateTimeFormatter.ofPattern("HH:mm:ss");

    public FrmAjusteInventario(
            java.awt.Frame parent,
            boolean modal) {

        super(parent, modal);

        initComponents();

        configurarComponentes();

        setLocationRelativeTo(getParent());

    }

    // ============================================================
    // CONFIGURACIÓN GENERAL
    // ============================================================
    private void configurarComponentes() {

        configurarSesion();

        configurarComboProductos();

        configurarCamposSoloLectura();

        configurarTextAreas();

        configurarEstadoInicial();

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

                    if (texto == null
                    || texto.isBlank()) {

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
    // CAMPOS SOLO LECTURA
    // ============================================================
    private void configurarCamposSoloLectura() {

        txtInicialesProducto.setEditable(false);

        txtEstadoProducto.setEditable(false);

        lblCantStockSistema.setEnabled(true);

        txtDiferencia.setEditable(false);

        txtEstadoAjuste.setEditable(false);

        txtResumenProducto.setEditable(false);
        txtValorResumenProducto.setEditable(false);

        txtResumenStockSistema.setEditable(false);
        txtValorResumenStockSistema.setEditable(false);

        txtResumenConteoFisico.setEditable(false);
        txtValorResumenConteoFisico.setEditable(false);

        txtResumenDiferencia.setEditable(false);
        txtValorResumenDiferencia.setEditable(false);

        txtResumenTipo.setEditable(false);
        txtValorResumenTipo.setEditable(false);

        txtResumenMotivo.setEditable(false);

        txtaValorResumenMotivo.setEditable(false);
    }

    // ============================================================
    // TEXT AREAS
    // ============================================================
    private void configurarTextAreas() {

        // Motivo que escribe el usuario
        txtaMotivoDelAjuste.setLineWrap(true);
        txtaMotivoDelAjuste.setWrapStyleWord(true);

        // Motivo mostrado en el resumen
        txtaValorResumenMotivo.setLineWrap(true);
        txtaValorResumenMotivo.setWrapStyleWord(true);

        txtaMotivoDelAjuste.setText("");

        txtaValorResumenMotivo.setText("");
    }

    // ============================================================
    // ESTADO INICIAL
    // ============================================================
    private void configurarEstadoInicial() {

        productoSeleccionado = null;

        cantidadSistema
                = BigDecimal.ZERO;

        cantidadFisica
                = BigDecimal.ZERO;

        diferencia
                = BigDecimal.ZERO;

        mostrarEstadoSinProducto();

        actualizarResumen();
    }

    private void mostrarEstadoSinProducto() {

        txtInicialesProducto.setText("—");

        lblNombreProducto.setText(
                "Seleccione un producto"
        );

        lblIdProducto.setText("—");

        lblUnidad.setText(
                "Unidad: —"
        );

        lblCategoria.setText(
                "Categoría: —"
        );

        txtEstadoProducto.setText(
                "• Sin consulta"
        );

        lblCantStockSistema.setText(
                "—"
        );

        lblUnidadStock.setText(
                ""
        );

        txtConteoFisico.setText("");

        txtDiferencia.setText(
                "—"
        );

        txtEstadoAjuste.setText(
                "• Sin producto"
        );
    }

    // ============================================================
    // LISTENERS
    // ============================================================
    private void configurarListeners() {

        cmbProductos.addActionListener(
                e -> productoSeleccionado()
        );

        txtConteoFisico.addCaretListener(
                e -> actualizarComparacion()
        );

        txtaMotivoDelAjuste
                .getDocument()
                .addDocumentListener(
                        new javax.swing.event.DocumentListener() {

                    @Override
                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e) {

                        actualizarResumen();
                    }

                    @Override
                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e) {

                        actualizarResumen();
                    }

                    @Override
                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e) {

                        actualizarResumen();
                    }
                }
                );
    }

    // ============================================================
    // PRODUCTO SELECCIONADO
    // ============================================================
    private void productoSeleccionado() {

        Object seleccionado
                = cmbProductos.getSelectedItem();

        if (!(seleccionado instanceof Producto producto)) {

            limpiarProductoSeleccionado();

            return;
        }

        productoSeleccionado
                = producto;

        mostrarDatosProducto(
                producto
        );

        cargarStockProducto(
                producto
        );

        actualizarComparacion();
    }

    private void mostrarDatosProducto(
            Producto producto) {

        lblNombreProducto.setText(
                producto.getNombre()
        );

        lblIdProducto.setText(
                producto.getCodigo()
        );

        String abreviaturaUnidad
                = obtenerAbreviaturaUnidad(
                        producto
                );

        String categoria
                = obtenerCategoriaProducto(
                        producto
                );

        lblUnidad.setText(
                "Unidad: "
                + valorTexto(
                        abreviaturaUnidad
                )
        );

        lblCategoria.setText(
                "Categoría: "
                + valorTexto(
                        categoria
                )
        );

        txtEstadoProducto.setText(
                producto.isActivo()
                ? "• ACTIVO"
                : "• INACTIVO"
        );

        txtInicialesProducto.setText(
                obtenerIniciales(
                        producto.getNombre()
                )
        );
    }

    // ============================================================
    // UNIDAD DE MEDIDA
    // ============================================================
    private String obtenerAbreviaturaUnidad(
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

    // ============================================================
    // CATEGORÍA
    // ============================================================
    private String obtenerCategoriaProducto(
            Producto producto) {

        Categoria categoria
                = categoriaService.buscarPorId(
                        producto.getIdCategoria()
                );

        return categoria != null
                ? categoria.getNombre()
                : "";
    }

    // ============================================================
    // STOCK
    // ============================================================
    private void cargarStockProducto(
            Producto producto) {

        try {

            StockConsulta consulta
                    = inventarioService
                            .consultarStockPorProducto(
                                    producto.getIdProducto()
                            );

            if (consulta == null) {

                cantidadSistema
                        = BigDecimal.ZERO;

                lblCantStockSistema.setText(
                        "0"
                );

                lblUnidadStock.setText(
                        obtenerAbreviaturaUnidad(
                                producto
                        )
                );

                return;
            }

            cantidadSistema
                    = consulta.getCantidadActual();

            lblCantStockSistema.setText(
                    formatearCantidad(
                            cantidadSistema
                    )
            );

            lblUnidadStock.setText(
                    valorTexto(
                            consulta.getAbreviaturaUnidad()
                    )
            );

        } catch (RuntimeException ex) {

            cantidadSistema
                    = BigDecimal.ZERO;

            lblCantStockSistema.setText(
                    "—"
            );

            lblUnidadStock.setText(
                    ""
            );

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo consultar el stock del producto.\n"
                    + obtenerMensajeError(ex),
                    "Error de consulta",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // COMPARACIÓN
    // ============================================================
    private void actualizarComparacion() {

        cantidadFisica
                = obtenerCantidadFisica();

        if (productoSeleccionado == null) {

            diferencia
                    = BigDecimal.ZERO;

            txtDiferencia.setText(
                    "—"
            );

            txtEstadoAjuste.setText(
                    "• Sin producto"
            );

            actualizarResumen();

            return;
        }

        diferencia
                = cantidadFisica.subtract(
                        cantidadSistema
                );

        mostrarDiferencia();

        actualizarResumen();
    }

    private BigDecimal obtenerCantidadFisica() {

        String texto
                = txtConteoFisico
                        .getText()
                        .trim();

        if (texto.isBlank()) {

            return BigDecimal.ZERO;
        }

        try {

            return new BigDecimal(
                    texto.replace(",", ".")
            );

        } catch (NumberFormatException ex) {

            return BigDecimal.ZERO;
        }
    }

    private void mostrarDiferencia() {

        String unidad
                = valorTexto(
                        lblUnidadStock.getText()
                );

        if (txtConteoFisico
                .getText()
                .trim()
                .isBlank()) {

            txtDiferencia.setText(
                    "—"
            );

            txtEstadoAjuste.setText(
                    "• COMPLETE EL CONTEO"
            );

            return;
        }

        txtDiferencia.setText(
                formatearCantidadConSigno(
                        diferencia
                )
                + (unidad.equals("—")
                ? ""
                : " " + unidad)
        );

        if (diferencia.compareTo(
                BigDecimal.ZERO) > 0) {

            txtEstadoAjuste.setText(
                    "• ENTRADA POR AJUSTE"
            );

            txtEstadoAjuste.setForeground(
                    new Color(
                            43,
                            76,
                            92
                    )
            );

        } else if (diferencia.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            txtEstadoAjuste.setText(
                    "• SALIDA POR AJUSTE"
            );

            txtEstadoAjuste.setForeground(
                    new Color(
                            168,
                            89,
                            15
                    )
            );

        } else {

            txtEstadoAjuste.setText(
                    "• SIN DIFERENCIA"
            );

            txtEstadoAjuste.setForeground(
                    new Color(
                            176,
                            58,
                            46
                    )
            );
        }
    }

    // ============================================================
    // RESUMEN
    // ============================================================
    private void actualizarResumen() {

        if (productoSeleccionado == null) {

            txtValorResumenProducto.setText(
                    "—"
            );

            txtValorResumenStockSistema.setText(
                    "—"
            );

            txtValorResumenConteoFisico.setText(
                    "—"
            );

            txtValorResumenDiferencia.setText(
                    "—"
            );

            txtValorResumenTipo.setText(
                    "• Sin producto"
            );

            txtaValorResumenMotivo.setText(
                    ""
            );

            return;
        }

        String unidad
                = valorTexto(
                        lblUnidadStock.getText()
                );

        txtValorResumenProducto.setText(
                productoSeleccionado.getNombre()
                + " ("
                + productoSeleccionado.getCodigo()
                + ")"
        );

        txtValorResumenStockSistema.setText(
                formatearCantidad(
                        cantidadSistema
                )
                + (unidad.equals("—")
                ? ""
                : " " + unidad)
        );

        String textoConteo
                = txtConteoFisico
                        .getText()
                        .trim();

        txtValorResumenConteoFisico.setText(
                textoConteo.isBlank()
                ? "—"
                : formatearCantidad(
                        cantidadFisica
                )
                + (unidad.equals("—")
                ? ""
                : " " + unidad)
        );

        if (textoConteo.isBlank()) {

            txtValorResumenDiferencia.setText(
                    "—"
            );

            txtValorResumenTipo.setText(
                    "• Complete el conteo"
            );

        } else {

            txtValorResumenDiferencia.setText(
                    formatearCantidadConSigno(
                            diferencia
                    )
                    + (unidad.equals("—")
                    ? ""
                    : " " + unidad)
            );

            txtValorResumenTipo.setText(
                    obtenerTextoTipoAjuste(
                            diferencia
                    )
            );
        }

        txtaValorResumenMotivo.setText(
                txtaMotivoDelAjuste
                        .getText()
                        .trim()
        );
    }

    private String obtenerTextoTipoAjuste(
            BigDecimal diferencia) {

        if (diferencia == null) {
            return "• —";
        }

        int comparacion
                = diferencia.compareTo(
                        BigDecimal.ZERO
                );

        if (comparacion > 0) {
            return "• ENTRADA POR AJUSTE";
        }

        if (comparacion < 0) {
            return "• SALIDA POR AJUSTE";
        }

        return "• SIN DIFERENCIA";
    }

    // ============================================================
    // REGISTRAR AJUSTE
    // ============================================================
    private void registrarAjuste() {

        if (!SesionUsuario.haySesion()) {

            JOptionPane.showMessageDialog(
                    this,
                    "No existe una sesión de usuario activa.",
                    "Sesión",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (productoSeleccionado == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Debe seleccionar un producto.",
                    "Producto",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String textoConteo
                = txtConteoFisico
                        .getText()
                        .trim();

        if (textoConteo.isBlank()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Debe ingresar la cantidad física contada.",
                    "Conteo físico",
                    JOptionPane.WARNING_MESSAGE
            );

            txtConteoFisico.requestFocus();

            return;
        }

        try {

            cantidadFisica
                    = new BigDecimal(
                            textoConteo
                                    .replace(",", ".")
                    );

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "La cantidad física no es válida.",
                    "Conteo físico",
                    JOptionPane.WARNING_MESSAGE
            );

            txtConteoFisico.requestFocus();

            return;
        }

        if (cantidadFisica.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "La cantidad física no puede ser negativa.",
                    "Conteo físico",
                    JOptionPane.WARNING_MESSAGE
            );

            txtConteoFisico.requestFocus();

            return;
        }

        diferencia
                = cantidadFisica.subtract(
                        cantidadSistema
                );

        if (diferencia.compareTo(
                BigDecimal.ZERO
        ) == 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "No existe diferencia entre el stock del sistema "
                    + "y el conteo físico.",
                    "Ajuste sin diferencia",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String motivo
                = txtaMotivoDelAjuste
                        .getText()
                        .trim();

        if (motivo.isBlank()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Debe indicar el motivo del ajuste.",
                    "Motivo",
                    JOptionPane.WARNING_MESSAGE
            );

            txtaMotivoDelAjuste.requestFocus();

            return;
        }

        int confirmacion
                = JOptionPane.showConfirmDialog(
                        this,
                        construirMensajeConfirmacion(),
                        "Confirmar ajuste",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (confirmacion
                != JOptionPane.YES_OPTION) {

            return;
        }

        RespuestaOperacion<Void> respuesta
                = ajusteInventarioService.registrarAjuste(
                        productoSeleccionado.getIdProducto(),
                        cantidadFisica,
                        motivo,
                        SesionUsuario.actual()
                                .getIdUsuario()
                );

        if (!respuesta.isExito()) {

            JOptionPane.showMessageDialog(
                    this,
                    respuesta.getMensaje(),
                    "No se pudo registrar el ajuste",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "El ajuste de inventario fue registrado correctamente.",
                "Ajuste registrado",
                JOptionPane.INFORMATION_MESSAGE
        );

        limpiarFormulario();
    }

    private String construirMensajeConfirmacion() {

        String unidad
                = valorTexto(
                        lblUnidadStock.getText()
                );

        return "Producto: "
                + productoSeleccionado.getNombre()
                + "\n"
                + "Stock sistema: "
                + formatearCantidad(
                        cantidadSistema
                )
                + " "
                + unidad
                + "\n"
                + "Conteo físico: "
                + formatearCantidad(
                        cantidadFisica
                )
                + " "
                + unidad
                + "\n"
                + "Diferencia: "
                + formatearCantidadConSigno(
                        diferencia
                )
                + " "
                + unidad
                + "\n\n"
                + "¿Desea registrar este ajuste?";
    }

    // ============================================================
    // LIMPIAR FORMULARIO
    // ============================================================
    private void limpiarFormulario() {

        productoSeleccionado
                = null;

        cantidadSistema
                = BigDecimal.ZERO;

        cantidadFisica
                = BigDecimal.ZERO;

        diferencia
                = BigDecimal.ZERO;

        cmbProductos.setSelectedItem(
                null
        );

        limpiarTextoCombo();

        txtConteoFisico.setText("");

        txtaMotivoDelAjuste.setText("");

        mostrarEstadoSinProducto();

        actualizarResumen();
    }

    private void limpiarProductoSeleccionado() {

        productoSeleccionado
                = null;

        cantidadSistema
                = BigDecimal.ZERO;

        cantidadFisica
                = BigDecimal.ZERO;

        diferencia
                = BigDecimal.ZERO;

        mostrarEstadoSinProducto();

        actualizarResumen();
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
    // FORMATEOS
    // ============================================================
    private String formatearCantidad(
            BigDecimal valor) {

        if (valor == null) {
            return "—";
        }

        return valor
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                .toPlainString();
    }

    private String formatearCantidadConSigno(
            BigDecimal valor) {

        if (valor == null) {
            return "—";
        }

        BigDecimal normalizado
                = valor.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        if (normalizado.compareTo(
                BigDecimal.ZERO
        ) > 0) {

            return "+"
                    + normalizado.toPlainString();
        }

        if (normalizado.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            return "−"
                    + normalizado
                            .abs()
                            .toPlainString();
        }

        return "0.00";
    }

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
    // CANCELAR
    // ============================================================
    private void cancelar() {

        boolean hayCambios
                = productoSeleccionado != null
                || !txtConteoFisico
                        .getText()
                        .trim()
                        .isBlank()
                || !txtaMotivoDelAjuste
                        .getText()
                        .trim()
                        .isBlank();

        if (!hayCambios) {

            dispose();

            return;
        }

        int confirmacion
                = JOptionPane.showConfirmDialog(
                        this,
                        "Hay información ingresada.\n"
                        + "¿Desea cancelar el ajuste?",
                        "Cancelar ajuste",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (confirmacion
                == JOptionPane.YES_OPTION) {

            dispose();
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
        lblAjusteDelInventario = new javax.swing.JLabel();
        lblCorreccionExistenciasFisica = new javax.swing.JLabel();
        lblNombreApellidoUsuario = new javax.swing.JLabel();
        lblFechaActual = new javax.swing.JLabel();
        lblHoraActual = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        pnlProducto = new javax.swing.JPanel();
        lblBuscarProducto = new javax.swing.JLabel();
        cmbProductos = new javax.swing.JComboBox<>();
        pnlEstadisticasProdSeleccionado = new javax.swing.JPanel();
        lblNombreProducto = new javax.swing.JLabel();
        lblIdProducto = new javax.swing.JLabel();
        lblUnidad = new javax.swing.JLabel();
        lblCategoria = new javax.swing.JLabel();
        txtEstadoProducto = new javax.swing.JTextField();
        pnlIniciales = new javax.swing.JPanel();
        txtInicialesProducto = new javax.swing.JTextField();
        pnlComparacionExistencias = new javax.swing.JPanel();
        lblStockSistema = new javax.swing.JLabel();
        lblCantStockSistema = new javax.swing.JLabel();
        lblUnidadStock = new javax.swing.JLabel();
        lblConteoFisico = new javax.swing.JLabel();
        txtConteoFisico = new javax.swing.JTextField();
        lblDiferencia = new javax.swing.JLabel();
        txtDiferencia = new javax.swing.JTextField();
        lblValorActualNoEditable = new javax.swing.JLabel();
        lblCantidadRealContadaAlmacén = new javax.swing.JLabel();
        lblCalculadaFisicoSistema = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        txtEstadoAjuste = new javax.swing.JTextField();
        pnlMotivoAjuste = new javax.swing.JPanel();
        spnlMotivoDelAjuste = new javax.swing.JScrollPane();
        txtaMotivoDelAjuste = new javax.swing.JTextArea();
        pnlResumen = new javax.swing.JPanel();
        txtResumenProducto = new javax.swing.JTextField();
        txtValorResumenProducto = new javax.swing.JTextField();
        txtResumenStockSistema = new javax.swing.JTextField();
        txtValorResumenStockSistema = new javax.swing.JTextField();
        txtResumenConteoFisico = new javax.swing.JTextField();
        txtValorResumenConteoFisico = new javax.swing.JTextField();
        txtResumenDiferencia = new javax.swing.JTextField();
        txtValorResumenDiferencia = new javax.swing.JTextField();
        txtResumenTipo = new javax.swing.JTextField();
        txtValorResumenTipo = new javax.swing.JTextField();
        txtResumenMotivo = new javax.swing.JTextField();
        spnlResumenMotivo = new javax.swing.JScrollPane();
        txtaValorResumenMotivo = new javax.swing.JTextArea();
        btnRegistrarAjuste = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlSuperior.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblAjusteDelInventario.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblAjusteDelInventario.setText("AJUSTE DE INVENTARIO");

        lblCorreccionExistenciasFisica.setFont(new java.awt.Font("Consolas", 0, 10)); // NOI18N
        lblCorreccionExistenciasFisica.setText("Corrección de existencias físicas");

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
                    .addComponent(lblCorreccionExistenciasFisica, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblAjusteDelInventario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(lblAjusteDelInventario)
                    .addComponent(lblNombreApellidoUsuario)
                    .addComponent(lblUsuario))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCorreccionExistenciasFisica, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFechaActual)
                    .addComponent(lblHoraActual, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        pnlProducto.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "01. PRODUCTO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblBuscarProducto.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblBuscarProducto.setText("BUSCAR PRODUCTO");

        cmbProductos.setName(""); // NOI18N

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
                .addGap(26, 104, Short.MAX_VALUE))
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

        javax.swing.GroupLayout pnlProductoLayout = new javax.swing.GroupLayout(pnlProducto);
        pnlProducto.setLayout(pnlProductoLayout);
        pnlProductoLayout.setHorizontalGroup(
            pnlProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductoLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(pnlProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBuscarProducto)
                    .addComponent(cmbProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlEstadisticasProdSeleccionado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        pnlProductoLayout.setVerticalGroup(
            pnlProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblBuscarProducto)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlEstadisticasProdSeleccionado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        pnlComparacionExistencias.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "02. COMPARACIÓN DE EXISTENCIA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        lblStockSistema.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblStockSistema.setText("STOCK DEL SISTEMA");

        lblCantStockSistema.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        lblCantStockSistema.setText("350.00");

        lblUnidadStock.setText("CAJA");

        lblConteoFisico.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblConteoFisico.setText("CONTEO FÍSICO");

        txtConteoFisico.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        txtConteoFisico.setText("342.00");

        lblDiferencia.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblDiferencia.setText("DIFERENCIA");

        txtDiferencia.setEditable(false);
        txtDiferencia.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        txtDiferencia.setText("-8.00 CAJA");

        lblValorActualNoEditable.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblValorActualNoEditable.setText("Valor actual — no editable ");

        lblCantidadRealContadaAlmacén.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCantidadRealContadaAlmacén.setText("Cantidad real contada en almacén ");

        lblCalculadaFisicoSistema.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblCalculadaFisicoSistema.setText("Calculada: físico − sistema");

        txtEstadoAjuste.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        txtEstadoAjuste.setText("• SALIDA POR AJUSTE");

        javax.swing.GroupLayout pnlComparacionExistenciasLayout = new javax.swing.GroupLayout(pnlComparacionExistencias);
        pnlComparacionExistencias.setLayout(pnlComparacionExistenciasLayout);
        pnlComparacionExistenciasLayout.setHorizontalGroup(
            pnlComparacionExistenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlComparacionExistenciasLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(pnlComparacionExistenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1)
                    .addGroup(pnlComparacionExistenciasLayout.createSequentialGroup()
                        .addGroup(pnlComparacionExistenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(pnlComparacionExistenciasLayout.createSequentialGroup()
                                .addComponent(lblCantStockSistema)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblUnidadStock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(lblValorActualNoEditable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblStockSistema, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(36, 36, 36)
                        .addGroup(pnlComparacionExistenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlComparacionExistenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtConteoFisico)
                                .addComponent(lblConteoFisico, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE))
                            .addComponent(lblCantidadRealContadaAlmacén))
                        .addGap(36, 36, 36)
                        .addGroup(pnlComparacionExistenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCalculadaFisicoSistema, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtDiferencia, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDiferencia))))
                .addGap(12, 12, 12))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlComparacionExistenciasLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtEstadoAjuste, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(204, 204, 204))
        );
        pnlComparacionExistenciasLayout.setVerticalGroup(
            pnlComparacionExistenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlComparacionExistenciasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlComparacionExistenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblStockSistema)
                    .addComponent(lblConteoFisico)
                    .addComponent(lblDiferencia))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlComparacionExistenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlComparacionExistenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(lblCantStockSistema, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblUnidadStock))
                    .addGroup(pnlComparacionExistenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtConteoFisico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtDiferencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlComparacionExistenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblValorActualNoEditable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCantidadRealContadaAlmacén, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCalculadaFisicoSistema, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtEstadoAjuste, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(9, Short.MAX_VALUE))
        );

        pnlMotivoAjuste.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "03. MOTIVO DEL AJUSTE", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        txtaMotivoDelAjuste.setColumns(20);
        txtaMotivoDelAjuste.setRows(5);
        spnlMotivoDelAjuste.setViewportView(txtaMotivoDelAjuste);

        javax.swing.GroupLayout pnlMotivoAjusteLayout = new javax.swing.GroupLayout(pnlMotivoAjuste);
        pnlMotivoAjuste.setLayout(pnlMotivoAjusteLayout);
        pnlMotivoAjusteLayout.setHorizontalGroup(
            pnlMotivoAjusteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMotivoAjusteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlMotivoDelAjuste)
                .addContainerGap())
        );
        pnlMotivoAjusteLayout.setVerticalGroup(
            pnlMotivoAjusteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMotivoAjusteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlMotivoDelAjuste, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                .addContainerGap())
        );

        pnlResumen.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "04. RESUMEN", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Consolas", 0, 12))); // NOI18N

        txtResumenProducto.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtResumenProducto.setText("PRODUCTO");

        txtValorResumenProducto.setBackground(new java.awt.Color(153, 102, 0));
        txtValorResumenProducto.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtValorResumenProducto.setText("Porcelanato Beige 60×60 (P-00231) ");

        txtResumenStockSistema.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtResumenStockSistema.setText("STOCK SISTEMA");

        txtValorResumenStockSistema.setBackground(new java.awt.Color(153, 102, 0));
        txtValorResumenStockSistema.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtValorResumenStockSistema.setText("350.00 CAJA ");

        txtResumenConteoFisico.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtResumenConteoFisico.setText("CONTEO FÍSICO");

        txtValorResumenConteoFisico.setBackground(new java.awt.Color(153, 102, 0));
        txtValorResumenConteoFisico.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtValorResumenConteoFisico.setText("342.00 CAJA ");

        txtResumenDiferencia.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtResumenDiferencia.setText("DIFERENCIA");

        txtValorResumenDiferencia.setBackground(new java.awt.Color(153, 102, 0));
        txtValorResumenDiferencia.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtValorResumenDiferencia.setText("−8.00 CAJA ");

        txtResumenTipo.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtResumenTipo.setText("TIPO");

        txtValorResumenTipo.setBackground(new java.awt.Color(153, 102, 0));
        txtValorResumenTipo.setFont(new java.awt.Font("Consolas", 1, 12)); // NOI18N
        txtValorResumenTipo.setText("• SALIDA POR AJUSTE");

        txtResumenMotivo.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtResumenMotivo.setText("MOTIVO");

        txtaValorResumenMotivo.setBackground(new java.awt.Color(153, 102, 0));
        txtaValorResumenMotivo.setColumns(20);
        txtaValorResumenMotivo.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtaValorResumenMotivo.setRows(5);
        txtaValorResumenMotivo.setText("Diferencia detectada durante conteo físico mensual del almacén principal.\n");
        spnlResumenMotivo.setViewportView(txtaValorResumenMotivo);

        javax.swing.GroupLayout pnlResumenLayout = new javax.swing.GroupLayout(pnlResumen);
        pnlResumen.setLayout(pnlResumenLayout);
        pnlResumenLayout.setHorizontalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlResumenLayout.createSequentialGroup()
                        .addComponent(txtResumenProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorResumenProducto))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlResumenLayout.createSequentialGroup()
                        .addComponent(txtResumenStockSistema, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorResumenStockSistema))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlResumenLayout.createSequentialGroup()
                        .addComponent(txtResumenConteoFisico, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorResumenConteoFisico))
                    .addGroup(pnlResumenLayout.createSequentialGroup()
                        .addComponent(txtResumenDiferencia, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorResumenDiferencia))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlResumenLayout.createSequentialGroup()
                        .addComponent(txtResumenTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorResumenTipo))
                    .addGroup(pnlResumenLayout.createSequentialGroup()
                        .addComponent(txtResumenMotivo, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnlResumenMotivo)))
                .addContainerGap())
        );
        pnlResumenLayout.setVerticalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtResumenProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtValorResumenProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtResumenStockSistema, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtValorResumenStockSistema, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtResumenConteoFisico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtValorResumenConteoFisico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtResumenDiferencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtValorResumenDiferencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtResumenTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtValorResumenTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtResumenMotivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnlResumenMotivo, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnRegistrarAjuste.setBackground(new java.awt.Color(153, 102, 0));
        btnRegistrarAjuste.setText("Registrar Ajuste");
        btnRegistrarAjuste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarAjusteActionPerformed(evt);
            }
        });

        btnCancelar.setBackground(new java.awt.Color(51, 51, 51));
        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlComparacionExistencias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlProducto, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlSuperior, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlMotivoAjuste, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlResumen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnCancelar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRegistrarAjuste)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlComparacionExistencias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlMotivoAjuste, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResumen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRegistrarAjuste)
                    .addComponent(btnCancelar))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRegistrarAjusteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarAjusteActionPerformed
        // TODO add your handling code here:
        registrarAjuste();
    }//GEN-LAST:event_btnRegistrarAjusteActionPerformed

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
            java.util.logging.Logger.getLogger(FrmAjusteInventario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmAjusteInventario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmAjusteInventario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmAjusteInventario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmAjusteInventario dialog = new FrmAjusteInventario(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnRegistrarAjuste;
    private javax.swing.JComboBox<Producto> cmbProductos;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblAjusteDelInventario;
    private javax.swing.JLabel lblBuscarProducto;
    private javax.swing.JLabel lblCalculadaFisicoSistema;
    private javax.swing.JLabel lblCantStockSistema;
    private javax.swing.JLabel lblCantidadRealContadaAlmacén;
    private javax.swing.JLabel lblCategoria;
    private javax.swing.JLabel lblConteoFisico;
    private javax.swing.JLabel lblCorreccionExistenciasFisica;
    private javax.swing.JLabel lblDiferencia;
    private javax.swing.JLabel lblFechaActual;
    private javax.swing.JLabel lblHoraActual;
    private javax.swing.JLabel lblIdProducto;
    private javax.swing.JLabel lblNombreApellidoUsuario;
    private javax.swing.JLabel lblNombreProducto;
    private javax.swing.JLabel lblStockSistema;
    private javax.swing.JLabel lblUnidad;
    private javax.swing.JLabel lblUnidadStock;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblValorActualNoEditable;
    private javax.swing.JPanel pnlComparacionExistencias;
    private javax.swing.JPanel pnlEstadisticasProdSeleccionado;
    private javax.swing.JPanel pnlIniciales;
    private javax.swing.JPanel pnlMotivoAjuste;
    private javax.swing.JPanel pnlProducto;
    private javax.swing.JPanel pnlResumen;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JScrollPane spnlMotivoDelAjuste;
    private javax.swing.JScrollPane spnlResumenMotivo;
    private javax.swing.JTextField txtConteoFisico;
    private javax.swing.JTextField txtDiferencia;
    private javax.swing.JTextField txtEstadoAjuste;
    private javax.swing.JTextField txtEstadoProducto;
    private javax.swing.JTextField txtInicialesProducto;
    private javax.swing.JTextField txtResumenConteoFisico;
    private javax.swing.JTextField txtResumenDiferencia;
    private javax.swing.JTextField txtResumenMotivo;
    private javax.swing.JTextField txtResumenProducto;
    private javax.swing.JTextField txtResumenStockSistema;
    private javax.swing.JTextField txtResumenTipo;
    private javax.swing.JTextField txtValorResumenConteoFisico;
    private javax.swing.JTextField txtValorResumenDiferencia;
    private javax.swing.JTextField txtValorResumenProducto;
    private javax.swing.JTextField txtValorResumenStockSistema;
    private javax.swing.JTextField txtValorResumenTipo;
    private javax.swing.JTextArea txtaMotivoDelAjuste;
    private javax.swing.JTextArea txtaValorResumenMotivo;
    // End of variables declaration//GEN-END:variables
}
