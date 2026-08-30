/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.auditoria.logica;

import com.ferronor.sic.auditoria.modelo.Auditoria;
import java.util.List;

public interface AuditoriaService {

    void registrarLogin(int idUsuario);

    void registrarLogout(int idUsuario);

    void registrarCambioEstadoUsuario(int idUsuarioQueActua, int idUsuarioAfectado, boolean activado);

    List<Auditoria> listar();
}
