package com.ferronor.sic.ventas.dao;

import com.ferronor.sic.shared.IGeneralDAO;
import com.ferronor.sic.ventas.modelo.CuentaCobrar;
import com.ferronor.sic.ventas.modelo.EstadoCuenta;
import java.math.BigDecimal;
import java.util.List;

public interface CuentaCobrarDAO extends IGeneralDAO<CuentaCobrar, Integer> {

    CuentaCobrar buscarPorVenta(int idVenta);

    List<CuentaCobrar> listarPorEstado(EstadoCuenta estado);

    // Actualiza montoCobrado/saldoPendiente/estado tras aplicar un cobro
    void registrarCobro(int idCuentaCobrar, BigDecimal nuevoMontoCobrado, BigDecimal nuevoSaldoPendiente,
            EstadoCuenta nuevoEstado);
}