# Arquitectura del Sistema — Decor Home Ferronor

Este documento es de lectura **obligatoria** antes de escribir cualquier código
nuevo. Las reglas aquí descritas no son sugerencias: mantienen la consistencia
entre los módulos ya construidos.

## 1. Estructura de paquetes
```
com.ferronor.sic/
├── Main.java — punto de entrada de la aplicación (arranca FlatDarkLaf y abre FrmLogin)
├── config/ — Configuracion (properties externos), Constantes (IGV, escala, redondeo)
├── conexion/ — ConexionPostgres, TransactionManager, TransactionContext
├── shared/ — RespuestaOperacion<T>, SesionUsuario, ServiceFactory, FrmBase,
│ IGeneralDAO, IHistoricoDAO
│ ├── dao/ — AbstractDAO
│ └── ui/ — ComboAutoFiltro (combo editable con búsqueda dinámica, ver sección 3.1)
├── util/ — Validaciones, CalculadoraImpuestos, CalculadoraCPP,
│ ExportadorCSV, ExportadorPDF, TablaExportUtil (exportación de reportes)
├── exception/ — DaoException, ServiceException
├── seguridad/ — usuario, rol, permiso, login
├── auditoria/ — registro transversal de acciones
├── maestros/ — categoria, unidadmedida, formapago, tipocomprobante,
│ proveedor, cliente, producto, plancuenta
├── inventario/ — stock, movimientoinventario, ajusteinventario, kardex
├── compras/ — compra, ordencompra, devolucioncompra, cuentapagar
├── ventas/ — venta, comprobante, devolucionventa, cuentacobrar
├── tesoreria/ — caja, cuentabancaria, movimientocaja, movimientobanco
├── contabilidad/ — asientocontable, detalleasiento, libros y balances (derivados)
└── procesos/ — coordinadores multi-módulo (ProcesoVenta, ProcesoCompra,
ProcesoCobroCliente, ProcesoPagoProveedor)
```
**Estado actual:** backend (modelo + DAO + logica) y capa `vista/` cerrados en
los 8 módulos, más los 4 coordinadores de `procesos/`. **Pendiente: gestión de
Usuarios y Roles desde la interfaz** (Seguridad) — el resto de módulos tiene
sus pantallas principales terminadas. Ver el detalle completo en `README.md`.

Cada módulo de negocio se organiza por **dominio primero**, no por capa global:
```
modulo/
├── modelo/
│ └── dto/ (solo si el módulo tiene datos derivados, ej. inventario/modelo/dto/KardexItem)
├── dao/
├── logica/
└── vista/
```
Nunca crear un paquete `modelo/`, `dao/`, `dto/` global a nivel de toda la
aplicación — eso rompe la cohesión que permite entender un módulo completo
mirando una sola carpeta.

## 2. Las 17 reglas de arquitectura (backend)

1. **Paquetes por dominio, no por capa global.**
2. **Cada `Service` es dueño solo de sus propias entidades.**
3. **Los coordinadores en `procesos/` se usan únicamente cuando una operación
   involucra 2+ módulos.** Si es interna a un solo módulo, el propio `Service`
   es el orquestador transaccional.
4. **Los DAO de reportes o consulta se mantienen separados por entidad**,
   nunca fusionados en una clase genérica.
5. **Los DTO viven dentro de su módulo**, nunca en un paquete `dto/` global.
6. **Ningún `Service` manipula `Connection` directamente.** La transacción la
   controla el orquestador de más alto nivel mediante `TransactionContext`
   (de `TransactionManager.iniciar()`). Los DAO obtienen la conexión activa
   vía `TransactionManager.obtenerActual()`. Fuera de una transacción activa,
   un DAO abre su propia conexión con `autoCommit=true`.
7. **Los DAO nunca cierran una conexión que no abrieron ellos mismos.**
8. **Errores de negocio → `RespuestaOperacion<T>`. Errores técnicos →
   excepciones propias** (`DaoException`, `ServiceException`).
9. **Todo orquestador revisa `RespuestaOperacion.isExito()`** tras cada paso.
   Garantizado automáticamente por `TransactionContext` (implementa
   `AutoCloseable`): usar siempre
   `try (TransactionContext tx = TransactionManager.iniciar()) { ... tx.commit(); }`
   — sin bloque `catch`.
10. **Los reportes derivados (Kardex, Libro Diario, Libro Mayor, Balances,
    Estado de Resultados) nunca son tablas persistentes.**
11. **`Stock` y `MovimientoInventario` siempre se actualizan juntos, en la
    misma transacción.**
12. **`CuentaPagar` pertenece a `compras/`, `CuentaCobrar` a `ventas/`** —
    nunca a `tesoreria/`.
13. **Fórmulas de negocio compartidas (IGV, CPP) centralizadas en `util/`**,
    nunca repetidas dentro de cada `Service`.
14. **Los índices de base de datos solo se crean para casos de uso reales
    identificados**, nunca "por si acaso".
15. Ver regla 3.
16. **Integridad referencial:** `ON UPDATE CASCADE` en todas las FK;
    `ON DELETE RESTRICT` por defecto; `ON DELETE CASCADE` únicamente en
    tablas de detalle puro (`detalle_venta`, `detalle_compra`,
    `detalle_orden_compra`, `detalle_asiento`, `rol_permiso`).
17. **Convención de escritura en los DAO:** `INSERT` usa `RETURNING id_xxx`;
    `UPDATE` usa `RETURNING <columna>` o `RETURNING 1` si solo se verifica
    existencia; el `ResultSet` se lee siempre por nombre de columna; si
    `!rs.next()` tras un `RETURNING`, se lanza `DaoException` de inmediato;
    nunca usar `Statement.RETURN_GENERATED_KEYS`.

## 3. Patrón estándar de una entidad con estado (`activo`)

Toda entidad con columna `activo` (Producto, Proveedor, Cliente, Usuario)
implementa el mismo contrato:

**DAO:** `insertar`, `actualizar`, `buscarPorId`, `listar`, `listarActivos`,
`activar(id)`, `desactivar(id)` (usando un método privado interno
`cambiarEstado(id, boolean)`).

**Service:** `registrar`, `actualizar`, `activar`, `desactivar`, `listar`,
`listarActivos`, más los métodos de búsqueda específicos de la entidad
(`buscarPorId`, `buscarPorNombre`, `buscarPorCodigo`, etc. — todo método de
consulta del DAO debe tener su espejo en el Service).

## 3.1 Patrón de formularios (capa `vista/`)

Hay **dos familias** de formulario, según su rol en la navegación — no una
sola plantilla universal:

**A) Pantallas principales de módulo (`JFrame`)** — hoy: los 7 CRUD de
Maestros (`FrmGestionCategorias`, `FrmGestionClientes`, etc.) y `FrmVentas`.
Estas **heredan de `FrmBase`**, pasando el permiso de módulo en el
constructor (`super("MAESTROS")`, `super("VENTAS")`, etc.) — `FrmBase` valida
sesión activa y permiso automáticamente al abrirse.

**B) Pantallas secundarias / diálogos (`JDialog`)** — la mayoría del sistema:
todo Tesorería, Contabilidad, Inventario, y el resto de Compras/Ventas
(`FrmCompras`, `FrmKardex`, `FrmLibroMayor`, `FrmCierreCaja`, etc.). Estas
**no heredan de `FrmBase`** — usan el constructor estándar de NetBeans
`FrmX(java.awt.Frame parent, boolean modal)` y llaman a `super(parent, modal)`.
El control de acceso no vive en el constructor del formulario: vive en
`FrmPrincipal`, que decide qué ítems de menú mostrar según
`SesionUsuario.puedeAcceder(...)` (ver sección 4) — si el ítem de menú no
aparece, el diálogo nunca se abre. Se instancian siempre como
`new FrmX(this, true).setVisible(true)` desde `FrmPrincipal` u otro
formulario padre, nunca con `parent = null`.

Reglas comunes a ambas familias:

1. **Obtener el `Service` desde `ServiceFactory`**, nunca instanciando DAO ni
   Service manualmente en el formulario.
2. **La vista nunca accede al DAO directamente.**
3. **Las consultas usan los métodos de lectura del `Service`.** Si falta uno,
   se agrega ahí, nunca se resuelve accediendo al DAO desde la vista.
4. **Las escrituras siempre devuelven `RespuestaOperacion<T>`**; el
   formulario revisa `isExito()` y muestra `getMensaje()` en `JOptionPane`
   cuando falla.
5. **No modificar el `DefaultTableModel` con datos parciales** — la tabla se
   recarga completa desde el `Service`. **Excepción:** columnas de referencia
   editable en tablas de detalle transaccional (ej. costo unitario en
   `FrmCompras` cuando la compra viene de una Orden de Compra) — ahí el
   usuario confirma o corrige el valor línea por línea antes de registrar;
   el resto de la fila (producto, cantidad) sigue sin ser editable.
6. **Recargar la tabla después de cualquier operación exitosa.**
7. **Crear/editar en un diálogo separado**, salvo la excepción del punto 5.
8. **Nunca borrado físico.** Si la entidad tiene `activo`, el botón
   "Eliminar" se reemplaza por "Activar"/"Desactivar". Si no tiene `activo`
   (`Categoria`, `UnidadMedida`, `FormaPago`), no se ofrece ninguna baja.
9. **Combos con búsqueda dinámica usan `ComboAutoFiltro.mejorarCombo(combo,
   servicio::metodoBusqueda)`** (`shared/ui/`) — nunca una implementación
   propia de filtro en la vista. La entidad mostrada en el combo debe tener
   `toString()` propio (ej. `Cliente`, `Producto`, `Proveedor`, `OrdenCompra`)
   o el combo necesita un `ListCellRenderer` explícito — de lo contrario se
   muestra el `hashCode` del objeto en vez de un texto legible.

**Búsqueda con filtro parcial:** se resuelve en el `Service`
(`buscarPorNombreParcial(texto)`), nunca con `.stream().filter(...)` en la
vista. Para catálogos pequeños puede filtrar en memoria hoy; para entidades
de volumen alto (`Producto`) migra a `ILIKE` en el DAO sin que la vista
cambie.

## 4. Modelo de permisos (Seguridad)

Dos niveles:

- **Permisos de módulo** (controlan si el menú aparece):
  `MAESTROS`, `INVENTARIO`, `COMPRAS`, `VENTAS`, `CAJA`, `TESORERIA`,
  `CONTABILIDAD`, `SEGURIDAD`.
- **Permisos de operación** (controlan una acción sensible dentro del módulo):
  `ADMIN_USUARIOS`, `AJUSTAR_STOCK`, `REGISTRAR_COMPRA`, `REGISTRAR_VENTA`,
  `ANULAR_VENTA`, `VER_BALANCE`, `GENERAR_REPORTES`.

Una pantalla nueva dentro de un módulo existente **hereda** el permiso del
módulo, no necesita un permiso propio. Los `Service` solo validan el permiso
de operación en las acciones realmente sensibles (hoy: `AjusteInventarioService`
valida `AJUSTAR_STOCK`; `UsuarioService` valida `ADMIN_USUARIOS` en
`registrar`/`actualizar`/`activar`/`desactivar`, pero no en `cambiarPassword`,
que cualquier usuario autenticado puede usar sobre sí mismo).

**Caso especial — Tesorería, gate por ítem de menú:** el menú "Tesorería" en
`FrmPrincipal` mezcla dos audiencias distintas bajo un mismo módulo, así que
no basta un único permiso para todo el menú. Se separa en dos bloques dentro
de `crearMenuTesoreria()`:

- **`CAJA`** — operación diaria del rol Cajero: Abrir Caja, Cobro a Cliente,
  Movimientos de Caja, Cierre de Caja.
- **`TESORERIA`** — control del rol Tesorería: Cuentas por Pagar, Cuentas por
  Cobrar, Pago a Proveedor, Movimientos Bancarios.

El gate de nivel superior que decide si el menú "Tesorería" aparece siquiera
es `CAJA || TESORERIA` — cualquiera de los dos roles necesita verlo, pero
cada uno ve solo su bloque.

**Caso especial — segregación de funciones en Compras:** "Aprobación de
Orden de Compra" no usa el permiso de módulo `COMPRAS` (que tiene Logística,
quien también *solicita* las órdenes) — usa `ADMIN_USUARIOS`, para que quien
solicita una orden nunca pueda autoaprobarla.

## 5. Manejo de errores — ejemplo de referencia

```java
public RespuestaOperacion<Void> registrar(Categoria categoria) {
    if (categoria == null) return RespuestaOperacion.error("La categoría es obligatoria");
    RespuestaOperacion<String> r = Validaciones.requerido(categoria.getNombre(), "El nombre", 50);
    if (!r.isExito()) return RespuestaOperacion.error(r.getMensaje());
    categoria.setNombre(r.getResultado());

    if (categoriaDAO.buscarPorNombre(categoria.getNombre()) != null) {
        return RespuestaOperacion.error("Ya existe una categoría con ese nombre");
    }
    categoriaDAO.insertar(categoria);
    return RespuestaOperacion.ok();
}
```

## 6. Operación transaccional multi-módulo — ejemplo de referencia

```java
public class ProcesoVenta {
    public RespuestaOperacion<Void> ejecutar(...) {
        try (TransactionContext tx = TransactionManager.iniciar()) {
            RespuestaOperacion<Void> rVenta = ventaService.registrarVenta(...);
            if (!rVenta.isExito()) return rVenta;

            RespuestaOperacion<Void> rInventario = inventarioService.registrarSalida(...);
            if (!rInventario.isExito()) return rInventario;

            RespuestaOperacion<Void> rContabilidad = contabilidadService.generarAsientoVenta(...);
            if (!rContabilidad.isExito()) return rContabilidad;

            tx.commit();
            return RespuestaOperacion.ok();
        }
    }
}
```

Ningún `catch` es necesario: si un paso falla, se hace `return` sin llamar a
`tx.commit()`, y el `close()` automático de `TransactionContext` revierte la
transacción. No es necesario (ni el estilo de este proyecto) llamar a
`tx.rollback()` explícitamente en cada rama de fallo.

## 7. Decisiones de alcance del proyecto

- Una sola sucursal (sin `InventarioSucursal` ni soporte multi-almacén).
- IGV fijo al 18%, `precio_venta` de producto ya incluye IGV.
- Balance de Comprobación (RF07) y Balance General (RF13, prioridad baja)
  ambos como reportes derivados, no como tablas.
- El diagrama de estados de Venta usa 5 estados (`INICIADA`, `PAGO_PENDIENTE`,
  `PAGADA`, `DESPACHADA`, `CANCELADA`), sin `COMPROBANTE_EMITIDO` — revisión
  respecto a la Figura 2 del PA1 original, pendiente de documentar
  formalmente en el próximo entregable académico.
- Sin auditoría de intentos de login fallidos con usuario inexistente
  (`auditoria.id_usuario` es `NOT NULL`).

## 8. Módulo de referencia

**Inventario** (`inventario/`) es la plantilla oficial de backend.

Para la capa de vista no hay una sola plantilla — depende de la familia
(sección 3.1): **`FrmGestionCategorias`** (`maestros/vista/`) es la
referencia para pantallas principales tipo `JFrame`/`FrmBase`;
**`FrmCuentasCobrar`** (`tesoreria/vista/`) es la referencia para un diálogo
de solo consulta simple, y **`FrmCompras`** (`compras/vista/`) para un
diálogo maestro-detalle transaccional con tabla editable. Ante cualquier
duda, copiar la forma del ejemplo más cercano al caso, no inventar un patrón
distinto.
