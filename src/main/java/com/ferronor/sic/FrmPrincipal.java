package com.ferronor.sic;

import com.ferronor.sic.compras.vista.FrmAprobacionOrdenCompra;
import com.ferronor.sic.compras.vista.FrmCompras;
import com.ferronor.sic.compras.vista.FrmDevolucionProveedor;
import com.ferronor.sic.compras.vista.FrmHistorialCompras;
import com.ferronor.sic.compras.vista.FrmOrdenCompra;
import com.ferronor.sic.compras.vista.FrmPagoProveedor;
import com.ferronor.sic.contabilidad.vista.FrmBalanceGeneral;
import com.ferronor.sic.contabilidad.vista.FrmBalanzaComprobacion;
import com.ferronor.sic.contabilidad.vista.FrmEstadoDeResultados;
import com.ferronor.sic.contabilidad.vista.FrmLibroDiario;
import com.ferronor.sic.contabilidad.vista.FrmLibroMayor;
import com.ferronor.sic.dashboard.vista.FrmDashboard;
import com.ferronor.sic.inventario.vista.FrmAjusteInventario;
import com.ferronor.sic.inventario.vista.FrmConsultarStock;
import com.ferronor.sic.inventario.vista.FrmKardex;
import com.ferronor.sic.maestros.vista.FrmGestionCategorias;
import com.ferronor.sic.maestros.vista.FrmGestionClientes;
import com.ferronor.sic.maestros.vista.FrmGestionFormasPago;
import com.ferronor.sic.maestros.vista.FrmGestionProductos;
import com.ferronor.sic.maestros.vista.FrmGestionProveedores;
import com.ferronor.sic.maestros.vista.FrmGestionTiposComprobante;
import com.ferronor.sic.maestros.vista.FrmGestionUnidadesMedida;
import com.ferronor.sic.seguridad.vista.FrmLogin;
import com.ferronor.sic.shared.SesionUsuario;
import com.ferronor.sic.tesoreria.vista.FrmAbrirCaja;
import com.ferronor.sic.tesoreria.vista.FrmCierreCaja;
import com.ferronor.sic.tesoreria.vista.FrmCuentasCobrar;
import com.ferronor.sic.tesoreria.vista.FrmCuentasPagar;
import com.ferronor.sic.tesoreria.vista.FrmMovsBancarios;
import com.ferronor.sic.tesoreria.vista.FrmMovsCaja;
import com.ferronor.sic.ventas.vista.FrmCobroCliente;
import com.ferronor.sic.ventas.vista.FrmDevolucionCliente;
import com.ferronor.sic.ventas.vista.FrmHistorialVentas;
import com.ferronor.sic.ventas.vista.FrmVentas;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Ventana Principal del Sistema Decor Home Ferronor.
 * Implementa el panel lateral de acceso a módulos y el panel central de bienvenida.
 */
public class FrmPrincipal extends javax.swing.JFrame {

    // Paleta de colores del diseño del mockup
    private static final Color COLOR_BARRA_LATERAL = new Color(0x56, 0x5D, 0x5B);
    private static final Color COLOR_HEADER_LATERAL = new Color(0x3B, 0x41, 0x3F);
    private static final Color COLOR_BTN_NORMAL = new Color(0x5E, 0x66, 0x64);
    private static final Color COLOR_BTN_HOVER = new Color(0x4B, 0x52, 0x50);
    private static final Color COLOR_BTN_CERRAR_SESION = new Color(0x9E, 0x62, 0x49);
    private static final Color COLOR_BTN_CERRAR_HOVER = new Color(0xB5, 0x72, 0x55);
    private static final Color COLOR_FONDO_CENTRAL = new Color(0x1F, 0x24, 0x27);
    private static final Color COLOR_NARANJA_ACCENTO = new Color(0xE0, 0x64, 0x29);
    private static final Color COLOR_AZUL_ACCENTO = new Color(0x43, 0x88, 0xCC);

    public FrmPrincipal() {
        initComponents();
        configurarVentana();
        aplicarPermisos();
    }

    private void configurarVentana() {
        SesionUsuario sesion = SesionUsuario.actual();
        if (sesion != null) {
            setTitle("Decor Home Ferronor — " + sesion.getNombreCompleto() + " (" + sesion.getNombreRol() + ")");
            lblNombreUsuario.setText(sesion.getNombreCompleto().toUpperCase());
            lblRolUsuario.setText(sesion.getNombreRol().toUpperCase());
        } else {
            setTitle("Decor Home Ferronor — Sistema Comercial y Contable");
            lblNombreUsuario.setText("USUARIO DEL SISTEMA");
            lblRolUsuario.setText("ADMINISTRADOR");
        }

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1020, 680));
        setSize(1100, 720);
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cerrarSesion();
            }
        });
    }

    private void aplicarPermisos() {
        SesionUsuario sesion = SesionUsuario.actual();
        if (sesion == null) return;

        btnModuloMaestros.setEnabled(sesion.tienePermiso("MAESTROS"));
        btnModuloInventario.setEnabled(sesion.tienePermiso("INVENTARIO"));
        btnModuloVentas.setEnabled(sesion.tienePermiso("VENTAS") || sesion.tienePermiso("REGISTRAR_VENTA"));
        btnModuloCompras.setEnabled(sesion.tienePermiso("COMPRAS") || sesion.tienePermiso("REGISTRAR_COMPRA"));
        btnModuloTesoreria.setEnabled(sesion.tienePermiso("TESORERIA") || sesion.tienePermiso("CAJA"));
        btnModuloContabilidad.setEnabled(sesion.tienePermiso("CONTABILIDAD"));
    }

    private void cerrarSesion() {
        int resp = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de que desea cerrar la sesión actual?",
                "Confirmar Cierre de Sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (resp == JOptionPane.YES_OPTION) {
            SesionUsuario.cerrar();
            dispose();
            new FrmLogin().setVisible(true);
        }
    }

    // =========================================================================
    // MENÚS EMERGENTES CONTEXTUALES DE CADA MÓDULO
    // =========================================================================

    private void mostrarMenuMaestros(Component invoker) {
        JPopupMenu popup = new JPopupMenu();
        popup.add(crearMenuItem("Categorías de Productos", e -> new FrmGestionCategorias().setVisible(true)));
        popup.add(crearMenuItem("Clientes", e -> new FrmGestionClientes().setVisible(true)));
        popup.add(crearMenuItem("Productos y Catálogo", e -> new FrmGestionProductos().setVisible(true)));
        popup.add(crearMenuItem("Proveedores", e -> new FrmGestionProveedores().setVisible(true)));
        popup.addSeparator();
        popup.add(crearMenuItem("Unidades de Medida", e -> new FrmGestionUnidadesMedida().setVisible(true)));
        popup.add(crearMenuItem("Formas de Pago", e -> new FrmGestionFormasPago().setVisible(true)));
        popup.add(crearMenuItem("Tipos de Comprobante", e -> new FrmGestionTiposComprobante().setVisible(true)));
        popup.show(invoker, invoker.getWidth(), 0);
    }

    private void mostrarMenuInventario(Component invoker) {
        JPopupMenu popup = new JPopupMenu();
        popup.add(crearMenuItem("Consultar Stock", e -> new FrmConsultarStock(this, true).setVisible(true)));
        popup.add(crearMenuItem("Kardex de Movimientos", e -> new FrmKardex(this, true).setVisible(true)));
        if (SesionUsuario.puedeAcceder("AJUSTAR_STOCK")) {
            popup.addSeparator();
            popup.add(crearMenuItem("Ajustes de Inventario", e -> new FrmAjusteInventario(this, true).setVisible(true)));
        }
        popup.show(invoker, invoker.getWidth(), 0);
    }

    private void mostrarMenuVentas(Component invoker) {
        JPopupMenu popup = new JPopupMenu();
        if (SesionUsuario.puedeAcceder("REGISTRAR_VENTA")) {
            popup.add(crearMenuItem("Registrar Venta", e -> new FrmVentas().setVisible(true)));
        }
        if (SesionUsuario.puedeAcceder("VENTAS")) {
            popup.add(crearMenuItem("Historial de Ventas", e -> new FrmHistorialVentas(this, true).setVisible(true)));
            popup.add(crearMenuItem("Devolución de Cliente", e -> new FrmDevolucionCliente(this, true).setVisible(true)));
        }
        popup.show(invoker, invoker.getWidth(), 0);
    }

    private void mostrarMenuCompras(Component invoker) {
        JPopupMenu popup = new JPopupMenu();
        if (SesionUsuario.puedeAcceder("REGISTRAR_COMPRA")) {
            popup.add(crearMenuItem("Registrar Compra", e -> new FrmCompras().setVisible(true)));
        }
        if (SesionUsuario.puedeAcceder("COMPRAS")) {
            popup.add(crearMenuItem("Historial de Compras", e -> new FrmHistorialCompras(this, true).setVisible(true)));
            popup.add(crearMenuItem("Solicitar Orden de Compra", e -> new FrmOrdenCompra(this, true).setVisible(true)));
            popup.add(crearMenuItem("Devolución a Proveedor", e -> new FrmDevolucionProveedor(this, true).setVisible(true)));
        }
        if (SesionUsuario.puedeAcceder("ADMIN_USUARIOS")) {
            popup.addSeparator();
            popup.add(crearMenuItem("Aprobación de Órdenes de Compra", e -> new FrmAprobacionOrdenCompra(this, true).setVisible(true)));
        }
        popup.show(invoker, invoker.getWidth(), 0);
    }

    private void mostrarMenuTesoreria(Component invoker) {
        JPopupMenu popup = new JPopupMenu();
        if (SesionUsuario.puedeAcceder("CAJA")) {
            popup.add(crearMenuItem("Abrir Caja", e -> new FrmAbrirCaja(this, true).setVisible(true)));
            popup.add(crearMenuItem("Cobro a Cliente", e -> new FrmCobroCliente(this, true).setVisible(true)));
            popup.add(crearMenuItem("Movimientos de Caja", e -> new FrmMovsCaja(this, true).setVisible(true)));
            popup.add(crearMenuItem("Cierre de Caja", e -> new FrmCierreCaja(this, true).setVisible(true)));
        }
        if (SesionUsuario.puedeAcceder("TESORERIA")) {
            if (popup.getComponentCount() > 0) {
                popup.addSeparator();
            }
            popup.add(crearMenuItem("Cuentas por Pagar (Proveedores)", e -> new FrmCuentasPagar(this, true).setVisible(true)));
            popup.add(crearMenuItem("Cuentas por Cobrar (Clientes)", e -> new FrmCuentasCobrar(this, true).setVisible(true)));
            popup.add(crearMenuItem("Pago a Proveedor", e -> new FrmPagoProveedor(this, true).setVisible(true)));
            popup.add(crearMenuItem("Movimientos Bancarios", e -> new FrmMovsBancarios(this, true).setVisible(true)));
        }
        popup.show(invoker, invoker.getWidth(), 0);
    }

    private void mostrarMenuContabilidad(Component invoker) {
        JPopupMenu popup = new JPopupMenu();
        popup.add(crearMenuItem("Libro Diario", e -> new FrmLibroDiario(this, true).setVisible(true)));
        popup.add(crearMenuItem("Libro Mayor", e -> new FrmLibroMayor(this, true).setVisible(true)));
        popup.add(crearMenuItem("Balanza de Comprobación", e -> new FrmBalanzaComprobacion(this, true).setVisible(true)));
        popup.addSeparator();
        popup.add(crearMenuItem("Estado de Resultados", e -> new FrmEstadoDeResultados(this, true).setVisible(true)));
        popup.add(crearMenuItem("Balance General", e -> new FrmBalanceGeneral(this, true).setVisible(true)));
        popup.show(invoker, invoker.getWidth(), 0);
    }

    private JMenuItem crearMenuItem(String texto, java.awt.event.ActionListener accion) {
        JMenuItem item = new JMenuItem(texto);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        item.setPreferredSize(new Dimension(240, 30));
        item.addActionListener(accion);
        return item;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlContenedorPrincipal = new javax.swing.JPanel();
        pnlLateral = new javax.swing.JPanel();
        pnlHeaderLateral = new javax.swing.JPanel();
        lblTituloLateral1 = new javax.swing.JLabel();
        lblTituloLateral2 = new javax.swing.JLabel();
        pnlBotonesModulos = new javax.swing.JPanel();
        btnModuloMaestros = new javax.swing.JButton();
        btnModuloInventario = new javax.swing.JButton();
        btnModuloVentas = new javax.swing.JButton();
        btnModuloCompras = new javax.swing.JButton();
        btnModuloTesoreria = new javax.swing.JButton();
        btnModuloContabilidad = new javax.swing.JButton();
        btnModuloDashboard = new javax.swing.JButton();
        btnCerrarSesion = new javax.swing.JButton();
        pnlBienvenida = new PanelBienvenidaGrafico();
        pnlCentroBienvenida = new javax.swing.JPanel();
        sepNaranjaSuperior = new javax.swing.JSeparator();
        lblBienvenidoTitulo = new javax.swing.JLabel();
        sepNaranjaMedio = new javax.swing.JSeparator();
        pnlUsuarioAvatar = new javax.swing.JPanel();
        pnlAvatarIcono = new PanelAvatarGrafico();
        pnlInfoUsuario = new javax.swing.JPanel();
        lblNombreUsuario = new javax.swing.JLabel();
        lblRolUsuario = new javax.swing.JLabel();
        lblSubtituloSistema = new javax.swing.JLabel();
        sepNaranjaInferior = new javax.swing.JSeparator();
        lblMarcaEmpresa = new javax.swing.JLabel();
        sepAzulMarca = new javax.swing.JSeparator();
        pnlDecoracionInferior = new PanelDecoracionGrafica();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Decor Home Ferronor");
        setMinimumSize(new java.awt.Dimension(1020, 680));

        pnlContenedorPrincipal.setLayout(new java.awt.BorderLayout());

        pnlLateral.setPreferredSize(new java.awt.Dimension(270, 700));
        pnlLateral.setLayout(new java.awt.BorderLayout());

        pnlHeaderLateral.setLayout(new java.awt.GridLayout(2, 1, 0, 2));

        lblTituloLateral1.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTituloLateral1.setForeground(new java.awt.Color(255, 255, 255));
        lblTituloLateral1.setText("PANEL DE ACCESO A");
        pnlHeaderLateral.add(lblTituloLateral1);

        lblTituloLateral2.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTituloLateral2.setForeground(new java.awt.Color(255, 255, 255));
        lblTituloLateral2.setText("MODULOS");
        pnlHeaderLateral.add(lblTituloLateral2);

        pnlLateral.add(pnlHeaderLateral, java.awt.BorderLayout.NORTH);

        pnlBotonesModulos.setLayout(new java.awt.GridLayout(7, 1, 0, 2));

        btnModuloMaestros.setText("  •  MAESTROS");
        pnlBotonesModulos.add(btnModuloMaestros);

        btnModuloInventario.setText("  •  INVENTARIO");
        pnlBotonesModulos.add(btnModuloInventario);

        btnModuloVentas.setText("  •  VENTAS");
        pnlBotonesModulos.add(btnModuloVentas);

        btnModuloCompras.setText("  •  COMPRAS");
        pnlBotonesModulos.add(btnModuloCompras);

        btnModuloTesoreria.setText("  •  TESORERÍA");
        pnlBotonesModulos.add(btnModuloTesoreria);

        btnModuloContabilidad.setText("  •  CONTABILIDAD");
        pnlBotonesModulos.add(btnModuloContabilidad);

        btnModuloDashboard.setText("  •  DASHBOARD / MÉTRICAS");
        pnlBotonesModulos.add(btnModuloDashboard);

        pnlLateral.add(pnlBotonesModulos, java.awt.BorderLayout.CENTER);

        btnCerrarSesion.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnCerrarSesion.setForeground(new java.awt.Color(255, 255, 255));
        btnCerrarSesion.setText("CERRAR SESIÓN");
        pnlLateral.add(btnCerrarSesion, java.awt.BorderLayout.SOUTH);

        pnlContenedorPrincipal.add(pnlLateral, java.awt.BorderLayout.WEST);

        pnlBienvenida.setLayout(new java.awt.GridBagLayout());

        pnlCentroBienvenida.setOpaque(false);
        pnlCentroBienvenida.setPreferredSize(new java.awt.Dimension(680, 620));
        pnlCentroBienvenida.setLayout(new javax.swing.BoxLayout(pnlCentroBienvenida, javax.swing.BoxLayout.Y_AXIS));

        sepNaranjaSuperior.setMaximumSize(new java.awt.Dimension(650, 4));
        sepNaranjaSuperior.setPreferredSize(new java.awt.Dimension(650, 4));
        pnlCentroBienvenida.add(sepNaranjaSuperior);

        lblBienvenidoTitulo.setFont(new java.awt.Font("Segoe UI", 1, 44)); // NOI18N
        lblBienvenidoTitulo.setForeground(new java.awt.Color(255, 255, 255));
        lblBienvenidoTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBienvenidoTitulo.setText("BIENVENIDO");
        pnlCentroBienvenida.add(lblBienvenidoTitulo);

        sepNaranjaMedio.setMaximumSize(new java.awt.Dimension(650, 4));
        sepNaranjaMedio.setPreferredSize(new java.awt.Dimension(650, 4));
        pnlCentroBienvenida.add(sepNaranjaMedio);

        pnlUsuarioAvatar.setOpaque(false);
        pnlUsuarioAvatar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 25, 0));

        pnlAvatarIcono.setPreferredSize(new java.awt.Dimension(120, 130));
        pnlUsuarioAvatar.add(pnlAvatarIcono);

        pnlInfoUsuario.setOpaque(false);
        pnlInfoUsuario.setLayout(new javax.swing.BoxLayout(pnlInfoUsuario, javax.swing.BoxLayout.Y_AXIS));

        lblNombreUsuario.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblNombreUsuario.setForeground(new java.awt.Color(255, 255, 255));
        lblNombreUsuario.setText("JEFERSON DANIEL ESPINOZA RUIZ");
        pnlInfoUsuario.add(lblNombreUsuario);

        lblRolUsuario.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblRolUsuario.setForeground(new java.awt.Color(210, 215, 220));
        lblRolUsuario.setText("ADMINISTRADOR");
        pnlInfoUsuario.add(lblRolUsuario);

        pnlUsuarioAvatar.add(pnlInfoUsuario);

        pnlCentroBienvenida.add(pnlUsuarioAvatar);

        lblSubtituloSistema.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblSubtituloSistema.setForeground(new java.awt.Color(235, 235, 235));
        lblSubtituloSistema.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSubtituloSistema.setText("AL SISTEMA DE INFORMACIÓN COMERCIAL Y CONTABLE");
        pnlCentroBienvenida.add(lblSubtituloSistema);

        sepNaranjaInferior.setMaximumSize(new java.awt.Dimension(650, 4));
        sepNaranjaInferior.setPreferredSize(new java.awt.Dimension(650, 4));
        pnlCentroBienvenida.add(sepNaranjaInferior);

        lblMarcaEmpresa.setFont(new java.awt.Font("Segoe UI", 1, 32)); // NOI18N
        lblMarcaEmpresa.setForeground(new java.awt.Color(255, 255, 255));
        lblMarcaEmpresa.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMarcaEmpresa.setText("DECOR HOME FERRONOR");
        pnlCentroBienvenida.add(lblMarcaEmpresa);

        sepAzulMarca.setMaximumSize(new java.awt.Dimension(650, 3));
        sepAzulMarca.setPreferredSize(new java.awt.Dimension(650, 3));
        pnlCentroBienvenida.add(sepAzulMarca);

        pnlDecoracionInferior.setPreferredSize(new java.awt.Dimension(650, 150));
        pnlCentroBienvenida.add(pnlDecoracionInferior);

        pnlBienvenida.add(pnlCentroBienvenida, new java.awt.GridBagConstraints());

        pnlContenedorPrincipal.add(pnlBienvenida, java.awt.BorderLayout.CENTER);

        getContentPane().add(pnlContenedorPrincipal, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void configurarBotonModulo(JButton btn, String texto) {
        btn.setText(texto);
        btn.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_BTN_NORMAL);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x4D, 0x54, 0x52)),
                BorderFactory.createEmptyBorder(0, 18, 0, 10)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(COLOR_BTN_HOVER);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(COLOR_BTN_NORMAL);
                }
            }
        });
    }

    // =========================================================================
    // COMPONENTES GRÁFICOS PERSONALIZADOS PARA RENDERIZAR EL DISEÑO DEL MOCKUP
    // =========================================================================

    /**
     * Fondo oscuro con textura sutil y degradado que replica el estilo mármol/piedra del mockup.
     */
    private static class PanelBienvenidaGrafico extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Fondo base degradado oscuro
            GradientPaint gp = new GradientPaint(0, 0, new Color(0x28, 0x2E, 0x31), w, h, new Color(0x16, 0x1A, 0x1C));
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            // Vetas sutiles decorativas estilo piedra / mármol del mockup
            g2.setColor(new Color(255, 255, 255, 12));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(w / 4, 0, w / 2, h);
            g2.drawLine(w * 3 / 4, 0, w / 3, h);
            g2.drawLine(0, h / 3, w, h / 2);

            g2.dispose();
        }
    }

    /**
     * Ícono vectorial de Avatar Profesional con engranaje institucional (tal como en el mockup).
     */
    private static class PanelAvatarGrafico extends JPanel {
        public PanelAvatarGrafico() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int cx = w / 2;

            // Traje / Hombros (Azul grisáceo)
            g2.setColor(new Color(0x71, 0x82, 0x94));
            g2.fillRoundRect(cx - 45, h - 55, 90, 55, 20, 20);

            // Camisa blanca
            g2.setColor(Color.WHITE);
            int[] xCamisa = {cx - 18, cx + 18, cx};
            int[] yCamisa = {h - 55, h - 55, h - 25};
            g2.fillPolygon(xCamisa, yCamisa, 3);

            // Corbata dorada/amarilla institucional
            g2.setColor(new Color(0xE5, 0xA9, 0x2B));
            int[] xCorbata = {cx - 6, cx + 6, cx + 4, cx, cx - 4};
            int[] yCorbata = {h - 55, h - 55, h - 30, h - 18, h - 30};
            g2.fillPolygon(xCorbata, yCorbata, 5);

            // Cuello
            g2.setColor(new Color(0xF2, 0xC4, 0x9B));
            g2.fillRect(cx - 10, h - 68, 20, 16);

            // Borde oscuro del traje
            g2.setColor(new Color(0x18, 0x1C, 0x1E));
            g2.setStroke(new BasicStroke(4f));
            g2.drawRoundRect(cx - 45, h - 55, 90, 55, 20, 20);

            // Engranaje (Cabeza ejecutiva)
            int rEngranaje = 28;
            int cyEngranaje = 35;
            g2.setColor(new Color(0x4A, 0x55, 0x5C));
            g2.fillOval(cx - rEngranaje, cyEngranaje - rEngranaje, rEngranaje * 2, rEngranaje * 2);

            // Dientes del engranaje
            for (int i = 0; i < 8; i++) {
                double ang = i * Math.PI / 4;
                int dx = (int) (Math.cos(ang) * (rEngranaje + 4));
                int dy = (int) (Math.sin(ang) * (rEngranaje + 4));
                g2.fillRect(cx + dx - 6, cyEngranaje + dy - 6, 12, 12);
            }

            // Centro hueco del engranaje
            g2.setColor(COLOR_FONDO_CENTRAL);
            g2.fillOval(cx - 14, cyEngranaje - 14, 28, 28);

            // Borde del engranaje
            g2.setColor(new Color(0x18, 0x1C, 0x1E));
            g2.setStroke(new BasicStroke(3.5f));
            g2.drawOval(cx - rEngranaje, cyEngranaje - rEngranaje, rEngranaje * 2, rEngranaje * 2);
            g2.drawOval(cx - 14, cyEngranaje - 14, 28, 28);

            g2.dispose();
        }
    }

    /**
     * Ilustración inferior: Equipo Ferronor y símbolo 3D de Porcelanatos / Cerámicos superpuestos.
     */
    private static class PanelDecoracionGrafica extends JPanel {
        public PanelDecoracionGrafica() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 1. ILUSTRACIÓN DE EQUIPO (Lado Izquierdo)
            int xBase = 50;
            int yBase = h - 10;

            Color poloNaranja = new Color(0xEA, 0x6C, 0x22);
            Color piel = new Color(0xF5, 0xC8, 0xA2);
            Color pantalon = new Color(0x5A, 0x62, 0x66);

            // 4 Personas del equipo con polos naranjas Ferronor
            int[] offsetX = {0, 45, 90, 135};
            int[] alturas = {105, 95, 90, 110};

            for (int i = 0; i < 4; i++) {
                int px = xBase + offsetX[i];
                int alt = alturas[i];
                int py = yBase - alt;

                // Cabeza
                g2.setColor(piel);
                g2.fillOval(px + 8, py, 22, 24);
                // Cabello
                g2.setColor(new Color(0x3B, 0x28, 0x1C));
                g2.fillArc(px + 7, py - 2, 24, 18, 0, 180);

                // Cuerpo / Polo naranja
                g2.setColor(poloNaranja);
                g2.fillRoundRect(px, py + 24, 38, alt - 50, 10, 10);

                // Pantalón
                g2.setColor(pantalon);
                g2.fillRect(px + 4, py + alt - 26, 30, 26);
            }

            // 2. ISOLOGO CERÁMICOS / PORCELANATOS ENCAPADOS (Lado Derecho)
            int rx = w - 180;
            int ry = h / 2 - 10;

            // Capa 1 (Base azul cyan)
            g2.setColor(new Color(0x3B, 0x82, 0xF6));
            dibujarRombo3D(g2, rx, ry + 26, 110, 45, 12, new Color(0x1E, 0x40, 0xAF));

            // Capa 2 (Media naranja Ferronor)
            g2.setColor(new Color(0xEA, 0x58, 0x0C));
            dibujarRombo3D(g2, rx, ry + 13, 110, 45, 12, new Color(0x9A, 0x34, 0x12));

            // Capa 3 (Superior blanco cerámico)
            g2.setColor(Color.WHITE);
            dibujarRombo3D(g2, rx, ry, 110, 45, 12, new Color(0xCC, 0xCC, 0xCC));

            g2.dispose();
        }

        private void dibujarRombo3D(Graphics2D g2, int x, int y, int ancho, int alto, int grosor, Color sombra) {
            Polygon rombo = new Polygon();
            rombo.addPoint(x, y + alto / 2);
            rombo.addPoint(x + ancho / 2, y);
            rombo.addPoint(x + ancho, y + alto / 2);
            rombo.addPoint(x + ancho / 2, y + alto);

            // Cara lateral / Grosor
            Polygon lateral = new Polygon();
            lateral.addPoint(x, y + alto / 2);
            lateral.addPoint(x + ancho / 2, y + alto);
            lateral.addPoint(x + ancho / 2, y + alto + grosor);
            lateral.addPoint(x, y + alto / 2 + grosor);

            Polygon lateral2 = new Polygon();
            lateral2.addPoint(x + ancho / 2, y + alto);
            lateral2.addPoint(x + ancho, y + alto / 2);
            lateral2.addPoint(x + ancho, y + alto / 2 + grosor);
            lateral2.addPoint(x + ancho / 2, y + alto + grosor);

            Color relleno = g2.getColor();
            g2.setColor(sombra);
            g2.fillPolygon(lateral);
            g2.fillPolygon(lateral2);

            g2.setColor(relleno);
            g2.fillPolygon(rombo);

            g2.setColor(new Color(0, 0, 0, 60));
            g2.drawPolygon(rombo);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JButton btnModuloCompras;
    private javax.swing.JButton btnModuloContabilidad;
    private javax.swing.JButton btnModuloDashboard;
    private javax.swing.JButton btnModuloInventario;
    private javax.swing.JButton btnModuloMaestros;
    private javax.swing.JButton btnModuloTesoreria;
    private javax.swing.JButton btnModuloVentas;
    private javax.swing.JLabel lblBienvenidoTitulo;
    private javax.swing.JLabel lblMarcaEmpresa;
    private javax.swing.JLabel lblNombreUsuario;
    private javax.swing.JLabel lblRolUsuario;
    private javax.swing.JLabel lblSubtituloSistema;
    private javax.swing.JLabel lblTituloLateral1;
    private javax.swing.JLabel lblTituloLateral2;
    private javax.swing.JPanel pnlAvatarIcono;
    private javax.swing.JPanel pnlBienvenida;
    private javax.swing.JPanel pnlBotonesModulos;
    private javax.swing.JPanel pnlCentroBienvenida;
    private javax.swing.JPanel pnlContenedorPrincipal;
    private javax.swing.JPanel pnlDecoracionInferior;
    private javax.swing.JPanel pnlHeaderLateral;
    private javax.swing.JPanel pnlInfoUsuario;
    private javax.swing.JPanel pnlLateral;
    private javax.swing.JPanel pnlUsuarioAvatar;
    private javax.swing.JSeparator sepAzulMarca;
    private javax.swing.JSeparator sepNaranjaInferior;
    private javax.swing.JSeparator sepNaranjaMedio;
    private javax.swing.JSeparator sepNaranjaSuperior;
    // End of variables declaration//GEN-END:variables
}
