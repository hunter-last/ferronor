/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.seguridad.logica;

import com.ferronor.sic.seguridad.modelo.Usuario;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public interface UsuarioService {

    RespuestaOperacion<Void> registrar(Usuario usuario, String passwordPlano);

    RespuestaOperacion<Void> actualizar(Usuario usuario);

    RespuestaOperacion<Void> cambiarPassword(int idUsuario, String passwordNuevoPlano);

    RespuestaOperacion<Void> activar(int idUsuario);

    RespuestaOperacion<Void> desactivar(int idUsuario);

    List<Usuario> listar();

    List<Usuario> listarActivos();

    Usuario buscarPorId(int idUsuario);

    Usuario buscarPorLogin(String usuarioLogin);
}
