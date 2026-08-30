package com.ferronor.sic.compras.logica;

import com.ferronor.sic.compras.modelo.EstadoOrdenCompra;
import com.ferronor.sic.compras.modelo.OrdenCompra;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public interface OrdenCompraService {

    // Registra la orden + sus detalle_orden_compra en estado PENDIENTE, una sola transacción.
    RespuestaOperacion<Integer> registrarSolicitud(OrdenCompra orden);

    // Solo transiciona órdenes PENDIENTE -> APROBADA/RECHAZADA (flujo de aprobación de Gerencia).
    RespuestaOperacion<Void> aprobar(int idOrdenCompra, int idUsuarioAprueba);

    RespuestaOperacion<Void> rechazar(int idOrdenCompra, int idUsuarioAprueba);

    OrdenCompra buscarPorId(int idOrdenCompra);

    List<OrdenCompra> listar();

    List<OrdenCompra> listarPorEstado(EstadoOrdenCompra estado);
    
    List<OrdenCompra> listarDisponiblesParaCompra();
}