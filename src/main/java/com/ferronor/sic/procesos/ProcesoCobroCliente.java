
package com.ferronor.sic.procesos;

import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.contabilidad.logica.ContabilidadService;
import com.ferronor.sic.contabilidad.modelo.dto.DatosCobroParaAsiento;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.tesoreria.logica.TesoreriaService;
import com.ferronor.sic.tesoreria.modelo.MovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.MovimientoCaja;
import com.ferronor.sic.tesoreria.modelo.OrigenMovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.OrigenMovimientoCaja;
import com.ferronor.sic.tesoreria.modelo.TipoMovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.TipoMovimientoCaja;
import com.ferronor.sic.ventas.logica.VentaService;
import com.ferronor.sic.ventas.modelo.CobroCliente;

// Orquestador del cobro de una cuenta por cobrar: único componente que conoce
// a la vez Ventas (dueño de cuenta_cobrar), Tesorería y Contabilidad.
// No participa Inventario aquí — el stock ya salió cuando se registró la venta.
public class ProcesoCobroCliente {

    private final VentaService ventaService;
    private final TesoreriaService tesoreriaService;
    private final ContabilidadService contabilidadService;

    public ProcesoCobroCliente(VentaService ventaService, TesoreriaService tesoreriaService,
            ContabilidadService contabilidadService) {
        this.ventaService = ventaService;
        this.tesoreriaService = tesoreriaService;
        this.contabilidadService = contabilidadService;
    }

    /**
     * Cobro recibido en efectivo (caja).
     */
    public RespuestaOperacion<Void> registrarCobroCaja(CobroCliente cobro, int idCaja, int idUsuario) {
        return ejecutar(cobro, "101", idUsuario, () -> {
            MovimientoCaja movimiento = new MovimientoCaja(idCaja, TipoMovimientoCaja.INGRESO,
                    OrigenMovimientoCaja.COBRO_CLIENTE, cobro.getIdVenta(), cobro.getMonto(),
                    "Cobro venta N° " + cobro.getIdVenta(), idUsuario);
            return tesoreriaService.registrarMovimientoCaja(movimiento);
        });
    }

    /**
     * Cobro recibido por transferencia/depósito bancario.
     */
    public RespuestaOperacion<Void> registrarCobroBanco(CobroCliente cobro, int idCuentaBancaria, int idUsuario) {
        return ejecutar(cobro, "104", idUsuario, () -> {
            MovimientoBanco movimiento = new MovimientoBanco(idCuentaBancaria, TipoMovimientoBanco.DEPOSITO,
                    OrigenMovimientoBanco.COBRO_CLIENTE, cobro.getIdVenta(), cobro.getMonto(),
                    null, idUsuario);
            return tesoreriaService.registrarMovimientoBanco(movimiento);
        });
    }

    private RespuestaOperacion<Void> ejecutar(CobroCliente cobro, String codigoCuentaEfectivo,
            int idUsuario, AccionTesoreria accionTesoreria) {

        try (TransactionContext tx = TransactionManager.iniciar()) {
            RespuestaOperacion<Void> resultadoCobro = ventaService.aplicarCobro(cobro);
            if (!resultadoCobro.isExito()) {
                tx.rollback();
                return resultadoCobro;
            }

            RespuestaOperacion<Void> resultadoTesoreria = accionTesoreria.ejecutar();
            if (!resultadoTesoreria.isExito()) {
                tx.rollback();
                return resultadoTesoreria;
            }

            DatosCobroParaAsiento datosAsiento = new DatosCobroParaAsiento(cobro.getIdVenta(),
                    cobro.getMonto(), codigoCuentaEfectivo);
            RespuestaOperacion<Integer> resultadoAsiento = contabilidadService.generarAsientoCobro(
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
