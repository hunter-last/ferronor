/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.dao.PlanCuentaDAO;
import com.ferronor.sic.maestros.modelo.PlanCuenta;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public class PlanCuentaServiceImpl implements PlanCuentaService {

    private final PlanCuentaDAO planCuentaDAO;

    public PlanCuentaServiceImpl(PlanCuentaDAO planCuentaDAO) {
        this.planCuentaDAO = planCuentaDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrar(PlanCuenta c) {
        RespuestaOperacion<Void> v = validarComun(c);
        if (!v.isExito()) {
            return v;
        }
        if (planCuentaDAO.buscarPorCodigo(c.getCodigo()) != null) {
            return RespuestaOperacion.error("Ya existe una cuenta con ese código");
        }
        planCuentaDAO.insertar(c);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> actualizar(PlanCuenta c) {
        RespuestaOperacion<Void> v = validarComun(c);
        if (!v.isExito()) {
            return v;
        }

        if (c.getIdCuenta() <= 0) {
            return RespuestaOperacion.error("La cuenta contable es inválida");
        }
        if (c.getIdCuentaPadre() != null && c.getIdCuentaPadre().equals(c.getIdCuenta())) {
            return RespuestaOperacion.error("Una cuenta no puede ser padre de sí misma");
        }

        PlanCuenta existente = planCuentaDAO.buscarPorId(c.getIdCuenta());
        if (existente == null) {
            return RespuestaOperacion.error("La cuenta contable no existe");
        }
        PlanCuenta conMismoCodigo = planCuentaDAO.buscarPorCodigo(c.getCodigo());
        if (conMismoCodigo != null && conMismoCodigo.getIdCuenta() != c.getIdCuenta()) {
            return RespuestaOperacion.error("Ya existe una cuenta con ese código");
        }

        boolean tieneHijos = !planCuentaDAO.listarHijos(c.getIdCuenta()).isEmpty();
        if (tieneHijos) {
            if (existente.getNivel() != c.getNivel()) {
                return RespuestaOperacion.error("No se puede cambiar el nivel de una cuenta que ya tiene subcuentas");
            }
            if (!existente.getCodigo().equals(c.getCodigo())) {
                return RespuestaOperacion.error("No se puede cambiar el código de una cuenta que ya tiene subcuentas");
            }
        }

        planCuentaDAO.actualizar(c);
        return RespuestaOperacion.ok();
    }

    @Override
    public List<PlanCuenta> listar() {
        return planCuentaDAO.listar();
    }

    @Override
    public List<PlanCuenta> listarHijos(int idCuentaPadre) {
        return planCuentaDAO.listarHijos(idCuentaPadre);
    }

    @Override
    public List<PlanCuenta> listarRaiz() {
        return planCuentaDAO.listarRaiz();
    }

    private RespuestaOperacion<Void> validarComun(PlanCuenta c) {
        if (c == null) {
            return RespuestaOperacion.error("La cuenta contable es obligatoria");
        }

        if (c.getCodigo() == null) {
            return RespuestaOperacion.error("El código es obligatorio");
        }
        c.setCodigo(c.getCodigo().trim());
        if (!c.getCodigo().matches("\\d+")) {
            return RespuestaOperacion.error("El código debe contener solo dígitos");
        }

        if (c.getNombreCuenta() == null) {
            return RespuestaOperacion.error("El nombre de la cuenta es obligatorio");
        }
        c.setNombreCuenta(c.getNombreCuenta().trim());
        if (c.getNombreCuenta().isEmpty()) {
            return RespuestaOperacion.error("El nombre de la cuenta es obligatorio");
        }

        if (c.getNivel() < 1 || c.getNivel() > 5) {
            return RespuestaOperacion.error("El nivel debe estar entre 1 y 5");
        }
        if (c.getCodigo().length() != c.getNivel()) {
            return RespuestaOperacion.error("El código debe tener " + c.getNivel() + " dígitos para el nivel " + c.getNivel());
        }

        if (c.getIdCuentaPadre() == null) {
            if (c.getNivel() != 1) {
                return RespuestaOperacion.error("Una cuenta sin padre debe ser de nivel 1");
            }
        } else {
            PlanCuenta padre = planCuentaDAO.buscarPorId(c.getIdCuentaPadre());
            if (padre == null) {
                return RespuestaOperacion.error("La cuenta padre indicada no existe");
            }
            if (c.getNivel() != padre.getNivel() + 1) {
                return RespuestaOperacion.error("El nivel debe ser consecutivo al de la cuenta padre (nivel " + (padre.getNivel() + 1) + ")");
            }
            if (!c.getCodigo().startsWith(padre.getCodigo())) {
                return RespuestaOperacion.error("El código debe iniciar con el código de la cuenta padre (" + padre.getCodigo() + ")");
            }
        }
        return RespuestaOperacion.ok();
    }

    @Override
    public PlanCuenta buscarPorId(int idCuenta) {
        return planCuentaDAO.buscarPorId(idCuenta);
    }

    @Override
    public PlanCuenta buscarPorCodigo(String codigo) {
        return planCuentaDAO.buscarPorCodigo(codigo);
    }
}
