/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.dao;

import com.ferronor.sic.maestros.modelo.UnidadMedida;
import com.ferronor.sic.shared.IGeneralDAO;

public interface UnidadMedidaDAO extends IGeneralDAO<UnidadMedida, Integer> {

    UnidadMedida buscarPorNombre(String nombre);

    UnidadMedida buscarPorAbreviatura(String abreviatura);

}
