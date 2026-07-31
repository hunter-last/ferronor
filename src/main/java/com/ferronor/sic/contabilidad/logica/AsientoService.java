/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.logica;

import com.ferronor.sic.contabilidad.modelo.AsientoContable;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.time.LocalDate;
import java.util.List;

public interface AsientoService {

    RespuestaOperacion<Integer> registrar(AsientoContable asiento);

    RespuestaOperacion<Void> anular(int idAsiento);

    AsientoContable buscarPorId(int idAsiento);
    
    List<AsientoContable> listar();

    List<AsientoContable> listarPorRangoFecha(LocalDate desde, LocalDate hasta);

    
}
