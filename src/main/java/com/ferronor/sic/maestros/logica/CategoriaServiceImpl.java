package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.dao.CategoriaDAO;
import com.ferronor.sic.maestros.modelo.Categoria;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaDAO categoriaDAO;

    public CategoriaServiceImpl(CategoriaDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrar(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            return RespuestaOperacion.error("El nombre de la categoría es obligatorio");
        }
        categoria.setNombre(categoria.getNombre().trim());
        if (categoriaDAO.buscarPorNombre(categoria.getNombre()) != null) {
            return RespuestaOperacion.error("Ya existe una categoría con ese nombre");
        }
        categoriaDAO.insertar(categoria);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> actualizar(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            return RespuestaOperacion.error("El nombre de la categoría es obligatorio");
        }
        categoria.setNombre(categoria.getNombre().trim());

        if (categoriaDAO.buscarPorId(categoria.getIdCategoria()) == null) {
            return RespuestaOperacion.error("La categoría no existe");
        }
        Categoria conMismoNombre = categoriaDAO.buscarPorNombre(categoria.getNombre());
        if (conMismoNombre != null && conMismoNombre.getIdCategoria() != categoria.getIdCategoria()) {
            return RespuestaOperacion.error("Ya existe una categoría con ese nombre");
        }
        categoriaDAO.actualizar(categoria);
        return RespuestaOperacion.ok();
    }

    @Override
    public List<Categoria> listar() {
        return categoriaDAO.listar();
    }

    @Override
    public Categoria buscarPorId(int idCategoria) {
        return categoriaDAO.buscarPorId(idCategoria);
    }

    @Override
    public Categoria buscarPorNombre(String nombre) {
        return categoriaDAO.buscarPorNombre(nombre);
    }

    // CategoriaServiceImpl — agregar
    @Override
    public List<Categoria> buscarPorNombreParcial(String textoParcial) {
        if (textoParcial == null || textoParcial.isBlank()) {
            return categoriaDAO.listar();
        }
        String texto = textoParcial.trim().toLowerCase();
        return categoriaDAO.listar().stream()
                .filter(c -> c.getNombre().toLowerCase().contains(texto))
                .toList();
    }
}
