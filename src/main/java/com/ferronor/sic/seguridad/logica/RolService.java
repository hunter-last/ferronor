/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.seguridad.logica;

import com.ferronor.sic.seguridad.modelo.Permiso;
import com.ferronor.sic.seguridad.modelo.Rol;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public interface RolService {

    RespuestaOperacion<Void> registrar(Rol rol);

    RespuestaOperacion<Void> actualizar(Rol rol);

    List<Rol> listar();

    Rol buscarPorId(int idRol);

    Rol buscarPorNombre(String nombre);

    RespuestaOperacion<Void> asignarPermiso(int idRol, int idPermiso);

    RespuestaOperacion<Void> revocarPermiso(int idRol, int idPermiso);

    List<Permiso> obtenerPermisos(int idRol);
}
