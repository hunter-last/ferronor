package com.ferronor.sic.tesoreria.logica;

import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.tesoreria.modelo.Caja;
import com.ferronor.sic.tesoreria.modelo.CierreCaja;
import com.ferronor.sic.tesoreria.modelo.CuentaBancaria;
import com.ferronor.sic.tesoreria.modelo.MovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.MovimientoCaja;
import com.ferronor.sic.tesoreria.modelo.OrigenMovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.OrigenMovimientoCaja;
import com.ferronor.sic.tesoreria.modelo.TipoMovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.TipoMovimientoCaja;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class TesoreriaServiceImpl implements TesoreriaService {

    private final CajaService cajaService;
    private final BancoService bancoService;

    public TesoreriaServiceImpl(CajaService cajaService, BancoService bancoService) {
        this.cajaService = cajaService;
        this.bancoService = bancoService;
    }

    @Override
    public RespuestaOperacion<Void> registrarMovimientoCaja(MovimientoCaja movimiento) {
        return cajaService.registrarMovimiento(movimiento);
    }

    @Override
    public RespuestaOperacion<Void> registrarMovimientoBanco(MovimientoBanco movimiento) {
        return bancoService.registrarMovimiento(movimiento);
    }

    @Override
    public RespuestaOperacion<Void> depositarCajaEnBanco(int idCaja, int idCuentaBancaria, BigDecimal monto,
            int idUsuario) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return RespuestaOperacion.error("El monto del depósito debe ser mayor a cero");
        }

        String descripcion = "Depósito de caja " + idCaja + " a cuenta bancaria " + idCuentaBancaria;
        MovimientoCaja movimientoCaja = new MovimientoCaja(idCaja, TipoMovimientoCaja.EGRESO,
                OrigenMovimientoCaja.DEPOSITO_CAJA, null, monto, descripcion, idUsuario);
        MovimientoBanco movimientoBanco = new MovimientoBanco(idCuentaBancaria, TipoMovimientoBanco.DEPOSITO,
                OrigenMovimientoBanco.DEPOSITO_CAJA, null, monto, null, idUsuario);

        // TesoreriaServiceImpl es dueño de la transacción global de esta operación
        // (coordina dos tablas de responsabilidades internas distintas). Reutiliza
        // la validación + inserción + actualización de saldo que ya implementan
        // CajaService/BancoService.registrarMovimiento en vez de duplicarla aquí:
        // sus propios TransactionManager.iniciar() se unen a esta transacción
        // (TransactionContext(false)) sin comprometerla por su cuenta.
        try (TransactionContext tx = TransactionManager.iniciar()) {
            RespuestaOperacion<Void> resultadoCaja = cajaService.registrarMovimiento(movimientoCaja);
            if (!resultadoCaja.isExito()) {
                return RespuestaOperacion.error(resultadoCaja.getMensaje());
            }

            RespuestaOperacion<Void> resultadoBanco = bancoService.registrarMovimiento(movimientoBanco);
            if (!resultadoBanco.isExito()) {
                return RespuestaOperacion.error(resultadoBanco.getMensaje());
            }

            tx.commit();
            return RespuestaOperacion.ok();
        }
    }

    @Override
    public RespuestaOperacion<Void> abrirCaja(int idCaja, int idUsuario) {
        return cajaService.abrir(idCaja, idUsuario);
    }

    @Override
    public RespuestaOperacion<Void> cerrarCaja(int idCaja, BigDecimal saldoFinalReal, int idUsuario) {
        RespuestaOperacion<CierreCaja> resultado = cajaService.cerrar(idCaja, saldoFinalReal, idUsuario);
        if (!resultado.isExito()) {
            return RespuestaOperacion.error(resultado.getMensaje());
        }
        return RespuestaOperacion.ok();
    }

    @Override
    public Optional<Caja> obtenerCajaAbierta() {
        return cajaService.obtenerAbierta();
    }

    @Override
    public List<CuentaBancaria> listarCuentasBancariasActivas() {
        return bancoService.listarActivas();
    }
}