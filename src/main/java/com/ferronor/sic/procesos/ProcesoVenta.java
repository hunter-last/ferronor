package com.ferronor.sic.procesos;

import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.contabilidad.logica.ContabilidadService;
import com.ferronor.sic.contabilidad.modelo.dto.DatosVentaParaAsiento;
import com.ferronor.sic.inventario.logica.InventarioService;
import com.ferronor.sic.inventario.modelo.OrigenMovimiento;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.tesoreria.logica.TesoreriaService;
import com.ferronor.sic.tesoreria.modelo.MovimientoCaja;
import com.ferronor.sic.tesoreria.modelo.MovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.OrigenMovimientoCaja;
import com.ferronor.sic.tesoreria.modelo.OrigenMovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.TipoMovimientoCaja;
import com.ferronor.sic.tesoreria.modelo.TipoMovimientoBanco;
import com.ferronor.sic.ventas.logica.VentaService;
import com.ferronor.sic.ventas.modelo.DetalleVenta;
import com.ferronor.sic.ventas.modelo.Venta;
import java.math.BigDecimal;

// Orquestador de la venta: es el único componente que conoce a la vez Ventas,
// Inventario, Tesorería y Contabilidad. Ninguno de esos 4 Services se llama
// entre sí — solo procesos/ los conoce a todos. Dueño de la transacción global.
public class ProcesoVenta {

    private final VentaService ventaService;
    private final InventarioService inventarioService;
    private final TesoreriaService tesoreriaService;
    private final ContabilidadService contabilidadService;

    public ProcesoVenta(VentaService ventaService, InventarioService inventarioService,
            TesoreriaService tesoreriaService, ContabilidadService contabilidadService) {
        this.ventaService = ventaService;
        this.inventarioService = inventarioService;
        this.tesoreriaService = tesoreriaService;
        this.contabilidadService = contabilidadService;
    }

    /**
     * Venta al contado, cobrada en efectivo (caja).
     */
    public RespuestaOperacion<Integer> registrarVentaContadoCaja(Venta venta, int idTipoComprobante, int idCaja) {
        return ejecutar(venta, idTipoComprobante, "101", idVenta -> {
            MovimientoCaja movimiento = new MovimientoCaja(idCaja, TipoMovimientoCaja.INGRESO,
                    OrigenMovimientoCaja.VENTA_CONTADO, idVenta, venta.getTotal(),
                    "Venta N° " + idVenta, venta.getIdUsuario());
            return tesoreriaService.registrarMovimientoCaja(movimiento);
        });
    }

    /**
     * Venta al contado, cobrada por transferencia/depósito bancario.
     */
    public RespuestaOperacion<Integer> registrarVentaContadoBanco(Venta venta, int idTipoComprobante,
            int idCuentaBancaria) {
        return ejecutar(venta, idTipoComprobante, "104", idVenta -> {
            MovimientoBanco movimiento = new MovimientoBanco(idCuentaBancaria, TipoMovimientoBanco.DEPOSITO,
                    OrigenMovimientoBanco.VENTA_CONTADO, idVenta, venta.getTotal(),
                    null, venta.getIdUsuario());
            return tesoreriaService.registrarMovimientoBanco(movimiento);
        });
    }

    /**
     * Venta al crédito: no hay movimiento de Tesorería en este momento, solo
     * cuenta_cobrar.
     */
    public RespuestaOperacion<Integer> registrarVentaCredito(Venta venta, int idTipoComprobante) {
        return ejecutar(venta, idTipoComprobante, "12", idVenta -> RespuestaOperacion.ok());
    }

    // Orquesta el registro de venta + salida de stock + (opcional) movimiento de tesorería
    // + asiento contable, todo en una sola transacción. accionTesoreria es un no-op (ok())
    // para el caso crédito.
    private RespuestaOperacion<Integer> ejecutar(Venta venta, int idTipoComprobante,
            String codigoCuentaContrapartida, AccionTesoreria accionTesoreria) {

        try (TransactionContext tx = TransactionManager.iniciar()) {
            RespuestaOperacion<Integer> resultadoVenta = ventaService.registrarVenta(venta, idTipoComprobante);
            if (!resultadoVenta.isExito()) {
                tx.rollback();
                return resultadoVenta;
            }
            int idVenta = resultadoVenta.getResultado();

            BigDecimal costoVentaTotal = BigDecimal.ZERO;

            for (DetalleVenta detalle : venta.getDetalles()) {
                RespuestaOperacion<BigDecimal> resultadoStock = inventarioService.registrarSalida(
                        detalle.getIdProducto(), detalle.getCantidad(), OrigenMovimiento.VENTA,
                        idVenta, venta.getIdUsuario());
                if (!resultadoStock.isExito()) {
                    tx.rollback();
                    return RespuestaOperacion.error(resultadoStock.getMensaje());
                }
                costoVentaTotal = costoVentaTotal.add(resultadoStock.getResultado());
            }

            RespuestaOperacion<Void> resultadoTesoreria = accionTesoreria.ejecutar(idVenta);
            if (!resultadoTesoreria.isExito()) {
                tx.rollback();
                return RespuestaOperacion.error(resultadoTesoreria.getMensaje());
            }

            DatosVentaParaAsiento datosAsiento = new DatosVentaParaAsiento(idVenta, venta.getSubtotal(),
                    venta.getIgv(), venta.getTotal(), costoVentaTotal, codigoCuentaContrapartida);
            RespuestaOperacion<Integer> resultadoAsiento = contabilidadService.generarAsientoVenta(
                    datosAsiento, venta.getIdUsuario());
            if (!resultadoAsiento.isExito()) {
                tx.rollback();
                return RespuestaOperacion.error(resultadoAsiento.getMensaje());
            }

            tx.commit();
            return RespuestaOperacion.ok(idVenta);
        }
    }

    @FunctionalInterface
    private interface AccionTesoreria {

        RespuestaOperacion<Void> ejecutar(int idVenta);
    }
}
