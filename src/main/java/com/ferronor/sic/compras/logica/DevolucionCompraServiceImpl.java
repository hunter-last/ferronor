package com.ferronor.sic.compras.logica;

import com.ferronor.sic.compras.dao.CompraDAO;
import com.ferronor.sic.compras.dao.DetalleCompraDAO;
import com.ferronor.sic.compras.dao.DevolucionCompraDAO;
import com.ferronor.sic.compras.modelo.DetalleCompra;
import com.ferronor.sic.compras.modelo.DevolucionCompra;
import com.ferronor.sic.maestros.dao.ProductoDAO;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.math.BigDecimal;
import java.util.List;

public class DevolucionCompraServiceImpl implements DevolucionCompraService {

    private final DevolucionCompraDAO devolucionCompraDAO;
    private final CompraDAO compraDAO;
    private final ProductoDAO productoDAO;
    private final DetalleCompraDAO detalleCompraDAO;

    public DevolucionCompraServiceImpl(DevolucionCompraDAO devolucionCompraDAO, CompraDAO compraDAO,
            ProductoDAO productoDAO, DetalleCompraDAO detalleCompraDAO) {
        this.devolucionCompraDAO = devolucionCompraDAO;
        this.compraDAO = compraDAO;
        this.productoDAO = productoDAO;
        this.detalleCompraDAO = detalleCompraDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrarDevolucion(DevolucionCompra devolucion) {
        if (devolucion == null) {
            return RespuestaOperacion.error("La devolución es obligatoria");
        }
        if (compraDAO.buscarPorId(devolucion.getIdCompra()) == null) {
            return RespuestaOperacion.error("La compra no existe");
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

        BigDecimal cantidadComprada = detalleCompraDAO.listarPorCompra(devolucion.getIdCompra()).stream()
                .filter(d -> d.getIdProducto() == devolucion.getIdProducto())
                .map(DetalleCompra::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cantidadYaDevuelta = devolucionCompraDAO.listarPorCompra(devolucion.getIdCompra()).stream()
                .filter(d -> d.getIdProducto() == devolucion.getIdProducto())
                .map(DevolucionCompra::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (cantidadYaDevuelta.add(devolucion.getCantidad()).compareTo(cantidadComprada) > 0) {
            return RespuestaOperacion.error("La cantidad a devolver excede lo comprado. Comprado: "
                    + cantidadComprada + ", ya devuelto: " + cantidadYaDevuelta);
        }

        devolucionCompraDAO.insertar(devolucion);
        return RespuestaOperacion.ok();
    }

    @Override
    public List<DevolucionCompra> listarPorCompra(int idCompra) {
        return devolucionCompraDAO.listarPorCompra(idCompra);
    }
}