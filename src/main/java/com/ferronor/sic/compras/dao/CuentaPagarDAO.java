package com.ferronor.sic.compras.dao;

import com.ferronor.sic.compras.modelo.CuentaPagar;
import com.ferronor.sic.compras.modelo.EstadoCuenta;
import com.ferronor.sic.compras.modelo.dto.CuentaPagarConsulta;
import com.ferronor.sic.shared.IGeneralDAO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CuentaPagarDAO extends IGeneralDAO<CuentaPagar, Integer> {

    CuentaPagar buscarPorCompra(int idCompra);

    List<CuentaPagar> listarPorEstado(EstadoCuenta estado);

    // Actualiza montoPagado/saldoPendiente/estado tras aplicar un pago
    void registrarPago(int idCuentaPagar, BigDecimal nuevoMontoPagado,
            BigDecimal nuevoSaldoPendiente, EstadoCuenta nuevoEstado);

    // Consulta enriquecida (JOIN cuenta_pagar + compra + proveedor) con filtros opcionales.
    // null en cualquier parámetro significa "sin filtro". fechaDesde/fechaHasta filtran
    // por compra.fecha, no por cuenta_pagar.fecha_vencimiento.
    List<CuentaPagarConsulta> consultar(EstadoCuenta estado, Integer idProveedor,
            LocalDate fechaDesde, LocalDate fechaHasta);
}