/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.logica;

import com.ferronor.sic.contabilidad.dao.AsientoDAO;
import com.ferronor.sic.contabilidad.dao.DetalleAsientoDAO;
import com.ferronor.sic.contabilidad.modelo.AsientoContable;
import com.ferronor.sic.contabilidad.modelo.DetalleAsiento;
import com.ferronor.sic.contabilidad.modelo.dto.LibroMayorItem;
import com.ferronor.sic.contabilidad.modelo.dto.MovimientoCuenta;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LibroMayorServiceImpl implements LibroMayorService {

    private final DetalleAsientoDAO detalleAsientoDAO;

    public LibroMayorServiceImpl(DetalleAsientoDAO detalleAsientoDAO) {
        this.detalleAsientoDAO = detalleAsientoDAO;
    }

    @Override
    public List<LibroMayorItem> obtenerLibroMayor(int idCuenta, LocalDate desde, LocalDate hasta) {
        List<MovimientoCuenta> movimientos = detalleAsientoDAO.listarMovimientosPorCuenta(idCuenta, hasta);
        List<LibroMayorItem> resultado = new ArrayList<>();
        BigDecimal saldo = BigDecimal.ZERO;
        LocalDateTime limiteDesde = desde.atStartOfDay();

        for (MovimientoCuenta m : movimientos) {
            saldo = saldo.add(m.getDebe()).subtract(m.getHaber());
            if (!m.getFecha().isBefore(limiteDesde)) {
                resultado.add(new LibroMayorItem(m.getFecha(), m.getGlosa(), m.getDebe(), m.getHaber(), saldo));
            }
        }
        return resultado;
    }
}
