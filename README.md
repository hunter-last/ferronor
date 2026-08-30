# Sistema de Gestión Comercial y Contable — Decor Home Ferronor

Sistema de escritorio (Java Swing + JDBC + PostgreSQL) desarrollado para Decor Home
Ferronor S.A.C., como proyecto conjunto de los cursos de Ingeniería de Software y
Sistemas de Información Contable — UNPRG, semestre 2026-I.

## Stack técnico

- **Lenguaje:** Java 23
- **UI:** Java Swing
- **Base de datos:** PostgreSQL 15+
- **Acceso a datos:** JDBC puro (sin ORM), patrón DAO
- **Build:** Maven
- **Reportes:** Apache PDFBox (exportación a PDF), exportación CSV propia

## Requisitos previos

- JDK 23
- PostgreSQL instalado y corriendo localmente
- Maven (o usar el wrapper de NetBeans)

## Instalación de la base de datos

**Base de datos:** `dbferronor` · **Motor:** PostgreSQL

### Paso 1 — Crear la base de datos vacía

- **pgAdmin:** clic derecho en *Databases* → *Create* → *Database* → nombre `dbferronor`
- **Consola:** `createdb -U postgres dbferronor` (agrega `-p 5433` si tu servidor no usa el puerto por defecto)

### Paso 2 — Conectarte a la base de datos

- **pgAdmin:** selecciona `dbferronor` → *Query Tool*
- **Consola:** `psql -U postgres -d dbferronor`

### Paso 3 — Ejecutar los scripts de `sql/`, en este orden exacto

| # | Archivo | Contenido |
|---|---|---|
| 1 | `01_seguridad.sql` | Roles, permisos, usuarios |
| 2 | `02_maestros.sql` | Categorías, productos, clientes, proveedores |
| 3 | `03_inventario.sql` | Stock, movimientos |
| 4 | `04_compras.sql` | Órdenes de compra, compras |
| 5 | `05_ventas.sql` | Ventas, comprobantes |
| 6 | `06_tesoreria.sql` | Caja, cuentas bancarias |
| 7 | `07_contabilidad.sql` | Plan de cuentas, asientos |
| 8 | `08_auditoria.sql` | Registro de eventos |
| 9 | `09_indices.sql` | Índices y restricciones adicionales |
| 10 | `10_datos_iniciales.sql` | Roles/usuarios de prueba, catálogos base y datos de ejemplo |

**Importante:** el orden es obligatorio — cada script depende de las tablas creadas por el anterior (llaves foráneas entre módulos).

Ejecuta cada archivo, uno por uno, y **verifica que no haya errores** antes de pasar al siguiente. Si usas pgAdmin: abre el archivo `.sql` en el *Query Tool* de `dbferronor` y ejecútalo (F5) antes de abrir el siguiente.

### Usuarios de prueba

`10_datos_iniciales.sql` crea un usuario por rol para poder probar el control de acceso del sistema:

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `12345678` | Administrador (acceso total) |
| `cajero1` | `Ferronor123` | Cajero |
| `tesoreria1` | `Ferronor123` | Tesorería |
| `contador1` | `Ferronor123` | Contador |
| `logistica1` | `Ferronor123` | Logística |

Cada uno ve un menú distinto en `FrmPrincipal` según sus permisos — es la forma más rápida de comprobar que el control de acceso por rol funciona.

## Configuración de credenciales

**Nunca subas tus credenciales reales al repositorio.**

1. Copia `src/main/resources/config.properties.example` a `src/main/resources/config.properties`.
2. Completa `config.properties` con tu usuario y contraseña reales de PostgreSQL, apuntando a la base `dbferronor`.
3. Este archivo está en `.gitignore` — no se sube nunca.

## Cómo correr el proyecto

Desde NetBeans: abre el proyecto Maven y ejecuta `Main.java`
(`src/main/java/com/ferronor/sic/Main.java`), el punto de entrada de la aplicación.

Desde línea de comandos:

```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.ferronor.sic.Main"
```


## Estructura del proyecto

```
com.ferronor.sic/
├── config/        — configuración y constantes del sistema
├── conexion/      — conexión a PostgreSQL y manejo de transacciones
├── shared/        — clases transversales (RespuestaOperacion, SesionUsuario, ServiceFactory, FrmBase, etc.)
├── util/          — utilidades (cálculos de IGV, CPP, validaciones, exportadores CSV/PDF)
├── exception/     — excepciones propias del sistema
├── seguridad/     — usuarios, roles, permisos, login
├── auditoria/     — registro de acciones del sistema
├── maestros/      — catálogos base (productos, clientes, proveedores, categorías, formas de pago, etc.)
├── inventario/    — control de stock, movimientos, Kardex
├── compras/       — órdenes de compra, aprobación, compras, devoluciones
├── ventas/        — ventas, comprobantes, cobros, devoluciones
├── tesoreria/     — caja, bancos, cuentas por pagar/cobrar
├── contabilidad/  — asientos contables y reportes financieros
└── procesos/      — coordinadores de operaciones que cruzan varios módulos
```

Cada módulo de negocio se organiza por **dominio primero**, no por capa global:
```
modulo/
├── modelo/
│ └── dto/ (solo si el módulo tiene datos derivados, ej. inventario/modelo/dto/KardexItem)
├── dao/
├── logica/
└── vista/
<<<<<<< HEAD



\## DETALLE DE LA ESTRUCTURA

com.ferronor.sic
│
├── Main.java
│
├── config/
│   ├── Configuracion.java          → lee config.properties externo; falla si falta una clave, sin defaults silenciosos
│   └── Constantes.java             → IGV 18%, escala moneda, redondeo
│
├── conexion/
│   └── TransactionManager.java     → TransactionManager.iniciar() devuelve TransactionContext (AutoCloseable, ThreadLocal<Connection>, rollback automático en close() si no hubo commit)
│
├── shared/
│   ├── dao/
│   │   └── AbstractDAO.java
│   ├── IGeneralDAO.java            → sin eliminar, filosofía anular/desactivar
│   ├── IHistoricoDAO.java          → tablas solo-insertan (movimiento_inventario, ajuste_inventario)
│   ├── ServiceFactory.java         → provee instancias de Service a las vistas, sin acceso directo a DAO desde vista
│   ├── SesionUsuario.java          → tienePermiso(String) instancia + puedeAcceder(String) estático (renombrado por conflicto de sobrecarga)
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
│   └── ServiceException.java       (BusinessException descartada, no se usa)
│
├── auditoria/                      ── CERRADO — transversal, independiente de seguridad
│   ├── modelo/Auditoria.java
│   ├── dao/AuditoriaDAO.java
│   └── logica/AuditoriaService.java
│
├── seguridad/                      ── CERRADO
│   ├── modelo/
│   │   ├── Usuario.java
│   │   ├── Rol.java
│   │   ├── Permiso.java
│   │   └── RolPermiso.java         → PK compuesta, verbos de dominio (asignar/revocar), sin RolPermisoService aparte
│   ├── dao/
│   │   ├── UsuarioDAO.java         → actualizarPassword() dedicado
│   │   ├── RolDAO.java
│   │   ├── PermisoDAO.java         → listarPorRol() con JOIN, evita N+1
│   │   └── RolPermisoDAO.java
│   ├── logica/
│   │   ├── LoginService.java       → BCrypt, error genérico, login normalizado a minúsculas
│   │   ├── UsuarioService.java     → registrar()/actualizar()/cambiarPassword() separados; excepción de bootstrap: si no hay usuarios, registrar() omite validación de permiso ADMIN_USUARIOS
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
│   │   └── PlanCuenta.java         → jerarquía autorreferenciada PCGE (nivel=nivel_padre+1, no auto-referencia)
│   ├── dao/  (uno por entidad — RETURNING, UPPER() para duplicados insensibles a mayúsculas, cambiarEstado() interno)
│   │   ├── ClienteDAO.java
│   │   ├── ProveedorDAO.java
│   │   ├── ProductoDAO.java
│   │   ├── CategoriaDAO.java
│   │   ├── UnidadMedidaDAO.java
│   │   ├── FormaPagoDAO.java
│   │   ├── TipoComprobanteDAO.java
│   │   └── PlanCuentaDAO.java
│   ├── logica/  (uno por entidad — validarComun, contrato registrar/actualizar/activar/desactivar/listar/listarActivos, buscarPorId/Nombre/Codigo)
│   │   ├── ClienteService.java
│   │   ├── ProveedorService.java
│   │   ├── ProductoService.java
│   │   ├── CategoriaService.java   → buscarPorNombreParcial() (filtra en memoria hoy, migrará a ILIKE)
│   │   ├── UnidadMedidaService.java
│   │   ├── FormaPagoService.java
│   │   ├── TipoComprobanteService.java
│   │   └── PlanCuentaService.java
│   └── vista/  (FrmCategoria = plantilla oficial de formularios, ver sección abajo)
│       ├── FrmClientes.java
│       ├── FrmProveedores.java
│       ├── FrmProductos.java
│       ├── FrmCategorias.java
│       ├── FrmUnidadMedida.java
│       ├── FrmFormaPago.java
│       ├── FrmTipoComprobante.java
│       └── FrmPlanCuentas.java
│
├── inventario/                     ── CERRADO (plantilla de referencia oficial, validado con smoke test real)
│   ├── modelo/
│   │   ├── Stock.java
│   │   ├── MovimientoInventario.java   → reconstruir() para reconstrucción desde BD sin recalcular derivados
│   │   ├── AjusteInventario.java
│   │   └── dto/KardexItem.java     → derivado, no tabla persistente
│   ├── dao/
│   │   ├── StockDAO.java
│   │   ├── MovimientoInventarioDAO.java   → listarHastaFecha(), vincularDocumentoOrigen() (resuelve dependencia circular ajuste↔movimiento)
│   │   └── AjusteInventarioDAO.java   (sin KardexDAO)
│   ├── logica/
│   │   ├── InventarioService.java  → registrarEntrada()/registrarSalida()
│   │   ├── AjusteInventarioService.java
│   │   └── KardexService.java      → obtenerKardex(), reconstruye saldo desde el inicio del historial completo
│   └── vista/
│       ├── FrmStock.java
│       ├── FrmMovimientos.java
│       ├── FrmKardex.java
│       └── FrmAjusteInventario.java
│
├── compras/                        ── PENDIENTE (Rafael)
│   ├── modelo/ (Compra, DetalleCompra, OrdenCompra, DetalleOrdenCompra, DevolucionCompra, CuentaPagar)
│   ├── dao/ (uno por entidad)
│   ├── logica/CompraService.java
│   └── vista/ (FrmCompras, FrmOrdenCompra, FrmDevolucionCompra, FrmCuentasPagar)
│
├── ventas/                         ── PENDIENTE (Rafael)
│   ├── modelo/ (Venta, DetalleVenta, Comprobante, DevolucionVenta, CuentaCobrar)
│   ├── dao/ (uno por entidad)
│   ├── logica/VentaService.java
│   └── vista/ (FrmVentas, FrmComprobantes, FrmDevolucionVenta, FrmCuentasCobrar)
│
├── tesoreria/                      ── PENDIENTE (Rafael)
│   ├── modelo/ (Caja, MovimientoCaja, CuentaBancaria, MovimientoBanco, CierreCaja)
│   ├── dao/ (uno por entidad)
│   ├── logica/TesoreriaService.java
│   └── vista/ (FrmCaja, FrmBanco, FrmMovimientoCaja, FrmCierreCaja)
│
├── contabilidad/                   ── PENDIENTE (Jeferson)
│   ├── modelo/ (AsientoContable, DetalleAsiento)
│   │   └── dto/ (LibroDiarioItem, LibroMayorItem, BalanceComprobacionItem, BalanceGeneralItem, EstadoResultadosItem) → todos derivados
│   ├── dao/ (AsientoContableDAO, DetalleAsientoDAO — los reportes se derivan de estos dos, sin DAO propio)
│   ├── logica/
│   │   ├── ContabilidadService.java   → fachada
│   │   ├── AsientoService.java        → valida partida doble (Debe=Haber) antes de insertar
│   │   ├── LibroDiarioService.java
│   │   ├── LibroMayorService.java
│   │   ├── BalanceComprobacionService.java
│   │   ├── BalanceGeneralService.java
│   │   └── EstadoResultadosService.java
│   └── vista/ (FrmAsientos, FrmLibroDiario, FrmLibroMayor, FrmBalanceComprobacion, FrmBalanceGeneral, FrmEstadoResultados)
│
└── procesos/                       ── PENDIENTE (Jeferson)
    ├── ProcesoVenta.java            → Venta + Inventario + Tesorería + Contabilidad
    ├── ProcesoCobroCliente.java     → Tesorería + Venta + Contabilidad
    ├── ProcesoCompra.java           → Compra + Inventario + Tesorería + Contabilidad
    └── ProcesoPagoProveedor.java    → Tesorería + Compra + Contabilidad




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

=======
>>>>>>> develop
```


Nunca crear un paquete `modelo/`, `dao/`, `dto/` global a nivel de toda la aplicación — eso rompe la cohesión que permite entender un módulo completo mirando una sola carpeta.

## Estado actual del proyecto

**Backend (modelo + DAO + lógica) cerrado en los 8 módulos:** Seguridad, Auditoría, Maestros, Inventario, Compras, Ventas, Tesorería y Contabilidad, más los coordinadores de `procesos/` (ProcesoVenta, ProcesoCompra, ProcesoCobroCliente, ProcesoPagoProveedor).

**Capa de vista (Swing):**

| Módulo | Pantallas |
|---|---|
| Seguridad | Login |
| Maestros | Categorías, Productos, Clientes, Proveedores, Formas de Pago, Tipos de Comprobante, Unidades de Medida |
| Inventario | Consultar Stock, Kardex, Ajuste de Inventario |
| Compras | Solicitar Orden de Compra, Aprobación de Orden de Compra, Registrar Compra, Devolución a Proveedor, Historial de Compras |
| Ventas | Registrar Venta (con generación de comprobante en PDF), Cobro a Cliente, Devolución de Cliente, Historial de Ventas |
| Tesorería | Abrir Caja, Cierre de Caja, Movimientos de Caja, Movimientos Bancarios, Cuentas por Pagar, Cuentas por Cobrar, Pago a Proveedor |
| Contabilidad | Libro Diario, Libro Mayor, Balanza de Comprobación, Balance General, Estado de Resultados |

Libro Mayor, Balanza de Comprobación y Balance General incluyen exportación de reportes a CSV y PDF.

**Pendiente:** gestión de Usuarios y Roles desde la interfaz (Seguridad).

Ver `ARQUITECTURA.md` para el detalle de diseño y las reglas de arquitectura del proyecto, y `CONTRIBUTING.md` para las reglas de ramas y PR del equipo.
