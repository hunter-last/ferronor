\# Guía de contribución — Sistema Decor Home Ferronor



Este documento define cómo trabajamos como equipo en este repositorio. Léelo

completo antes de tu primer commit.



\## Flujo de trabajo con Git



1\. \*\*Nunca trabajar directamente sobre `main`.\*\* `main` siempre debe reflejar

&#x20;  código que compila y pasa el smoke test.

2\. Crea una rama por tarea, con el prefijo según el tipo de cambio:



feature/compras-registrar-orden

feature/ventas-registrar-venta

fix/kardex-saldo-inicial

docs/actualizar-readme



3\. Haz commits pequeños y descriptivos. Preferible:



feat(compras): agregar CompraDAO con RETURNING

fix(ventas): corregir validación de forma de pago



a un solo commit gigante de "avance del día".

4\. Antes de subir tu rama, verifica que el proyecto \*\*compila\*\* y que el

&#x20;  smoke test (`com.ferronor.sic.pruebas.Main`) sigue pasando.

5\. Abre un Pull Request hacia `main`. Describe brevemente qué módulo o clase

&#x20;  tocaste y por qué.

6\. Todo PR necesita al menos una revisión antes de mezclarse — aunque seamos

&#x20;  pocos, revisar el código del otro ayuda a detectar errores de patrón temprano

&#x20;  (por ejemplo, olvidar `RETURNING` o mezclar `Vista → DAO` directo).



\## Antes de escribir código nuevo



1\. Lee `ARQUITECTURA.md` completo. Las 17 reglas ahí descritas no son

&#x20;  opcionales — existen porque ya se detectaron y corrigieron los problemas

&#x20;  que buscan evitar.

2\. Mira el módulo `inventario/` como plantilla de referencia. Si tienes duda

&#x20;  de cómo debería verse un DAO o un Service nuevo, cópiale la forma a

&#x20;  `StockDAOImpl`/`InventarioServiceImpl`, no inventes un patrón distinto.

3\. Si tu módulo necesita una tabla, columna, o cambio en el DDL que \*\*no\*\*

&#x20;  está ya en `database/`, \*\*coordina con el equipo antes de modificarlo\*\*. El

&#x20;  modelo de datos está congelado y validado — un cambio sin avisar puede

&#x20;  romper el trabajo de otro módulo que ya depende de la tabla actual.



\## Reglas de código (resumen — el detalle completo está en ARQUITECTURA.md)



\- Nunca `Vista → DAO` directo. Siempre `Vista → Service → DAO → BD`.

\- Nunca excepciones para errores de negocio — usa `RespuestaOperacion<T>`.

\- Nunca `Statement.RETURN\_GENERATED\_KEYS` — usa `RETURNING` en el SQL.

\- Nunca abrir/cerrar `Connection` manualmente en un `Service` — usa

&#x20; `TransactionManager.iniciar()` con `try-with-resources`.

\- Nunca credenciales de base de datos en el código — usa `Configuracion`.



\## Seguridad y datos sensibles



\- \*\*Nunca\*\* subas `config.properties` (tiene tu contraseña real de

&#x20; PostgreSQL). Ya está en `.gitignore`, pero verifícalo si `git status` te lo

&#x20; muestra como cambio.

\- Si por error subiste una credencial real a un commit, avisa al equipo de

&#x20; inmediato — hay que rotar la contraseña, no basta con borrar el archivo en

&#x20; un commit posterior (el historial de Git la conserva).



\## Reparto de módulos actual



| Módulo | Responsable |

|---|---|

| Inventario, Maestros, Seguridad | Jeferson (ya completos, sirven de plantilla) |

| Compras, Ventas, Tesorería | Rafael |

| Contabilidad, Procesos (`procesos/`) | Jeferson |

| DER, diccionario de datos, documentación de análisis | Juan Diego |



\## Preguntas



Si algo de `ARQUITECTURA.md` no queda claro, o encuentras un caso que las

reglas no cubren explícitamente, coordínalo con el equipo antes de decidir

por tu cuenta — es más barato resolver una duda en 5 minutos de chat que

deshacer un patrón inconsistente después.

