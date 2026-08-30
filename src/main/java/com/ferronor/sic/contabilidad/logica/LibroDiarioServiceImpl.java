/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.logica;

import com.ferronor.sic.contabilidad.dao.AsientoDAO;
import com.ferronor.sic.contabilidad.dao.DetalleAsientoDAO;
import com.ferronor.sic.contabilidad.modelo.AsientoContable;
import com.ferronor.sic.contabilidad.modelo.DetalleAsiento;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibroDiarioServiceImpl implements LibroDiarioService {

    private final AsientoDAO asientoDAO;
    private final DetalleAsientoDAO detalleAsientoDAO;

    public LibroDiarioServiceImpl(AsientoDAO asientoDAO, DetalleAsientoDAO detalleAsientoDAO) {
        this.asientoDAO = asientoDAO;
        this.detalleAsientoDAO = detalleAsientoDAO;
    }

    @Override
    public List<AsientoContable> obtenerLibroDiario(LocalDate desde, LocalDate hasta) {
        List<AsientoContable> asientos = asientoDAO.listarPorRangoFecha(desde, hasta);
        if (asientos.isEmpty()) {
            return asientos;
        }

        List<Integer> ids = asientos.stream().map(AsientoContable::getIdAsiento).toList();
        List<DetalleAsiento> todosDetalles = detalleAsientoDAO.listarPorAsientos(ids);

        Map<Integer, List<DetalleAsiento>> porAsiento = new HashMap<>();
        for (DetalleAsiento d : todosDetalles) {
            porAsiento.computeIfAbsent(d.getIdAsiento(), k -> new ArrayList<>()).add(d);
        }
        for (AsientoContable a : asientos) {
            List<DetalleAsiento> detalles = porAsiento.get(a.getIdAsiento());
            if (detalles != null) {
                detalles.forEach(a::agregarDetalle);
            }
        }
        return asientos;
    }
}
