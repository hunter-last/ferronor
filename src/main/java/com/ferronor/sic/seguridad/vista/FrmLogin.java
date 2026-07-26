
package com.ferronor.sic.seguridad.vista;

import com.ferronor.sic.FrmPrincipal;
import com.ferronor.sic.seguridad.logica.*;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;

import javax.swing.*;
import java.awt.*;

public class FrmLogin extends JFrame {

    private final JTextField txtUsuario = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final LoginService loginService;

    public FrmLogin() {
        this.loginService = ServiceFactory.loginService();
        construirInterfaz();
    }

    private void construirInterfaz() {
        setTitle("Decor Home Ferronor — Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        setSize(360, 220);
        setLocationRelativeTo(null);
        setResizable(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblTitulo = new JLabel("Decor Home Ferronor", SwingConstants.CENTER);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));
        add(lblTitulo, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0; add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1; add(txtUsuario, gbc);

        gbc.gridy = 2; gbc.gridx = 0; add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; add(txtPassword, gbc);

        JButton btnIngresar = new JButton("Ingresar");
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        add(btnIngresar, gbc);

        btnIngresar.addActionListener(e -> intentarLogin());
        txtPassword.addActionListener(e -> intentarLogin()); // Enter también dispara el login
    }

    private void intentarLogin() {
        String usuario = txtUsuario.getText();
        String password = new String(txtPassword.getPassword());

        RespuestaOperacion<Void> r = loginService.iniciarSesion(usuario, password);

        if (!r.isExito()) {
            JOptionPane.showMessageDialog(this, r.getMensaje(), "Error de acceso", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            return;
        }

        new FrmPrincipal().setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmLogin().setVisible(true));
    }
}