package com.ferronor.sic.tesoreria.logica;

import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.tesoreria.dao.CuentaBancariaDAO;
import com.ferronor.sic.tesoreria.dao.MovimientoBancoDAO;
import com.ferronor.sic.tesoreria.modelo.CuentaBancaria;
import com.ferronor.sic.tesoreria.modelo.MovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.TipoMovimientoBanco;
import java.math.BigDecimal;
import java.util.List;

public class BancoServiceImpl implements BancoService {

    private final CuentaBancariaDAO cuentaBancariaDAO;
    private final MovimientoBancoDAO movimientoBancoDAO;

    public BancoServiceImpl(CuentaBancariaDAO cuentaBancariaDAO, MovimientoBancoDAO movimientoBancoDAO) {
        this.cuentaBancariaDAO = cuentaBancariaDAO;
        this.movimientoBancoDAO = movimientoBancoDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrarMovimiento(MovimientoBanco movimiento) {
        if (movimiento == null || movimiento.getMonto() == null || movimiento.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            return RespuestaOperacion.error("El monto del movimiento debe ser mayor a cero");
        }
        CuentaBancaria cuenta = cuentaBancariaDAO.buscarPorId(movimiento.getIdCuentaBancaria());
        if (cuenta == null) {
            return RespuestaOperacion.error("La cuenta bancaria no existe");
        }
        if (!cuenta.isActiva()) {
            return RespuestaOperacion.error("La cuenta bancaria no está activa");
        }

        BigDecimal nuevoSaldo = movimiento.getTipo() == TipoMovimientoBanco.DEPOSITO
                ? cuenta.getSaldoActual().add(movimiento.getMonto())
                : cuenta.getSaldoActual().subtract(movimiento.getMonto());

        if (movimiento.getTipo() != TipoMovimientoBanco.DEPOSITO && nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            return RespuestaOperacion.error("Saldo insuficiente en la cuenta bancaria");
        }

        try (TransactionContext tx = TransactionManager.iniciar()) {
            movimientoBancoDAO.insertar(movimiento);
            cuentaBancariaDAO.actualizarSaldo(cuenta.getIdCuentaBancaria(), nuevoSaldo);
            tx.commit();
            return RespuestaOperacion.ok();
        }
    }

    @Override
    public List<CuentaBancaria> listarActivas() {
        return cuentaBancariaDAO.listarActivas();
    }

    @Override
    public CuentaBancaria buscarPorId(int idCuentaBancaria) {
        return cuentaBancariaDAO.buscarPorId(idCuentaBancaria);
    }

    @Override
    public List<MovimientoBanco> listarMovimientos() {
        return movimientoBancoDAO.listar();
    }

    @Override
    public List<MovimientoBanco> listarMovimientosPorCuenta(
            int idCuentaBancaria) {

        return movimientoBancoDAO.listarPorCuenta(
                idCuentaBancaria
        );
    }
}
