/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.seguridad.logica;

import com.ferronor.sic.seguridad.dao.PermisoDAO;
import com.ferronor.sic.seguridad.modelo.Permiso;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.util.Validaciones;
import java.util.List;

public class PermisoServiceImpl implements PermisoService {

    private final PermisoDAO permisoDAO;

    public PermisoServiceImpl(PermisoDAO permisoDAO) {
        this.permisoDAO = permisoDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrar(Permiso p) {
        RespuestaOperacion<Void> v = validarComun(p);
        if (!v.isExito()) {
            return v;
        }
        if (permisoDAO.buscarPorCodigo(p.getCodigo()) != null) {
            return RespuestaOperacion.error("Ya existe un permiso con ese código");
        }
        permisoDAO.insertar(p);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> actualizar(Permiso p) {
        RespuestaOperacion<Void> v = validarComun(p);
        if (!v.isExito()) {
            return v;
        }
        if (p.getIdPermiso() <= 0) {
            return RespuestaOperacion.error("El permiso es inválido");
        }
        if (permisoDAO.buscarPorId(p.getIdPermiso()) == null) {
            return RespuestaOperacion.error("El permiso no existe");
        }
        Permiso conMismoCodigo = permisoDAO.buscarPorCodigo(p.getCodigo());
        if (conMismoCodigo != null && conMismoCodigo.getIdPermiso() != p.getIdPermiso()) {
            return RespuestaOperacion.error("Ya existe un permiso con ese código");
        }
        permisoDAO.actualizar(p);
        return RespuestaOperacion.ok();
    }

    @Override
    public List<Permiso> listar() {
        return permisoDAO.listar();
    }

    @Override
    public Permiso buscarPorId(int idPermiso) {
        return permisoDAO.buscarPorId(idPermiso);
    }

    @Override
    public Permiso buscarPorCodigo(String codigo) {
        return permisoDAO.buscarPorCodigo(codigo);
    }

    private RespuestaOperacion<Void> validarComun(Permiso p) {
        if (p == null) {
            return RespuestaOperacion.error("El permiso es obligatorio");
        }

        RespuestaOperacion<String> codigo
                = Validaciones.requerido(p.getCodigo(), "El código del permiso", 50);

        if (!codigo.isExito()) {
            return RespuestaOperacion.error(codigo.getMensaje());
        }

        p.setCodigo(codigo.getResultado().toUpperCase(java.util.Locale.ROOT));

        RespuestaOperacion<String> nombre
                = Validaciones.requerido(p.getNombre(), "El nombre del permiso", 100);

        if (!nombre.isExito()) {
            return RespuestaOperacion.error(nombre.getMensaje());
        }

        p.setNombre(nombre.getResultado());

        return RespuestaOperacion.ok();
    }
}
