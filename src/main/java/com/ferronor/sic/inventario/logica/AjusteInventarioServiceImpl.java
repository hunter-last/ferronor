package com.ferronor.sic.inventario.logica;

import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.inventario.dao.AjusteInventarioDAO;
import com.ferronor.sic.inventario.dao.MovimientoInventarioDAO;
import com.ferronor.sic.inventario.dao.StockDAO;
import com.ferronor.sic.inventario.modelo.AjusteInventario;
import com.ferronor.sic.inventario.modelo.MovimientoInventario;
import com.ferronor.sic.inventario.modelo.OrigenMovimiento;
import com.ferronor.sic.inventario.modelo.Stock;
import com.ferronor.sic.inventario.modelo.TipoMovimiento;
import com.ferronor.sic.maestros.dao.ProductoDAO;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.SesionUsuario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AjusteInventarioServiceImpl implements AjusteInventarioService {

    private final StockDAO stockDAO;
    private final MovimientoInventarioDAO movimientoDAO;
    private final AjusteInventarioDAO ajusteDAO;
    private final ProductoDAO productoDAO;

    // constructor de AjusteInventarioServiceImpl agrega ProductoDAO
    public AjusteInventarioServiceImpl(StockDAO stockDAO, MovimientoInventarioDAO movimientoDAO,
            AjusteInventarioDAO ajusteDAO, ProductoDAO productoDAO) {
        this.stockDAO = stockDAO;
        this.movimientoDAO = movimientoDAO;
        this.ajusteDAO = ajusteDAO;
        this.productoDAO = productoDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrarAjuste(int idProducto, BigDecimal cantidadFisica,
            String motivo, int idUsuario) {

        // AjusteInventarioServiceImpl.registrarAjuste() — al inicio del método
        if (!SesionUsuario.haySesion() || !SesionUsuario.actual().tienePermiso("AJUSTAR_STOCK")) {
            return RespuestaOperacion.error("No tiene permisos para ajustar inventario");
        }

        if (productoDAO.buscarPorId(idProducto) == null) {
            return RespuestaOperacion.error("El producto " + idProducto + " no existe");
        }

        if (cantidadFisica.compareTo(BigDecimal.ZERO) < 0) {
            return RespuestaOperacion.error("La cantidad física no puede ser negativa");
        }
        if (motivo == null || motivo.isBlank()) {
            return RespuestaOperacion.error("El ajuste debe indicar un motivo");
        }

        try (TransactionContext tx = TransactionManager.iniciar()) {
            Stock stock = stockDAO.buscarPorId(idProducto);
            BigDecimal cantidadSistema = (stock != null) ? stock.getCantidadActual() : BigDecimal.ZERO;

            AjusteInventario ajuste = new AjusteInventario(idProducto, cantidadSistema, cantidadFisica, motivo, idUsuario);

            if (ajuste.getDiferencia().compareTo(BigDecimal.ZERO) == 0) {
                return RespuestaOperacion.error("No hay diferencia entre el stock del sistema y el conteo físico");
            }

            BigDecimal cantidadMovimiento = ajuste.getDiferencia().abs();
            TipoMovimiento tipo = (ajuste.getDiferencia().compareTo(BigDecimal.ZERO) > 0)
                    ? TipoMovimiento.ENTRADA : TipoMovimiento.SALIDA;

            // Regla de negocio: un ajuste por aumento se valoriza al CPP vigente del producto
            // (o 0 si el producto aún no tenía stock ni costo previo).
            BigDecimal costoUnitario = (stock != null) ? stock.getCostoPromedioActual() : BigDecimal.ZERO;

            MovimientoInventario movimiento = new MovimientoInventario(
                    idProducto, tipo, OrigenMovimiento.AJUSTE_INVENTARIO, 0, cantidadMovimiento, costoUnitario, idUsuario);

            registrarMovimientoYVincular(ajuste, movimiento);
            if (stock == null) {

                stock = new Stock(idProducto, cantidadFisica, BigDecimal.ZERO, null);
                stockDAO.insertar(stock);
            } else {
                stock.setCantidadActual(cantidadFisica);
                stockDAO.actualizar(stock);
            }

            tx.commit();
            return RespuestaOperacion.ok();
        }
    }

    private void registrarMovimientoYVincular(AjusteInventario ajuste, MovimientoInventario movimiento) {

        movimientoDAO.insertar(movimiento); // genera id_movimiento

        // el ajuste referencia su propio movimiento como documento de origen, y viceversa
        ajuste.setIdMovimientoGenerado(movimiento.getIdMovimiento());
        ajusteDAO.insertar(ajuste); // genera id_ajuste

        movimientoDAO.vincularDocumentoOrigen(movimiento.getIdMovimiento(), ajuste.getIdAjuste());
    }

    @Override
    public List<AjusteInventario> listarAjustes(LocalDate desde, LocalDate hasta) {
        return ajusteDAO.listarPorRangoFecha(desde, hasta);
    }
}
