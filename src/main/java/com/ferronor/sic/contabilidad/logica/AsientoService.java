/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.logica;

import com.ferronor.sic.contabilidad.modelo.AsientoContable;
import com.ferronor.sic.contabilidad.modelo.dto.DatosCobroParaAsiento;
import com.ferronor.sic.contabilidad.modelo.dto.DatosCompraParaAsiento;
import com.ferronor.sic.contabilidad.modelo.dto.DatosPagoParaAsiento;
import com.ferronor.sic.contabilidad.modelo.dto.DatosVentaParaAsiento;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.time.LocalDate;
import java.util.List;

public interface AsientoService {

    RespuestaOperacion<Integer> registrar(AsientoContable asiento);

    RespuestaOperacion<Integer> generarAsientoVenta(DatosVentaParaAsiento datos, int idUsuario);

    RespuestaOperacion<Integer> generarAsientoCompra(DatosCompraParaAsiento datos, int idUsuario);

    RespuestaOperacion<Integer> generarAsientoCobro(DatosCobroParaAsiento datos, int idUsuario);

    RespuestaOperacion<Integer> generarAsientoPago(DatosPagoParaAsiento datos, int idUsuario);

    RespuestaOperacion<Void> anular(int idAsiento);

    AsientoContable buscarPorId(int idAsiento);

    List<AsientoContable> listar();

    List<AsientoContable> listarPorRangoFecha(LocalDate desde, LocalDate hasta);
}
