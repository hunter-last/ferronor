package com.ferronor.sic;

import com.ferronor.sic.compras.vista.FrmCompras;
import com.ferronor.sic.compras.vista.FrmDevolucionProveedor;
import com.ferronor.sic.compras.vista.FrmOrdenCompra;
import com.ferronor.sic.compras.vista.FrmPagoProveedor;
import com.ferronor.sic.contabilidad.vista.FrmLibroDiario;
import com.ferronor.sic.contabilidad.vista.FrmLibroMayor;
import com.ferronor.sic.inventario.vista.FrmAjusteInventario;
import com.ferronor.sic.inventario.vista.FrmConsultarStock;
import com.ferronor.sic.inventario.vista.FrmKardex;
import com.ferronor.sic.maestros.vista.FrmCategoria;
import com.ferronor.sic.seguridad.vista.FrmLogin;
import com.ferronor.sic.shared.SesionUsuario;
import com.ferronor.sic.tesoreria.vista.FrmCierreCaja;
import com.ferronor.sic.tesoreria.vista.FrmCuentasCobrar;
import com.ferronor.sic.tesoreria.vista.FrmCuentasPagar;
import com.ferronor.sic.tesoreria.vista.FrmMovsBancarios;
import com.ferronor.sic.tesoreria.vista.FrmMovsCaja;
import com.ferronor.sic.ventas.vista.FrmCobroCliente;
import com.ferronor.sic.ventas.vista.FrmDevolucionCliente;
import com.ferronor.sic.ventas.vista.FrmVentas;

import javax.swing.*;

public class FrmPrincipal extends JFrame {

    public FrmPrincipal() {
        construirInterfaz();
    }

    private void construirInterfaz() {
        SesionUsuario sesion = SesionUsuario.actual();

        setTitle("Decor Home Ferronor — " + sesion.getNombreCompleto() + " (" + sesion.getNombreRol() + ")");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();

        if (sesion.tienePermiso("MAESTROS")) {
            menuBar.add(crearMenuMaestros());
        }
        if (sesion.tienePermiso("INVENTARIO")) {
            menuBar.add(crearMenuInventario());
        }
        if (sesion.tienePermiso("VENTAS")) {
            menuBar.add(crearMenuVentas());
        }
        if (sesion.tienePermiso("COMPRAS")) {
            menuBar.add(crearMenuCompras());
        }
        // Tesorería debe aparecer para Cajero (permiso CAJA) o para
        // Tesorería (permiso TESORERIA) — son operaciones distintas
        // del mismo módulo, no la misma audiencia.
        if (sesion.tienePermiso("TESORERIA") || sesion.tienePermiso("CAJA")) {
            menuBar.add(crearMenuTesoreria());
        }
        if (sesion.tienePermiso("CONTABILIDAD")) {
            menuBar.add(crearMenuContabilidad());
        }
        // Seguridad: FrmUsuarios/FrmRoles todavía no existen como
        // pantallas — no se agrega el menú hasta tener algo funcional
        // que mostrar (evita ítems "aún no implementado").

        JMenu menuSesion = new JMenu("Sesión");
        menuSesion.add(crearItem("Cerrar sesión", e -> cerrarSesion()));
        menuBar.add(menuSesion);
        setJMenuBar(menuBar);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cerrarSesion();
            }
        });
    }

    private JMenu crearMenuMaestros() {
        JMenu menu = new JMenu("Maestros");
        menu.add(crearItem("Categorías", e -> {
            new FrmCategoria().setVisible(true);
        }));
        return menu;
    }

    private JMenu crearMenuInventario() {
        JMenu menu = new JMenu("Inventario");

        menu.add(crearItem("Consultar Stock", e -> {
            new FrmConsultarStock(this, true).setVisible(true);
        }));

        menu.add(crearItem("Kardex", e -> {
            new FrmKardex(this, true).setVisible(true);
        }));

        if (SesionUsuario.puedeAcceder("AJUSTAR_STOCK")) {
            menu.add(crearItem("Ajustes de Inventario", e -> {
                new FrmAjusteInventario(this, true).setVisible(true);
            }));
        }

        return menu;
    }

    private JMenu crearMenuVentas() {
        JMenu menu = new JMenu("Ventas");

        if (SesionUsuario.puedeAcceder("REGISTRAR_VENTA")) {
            menu.add(crearItem("Registrar Venta", e -> {
                new FrmVentas().setVisible(true);
            }));
        }

        if (SesionUsuario.puedeAcceder("VENTAS")) {
            menu.add(crearItem("Devolución de Cliente", e -> {
                new FrmDevolucionCliente(this, true).setVisible(true);
            }));
        }

        // Cuentas por Cobrar vive en el menú Tesorería (ver
        // crearMenuTesoreria), no aquí — mismo criterio de paquetes
        // ya definido en el informe (Ventas→Tesorería).
        return menu;
    }

    private JMenu crearMenuCompras() {
        JMenu menu = new JMenu("Compras");

        if (SesionUsuario.puedeAcceder("REGISTRAR_COMPRA")) {
            menu.add(crearItem("Registrar Compra", e -> {
                new FrmCompras().setVisible(true);
            }));
        }

        if (SesionUsuario.puedeAcceder("COMPRAS")) {
            menu.add(crearItem("Solicitar Orden de Compra", e -> {
                new FrmOrdenCompra(this, true).setVisible(true);
            }));

            menu.add(crearItem("Devolución a Proveedor", e -> {
                new FrmDevolucionProveedor(this, true).setVisible(true);
            }));
        }

        // Cuentas por Pagar y Pago a Proveedor viven en el menú
        // Tesorería (ver crearMenuTesoreria), no aquí — mismo criterio
        // de paquetes ya definido en el informe (Compras→Tesorería).
        return menu;
    }

    private JMenu crearMenuTesoreria() {
        JMenu menu = new JMenu("Tesorería");

        // Operación diaria de caja: el Cajero la necesita (permiso CAJA)
        // aunque no tenga el permiso general TESORERIA.
        if (SesionUsuario.puedeAcceder("CAJA")) {
            menu.add(crearItem("Cobro a Cliente", e -> {
                new FrmCobroCliente(this, true).setVisible(true);
            }));

            menu.add(crearItem("Movimientos de Caja", e -> {
                new FrmMovsCaja(this, true).setVisible(true);
            }));

            menu.add(crearItem("Cierre de Caja", e -> {
                new FrmCierreCaja(this, true).setVisible(true);
            }));
        }

        // Control de tesorería propiamente dicho: valida liquidaciones,
        // paga, deposita, consulta cuentas — permiso TESORERIA.
        if (SesionUsuario.puedeAcceder("TESORERIA")) {
            if (menu.getItemCount() > 0) {
                menu.addSeparator();
            }

            menu.add(crearItem("Cuentas por Pagar", e -> {
                new FrmCuentasPagar(this, true).setVisible(true);
            }));

            menu.add(crearItem("Cuentas por Cobrar", e -> {
                new FrmCuentasCobrar(this, true).setVisible(true);
            }));

            menu.add(crearItem("Pago a Proveedor", e -> {
                new FrmPagoProveedor(this, true).setVisible(true);
            }));

            menu.add(crearItem("Movimientos Bancarios", e -> {
                new FrmMovsBancarios(this, true).setVisible(true);
            }));
        }

        return menu;
    }

    private JMenu crearMenuContabilidad() {
        JMenu menu = new JMenu("Contabilidad");

        menu.add(crearItem("Libro Diario", e -> {
            new FrmLibroDiario(this, true).setVisible(true);
        }));

        menu.add(crearItem("Libro Mayor", e -> {
            new FrmLibroMayor(this, true).setVisible(true);
        }));

        // Balance de Comprobación, Estado de Resultados y Balance
        // General se agregan cuando existan sus FrmX reales.
        return menu;
    }
    
     private JMenu crearMenuSeguridad() {
        JMenu menu = new JMenu("Seguridad");
        if (SesionUsuario.puedeAcceder("ADMIN_USUARIOS")) {
            menu.add(crearItem("Usuarios", e -> {
                /* TODO */ }));
            menu.add(crearItem("Roles", e -> {
                /* TODO */ }));
        }
        return menu;
    }


    private JMenuItem crearItem(String texto, java.awt.event.ActionListener accion) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(accion);
        return item;
    }

    private void cerrarSesion() {
        SesionUsuario.cerrar();
        dispose();
        new FrmLogin().setVisible(true);
    }

}
