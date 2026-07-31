/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.logica;

import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.contabilidad.dao.AsientoDAO;
import com.ferronor.sic.contabilidad.dao.DetalleAsientoDAO;
import com.ferronor.sic.contabilidad.modelo.AsientoContable;
import com.ferronor.sic.contabilidad.modelo.DetalleAsiento;
import com.ferronor.sic.contabilidad.modelo.EstadoAsiento;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AsientoServiceImpl implements AsientoService {

    private final AsientoDAO asientoDAO;
    private final DetalleAsientoDAO detalleAsientoDAO;

    public AsientoServiceImpl(AsientoDAO asientoDAO, DetalleAsientoDAO detalleAsientoDAO) {
        this.asientoDAO = asientoDAO;
        this.detalleAsientoDAO = detalleAsientoDAO;
    }

    @Override
    public RespuestaOperacion<Integer> registrar(AsientoContable asiento) {
        if (asiento == null) {
            return RespuestaOperacion.error("El asiento contable es obligatorio");
        }
        if (asiento.getGlosa() == null || asiento.getGlosa().isBlank()) {
            return RespuestaOperacion.error("La glosa del asiento es obligatoria");
        }
        if (asiento.getDetalles().size() < 2) {
            return RespuestaOperacion.error("Un asiento contable necesita al menos dos líneas (partida doble)");
        }

        BigDecimal totalDebe = BigDecimal.ZERO;
        BigDecimal totalHaber = BigDecimal.ZERO;
        for (DetalleAsiento d : asiento.getDetalles()) {
            boolean tieneDebe = d.getDebe().compareTo(BigDecimal.ZERO) > 0;
            boolean tieneHaber = d.getHaber().compareTo(BigDecimal.ZERO) > 0;
            if (tieneDebe == tieneHaber) { // ambos > 0, o ambos en cero
                return RespuestaOperacion.error("Cada línea del asiento debe tener Debe o Haber, pero no ambos ni ninguno");
            }
            totalDebe = totalDebe.add(d.getDebe());
            totalHaber = totalHaber.add(d.getHaber());
        }
        if (totalDebe.compareTo(totalHaber) != 0) {
            return RespuestaOperacion.error("El asiento no cuadra: Debe=" + totalDebe + " Haber=" + totalHaber);
        }

        try (TransactionContext tx = TransactionManager.iniciar()) {
            asientoDAO.insertar(asiento);
            for (DetalleAsiento d : asiento.getDetalles()) {
                d.setIdAsiento(asiento.getIdAsiento());
                detalleAsientoDAO.insertar(d);
            }
            tx.commit();
            return RespuestaOperacion.ok(asiento.getIdAsiento());
        }
    }

    @Override
    public RespuestaOperacion<Void> anular(int idAsiento) {
        AsientoContable asiento = asientoDAO.buscarPorId(idAsiento);
        if (asiento == null) {
            return RespuestaOperacion.error("El asiento no existe");
        }
        if (asiento.getEstado() == EstadoAsiento.ANULADO) {
            return RespuestaOperacion.error("El asiento ya se encuentra anulado");
        }
        asientoDAO.anular(idAsiento);
        return RespuestaOperacion.ok();
    }

    @Override
    public AsientoContable buscarPorId(int idAsiento) {
        AsientoContable asiento = asientoDAO.buscarPorId(idAsiento);
        if (asiento != null) {
            for (DetalleAsiento d : detalleAsientoDAO.listarPorAsiento(idAsiento)) {
                asiento.agregarDetalle(d);
            }
        }
        return asiento;
    }

    @Override
    public List<AsientoContable> listarPorRangoFecha(LocalDate desde, LocalDate hasta) {
        return asientoDAO.listarPorRangoFecha(desde, hasta);
    }

    @Override
    public List<AsientoContable> listar() {
        return asientoDAO.listar();
    } // nuevo método en AsientoDAO, sin filtro de fecha
}
