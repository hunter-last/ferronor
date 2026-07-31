/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.dao;

import com.ferronor.sic.contabilidad.modelo.dto.BalanceComprobacionItem;
import java.time.LocalDate;
import java.util.List;

public interface BalanceComprobacionDAO {
    List<BalanceComprobacionItem> obtenerAgregadoPorCuenta(LocalDate hasta);
}
