package com.ferronor.sic.maestros.vista;

import com.ferronor.sic.maestros.logica.CategoriaService;
import com.ferronor.sic.maestros.modelo.Categoria;
import com.ferronor.sic.shared.FrmBase;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.ServiceFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class FrmCategoria extends FrmBase {

    private final CategoriaService categoriaService = ServiceFactory.categoriaService();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(new Object[]{"Id", "Nombre"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JTextField txtBuscar = new JTextField(20);

    public FrmCategoria() {
        super("MAESTROS");
        construirInterfaz();
        cargarTabla(null);
    }

    private void construirInterfaz() {
        setTitle("Categorías");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.add(new JLabel("Buscar:"));
        panelBusqueda.add(txtBuscar);
        JButton btnBuscar = new JButton("Buscar");
        JButton btnLimpiar = new JButton("Mostrar todas");
        panelBusqueda.add(btnBuscar);
        panelBusqueda.add(btnLimpiar);
        add(panelBusqueda, BorderLayout.NORTH);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnNuevo = new JButton("Nuevo");
        JButton btnEditar = new JButton("Editar");
        JButton btnActualizar = new JButton("Actualizar lista");
        JButton btnCerrar = new JButton("Cerrar");
        panelAcciones.add(btnNuevo);
        panelAcciones.add(btnEditar);
        panelAcciones.add(btnActualizar);
        panelAcciones.add(btnCerrar);
        add(panelAcciones, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> cargarTabla(txtBuscar.getText()));
        btnLimpiar.addActionListener(e -> {
            txtBuscar.setText("");
            cargarTabla(null);
        });
        btnActualizar.addActionListener(e -> cargarTabla(null));
        btnNuevo.addActionListener(e -> abrirDialogo(null));
        btnEditar.addActionListener(e -> editarSeleccionada());
        btnCerrar.addActionListener(e -> dispose());

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarSeleccionada();
                }
            }
        });
    }

    private void cargarTabla(String filtroNombre) {
        modeloTabla.setRowCount(0);
        for (Categoria c : categoriaService.buscarPorNombreParcial(filtroNombre)) {
            modeloTabla.addRow(new Object[]{c.getIdCategoria(), c.getNombre()});
        }
    }

    private void editarSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una categoría de la tabla", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) modeloTabla.getValueAt(fila, 0);
        Categoria categoria = categoriaService.buscarPorId(id);
        abrirDialogo(categoria);
    }

    private void abrirDialogo(Categoria categoriaExistente) {
        boolean esNuevo = (categoriaExistente == null);
        JTextField txtNombre = new JTextField(esNuevo ? "" : categoriaExistente.getNombre(), 20);

        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);

        int opcion = JOptionPane.showConfirmDialog(this, panel,
                esNuevo ? "Nueva categoría" : "Editar categoría",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        RespuestaOperacion<Void> resultado;
        if (esNuevo) {
            Categoria nueva = new Categoria(txtNombre.getText());
            resultado = categoriaService.registrar(nueva);
            JOptionPane.showMessageDialog(this, "Categoria agregada");
        } else {
            categoriaExistente.setNombre(txtNombre.getText());
            resultado = categoriaService.actualizar(categoriaExistente);
        }

        if (!resultado.isExito()) {
            JOptionPane.showMessageDialog(this, resultado.getMensaje(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        cargarTabla(null);
    }
    
   
}
