/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.seguridad.logica;

import com.ferronor.sic.seguridad.modelo.Permiso;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public interface PermisoService {

    RespuestaOperacion<Void> registrar(Permiso permiso);

    RespuestaOperacion<Void> actualizar(Permiso permiso);

    List<Permiso> listar();

    Permiso buscarPorId(int idPermiso);

    Permiso buscarPorCodigo(String codigo);
}
