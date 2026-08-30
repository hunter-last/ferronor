package com.ferronor.sic;

import com.ferronor.sic.seguridad.vista.FrmLogin;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        FlatDarkLaf.setup();

        SwingUtilities.invokeLater(() -> {
            new FrmLogin().setVisible(true);
        });
    }
}
