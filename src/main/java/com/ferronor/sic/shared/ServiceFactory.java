package com.ferronor.sic.shared;

import com.ferronor.sic.seguridad.dao.*;
import com.ferronor.sic.seguridad.logica.*;
import com.ferronor.sic.maestros.dao.*;
import com.ferronor.sic.maestros.logica.*;
import com.ferronor.sic.inventario.dao.*;
import com.ferronor.sic.inventario.logica.*;
import com.ferronor.sic.compras.dao.*;
import com.ferronor.sic.compras.logica.*;
import com.ferronor.sic.tesoreria.dao.*;
import com.ferronor.sic.tesoreria.logica.*;
import com.ferronor.sic.ventas.dao.*;
import com.ferronor.sic.ventas.logica.*;

public final class ServiceFactory {

    private ServiceFactory() {}

    // Seguridad
    public static LoginService loginService() {
        return new LoginServiceImpl(new UsuarioDAOImpl(), new RolDAOImpl(), new PermisoDAOImpl());
    }
    public static UsuarioService usuarioService() {
        return new UsuarioServiceImpl(new UsuarioDAOImpl(), new RolDAOImpl());
    }
    public static RolService rolService() {
        return new RolServiceImpl(new RolDAOImpl(), new RolPermisoDAOImpl(), new PermisoDAOImpl());
    }
    public static PermisoService permisoService() {
        return new PermisoServiceImpl(new PermisoDAOImpl());
    }

    // Maestros
    public static CategoriaService categoriaService() {
        return new CategoriaServiceImpl(new CategoriaDAOImpl());
    }
    public static UnidadMedidaService unidadMedidaService() {
        return new UnidadMedidaServiceImpl(new UnidadMedidaDAOImpl());
    }
    public static FormaPagoService formaPagoService() {
        return new FormaPagoServiceImpl(new FormaPagoDAOImpl());
    }
    public static TipoComprobanteService tipoComprobanteService() {
        return new TipoComprobanteServiceImpl(new TipoComprobanteDAOImpl());
    }
    public static ProveedorService proveedorService() {
        return new ProveedorServiceImpl(new ProveedorDAOImpl());
    }
    public static ClienteService clienteService() {
        return new ClienteServiceImpl(new ClienteDAOImpl());
    }
    public static ProductoService productoService() {
        return new ProductoServiceImpl(new ProductoDAOImpl(), new CategoriaDAOImpl(), new UnidadMedidaDAOImpl());
    }
    public static PlanCuentaService planCuentaService() {
        return new PlanCuentaServiceImpl(new PlanCuentaDAOImpl());
    }

    // Inventario
    public static InventarioService inventarioService() {
        return new InventarioServiceImpl(new StockDAOImpl(), new MovimientoInventarioDAOImpl(), new ProductoDAOImpl());
    }
    public static AjusteInventarioService ajusteInventarioService() {
        return new AjusteInventarioServiceImpl(new StockDAOImpl(), new MovimientoInventarioDAOImpl(),
                new AjusteInventarioDAOImpl(), new ProductoDAOImpl());
    }
    public static KardexService kardexService() {
        return new KardexServiceImpl(new MovimientoInventarioDAOImpl());
    }

    // Compras
    public static OrdenCompraService ordenCompraService() {
        return new OrdenCompraServiceImpl(new OrdenCompraDAOImpl(), new DetalleOrdenCompraDAOImpl(),
                new ProveedorDAOImpl(), new ProductoDAOImpl());
    }

    public static CompraService compraService() {
        return new CompraServiceImpl(new CompraDAOImpl(), new DetalleCompraDAOImpl(), new CuentaPagarDAOImpl(),
                new ProveedorDAOImpl(), new ProductoDAOImpl(), new FormaPagoDAOImpl(), new OrdenCompraDAOImpl());
    }

    public static DevolucionCompraService devolucionCompraService() {
        return new DevolucionCompraServiceImpl(new DevolucionCompraDAOImpl(), new CompraDAOImpl(),
                new ProductoDAOImpl());
    }

    // Tesorería: CajaService/BancoService quedan internos, no se exponen aquí.
    public static TesoreriaService tesoreriaService() {
        CajaService cajaService = new CajaServiceImpl(new CajaDAOImpl(), new MovimientoCajaDAOImpl(),
                new CierreCajaDAOImpl());
        BancoService bancoService = new BancoServiceImpl(new CuentaBancariaDAOImpl(), new MovimientoBancoDAOImpl());
        return new TesoreriaServiceImpl(cajaService, bancoService);
    }

    // Ventas
    public static VentaService ventaService() {
        return new VentaServiceImpl(new VentaDAOImpl(), new DetalleVentaDAOImpl(), new ComprobanteDAOImpl(),
                new CorrelativoComprobanteDAOImpl(), new CuentaCobrarDAOImpl(), new ClienteDAOImpl(),
                new FormaPagoDAOImpl(), new TipoComprobanteDAOImpl(), new ProductoDAOImpl());
    }

    public static DevolucionVentaService devolucionVentaService() {
        return new DevolucionVentaServiceImpl(new DevolucionVentaDAOImpl(), new VentaDAOImpl(),
                new ProductoDAOImpl());
    }
}