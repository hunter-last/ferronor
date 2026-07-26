package com.ferronor.sic;

import com.ferronor.sic.maestros.vista.FrmCategoria;
import com.ferronor.sic.seguridad.vista.FrmLogin;
import com.ferronor.sic.shared.SesionUsuario;

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
        if (sesion.tienePermiso("TESORERIA")) {
            menuBar.add(new JMenu("Tesorería"));
        }
        if (sesion.tienePermiso("CONTABILIDAD")) {
            menuBar.add(crearMenuContabilidad());
        }
        if (sesion.tienePermiso("SEGURIDAD")) {
            menuBar.add(crearMenuSeguridad());
        }

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
        menu.add(crearItem("Stock", e -> {
            /* TODO */ }));
        menu.add(crearItem("Kardex", e -> {
            /* TODO */ }));
        if (SesionUsuario.puedeAcceder("AJUSTAR_STOCK")) {
            menu.add(crearItem("Ajustes de Inventario", e -> {
                /* TODO */ }));
        }
        return menu;
    }

    private JMenu crearMenuVentas() {
        JMenu menu = new JMenu("Ventas");
        if (SesionUsuario.puedeAcceder("REGISTRAR_VENTA")) {
            menu.add(crearItem("Registrar Venta", e -> {
                /* TODO */ }));
        }
        return menu;
    }

    private JMenu crearMenuCompras() {
        JMenu menu = new JMenu("Compras");
        if (SesionUsuario.puedeAcceder("REGISTRAR_COMPRA")) {
            menu.add(crearItem("Registrar Compra", e -> {
                /* TODO */ }));
        }
        return menu;
    }

    private JMenu crearMenuContabilidad() {
        JMenu menu = new JMenu("Contabilidad");
        if (SesionUsuario.puedeAcceder("VER_BALANCE")) {
            menu.add(crearItem("Balance", e -> {
                /* TODO */ }));
        }
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
