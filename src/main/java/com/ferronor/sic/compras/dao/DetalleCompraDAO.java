package com.ferronor.sic.compras.dao;

import com.ferronor.sic.compras.modelo.DetalleCompra;
import com.ferronor.sic.shared.IHistoricoDAO;
import java.util.List;

public interface DetalleCompraDAO extends IHistoricoDAO<DetalleCompra, Integer> {

    List<DetalleCompra> listarPorCompra(int idCompra);
}