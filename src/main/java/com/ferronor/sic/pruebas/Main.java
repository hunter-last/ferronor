package com.ferronor.sic.pruebas;

import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.inventario.dao.*;
import com.ferronor.sic.inventario.logica.*;
import com.ferronor.sic.inventario.modelo.*;
import com.ferronor.sic.inventario.modelo.dto.KardexItem;
import com.ferronor.sic.maestros.dao.*;
import com.ferronor.sic.maestros.logica.*;
import com.ferronor.sic.maestros.modelo.*;
import com.ferronor.sic.seguridad.dao.*;
import com.ferronor.sic.seguridad.logica.*;
import com.ferronor.sic.seguridad.modelo.*;
import com.ferronor.sic.shared.RespuestaOperacion;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Main {

    private static int fallos = 0;

    public static void main(String[] args) {
        System.out.println("=== SMOKE TEST FERRONOR SIC (sin efectos persistentes: rollback al final) ===");

        // Todo el test corre dentro de una sola transacción — todos los DAO la comparten automáticamente
        try (TransactionContext tx = TransactionManager.iniciar()) {

            RolDAO rolDAO = new RolDAOImpl();
            PermisoDAO permisoDAO = new PermisoDAOImpl();
            RolPermisoDAO rolPermisoDAO = new RolPermisoDAOImpl();
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
            RolService rolService = new RolServiceImpl(rolDAO, rolPermisoDAO, permisoDAO);
            UsuarioService usuarioService = new UsuarioServiceImpl(usuarioDAO, rolDAO);

            CategoriaDAO categoriaDAO = new CategoriaDAOImpl();
            UnidadMedidaDAO unidadDAO = new UnidadMedidaDAOImpl();
            ProductoDAO productoDAO = new ProductoDAOImpl();
            StockDAO stockDAO = new StockDAOImpl();
            MovimientoInventarioDAO movimientoDAO = new MovimientoInventarioDAOImpl();
            CategoriaService categoriaService = new CategoriaServiceImpl(categoriaDAO);
            UnidadMedidaService unidadService = new UnidadMedidaServiceImpl(unidadDAO);
            ProductoService productoService = new ProductoServiceImpl(productoDAO, categoriaDAO, unidadDAO);
            InventarioService inventarioService = new InventarioServiceImpl(stockDAO, movimientoDAO, productoDAO);
            KardexService kardexService = new KardexServiceImpl(movimientoDAO);

            Rol rolAdmin = rolDAO.buscarPorNombre("Administrador");
            if (rolAdmin == null) {
                fallo("No se encontró el rol 'Administrador' — ¿corriste 10_datos_iniciales.sql?");
                return;
            }

            Usuario admin = usuarioDAO.buscarPorLogin("admin");
            int idUsuario = admin.getIdUsuario();

            Categoria categoria = new Categoria("Cerámicos TEST " + System.currentTimeMillis());
            verificar("Crear categoría", categoriaService.registrar(categoria));

            UnidadMedida unidad = new UnidadMedida("Unidad TEST " + System.currentTimeMillis(), "und");
            verificar("Crear unidad de medida", unidadService.registrar(unidad));

            Producto producto = new Producto("TEST-" + System.currentTimeMillis(), "Producto de prueba",
                    categoria.getIdCategoria(), unidad.getIdUnidadMedida(),
                    new BigDecimal("10"), new BigDecimal("50.00"));
            verificar("Crear producto", productoService.registrar(producto));

            verificar("Entrada de inventario (100 @ 30.00)", inventarioService.registrarEntrada(
                    producto.getIdProducto(), new BigDecimal("100"), new BigDecimal("30.00"),
                    OrigenMovimiento.COMPRA, 999, idUsuario));
            System.out.println("  Stock: " + inventarioService.obtenerStock(producto.getIdProducto())
                    + " | CPP: " + inventarioService.obtenerCostoPromedioActual(producto.getIdProducto()));

            verificar("Segunda entrada (50 @ 36.00)", inventarioService.registrarEntrada(
                    producto.getIdProducto(), new BigDecimal("50"), new BigDecimal("36.00"),
                    OrigenMovimiento.COMPRA, 998, idUsuario));
            BigDecimal cppEsperado = new BigDecimal("32.0000");
            BigDecimal cppReal = inventarioService.obtenerCostoPromedioActual(producto.getIdProducto());
            System.out.println("  CPP esperado=32.00, real=" + cppReal + (cppReal.compareTo(cppEsperado) == 0 ? "  OK" : "  ERROR"));

            verificar("Salida (40 unidades)", inventarioService.registrarSalida(
                    producto.getIdProducto(), new BigDecimal("40"), OrigenMovimiento.VENTA, 997, idUsuario));
            BigDecimal stockEsperado = new BigDecimal("110");
            BigDecimal stockReal = inventarioService.obtenerStock(producto.getIdProducto());
            System.out.println("  Stock esperado=110, real=" + stockReal + (stockReal.compareTo(stockEsperado) == 0 ? "  OK" : "  ERROR"));

            System.out.println("--- Kardex ---");
            for (KardexItem item : kardexService.obtenerKardex(producto.getIdProducto(),
                    LocalDate.now().minusDays(1), LocalDate.now().plusDays(1))) {
                System.out.printf("  %s | %s | entrada=%s salida=%s saldo=%s%n",
                        item.getFecha(), item.getTipoMovimiento(), item.getEntrada(), item.getSalida(), item.getSaldoCantidad());
            }

            RespuestaOperacion<Void> rFallido = inventarioService.registrarSalida(
                    producto.getIdProducto(), new BigDecimal("99999"), OrigenMovimiento.VENTA, 996, idUsuario);
            boolean rechazoCorrecto = !rFallido.isExito();
            System.out.println("Venta imposible correctamente rechazada: " + rechazoCorrecto + " -> " + rFallido.getMensaje());
            BigDecimal stockTrasFallo = inventarioService.obtenerStock(producto.getIdProducto());
            System.out.println("  Stock tras intento fallido (debe seguir en 110): " + stockTrasFallo
                    + (stockTrasFallo.compareTo(stockEsperado) == 0 ? "  OK" : "  ERROR"));

            tx.rollback(); // SIEMPRE, para no dejar datos de prueba en la BD real
            System.out.println("=== " + (fallos == 0 ? "TODOS LOS PASOS OK" : fallos + " PASO(S) FALLARON") + " (rollback aplicado, BD sin cambios) ===");
        }
    }

    private static void verificar(String paso, RespuestaOperacion<Void> r) {
        System.out.println(paso + ": " + (r.isExito() ? "OK" : "FALLÓ -> " + r.getMensaje()));
        if (!r.isExito()) {
            fallos++;
        }
    }

    private static void fallo(String mensaje) {
        System.out.println("ERROR: " + mensaje);
        fallos++;
    }
}
