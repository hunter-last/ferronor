package com.ferronor.sic.dashboard.vista;

import com.ferronor.sic.dashboard.logica.DashboardService;
import com.ferronor.sic.dashboard.modelo.dto.AlertaStockDTO;
import com.ferronor.sic.dashboard.modelo.dto.DashboardKpisDTO;
import com.ferronor.sic.dashboard.modelo.dto.TopProductoDTO;
import com.ferronor.sic.dashboard.modelo.dto.UltimaVentaDTO;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;
import com.ferronor.sic.shared.SesionUsuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tablero de Control y Métricas del Sistema Decor Home Ferronor.
 */
public class FrmDashboard extends javax.swing.JFrame {

    private final DashboardService dashboardService = ServiceFactory.dashboardService();
    private static final DecimalFormat FORMATO_MONEDA = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy — hh:mm a");

    public FrmDashboard() {
        initComponents();
        configurarVentana();
        configurarIconos();
        aplicarVisibilidadPorRol();
        cargarDatosDashboard();
    }

    private void configurarVentana() {
        SesionUsuario sesion = SesionUsuario.actual();
        if (sesion != null) {
            setTitle("Decor Home Ferronor — Tablero de Control (" + sesion.getNombreRol() + ")");
            lblUsuarioInfo.setText("Usuario: " + sesion.getNombreCompleto() + " (" + sesion.getNombreRol() + ")");
        } else {
            setTitle("Decor Home Ferronor — Tablero de Control");
            lblUsuarioInfo.setText("Usuario: Sesión activa");
        }
        lblFechaHora.setText(LocalDateTime.now().format(FORMATO_FECHA_HORA));
        setLocationRelativeTo(null);
    }

    private void configurarIconos() {
        btnActualizar.setIcon(obtenerIcono("Refresh.png"));
        tabPaneles.setIconAt(0, obtenerIcono("Best.png"));
        tabPaneles.setIconAt(1, obtenerIcono("Warning.png"));
        tabPaneles.setIconAt(2, obtenerIcono("Clock.png"));
    }

    private ImageIcon obtenerIcono(String nombre) {
        String[] rutas = {
            "/com/ferronor/sic/images/" + nombre,
            "/images/" + nombre,
            "images/" + nombre
        };
        for (String r : rutas) {
            java.net.URL url = getClass().getResource(r);
            if (url != null) {
                return new ImageIcon(url);
            }
        }
        return null;
    }

    private void aplicarVisibilidadPorRol() {
        SesionUsuario sesion = SesionUsuario.actual();
        if (sesion == null) return;
        String rol = sesion.getNombreRol();

        if ("Cajero".equalsIgnoreCase(rol)) {
            pnlKpiVentas.setBackground(new Color(30, 45, 35));
            pnlKpiTesoreria.setBackground(new Color(30, 40, 45));
            tabPaneles.setSelectedIndex(2);
        } else if ("Logistica".equalsIgnoreCase(rol)) {
            pnlKpiStock.setBackground(new Color(45, 30, 30));
            pnlKpiCompras.setBackground(new Color(45, 40, 30));
            tabPaneles.setSelectedIndex(1);
        } else if ("Tesoreria".equalsIgnoreCase(rol)) {
            pnlKpiCxCobrar.setBackground(new Color(45, 40, 30));
            pnlKpiCxPagar.setBackground(new Color(45, 30, 30));
            pnlKpiTesoreria.setBackground(new Color(30, 40, 45));
        }
    }

    public void cargarDatosDashboard() {
        lblFechaHora.setText(LocalDateTime.now().format(FORMATO_FECHA_HORA));
        cargarKpis();
        cargarTopProductos();
        cargarAlertasStock();
        cargarUltimasVentas();
    }

    private void cargarKpis() {
        RespuestaOperacion<DashboardKpisDTO> resp = dashboardService.obtenerKpis();
        if (!resp.isExito() || resp.getResultado() == null) {
            return;
        }

        DashboardKpisDTO k = resp.getResultado();

        lblKpiVentasMonto.setText("S/ " + FORMATO_MONEDA.format(k.getTotalVentasMes()));
        lblKpiVentasSub.setText(k.getCantVentasMes() + " ventas mes | Hoy: S/ " + FORMATO_MONEDA.format(k.getTotalVentasHoy()));

        lblKpiCxCobrarMonto.setText("S/ " + FORMATO_MONEDA.format(k.getCxCobrarPendientes()));
        lblKpiCxCobrarSub.setText("Vencidas: S/ " + FORMATO_MONEDA.format(k.getCxCobrarVencidas()));

        lblKpiCxPagarMonto.setText("S/ " + FORMATO_MONEDA.format(k.getCxPagarPendientes()));
        lblKpiCxPagarSub.setText("Vencidas: S/ " + FORMATO_MONEDA.format(k.getCxPagarVencidas()));

        lblKpiTesoreriaMonto.setText("S/ " + FORMATO_MONEDA.format(k.getSaldoCajaTotal().add(k.getSaldoBancosTotal())));
        lblKpiTesoreriaSub.setText("Caja: S/ " + FORMATO_MONEDA.format(k.getSaldoCajaTotal()) + " | Bancos: S/ " + FORMATO_MONEDA.format(k.getSaldoBancosTotal()));

        lblKpiStockMonto.setText(k.getCantStockBajo() + " PRODS.");
        lblKpiStockSub.setText(k.getCantStockAgotado() + " agotados | Bajo stock mín.");

        lblKpiComprasMonto.setText("S/ " + FORMATO_MONEDA.format(k.getTotalComprasMes()));
        lblKpiComprasSub.setText(k.getCantComprasMes() + " compras | " + k.getOrdenesCompraPendientes() + " órdenes pend.");
    }

    private void cargarTopProductos() {
        DefaultTableModel model = (DefaultTableModel) tblTopProductos.getModel();
        model.setRowCount(0);

        RespuestaOperacion<List<TopProductoDTO>> resp = dashboardService.obtenerTopProductos(5);
        if (resp.isExito() && resp.getResultado() != null) {
            int pos = 1;
            for (TopProductoDTO p : resp.getResultado()) {
                model.addRow(new Object[]{
                    pos++,
                    p.getCodigo(),
                    p.getNombre(),
                    p.getCategoria(),
                    FORMATO_MONEDA.format(p.getCantidadVendida()),
                    "S/ " + FORMATO_MONEDA.format(p.getTotalRecaudado())
                });
            }
        }
    }

    private void cargarAlertasStock() {
        DefaultTableModel model = (DefaultTableModel) tblAlertasStock.getModel();
        model.setRowCount(0);

        RespuestaOperacion<List<AlertaStockDTO>> resp = dashboardService.obtenerAlertasStock(0);
        if (resp.isExito() && resp.getResultado() != null) {
            for (AlertaStockDTO a : resp.getResultado()) {
                model.addRow(new Object[]{
                    a.getCodigo(),
                    a.getNombre(),
                    a.getCategoria(),
                    a.getUnidad(),
                    FORMATO_MONEDA.format(a.getStockActual()),
                    FORMATO_MONEDA.format(a.getStockMinimo()),
                    a.getEstadoStock()
                });
            }
        }
    }

    private void cargarUltimasVentas() {
        DefaultTableModel model = (DefaultTableModel) tblUltimasVentas.getModel();
        model.setRowCount(0);

        RespuestaOperacion<List<UltimaVentaDTO>> resp = dashboardService.obtenerUltimasVentas(5);
        if (resp.isExito() && resp.getResultado() != null) {
            for (UltimaVentaDTO v : resp.getResultado()) {
                String fechaStr = v.getFecha() != null ? v.getFecha().format(FORMATO_FECHA_HORA) : "-";
                model.addRow(new Object[]{
                    v.getIdVenta(),
                    v.getComprobante(),
                    v.getCliente(),
                    fechaStr,
                    v.getFormaPago(),
                    "S/ " + FORMATO_MONEDA.format(v.getTotal()),
                    v.getEstado()
                });
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlHeader = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        lblUsuarioInfo = new javax.swing.JLabel();
        lblFechaHora = new javax.swing.JLabel();
        btnActualizar = new javax.swing.JButton();
        pnlKpis = new javax.swing.JPanel();
        pnlKpiVentas = new javax.swing.JPanel();
        lblKpiVentasMonto = new javax.swing.JLabel();
        lblKpiVentasSub = new javax.swing.JLabel();
        pnlKpiCxCobrar = new javax.swing.JPanel();
        lblKpiCxCobrarMonto = new javax.swing.JLabel();
        lblKpiCxCobrarSub = new javax.swing.JLabel();
        pnlKpiCxPagar = new javax.swing.JPanel();
        lblKpiCxPagarMonto = new javax.swing.JLabel();
        lblKpiCxPagarSub = new javax.swing.JLabel();
        pnlKpiTesoreria = new javax.swing.JPanel();
        lblKpiTesoreriaMonto = new javax.swing.JLabel();
        lblKpiTesoreriaSub = new javax.swing.JLabel();
        pnlKpiStock = new javax.swing.JPanel();
        lblKpiStockMonto = new javax.swing.JLabel();
        lblKpiStockSub = new javax.swing.JLabel();
        pnlKpiCompras = new javax.swing.JPanel();
        lblKpiComprasMonto = new javax.swing.JLabel();
        lblKpiComprasSub = new javax.swing.JLabel();
        tabPaneles = new javax.swing.JTabbedPane();
        pnlTabTopProductos = new javax.swing.JPanel();
        spnlTopProductos = new javax.swing.JScrollPane();
        tblTopProductos = new javax.swing.JTable();
        pnlTabAlertasStock = new javax.swing.JPanel();
        spnlAlertasStock = new javax.swing.JScrollPane();
        tblAlertasStock = new javax.swing.JTable();
        pnlTabUltimasVentas = new javax.swing.JPanel();
        spnlUltimasVentas = new javax.swing.JScrollPane();
        tblUltimasVentas = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Decor Home Ferronor — Tablero de Control");
        setMinimumSize(new java.awt.Dimension(1000, 680));
        setPreferredSize(new java.awt.Dimension(1050, 720));

        pnlHeader.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTitulo.setText("TABLERO DE CONTROL Y MÉTRICAS");

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblSubtitulo.setText("Decor Home Ferronor — Indicadores Clave del Negocio en Tiempo Real");

        lblUsuarioInfo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblUsuarioInfo.setText("Usuario: Administrador (Admin)");

        lblFechaHora.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblFechaHora.setText("01/09/2026 — 12:00");

        btnActualizar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnActualizar.setText("Actualizar Métricas");
        btnActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlHeaderLayout = new javax.swing.GroupLayout(pnlHeader);
        pnlHeader.setLayout(pnlHeaderLayout);
        pnlHeaderLayout.setHorizontalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitulo)
                    .addComponent(lblSubtitulo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblUsuarioInfo)
                    .addComponent(lblFechaHora))
                .addGap(18, 18, 18)
                .addComponent(btnActualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );
        pnlHeaderLayout.setVerticalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addComponent(lblTitulo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSubtitulo))
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addComponent(lblUsuarioInfo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblFechaHora))
                    .addComponent(btnActualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );

        pnlKpis.setLayout(new java.awt.GridLayout(2, 3, 10, 10));

        pnlKpiVentas.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "VENTAS DEL MES", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N

        lblKpiVentasMonto.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblKpiVentasMonto.setForeground(new java.awt.Color(76, 175, 80));
        lblKpiVentasMonto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblKpiVentasMonto.setText("S/ 0.00");

        lblKpiVentasSub.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblKpiVentasSub.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblKpiVentasSub.setText("0 ventas mes | Hoy: S/ 0.00");

        javax.swing.GroupLayout pnlKpiVentasLayout = new javax.swing.GroupLayout(pnlKpiVentas);
        pnlKpiVentas.setLayout(pnlKpiVentasLayout);
        pnlKpiVentasLayout.setHorizontalGroup(
            pnlKpiVentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpiVentasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlKpiVentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKpiVentasMonto, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                    .addComponent(lblKpiVentasSub, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlKpiVentasLayout.setVerticalGroup(
            pnlKpiVentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpiVentasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblKpiVentasMonto)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblKpiVentasSub)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlKpis.add(pnlKpiVentas);

        pnlKpiCxCobrar.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "POR COBRAR (CLIENTES)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N

        lblKpiCxCobrarMonto.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblKpiCxCobrarMonto.setForeground(new java.awt.Color(255, 152, 0));
        lblKpiCxCobrarMonto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblKpiCxCobrarMonto.setText("S/ 0.00");

        lblKpiCxCobrarSub.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblKpiCxCobrarSub.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblKpiCxCobrarSub.setText("Vencidas: S/ 0.00");

        javax.swing.GroupLayout pnlKpiCxCobrarLayout = new javax.swing.GroupLayout(pnlKpiCxCobrar);
        pnlKpiCxCobrar.setLayout(pnlKpiCxCobrarLayout);
        pnlKpiCxCobrarLayout.setHorizontalGroup(
            pnlKpiCxCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpiCxCobrarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlKpiCxCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKpiCxCobrarMonto, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                    .addComponent(lblKpiCxCobrarSub, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlKpiCxCobrarLayout.setVerticalGroup(
            pnlKpiCxCobrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpiCxCobrarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblKpiCxCobrarMonto)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblKpiCxCobrarSub)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlKpis.add(pnlKpiCxCobrar);

        pnlKpiCxPagar.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "POR PAGAR (PROVEEDORES)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N

        lblKpiCxPagarMonto.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblKpiCxPagarMonto.setForeground(new java.awt.Color(244, 67, 54));
        lblKpiCxPagarMonto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblKpiCxPagarMonto.setText("S/ 0.00");

        lblKpiCxPagarSub.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblKpiCxPagarSub.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblKpiCxPagarSub.setText("Vencidas: S/ 0.00");

        javax.swing.GroupLayout pnlKpiCxPagarLayout = new javax.swing.GroupLayout(pnlKpiCxPagar);
        pnlKpiCxPagar.setLayout(pnlKpiCxPagarLayout);
        pnlKpiCxPagarLayout.setHorizontalGroup(
            pnlKpiCxPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpiCxPagarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlKpiCxPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKpiCxPagarMonto, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                    .addComponent(lblKpiCxPagarSub, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlKpiCxPagarLayout.setVerticalGroup(
            pnlKpiCxPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpiCxPagarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblKpiCxPagarMonto)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblKpiCxPagarSub)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlKpis.add(pnlKpiCxPagar);

        pnlKpiTesoreria.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "LIQUIDEZ TOTAL (CAJA + BANCOS)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N

        lblKpiTesoreriaMonto.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblKpiTesoreriaMonto.setForeground(new java.awt.Color(33, 150, 243));
        lblKpiTesoreriaMonto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblKpiTesoreriaMonto.setText("S/ 0.00");

        lblKpiTesoreriaSub.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblKpiTesoreriaSub.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblKpiTesoreriaSub.setText("Caja: S/ 0.00 | Bancos: S/ 0.00");

        javax.swing.GroupLayout pnlKpiTesoreriaLayout = new javax.swing.GroupLayout(pnlKpiTesoreria);
        pnlKpiTesoreria.setLayout(pnlKpiTesoreriaLayout);
        pnlKpiTesoreriaLayout.setHorizontalGroup(
            pnlKpiTesoreriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpiTesoreriaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlKpiTesoreriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKpiTesoreriaMonto, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                    .addComponent(lblKpiTesoreriaSub, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlKpiTesoreriaLayout.setVerticalGroup(
            pnlKpiTesoreriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpiTesoreriaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblKpiTesoreriaMonto)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblKpiTesoreriaSub)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlKpis.add(pnlKpiTesoreria);

        pnlKpiStock.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ALERTAS DE INVENTARIO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N

        lblKpiStockMonto.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblKpiStockMonto.setForeground(new java.awt.Color(233, 30, 99));
        lblKpiStockMonto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblKpiStockMonto.setText("0 PRODS.");

        lblKpiStockSub.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblKpiStockSub.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblKpiStockSub.setText("0 agotados | Bajo stock mín.");

        javax.swing.GroupLayout pnlKpiStockLayout = new javax.swing.GroupLayout(pnlKpiStock);
        pnlKpiStock.setLayout(pnlKpiStockLayout);
        pnlKpiStockLayout.setHorizontalGroup(
            pnlKpiStockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpiStockLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlKpiStockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKpiStockMonto, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                    .addComponent(lblKpiStockSub, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlKpiStockLayout.setVerticalGroup(
            pnlKpiStockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpiStockLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblKpiStockMonto)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblKpiStockSub)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlKpis.add(pnlKpiStock);

        pnlKpiCompras.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "COMPRAS DEL MES", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N

        lblKpiComprasMonto.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblKpiComprasMonto.setForeground(new java.awt.Color(156, 39, 176));
        lblKpiComprasMonto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblKpiComprasMonto.setText("S/ 0.00");

        lblKpiComprasSub.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblKpiComprasSub.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblKpiComprasSub.setText("0 compras | 0 órdenes pend.");

        javax.swing.GroupLayout pnlKpiComprasLayout = new javax.swing.GroupLayout(pnlKpiCompras);
        pnlKpiCompras.setLayout(pnlKpiComprasLayout);
        pnlKpiComprasLayout.setHorizontalGroup(
            pnlKpiComprasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpiComprasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlKpiComprasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKpiComprasMonto, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                    .addComponent(lblKpiComprasSub, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlKpiComprasLayout.setVerticalGroup(
            pnlKpiComprasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpiComprasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblKpiComprasMonto)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblKpiComprasSub)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlKpis.add(pnlKpiCompras);

        tabPaneles.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        tblTopProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "#", "Código", "Producto", "Categoría", "Cant. Vendida", "Total Recaudado (S/)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblTopProductos.setRowHeight(24);
        spnlTopProductos.setViewportView(tblTopProductos);

        javax.swing.GroupLayout pnlTabTopProductosLayout = new javax.swing.GroupLayout(pnlTabTopProductos);
        pnlTabTopProductos.setLayout(pnlTabTopProductosLayout);
        pnlTabTopProductosLayout.setHorizontalGroup(
            pnlTabTopProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTabTopProductosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlTopProductos, javax.swing.GroupLayout.DEFAULT_SIZE, 1014, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlTabTopProductosLayout.setVerticalGroup(
            pnlTabTopProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTabTopProductosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlTopProductos, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPaneles.addTab("Top 5 Productos Más Vendidos", pnlTabTopProductos);

        tblAlertasStock.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Código", "Producto", "Categoría", "U.M.", "Stock Actual", "Stock Mínimo", "Estado"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblAlertasStock.setRowHeight(24);
        spnlAlertasStock.setViewportView(tblAlertasStock);

        javax.swing.GroupLayout pnlTabAlertasStockLayout = new javax.swing.GroupLayout(pnlTabAlertasStock);
        pnlTabAlertasStock.setLayout(pnlTabAlertasStockLayout);
        pnlTabAlertasStockLayout.setHorizontalGroup(
            pnlTabAlertasStockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTabAlertasStockLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlAlertasStock, javax.swing.GroupLayout.DEFAULT_SIZE, 1014, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlTabAlertasStockLayout.setVerticalGroup(
            pnlTabAlertasStockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTabAlertasStockLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlAlertasStock, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPaneles.addTab("Alertas de Stock Crítico", pnlTabAlertasStock);

        tblUltimasVentas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "ID Venta", "Comprobante", "Cliente", "Fecha / Hora", "Forma Pago", "Total", "Estado"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblUltimasVentas.setRowHeight(24);
        spnlUltimasVentas.setViewportView(tblUltimasVentas);

        javax.swing.GroupLayout pnlTabUltimasVentasLayout = new javax.swing.GroupLayout(pnlTabUltimasVentas);
        pnlTabUltimasVentas.setLayout(pnlTabUltimasVentasLayout);
        pnlTabUltimasVentasLayout.setHorizontalGroup(
            pnlTabUltimasVentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTabUltimasVentasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlUltimasVentas, javax.swing.GroupLayout.DEFAULT_SIZE, 1014, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlTabUltimasVentasLayout.setVerticalGroup(
            pnlTabUltimasVentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTabUltimasVentasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlUltimasVentas, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPaneles.addTab("Últimas 5 Ventas", pnlTabUltimasVentas);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlKpis, javax.swing.GroupLayout.DEFAULT_SIZE, 1038, Short.MAX_VALUE)
                    .addComponent(tabPaneles))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlKpis, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabPaneles, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarActionPerformed
        cargarDatosDashboard();
    }//GEN-LAST:event_btnActualizarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JLabel lblFechaHora;
    private javax.swing.JLabel lblKpiComprasMonto;
    private javax.swing.JLabel lblKpiComprasSub;
    private javax.swing.JLabel lblKpiCxCobrarMonto;
    private javax.swing.JLabel lblKpiCxCobrarSub;
    private javax.swing.JLabel lblKpiCxPagarMonto;
    private javax.swing.JLabel lblKpiCxPagarSub;
    private javax.swing.JLabel lblKpiStockMonto;
    private javax.swing.JLabel lblKpiStockSub;
    private javax.swing.JLabel lblKpiTesoreriaMonto;
    private javax.swing.JLabel lblKpiTesoreriaSub;
    private javax.swing.JLabel lblKpiVentasMonto;
    private javax.swing.JLabel lblKpiVentasSub;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUsuarioInfo;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlKpiCompras;
    private javax.swing.JPanel pnlKpiCxCobrar;
    private javax.swing.JPanel pnlKpiCxPagar;
    private javax.swing.JPanel pnlKpiStock;
    private javax.swing.JPanel pnlKpiTesoreria;
    private javax.swing.JPanel pnlKpiVentas;
    private javax.swing.JPanel pnlKpis;
    private javax.swing.JPanel pnlTabAlertasStock;
    private javax.swing.JPanel pnlTabTopProductos;
    private javax.swing.JPanel pnlTabUltimasVentas;
    private javax.swing.JScrollPane spnlAlertasStock;
    private javax.swing.JScrollPane spnlTopProductos;
    private javax.swing.JScrollPane spnlUltimasVentas;
    private javax.swing.JTabbedPane tabPaneles;
    private javax.swing.JTable tblAlertasStock;
    private javax.swing.JTable tblTopProductos;
    private javax.swing.JTable tblUltimasVentas;
    // End of variables declaration//GEN-END:variables
}
