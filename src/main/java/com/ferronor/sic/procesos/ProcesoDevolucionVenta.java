
package com.ferronor.sic.procesos;

import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.inventario.logica.InventarioService;
import com.ferronor.sic.inventario.modelo.MovimientoInventario;
import com.ferronor.sic.inventario.modelo.OrigenMovimiento;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.ventas.logica.DevolucionVentaService;
import com.ferronor.sic.ventas.modelo.DevolucionVenta;

// Orquestador de la devolución de venta: coordina DevolucionVentaService + InventarioService.
// Mismo alcance limitado que ProcesoDevolucionCompra — sin Tesorería/Contabilidad,
// porque devolucion_venta tampoco tiene columna de monto en el DDL.
// Regla asumida: una venta genera un único movimiento de salida por producto
// (ProcesoVenta llama a registrarSalida una vez por línea de detalle_venta).
public class ProcesoDevolucionVenta {

    private final DevolucionVentaService devolucionVentaService;
    private final InventarioService inventarioService;

    public ProcesoDevolucionVenta(DevolucionVentaService devolucionVentaService,
            InventarioService inventarioService) {
        this.devolucionVentaService = devolucionVentaService;
        this.inventarioService = inventarioService;
    }

    public RespuestaOperacion<Void> registrarDevolucion(DevolucionVenta devolucion) {
        MovimientoInventario movimientoOriginal = inventarioService.buscarMovimientoOrigen(
                devolucion.getIdProducto(), OrigenMovimiento.VENTA, devolucion.getIdVenta());
        if (movimientoOriginal == null) {
            return RespuestaOperacion.error(
                    "No se encontró el movimiento de salida de la venta N° " + devolucion.getIdVenta()
                    + " para el producto " + devolucion.getIdProducto());
        }

        try (TransactionContext tx = TransactionManager.iniciar()) {
            RespuestaOperacion<Void> resultadoDevolucion = devolucionVentaService.registrarDevolucion(devolucion);
            if (!resultadoDevolucion.isExito()) {
                tx.rollback();
                return resultadoDevolucion;
            }

            RespuestaOperacion<Void> resultadoStock = inventarioService.registrarEntrada(
                    devolucion.getIdProducto(), devolucion.getCantidad(), movimientoOriginal.getCostoUnitario(),
                    OrigenMovimiento.DEVOLUCION_VENTA, devolucion.getIdVenta(), devolucion.getIdUsuario());
            if (!resultadoStock.isExito()) {
                tx.rollback();
                return resultadoStock;
            }

            tx.commit();
            return RespuestaOperacion.ok();
        }
    }
}


    // Busca, en el historial real de movimientos, el costo exacto que tenía el producto
    // en el momento en que salió por esa venta — no el costo promedio de hoy.
