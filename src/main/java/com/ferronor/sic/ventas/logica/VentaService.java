package com.ferronor.sic.ventas.logica;

import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.ventas.modelo.CobroCliente;
import com.ferronor.sic.ventas.modelo.Comprobante;
import com.ferronor.sic.ventas.modelo.CuentaCobrar;
import com.ferronor.sic.ventas.modelo.EstadoCuenta;
import com.ferronor.sic.ventas.modelo.Venta;
import com.ferronor.sic.ventas.modelo.dto.CuentaCobrarConsulta;
import java.time.LocalDate;
import java.util.List;

// Fachada pública de Ventas: único punto de entrada para procesos/ y otros
// componentes externos al módulo. No conoce InventarioService ni TesoreriaService.
public interface VentaService {

    // Registra venta + detalle_venta + comprobante (numeración atómica vía
    // correlativo_comprobante) + cuenta_cobrar si es crédito, todo en una transacción.
    // Decide el estado internamente (contado -> DESPACHADA directo; crédito ->
    // PAGO_PENDIENTE); no expone un setEstado público.
    RespuestaOperacion<Integer> registrarVenta(Venta venta, int idTipoComprobante);

    // Aplica el cobro contra cuenta_cobrar (actualiza montoCobrado/saldoPendiente/estado)
    // y transiciona PAGO_PENDIENTE -> DESPACHADA cuando el saldo llega a cero.
    // Solo debe invocarlo ProcesoCobroCliente, nunca la vista directo.
    RespuestaOperacion<Void> aplicarCobro(CobroCliente cobro);

    Venta buscarPorId(int idVenta);

    List<Venta> listar();

    // VentaService es dueño de cuenta_cobrar (1-1 con venta): mismo criterio que
    // CompraService con cuenta_pagar, no se crea un CuentaCobrarService aparte.
    CuentaCobrar buscarCuentaCobrarPorVenta(int idVenta);

    List<CuentaCobrar> listarCuentasPorCobrarPendientes();

    List<CuentaCobrar> listarCuentasPorCobrarVencidas();

    List<CuentaCobrarConsulta> consultarCuentasPorCobrar(EstadoCuenta estado, Integer idCliente,
            LocalDate fechaDesde, LocalDate fechaHasta);
    
    Comprobante buscarComprobantePorVenta(int idVenta);
}
