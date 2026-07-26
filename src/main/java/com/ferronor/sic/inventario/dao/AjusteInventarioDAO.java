
package com.ferronor.sic.inventario.dao;

import com.ferronor.sic.inventario.modelo.AjusteInventario;
import com.ferronor.sic.shared.IHistoricoDAO;
import java.util.List;

public interface AjusteInventarioDAO extends IHistoricoDAO<AjusteInventario, Integer> {
    List<AjusteInventario> listarPorProducto(int idProducto);
    List<AjusteInventario> listarPorRangoFecha(java.time.LocalDate desde, java.time.LocalDate hasta);
}