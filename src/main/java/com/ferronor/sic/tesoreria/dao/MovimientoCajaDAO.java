package com.ferronor.sic.tesoreria.dao;

import com.ferronor.sic.shared.IHistoricoDAO;
import com.ferronor.sic.tesoreria.modelo.MovimientoCaja;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoCajaDAO extends IHistoricoDAO<MovimientoCaja, Integer> {

    List<MovimientoCaja> listarPorCaja(int idCaja);

    // Usado por CajaService.cerrar() para reconstruir saldo_inicial sin traer
    // el historial completo de la caja.
    List<MovimientoCaja> listarPorCajaDesde(int idCaja, LocalDateTime desde);
}