/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.logica;

import com.ferronor.sic.contabilidad.modelo.dto.BalanceComprobacionItem;
import com.ferronor.sic.contabilidad.modelo.dto.EstadoResultadosDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class EstadoResultadosServiceImpl implements EstadoResultadosService {

    private final BalanceComprobacionService balanceComprobacionService;

    public EstadoResultadosServiceImpl(BalanceComprobacionService balanceComprobacionService) {
        this.balanceComprobacionService = balanceComprobacionService;
    }

    @Override
    public EstadoResultadosDTO obtenerEstadoResultados(LocalDate hasta) {
        List<BalanceComprobacionItem> balance = balanceComprobacionService.obtenerBalance(hasta);
        
        // NOTA: "7"/"6" como prefijo de Ingresos/Gastos es válido para el catálogo actual
        // de Ferronor (plan_cuenta sembrado en 10_datos_iniciales.sql), no una regla
        // universal del PCGE. Si el catálogo de cuentas cambia, este filtro debe revisarse.
        BigDecimal totalIngresos = balance.stream()
                .filter(i -> i.getCodigo().startsWith("7"))
                .map(BalanceComprobacionItem::getSaldoAcreedor) // Ingresos: naturaleza acreedora
                .reduce(BigDecimal.ZERO, BigDecimal::add);

       

        BigDecimal totalGastos = balance.stream()
                .filter(i -> i.getCodigo().startsWith("6"))
                .map(BalanceComprobacionItem::getSaldoDeudor) // Gastos: naturaleza deudora
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new EstadoResultadosDTO(totalIngresos, totalGastos);
    }
}
