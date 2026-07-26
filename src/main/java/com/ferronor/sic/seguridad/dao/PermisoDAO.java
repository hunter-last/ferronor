/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.seguridad.dao;


import com.ferronor.sic.seguridad.modelo.Permiso;
import com.ferronor.sic.shared.IGeneralDAO;
import java.util.List;


public interface PermisoDAO extends IGeneralDAO<Permiso, Integer> {

    Permiso buscarPorCodigo(String codigo);

    List<Permiso> listarPorRol(int idRol); // hace JOIN con rol_permiso — lo necesita RolPermisoDAO/LoginService
}
