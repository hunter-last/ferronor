package com.ferronor.sic.ventas.logica;

import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.ventas.modelo.DevolucionVenta;
import java.util.List;

public interface DevolucionVentaService {

    // No toca Stock ni Tesorería: la integración con Inventario (entrada de producto
    // devuelto) y, si corresponde, la devolución de dinero, quedan fuera de alcance
    // de este Service — corresponden a un futuro ProcesoDevolucionVenta.
    RespuestaOperacion<Void> registrarDevolucion(DevolucionVenta devolucion);

    List<DevolucionVenta> listarPorVenta(int idVenta);
}