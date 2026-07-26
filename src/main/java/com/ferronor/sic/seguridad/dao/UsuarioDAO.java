/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.seguridad.dao;

import com.ferronor.sic.seguridad.modelo.Usuario;
import com.ferronor.sic.shared.IGeneralDAO;
import java.util.List;

public interface UsuarioDAO extends IGeneralDAO<Usuario, Integer> {

    Usuario buscarPorLogin(String usuarioLogin);

    List<Usuario> listarActivos();

    void activar(int idUsuario);

    void desactivar(int idUsuario);
    
    void actualizarPassword(int idUsuario, String passwordHash);

}
