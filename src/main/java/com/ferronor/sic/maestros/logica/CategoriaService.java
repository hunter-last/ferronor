package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.modelo.Categoria;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public interface CategoriaService {

    RespuestaOperacion<Void> registrar(Categoria categoria);

    RespuestaOperacion<Void> actualizar(Categoria categoria);

    Categoria buscarPorId(int idCategoria);

    Categoria buscarPorNombre(String nombre);

    List<Categoria> listar();

    List<Categoria> buscarPorNombreParcial(String textoParcial);
}
