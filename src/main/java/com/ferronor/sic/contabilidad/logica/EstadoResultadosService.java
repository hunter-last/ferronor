/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.logica;

import com.ferronor.sic.contabilidad.modelo.dto.EstadoResultadosDTO;
import java.time.LocalDate;

public interface EstadoResultadosService {

    EstadoResultadosDTO obtenerEstadoResultados(LocalDate hasta);
}
