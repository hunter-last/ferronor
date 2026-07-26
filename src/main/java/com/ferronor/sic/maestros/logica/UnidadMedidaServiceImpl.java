package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.dao.UnidadMedidaDAO;
import com.ferronor.sic.maestros.modelo.UnidadMedida;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public class UnidadMedidaServiceImpl implements UnidadMedidaService {

    private final UnidadMedidaDAO unidadMedidaDAO;

    public UnidadMedidaServiceImpl(UnidadMedidaDAO unidadMedidaDAO) {
        this.unidadMedidaDAO = unidadMedidaDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrar(UnidadMedida u) {
        RespuestaOperacion<Void> v = validarComun(u);
        if (!v.isExito()) {
            return v;
        }
        if (unidadMedidaDAO.buscarPorNombre(u.getNombre()) != null) {
            return RespuestaOperacion.error("Ya existe una unidad de medida con ese nombre");
        }
        if (unidadMedidaDAO.buscarPorAbreviatura(u.getAbreviatura()) != null) {
            return RespuestaOperacion.error("Ya existe una unidad de medida con esa abreviatura");
        }
        unidadMedidaDAO.insertar(u);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> actualizar(UnidadMedida u) {
        RespuestaOperacion<Void> v = validarComun(u);
        if (!v.isExito()) {
            return v;
        }
        if (unidadMedidaDAO.buscarPorId(u.getIdUnidadMedida()) == null) {
            return RespuestaOperacion.error("La unidad de medida no existe");
        }
        if (esOtroRegistro(unidadMedidaDAO.buscarPorNombre(u.getNombre()), u)) {
            return RespuestaOperacion.error("Ya existe una unidad de medida con ese nombre");
        }
        if (esOtroRegistro(unidadMedidaDAO.buscarPorAbreviatura(u.getAbreviatura()), u)) {
            return RespuestaOperacion.error("Ya existe una unidad de medida con esa abreviatura");
        }
        unidadMedidaDAO.actualizar(u);
        return RespuestaOperacion.ok();
    }

    private RespuestaOperacion<Void> validarComun(UnidadMedida u) {
        if (u.getNombre() == null || u.getNombre().isBlank()) {
            return RespuestaOperacion.error("El nombre de la unidad de medida es obligatorio");
        }
        u.setNombre(u.getNombre().trim());
        if (u.getAbreviatura() == null || u.getAbreviatura().isBlank()) {
            return RespuestaOperacion.error("La abreviatura es obligatoria");
        }
        u.setAbreviatura(u.getAbreviatura().trim().toUpperCase(java.util.Locale.ROOT));
        return RespuestaOperacion.ok();
    }

    @Override
    public List<UnidadMedida> listar() {
        return unidadMedidaDAO.listar();
    }

    private boolean esOtroRegistro(UnidadMedida encontrada, UnidadMedida actual) {
        return encontrada != null && encontrada.getIdUnidadMedida() != actual.getIdUnidadMedida();
    }

    @Override
    public UnidadMedida buscarPorId(int idUnidadMedida) {
        return unidadMedidaDAO.buscarPorId(idUnidadMedida);
    }

    @Override
    public UnidadMedida buscarPorNombre(String nombre) {
        return unidadMedidaDAO.buscarPorNombre(nombre);
    }

    @Override
    public UnidadMedida buscarPorAbreviatura(String abreviatura) {
        return unidadMedidaDAO.buscarPorAbreviatura(abreviatura);
    }

}
