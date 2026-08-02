package com.ferronor.sic.ventas.logica;

import com.ferronor.sic.maestros.dao.ProductoDAO;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.ventas.dao.DevolucionVentaDAO;
import com.ferronor.sic.ventas.dao.VentaDAO;
import com.ferronor.sic.ventas.modelo.DevolucionVenta;
import java.math.BigDecimal;
import java.util.List;

public class DevolucionVentaServiceImpl implements DevolucionVentaService {

    private final DevolucionVentaDAO devolucionVentaDAO;
    private final VentaDAO ventaDAO;
    private final ProductoDAO productoDAO;

    public DevolucionVentaServiceImpl(DevolucionVentaDAO devolucionVentaDAO, VentaDAO ventaDAO,
            ProductoDAO productoDAO) {
        this.devolucionVentaDAO = devolucionVentaDAO;
        this.ventaDAO = ventaDAO;
        this.productoDAO = productoDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrarDevolucion(DevolucionVenta devolucion) {
        if (devolucion == null) {
            return RespuestaOperacion.error("La devolución es obligatoria");
        }
        if (ventaDAO.buscarPorId(devolucion.getIdVenta()) == null) {
            return RespuestaOperacion.error("La venta no existe");
        }
        if (productoDAO.buscarPorId(devolucion.getIdProducto()) == null) {
            return RespuestaOperacion.error("El producto no existe");
        }
        if (devolucion.getCantidad() == null || devolucion.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            return RespuestaOperacion.error("La cantidad debe ser mayor a cero");
        }
        if (devolucion.getMotivo() == null || devolucion.getMotivo().isBlank()) {
            return RespuestaOperacion.error("El motivo de la devolución es obligatorio");
        }
        devolucionVentaDAO.insertar(devolucion);
        return RespuestaOperacion.ok();
    }

    @Override
    public List<DevolucionVenta> listarPorVenta(int idVenta) {
        return devolucionVentaDAO.listarPorVenta(idVenta);
    }
}