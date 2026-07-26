/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.seguridad.logica;

import com.ferronor.sic.shared.RespuestaOperacion;

public interface LoginService {

    RespuestaOperacion<Void> iniciarSesion(String usuarioLogin, String password);

    void cerrarSesion();
}
