/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.modelo.PlanCuenta;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public interface PlanCuentaService {

    RespuestaOperacion<Void> registrar(PlanCuenta cuenta);

    RespuestaOperacion<Void> actualizar(PlanCuenta cuenta);

    List<PlanCuenta> listar();

    List<PlanCuenta> listarHijos(int idCuentaPadre);

    List<PlanCuenta> listarRaiz();

    PlanCuenta buscarPorId(int idCuenta);

    PlanCuenta buscarPorCodigo(String codigo);
}
