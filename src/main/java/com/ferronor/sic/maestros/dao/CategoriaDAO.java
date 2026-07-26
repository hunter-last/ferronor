
package com.ferronor.sic.maestros.dao;

import com.ferronor.sic.maestros.modelo.Categoria;
import com.ferronor.sic.shared.IGeneralDAO;

public interface CategoriaDAO extends IGeneralDAO<Categoria, Integer> {

    Categoria buscarPorNombre(String nombre);
}
