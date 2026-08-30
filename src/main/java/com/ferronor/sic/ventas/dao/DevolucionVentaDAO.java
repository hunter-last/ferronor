package com.ferronor.sic.ventas.dao;

import com.ferronor.sic.shared.IHistoricoDAO;
import com.ferronor.sic.ventas.modelo.DevolucionVenta;
import java.util.List;

public interface DevolucionVentaDAO extends IHistoricoDAO<DevolucionVenta, Integer> {

    List<DevolucionVenta> listarPorVenta(int idVenta);
}