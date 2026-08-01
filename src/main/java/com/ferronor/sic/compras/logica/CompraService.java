package com.ferronor.sic.compras.logica;

import com.ferronor.sic.compras.modelo.Compra;
import com.ferronor.sic.compras.modelo.CuentaPagar;
import com.ferronor.sic.compras.modelo.PagoProveedor;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public interface CompraService {

    // Registra compra + detalle_compra + cuenta_pagar (si la forma de pago es a crédito),
    // todo en una sola transacción. Punto de entrada usado por ProcesoCompra.
    RespuestaOperacion<Integer> registrarCompra(Compra compra);

    // Aplica el pago contra cuenta_pagar. Punto de entrada usado por ProcesoPagoProveedor.
    RespuestaOperacion<Void> aplicarPago(PagoProveedor pago);

    Compra buscarPorId(int idCompra);

    List<Compra> listar();

    // CompraService es dueño de cuenta_pagar (1-1 con compra): estas consultas quedan
    // aquí en vez de crear un CuentaPagarService separado solo para no repartir una
    // tabla que pertenece por completo a esta responsabilidad de negocio.
    CuentaPagar buscarCuentaPagarPorCompra(int idCompra);

    List<CuentaPagar> listarCuentasPorPagarPendientes();

    List<CuentaPagar> listarCuentasPorPagarVencidas();
}