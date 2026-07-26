/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.modelo.TipoComprobante;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public interface TipoComprobanteService {

    RespuestaOperacion<Void> registrar(TipoComprobante tipoComprobante);

    RespuestaOperacion<Void> actualizar(TipoComprobante tipoComprobante);

    List<TipoComprobante> listar();

    TipoComprobante buscarPorId(int idTipoComprobante);

    TipoComprobante buscarPorNombre(String nombre);
}
