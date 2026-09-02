package com.ferronor.sic.seguridad.vista;

import com.ferronor.sic.FrmPrincipal;
import com.ferronor.sic.seguridad.logica.LoginService;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class FrmLogin extends JFrame {

    private final JTextField txtUsuario = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);

    private final LoginService loginService;

    public FrmLogin() {
        this.loginService = ServiceFactory.loginService();

        configurarTema();
        construirInterfaz();
    }

    private void configurarTema() {
        /*
         * FlatLaf ya está incluido en pom.xml.
         * Estas propiedades complementan el tema oscuro para
         * darle una apariencia más moderna.
         */
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollBar.thumbArc", 999);
    }

    private void construirInterfaz() {

        setTitle("Decor Home Ferronor — Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBorder(new EmptyBorder(25, 30, 25, 30));

        // =========================================================
        // ENCABEZADO
        // =========================================================
        JLabel lblTitulo = new JLabel("DECOR HOME FERRONOR");
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 22f));

        JLabel lblSubtitulo = new JLabel("Sistema de Gestión Comercial y Contable");
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSubtitulo.setFont(lblSubtitulo.getFont().deriveFont(Font.PLAIN, 12f));

        panelPrincipal.add(lblTitulo);
        panelPrincipal.add(Box.createVerticalStrut(5));
        panelPrincipal.add(lblSubtitulo);
        panelPrincipal.add(Box.createVerticalStrut(28));

        // =========================================================
        // USUARIO
        // =========================================================
        JLabel lblUsuario = new JLabel("Usuario");
        lblUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblUsuario.setHorizontalAlignment(SwingConstants.CENTER);
        txtUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtUsuario.setPreferredSize(new Dimension(280, 38));

        panelPrincipal.add(lblUsuario);
        panelPrincipal.add(Box.createVerticalStrut(6));
        panelPrincipal.add(txtUsuario);

        panelPrincipal.add(Box.createVerticalStrut(16));

        // =========================================================
        // CONTRASEÑA
        // =========================================================
        JLabel lblPassword = new JLabel("Contraseña");
        lblPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPassword.setHorizontalAlignment(SwingConstants.CENTER);

        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPassword.setPreferredSize(new Dimension(280, 38));

        panelPrincipal.add(lblPassword);
        panelPrincipal.add(Box.createVerticalStrut(6));
        panelPrincipal.add(txtPassword);

        panelPrincipal.add(Box.createVerticalStrut(24));

        // =========================================================
        // BOTÓN
        // =========================================================
        JButton btnIngresar = new JButton("INGRESAR");
        btnIngresar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnIngresar.setPreferredSize(new Dimension(180, 42));
        btnIngresar.setMaximumSize(new Dimension(180, 42));
        btnIngresar.setFont(
                btnIngresar.getFont().deriveFont(Font.BOLD, 13f)
        );

        panelPrincipal.add(btnIngresar);

        panelPrincipal.add(Box.createVerticalStrut(12));

        JLabel lblPie = new JLabel("© Ferronor");
        lblPie.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPie.setFont(lblPie.getFont().deriveFont(Font.PLAIN, 10f));

        panelPrincipal.add(lblPie);

        // =========================================================
        // AGREGAR AL FONDO
        // =========================================================
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;

        fondo.add(panelPrincipal, gbc);

        setContentPane(fondo);

        // =========================================================
        // EVENTOS
        // =========================================================
        btnIngresar.addActionListener(e -> intentarLogin());

        txtPassword.addActionListener(e -> intentarLogin());

        SwingUtilities.invokeLater(() -> {
            txtUsuario.requestFocusInWindow();
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void intentarLogin() {

        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (usuario.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ingrese su usuario.",
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );
            txtUsuario.requestFocusInWindow();
            return;
        }

        if (password.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ingrese su contraseña.",
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );
            txtPassword.requestFocusInWindow();
            return;
        }

        try {
            RespuestaOperacion<Void> resultado
                    = loginService.iniciarSesion(usuario, password);

            if (!resultado.isExito()) {

                JOptionPane.showMessageDialog(
                        this,
                        resultado.getMensaje(),
                        "Error de acceso",
                        JOptionPane.ERROR_MESSAGE
                );

                txtPassword.setText("");
                txtPassword.requestFocusInWindow();
                return;
            }

            new FrmPrincipal().setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error al conectar con la base de datos:\n" + ex.getMessage(),
                    "Error del Sistema",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {

        FlatDarkLaf.setup();

        SwingUtilities.invokeLater(() -> {
            new FrmLogin().setVisible(true);
        });
    }
}
