package com.ferronor.sic.ventas.logica;

import com.ferronor.sic.maestros.dao.ProductoDAO;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.ventas.dao.DetalleVentaDAO;
import com.ferronor.sic.ventas.dao.DevolucionVentaDAO;
import com.ferronor.sic.ventas.dao.VentaDAO;
import com.ferronor.sic.ventas.modelo.DetalleVenta;
import com.ferronor.sic.ventas.modelo.DevolucionVenta;
import java.math.BigDecimal;
import java.util.List;

public class DevolucionVentaServiceImpl implements DevolucionVentaService {

    private final DevolucionVentaDAO devolucionVentaDAO;
    private final VentaDAO ventaDAO;
    private final ProductoDAO productoDAO;
    private final DetalleVentaDAO detalleVentaDAO;

    public DevolucionVentaServiceImpl(DevolucionVentaDAO devolucionVentaDAO, VentaDAO ventaDAO,
            ProductoDAO productoDAO, DetalleVentaDAO detalleVentaDAO) {
        this.devolucionVentaDAO = devolucionVentaDAO;
        this.ventaDAO = ventaDAO;
        this.productoDAO = productoDAO;
        this.detalleVentaDAO = detalleVentaDAO;
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

        BigDecimal cantidadVendida = detalleVentaDAO.listarPorVenta(devolucion.getIdVenta()).stream()
                .filter(d -> d.getIdProducto() == devolucion.getIdProducto())
                .map(DetalleVenta::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cantidadYaDevuelta = devolucionVentaDAO.listarPorVenta(devolucion.getIdVenta()).stream()
                .filter(d -> d.getIdProducto() == devolucion.getIdProducto())
                .map(DevolucionVenta::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (cantidadYaDevuelta.add(devolucion.getCantidad()).compareTo(cantidadVendida) > 0) {
            return RespuestaOperacion.error("La cantidad a devolver excede lo vendido. Vendido: "
                    + cantidadVendida + ", ya devuelto: " + cantidadYaDevuelta);
        }

        devolucionVentaDAO.insertar(devolucion);
        return RespuestaOperacion.ok();
    }

    @Override
    public List<DevolucionVenta> listarPorVenta(int idVenta) {
        return devolucionVentaDAO.listarPorVenta(idVenta);
    }
}
