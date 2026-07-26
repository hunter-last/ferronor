
package com.ferronor.sic.shared;

import java.util.List;

public interface IHistoricoDAO<T, ID> {
    void insertar(T entidad);
    T buscarPorId(ID id);
    List<T> listar();
}