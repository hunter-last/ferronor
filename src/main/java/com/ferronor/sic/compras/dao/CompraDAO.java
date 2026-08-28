package com.ferronor.sic.compras.dao;

import com.ferronor.sic.compras.modelo.Compra;
import com.ferronor.sic.shared.IHistoricoDAO;

// Una compra registrada no se edita (solo-inserta, como movimiento_inventario):
// correcciones se manejan con devolucion_compra, no con UPDATE sobre compra.
public interface CompraDAO extends IHistoricoDAO<Compra, Integer> {

    Compra buscarPorNumeroFactura(int idProveedor, String numeroFactura);
    
    Compra buscarPorOrdenCompra(int idOrdenCompra);
}