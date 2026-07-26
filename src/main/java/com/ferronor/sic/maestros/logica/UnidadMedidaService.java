/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.modelo.UnidadMedida;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public interface UnidadMedidaService {

    RespuestaOperacion<Void> registrar(UnidadMedida unidadMedida);

    RespuestaOperacion<Void> actualizar(UnidadMedida unidadMedida);

    List<UnidadMedida> listar();

    UnidadMedida buscarPorId(int idUnidadMedida);

    UnidadMedida buscarPorNombre(String nombre);

    UnidadMedida buscarPorAbreviatura(String abreviatura);
}
