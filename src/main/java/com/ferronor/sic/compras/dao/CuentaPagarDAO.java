package com.ferronor.sic.compras.dao;

import com.ferronor.sic.compras.modelo.CuentaPagar;
import com.ferronor.sic.compras.modelo.EstadoCuenta;
import com.ferronor.sic.shared.IGeneralDAO;
import java.math.BigDecimal;
import java.util.List;

public interface CuentaPagarDAO extends IGeneralDAO<CuentaPagar, Integer> {

    CuentaPagar buscarPorCompra(int idCompra);

    List<CuentaPagar> listarPorEstado(EstadoCuenta estado);

    // Actualiza montoPagado/saldoPendiente/estado tras aplicar un pago
    void registrarPago(int idCuentaPagar, BigDecimal nuevoMontoPagado,
            BigDecimal nuevoSaldoPendiente, EstadoCuenta nuevoEstado);
}