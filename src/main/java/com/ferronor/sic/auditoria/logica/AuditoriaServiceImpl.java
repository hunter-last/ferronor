/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.auditoria.logica;

import com.ferronor.sic.auditoria.dao.AuditoriaDAO;
import com.ferronor.sic.auditoria.modelo.Auditoria;
import com.ferronor.sic.auditoria.modelo.TipoOperacionAuditoria;
import java.net.InetAddress;
import java.util.List;

public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaDAO auditoriaDAO;

    public AuditoriaServiceImpl(AuditoriaDAO auditoriaDAO) {
        this.auditoriaDAO = auditoriaDAO;
    }

    @Override
    public void registrarLogin(int idUsuario) {
        registrarSeguro(idUsuario, "usuario", idUsuario, TipoOperacionAuditoria.LOGIN, "Inicio de sesión");
    }

    @Override
    public void registrarLogout(int idUsuario) {
        registrarSeguro(idUsuario, "usuario", idUsuario, TipoOperacionAuditoria.LOGOUT, "Cierre de sesión");
    }

    @Override
    public void registrarCambioEstadoUsuario(int idUsuarioQueActua, int idUsuarioAfectado, boolean activado) {
        registrarSeguro(idUsuarioQueActua, "usuario", idUsuarioAfectado, TipoOperacionAuditoria.UPDATE,
                activado ? "Usuario activado" : "Usuario desactivado");
    }

    @Override
    public List<Auditoria> listar() {
        return auditoriaDAO.listar();
    }

    // La auditoría es "best effort": si falla, no debe bloquear login/activación (no es el proceso crítico)
    private void registrarSeguro(int idUsuario, String tabla, int idRegistro, TipoOperacionAuditoria op, String descripcion) {
        try {
            Auditoria a = new Auditoria(idUsuario, tabla, idRegistro, op, descripcion, obtenerNombreEquipo());
            auditoriaDAO.insertar(a);
        } catch (Exception e) {
            System.err.println("No se pudo registrar auditoría: " + e.getMessage());
        }
    }

    private String obtenerNombreEquipo() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "DESCONOCIDO";
        }
    }
}
