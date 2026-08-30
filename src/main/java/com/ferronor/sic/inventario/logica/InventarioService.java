package com.ferronor.sic.inventario.logica;

import com.ferronor.sic.inventario.modelo.MovimientoInventario;
import com.ferronor.sic.inventario.modelo.OrigenMovimiento;
import com.ferronor.sic.inventario.modelo.dto.StockConsulta;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InventarioService {

    RespuestaOperacion<Void> registrarEntrada(int idProducto, BigDecimal cantidad, BigDecimal costoUnitario,
            OrigenMovimiento origen, int idDocumentoOrigen, int idUsuario);

    RespuestaOperacion<BigDecimal> registrarSalida(int idProducto, BigDecimal cantidad,
            OrigenMovimiento origen, int idDocumentoOrigen, int idUsuario);

    BigDecimal obtenerStock(int idProducto);

    BigDecimal obtenerCostoPromedioActual(int idProducto);

    List<StockConsulta> consultarStock();

    StockConsulta consultarStockPorProducto(int idProducto);

    List<MovimientoInventario> listarMovimientos(int idProducto, LocalDate desde, LocalDate hasta);

    MovimientoInventario buscarMovimientoOrigen(int idProducto, OrigenMovimiento origen, int idDocumentoOrigen);

}
