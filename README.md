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
