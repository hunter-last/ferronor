package com.ferronor.sic.compras.dao;

import com.ferronor.sic.compras.modelo.DetalleOrdenCompra;
import com.ferronor.sic.shared.IHistoricoDAO;
import java.util.List;

public interface DetalleOrdenCompraDAO extends IHistoricoDAO<DetalleOrdenCompra, Integer> {

    List<DetalleOrdenCompra> listarPorOrdenCompra(int idOrdenCompra);
}