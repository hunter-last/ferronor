package com.ferronor.sic.compras.dao;

import com.ferronor.sic.compras.modelo.DevolucionCompra;
import com.ferronor.sic.shared.IHistoricoDAO;
import java.util.List;

public interface DevolucionCompraDAO extends IHistoricoDAO<DevolucionCompra, Integer> {

    List<DevolucionCompra> listarPorCompra(int idCompra);
}