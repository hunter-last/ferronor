package com.ferronor.sic.tesoreria.dao;

import com.ferronor.sic.shared.IGeneralDAO;
import com.ferronor.sic.tesoreria.modelo.CuentaBancaria;
import java.math.BigDecimal;
import java.util.List;

public interface CuentaBancariaDAO extends IGeneralDAO<CuentaBancaria, Integer> {

    List<CuentaBancaria> listarActivas();

    void actualizarSaldo(int idCuentaBancaria, BigDecimal nuevoSaldo);
}