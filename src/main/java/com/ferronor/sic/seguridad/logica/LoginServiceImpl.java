/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.seguridad.logica;

import com.ferronor.sic.exception.ServiceException;
import com.ferronor.sic.seguridad.dao.PermisoDAO;
import com.ferronor.sic.seguridad.dao.RolDAO;
import com.ferronor.sic.seguridad.dao.UsuarioDAO;
import com.ferronor.sic.seguridad.modelo.Permiso;
import com.ferronor.sic.seguridad.modelo.Rol;
import com.ferronor.sic.seguridad.modelo.Usuario;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.SesionUsuario;

import java.util.List;
import java.util.Set;
import org.mindrot.jbcrypt.BCrypt;

public class LoginServiceImpl implements LoginService {

    private final UsuarioDAO usuarioDAO;
    private final RolDAO rolDAO;
    private final PermisoDAO permisoDAO; // ya no depende de RolPermisoDAO

    public LoginServiceImpl(UsuarioDAO usuarioDAO, RolDAO rolDAO, PermisoDAO permisoDAO) {
        this.usuarioDAO = usuarioDAO;
        this.rolDAO = rolDAO;
        this.permisoDAO = permisoDAO;
    }

    @Override
    public RespuestaOperacion<Void> iniciarSesion(String usuarioLogin, String password) {
        if (usuarioLogin == null || usuarioLogin.isBlank() || password == null || password.isBlank()) {
            return RespuestaOperacion.error("Usuario y contraseña son obligatorios");
        }

        Usuario usuario = usuarioDAO.buscarPorLogin(usuarioLogin.trim().toLowerCase(java.util.Locale.ROOT));
        if (usuario == null || !usuario.isActivo()) {
            return RespuestaOperacion.error("Usuario o contraseña incorrectos");
        }

        try {
            if (!BCrypt.checkpw(password, usuario.getPasswordHash())) {
                return RespuestaOperacion.error("Usuario o contraseña incorrectos");
            }
        } catch (IllegalArgumentException e) {
            throw new ServiceException("El hash de contraseña almacenado es inválido para el usuario " + usuarioLogin, e);
        }

        Rol rol = rolDAO.buscarPorId(usuario.getIdRol());

        if (rol == null) {
            throw new ServiceException("El rol asociado al usuario no existe");
        }
        
        List<Permiso> permisos = permisoDAO.listarPorRol(rol.getIdRol());        
        Set<String> codigosPermiso = permisos.stream().map(Permiso::getCodigo).collect(java.util.stream.Collectors.toSet());

        SesionUsuario.iniciar(usuario.getIdUsuario(), usuario.getNombreCompleto(), rol.getNombre(), codigosPermiso);
        return RespuestaOperacion.ok();
    }

    @Override
    public void cerrarSesion() {
        SesionUsuario.cerrar();
    }
}
