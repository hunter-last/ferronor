package com.ferronor.sic.compras.dao;

import com.ferronor.sic.compras.modelo.Compra;
import com.ferronor.sic.compras.modelo.dto.CompraConsulta;
import com.ferronor.sic.shared.IHistoricoDAO;
import java.time.LocalDate;
import java.util.List;

// Una compra registrada no se edita (solo-inserta, como movimiento_inventario):
// correcciones se manejan con devolucion_compra, no con UPDATE sobre compra.
public interface CompraDAO extends IHistoricoDAO<Compra, Integer> {

    Compra buscarPorNumeroFactura(int idProveedor, String numeroFactura);

    Compra buscarPorOrdenCompra(int idOrdenCompra);

    List<CompraConsulta> consultarHistorial(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Integer idProveedor,
            Integer idFormaPago,
            Boolean conOrdenCompra);
}
