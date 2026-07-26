package com.ferronor.sic.pruebas;

import com.ferronor.sic.conexion.TransactionContext;
import com.ferronor.sic.conexion.TransactionManager;

import com.ferronor.sic.seguridad.dao.PermisoDAOImpl;
import com.ferronor.sic.seguridad.dao.RolDAOImpl;
import com.ferronor.sic.seguridad.dao.RolPermisoDAOImpl;
import com.ferronor.sic.seguridad.dao.UsuarioDAOImpl;

import com.ferronor.sic.seguridad.logica.LoginServiceImpl;
import com.ferronor.sic.seguridad.logica.PermisoServiceImpl;
import com.ferronor.sic.seguridad.logica.RolServiceImpl;
import com.ferronor.sic.seguridad.logica.UsuarioServiceImpl;

import com.ferronor.sic.seguridad.modelo.Permiso;
import com.ferronor.sic.seguridad.modelo.Rol;
import com.ferronor.sic.seguridad.modelo.Usuario;

import com.ferronor.sic.shared.RespuestaOperacion;
import com.ferronor.sic.shared.SesionUsuario;

import java.util.List;

public class MainPruebaSeguridad {

    private static int pruebas = 0;
    private static int correctas = 0;
    private static int fallidas = 0;

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println("      FERRONOR SIC - SMOKE TEST SEGURIDAD");
        System.out.println("==============================================");
        System.out.println("Todas las operaciones se ejecutarán");
        System.out.println("dentro de una única transacción.");
        System.out.println("Al finalizar se ejecutará ROLLBACK.");
        System.out.println("==============================================\n");

        RolDAOImpl rolDAO = new RolDAOImpl();
        PermisoDAOImpl permisoDAO = new PermisoDAOImpl();
        RolPermisoDAOImpl rolPermisoDAO = new RolPermisoDAOImpl();
        UsuarioDAOImpl usuarioDAO = new UsuarioDAOImpl();

        RolServiceImpl rolService = new RolServiceImpl(
                rolDAO,
                rolPermisoDAO,
                permisoDAO
        );

        PermisoServiceImpl permisoService = new PermisoServiceImpl(
                permisoDAO
        );

        UsuarioServiceImpl usuarioService = new UsuarioServiceImpl(
                usuarioDAO,
                rolDAO
        );

        LoginServiceImpl loginService = new LoginServiceImpl(
                usuarioDAO,
                rolDAO,
                permisoDAO
        );

        try (TransactionContext tx = TransactionManager.iniciar()) {

            RespuestaOperacion<Void> loginAdmin
                    = loginService.iniciarSesion("admin", "12345678");

            verificar(
                    "Login administrador inicial",
                    loginAdmin.isExito()
            );

            // =====================================================
// PARTE 2 - ROLES, PERMISOS Y ASIGNACIONES
// =====================================================
// ROLES
// =====================================================
            System.out.println("\n========== ROLES ==========");

            Rol rolAdmin = rolDAO.buscarPorNombre("Administrador");

            if (rolAdmin == null) {

                rolAdmin = new Rol("Administrador");

                RespuestaOperacion<Void> r
                        = rolService.registrar(rolAdmin);

                verificar(
                        "Registrar rol administrador",
                        r.isExito()
                );

            } else {

                verificar(
                        "Obtener rol administrador existente",
                        true
                );
            }

            int idRolAdmin = rolAdmin.getIdRol();

            System.out.println(
                    "ID Rol administrador: " + idRolAdmin
            );

            System.out.println("ID Rol creado: " + rolAdmin.getIdRol());

// =====================================================
// PERMISOS
// =====================================================
            System.out.println("\n========== PERMISOS ==========");

            Permiso permisoCrearProducto
                    = new Permiso(
                            "PRODUCTO_CREAR",
                            "Crear productos"
                    );

            Permiso permisoEditarProducto
                    = new Permiso(
                            "PRODUCTO_EDITAR",
                            "Editar productos"
                    );

            Permiso permisoRegistrarVenta
                    = new Permiso(
                            "VENTA_REGISTRAR",
                            "Registrar ventas"
                    );

            RespuestaOperacion<Void> r1
                    = permisoService.registrar(permisoCrearProducto);

            RespuestaOperacion<Void> r2
                    = permisoService.registrar(permisoEditarProducto);

            RespuestaOperacion<Void> r3
                    = permisoService.registrar(permisoRegistrarVenta);

            verificar(
                    "Registrar permiso PRODUCTO_CREAR",
                    r1.isExito()
            );

            verificar(
                    "Registrar permiso PRODUCTO_EDITAR",
                    r2.isExito()
            );

            verificar(
                    "Registrar permiso VENTA_REGISTRAR",
                    r3.isExito()
            );

            System.out.println(
                    "Permisos creados: "
                    + permisoCrearProducto.getIdPermiso()
                    + ", "
                    + permisoEditarProducto.getIdPermiso()
                    + ", "
                    + permisoRegistrarVenta.getIdPermiso()
            );

// =====================================================
// ASIGNAR PERMISOS AL ROL
// =====================================================
            System.out.println("\n========== ASIGNACIONES ==========");

            RespuestaOperacion<Void> a1
                    = rolService.asignarPermiso(
                            rolAdmin.getIdRol(),
                            permisoCrearProducto.getIdPermiso()
                    );

            RespuestaOperacion<Void> a2
                    = rolService.asignarPermiso(
                            rolAdmin.getIdRol(),
                            permisoEditarProducto.getIdPermiso()
                    );

            RespuestaOperacion<Void> a3
                    = rolService.asignarPermiso(
                            rolAdmin.getIdRol(),
                            permisoRegistrarVenta.getIdPermiso()
                    );

            verificar(
                    "Asignar permiso PRODUCTO_CREAR",
                    a1.isExito()
            );

            verificar(
                    "Asignar permiso PRODUCTO_EDITAR",
                    a2.isExito()
            );

            verificar(
                    "Asignar permiso VENTA_REGISTRAR",
                    a3.isExito()
            );

// =====================================================
// VERIFICAR PERMISOS DEL ROL
// =====================================================
            List<Permiso> permisosRol
                    = rolService.obtenerPermisos(idRolAdmin);

            verificar(
                    "Rol contiene PRODUCTO_CREAR",
                    permisosRol.stream()
                            .anyMatch(p -> p.getCodigo().equals("PRODUCTO_CREAR"))
            );

            verificar(
                    "Rol contiene PRODUCTO_EDITAR",
                    permisosRol.stream()
                            .anyMatch(p -> p.getCodigo().equals("PRODUCTO_EDITAR"))
            );

            verificar(
                    "Rol contiene VENTA_REGISTRAR",
                    permisosRol.stream()
                            .anyMatch(p -> p.getCodigo().equals("VENTA_REGISTRAR"))
            );

            System.out.println(
                    "Cantidad permisos del rol: "
                    + permisosRol.size()
            );

            // =====================================================
// PARTE 3 - USUARIO, LOGIN Y SESIÓN
// =====================================================
            System.out.println("\n========== USUARIO ==========");

// Crear usuario administrador
            Usuario usuarioAdmin = new Usuario(
                    "Juan",
                    "Perez",
                    "jperez",
                    "",
                    rolAdmin.getIdRol()
            );

            RespuestaOperacion<Void> resultadoUsuario = usuarioService.registrar(usuarioAdmin, "12345678");
            System.out.println("Resultado usuario: " + resultadoUsuario.getMensaje()); // agrega esta línea
            verificar("Registrar usuario administrador", resultadoUsuario.isExito());

            System.out.println(
                    "ID Usuario creado: "
                    + usuarioAdmin.getIdUsuario()
            );

// =====================================================
// BUSCAR USUARIO
// =====================================================
            Usuario usuarioEncontrado
                    = usuarioService.buscarPorLogin("jperez");

            verificar(
                    "Buscar usuario por login",
                    usuarioEncontrado != null
            );

// =====================================================
// LOGIN CORRECTO
// =====================================================
            System.out.println("\n========== LOGIN ==========");

            RespuestaOperacion<Void> loginCorrecto
                    = loginService.iniciarSesion(
                            "jperez",
                            "12345678"
                    );

            verificar(
                    "Login con contraseña correcta",
                    loginCorrecto.isExito()
            );

// =====================================================
// VALIDAR SESIÓN
// =====================================================
            verificar(
                    "Sesión creada correctamente",
                    SesionUsuario.haySesion()
            );

            if (SesionUsuario.haySesion()) {

                System.out.println(
                        "Usuario sesión: "
                        + SesionUsuario.actual().getNombreCompleto()
                );

                System.out.println(
                        "Rol sesión: "
                        + SesionUsuario.actual().getNombreRol()
                );

            }

            verificar("Sesión contiene permisos", SesionUsuario.haySesion() && !SesionUsuario.actual().getPermisos().isEmpty());
            verificar("Tiene permiso PRODUCTO_CREAR", SesionUsuario.haySesion() && SesionUsuario.actual().tienePermiso("PRODUCTO_CREAR"));

// =====================================================
// LOGIN INCORRECTO
// =====================================================
            RespuestaOperacion<Void> loginIncorrecto
                    = loginService.iniciarSesion(
                            "jperez",
                            "claveIncorrecta"
                    );

            verificar(
                    "Rechaza contraseña incorrecta",
                    !loginIncorrecto.isExito()
            );

// =====================================================
// CAMBIO DE PASSWORD
// =====================================================
            System.out.println("\n========== CAMBIO PASSWORD ==========");

            RespuestaOperacion<Void> cambioPassword
                    = usuarioService.cambiarPassword(
                            usuarioAdmin.getIdUsuario(),
                            "87654321"
                    );

            verificar(
                    "Cambiar contraseña usuario",
                    cambioPassword.isExito()
            );

// Cerrar sesión actual
            loginService.cerrarSesion();

            verificar(
                    "Cerrar sesión",
                    !SesionUsuario.haySesion()
            );

// =====================================================
// LOGIN CON NUEVA PASSWORD
// =====================================================
            RespuestaOperacion<Void> nuevoLogin
                    = loginService.iniciarSesion(
                            "jperez",
                            "87654321"
                    );

            verificar(
                    "Login con nueva contraseña",
                    nuevoLogin.isExito()
            );

            // =====================================================
// PARTE 4 - ESTADOS, REVOCACIÓN Y FINALIZACIÓN
// =====================================================
            System.out.println("\n========== ESTADO USUARIO ==========");

            RespuestaOperacion<Void> desactivar = usuarioService.desactivar(usuarioAdmin.getIdUsuario());
            verificar("Desactivar usuario", desactivar.isExito());

// No cerramos sesión aquí — un intento de login fallido no sobreescribe la sesión activa
            RespuestaOperacion<Void> loginUsuarioInactivo = loginService.iniciarSesion("jperez", "87654321");
            verificar("Rechazar login usuario desactivado", !loginUsuarioInactivo.isExito());

// Reutilizamos la sesión admin, que sigue activa
            RespuestaOperacion<Void> activar = usuarioService.activar(usuarioAdmin.getIdUsuario());
            verificar("Activar usuario", activar.isExito());

// Recién ahora cerramos sesión y probamos el login ya reactivado
            loginService.cerrarSesion();
            RespuestaOperacion<Void> loginDespuesActivar = loginService.iniciarSesion("jperez", "87654321");
            verificar("Login después de activar usuario", loginDespuesActivar.isExito());

// =====================================================
// REVOCAR PERMISO
// =====================================================
            System.out.println("\n========== REVOCACIÓN PERMISOS ==========");

            RespuestaOperacion<Void> revocado = rolService.revocarPermiso(idRolAdmin, permisoEditarProducto.getIdPermiso());
            verificar("Revocar permiso PRODUCTO_EDITAR", revocado.isExito());

            List<Permiso> permisosActuales = rolService.obtenerPermisos(idRolAdmin);
            verificar("Permiso PRODUCTO_EDITAR eliminado", permisosActuales.stream().noneMatch(p -> p.getCodigo().equals("PRODUCTO_EDITAR")));

// verificar que ya no existe
            List<Permiso> permisosDespuesRevocar
                    = rolService.obtenerPermisos(
                            rolAdmin.getIdRol()
                    );

            verificar(
                    "Permiso eliminado del rol",
                    permisosDespuesRevocar.stream()
                            .noneMatch(p -> p.getCodigo().equals("PRODUCTO_EDITAR"))
            );

            System.out.println(
                    "Permisos actuales del rol: "
                    + permisosDespuesRevocar.size()
            );

// =====================================================
// CIERRE SESIÓN
// =====================================================
            loginService.cerrarSesion();

            verificar(
                    "Cerrar sesión final",
                    !SesionUsuario.haySesion()
            );

// =====================================================
// ROLLBACK
// =====================================================
            System.out.println("\n========== ROLLBACK ==========");

            tx.rollback();

            System.out.println(
                    "[ OK ] Rollback ejecutado"
            );
            /*
             Aquí continuarán las pruebas:
             
             PARTE 2:
             - Registrar rol
             - Registrar permisos
             - Asignar permisos
             
             PARTE 3:
             - Registrar usuario
             - Login
             - Sesión
             - Cambio contraseña
             
             PARTE 4:
             - Validaciones
             - Rollback
             - Resumen
             */

            tx.rollback();

        } catch (Exception e) {

            System.out.println("\nERROR DURANTE EL SMOKE TEST");
            e.printStackTrace();

        }

        System.out.println("\n==============================================");
        System.out.println("              RESUMEN FINAL");
        System.out.println("==============================================");
        System.out.println("Pruebas : " + pruebas);
        System.out.println("Correctas : " + correctas);
        System.out.println("Fallidas : " + fallidas);

        if (fallidas == 0) {
            System.out.println("RESULTADO : APROBADO");
        } else {
            System.out.println("RESULTADO : FALLÓ");
        }

        System.out.println("==============================================");

    }

    private static void verificar(String nombre, boolean resultado) {

        pruebas++;

        if (resultado) {
            correctas++;
            System.out.println("[ OK ] " + nombre);
        } else {
            fallidas++;
            System.out.println("[FAIL] " + nombre);
        }

    }

}
