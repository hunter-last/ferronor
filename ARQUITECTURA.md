\# Arquitectura del Sistema — Decor Home Ferronor



Este documento es de lectura \*\*obligatoria\*\* antes de escribir cualquier código

nuevo. Las reglas aquí descritas no son sugerencias: mantienen la consistencia

entre los módulos ya construidos (Inventario, Maestros, Seguridad) y los que

faltan (Compras, Ventas, Tesorería, Contabilidad).



\## 1. Estructura de paquetes



com.ferronor.sic/

├── config/ — Configuracion (properties externos), Constantes (IGV, escala, redondeo)

├── conexion/ — ConexionPostgres, TransactionManager, TransactionContext

├── shared/ — RespuestaOperacion<T>, SesionUsuario, ServiceFactory, FrmBase,

│ IGeneralDAO, IHistoricoDAO

│ └── dao/ — AbstractDAO

├── util/ — Validaciones, CalculadoraImpuestos, CalculadoraCPP

├── exception/ — DaoException, ServiceException

├── seguridad/ — usuario, rol, permiso, login

├── auditoria/ — registro transversal de acciones

├── maestros/ — categoria, unidadmedida, formapago, tipocomprobante,

│ proveedor, cliente, producto, plancuenta

├── inventario/ — stock, movimientoinventario, ajusteinventario, kardex

├── compras/ — \[pendiente]

├── ventas/ — \[pendiente]

├── tesoreria/ — \[pendiente]

├── contabilidad/ — \[pendiente]

└── procesos/ — coordinadores multi-módulo \[pendiente]





Cada módulo de negocio se organiza por \*\*dominio primero\*\*, no por capa global:



modulo/

├── modelo/

│ └── dto/ (solo si el módulo tiene datos derivados, ej. inventario/modelo/dto/KardexItem)

├── dao/

├── logica/

└── vista/





Nunca crear un paquete `modelo/`, `dao/`, `dto/` global a nivel de toda la

aplicación — eso rompe la cohesión que permite entender un módulo completo

mirando una sola carpeta.



\## 2. Las 17 reglas de arquitectura (backend)



1\. \*\*Paquetes por dominio, no por capa global.\*\*

2\. \*\*Cada `Service` es dueño solo de sus propias entidades.\*\*

3\. \*\*Los coordinadores en `procesos/` se usan únicamente cuando una operación

&#x20;  involucra 2+ módulos.\*\* Si es interna a un solo módulo, el propio `Service`

&#x20;  es el orquestador transaccional.

4\. \*\*Los DAO de reportes o consulta se mantienen separados por entidad\*\*,

&#x20;  nunca fusionados en una clase genérica.

5\. \*\*Los DTO viven dentro de su módulo\*\*, nunca en un paquete `dto/` global.

6\. \*\*Ningún `Service` manipula `Connection` directamente.\*\* La transacción la

&#x20;  controla el orquestador de más alto nivel mediante `TransactionContext`

&#x20;  (de `TransactionManager.iniciar()`). Los DAO obtienen la conexión activa

&#x20;  vía `TransactionManager.obtenerActual()`. Fuera de una transacción activa,

&#x20;  un DAO abre su propia conexión con `autoCommit=true`.

7\. \*\*Los DAO nunca cierran una conexión que no abrieron ellos mismos.\*\*

8\. \*\*Errores de negocio → `RespuestaOperacion<T>`. Errores técnicos →

&#x20;  excepciones propias\*\* (`DaoException`, `ServiceException`).

9\. \*\*Todo orquestador revisa `RespuestaOperacion.isExito()`\*\* tras cada paso.

&#x20;  Garantizado automáticamente por `TransactionContext` (implementa

&#x20;  `AutoCloseable`): usar siempre `try (TransactionContext tx =

&#x20;  TransactionManager.iniciar()) { ... tx.commit(); }`\\\\\\\\\\\\\\\*\\\\\\\\\\\\\\\*sin bloque`catch`\*\*.

10\. \*\*Los reportes derivados (Kardex, Libro Diario, Libro Mayor, Balances,

&#x20;   Estado de Resultados) nunca son tablas persistentes.\*\*

11\. \*\*`Stock` y `MovimientoInventario` siempre se actualizan juntos, en la

&#x20;   misma transacción.\*\*

12\. \*\*`CuentaPagar` pertenece a `compras/`, `CuentaCobrar` a `ventas/`\*\* —

&#x20;   nunca a `tesoreria/`.

13\. \*\*Fórmulas de negocio compartidas (IGV, CPP) centralizadas en `util/`\*\*,

&#x20;   nunca repetidas dentro de cada `Service`.

14\. \*\*Los índices de base de datos solo se crean para casos de uso reales

&#x20;   identificados\*\*, nunca "por si acaso".

15\. Ver regla 3.

16\. \*\*Integridad referencial:\*\* `ON UPDATE CASCADE` en todas las FK;

&#x20;   `ON DELETE RESTRICT` por defecto; `ON DELETE CASCADE` únicamente en

&#x20;   tablas de detalle puro (`detalle\\\\\\\\\\\\\\\_venta`, `detalle\\\\\\\\\\\\\\\_compra`,

&#x20;   `detalle\\\\\\\\\\\\\\\_orden\\\\\\\\\\\\\\\_compra`, `detalle\\\\\\\\\\\\\\\_asiento`, `rol\\\\\\\\\\\\\\\_permiso`).

17\. \*\*Convención de escritura en los DAO:\*\* `INSERT` usa `RETURNING id\\\\\\\\\\\\\\\_xxx`;

&#x20;   `UPDATE` usa `RETURNING <columna>` o `RETURNING 1` si solo se verifica

&#x20;   existencia; el `ResultSet` se lee siempre por nombre de columna; si

&#x20;   `!rs.next()` tras un `RETURNING`, se lanza `DaoException` de inmediato;

&#x20;   nunca usar `Statement.RETURN\\\\\\\\\\\\\\\_GENERATED\\\\\\\\\\\\\\\_KEYS`.



\## 3. Patrón estándar de una entidad con estado (`activo`)



Toda entidad con columna `activo` (Producto, Proveedor, Cliente, Usuario)

implementa el mismo contrato:



\*\*DAO:\*\* `insertar`, `actualizar`, `buscarPorId`, `listar`, `listarActivos`,

`activar(id)`, `desactivar(id)` (usando un método privado interno

`cambiarEstado(id, boolean)`).



\*\*Service:\*\* `registrar`, `actualizar`, `activar`, `desactivar`, `listar`,

`listarActivos`, más los métodos de búsqueda específicos de la entidad

(`buscarPorId`, `buscarPorNombre`, `buscarPorCodigo`, etc. — todo método de

consulta del DAO debe tener su espejo en el Service).



\## 3.1 Patrón de formularios (capa `vista/`)



Todo formulario CRUD sigue estas 9 reglas. Usar `maestros/vista/FrmCategoria.java`

como plantilla de referencia antes de crear un `Frm` nuevo.



1\. \*\*Heredar de `FrmBase`\*\*, pasando el permiso de módulo en el constructor

&#x20;  (`super("MAESTROS")`, `super("INVENTARIO")`, etc.) — valida sesión activa

&#x20;  y permiso automáticamente.

2\. \*\*Obtener el `Service` desde `ServiceFactory`\*\*, nunca instanciando DAO ni

&#x20;  Service manualmente en el formulario.

3\. \*\*La vista nunca accede al DAO directamente.\*\*

4\. \*\*Las consultas usan los métodos de lectura del `Service`.\*\* Si falta uno,

&#x20;  se agrega ahí, nunca se resuelve accediendo al DAO desde la vista.

5\. \*\*Las escrituras siempre devuelven `RespuestaOperacion<T>`\*\*; el

&#x20;  formulario revisa `isExito()` y muestra `getMensaje()` en `JOptionPane`

&#x20;  cuando falla.

6\. \*\*No modificar el `DefaultTableModel` con datos parciales\*\* — la tabla se

&#x20;  recarga completa desde el `Service`.

7\. \*\*Recargar la tabla después de cualquier operación exitosa.\*\*

8\. \*\*Crear/editar en un diálogo separado\*\*, nunca editando celdas de la tabla

&#x20;  directamente.

9\. \*\*Nunca borrado físico.\*\* Si la entidad tiene `activo`, el botón

&#x20;  "Eliminar" se reemplaza por "Activar"/"Desactivar". Si no tiene `activo`

&#x20;  (`Categoria`, `UnidadMedida`, `FormaPago`), no se ofrece ninguna baja.



\*\*Búsqueda con filtro parcial:\*\* se resuelve en el `Service`

(`buscarPorNombreParcial(texto)`), nunca con `.stream().filter(...)` en la

vista. Para catálogos pequeños puede filtrar en memoria hoy; para entidades

de volumen alto (`Producto`) migra a `ILIKE` en el DAO sin que la vista

cambie.



\## 4. Modelo de permisos (Seguridad)



Dos niveles:



\- \*\*Permisos de módulo\*\* (controlan si el menú aparece):

&#x20; `MAESTROS`, `INVENTARIO`, `COMPRAS`, `VENTAS`, `TESORERIA`, `CONTABILIDAD`,

&#x20; `SEGURIDAD`.

\- \*\*Permisos de operación\*\* (controlan una acción sensible dentro del módulo):

&#x20; `ADMIN\\\\\\\\\\\\\\\_USUARIOS`, `AJUSTAR\\\\\\\\\\\\\\\_STOCK`, `REGISTRAR\\\\\\\\\\\\\\\_COMPRA`, `REGISTRAR\\\\\\\\\\\\\\\_VENTA`,

&#x20; `ANULAR\\\\\\\\\\\\\\\_VENTA`, `VER\\\\\\\\\\\\\\\_BALANCE`, `GENERAR\\\\\\\\\\\\\\\_REPORTES`.



Una pantalla nueva dentro de un módulo existente \*\*hereda\*\* el permiso del

módulo, no necesita un permiso propio. Los `Service` solo validan el permiso

de operación en las acciones realmente sensibles (hoy: `AjusteInventarioService`

valida `AJUSTAR\\\\\\\\\\\\\\\_STOCK`; `UsuarioService` valida `ADMIN\\\\\\\\\\\\\\\_USUARIOS` en

`registrar`/`actualizar`/`activar`/`desactivar`, pero no en `cambiarPassword`,

que cualquier usuario autenticado puede usar sobre sí mismo).



\## 5. Manejo de errores — ejemplo de referencia



```java

public RespuestaOperacion<Void> registrar(Categoria categoria) {

\\\\\\\&#x20;   if (categoria == null) return RespuestaOperacion.error("La categoría es obligatoria");

\\\\\\\&#x20;   RespuestaOperacion<String> r = Validaciones.requerido(categoria.getNombre(), "El nombre", 50);

\\\\\\\&#x20;   if (!r.isExito()) return RespuestaOperacion.error(r.getMensaje());

\\\\\\\&#x20;   categoria.setNombre(r.getResultado());



\\\\\\\&#x20;   if (categoriaDAO.buscarPorNombre(categoria.getNombre()) != null) {

\\\\\\\&#x20;       return RespuestaOperacion.error("Ya existe una categoría con ese nombre");

\\\\\\\&#x20;   }

\\\\\\\&#x20;   categoriaDAO.insertar(categoria);

\\\\\\\&#x20;   return RespuestaOperacion.ok();

}

```



\## 6. Operación transaccional multi-módulo — ejemplo de referencia



```java

public class ProcesoVenta {

\\\\\\\&#x20;   public RespuestaOperacion<Void> ejecutar(...) {

\\\\\\\&#x20;       try (TransactionContext tx = TransactionManager.iniciar()) {

\\\\\\\&#x20;           RespuestaOperacion<Void> rVenta = ventaService.registrarVenta(...);

\\\\\\\&#x20;           if (!rVenta.isExito()) return rVenta;



\\\\\\\&#x20;           RespuestaOperacion<Void> rInventario = inventarioService.registrarSalida(...);

\\\\\\\&#x20;           if (!rInventario.isExito()) return rInventario;



\\\\\\\&#x20;           RespuestaOperacion<Void> rContabilidad = contabilidadService.generarAsientoVenta(...);

\\\\\\\&#x20;           if (!rContabilidad.isExito()) return rContabilidad;



\\\\\\\&#x20;           tx.commit();

\\\\\\\&#x20;           return RespuestaOperacion.ok();

\\\\\\\&#x20;       }

\\\\\\\&#x20;   }

}

```



\## 7. Decisiones de alcance del proyecto



\- Una sola sucursal (sin `InventarioSucursal` ni soporte multi-almacén).

\- IGV fijo al 18%, `precio\\\\\\\\\\\\\\\_venta` de producto ya incluye IGV.

\- Balance de Comprobación (RF07) y Balance General (RF13, prioridad baja)

&#x20; ambos como reportes derivados, no como tablas.

\- El diagrama de estados de Venta usa 5 estados (`INICIADA`, `PAGO\\\\\\\\\\\\\\\_PENDIENTE`,

&#x20; `PAGADA`, `DESPACHADA`, `CANCELADA`), sin `COMPROBANTE\\\\\\\\\\\\\\\_EMITIDO` — revisión

&#x20; respecto a la Figura 2 del PA1 original, pendiente de documentar

&#x20; formalmente en el próximo entregable académico.

\- Sin auditoría de intentos de login fallidos con usuario inexistente

&#x20; (`auditoria.id\\\\\\\\\\\\\\\_usuario` es `NOT NULL`).



\## 8. Módulo de referencia



\*\*Inventario\*\* (`inventario/`) es la plantilla oficial de backend.

\*\*`FrmCategoria`\*\* (`maestros/vista/`) es la plantilla oficial de formularios.

Ante cualquier duda, copiar su forma, no inventar un patrón distinto.

