package com.ferronor.sic.tesoreria.logica;

import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.tesoreria.modelo.Caja;
import com.ferronor.sic.tesoreria.modelo.CuentaBancaria;
import com.ferronor.sic.tesoreria.modelo.MovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.MovimientoCaja;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// Fachada pública de Tesorería: único punto de entrada para procesos/ y otros
// componentes externos al módulo. CajaService/BancoService permanecen internos.
public interface TesoreriaService {

    RespuestaOperacion<Void> registrarMovimientoCaja(MovimientoCaja movimiento);

    RespuestaOperacion<Void> registrarMovimientoBanco(MovimientoBanco movimiento);

    // Operación atómica propia de Tesorería: EGRESO en caja + DEPOSITO en banco,
    // ambos con origen=DEPOSITO_CAJA e idDocumentoOrigen=NULL (no hay documento
    // comercial de origen, es una transferencia interna).
    RespuestaOperacion<Void> depositarCajaEnBanco(int idCaja, int idCuentaBancaria, BigDecimal monto, int idUsuario);

    RespuestaOperacion<Void> abrirCaja(int idCaja, int idUsuario);

    RespuestaOperacion<Void> cerrarCaja(int idCaja, BigDecimal saldoFinalReal, int idUsuario);

    Optional<Caja> obtenerCajaAbierta();

    List<CuentaBancaria> listarCuentasBancariasActivas();

    List<MovimientoBanco> listarMovimientosBancarios();

    List<MovimientoBanco> listarMovimientosBancariosPorCuenta(
            int idCuentaBancaria);

    List<MovimientoCaja> listarMovimientosCaja();

    List<MovimientoCaja> listarMovimientosCajaPorCaja(
            int idCaja);

    List<Caja> listarCajas();
}
