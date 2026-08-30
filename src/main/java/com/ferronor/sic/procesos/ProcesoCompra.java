
package com.ferronor.sic.procesos;

import com.ferronor.sic.compras.logica.CompraService;
import com.ferronor.sic.compras.modelo.Compra;
import com.ferronor.sic.compras.modelo.DetalleCompra;
import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.contabilidad.logica.ContabilidadService;
import com.ferronor.sic.contabilidad.modelo.dto.DatosCompraParaAsiento;
import com.ferronor.sic.inventario.logica.InventarioService;
import com.ferronor.sic.inventario.modelo.OrigenMovimiento;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.tesoreria.logica.TesoreriaService;
import com.ferronor.sic.tesoreria.modelo.MovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.MovimientoCaja;
import com.ferronor.sic.tesoreria.modelo.OrigenMovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.OrigenMovimientoCaja;
import com.ferronor.sic.tesoreria.modelo.TipoMovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.TipoMovimientoCaja;

// Orquestador de la compra: mismo criterio que ProcesoVenta — es el único componente
// que conoce a la vez Compras, Inventario, Tesorería y Contabilidad.
public class ProcesoCompra {

    private final CompraService compraService;
    private final InventarioService inventarioService;
    private final TesoreriaService tesoreriaService;
    private final ContabilidadService contabilidadService;

    public ProcesoCompra(CompraService compraService, InventarioService inventarioService,
            TesoreriaService tesoreriaService, ContabilidadService contabilidadService) {
        this.compraService = compraService;
        this.inventarioService = inventarioService;
        this.tesoreriaService = tesoreriaService;
        this.contabilidadService = contabilidadService;
    }

    /**
     * Compra al contado, pagada en efectivo (caja).
     */
    public RespuestaOperacion<Integer> registrarCompraContadoCaja(Compra compra, int idCaja) {
        return ejecutar(compra, "101", idCompra -> {
            MovimientoCaja movimiento = new MovimientoCaja(idCaja, TipoMovimientoCaja.EGRESO,
                    OrigenMovimientoCaja.COMPRA_CONTADO, idCompra, compra.getTotal(),
                    "Compra N° " + idCompra, compra.getIdUsuario());
            return tesoreriaService.registrarMovimientoCaja(movimiento);
        });
    }

    /**
     * Compra al contado, pagada por transferencia bancaria.
     */
    public RespuestaOperacion<Integer> registrarCompraContadoBanco(Compra compra, int idCuentaBancaria) {
        return ejecutar(compra, "104", idCompra -> {
            MovimientoBanco movimiento = new MovimientoBanco(idCuentaBancaria, TipoMovimientoBanco.TRANSFERENCIA,
                    OrigenMovimientoBanco.COMPRA_CONTADO, idCompra, compra.getTotal(),
                    null, compra.getIdUsuario());
            return tesoreriaService.registrarMovimientoBanco(movimiento);
        });
    }

    /**
     * Compra al crédito: no hay movimiento de Tesorería en este momento, solo
     * cuenta_pagar.
     */
    public RespuestaOperacion<Integer> registrarCompraCredito(Compra compra) {
        return ejecutar(compra, "42", idCompra -> RespuestaOperacion.ok());
    }

    private RespuestaOperacion<Integer> ejecutar(Compra compra, String codigoCuentaContrapartida,
            AccionTesoreria accionTesoreria) {

        try (TransactionContext tx = TransactionManager.iniciar()) {
            RespuestaOperacion<Integer> resultadoCompra = compraService.registrarCompra(compra);
            if (!resultadoCompra.isExito()) {
                tx.rollback();
                return resultadoCompra;
            }
            int idCompra = resultadoCompra.getResultado();

            for (DetalleCompra detalle : compra.getDetalles()) {
                RespuestaOperacion<Void> resultadoStock = inventarioService.registrarEntrada(
                        detalle.getIdProducto(), detalle.getCantidad(), detalle.getCostoUnitario(),
                        OrigenMovimiento.COMPRA, idCompra, compra.getIdUsuario());
                if (!resultadoStock.isExito()) {
                    tx.rollback();
                    return RespuestaOperacion.error(resultadoStock.getMensaje());
                }
            }

            RespuestaOperacion<Void> resultadoTesoreria = accionTesoreria.ejecutar(idCompra);
            if (!resultadoTesoreria.isExito()) {
                tx.rollback();
                return RespuestaOperacion.error(resultadoTesoreria.getMensaje());
            }

            DatosCompraParaAsiento datosAsiento = new DatosCompraParaAsiento(idCompra, compra.getSubtotal(),
                    compra.getIgv(), compra.getTotal(), codigoCuentaContrapartida);
            RespuestaOperacion<Integer> resultadoAsiento = contabilidadService.generarAsientoCompra(
                    datosAsiento, compra.getIdUsuario());
            if (!resultadoAsiento.isExito()) {
                tx.rollback();
                return RespuestaOperacion.error(resultadoAsiento.getMensaje());
            }

            tx.commit();
            return RespuestaOperacion.ok(idCompra);
        }
    }

    @FunctionalInterface
    private interface AccionTesoreria {

        RespuestaOperacion<Void> ejecutar(int idCompra);
    }
}
