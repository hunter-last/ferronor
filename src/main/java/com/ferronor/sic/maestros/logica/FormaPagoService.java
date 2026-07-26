/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.modelo.FormaPago;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public interface FormaPagoService {

    RespuestaOperacion<Void> registrar(FormaPago formaPago);

    RespuestaOperacion<Void> actualizar(FormaPago formaPago);

    List<FormaPago> listar();

    FormaPago buscarPorId(int idFormaPago);

    FormaPago buscarPorNombre(String nombre);
}
