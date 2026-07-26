/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.inventario.logica;

import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.inventario.dao.MovimientoInventarioDAO;
import com.ferronor.sic.inventario.dao.StockDAO;
import com.ferronor.sic.inventario.modelo.MovimientoInventario;
import com.ferronor.sic.inventario.modelo.OrigenMovimiento;
import com.ferronor.sic.inventario.modelo.Stock;
import com.ferronor.sic.inventario.modelo.TipoMovimiento;
import com.ferronor.sic.maestros.dao.ProductoDAO;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.util.CalculadoraCPP;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InventarioServiceImpl implements InventarioService {

    private final StockDAO stockDAO;
    private final MovimientoInventarioDAO movimientoDAO;
    private final ProductoDAO productoDAO;

    public InventarioServiceImpl(StockDAO stockDAO, MovimientoInventarioDAO movimientoDAO, ProductoDAO productoDAO) {
        this.stockDAO = stockDAO;
        this.movimientoDAO = movimientoDAO;
        this.productoDAO = productoDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrarEntrada(int idProducto, BigDecimal cantidad, BigDecimal costoUnitario,
            OrigenMovimiento origen, int idDocumentoOrigen, int idUsuario) {

        if (productoDAO.buscarPorId(idProducto) == null) {
            return RespuestaOperacion.error("El producto " + idProducto + " no existe");
        }

        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return RespuestaOperacion.error("La cantidad de entrada debe ser mayor a cero");
        }
        if (costoUnitario == null) {
            return RespuestaOperacion.error("El costo unitario es obligatorio");
        }

        if (costoUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            return RespuestaOperacion.error("El costo unitario debe ser mayor a cero");
        }

        if (origen == null) {
            return RespuestaOperacion.error("El origen del movimiento es obligatorio");
        }

        try (TransactionContext tx = TransactionManager.iniciar()) {
            Stock stock = stockDAO.buscarPorId(idProducto);
            boolean esNuevo = (stock == null);
            if (esNuevo) {
                stock = new Stock(idProducto, BigDecimal.ZERO, BigDecimal.ZERO, null);
            }

            BigDecimal nuevoCPP = CalculadoraCPP.calcularNuevoCPP(
                    stock.getCantidadActual(), stock.getCostoPromedioActual(), cantidad, costoUnitario);

            stock.setCantidadActual(stock.getCantidadActual().add(cantidad));
            stock.setCostoPromedioActual(nuevoCPP);

            if (esNuevo) {
                stockDAO.insertar(stock);
            } else {
                stockDAO.actualizar(stock);
            }

            MovimientoInventario movimiento = new MovimientoInventario(
                    idProducto, TipoMovimiento.ENTRADA, origen, idDocumentoOrigen, cantidad, costoUnitario, idUsuario);
            movimientoDAO.insertar(movimiento);

            tx.commit();
            return RespuestaOperacion.ok();
        }
    }

    @Override
    public RespuestaOperacion<Void> registrarSalida(int idProducto, BigDecimal cantidad,
            OrigenMovimiento origen, int idDocumentoOrigen, int idUsuario) {

        if (origen == null) {
            return RespuestaOperacion.error(
                    "El origen del movimiento es obligatorio");
        }

        if (productoDAO.buscarPorId(idProducto) == null) {
            return RespuestaOperacion.error(
                    "El producto " + idProducto + " no existe");
        }

        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return RespuestaOperacion.error("La cantidad de salida debe ser mayor a cero");
        }

        try (TransactionContext tx = TransactionManager.iniciar()) {
            Stock stock = stockDAO.buscarPorId(idProducto);

            if (stock == null || stock.getCantidadActual().compareTo(cantidad) < 0) {
                return RespuestaOperacion.error("Stock insuficiente para el producto " + idProducto);
            }

            BigDecimal costoUnitarioSalida = stock.getCostoPromedioActual();
            stock.setCantidadActual(stock.getCantidadActual().subtract(cantidad));
            stockDAO.actualizar(stock);

            MovimientoInventario movimiento = new MovimientoInventario(
                    idProducto, TipoMovimiento.SALIDA, origen, idDocumentoOrigen, cantidad, costoUnitarioSalida, idUsuario);
            movimientoDAO.insertar(movimiento);

            tx.commit();
            return RespuestaOperacion.ok();
        }
    }

    @Override
    public BigDecimal obtenerStock(int idProducto) {
        Stock stock = stockDAO.buscarPorId(idProducto);
        return (stock != null) ? stock.getCantidadActual() : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal obtenerCostoPromedioActual(int idProducto) {
        Stock stock = stockDAO.buscarPorId(idProducto);
        return (stock != null) ? stock.getCostoPromedioActual() : BigDecimal.ZERO;
    }

    @Override
    public List<MovimientoInventario> listarMovimientos(int idProducto, LocalDate desde, LocalDate hasta) {
        return movimientoDAO.listarPorProductoYFecha(idProducto, desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
    }
}
