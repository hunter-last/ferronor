package com.ferronor.sic.tesoreria.dao;

import com.ferronor.sic.shared.IHistoricoDAO;
import com.ferronor.sic.tesoreria.modelo.CierreCaja;
import java.util.List;

public interface CierreCajaDAO extends IHistoricoDAO<CierreCaja, Integer> {

    List<CierreCaja> listarPorCaja(int idCaja);
}