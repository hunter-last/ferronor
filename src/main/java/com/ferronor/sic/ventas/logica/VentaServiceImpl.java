package com.ferronor.sic.ventas.logica;

import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.maestros.dao.ClienteDAO;
import com.ferronor.sic.maestros.dao.FormaPagoDAO;
import com.ferronor.sic.maestros.dao.ProductoDAO;
import com.ferronor.sic.maestros.dao.TipoComprobanteDAO;
import com.ferronor.sic.maestros.modelo.FormaPago;
import com.ferronor.sic.maestros.modelo.TipoComprobante;
import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.util.CalculadoraImpuestos;
import com.ferronor.sic.config.Constantes;
import com.ferronor.sic.exception.ServiceException;
import com.ferronor.sic.ventas.dao.ComprobanteDAO;
import com.ferronor.sic.ventas.dao.CorrelativoComprobanteDAO;
import com.ferronor.sic.ventas.dao.CuentaCobrarDAO;
import com.ferronor.sic.ventas.dao.DetalleVentaDAO;
import com.ferronor.sic.ventas.dao.VentaDAO;
import com.ferronor.sic.ventas.modelo.CobroCliente;
import com.ferronor.sic.ventas.modelo.Comprobante;
import com.ferronor.sic.ventas.modelo.CuentaCobrar;
import com.ferronor.sic.ventas.modelo.DetalleVenta;
import com.ferronor.sic.ventas.modelo.EstadoCuenta;
import com.ferronor.sic.ventas.modelo.EstadoVenta;
import com.ferronor.sic.ventas.modelo.Venta;
import com.ferronor.sic.ventas.modelo.dto.CuentaCobrarConsulta;
import com.ferronor.sic.ventas.modelo.dto.VentaConsulta;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class VentaServiceImpl implements VentaService {

    private final VentaDAO ventaDAO;
    private final DetalleVentaDAO detalleVentaDAO;
    private final ComprobanteDAO comprobanteDAO;
    private final CorrelativoComprobanteDAO correlativoComprobanteDAO;
    private final CuentaCobrarDAO cuentaCobrarDAO;
    private final ClienteDAO clienteDAO;
    private final FormaPagoDAO formaPagoDAO;
    private final TipoComprobanteDAO tipoComprobanteDAO;
    private final ProductoDAO productoDAO;

    public VentaServiceImpl(VentaDAO ventaDAO, DetalleVentaDAO detalleVentaDAO, ComprobanteDAO comprobanteDAO,
            CorrelativoComprobanteDAO correlativoComprobanteDAO, CuentaCobrarDAO cuentaCobrarDAO,
            ClienteDAO clienteDAO, FormaPagoDAO formaPagoDAO, TipoComprobanteDAO tipoComprobanteDAO,
            ProductoDAO productoDAO) {
        this.ventaDAO = ventaDAO;
        this.detalleVentaDAO = detalleVentaDAO;
        this.comprobanteDAO = comprobanteDAO;
        this.correlativoComprobanteDAO = correlativoComprobanteDAO;
        this.cuentaCobrarDAO = cuentaCobrarDAO;
        this.clienteDAO = clienteDAO;
        this.formaPagoDAO = formaPagoDAO;
        this.tipoComprobanteDAO = tipoComprobanteDAO;
        this.productoDAO = productoDAO;
    }

    @Override
    public RespuestaOperacion<Integer> registrarVenta(Venta venta, int idTipoComprobante) {
        RespuestaOperacion<FormaPago> validacion = validar(venta, idTipoComprobante);
        if (!validacion.isExito()) {
            return RespuestaOperacion.error(validacion.getMensaje());
        }
        FormaPago formaPago = validacion.getResultado();
        TipoComprobante tipoComprobante = tipoComprobanteDAO.buscarPorId(idTipoComprobante);

        calcularMontos(venta);
        venta.setEstado(formaPago.isEsCredito() ? EstadoVenta.PAGO_PENDIENTE : EstadoVenta.DESPACHADA);

        try (TransactionContext tx = TransactionManager.iniciar()) {
            ventaDAO.insertar(venta);

            for (DetalleVenta detalle : venta.getDetalles()) {
                detalle.setIdVenta(venta.getIdVenta());
                detalleVentaDAO.insertar(detalle);
            }

            int numero = correlativoComprobanteDAO.obtenerSiguienteNumero(idTipoComprobante);
            Comprobante comprobante = new Comprobante(venta.getIdVenta(), idTipoComprobante,
                    tipoComprobante.getSerie(), String.format("%06d", numero));
            comprobanteDAO.insertar(comprobante);

            if (formaPago.isEsCredito()) {
                // fecha_vencimiento es nullable en cuenta_cobrar (a diferencia de cuenta_pagar):
                // no hay un campo de plazo de crédito por cliente en el modelo actual, así
                // que se deja en null en vez de inventar una regla de días fija.
                CuentaCobrar cuentaCobrar = new CuentaCobrar(venta.getIdVenta(), venta.getTotal(), null);
                cuentaCobrarDAO.insertar(cuentaCobrar);
            }

            tx.commit();
            return RespuestaOperacion.ok(venta.getIdVenta());
        }
    }

    private RespuestaOperacion<FormaPago> validar(Venta venta, int idTipoComprobante) {
        if (venta == null) {
            return RespuestaOperacion.error("La venta es obligatoria");
        }
        if (clienteDAO.buscarPorId(venta.getIdCliente()) == null) {
            return RespuestaOperacion.error("El cliente no existe");
        }
        FormaPago formaPago = formaPagoDAO.buscarPorId(venta.getIdFormaPago());
        if (formaPago == null) {
            return RespuestaOperacion.error("La forma de pago no existe");
        }
        if (tipoComprobanteDAO.buscarPorId(idTipoComprobante) == null) {
            return RespuestaOperacion.error("El tipo de comprobante no existe");
        }
        if (venta.getDetalles().isEmpty()) {
            return RespuestaOperacion.error("La venta debe tener al menos un producto");
        }
        for (DetalleVenta detalle : venta.getDetalles()) {
            if (productoDAO.buscarPorId(detalle.getIdProducto()) == null) {
                return RespuestaOperacion.error("El producto " + detalle.getIdProducto() + " no existe");
            }
            if (detalle.getCantidad() == null || detalle.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                return RespuestaOperacion.error("La cantidad del producto " + detalle.getIdProducto()
                        + " debe ser mayor a cero");
            }
            if (detalle.getPrecioUnitario() == null || detalle.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) {
                return RespuestaOperacion.error("El precio unitario del producto " + detalle.getIdProducto()
                        + " es inválido");
            }
        }
        return RespuestaOperacion.ok(formaPago);
    }

    // Decisión de diseño confirmada: el IGV se calcula UNA sola vez sobre el total
    // acumulado de la venta (detalle.subtotal ya incluye IGV), nunca línea por línea.
    private void calcularMontos(Venta venta) {
        BigDecimal totalConIgv = BigDecimal.ZERO;
        for (DetalleVenta detalle : venta.getDetalles()) {
            totalConIgv = totalConIgv.add(detalle.getSubtotal());
        }
        totalConIgv = totalConIgv.setScale(Constantes.ESCALA_MONEDA, Constantes.REDONDEO);

        venta.setTotal(totalConIgv);
        venta.setSubtotal(CalculadoraImpuestos.calcularValorVenta(totalConIgv));
        venta.setIgv(CalculadoraImpuestos.calcularIGV(totalConIgv));
    }

    @Override
    public RespuestaOperacion<Void> aplicarCobro(CobroCliente cobro) {
        if (cobro == null || cobro.getMonto() == null || cobro.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            return RespuestaOperacion.error("El monto del cobro debe ser mayor a cero");
        }
        CuentaCobrar cuentaCobrar = cuentaCobrarDAO.buscarPorVenta(cobro.getIdVenta());
        if (cuentaCobrar == null) {
            return RespuestaOperacion.error("La venta " + cobro.getIdVenta() + " no tiene cuenta por cobrar");
        }
        if (cuentaCobrar.getEstado() == EstadoCuenta.PAGADA) {
            return RespuestaOperacion.error("La cuenta por cobrar ya está saldada");
        }
        BigDecimal nuevoMontoCobrado = cuentaCobrar.getMontoCobrado().add(cobro.getMonto());
        if (nuevoMontoCobrado.compareTo(cuentaCobrar.getMontoTotal()) > 0) {
            return RespuestaOperacion.error("El monto del cobro excede el saldo pendiente");
        }
        BigDecimal nuevoSaldo = cuentaCobrar.getMontoTotal().subtract(nuevoMontoCobrado);
        boolean saldada = nuevoSaldo.compareTo(BigDecimal.ZERO) == 0;
        EstadoCuenta nuevoEstado = saldada ? EstadoCuenta.PAGADA : cuentaCobrar.getEstado();

        try (TransactionContext tx = TransactionManager.iniciar()) {
            cuentaCobrarDAO.registrarCobro(cuentaCobrar.getIdCuentaCobrar(), nuevoMontoCobrado, nuevoSaldo,
                    nuevoEstado);
            if (saldada) {
                ventaDAO.cambiarEstado(cobro.getIdVenta(), EstadoVenta.DESPACHADA);
            }
            tx.commit();
            return RespuestaOperacion.ok();
        }
    }

    @Override
    public Venta buscarPorId(int idVenta) {
        Venta venta = ventaDAO.buscarPorId(idVenta);
        if (venta != null) {
            cargarDetalles(venta);
        }
        return venta;
    }

    @Override
    public List<Venta> listar() {
        List<Venta> ventas = ventaDAO.listar();
        ventas.forEach(this::cargarDetalles);
        return ventas;
    }

    @Override
    public List<VentaConsulta> consultarHistorial(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Integer idCliente,
            EstadoVenta estado,
            Integer idTipoComprobante) {

        if (fechaDesde != null
                && fechaHasta != null
                && fechaDesde.isAfter(fechaHasta)) {

            throw new ServiceException(
                    "La fecha desde no puede ser posterior a la fecha hasta"
            );
        }

        return ventaDAO.consultarHistorial(
                fechaDesde,
                fechaHasta,
                idCliente,
                estado,
                idTipoComprobante
        );
    }

    @Override
    public List<CuentaCobrarConsulta> consultarCuentasPorCobrar(EstadoCuenta estado, Integer idCliente,
            LocalDate fechaDesde, LocalDate fechaHasta) {

        return cuentaCobrarDAO.consultar(estado, idCliente, fechaDesde, fechaHasta);
    }

    private void cargarDetalles(Venta venta) {
        for (DetalleVenta detalle : detalleVentaDAO.listarPorVenta(venta.getIdVenta())) {
            venta.agregarDetalle(detalle);
        }
    }

    @Override
    public CuentaCobrar buscarCuentaCobrarPorVenta(int idVenta) {
        return cuentaCobrarDAO.buscarPorVenta(idVenta);
    }

    @Override
    public Comprobante buscarComprobantePorVenta(int idVenta) {
        return comprobanteDAO.buscarPorVenta(idVenta);
    }

    @Override
    public List<CuentaCobrar> listarCuentasPorCobrarPendientes() {
        return cuentaCobrarDAO.listarPorEstado(EstadoCuenta.PENDIENTE);
    }

    @Override
    public List<CuentaCobrar> listarCuentasPorCobrarVencidas() {
        return cuentaCobrarDAO.listarPorEstado(EstadoCuenta.VENCIDA);
    }
}
