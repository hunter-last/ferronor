
package com.ferronor.sic.shared;

import com.ferronor.sic.auditoria.dao.AuditoriaDAOImpl;
import com.ferronor.sic.auditoria.logica.AuditoriaServiceImpl;
import com.ferronor.sic.seguridad.dao.*;
import com.ferronor.sic.seguridad.logica.*;
import com.ferronor.sic.maestros.dao.*;
import com.ferronor.sic.maestros.logica.*;
import com.ferronor.sic.inventario.dao.*;
import com.ferronor.sic.inventario.logica.*;

public final class ServiceFactory {

    private ServiceFactory() {}

    // Seguridad
    public static LoginService loginService() {
        return new LoginServiceImpl(new UsuarioDAOImpl(), new RolDAOImpl(), new PermisoDAOImpl(), new AuditoriaServiceImpl(new AuditoriaDAOImpl()));
    }
    public static UsuarioService usuarioService() {
        return new UsuarioServiceImpl(new UsuarioDAOImpl(), new RolDAOImpl(), new AuditoriaServiceImpl(new AuditoriaDAOImpl()));
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
}