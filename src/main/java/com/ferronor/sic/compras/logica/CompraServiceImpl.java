package com.ferronor.sic.compras.logica;

import com.ferronor.sic.compras.dao.CompraDAO;
import com.ferronor.sic.compras.dao.CuentaPagarDAO;
import com.ferronor.sic.compras.dao.DetalleCompraDAO;
import com.ferronor.sic.compras.dao.OrdenCompraDAO;
import com.ferronor.sic.compras.modelo.Compra;
import com.ferronor.sic.compras.modelo.CuentaPagar;
import com.ferronor.sic.compras.modelo.DetalleCompra;
import com.ferronor.sic.compras.modelo.EstadoCuenta;
import com.ferronor.sic.compras.modelo.EstadoOrdenCompra;
import com.ferronor.sic.compras.modelo.OrdenCompra;
import com.ferronor.sic.compras.modelo.PagoProveedor;
import com.ferronor.sic.compras.modelo.dto.CuentaPagarConsulta;
import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.config.Constantes;
import com.ferronor.sic.maestros.dao.FormaPagoDAO;
import com.ferronor.sic.maestros.dao.ProductoDAO;
import com.ferronor.sic.maestros.dao.ProveedorDAO;
import com.ferronor.sic.maestros.modelo.FormaPago;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.util.CalculadoraImpuestos;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CompraServiceImpl implements CompraService {

    private final CompraDAO compraDAO;
    private final DetalleCompraDAO detalleCompraDAO;
    private final CuentaPagarDAO cuentaPagarDAO;
    private final ProveedorDAO proveedorDAO;
    private final ProductoDAO productoDAO;
    private final FormaPagoDAO formaPagoDAO;
    private final OrdenCompraDAO ordenCompraDAO;

    public CompraServiceImpl(CompraDAO compraDAO, DetalleCompraDAO detalleCompraDAO, CuentaPagarDAO cuentaPagarDAO,
            ProveedorDAO proveedorDAO, ProductoDAO productoDAO, FormaPagoDAO formaPagoDAO,
            OrdenCompraDAO ordenCompraDAO) {
        this.compraDAO = compraDAO;
        this.detalleCompraDAO = detalleCompraDAO;
        this.cuentaPagarDAO = cuentaPagarDAO;
        this.proveedorDAO = proveedorDAO;
        this.productoDAO = productoDAO;
        this.formaPagoDAO = formaPagoDAO;
        this.ordenCompraDAO = ordenCompraDAO;
    }

    @Override
    public RespuestaOperacion<Integer> registrarCompra(Compra compra) {
        RespuestaOperacion<FormaPago> validacion = validar(compra);
        if (!validacion.isExito()) {
            return RespuestaOperacion.error(validacion.getMensaje());
        }
        FormaPago formaPago = validacion.getResultado();

        if (formaPago.isEsCredito()) {
            if (compra.getPlazoDias() == null || compra.getPlazoDias() <= 0) {
                return RespuestaOperacion.error("El plazo en días es obligatorio para compras a crédito");
            }
        } else {
            compra.setPlazoDias(null);
        }

        calcularMontos(compra);

        try (TransactionContext tx = TransactionManager.iniciar()) {
            compraDAO.insertar(compra);

            for (DetalleCompra detalle : compra.getDetalles()) {
                detalle.setIdCompra(compra.getIdCompra());
                detalleCompraDAO.insertar(detalle);
            }

            if (formaPago.isEsCredito()) {
                java.time.LocalDate fechaVencimiento = compra.getFecha().toLocalDate().plusDays(compra.getPlazoDias());
                CuentaPagar cuentaPagar = new CuentaPagar(compra.getIdCompra(), compra.getTotal(), fechaVencimiento);
                cuentaPagarDAO.insertar(cuentaPagar);
            }

            tx.commit();
            return RespuestaOperacion.ok(compra.getIdCompra());
        }
    }

    private RespuestaOperacion<FormaPago> validar(Compra compra) {
        if (compra == null) {
            return RespuestaOperacion.error("La compra es obligatoria");
        }
        if (proveedorDAO.buscarPorId(compra.getIdProveedor()) == null) {
            return RespuestaOperacion.error("El proveedor no existe");
        }
        FormaPago formaPago = formaPagoDAO.buscarPorId(compra.getIdFormaPago());
        if (formaPago == null) {
            return RespuestaOperacion.error("La forma de pago no existe");
        }
        if (compra.getNumeroFactura() == null || compra.getNumeroFactura().isBlank()) {
            return RespuestaOperacion.error("El número de factura es obligatorio");
        }
        if (compraDAO.buscarPorNumeroFactura(compra.getIdProveedor(), compra.getNumeroFactura()) != null) {
            return RespuestaOperacion.error("Ya existe una compra con ese número de factura para este proveedor");
        }
        if (compra.getIdOrdenCompra() != null) {

            int idOrdenCompra = compra.getIdOrdenCompra();

            OrdenCompra orden
                    = ordenCompraDAO.buscarPorId(idOrdenCompra);

            if (orden == null) {
                return RespuestaOperacion.error(
                        "La orden de compra referenciada no existe"
                );
            }

            if (orden.getEstado() != EstadoOrdenCompra.APROBADA) {
                return RespuestaOperacion.error(
                        "La orden de compra referenciada no está aprobada"
                );
            }

            Compra compraExistente
                    = compraDAO.buscarPorOrdenCompra(idOrdenCompra);

            if (compraExistente != null) {
                return RespuestaOperacion.error(
                        "La orden de compra ya fue utilizada en la compra "
                        + compraExistente.getIdCompra()
                );
            }
        }
        if (compra.getDetalles().isEmpty()) {
            return RespuestaOperacion.error("La compra debe tener al menos un producto");
        }
        for (DetalleCompra detalle : compra.getDetalles()) {
            if (productoDAO.buscarPorId(detalle.getIdProducto()) == null) {
                return RespuestaOperacion.error("El producto " + detalle.getIdProducto() + " no existe");
            }
            if (detalle.getCantidad() == null || detalle.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                return RespuestaOperacion.error("La cantidad del producto " + detalle.getIdProducto()
                        + " debe ser mayor a cero");
            }
            if (detalle.getCostoUnitario() == null || detalle.getCostoUnitario().compareTo(BigDecimal.ZERO) < 0) {
                return RespuestaOperacion.error("El costo unitario del producto " + detalle.getIdProducto()
                        + " es inválido");
            }
        }
        return RespuestaOperacion.ok(formaPago);
    }

    private void calcularMontos(Compra compra) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleCompra detalle : compra.getDetalles()) {
            subtotal = subtotal.add(detalle.getSubtotal());
        }
        subtotal = subtotal.setScale(Constantes.ESCALA_MONEDA, Constantes.REDONDEO);
        compra.setSubtotal(subtotal);
        compra.setIgv(CalculadoraImpuestos.calcularIGVDesdeSubtotal(subtotal));
        compra.setTotal(CalculadoraImpuestos.calcularTotalConIgv(subtotal));
    }

    @Override
    public RespuestaOperacion<Void> aplicarPago(PagoProveedor pago) {
        if (pago == null || pago.getMonto() == null || pago.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            return RespuestaOperacion.error("El monto del pago debe ser mayor a cero");
        }
        CuentaPagar cuentaPagar = cuentaPagarDAO.buscarPorCompra(pago.getIdCompra());
        if (cuentaPagar == null) {
            return RespuestaOperacion.error("La compra " + pago.getIdCompra() + " no tiene cuenta por pagar");
        }
        if (cuentaPagar.getEstado() == EstadoCuenta.PAGADA) {
            return RespuestaOperacion.error("La cuenta por pagar ya está saldada");
        }
        BigDecimal nuevoMontoPagado = cuentaPagar.getMontoPagado().add(pago.getMonto());
        if (nuevoMontoPagado.compareTo(cuentaPagar.getMontoTotal()) > 0) {
            return RespuestaOperacion.error("El monto del pago excede el saldo pendiente");
        }
        BigDecimal nuevoSaldo = cuentaPagar.getMontoTotal().subtract(nuevoMontoPagado);
        EstadoCuenta nuevoEstado = nuevoSaldo.compareTo(BigDecimal.ZERO) == 0
                ? EstadoCuenta.PAGADA : cuentaPagar.getEstado();

        cuentaPagarDAO.registrarPago(cuentaPagar.getIdCuentaPagar(), nuevoMontoPagado, nuevoSaldo, nuevoEstado);
        return RespuestaOperacion.ok();
    }

    @Override
    public Compra buscarPorId(int idCompra) {
        Compra compra = compraDAO.buscarPorId(idCompra);
        if (compra != null) {
            cargarDetalles(compra);
        }
        return compra;
    }

    @Override
    public List<Compra> listar() {
        List<Compra> compras = compraDAO.listar();
        compras.forEach(this::cargarDetalles);
        return compras;
    }

    private void cargarDetalles(Compra compra) {
        for (DetalleCompra detalle : detalleCompraDAO.listarPorCompra(compra.getIdCompra())) {
            compra.agregarDetalle(detalle);
        }
    }

    @Override
    public CuentaPagar buscarCuentaPagarPorCompra(int idCompra) {
        return cuentaPagarDAO.buscarPorCompra(idCompra);
    }

    @Override
    public List<CuentaPagar> listarCuentasPorPagarPendientes() {
        return cuentaPagarDAO.listarPorEstado(EstadoCuenta.PENDIENTE);
    }

    @Override
    public List<CuentaPagar> listarCuentasPorPagarVencidas() {
        return cuentaPagarDAO.listarPorEstado(EstadoCuenta.VENCIDA);
    }

    @Override
    public List<CuentaPagarConsulta> consultarCuentasPorPagar(EstadoCuenta estado, Integer idProveedor,
            LocalDate fechaDesde, LocalDate fechaHasta) {
        return cuentaPagarDAO.consultar(estado, idProveedor, fechaDesde, fechaHasta);
    }
}
