package com.ferronor.sic.maestros.dao;

import com.ferronor.sic.maestros.modelo.Producto;
import com.ferronor.sic.shared.IGeneralDAO;
import java.util.List;

public interface ProductoDAO extends IGeneralDAO<Producto, Integer> {

    Producto buscarPorCodigo(String codigo);

    List<Producto> listarActivos();

    List<Producto> buscarActivosPorNombreOCodigoParcial(String texto);

    void desactivar(int idProducto);

    void activar(int idProducto);
}
