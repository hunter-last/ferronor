package com.ferronor.sic.pruebas;

import com.ferronor.sic.auditoria.dao.AuditoriaDAO;
import com.ferronor.sic.auditoria.dao.AuditoriaDAOImpl;
import com.ferronor.sic.auditoria.logica.AuditoriaService;
import com.ferronor.sic.auditoria.logica.AuditoriaServiceImpl;
import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;

import com.ferronor.sic.inventario.dao.MovimientoInventarioDAO;
import com.ferronor.sic.inventario.dao.MovimientoInventarioDAOImpl;
import com.ferronor.sic.inventario.dao.StockDAO;
import com.ferronor.sic.inventario.dao.StockDAOImpl;
import com.ferronor.sic.inventario.logica.InventarioService;
import com.ferronor.sic.inventario.logica.InventarioServiceImpl;
import com.ferronor.sic.inventario.logica.KardexService;
import com.ferronor.sic.inventario.logica.KardexServiceImpl;
import com.ferronor.sic.inventario.modelo.OrigenMovimiento;
import com.ferronor.sic.inventario.modelo.Stock;
import com.ferronor.sic.inventario.modelo.TipoMovimiento;

import com.ferronor.sic.maestros.dao.CategoriaDAO;
import com.ferronor.sic.maestros.dao.CategoriaDAOImpl;
import com.ferronor.sic.maestros.dao.ProductoDAO;
import com.ferronor.sic.maestros.dao.ProductoDAOImpl;
import com.ferronor.sic.maestros.dao.UnidadMedidaDAO;
import com.ferronor.sic.maestros.dao.UnidadMedidaDAOImpl;

import com.ferronor.sic.maestros.logica.CategoriaService;
import com.ferronor.sic.maestros.logica.CategoriaServiceImpl;
import com.ferronor.sic.maestros.logica.ProductoService;
import com.ferronor.sic.maestros.logica.ProductoServiceImpl;
import com.ferronor.sic.maestros.logica.UnidadMedidaService;
import com.ferronor.sic.maestros.logica.UnidadMedidaServiceImpl;

import com.ferronor.sic.maestros.modelo.Categoria;
import com.ferronor.sic.maestros.modelo.Producto;
import com.ferronor.sic.maestros.modelo.UnidadMedida;

import com.ferronor.sic.seguridad.dao.PermisoDAO;
import com.ferronor.sic.seguridad.dao.PermisoDAOImpl;
import com.ferronor.sic.seguridad.dao.RolDAO;
import com.ferronor.sic.seguridad.dao.RolDAOImpl;
import com.ferronor.sic.seguridad.dao.RolPermisoDAO;
import com.ferronor.sic.seguridad.dao.RolPermisoDAOImpl;
import com.ferronor.sic.seguridad.dao.UsuarioDAO;
import com.ferronor.sic.seguridad.dao.UsuarioDAOImpl;
import com.ferronor.sic.seguridad.logica.UsuarioService;
import com.ferronor.sic.seguridad.logica.UsuarioServiceImpl;
import com.ferronor.sic.seguridad.modelo.Rol;
import com.ferronor.sic.seguridad.modelo.Usuario;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.math.BigDecimal;

public class MainPrueba2 {

    private static int pruebas = 0;
    private static int exitos = 0;
    private static int fallos = 0;

    public static void main(String[] args) {

        imprimirBanner();

        try (TransactionContext tx = TransactionManager.iniciar()) {

            /*======================================================
             * DAO
             ======================================================*/
            RolDAO rolDAO = new RolDAOImpl();
            PermisoDAO permisoDAO = new PermisoDAOImpl();
            RolPermisoDAO rolPermisoDAO = new RolPermisoDAOImpl();
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

            CategoriaDAO categoriaDAO = new CategoriaDAOImpl();
            UnidadMedidaDAO unidadDAO = new UnidadMedidaDAOImpl();
            ProductoDAO productoDAO = new ProductoDAOImpl();

            StockDAO stockDAO = new StockDAOImpl();
            MovimientoInventarioDAO movimientoDAO
                    = new MovimientoInventarioDAOImpl();

            /*======================================================
             * SERVICES
             ======================================================*/
            
            AuditoriaDAO auditoriaDAO = new AuditoriaDAOImpl();
            AuditoriaService auditoriaService = new AuditoriaServiceImpl(auditoriaDAO);

            UsuarioService usuarioService
                    = new UsuarioServiceImpl(
                            usuarioDAO,
                            rolDAO, auditoriaService);

            CategoriaService categoriaService
                    = new CategoriaServiceImpl(
                            categoriaDAO);

            UnidadMedidaService unidadService
                    = new UnidadMedidaServiceImpl(
                            unidadDAO);

            ProductoService productoService
                    = new ProductoServiceImpl(
                            productoDAO,
                            categoriaDAO,
                            unidadDAO);

            InventarioService inventarioService
                    = new InventarioServiceImpl(
                            stockDAO,
                            movimientoDAO,
                            productoDAO);

            KardexService kardexService
                    = new KardexServiceImpl(
                            movimientoDAO);

            /*======================================================
             * DATOS DE PRUEBA
             ======================================================*/
            Rol rol = rolDAO.buscarPorNombre("Administrador");

            if (rol == null) {
                throw new IllegalStateException(
                        "No existe el rol Administrador. Ejecuta el script de datos iniciales.");
            }

            Categoria categoria = new Categoria(
                    "Categoria Smoke " + System.currentTimeMillis());

            verificar(
                    "Registrar categoría",
                    categoriaService.registrar(categoria));

            UnidadMedida unidad = new UnidadMedida(
                    "Unidad Smoke " + System.currentTimeMillis(),
                    "UND");

            verificar(
                    "Registrar unidad",
                    unidadService.registrar(unidad));

            Producto producto = new Producto(
                    "SMK-" + System.currentTimeMillis(),
                    "Producto Smoke Test",
                    categoria.getIdCategoria(),
                    unidad.getIdUnidadMedida(),
                    new BigDecimal("10"),
                    new BigDecimal("50"));

            verificar(
                    "Registrar producto",
                    productoService.registrar(producto));

            int idProducto = producto.getIdProducto();

            Usuario admin = usuarioDAO.buscarPorLogin("admin");
            int idUsuario = admin.getIdUsuario();

            /*======================================================
             * VALIDACIÓN DEL STOCK INICIAL
             ======================================================*/
            System.out.println();
            System.out.println("========== STOCK INICIAL ==========");

            // ========= CONTINÚA EN LA PARTE 2 =========
            /*======================================================
             * PRIMERA ENTRADA DE INVENTARIO
             ======================================================*/
            System.out.println();
            System.out.println("========== PRIMERA ENTRADA ==========");

            verificar(
                    "Registrar entrada (100 @ 30.00)",
                    inventarioService.registrarEntrada(
                            idProducto,
                            new BigDecimal("100"),
                            new BigDecimal("30.00"),
                            OrigenMovimiento.COMPRA,
                            1001,
                            idUsuario));

            checkBigDecimal(
                    "Stock después de primera entrada",
                    new BigDecimal("100"),
                    inventarioService.obtenerStock(idProducto));

            checkBigDecimal(
                    "CPP después de primera entrada",
                    new BigDecimal("30.0000"),
                    inventarioService.obtenerCostoPromedioActual(idProducto));

            /*======================================================
             * SEGUNDA ENTRADA DE INVENTARIO
             ======================================================*/
            System.out.println();
            System.out.println("========== SEGUNDA ENTRADA ==========");

            verificar(
                    "Registrar entrada (50 @ 36.00)",
                    inventarioService.registrarEntrada(
                            idProducto,
                            new BigDecimal("50"),
                            new BigDecimal("36.00"),
                            OrigenMovimiento.COMPRA,
                            1002,
                            idUsuario));

            checkBigDecimal(
                    "Stock después de segunda entrada",
                    new BigDecimal("150"),
                    inventarioService.obtenerStock(idProducto));

            checkBigDecimal(
                    "CPP recalculado",
                    new BigDecimal("32.0000"),
                    inventarioService.obtenerCostoPromedioActual(idProducto));

            /*======================================================
             * MOVIMIENTOS REGISTRADOS
             ======================================================*/
            System.out.println();
            System.out.println("========== MOVIMIENTOS ==========");

            var movimientos
                    = movimientoDAO.listarPorProducto(idProducto);

            check(
                    "Existen movimientos registrados",
                    movimientos.size() == 2);

            if (!movimientos.isEmpty()) {

                var ultimo
                        = movimientos.get(movimientos.size() - 1);

                check(
                        "Último movimiento es ENTRADA",
                        ultimo.getTipo() == TipoMovimiento.ENTRADA);

                check(
                        "Origen correcto",
                        ultimo.getOrigen() == OrigenMovimiento.COMPRA);

                checkBigDecimal(
                        "Cantidad registrada",
                        new BigDecimal("50"),
                        ultimo.getCantidad());

                checkBigDecimal(
                        "Costo unitario registrado",
                        new BigDecimal("36.00"),
                        ultimo.getCostoUnitario());

                checkBigDecimal(
                        "Costo total registrado",
                        new BigDecimal("1800.00"),
                        ultimo.getCostoTotal());

                check(
                        "Documento origen correcto",
                        ultimo.getIdDocumentoOrigen() == 1002);

                check(
                        "Usuario correcto",
                        ultimo.getIdUsuario() == idUsuario);
            }

            /*======================================================
             * VALIDACIÓN DEL STOCK EN BASE DE DATOS
             ======================================================*/
            System.out.println();
            System.out.println("========== STOCK EN BD ==========");

            Stock stockActual
                    = stockDAO.buscarPorId(idProducto);

            check(
                    "Registro de stock existe",
                    stockActual != null);

            if (stockActual != null) {

                checkBigDecimal(
                        "Cantidad persistida",
                        new BigDecimal("150"),
                        stockActual.getCantidadActual());

                checkBigDecimal(
                        "CPP persistido",
                        new BigDecimal("32.0000"),
                        stockActual.getCostoPromedioActual());

                check(
                        "Fecha actualización asignada",
                        stockActual.getFechaUltimaActualizacion() != null);
            }

            // ========= CONTINÚA EN LA PARTE 3 =========
            /*======================================================
             * SALIDA DE INVENTARIO
             ======================================================*/
            System.out.println();
            System.out.println("========== SALIDA DE INVENTARIO ==========");

         /*   verificar(
                    "Registrar salida (40 unidades)",
                    inventarioService.registrarSalida(
                            idProducto,
                            new BigDecimal("40"),
                            OrigenMovimiento.VENTA,
                            2001,
                            idUsuario));

            checkBigDecimal(
                    "Stock después de salida",
                    new BigDecimal("110"),
                    inventarioService.obtenerStock(idProducto));

            checkBigDecimal(
                    "CPP después de salida",
                    new BigDecimal("32.0000"),
                    inventarioService.obtenerCostoPromedioActual(idProducto));*/

            /*======================================================
             * VALIDAR MOVIMIENTO DE SALIDA
             ======================================================*/
            System.out.println();
            System.out.println("========== MOVIMIENTO DE SALIDA ==========");

            var movimientosFinales
                    = movimientoDAO.listarPorProducto(idProducto);

            check(
                    "Existen tres movimientos",
                    movimientosFinales.size() == 3);

            if (!movimientosFinales.isEmpty()) {

                var ultimo
                        = movimientosFinales.get(movimientosFinales.size() - 1);

                check(
                        "Tipo SALIDA",
                        ultimo.getTipo() == TipoMovimiento.SALIDA);

                check(
                        "Origen VENTA",
                        ultimo.getOrigen() == OrigenMovimiento.VENTA);

                checkBigDecimal(
                        "Cantidad salida",
                        new BigDecimal("40"),
                        ultimo.getCantidad());

                checkBigDecimal(
                        "Costo unitario salida",
                        new BigDecimal("32.0000"),
                        ultimo.getCostoUnitario());

                checkBigDecimal(
                        "Costo total salida",
                        new BigDecimal("1280.0000"),
                        ultimo.getCostoTotal());
            }

            /*======================================================
             * PRUEBAS DE VALIDACIÓN
             ======================================================*/
            System.out.println();
            System.out.println("========== VALIDACIONES ==========");

            check(
                    "Rechaza salida con stock insuficiente",
                    !inventarioService.registrarSalida(
                            idProducto,
                            new BigDecimal("999999"),
                            OrigenMovimiento.VENTA,
                            3001,
                            idUsuario).isExito());

            check(
                    "Rechaza entrada con cantidad cero",
                    !inventarioService.registrarEntrada(
                            idProducto,
                            BigDecimal.ZERO,
                            new BigDecimal("30"),
                            OrigenMovimiento.COMPRA,
                            3002,
                            idUsuario).isExito());

            check(
                    "Rechaza salida con cantidad cero",
                    !inventarioService.registrarSalida(
                            idProducto,
                            BigDecimal.ZERO,
                            OrigenMovimiento.VENTA,
                            3003,
                            idUsuario).isExito());

            /*======================================================
             * KARDEX
             ======================================================*/
            System.out.println();
            System.out.println("========== KARDEX ==========");

            var kardex
                    = kardexService.obtenerKardex(
                            idProducto,
                            java.time.LocalDate.now().minusDays(1),
                            java.time.LocalDate.now().plusDays(1));

            check(
                    "Kardex generado",
                    !kardex.isEmpty());

            var ultimoKardex
                    = kardex.get(kardex.size() - 1);

            checkBigDecimal(
                    "Saldo final kardex cantidad",
                    new BigDecimal("110"),
                    ultimoKardex.getSaldoCantidad());

            System.out.printf("%-20s %-10s %-10s %-10s %-10s%n",
                    "Fecha",
                    "Tipo",
                    "Entrada",
                    "Salida",
                    "Saldo");

            for (var item : kardex) {

                System.out.printf(
                        "%-20s %-10s %-10s %-10s %-10s%n",
                        item.getFecha(),
                        item.getTipoMovimiento(),
                        item.getEntrada(),
                        item.getSalida(),
                        item.getSaldoCantidad());
            }

            /*======================================================
             * ROLLBACK
             ======================================================*/
            System.out.println();
            System.out.println("========== ROLLBACK ==========");

            tx.rollback();

            check(
                    "Rollback ejecutado",
                    true);

        } catch (Exception ex) {

            fallos++;

            System.out.println();
            System.out.println("ERROR DURANTE EL SMOKE TEST");
            ex.printStackTrace();

        } finally {

            imprimirResumen();
        }

    }

    /*======================================================
     * MÉTODOS AUXILIARES
     ======================================================*/
    private static void verificar(String prueba,
            RespuestaOperacion<Void> respuesta) {

        pruebas++;

        if (respuesta.isExito()) {

            exitos++;
            System.out.println("[ OK ] " + prueba);

        } else {

            fallos++;
            System.out.println("[FAIL] " + prueba);
            System.out.println("       " + respuesta.getMensaje());
        }
    }

    private static void check(String prueba,
            boolean condicion) {

        pruebas++;

        if (condicion) {

            exitos++;
            System.out.println("[ OK ] " + prueba);

        } else {

            fallos++;
            System.out.println("[FAIL] " + prueba);
        }
    }

    private static void checkBigDecimal(String prueba,
            BigDecimal esperado,
            BigDecimal obtenido) {

        pruebas++;

        if (esperado.compareTo(obtenido) == 0) {

            exitos++;
            System.out.println("[ OK ] " + prueba + " -> " + obtenido);

        } else {

            fallos++;

            System.out.println("[FAIL] " + prueba);
            System.out.println("       Esperado : " + esperado);
            System.out.println("       Obtenido : " + obtenido);
        }
    }

    private static void imprimirBanner() {

        System.out.println("==============================================");
        System.out.println("      FERRONOR SIC - SMOKE TEST INVENTARIO");
        System.out.println("==============================================");
        System.out.println("Todas las operaciones se ejecutarán");
        System.out.println("dentro de una única transacción.");
        System.out.println("Al finalizar se ejecutará ROLLBACK.");
        System.out.println("==============================================");
        System.out.println();
    }

    private static void imprimirResumen() {

        System.out.println();
        System.out.println("==============================================");
        System.out.println("              RESUMEN FINAL");
        System.out.println("==============================================");
        System.out.println("Pruebas : " + pruebas);
        System.out.println("Correctas : " + exitos);
        System.out.println("Fallidas : " + fallos);

        if (fallos == 0) {
            System.out.println("RESULTADO : APROBADO");
        } else {
            System.out.println("RESULTADO : FALLÓ");
        }

        System.out.println("==============================================");
    }

}
