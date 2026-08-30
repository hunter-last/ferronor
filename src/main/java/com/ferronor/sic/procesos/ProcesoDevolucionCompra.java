package com.ferronor.sic.procesos;

import com.ferronor.sic.compras.logica.DevolucionCompraService;
import com.ferronor.sic.compras.modelo.DevolucionCompra;
import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.inventario.logica.InventarioService;
import com.ferronor.sic.inventario.modelo.OrigenMovimiento;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.math.BigDecimal;

// Orquestador de la devolución de compra: coordina DevolucionCompraService + InventarioService.
// Alcance deliberadamente limitado a stock — devolucion_compra no tiene columna de monto/valor
// en el DDL, así que no hay dato para tocar Tesorería ni Contabilidad. Si el proyecto llega a
// necesitar reversar dinero o generar asiento contable por una devolución, eso requiere primero
// un cambio de esquema (agregar monto), no es algo que este proceso deba asumir en silencio.
public class ProcesoDevolucionCompra {

    private final DevolucionCompraService devolucionCompraService;
    private final InventarioService inventarioService;

    public ProcesoDevolucionCompra(DevolucionCompraService devolucionCompraService,
            InventarioService inventarioService) {
        this.devolucionCompraService = devolucionCompraService;
        this.inventarioService = inventarioService;
    }

    // Una devolución de compra significa que el producto SALE del stock
    // (se lo regresamos al proveedor). El costo lo calcula registrarSalida()
    // internamente desde el costo promedio actual — no hace falta pasarlo.
    public RespuestaOperacion<Void> registrarDevolucion(DevolucionCompra devolucion) {
        try (TransactionContext tx = TransactionManager.iniciar()) {
            RespuestaOperacion<Void> resultadoDevolucion = devolucionCompraService.registrarDevolucion(devolucion);
            if (!resultadoDevolucion.isExito()) {
                tx.rollback();
                return resultadoDevolucion;
            }

            RespuestaOperacion<BigDecimal> resultadoStock = inventarioService.registrarSalida(
                    devolucion.getIdProducto(), devolucion.getCantidad(), OrigenMovimiento.DEVOLUCION_COMPRA,
                    devolucion.getIdCompra(), devolucion.getIdUsuario());
            if (!resultadoStock.isExito()) {
                tx.rollback();
                return RespuestaOperacion.error(resultadoStock.getMensaje());
            }

            tx.commit();
            return RespuestaOperacion.ok();
        }
    }
}
