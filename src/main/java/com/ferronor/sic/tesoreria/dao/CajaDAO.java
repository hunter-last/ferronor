package com.ferronor.sic.tesoreria.dao;

import com.ferronor.sic.shared.IGeneralDAO;
import com.ferronor.sic.tesoreria.modelo.Caja;
import java.math.BigDecimal;
import java.util.Optional;

public interface CajaDAO extends IGeneralDAO<Caja, Integer> {

    Optional<Caja> buscarAbierta();

    void abrir(int idCaja, int idUsuario);

    void cerrar(int idCaja);

    void actualizarSaldo(int idCaja, BigDecimal nuevoSaldo);
}