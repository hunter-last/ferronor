/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.logica;

import com.ferronor.sic.contabilidad.modelo.dto.BalanceComprobacionItem;
import com.ferronor.sic.contabilidad.modelo.dto.EstadoResultadosDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public EstadoResultadosDTO obtenerEstadoResultados(
            LocalDate desde,
            LocalDate hasta) {

        if (desde == null) {
            throw new IllegalArgumentException(
                    "La fecha desde es obligatoria"
            );
        }

        if (hasta == null) {
            throw new IllegalArgumentException(
                    "La fecha hasta es obligatoria"
            );
        }

        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "La fecha desde no puede ser posterior a la fecha hasta"
            );
        }

        /*
         * Balance acumulado hasta el final del período.
         */
        List<BalanceComprobacionItem> balanceHasta
                = balanceComprobacionService.obtenerBalance(
                        hasta
                );

        /*
         * Balance acumulado hasta el día anterior
         * al inicio del período.
         *
         * Ejemplo:
         *
         * Período: 01/08/2026 - 31/08/2026
         *
         * Se obtiene:
         *
         * acumulado al 31/08
         * -
         * acumulado al 31/07
         */
        LocalDate fechaAnterior
                = desde.minusDays(1);

        List<BalanceComprobacionItem> balanceAnterior
                = balanceComprobacionService.obtenerBalance(
                        fechaAnterior
                );

        /*
         * Indexamos el acumulado anterior por código.
         */
        Map<String, BalanceComprobacionItem> mapaAnterior
                = new HashMap<>();

        for (BalanceComprobacionItem item
                : balanceAnterior) {

            if (item == null
                    || item.getCodigo() == null) {

                continue;
            }

            mapaAnterior.put(
                    item.getCodigo(),
                    item
            );
        }

        BigDecimal totalIngresos
                = BigDecimal.ZERO;

        BigDecimal totalGastos
                = BigDecimal.ZERO;

        for (BalanceComprobacionItem actual
                : balanceHasta) {

            if (actual == null
                    || actual.getCodigo() == null) {

                continue;
            }

            BalanceComprobacionItem anterior
                    = mapaAnterior.get(
                            actual.getCodigo()
                    );

            BigDecimal debeActual
                    = valorMonetario(
                            actual.getTotalDebe()
                    );

            BigDecimal haberActual
                    = valorMonetario(
                            actual.getTotalHaber()
                    );

            BigDecimal debeAnterior
                    = anterior == null
                            ? BigDecimal.ZERO
                            : valorMonetario(
                                    anterior.getTotalDebe()
                            );

            BigDecimal haberAnterior
                    = anterior == null
                            ? BigDecimal.ZERO
                            : valorMonetario(
                                    anterior.getTotalHaber()
                            );

            /*
             * Movimientos correspondientes únicamente
             * al período solicitado.
             */
            BigDecimal debePeriodo
                    = debeActual.subtract(
                            debeAnterior
                    );

            BigDecimal haberPeriodo
                    = haberActual.subtract(
                            haberAnterior
                    );

            /*
             * Cuentas de ingresos:
             * catálogo actual de Ferronor → código 7xxx
             *
             * Naturaleza acreedora.
             */
            if (actual.getCodigo().startsWith("7")) {

                BigDecimal saldoAcreedorPeriodo
                        = haberPeriodo.subtract(
                                debePeriodo
                        );

                if (saldoAcreedorPeriodo.compareTo(
                        BigDecimal.ZERO
                ) > 0) {

                    totalIngresos
                            = totalIngresos.add(
                                    saldoAcreedorPeriodo
                            );
                }
            }

            /*
             * Cuentas de gastos:
             * catálogo actual de Ferronor → código 6xxx
             *
             * Naturaleza deudora.
             */
            if (actual.getCodigo().startsWith("6")) {

                BigDecimal saldoDeudorPeriodo
                        = debePeriodo.subtract(
                                haberPeriodo
                        );

                if (saldoDeudorPeriodo.compareTo(
                        BigDecimal.ZERO
                ) > 0) {

                    totalGastos
                            = totalGastos.add(
                                    saldoDeudorPeriodo
                            );
                }
            }
        }

        return new EstadoResultadosDTO(
                totalIngresos,
                totalGastos
        );
    }

    private EstadoResultadosDTO construirEstadoResultados(
            List<BalanceComprobacionItem> balance) {

        BigDecimal totalIngresos
                = BigDecimal.ZERO;

        BigDecimal totalGastos
                = BigDecimal.ZERO;

        if (balance != null) {

            for (BalanceComprobacionItem item
                    : balance) {

                if (item == null
                        || item.getCodigo() == null) {

                    continue;
                }

                if (item.getCodigo().startsWith("7")) {

                    totalIngresos
                            = totalIngresos.add(
                                    valorMonetario(
                                            item.getSaldoAcreedor()
                                    )
                            );
                }

                if (item.getCodigo().startsWith("6")) {

                    totalGastos
                            = totalGastos.add(
                                    valorMonetario(
                                            item.getSaldoDeudor()
                                    )
                            );
                }
            }
        }

        return new EstadoResultadosDTO(
                totalIngresos,
                totalGastos
        );
    }

    private BigDecimal valorMonetario(
            BigDecimal valor) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }
}
