\# Sistema de Gestión Comercial y Contable — Decor Home Ferronor



Sistema de escritorio (Java Swing + JDBC + PostgreSQL) desarrollado para Decor Home

Ferronor S.A.C., como proyecto conjunto de los cursos de Ingeniería de Software y

Sistemas de Información Contable — UNPRG, semestre 2026-I.



\## Stack técnico



\- \*\*Lenguaje:\*\* Java 23

\- \*\*UI:\*\* Java Swing

\- \*\*Base de datos:\*\* PostgreSQL 15+

\- \*\*Acceso a datos:\*\* JDBC puro (sin ORM), patrón DAO

\- \*\*Build:\*\* Maven



\## Requisitos previos



\- JDK 23

\- PostgreSQL instalado y corriendo localmente

\- Maven (o usar el wrapper de NetBeans)



\## Cómo levantar la base de datos



1\. Crea la base ejecutando los scripts en `database/` \*\*en este orden exacto\*\*:



01\_seguridad.sql

02\_maestros.sql

03\_inventario.sql

04\_compras.sql

05\_ventas.sql

06\_tesoreria.sql

07\_contabilidad.sql

08\_auditoria.sql

09\_indices.sql

10\_datos\_iniciales.sql



2\. Verifica que la base `dbferronor` quedó creada con las tablas de los 8 módulos.



\## Configuración de credenciales



\*\*Nunca subas tus credenciales reales al repositorio.\*\*



1\. Copia `src/main/resources/config.properties.example` a

&#x20;  `src/main/resources/config.properties`.

2\. Completa `config.properties` con tu usuario y contraseña reales de PostgreSQL.

3\. Este archivo está en `.gitignore` — no se sube nunca.



\## Cómo correr el proyecto



Desde NetBeans: abre el proyecto Maven y ejecuta `Main.java`.



Desde línea de comandos:





\## Smoke test



`src/main/java/com/ferronor/sic/pruebas/Main.java` contiene una prueba de humo que

ejercita el flujo completo (Seguridad → Maestros → Inventario) sin dejar datos

persistidos (usa una transacción con rollback automático al final). Ejecútalo

después de levantar la base para confirmar que todo está conectado correctamente:



\## Estructura del proyecto





com.ferronor.sic/

├── config/ — configuración y constantes del sistema

├── conexion/ — conexión a PostgreSQL y manejo de transacciones

├── shared/  — clases transversales (RespuestaOperacion, SesionUsuario, ServiceFactory, FrmBase, etc.)

├── util/ — utilidades (cálculos de IGV, CPP, validaciones)

├── exception/ — excepciones propias del sistema

├── seguridad/ — usuarios, roles, permisos, login

├── auditoria/ — registro de acciones del sistema

├── maestros/ — catálogos base (productos, clientes, proveedores, etc.)

├── inventario/ — control de stock, movimientos, Kardex

├── compras/ — gestión de compras a proveedores

├── ventas/ — gestión de ventas a clientes

├── tesoreria/ — caja y bancos

├── contabilidad/ — asientos contables y reportes financieros

└── procesos/ — coordinadores de operaciones que cruzan varios módulos





Cada módulo de negocio se organiza por \*\*dominio primero\*\*, no por capa global:

modulo/

├── modelo/

│ └── dto/ (solo si el módulo tiene datos derivados, ej. inventario/modelo/dto/KardexItem)

├── dao/

├── logica/

└── vista/



\## DETALLE DE LA ESTRUCTURA





com.ferronor.sic

│

├── Main.java

│

├── config/

│   ├── Configuracion.java          → lee config.properties, falla si falta una clave (sin defaults silenciosos)

│   └── Constantes.java             → IGV (18%), escala moneda, redondeo

│

├── conexion/

│   └── TransactionManager.java     → incluye TransactionContext (AutoCloseable, ThreadLocal<Connection>, rollback automático en close())

│

├── shared/

│   ├── AbstractDAO.java

│   ├── IGeneralDAO.java            → contrato CRUD sin eliminar (filosofía anular/desactivar)

│   ├── IHistoricoDAO.java          → para tablas solo-insertan (movimiento\_inventario, ajuste\_inventario)

│   ├── SesionUsuario.java

│   ├── RespuestaOperacion.java

│   ├── ResultadoBusqueda.java

│   └── Paginacion.java

│

├── util/

│   ├── Validaciones.java           → requerido() con y sin longitud máxima

│   ├── CalculadoraImpuestos.java   → IGV centralizado (Regla 13)

│   ├── CalculadoraCPP.java         → costo promedio ponderado centralizado (Regla 13)

│   ├── Fechas.java

│   ├── Numeros.java

│   ├── Mensajes.java

│   ├── ExportadorPDF.java

│   ├── ExportadorExcel.java

│   └── Utilidades.java

│

├── exception/

│   ├── DaoException.java

│   └── ServiceException.java       (BusinessException fue descartada, no se usa)

│

├── auditoria/                      ── CERRADO — módulo transversal, independiente de seguridad

│   ├── modelo/

│   │   └── Auditoria.java

│   ├── dao/

│   │   └── AuditoriaDAO.java

│   └── logica/

│       └── AuditoriaService.java

│

├── seguridad/                      ── CERRADO

│   ├── modelo/

│   │   ├── Usuario.java

│   │   ├── Rol.java

│   │   ├── Permiso.java

│   │   └── RolPermiso.java         → PK compuesta, verbos de dominio (no CRUD genérico)

│   ├── dao/

│   │   ├── UsuarioDAO.java

│   │   ├── RolDAO.java

│   │   ├── PermisoDAO.java

│   │   └── RolPermisoDAO.java      → asignar()/revocar()

│   ├── logica/

│   │   ├── LoginService.java       → BCrypt, mensaje de error genérico

│   │   ├── UsuarioService.java     → registrar()/actualizar()/cambiarPassword() separados

│   │   └── RolService.java         → integra asignarPermiso/revocarPermiso/obtenerPermisos

│   └── vista/

│       ├── FrmLogin.java

│       ├── FrmUsuarios.java

│       ├── FrmRoles.java

│       └── FrmPermisos.java

│

├── maestros/                       ── CERRADO

│   ├── modelo/

│   │   ├── Cliente.java

│   │   ├── Proveedor.java

│   │   ├── Producto.java

│   │   ├── Categoria.java

│   │   ├── UnidadMedida.java

│   │   ├── FormaPago.java

│   │   ├── TipoComprobante.java

│   │   └── PlanCuenta.java         → jerarquía autorreferenciada, valida PCGE completo

│   ├── dao/

│   │   ├── ClienteDAO.java

│   │   ├── ProveedorDAO.java

│   │   ├── ProductoDAO.java

│   │   ├── CategoriaDAO.java

│   │   ├── UnidadMedidaDAO.java

│   │   ├── FormaPagoDAO.java

│   │   ├── TipoComprobanteDAO.java

│   │   └── PlanCuentaDAO.java

│   ├── logica/

│   │   ├── ClienteService.java

│   │   ├── ProveedorService.java

│   │   ├── ProductoService.java

│   │   ├── CategoriaService.java

│   │   ├── UnidadMedidaService.java

│   │   ├── FormaPagoService.java

│   │   ├── TipoComprobanteService.java

│   │   └── PlanCuentaService.java

│   └── vista/

│       ├── FrmClientes.java

│       ├── FrmProveedores.java

│       ├── FrmProductos.java

│       ├── FrmCategorias.java

│       ├── FrmUnidadMedida.java

│       ├── FrmFormaPago.java

│       ├── FrmTipoComprobante.java

│       └── FrmPlanCuentas.java

│

├── inventario/                     ── CERRADO (módulo de referencia oficial)

│   ├── modelo/

│   │   ├── Stock.java

│   │   ├── MovimientoInventario.java

│   │   ├── AjusteInventario.java

│   │   └── dto/

│   │       └── KardexItem.java     → derivado, no tabla persistente

│   ├── dao/

│   │   ├── StockDAO.java

│   │   ├── MovimientoInventarioDAO.java   → incluye vincularDocumentoOrigen()

│   │   └── AjusteInventarioDAO.java       (sin KardexDAO — KardexService usa MovimientoInventarioDAO.listarHastaFecha())

│   ├── logica/

│   │   ├── InventarioService.java

│   │   ├── AjusteInventarioService.java

│   │   └── KardexService.java

│   └── vista/

│       ├── FrmStock.java

│       ├── FrmMovimientos.java

│       ├── FrmKardex.java

│       └── FrmAjusteInventario.java

│

├── compras/                        ── PENDIENTE (Rafael)

│   ├── modelo/

│   │   ├── Compra.java

│   │   ├── DetalleCompra.java

│   │   ├── OrdenCompra.java

│   │   ├── DetalleOrdenCompra.java

│   │   ├── DevolucionCompra.java

│   │   └── CuentaPagar.java

│   ├── dao/

│   │   ├── CompraDAO.java

│   │   ├── DetalleCompraDAO.java

│   │   ├── OrdenCompraDAO.java

│   │   ├── DetalleOrdenCompraDAO.java

│   │   ├── DevolucionCompraDAO.java

│   │   └── CuentaPagarDAO.java

│   ├── logica/

│   │   └── CompraService.java

│   └── vista/

│       ├── FrmCompras.java

│       ├── FrmOrdenCompra.java

│       ├── FrmDevolucionCompra.java

│       └── FrmCuentasPagar.java

│

├── ventas/                         ── PENDIENTE (Rafael)

│   ├── modelo/

│   │   ├── Venta.java

│   │   ├── DetalleVenta.java

│   │   ├── Comprobante.java

│   │   ├── DevolucionVenta.java

│   │   └── CuentaCobrar.java

│   ├── dao/

│   │   ├── VentaDAO.java

│   │   ├── DetalleVentaDAO.java

│   │   ├── ComprobanteDAO.java

│   │   ├── DevolucionVentaDAO.java

│   │   └── CuentaCobrarDAO.java

│   ├── logica/

│   │   └── VentaService.java

│   └── vista/

│       ├── FrmVentas.java

│       ├── FrmComprobantes.java

│       ├── FrmDevolucionVenta.java

│       └── FrmCuentasCobrar.java

│

├── tesoreria/                      ── PENDIENTE (Rafael)

│   ├── modelo/

│   │   ├── Caja.java

│   │   ├── MovimientoCaja.java

│   │   ├── CuentaBancaria.java

│   │   ├── MovimientoBanco.java

│   │   └── CierreCaja.java

│   ├── dao/

│   │   ├── CajaDAO.java

│   │   ├── MovimientoCajaDAO.java

│   │   ├── CuentaBancariaDAO.java

│   │   ├── MovimientoBancoDAO.java

│   │   └── CierreCajaDAO.java

│   ├── logica/

│   │   └── TesoreriaService.java

│   └── vista/

│       ├── FrmCaja.java

│       ├── FrmBanco.java

│       ├── FrmMovimientoCaja.java

│       └── FrmCierreCaja.java

│

├── contabilidad/                   ── PENDIENTE (Jeferson)

│   ├── modelo/

│   │   ├── AsientoContable.java

│   │   ├── DetalleAsiento.java

│   │   └── dto/                    → todos derivados, no tablas

│   │       ├── LibroDiarioItem.java

│   │       ├── LibroMayorItem.java

│   │       ├── BalanceComprobacionItem.java

│   │       ├── BalanceGeneralItem.java

│   │       └── EstadoResultadosItem.java

│   ├── dao/

│   │   ├── AsientoContableDAO.java

│   │   └── DetalleAsientoDAO.java  (los reportes se derivan de estos dos, sin DAO propio)

│   ├── logica/

│   │   ├── ContabilidadService.java        → fachada

│   │   ├── AsientoService.java             → valida partida doble (Debe=Haber)

│   │   ├── LibroDiarioService.java

│   │   ├── LibroMayorService.java

│   │   ├── BalanceComprobacionService.java

│   │   ├── BalanceGeneralService.java

│   │   └── EstadoResultadosService.java

│   └── vista/

│       ├── FrmAsientos.java

│       ├── FrmLibroDiario.java

│       ├── FrmLibroMayor.java

│       ├── FrmBalanceComprobacion.java

│       ├── FrmBalanceGeneral.java

│       └── FrmEstadoResultados.java

│

└── procesos/                       ── PENDIENTE (Jeferson)

&#x20;   ├── ProcesoVenta.java            → Venta + Inventario + Tesorería + Contabilidad

&#x20;   ├── ProcesoCobroCliente.java     → Tesorería + Venta + Contabilidad

&#x20;   ├── ProcesoCompra.java           → Compra + Inventario + Tesorería + Contabilidad

&#x20;   └── ProcesoPagoProveedor.java    → Tesorería + Compra + Contabilidad





Nunca crear un paquete `modelo/`, `dao/`, `dto/` global a nivel de toda la

aplicación — eso rompe la cohesión que permite entender un módulo completo

mirando una sola carpeta.



\## 2. Las 17 reglas de arquitectura



1\. \*\*Paquetes por dominio, no por capa global.\*\* Ver estructura arriba.

2\. \*\*Cada `Service` es dueño solo de sus propias entidades.\*\* `VentaService` no

&#x20;  modifica `Stock` ni genera asientos contables directamente.

3\. \*\*Los coordinadores en `procesos/` se usan únicamente cuando una operación

&#x20;  involucra 2+ módulos.\*\* Si la operación es interna a un solo módulo (ej. un

&#x20;  depósito de caja a banco, ambos en Tesorería), el propio `Service` del módulo

&#x20;  es el orquestador transaccional — no crear un `Proceso\\\*` para eso.

4\. \*\*Los DAO de reportes o consulta se mantienen separados por entidad\*\*, nunca

&#x20;  fusionados en una clase genérica, aunque no hagan operaciones de escritura.

5\. \*\*Los DTO viven dentro de su módulo\*\* (`inventario/modelo/dto/KardexItem`),

&#x20;  nunca en un paquete `dto/` global.

6\. \*\*Ningún `Service` manipula `Connection` directamente.\*\* La transacción la

&#x20;  controla el orquestador de más alto nivel de la operación mediante un

&#x20;  `TransactionContext` (obtenido de `TransactionManager.iniciar()`). Los DAO

&#x20;  obtienen la conexión activa de forma transparente vía

&#x20;  `TransactionManager.obtenerActual()`. Un DAO invocado fuera de una

&#x20;  transacción activa abre su propia conexión con `autoCommit=true`

&#x20;  (confirma inmediatamente); invocado dentro de una transacción, hereda

&#x20;  `autoCommit=false` y no confirma nada hasta que el orquestador haga

&#x20;  `commit()`.

7\. \*\*Los DAO nunca cierran una conexión que no abrieron ellos mismos.\*\*

8\. \*\*Errores de negocio → `RespuestaOperacion<T>`. Errores técnicos →

&#x20;  excepciones propias\*\* (`DaoException`, `ServiceException`). Nunca usar

&#x20;  excepciones para validaciones de negocio (campo vacío, stock insuficiente,

&#x20;  etc.) — eso siempre se comunica con `RespuestaOperacion.error(mensaje)`.

9\. \*\*Todo orquestador revisa `RespuestaOperacion.isExito()` inmediatamente

&#x20;  después de cada paso\*\* y aborta sin continuar si falla. Esto queda

&#x20;  garantizado automáticamente por `TransactionContext` al implementar

&#x20;  `AutoCloseable`: usar siempre `try (TransactionContext tx =

&#x20;  TransactionManager.iniciar()) { ... tx.commit(); }`\\\*\\\*sin bloque`catch`\*\* —

&#x20;  el `close()` automático hace `rollback()` si `commit()` nunca se ejecutó.

10\. \*\*Los reportes derivados (Kardex, Libro Diario, Libro Mayor, Balances,

&#x20;   Estado de Resultados) nunca son tablas persistentes.\*\* Se calculan en la

&#x20;   capa de lógica a partir de las tablas transaccionales (`movimiento\\\_inventario`,

&#x20;   `asiento\\\_contable` + `detalle\\\_asiento`).

11\. \*\*`Stock` y `MovimientoInventario` siempre se actualizan juntos, en la

&#x20;   misma transacción.\*\* Nunca uno sin el otro.

12\. \*\*`CuentaPagar` pertenece a `compras/`, `CuentaCobrar` pertenece a

&#x20;   `ventas/`\*\* — nunca a `tesoreria/`. Tesorería administra el dinero, no la

&#x20;   deuda; la deuda la administra el módulo comercial que la originó.

13\. \*\*Las fórmulas de negocio compartidas (IGV, CPP, redondeos) se centralizan

&#x20;   en `util/`\*\* (`CalculadoraImpuestos`, `CalculadoraCPP`), nunca repetidas

&#x20;   dentro de cada `Service`.

14\. \*\*Los índices de base de datos solo se crean para casos de uso reales

&#x20;   identificados\*\*, nunca "por si acaso" — cada índice acelera lecturas pero

&#x20;   ralentiza escrituras.

15\. Ver regla 3.

16\. \*\*Integridad referencial:\*\* `ON UPDATE CASCADE` en todas las FK;

&#x20;   `ON DELETE RESTRICT` por defecto (filosofía de anular/desactivar, nunca

&#x20;   borrar físicamente); `ON DELETE CASCADE` únicamente en tablas de detalle

&#x20;   puro (`detalle\\\_venta`, `detalle\\\_compra`, `detalle\\\_orden\\\_compra`,

&#x20;   `detalle\\\_asiento`, `rol\\\_permiso`) — una tabla usa `CASCADE` solo cuando la

&#x20;   fila hija no tiene significado fuera de la existencia de la fila padre.

17\. \*\*Convención de escritura en los DAO:\*\*

&#x20;   - `INSERT` siempre usa `RETURNING id\\\_xxx`.

&#x20;   - `UPDATE` usa `RETURNING <columna>` si el objeto Java necesita ese valor

&#x20;     de vuelta (ej. `fecha\\\_ultima\\\_actualizacion`), o `RETURNING 1` si solo se

&#x20;     verifica que la fila existe.

&#x20;   - El `ResultSet` siempre se lee \*\*por nombre de columna\*\*, nunca por índice.

&#x20;   - Si `!rs.next()` después de un `RETURNING`, se lanza `DaoException`

&#x20;     inmediatamente — indica que la operación no afectó ninguna fila.

&#x20;   - Nunca usar `Statement.RETURN\\\_GENERATED\\\_KEYS`.



\## 3. Patrón estándar de una entidad con estado (`activo`)



Toda entidad con columna `activo` (Producto, Proveedor, Cliente, Usuario)

implementa el mismo contrato:



\*\*DAO:\*\* `insertar`, `actualizar`, `buscarPorId`, `listar`, `listarActivos`,

`activar(id)`, `desactivar(id)` — estos dos últimos usando un método privado

interno `cambiarEstado(id, boolean)` para no duplicar el SQL.



\*\*Service:\*\* `registrar`, `actualizar`, `activar`, `desactivar`, `listar`,

`listarActivos`, más los métodos de búsqueda específicos de la entidad

(`buscarPorId`, `buscarPorNombre`, `buscarPorCodigo`, etc. — \*\*todo método de

consulta que exista en el DAO debe tener su espejo en el Service\*\*, para que

las Vistas nunca necesiten llamar al DAO directamente).



`activar()`/`desactivar()` verifican primero el estado actual y devuelven un

mensaje de negocio claro si la operación no tiene efecto (ej. "El producto ya

se encuentra activo").



\## 4. Manejo de errores — ejemplo de referencia



```java

public RespuestaOperacion<Void> registrar(Categoria categoria) {

\&#x20;   if (categoria == null) {

\&#x20;       return RespuestaOperacion.error("La categoría es obligatoria");

\&#x20;   }

\&#x20;   RespuestaOperacion<String> r = Validaciones.requerido(categoria.getNombre(), "El nombre", 50);

\&#x20;   if (!r.isExito()) return RespuestaOperacion.error(r.getMensaje());

\&#x20;   categoria.setNombre(r.getResultado());



\&#x20;   if (categoriaDAO.buscarPorNombre(categoria.getNombre()) != null) {

\&#x20;       return RespuestaOperacion.error("Ya existe una categoría con ese nombre");

\&#x20;   }

\&#x20;   categoriaDAO.insertar(categoria);

\&#x20;   return RespuestaOperacion.ok();

}

```



\## 5. Operación transaccional multi-módulo — ejemplo de referencia



```java

public class ProcesoVenta {

\&#x20;   public RespuestaOperacion<Void> ejecutar(...) {

\&#x20;       try (TransactionContext tx = TransactionManager.iniciar()) {

\&#x20;           RespuestaOperacion<Void> rVenta = ventaService.registrarVenta(...);

\&#x20;           if (!rVenta.isExito()) return rVenta;



\&#x20;           RespuestaOperacion<Void> rInventario = inventarioService.registrarSalida(...);

\&#x20;           if (!rInventario.isExito()) return rInventario;



\&#x20;           RespuestaOperacion<Void> rContabilidad = contabilidadService.generarAsientoVenta(...);

\&#x20;           if (!rContabilidad.isExito()) return rContabilidad;



\&#x20;           tx.commit();

\&#x20;           return RespuestaOperacion.ok();

\&#x20;       }

\&#x20;   }

}

```



Ningún `catch` es necesario — si cualquier paso lanza una excepción técnica, el

`close()` automático de `TransactionContext` revierte la transacción.



\## 6. Decisiones de alcance del proyecto



\- Una sola sucursal (sin `InventarioSucursal` ni soporte multi-almacén).

\- IGV fijo al 18%, `precio\\\_venta` de producto ya incluye IGV.

\- Balance de Comprobación (RF07) y Balance General (RF13, prioridad baja)

&#x20; ambos implementados como reportes derivados, no como tablas.

\- El diagrama de estados de Venta usa 5 estados (`INICIADA`, `PAGO\\\_PENDIENTE`,

&#x20; `PAGADA`, `DESPACHADA`, `CANCELADA`), sin `COMPROBANTE\\\_EMITIDO` — revisión

&#x20; respecto a la Figura 2 del PA1 original, pendiente de documentar formalmente

&#x20; en el próximo entregable académico.

\- Sin auditoría de intentos de login fallidos con usuario inexistente (la FK

&#x20; `auditoria.id\\\_usuario` es `NOT NULL`, no hay a qué usuario asociar el intento).



\## 7. Módulo de referencia



El módulo \*\*Inventario\*\* (`inventario/`) es la plantilla oficial para

implementar Compras, Ventas, Tesorería y Contabilidad. Ante cualquier duda de

"¿cómo se supone que se ve un DAO/Service en este proyecto?", mirar primero

`StockDAOImpl`, `MovimientoInventarioDAOImpl` e `InventarioServiceImpl`.

