package com.ferronor.sic.tesoreria.logica;

import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.tesoreria.modelo.CuentaBancaria;
import com.ferronor.sic.tesoreria.modelo.MovimientoBanco;
import java.util.List;

// Interno del módulo: encapsula cuenta_bancaria + movimiento_banco.
// No se registra en ServiceFactory; solo lo consume TesoreriaServiceImpl (fachada).
public interface BancoService {

    RespuestaOperacion<Void> registrarMovimiento(MovimientoBanco movimiento);

    List<CuentaBancaria> listarActivas();

    CuentaBancaria buscarPorId(int idCuentaBancaria);

    List<MovimientoBanco> listarMovimientos();

    List<MovimientoBanco> listarMovimientosPorCuenta(
            int idCuentaBancaria);
}
