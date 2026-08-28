package com.ferronor.sic.tesoreria.logica;

import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.tesoreria.modelo.Caja;
import com.ferronor.sic.tesoreria.modelo.CierreCaja;
import com.ferronor.sic.tesoreria.modelo.MovimientoCaja;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// Interno del módulo: encapsula caja + movimiento_caja + cierre_caja.
// No se registra en ServiceFactory; solo lo consume TesoreriaServiceImpl (fachada).
public interface CajaService {

    RespuestaOperacion<Void> registrarMovimiento(MovimientoCaja movimiento);

    RespuestaOperacion<Void> abrir(int idCaja, int idUsuario);

    RespuestaOperacion<CierreCaja> cerrar(int idCaja, BigDecimal saldoFinalReal, int idUsuario);

    Optional<Caja> obtenerAbierta();

    Caja buscarPorId(int idCaja);

    // CajaService
    List<MovimientoCaja> listarMovimientos();

    List<MovimientoCaja> listarMovimientosPorCaja(
            int idCaja);
    
    List<Caja> listarCajas();
}
