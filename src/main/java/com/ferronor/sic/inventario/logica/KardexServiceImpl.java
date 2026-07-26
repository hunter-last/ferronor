package com.ferronor.sic.inventario.logica;

import com.ferronor.sic.inventario.dao.MovimientoInventarioDAO;
import com.ferronor.sic.inventario.modelo.MovimientoInventario;
import com.ferronor.sic.inventario.modelo.TipoMovimiento;
import com.ferronor.sic.inventario.modelo.dto.KardexItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class KardexServiceImpl implements KardexService {

    private final MovimientoInventarioDAO movimientoDAO;

    public KardexServiceImpl(MovimientoInventarioDAO movimientoDAO) {
        this.movimientoDAO = movimientoDAO;
    }

    @Override
    public List<KardexItem> obtenerKardex(int idProducto, LocalDate desde, LocalDate hasta) {
        LocalDateTime limiteDesde = desde.atStartOfDay();
        LocalDateTime limiteHasta = hasta.plusDays(1).atStartOfDay();

        List<MovimientoInventario> movimientos = movimientoDAO.listarHastaFecha(idProducto, limiteHasta);

        List<KardexItem> resultado = new ArrayList<>();
        BigDecimal saldoCantidad = BigDecimal.ZERO;
        BigDecimal saldoValor = BigDecimal.ZERO;

        for (MovimientoInventario mov : movimientos) {
            BigDecimal valorMovimiento = mov.getCantidad().multiply(mov.getCostoUnitario());
            BigDecimal entrada = BigDecimal.ZERO;
            BigDecimal salida = BigDecimal.ZERO;

            if (mov.getTipo() == TipoMovimiento.ENTRADA) {
                entrada = mov.getCantidad();
                saldoCantidad = saldoCantidad.add(mov.getCantidad());
                saldoValor = saldoValor.add(valorMovimiento);
            } else {
                salida = mov.getCantidad();
                saldoCantidad = saldoCantidad.subtract(mov.getCantidad());
                saldoValor = saldoValor.subtract(valorMovimiento);
            }

            if (!mov.getFecha().isBefore(limiteDesde)) {
                resultado.add(new KardexItem(
                        mov.getFecha(), mov.getTipo(), mov.getOrigen(), mov.getIdDocumentoOrigen(),
                        entrada, salida, saldoCantidad, mov.getCostoUnitario(), saldoValor
                ));
            }
        }
        return resultado;
    }

}
