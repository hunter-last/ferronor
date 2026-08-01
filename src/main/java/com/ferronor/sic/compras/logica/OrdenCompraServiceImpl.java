package com.ferronor.sic.compras.logica;

import com.ferronor.sic.compras.dao.DetalleOrdenCompraDAO;
import com.ferronor.sic.compras.dao.OrdenCompraDAO;
import com.ferronor.sic.compras.modelo.DetalleOrdenCompra;
import com.ferronor.sic.compras.modelo.EstadoOrdenCompra;
import com.ferronor.sic.compras.modelo.OrdenCompra;
import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.maestros.dao.ProductoDAO;
import com.ferronor.sic.maestros.dao.ProveedorDAO;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.math.BigDecimal;
import java.util.List;

public class OrdenCompraServiceImpl implements OrdenCompraService {

    private final OrdenCompraDAO ordenCompraDAO;
    private final DetalleOrdenCompraDAO detalleOrdenCompraDAO;
    private final ProveedorDAO proveedorDAO;
    private final ProductoDAO productoDAO;

    public OrdenCompraServiceImpl(OrdenCompraDAO ordenCompraDAO, DetalleOrdenCompraDAO detalleOrdenCompraDAO,
            ProveedorDAO proveedorDAO, ProductoDAO productoDAO) {
        this.ordenCompraDAO = ordenCompraDAO;
        this.detalleOrdenCompraDAO = detalleOrdenCompraDAO;
        this.proveedorDAO = proveedorDAO;
        this.productoDAO = productoDAO;
    }

    @Override
    public RespuestaOperacion<Integer> registrarSolicitud(OrdenCompra orden) {
        if (orden == null) {
            return RespuestaOperacion.error("La orden de compra es obligatoria");
        }
        if (proveedorDAO.buscarPorId(orden.getIdProveedor()) == null) {
            return RespuestaOperacion.error("El proveedor no existe");
        }
        if (orden.getDetalles().isEmpty()) {
            return RespuestaOperacion.error("La orden de compra debe tener al menos un producto");
        }
        for (DetalleOrdenCompra detalle : orden.getDetalles()) {
            if (productoDAO.buscarPorId(detalle.getIdProducto()) == null) {
                return RespuestaOperacion.error("El producto " + detalle.getIdProducto() + " no existe");
            }
            if (detalle.getCantidad() == null || detalle.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                return RespuestaOperacion.error("La cantidad del producto " + detalle.getIdProducto()
                        + " debe ser mayor a cero");
            }
        }

        try (TransactionContext tx = TransactionManager.iniciar()) {
            ordenCompraDAO.insertar(orden);
            for (DetalleOrdenCompra detalle : orden.getDetalles()) {
                detalle.setIdOrdenCompra(orden.getIdOrdenCompra());
                detalleOrdenCompraDAO.insertar(detalle);
            }
            tx.commit();
            return RespuestaOperacion.ok(orden.getIdOrdenCompra());
        }
    }

    @Override
    public RespuestaOperacion<Void> aprobar(int idOrdenCompra, int idUsuarioAprueba) {
        return cambiarEstado(idOrdenCompra, idUsuarioAprueba, EstadoOrdenCompra.APROBADA);
    }

    @Override
    public RespuestaOperacion<Void> rechazar(int idOrdenCompra, int idUsuarioAprueba) {
        return cambiarEstado(idOrdenCompra, idUsuarioAprueba, EstadoOrdenCompra.RECHAZADA);
    }

    private RespuestaOperacion<Void> cambiarEstado(int idOrdenCompra, int idUsuarioAprueba, EstadoOrdenCompra destino) {
        OrdenCompra orden = ordenCompraDAO.buscarPorId(idOrdenCompra);
        if (orden == null) {
            return RespuestaOperacion.error("La orden de compra no existe");
        }
        if (orden.getEstado() != EstadoOrdenCompra.PENDIENTE) {
            return RespuestaOperacion.error("La orden de compra ya fue procesada (" + orden.getEstado() + ")");
        }
        ordenCompraDAO.cambiarEstado(idOrdenCompra, destino, idUsuarioAprueba);
        return RespuestaOperacion.ok();
    }

    @Override
    public OrdenCompra buscarPorId(int idOrdenCompra) {
        OrdenCompra orden = ordenCompraDAO.buscarPorId(idOrdenCompra);
        if (orden != null) {
            cargarDetalles(orden);
        }
        return orden;
    }

    @Override
    public List<OrdenCompra> listar() {
        List<OrdenCompra> ordenes = ordenCompraDAO.listar();
        ordenes.forEach(this::cargarDetalles);
        return ordenes;
    }

    @Override
    public List<OrdenCompra> listarPorEstado(EstadoOrdenCompra estado) {
        List<OrdenCompra> ordenes = ordenCompraDAO.listarPorEstado(estado);
        ordenes.forEach(this::cargarDetalles);
        return ordenes;
    }

    private void cargarDetalles(OrdenCompra orden) {
        for (DetalleOrdenCompra detalle : detalleOrdenCompraDAO.listarPorOrdenCompra(orden.getIdOrdenCompra())) {
            orden.agregarDetalle(detalle);
        }
    }
}