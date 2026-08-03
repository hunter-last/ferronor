/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.logica;

import com.ferronor.sic.contabilidad.modelo.AsientoContable;
import com.ferronor.sic.contabilidad.modelo.dto.BalanceComprobacionItem;
import com.ferronor.sic.contabilidad.modelo.dto.DatosCobroParaAsiento;
import com.ferronor.sic.contabilidad.modelo.dto.DatosCompraParaAsiento;
import com.ferronor.sic.contabilidad.modelo.dto.DatosPagoParaAsiento;
import com.ferronor.sic.contabilidad.modelo.dto.DatosVentaParaAsiento;
import com.ferronor.sic.contabilidad.modelo.dto.LibroMayorItem;
import com.ferronor.sic.contabilidad.modelo.dto.EstadoResultadosDTO;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.time.LocalDate;
import java.util.List;

public class ContabilidadServiceImpl implements ContabilidadService {

    private final AsientoService asientoService;
    private final LibroDiarioService libroDiarioService;
    private final LibroMayorService libroMayorService;
    private final BalanceComprobacionService balanceComprobacionService;
    private final EstadoResultadosService estadoResultadosService;

    public ContabilidadServiceImpl(AsientoService asientoService, LibroDiarioService libroDiarioService,
            LibroMayorService libroMayorService, BalanceComprobacionService balanceComprobacionService,
            EstadoResultadosService estadoResultadosService) {
        this.asientoService = asientoService;
        this.libroDiarioService = libroDiarioService;
        this.libroMayorService = libroMayorService;
        this.balanceComprobacionService = balanceComprobacionService;
        this.estadoResultadosService = estadoResultadosService;
    }

    @Override
    public RespuestaOperacion<Integer> registrarAsiento(AsientoContable asiento) {
        return asientoService.registrar(asiento);
    }

    @Override
    public RespuestaOperacion<Void> anularAsiento(int idAsiento) {
        return asientoService.anular(idAsiento);
    }

    @Override
    public RespuestaOperacion<Integer> generarAsientoVenta(DatosVentaParaAsiento datos, int idUsuario) {
        return asientoService.generarAsientoVenta(datos, idUsuario);
    }

    @Override
    public RespuestaOperacion<Integer> generarAsientoCompra(DatosCompraParaAsiento datos, int idUsuario) {
        return asientoService.generarAsientoCompra(datos, idUsuario);
    }

    @Override
    public RespuestaOperacion<Integer> generarAsientoCobro(DatosCobroParaAsiento datos, int idUsuario) {
        return asientoService.generarAsientoCobro(datos, idUsuario);
    }

    @Override
    public RespuestaOperacion<Integer> generarAsientoPago(DatosPagoParaAsiento datos, int idUsuario) {
        return asientoService.generarAsientoPago(datos, idUsuario);
    }

    @Override
    public List<AsientoContable> obtenerLibroDiario(LocalDate desde, LocalDate hasta) {
        return libroDiarioService.obtenerLibroDiario(desde, hasta);
    }

    @Override
    public List<LibroMayorItem> obtenerLibroMayor(int idCuenta, LocalDate desde, LocalDate hasta) {
        return libroMayorService.obtenerLibroMayor(idCuenta, desde, hasta);
    }

    @Override
    public List<BalanceComprobacionItem> obtenerBalanceComprobacion(LocalDate hasta) {
        return balanceComprobacionService.obtenerBalance(hasta);
    }

    @Override
    public EstadoResultadosDTO obtenerEstadoResultados(LocalDate hasta) {
        return estadoResultadosService.obtenerEstadoResultados(hasta);
    }
}