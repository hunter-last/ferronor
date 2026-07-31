/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.logica;

import com.ferronor.sic.contabilidad.modelo.AsientoContable;
import com.ferronor.sic.contabilidad.modelo.dto.BalanceComprobacionItem;
import com.ferronor.sic.contabilidad.modelo.dto.EstadoResultadosDTO;
import com.ferronor.sic.contabilidad.modelo.dto.LibroMayorItem;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.time.LocalDate;
import java.util.List;

public interface ContabilidadService {

    RespuestaOperacion<Integer> registrarAsiento(AsientoContable asiento);

    RespuestaOperacion<Void> anularAsiento(int idAsiento);

    List<AsientoContable> obtenerLibroDiario(LocalDate desde, LocalDate hasta);

    List<LibroMayorItem> obtenerLibroMayor(int idCuenta, LocalDate desde, LocalDate hasta);

    List<BalanceComprobacionItem> obtenerBalanceComprobacion(LocalDate hasta);

    EstadoResultadosDTO obtenerEstadoResultados(LocalDate hasta);
}
