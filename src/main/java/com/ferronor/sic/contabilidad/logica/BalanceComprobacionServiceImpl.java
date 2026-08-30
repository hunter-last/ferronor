/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.logica;

import com.ferronor.sic.contabilidad.dao.BalanceComprobacionDAO;
import com.ferronor.sic.contabilidad.modelo.dto.BalanceComprobacionItem;
import java.time.LocalDate;
import java.util.List;

public class BalanceComprobacionServiceImpl implements BalanceComprobacionService {

    private final BalanceComprobacionDAO balanceComprobacionDAO;

    public BalanceComprobacionServiceImpl(BalanceComprobacionDAO balanceComprobacionDAO) {
        this.balanceComprobacionDAO = balanceComprobacionDAO;
    }

    @Override
    public List<BalanceComprobacionItem> obtenerBalance(LocalDate hasta) {
        return balanceComprobacionDAO.obtenerAgregadoPorCuenta(hasta);
    }
}
