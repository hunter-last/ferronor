package com.ferronor.sic.ventas.dao;

import com.ferronor.sic.shared.IHistoricoDAO;
import com.ferronor.sic.ventas.modelo.EstadoVenta;
import com.ferronor.sic.ventas.modelo.Venta;

// Una venta registrada no se edita en sus datos (solo-inserta, como compra):
// correcciones se manejan con devolucion_venta. Sí necesita, a diferencia de Compra,
// una transición de estado acotada (PAGO_PENDIENTE -> DESPACHADA al saldar la
// cuenta por cobrar), análoga a OrdenCompraDAO.cambiarEstado.
public interface VentaDAO extends IHistoricoDAO<Venta, Integer> {

    void cambiarEstado(int idVenta, EstadoVenta estado);
}