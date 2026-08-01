package com.ferronor.sic.compras.logica;

import com.ferronor.sic.compras.modelo.DevolucionCompra;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public interface DevolucionCompraService {

    // No toca Stock: el ajuste de inventario asociado lo coordina un Proceso*
    // futuro (fuera de alcance de este bloque).
    RespuestaOperacion<Void> registrarDevolucion(DevolucionCompra devolucion);

    List<DevolucionCompra> listarPorCompra(int idCompra);
}