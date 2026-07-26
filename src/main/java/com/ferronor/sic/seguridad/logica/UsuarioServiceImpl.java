/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.seguridad.logica;

import com.ferronor.sic.seguridad.dao.RolDAO;
import com.ferronor.sic.seguridad.dao.UsuarioDAO;
import com.ferronor.sic.seguridad.modelo.Usuario;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.SesionUsuario;
import com.ferronor.sic.util.Validaciones;
import java.util.List;

public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final RolDAO rolDAO;

    public UsuarioServiceImpl(UsuarioDAO usuarioDAO, RolDAO rolDAO) {
        this.usuarioDAO = usuarioDAO;
        this.rolDAO = rolDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrar(Usuario u, String passwordPlano) {
        boolean esBootstrap = usuarioDAO.listar().isEmpty();
        if (!esBootstrap) {
            if (!SesionUsuario.haySesion() || !SesionUsuario.actual().tienePermiso("ADMIN_USUARIOS")) {
                return RespuestaOperacion.error("No tiene permisos para administrar usuarios");
            }
        }

        if (u == null) {
            return RespuestaOperacion.error("El usuario es obligatorio");
        }
        RespuestaOperacion<Void> v = validarDatosBasicos(u);
        if (!v.isExito()) {
            return v;
        }

        if (passwordPlano == null || passwordPlano.length() < 8) {
            return RespuestaOperacion.error("La contraseña debe tener al menos 8 caracteres");
        }
        if (usuarioDAO.buscarPorLogin(u.getUsuarioLogin()) != null) {
            return RespuestaOperacion.error("Ya existe un usuario con ese nombre de usuario");
        }

        u.setPasswordHash(org.mindrot.jbcrypt.BCrypt.hashpw(passwordPlano, org.mindrot.jbcrypt.BCrypt.gensalt()));
        usuarioDAO.insertar(u);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> actualizar(Usuario u
    ) {
        if (u == null) {
            return RespuestaOperacion.error("El usuario es obligatorio");
        }
        RespuestaOperacion<Void> v = validarDatosBasicos(u);
        if (!v.isExito()) {
            return v;
        }

        if (u.getIdUsuario() <= 0) {
            return RespuestaOperacion.error("El usuario es inválido");
        }
        Usuario existente = usuarioDAO.buscarPorId(u.getIdUsuario());
        if (existente == null) {
            return RespuestaOperacion.error("El usuario no existe");
        }

        Usuario conMismoLogin = usuarioDAO.buscarPorLogin(u.getUsuarioLogin());
        if (conMismoLogin != null && conMismoLogin.getIdUsuario() != u.getIdUsuario()) {
            return RespuestaOperacion.error("Ya existe un usuario con ese nombre de usuario");
        }

        u.setPasswordHash(existente.getPasswordHash()); // actualizar() nunca toca la contraseña
        usuarioDAO.actualizar(u);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> cambiarPassword(int idUsuario, String passwordNuevoPlano
    ) {
        if (idUsuario <= 0) {
            return RespuestaOperacion.error("El usuario es inválido");
        }
        if (usuarioDAO.buscarPorId(idUsuario) == null) {
            return RespuestaOperacion.error("El usuario no existe");
        }
        if (passwordNuevoPlano == null || passwordNuevoPlano.length() < 8) {
            return RespuestaOperacion.error("La contraseña debe tener al menos 8 caracteres");
        }
        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(passwordNuevoPlano, org.mindrot.jbcrypt.BCrypt.gensalt());
        usuarioDAO.actualizarPassword(idUsuario, hash);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> activar(int idUsuario
    ) {
        if (!SesionUsuario.haySesion() || !SesionUsuario.actual().tienePermiso("ADMIN_USUARIOS")) {
            return RespuestaOperacion.error("No tiene permisos para administrar usuarios");
        }
        if (idUsuario <= 0) {
            return RespuestaOperacion.error("El usuario es inválido");
        }
        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            return RespuestaOperacion.error("El usuario no existe");
        }
        if (usuario.isActivo()) {
            return RespuestaOperacion.error("El usuario ya se encuentra activo");
        }
        usuarioDAO.activar(idUsuario);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> desactivar(int idUsuario
    ) {

        if (!SesionUsuario.haySesion() || !SesionUsuario.actual().tienePermiso("ADMIN_USUARIOS")) {
            return RespuestaOperacion.error("No tiene permisos para administrar usuarios");
        }
        if (idUsuario <= 0) {
            return RespuestaOperacion.error("El usuario es inválido");
        }
        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            return RespuestaOperacion.error("El usuario no existe");
        }
        if (!usuario.isActivo()) {
            return RespuestaOperacion.error("El usuario ya se encuentra desactivado");
        }
        usuarioDAO.desactivar(idUsuario);
        return RespuestaOperacion.ok();
    }

    @Override
    public List<Usuario> listar() {
        return usuarioDAO.listar();
    }

    @Override
    public List<Usuario> listarActivos() {
        return usuarioDAO.listarActivos();
    }

    @Override
    public Usuario buscarPorId(int idUsuario
    ) {
        return usuarioDAO.buscarPorId(idUsuario);
    }

    @Override
    public Usuario buscarPorLogin(String usuarioLogin
    ) {
        return usuarioDAO.buscarPorLogin(usuarioLogin);
    }

    private RespuestaOperacion<Void> validarDatosBasicos(Usuario u) {
        RespuestaOperacion<String> r;

        r = Validaciones.requerido(u.getNombres(), "Los nombres", 100);
        if (!r.isExito()) {
            return RespuestaOperacion.error(r.getMensaje());
        }
        u.setNombres(r.getResultado());

        r = Validaciones.requerido(u.getApellidos(), "Los apellidos", 100);
        if (!r.isExito()) {
            return RespuestaOperacion.error(r.getMensaje());
        }
        u.setApellidos(r.getResultado());

        r = Validaciones.requerido(u.getUsuarioLogin(), "El usuario de acceso", 30);
        if (!r.isExito()) {
            return RespuestaOperacion.error(r.getMensaje());
        }
        u.setUsuarioLogin(r.getResultado().toLowerCase(java.util.Locale.ROOT));

        if (rolDAO.buscarPorId(u.getIdRol()) == null) {
            return RespuestaOperacion.error("El rol indicado no existe");
        }
        return RespuestaOperacion.ok();
    }

}
