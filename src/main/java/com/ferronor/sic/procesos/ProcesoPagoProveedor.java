
package com.ferronor.sic.procesos;

import com.ferronor.sic.compras.logica.CompraService;
import com.ferronor.sic.compras.modelo.PagoProveedor;
import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.contabilidad.logica.ContabilidadService;
import com.ferronor.sic.contabilidad.modelo.dto.DatosPagoParaAsiento;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.tesoreria.logica.TesoreriaService;
import com.ferronor.sic.tesoreria.modelo.MovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.MovimientoCaja;
import com.ferronor.sic.tesoreria.modelo.OrigenMovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.OrigenMovimientoCaja;
import com.ferronor.sic.tesoreria.modelo.TipoMovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.TipoMovimientoCaja;

// Orquestador del pago de una cuenta por pagar: único componente que conoce
// a la vez Compras (dueño de cuenta_pagar), Tesorería y Contabilidad.
// Espejo de ProcesoCobroCliente — mismo criterio, direcciones opuestas.
public class ProcesoPagoProveedor {

    private final CompraService compraService;
    private final TesoreriaService tesoreriaService;
    private final ContabilidadService contabilidadService;

    public ProcesoPagoProveedor(CompraService compraService, TesoreriaService tesoreriaService,
            ContabilidadService contabilidadService) {
        this.compraService = compraService;
        this.tesoreriaService = tesoreriaService;
        this.contabilidadService = contabilidadService;
    }

    /**
     * Pago realizado en efectivo (caja).
     */
    public RespuestaOperacion<Void> registrarPagoCaja(PagoProveedor pago, int idCaja, int idUsuario) {
        return ejecutar(pago, "101", idUsuario, () -> {
            MovimientoCaja movimiento = new MovimientoCaja(idCaja, TipoMovimientoCaja.EGRESO,
                    OrigenMovimientoCaja.PAGO_PROVEEDOR, pago.getIdCompra(), pago.getMonto(),
                    "Pago compra N° " + pago.getIdCompra(), idUsuario);
            return tesoreriaService.registrarMovimientoCaja(movimiento);
        });
    }

    /**
     * Pago realizado por transferencia bancaria.
     */
    public RespuestaOperacion<Void> registrarPagoBanco(PagoProveedor pago, int idCuentaBancaria, int idUsuario) {
        return ejecutar(pago, "104", idUsuario, () -> {
            MovimientoBanco movimiento = new MovimientoBanco(idCuentaBancaria, TipoMovimientoBanco.TRANSFERENCIA,
                    OrigenMovimientoBanco.PAGO_PROVEEDOR, pago.getIdCompra(), pago.getMonto(),
                    null, idUsuario);
            return tesoreriaService.registrarMovimientoBanco(movimiento);
        });
    }

    private RespuestaOperacion<Void> ejecutar(PagoProveedor pago, String codigoCuentaEfectivo,
            int idUsuario, AccionTesoreria accionTesoreria) {

        try (TransactionContext tx = TransactionManager.iniciar()) {
            RespuestaOperacion<Void> resultadoPago = compraService.aplicarPago(pago);
            if (!resultadoPago.isExito()) {
                tx.rollback();
                return resultadoPago;
            }

            RespuestaOperacion<Void> resultadoTesoreria = accionTesoreria.ejecutar();
            if (!resultadoTesoreria.isExito()) {
                tx.rollback();
                return resultadoTesoreria;
            }

            DatosPagoParaAsiento datosAsiento = new DatosPagoParaAsiento(pago.getIdCompra(),
                    pago.getMonto(), codigoCuentaEfectivo);
            RespuestaOperacion<Integer> resultadoAsiento = contabilidadService.generarAsientoPago(
                    datosAsiento, idUsuario);
            if (!resultadoAsiento.isExito()) {
                tx.rollback();
                return RespuestaOperacion.error(resultadoAsiento.getMensaje());
            }

            tx.commit();
            return RespuestaOperacion.ok();
        }
    }

    @FunctionalInterface
    private interface AccionTesoreria {

        RespuestaOperacion<Void> ejecutar();
    }
}
