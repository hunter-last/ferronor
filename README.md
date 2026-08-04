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

## Requisitos previos

- JDK 23
- PostgreSQL instalado y corriendo localmente
- Maven (o usar el wrapper de NetBeans)

## Cómo levantar la base de datos

1. Crea la base de datos vacía:

```sql
   CREATE DATABASE dbferronor WITH ENCODING 'UTF8';
```

2. Ejecuta los scripts en `sql/` **en este orden exacto** sobre `dbferronor`:

   - `01_seguridad.sql`
   - `02_maestros.sql`
   - `03_inventario.sql`
   - `04_compras.sql`
   - `05_ventas.sql`
   - `06_tesoreria.sql`
   - `07_contabilidad.sql`
   - `08_auditoria.sql`
   - `09_indices.sql`
   - `10_datos_iniciales.sql`

3. Verifica que `dbferronor` quedó creada con las tablas de los 8 módulos.

## Configuración de credenciales

**Nunca subas tus credenciales reales al repositorio.**

1. Copia `src/main/resources/config.properties.example` a
   `src/main/resources/config.properties`.
2. Completa `config.properties` con tu usuario y contraseña reales de PostgreSQL,
   apuntando a la base `dbferronor`.
3. Este archivo está en `.gitignore` — no se sube nunca.

## Cómo correr el proyecto

Desde NetBeans: abre el proyecto Maven y ejecuta `Main.java`
(`src/main/java/com/ferronor/sic/pruebas/Main.java`).

Desde línea de comandos:

```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.ferronor.sic.pruebas.Main"
```

## Smoke test

`src/main/java/com/ferronor/sic/pruebas/Main.java` contiene una prueba de humo que
ejercita el flujo completo (Seguridad → Maestros → Inventario) sin dejar datos
persistidos (usa una transacción con rollback automático al final). Ejecútalo
después de levantar la base para confirmar que todo está conectado correctamente.

## Estructura del proyecto

com.ferronor.sic/
├── config/ — configuración y constantes del sistema
├── conexion/ — conexión a PostgreSQL y manejo de transacciones
├── shared/ — clases transversales (RespuestaOperacion, SesionUsuario, ServiceFactory, FrmBase, etc.)
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

Cada módulo de negocio se organiza por **dominio primero**, no por capa global:

modulo/
├── modelo/
│ └── dto/ (solo si el módulo tiene datos derivados, ej. inventario/modelo/dto/KardexItem)
├── dao/
├── logica/
└── vista/

Nunca crear un paquete `modelo/`, `dao/`, `dto/` global a nivel de toda la
aplicación — eso rompe la cohesión que permite entender un módulo completo
mirando una sola carpeta.

## Estado actual del proyecto

**Backend (modelo + DAO + logica) cerrado en los 8 módulos:** Seguridad, Auditoria,
Maestros, Inventario, Compras, Ventas, Tesorería y Contabilidad, más los 4
coordinadores de `procesos/` (ProcesoVenta, ProcesoCompra, ProcesoCobroCliente,
ProcesoPagoProveedor).

**Pendiente: capa de vista (Swing).** Por ahora solo existen `FrmLogin.java`
(seguridad) y `FrmCategoria.java` (maestros, plantilla oficial de formularios —
ver `ARQUITECTURA.md` sección 3.1). El resto de pantallas de cada módulo está
por construirse siguiendo ese mismo patrón.

Ver `ARQUITECTURA.md` para el detalle de diseño y las reglas de arquitectura del
proyecto, y `CONTRIBUTING.md` para las reglas de ramas y PR del equipo.