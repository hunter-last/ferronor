
package com.ferronor.sic.contabilidad.logica;

import com.ferronor.sic.contabilidad.modelo.dto.BalanceComprobacionItem;
import com.ferronor.sic.contabilidad.modelo.dto.BalanceGeneralDTO;
import com.ferronor.sic.contabilidad.modelo.dto.BalanceGeneralItem;
import com.ferronor.sic.contabilidad.modelo.dto.EstadoResultadosDTO;
import com.ferronor.sic.exception.ServiceException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// El sistema no ejecuta asiento de cierre contable automático (sin cuenta 59,
// sin reapertura de ejercicios) — por eso el resultado del ejercicio se
// incorpora al Patrimonio como una línea calculada, no persistida.
public class BalanceGeneralServiceImpl implements BalanceGeneralService {

    private final BalanceComprobacionService balanceComprobacionService;
    private final EstadoResultadosService estadoResultadosService;

    public BalanceGeneralServiceImpl(BalanceComprobacionService balanceComprobacionService,
            EstadoResultadosService estadoResultadosService) {
        this.balanceComprobacionService = balanceComprobacionService;
        this.estadoResultadosService = estadoResultadosService;
    }

    @Override
    public BalanceGeneralDTO obtenerBalanceGeneral(LocalDate fechaCorte) {
        List<BalanceComprobacionItem> cuentas = balanceComprobacionService.obtenerBalance(fechaCorte);
        BalanceGeneralDTO balance = new BalanceGeneralDTO(fechaCorte);

        for (BalanceComprobacionItem cuenta : cuentas) {
            char elemento = cuenta.getCodigo().charAt(0);
            switch (elemento) {
                case '1':
                case '2': {
                    BigDecimal saldo = cuenta.getSaldoDeudor().subtract(cuenta.getSaldoAcreedor());
                    balance.agregarActivo(new BalanceGeneralItem(cuenta.getCodigo(), cuenta.getNombreCuenta(), saldo));
                    break;
                }
                case '4': {
                    BigDecimal saldo = cuenta.getSaldoAcreedor().subtract(cuenta.getSaldoDeudor());
                    balance.agregarPasivo(new BalanceGeneralItem(cuenta.getCodigo(), cuenta.getNombreCuenta(), saldo));
                    break;
                }
                case '5': {
                    BigDecimal saldo = cuenta.getSaldoAcreedor().subtract(cuenta.getSaldoDeudor());
                    balance.agregarPatrimonio(new BalanceGeneralItem(cuenta.getCodigo(), cuenta.getNombreCuenta(), saldo));
                    break;
                }
                default:
                // elementos 6 y 7 (gestión) no van al Balance General
            }
        }

        EstadoResultadosDTO resultado = estadoResultadosService.obtenerEstadoResultados(fechaCorte);
        balance.agregarPatrimonio(new BalanceGeneralItem("RESULTADO", "Resultado del ejercicio",
                resultado.getUtilidadNeta()));

        BigDecimal diferencia = balance.getTotalActivo()
                .subtract(balance.getTotalPasivo().add(balance.getTotalPatrimonio()));
        if (diferencia.compareTo(BigDecimal.ZERO) != 0) {
            throw new ServiceException("El Balance General no cuadra. Diferencia: " + diferencia);
        }

        return balance;
    }
}
