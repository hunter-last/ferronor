package com.ferronor.sic.tesoreria.logica;

import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.tesoreria.dao.CajaDAO;
import com.ferronor.sic.tesoreria.dao.MovimientoCajaDAO;
import com.ferronor.sic.tesoreria.dao.CierreCajaDAO;
import com.ferronor.sic.tesoreria.modelo.Caja;
import com.ferronor.sic.tesoreria.modelo.CierreCaja;
import com.ferronor.sic.tesoreria.modelo.EstadoCaja;
import com.ferronor.sic.tesoreria.modelo.MovimientoCaja;
import com.ferronor.sic.tesoreria.modelo.TipoMovimientoCaja;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CajaServiceImpl implements CajaService {

    private final CajaDAO cajaDAO;
    private final MovimientoCajaDAO movimientoCajaDAO;
    private final CierreCajaDAO cierreCajaDAO;

    public CajaServiceImpl(CajaDAO cajaDAO, MovimientoCajaDAO movimientoCajaDAO, CierreCajaDAO cierreCajaDAO) {
        this.cajaDAO = cajaDAO;
        this.movimientoCajaDAO = movimientoCajaDAO;
        this.cierreCajaDAO = cierreCajaDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrarMovimiento(MovimientoCaja movimiento) {
        if (movimiento == null || movimiento.getMonto() == null || movimiento.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            return RespuestaOperacion.error("El monto del movimiento debe ser mayor a cero");
        }
        Caja caja = cajaDAO.buscarPorId(movimiento.getIdCaja());
        if (caja == null) {
            return RespuestaOperacion.error("La caja no existe");
        }
        if (caja.getEstado() != EstadoCaja.ABIERTA) {
            return RespuestaOperacion.error("La caja no está abierta");
        }

        BigDecimal nuevoSaldo = movimiento.getTipo() == TipoMovimientoCaja.INGRESO
                ? caja.getSaldoActual().add(movimiento.getMonto())
                : caja.getSaldoActual().subtract(movimiento.getMonto());

        if (movimiento.getTipo() == TipoMovimientoCaja.EGRESO && nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            return RespuestaOperacion.error("Saldo insuficiente en caja para registrar el egreso");
        }

        try (TransactionContext tx = TransactionManager.iniciar()) {
            movimientoCajaDAO.insertar(movimiento);
            cajaDAO.actualizarSaldo(caja.getIdCaja(), nuevoSaldo);
            tx.commit();
            return RespuestaOperacion.ok();
        }
    }

    @Override
    public RespuestaOperacion<Void> abrir(int idCaja, int idUsuario) {
        Caja caja = cajaDAO.buscarPorId(idCaja);
        if (caja == null) {
            return RespuestaOperacion.error("La caja no existe");
        }
        if (caja.getEstado() == EstadoCaja.ABIERTA) {
            return RespuestaOperacion.error("La caja ya se encuentra abierta");
        }
        cajaDAO.abrir(idCaja, idUsuario);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<CierreCaja> cerrar(int idCaja, BigDecimal saldoFinalReal, int idUsuario) {
        if (saldoFinalReal == null) {
            return RespuestaOperacion.error("El saldo final real es obligatorio");
        }
        Caja caja = cajaDAO.buscarPorId(idCaja);
        if (caja == null) {
            return RespuestaOperacion.error("La caja no existe");
        }
        if (caja.getEstado() != EstadoCaja.ABIERTA) {
            return RespuestaOperacion.error("La caja no está abierta");
        }

        // saldo_inicial no es una columna de `caja`: se reconstruye a partir del saldo
        // actual (= saldo final del sistema) descontando los movimientos del turno,
        // es decir, los registrados desde `fecha_apertura`.
        BigDecimal saldoFinalSistema = caja.getSaldoActual();
        List<MovimientoCaja> movimientosDelTurno = movimientoCajaDAO.listarPorCajaDesde(idCaja, caja.getFechaApertura());

        BigDecimal totalIngresos = movimientosDelTurno.stream()
                .filter(m -> m.getTipo() == TipoMovimientoCaja.INGRESO)
                .map(MovimientoCaja::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalEgresos = movimientosDelTurno.stream()
                .filter(m -> m.getTipo() == TipoMovimientoCaja.EGRESO)
                .map(MovimientoCaja::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldoInicial = saldoFinalSistema.subtract(totalIngresos).add(totalEgresos);

        CierreCaja cierre = new CierreCaja(idCaja, saldoInicial, saldoFinalSistema, saldoFinalReal, idUsuario);

        try (TransactionContext tx = TransactionManager.iniciar()) {
            cierreCajaDAO.insertar(cierre);
            cajaDAO.cerrar(idCaja);
            tx.commit();
            return RespuestaOperacion.ok(cierre);
        }
    }

    @Override
    public Optional<Caja> obtenerAbierta() {
        return cajaDAO.buscarAbierta();
    }

    @Override
    public Caja buscarPorId(int idCaja) {
        return cajaDAO.buscarPorId(idCaja);
    }

    @Override
    public List<MovimientoCaja> listarMovimientos() {
        return movimientoCajaDAO.listar();
    }

    @Override
    public List<MovimientoCaja> listarMovimientosPorCaja(
            int idCaja) {

        return movimientoCajaDAO.listarPorCaja(idCaja);
    }
}
