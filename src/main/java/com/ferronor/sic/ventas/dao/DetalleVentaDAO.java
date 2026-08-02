package com.ferronor.sic.ventas.dao;

import com.ferronor.sic.shared.IHistoricoDAO;
import com.ferronor.sic.ventas.modelo.DetalleVenta;
import java.util.List;

public interface DetalleVentaDAO extends IHistoricoDAO<DetalleVenta, Integer> {

    List<DetalleVenta> listarPorVenta(int idVenta);
}