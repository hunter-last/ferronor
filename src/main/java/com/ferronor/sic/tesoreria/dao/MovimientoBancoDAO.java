package com.ferronor.sic.tesoreria.dao;

import com.ferronor.sic.shared.IHistoricoDAO;
import com.ferronor.sic.tesoreria.modelo.MovimientoBanco;
import java.util.List;

public interface MovimientoBancoDAO extends IHistoricoDAO<MovimientoBanco, Integer> {

    List<MovimientoBanco> listarPorCuenta(int idCuentaBancaria);
}