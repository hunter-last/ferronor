/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.dao;

import com.ferronor.sic.maestros.modelo.PlanCuenta;
import com.ferronor.sic.shared.IGeneralDAO;
import java.util.List;

public interface PlanCuentaDAO extends IGeneralDAO<PlanCuenta, Integer> {

    PlanCuenta buscarPorCodigo(String codigo);

    List<PlanCuenta> listarHijos(int idCuentaPadre);

    List<PlanCuenta> listarRaiz();
}
