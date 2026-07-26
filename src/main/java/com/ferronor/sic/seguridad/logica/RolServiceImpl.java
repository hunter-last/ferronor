/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.seguridad.logica;

import com.ferronor.sic.seguridad.dao.PermisoDAO;
import com.ferronor.sic.seguridad.dao.RolDAO;
import com.ferronor.sic.seguridad.dao.RolPermisoDAO;
import com.ferronor.sic.seguridad.modelo.Permiso;
import com.ferronor.sic.seguridad.modelo.Rol;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.util.Validaciones;
import java.util.List;

public class RolServiceImpl implements RolService {

    private final RolDAO rolDAO;
    private final RolPermisoDAO rolPermisoDAO;
    private final PermisoDAO permisoDAO;

    public RolServiceImpl(RolDAO rolDAO, RolPermisoDAO rolPermisoDAO, PermisoDAO permisoDAO) {
        this.rolDAO = rolDAO;
        this.rolPermisoDAO = rolPermisoDAO;
        this.permisoDAO = permisoDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrar(Rol r) {
        RespuestaOperacion<Void> v = validarComun(r);
        if (!v.isExito()) {
            return v;
        }
        if (rolDAO.buscarPorNombre(r.getNombre()) != null) {
            return RespuestaOperacion.error("Ya existe un rol con ese nombre");
        }
        rolDAO.insertar(r);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> actualizar(Rol r) {
        RespuestaOperacion<Void> v = validarComun(r);
        if (!v.isExito()) {
            return v;
        }
        if (r.getIdRol() <= 0) {
            return RespuestaOperacion.error("El rol es inválido");
        }
        if (rolDAO.buscarPorId(r.getIdRol()) == null) {
            return RespuestaOperacion.error("El rol no existe");
        }
        Rol conMismoNombre = rolDAO.buscarPorNombre(r.getNombre());
        if (conMismoNombre != null && conMismoNombre.getIdRol() != r.getIdRol()) {
            return RespuestaOperacion.error("Ya existe un rol con ese nombre");
        }
        rolDAO.actualizar(r);
        return RespuestaOperacion.ok();
    }

    @Override
    public List<Rol> listar() {
        return rolDAO.listar();
    }

    @Override
    public Rol buscarPorId(int idRol) {
        return rolDAO.buscarPorId(idRol);
    }

    @Override
    public Rol buscarPorNombre(String nombre) {
        return rolDAO.buscarPorNombre(nombre);
    }

    private RespuestaOperacion<Void> validarComun(Rol r) {
        if (r == null) {
            return RespuestaOperacion.error("El rol es obligatorio");
        }
        RespuestaOperacion<String> res = Validaciones.requerido(r.getNombre(), "El nombre del rol", 50);
        if (!res.isExito()) {
            return RespuestaOperacion.error(res.getMensaje());
        }
        r.setNombre(res.getResultado());
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> asignarPermiso(int idRol, int idPermiso) {
        if (rolDAO.buscarPorId(idRol) == null) {
            return RespuestaOperacion.error("El rol no existe");
        }
        if (permisoDAO.buscarPorId(idPermiso) == null) {
            return RespuestaOperacion.error("El permiso no existe");
        }
        rolPermisoDAO.asignar(idRol, idPermiso);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> revocarPermiso(int idRol, int idPermiso) {
        if (rolDAO.buscarPorId(idRol) == null) {
            return RespuestaOperacion.error("El rol no existe");
        }
        if (permisoDAO.buscarPorId(idPermiso) == null) {
            return RespuestaOperacion.error("El permiso no existe");
        }
        rolPermisoDAO.revocar(idRol, idPermiso);
        return RespuestaOperacion.ok();
    }

    @Override
    public List<Permiso> obtenerPermisos(int idRol) {
        return permisoDAO.listarPorRol(idRol);
    }
}
