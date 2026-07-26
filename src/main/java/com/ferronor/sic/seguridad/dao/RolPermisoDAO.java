/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.seguridad.dao;

import java.util.List;

public interface RolPermisoDAO {

    void asignar(int idRol, int idPermiso);

    void revocar(int idRol, int idPermiso);

    List<Integer> listarIdsPermisoPorRol(int idRol);
}
