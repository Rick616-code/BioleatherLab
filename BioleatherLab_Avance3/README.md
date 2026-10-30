# Bioleather Lab — Avance 3

Prototipo académico para gestionar un catálogo de accesorios conceptuales, cuentas de clientes y solicitudes de pedidos. La aplicación incluye las alternativas A (web básica), B (vista móvil web y proyecto Android independiente) y C (web integral). Los productos, fotografías y precios son ilustrativos. No procesa pagos ni confirma entregas reales.

## Tecnologías y arquitectura

- **Servidor:** Java 17 (versión objetivo), Spring Boot 3.5.16, Maven y JDBC.
- **Frontend web:** HTML, CSS y JavaScript, servido por Spring Boot.
- **Datos:** H2 persistente en el entorno local; perfil MySQL opcional (no validado en las pruebas realizadas).
- **Seguridad:** BCrypt para contraseñas, sesiones temporales en memoria, autorización por roles, validación de entradas y cabeceras HTTP.
- **Librerías:** Google Guava (`ImmutableSet`), Apache Commons Lang (`StringUtils`), Apache POI (exportación XLSX) y Logback (registro de eventos).

El backend distribuye responsabilidades entre controladores REST (`controller/`), lógica de negocio (`service/`) y acceso a datos (`dao/`); los reportes están aislados en `reports/` y la gestión de sesiones en `security/`. Esta separación permite explicar MVC en un contexto REST, el patrón DAO y el principio de responsabilidad única de SOLID. Contar con pruebas no implica, por sí solo, haber seguido TDD.

```text
servidor/src/main/java/pe/bioleather/
├── BioleatherApplication.java
├── api/
├── controller/
├── service/
├── dao/
├── reports/
├── security/
└── config/
```

## Funcionalidades

| Alternativa | Pantallas previstas | Funciones principales |
|---|---|---|
| A — Web básica | A1–A5 | Catálogo, ficha, registro, solicitud y consulta por código. |
| B — Vista móvil web | B1–B5 | Inicio, catálogo, ficha, nueva solicitud y pedidos. La carpeta `android-b/` contiene un proyecto nativo Java independiente. |
| C — Web integral | C1–C8 | Registro y acceso, catálogo y filtros, ficha, carrito, historial, gestión de productos, pedidos y clientes. |

Los clientes pueden registrarse, iniciar sesión, crear solicitudes, consultar el historial y seguir los estados de sus pedidos. La administración permite gestionar productos y clientes, consultar y cambiar estados de pedidos, y exportar reportes Excel. El total del pedido se calcula en el servidor a partir de los precios almacenados.

## Historial de desarrollo

### Fase 1 — Funcionalidad y librerías

- Se centralizaron las sesiones en `security/AuthSessionService.java` y se eliminaron credenciales administrativas fijas de la configuración de VS Code.
- Se implementó la exportación de pedidos a Excel mediante `reports/ReporteController.java`, `PedidoReporteService.java` y `PedidoReporteDAO.java` con Apache POI.
- Se incorporaron Guava para valores permitidos, Apache Commons Lang para validaciones y Logback para registros.
- Se corrigió la edición de productos desactivados y se añadieron pruebas de integración de catálogo, autenticación y reportes.
- En la ejecución local de esta fase se corrigieron errores de compilación; finalizaron **4 pruebas aprobadas**.

### Fase 2 — Separación de responsabilidades

- El controlador centralizado se reorganizó en `AuthController`, `ProductController`, `ClientController` y `OrderController`.
- Se separaron los servicios `AuthService`, `ProductService`, `ClientService` y `OrderService`, junto a los DAO de usuarios, productos y pedidos.
- Se mantuvieron las rutas `/api/...`, las interfaces existentes y el almacenamiento de pedidos.
- Se amplió la batería de pruebas; resultado reportado: **7 pruebas aprobadas** con `BUILD SUCCESS`.

### Fase 3 — Seguridad y pruebas

- Se añadieron cabeceras de protección mediante `security/SecurityHeadersFilter.java` y se mejoraron las validaciones de datos de clientes.
- Se agregaron pruebas de cierre de sesión, aislamiento de pedidos entre clientes, bloqueo de cuentas desactivadas, validación de correos duplicados y cabeceras HTTP.
- Resultado comprobado en VS Code: **12 pruebas aprobadas, 0 fallos, 0 errores y 0 omitidas** al ejecutar `mvn clean test`.
- En el navegador se verificaron el registro, inicio y cierre de sesión, catálogo, fichas, carrito, pedidos, persistencia tras reiniciar, administración y descarga de Excel. Las vistas web A y B también se abrieron y sus funciones probadas respondieron.

La aplicación Android de `android-b/` se conserva sin modificaciones. Su funcionamiento fue comunicado por un compañero, pero no se verificó directamente en estas pruebas.

## Instalación y ejecución

Requisitos para el servidor: JDK 17 o superior, Maven y un navegador. En una terminal ubicada en `servidor/`:

```bash
mvn clean test
mvn spring-boot:run
```

Si la terminal está en la carpeta principal, ejecuta antes `cd servidor`. Mantén abierta la terminal del servidor durante la prueba.

| Interfaz | Dirección local |
|---|---|
| Selector de alternativas | `http://localhost:8080/alternativas.html` |
| A — Web básica | `http://localhost:8080/?modo=A` |
| B — Vista móvil web | `http://localhost:8080/?modo=B` |
| C — Web integral | `http://localhost:8080/?modo=C` |

Para crear un administrador de demostración, detén el servidor y define estas variables **antes de iniciarlo** en la misma terminal de Linux:

```bash
export ADMIN_EMAIL='admin@ejemplo.test'
read -rsp 'Contraseña de administrador: ' ADMIN_PASSWORD
echo
export ADMIN_PASSWORD
mvn spring-boot:run
```

La contraseña no se muestra al escribirla. Si ese correo ya está registrado como administrador en la base, el inicio no reemplaza automáticamente su contraseña. Utiliza credenciales y datos ficticios; nunca publiques contraseñas reales. Por defecto el servidor escucha en `127.0.0.1`.

La base H2 local se conserva entre ejecuciones en `servidor/data/`. Para utilizar MySQL se requiere crear la base, configurar las variables `DB_URL`, `DB_USER` y `DB_PASSWORD`, y activar el perfil `mysql`; es una configuración opcional que no se ha verificado en las demostraciones.

La alternativa B del navegador funciona sin Android Studio. El proyecto nativo `android-b/` requiere Android SDK y herramientas Gradle compatibles si se desea compilarlo por separado; no se incluye APK ni Gradle Wrapper.

## API y pruebas

- **Pública:** `GET /api/products`, `GET /api/products/{id}`, `POST /api/auth/register`, `POST /api/auth/login`.
- **Cliente autenticado:** `POST /api/orders`, `GET /api/orders/mine`, `GET /api/orders/code/{codigo}`, `GET /api/orders/{id}/history`.
- **Administrador:** `GET/POST/PUT /api/admin/products`, `GET/PUT /api/admin/clients`, `GET /api/admin/orders`, `PUT /api/admin/orders/{id}/state`.
- **Reporte Excel:** `GET /api/admin/reports/orders.xlsx` (solo administrador).

Las rutas protegidas utilizan `Authorization: Bearer <token>`. Las pruebas automatizadas están en `servidor/src/test/java/pe/bioleather/ApiIntegracionTest.java` y se ejecutan con `mvn clean test`. La comprobación de 12 pruebas corresponde a la ejecución reportada en el equipo del estudiante; las pruebas manuales se documentaron por separado.

## Pendientes para la entrega

- Actualizar el informe con la arquitectura implementada, una matriz de cobertura RF01–RF08 y capturas de las funciones probadas.
- Incluir evidencias concretas del uso de Guava, Commons Lang, POI y Logback.
- Documentar pruebas no funcionales: rendimiento, compatibilidad y respaldos; revisar el contenido del Excel frente a los pedidos mostrados.
- Diferenciar la base H2 utilizada de la opción MySQL, aún sin validar; justificar cómo se evalúa el porcentaje de Java.
- Confirmar con el docente la consigna aplicable sobre cantidad de pantallas: el alcance actual refleja A1–A5, B1–B5 y C1–C8, mientras que otro documento del curso menciona diez pantallas por alternativa.
- Preparar las evidencias y la sustentación; realizar la publicación en GitHub al final con un historial auténtico, sin incluir credenciales ni datos privados.

Este proyecto es un prototipo local, no una implementación aceptada por la empresa ni preparada para producción. Antes de un despliegue real habría que validar los procesos y reforzar HTTPS, gestión de secretos, concurrencia, protección contra abuso, pruebas y copias de seguridad.
