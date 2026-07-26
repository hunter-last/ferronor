
package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.dao.TipoComprobanteDAO;
import com.ferronor.sic.maestros.modelo.TipoComprobante;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public class TipoComprobanteServiceImpl implements TipoComprobanteService {

    private final TipoComprobanteDAO tipoComprobanteDAO;

    public TipoComprobanteServiceImpl(TipoComprobanteDAO tipoComprobanteDAO) {
        this.tipoComprobanteDAO = tipoComprobanteDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrar(TipoComprobante t) {
        RespuestaOperacion<Void> v = validarComun(t);
        if (!v.isExito()) {
            return v;
        }
        if (tipoComprobanteDAO.buscarPorNombre(t.getNombre()) != null) {
            return RespuestaOperacion.error("Ya existe un tipo de comprobante con ese nombre");
        }
        tipoComprobanteDAO.insertar(t);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> actualizar(TipoComprobante t) {
        RespuestaOperacion<Void> v = validarComun(t);
        if (!v.isExito()) {
            return v;
        }
        if (t.getIdTipoComprobante() <= 0) {
            return RespuestaOperacion.error("El tipo de comprobante es inválido");
        }
        if (tipoComprobanteDAO.buscarPorId(t.getIdTipoComprobante()) == null) {
            return RespuestaOperacion.error("El tipo de comprobante no existe");
        }
        TipoComprobante conMismoNombre = tipoComprobanteDAO.buscarPorNombre(t.getNombre());
        if (conMismoNombre != null && conMismoNombre.getIdTipoComprobante() != t.getIdTipoComprobante()) {
            return RespuestaOperacion.error("Ya existe un tipo de comprobante con ese nombre");
        }
        tipoComprobanteDAO.actualizar(t);
        return RespuestaOperacion.ok();
    }

    @Override
    public List<TipoComprobante> listar() {
        return tipoComprobanteDAO.listar();
    }

    private RespuestaOperacion<Void> validarComun(TipoComprobante t) {
        if (t == null) {
            return RespuestaOperacion.error("El tipo de comprobante es obligatorio");
        }
        if (t.getNombre() == null || t.getNombre().isBlank()) {
            return RespuestaOperacion.error("El nombre del tipo de comprobante es obligatorio");
        }
        t.setNombre(t.getNombre().trim());
        return RespuestaOperacion.ok();
    }

    @Override
    public TipoComprobante buscarPorId(int idTipoComprobante) {
        return tipoComprobanteDAO.buscarPorId(idTipoComprobante);
    }

    @Override
    public TipoComprobante buscarPorNombre(String nombre) {
        return tipoComprobanteDAO.buscarPorNombre(nombre);
    }
}
