
package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.dao.FormaPagoDAO;
import com.ferronor.sic.maestros.modelo.FormaPago;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public class FormaPagoServiceImpl implements FormaPagoService {

    private final FormaPagoDAO formaPagoDAO;

    public FormaPagoServiceImpl(FormaPagoDAO formaPagoDAO) {
        this.formaPagoDAO = formaPagoDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrar(FormaPago f) {
        RespuestaOperacion<Void> v = validarComun(f);
        if (!v.isExito()) {
            return v;
        }
        if (formaPagoDAO.buscarPorNombre(f.getNombre()) != null) {
            return RespuestaOperacion.error("Ya existe una forma de pago con ese nombre");
        }
        formaPagoDAO.insertar(f);
        return RespuestaOperacion.ok();
    }

    private RespuestaOperacion<Void> validarComun(FormaPago f) {
        if (f == null) {
            return RespuestaOperacion.error("La forma de pago es obligatoria");
        }
        if (f.getNombre() == null || f.getNombre().isBlank()) {
            return RespuestaOperacion.error("El nombre de la forma de pago es obligatorio");
        }
        f.setNombre(f.getNombre().trim());
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> actualizar(FormaPago f) {
        RespuestaOperacion<Void> v = validarComun(f);
        if (!v.isExito()) {
            return v;
        }
        if (f.getIdFormaPago() <= 0) {
            return RespuestaOperacion.error("La forma de pago es inválida");
        }
        if (formaPagoDAO.buscarPorId(f.getIdFormaPago()) == null) {
            return RespuestaOperacion.error("La forma de pago no existe");
        }
        FormaPago conMismoNombre = formaPagoDAO.buscarPorNombre(f.getNombre());
        if (conMismoNombre != null && conMismoNombre.getIdFormaPago() != f.getIdFormaPago()) {
            return RespuestaOperacion.error("Ya existe una forma de pago con ese nombre");
        }
        formaPagoDAO.actualizar(f);
        return RespuestaOperacion.ok();
    }

    @Override
    public List<FormaPago> listar() {
        return formaPagoDAO.listar();
    }

    @Override
    public FormaPago buscarPorId(int idFormaPago) {
        return formaPagoDAO.buscarPorId(idFormaPago);
    }

    @Override
    public FormaPago buscarPorNombre(String nombre) {
        return formaPagoDAO.buscarPorNombre(nombre);
    }

}
