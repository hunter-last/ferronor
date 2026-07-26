/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.seguridad.dao;

import com.ferronor.sic.seguridad.modelo.Rol;
import com.ferronor.sic.shared.IGeneralDAO;

public interface RolDAO extends IGeneralDAO<Rol, Integer> {

    Rol buscarPorNombre(String nombre);
}
