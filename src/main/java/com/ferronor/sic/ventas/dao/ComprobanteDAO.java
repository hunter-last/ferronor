package com.ferronor.sic.ventas.dao;

import com.ferronor.sic.shared.IHistoricoDAO;
import com.ferronor.sic.ventas.modelo.Comprobante;

// Solo-inserta a efectos de este módulo: no se expone actualizar() genérico.
public interface ComprobanteDAO extends IHistoricoDAO<Comprobante, Integer> {

    Comprobante buscarPorVenta(int idVenta);
}