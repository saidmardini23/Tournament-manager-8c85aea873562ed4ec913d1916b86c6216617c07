# Los Paisas — Sistema de Gestión de Torneos de Fútbol

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-views-005F0F?logo=thymeleaf&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-database-4479A1?logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-build-C71A36?logo=apachemaven&logoColor=white)

Aplicación web para administrar torneos de fútbol amateur: torneos, equipos, jugadores, árbitros, jornadas y partidos, con registro evento a evento de cada partido y generación automática de marcadores, estadísticas de jugadores y tablas de posiciones.

Todo vive en **un solo proyecto Spring Boot**: la interfaz web (Thymeleaf + HTML + CSS) y la API REST se sirven desde la misma aplicación, con un único comando de arranque. No hay frontend separado, ni Node.js, ni un segundo servidor.

Repositorio: **[github.com/3rwinxd/Tournament-manager](https://github.com/3rwinxd/Tournament-manager)**

---

## Contenido

- [¿Qué es?](#qué-es)
- [Características](#características)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Requisitos](#requisitos)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Interfaz web](#interfaz-web)
- [API REST](#api-rest)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Documentación](#documentación)
- [Pruebas](#pruebas)
- [Limitaciones conocidas](#limitaciones-conocidas)
- [Estado del proyecto y próximos pasos](#estado-del-proyecto-y-próximos-pasos)

---

## ¿Qué es?

**Los Paisas** centraliza la operación de un torneo: desde crear el torneo y sus equipos hasta registrar cada gol, asistencia o tarjeta de un partido. El sistema actualiza solo el marcador, las estadísticas individuales y la tabla de posiciones.

La aplicación tiene dos puertas de entrada que comparten exactamente la misma lógica de negocio:

- **Interfaz web** (Thymeleaf + HTML + CSS): páginas renderizadas en el servidor para que el organizador gestione el torneo desde el navegador.
- **API REST** (JSON): endpoints bajo `/api` para consumir o poblar los datos desde otras herramientas, como Postman.

---

## Características

**Interfaz web**

- **Dashboard** de administración con los totales de torneos, equipos, jugadores y partidos, y los próximos partidos programados.
- **Torneos**: listado con búsqueda, creación, edición y eliminación; menú del torneo con sus equipos y su tabla de posiciones.
- **Fixture**: jornadas con sus partidos, filtrables por estado y por equipo.
- **Rankings**: tabla de posiciones, goleadores y asistentes del torneo, con filtro por equipo.
- **Equipos**: CRUD, plantilla de jugadores con búsqueda, goleador y asistente líder del equipo.
- **Jugadores**: CRUD y ficha con sus estadísticas.
- **Árbitros**: listado con búsqueda y CRUD.
- **Manejo de errores amigable**: los errores de negocio se muestran como mensajes en la propia página; nunca se devuelve JSON al navegador.

**Lógica de negocio (disponible vía web y API)**

- **Partidos**: creación, cambio de estado, actualización de marcador, y relación con jornada, equipos y árbitro.
- **Eventos de partido**: goles (de jugada, cabeza, penal, tiro libre), autogoles, asistencias, tarjetas, sustituciones, lesiones y más. Al editar o eliminar un evento se revierten automáticamente sus efectos.
- **Estadísticas de jugadores**: goles, asistencias, tarjetas y partidos jugados, con rankings de goleadores y asistencias.
- **Tabla de posiciones**: cada equipo aparece desde que se registra y se actualiza cuando un partido pasa a `FINALIZADO` (3 puntos por victoria, 1 por empate).
- **Reglas de integridad**: el torneo de un equipo no se puede cambiar y un equipo con partidos no se puede eliminar.
- **Validación de entrada**: los Request validan longitudes, campos obligatorios y rangos antes de llegar a la lógica de negocio.
- **Respuestas controladas**: la API devuelve DTOs, no entidades; por ejemplo, la contraseña de un usuario nunca se devuelve.

Al registrar un gol, el sistema actualiza automáticamente el marcador del partido y la estadística del jugador. Al finalizar un partido por primera vez, actualiza la tabla de posiciones y registra el partido jugado de los jugadores de ambos equipos.

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1.1 |
| Vistas | Thymeleaf + HTML + CSS (renderizado en el servidor, dentro de la misma aplicación) |
| Persistencia | Spring Data JPA / Hibernate |
| Validación | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Base de datos | MySQL |
| Build | Maven (`mvnw`) |
| Utilidades | Lombok |
| Pruebas de API | Postman |
| Control de versiones | Git |

---

## Arquitectura

Una sola aplicación, dos puertas de entrada y una única capa de servicios:

```text
Navegador (HTML)                         Cliente de API (JSON)
      ↓                                          ↓
Controller web (@Controller)             Controller REST (@RestController)
arma el Request, llena el Model          recibe XRequest (@Valid)
y devuelve plantilla Thymeleaf           y devuelve XResponse
      ↓                                          ↓
      └──────────────────┬───────────────────────┘
                         ↓
                  Service (interfaz)
                         ↓
                  ServiceImpl        ← lógica de negocio; trabaja con entidades y arma el Response con DtoMapper
                         ↓
                  Repository
                         ↓
                  Base de datos (MySQL)

Transversal: dto/ (Request, Response, DtoMapper) y exceptions/ (GlobalExceptionHandler para REST, VistaExceptionHandler para vistas)
```

- **Controller web** (`controllers/web`): atiende las rutas de la interfaz, pone datos en el `Model` y devuelve el nombre de una plantilla o un `redirect`. No contiene reglas de negocio.
- **Controller REST** (`controllers`): expone los endpoints bajo `/api`. Recibe **Requests** validados y devuelve **Responses**; nunca expone entidades.
- **DTOs** (`dto/`): `request/` define lo que entra (con sus validaciones), `response/` lo que sale, y `DtoMapper` convierte entidades en Responses. Ver [`DTOS.md`](/lospaisas/docs/DTOS.md).
- **Service / ServiceImpl**: contrato y lógica de negocio de cada dominio. Algunos services colaboran entre sí (por ejemplo, `PartidoServiceImpl` usa `TablaPosicionService` y `EstadisticaJugadorService` para mantener consistentes los datos derivados de un partido).
- **Repository**: acceso a datos con `JpaRepository`.
- **Entity**: modelo del dominio (`Torneo`, `Equipo`, `Jugador`, `Partido`, `EventoPartido`, `EstadisticaJugador`, `TablaPosicion`, `Usuario`, `Rol`, `Arbitro`, `Jornada`) y sus enumerados. No se expone directamente.
- **Excepciones**: cada tipo de controller tiene su handler. `GlobalExceptionHandler` responde JSON a la API y `VistaExceptionHandler` redirige a la página de origen con un mensaje. Ver [`EXEPTION.md`](/lospaisas/docs/EXEPTION.md).

El modelo de dominio está descrito en [`CLASS_DIAGRAM.md`](/lospaisas/docs/CLASS_DIAGRAM.md) y el esquema de base de datos en [`DATABASE.md`](/lospaisas/docs/DATABASE.md).

---

## Requisitos

- Java JDK 21 o superior
- MySQL
- Maven (opcional: el proyecto incluye el wrapper `./mvnw`)
- Un navegador web moderno
- Postman, si se desea probar la API manualmente

No se necesita Node.js ni ninguna herramienta de frontend.

---

## Instalación y ejecución

**1. Clonar el proyecto**

```bash
git clone https://github.com/3rwinxd/Tournament-manager.git
cd Tournament-manager/lospaisas
```

**2. Configurar el entorno**

El repositorio incluye una plantilla `application.properties.example` con las variables disponibles. Cópiala y edítala con tus valores locales:

```bash
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
```

El archivo `application.properties` está en `.gitignore` y nunca se sube al repo.

**3. Crear la base de datos**

```sql
CREATE DATABASE lospaisas_db;
```

Configura la conexión en `application.properties`:

```text
jdbc:mysql://localhost:3306/lospaisas_db
```

**4. Ejecutar**

```bash
./mvnw spring-boot:run
```

En Windows: `mvnw.cmd spring-boot:run`.

Un solo comando levanta la interfaz web y la API:

| Qué | URL |
|---|---|
| Página de inicio | `http://localhost:8080/` |
| Panel de administración | `http://localhost:8080/admin` |
| API REST | `http://localhost:8080/api` |

---

## Interfaz web

Todas las rutas de gestión están disponibles también bajo el prefijo `/admin` (por ejemplo, `/torneos` y `/admin/torneos` muestran lo mismo).

| Sección | Rutas principales | Qué permite |
|---|---|---|
| Inicio | `/` | Página de entrada |
| Dashboard | `/admin`, `/admin/dashboard` | Totales del sistema y próximos partidos |
| Torneos | `/torneos`, `/torneos/{id}` | Listado con búsqueda, menú del torneo, equipos y tabla de posiciones |
| | `/torneos/{id}/fixture` | Jornadas y partidos, con filtro por estado y equipo |
| | `/torneos/{id}/rankings` | Posiciones, goleadores y asistentes |
| | `/torneos/nuevo`, `/torneos/{id}/editar`, `/torneos/{id}/opciones` | Crear, editar y administrar un torneo |
| Equipos | `/equipos/{id}` | Plantilla, goleador y asistente líder |
| | `/equipos/{id}/goleadores`, `/equipos/{id}/asistentes` | Rankings del equipo |
| | `/equipos/nuevo`, `/equipos/{id}/editar` | Crear y editar (el torneo de un equipo no se puede cambiar) |
| Jugadores | `/jugadores/{id}` | Ficha del jugador con sus estadísticas |
| | `/jugadores/nuevo`, `/jugadores/{id}/editar` | Crear y editar |
| Árbitros | `/arbitros`, `/arbitros/{id}` | Listado con búsqueda y ficha |
| | `/arbitros/nuevo`, `/arbitros/{id}/editar` | Crear y editar |

Las eliminaciones pasan por una página de confirmación (`/{id}/confirmar-eliminar`). Si una regla de negocio lo impide, el mensaje se muestra en pantalla y no se pierde ningún dato.

> Hoy la gestión de **jornadas, partidos y eventos de partido** se hace a través de la API REST; la interfaz web los muestra (fixture, rankings, dashboard) pero aún no tiene formularios para crearlos.

---

## API REST

La API está organizada por recursos:

```text
/api/torneos
/api/equipos
/api/jugadores
/api/usuarios
/api/roles
/api/arbitros
/api/jornadas
/api/partidos
/api/eventos-partido
/api/estadisticas-jugadores
/api/tabla-posiciones
```

Puntos clave para quien la consuma:

- Los cuerpos de las peticiones son **JSON** y las relaciones se envían como **ID** (`"torneoId": "..."`), no como objetos anidados.
- Las operaciones de estado y marcador de partidos, el registro de eventos y la creación de jornadas usan **body JSON**, no parámetros en la URL.
- Los **enums** deben enviarse en mayúsculas exactas (`FINALIZADO`, `GOL_JUGADA`).
- Los errores tienen un formato uniforme: `{ "error": "mensaje" }`, o `{ "campo": "mensaje" }` cuando falla una validación. Códigos usados: `400`, `404`, `409`, `500`.

Ejemplo, crear un equipo:

```http
POST /api/equipos
Content-Type: application/json

{
  "nombre": "Atlético Paisas",
  "entrenador": "Carlos Gómez",
  "torneoId": "3f2b8c1e-5a4d-4e7f-9b21-0c6d8a1e4f10"
}
```

El detalle completo de cada endpoint está en [`ENDPOINTS.md`](/lospaisas/docs/ENDPOINTS.md).

---

## Estructura del proyecto

Un único proyecto Maven (`lospaisas/`) que contiene tanto el backend como las vistas:

```text
Tournament-manager/
└── lospaisas/
    ├── docs/                           # Documentación técnica
    ├── pom.xml
    └── src/main/
        ├── java/com/lospaisas/
        │   ├── controllers/            # Endpoints REST (@RestController)
        │   │   └── web/                # Controllers de la interfaz (@Controller)
        │   ├── dto/
        │   │   ├── request/            # Datos de entrada, con validaciones
        │   │   ├── response/           # Datos de salida
        │   │   └── DtoMapper.java      # Conversión entidad -> Response
        │   ├── exceptions/             # GlobalExceptionHandler, VistaExceptionHandler y excepciones propias
        │   ├── services/               # Interfaces de lógica de negocio
        │   │   └── impl/               # Implementaciones
        │   ├── repositories/           # Acceso a datos (Spring Data JPA)
        │   └── models/
        │       ├── entity/             # Entidades del sistema
        │       └── enums/              # Estados, posiciones, roles, tipos de evento
        └── resources/
            ├── static/                 # CSS (app.css) e imágenes
            ├── templates/              # Plantillas Thymeleaf (layout, fragments y una carpeta por módulo)
            ├── application.properties.example
            └── application.properties  # Local, fuera de git
```

Ver **[`STRUCTURE.md`](/lospaisas/docs/STRUCTURE.md)** para el árbol completo.

---

## Documentación

| Documento | Contenido |
|---|---|
| [`STRUCTURE.md`](/lospaisas/docs/STRUCTURE.md) | Árbol completo del proyecto y convenciones |
| [`CLASS_DIAGRAM.md`](/lospaisas/docs/CLASS_DIAGRAM.md) | Diagrama de clases del dominio y su correspondencia con el código |
| [`DATABASE.md`](/lospaisas/docs/DATABASE.md) | Modelo de base de datos |
| [`ENDPOINTS.md`](/lospaisas/docs/ENDPOINTS.md) | Métodos HTTP, rutas, body de ejemplo, respuestas, códigos de estado y formato de errores |
| [`DTOS.md`](/lospaisas/docs/DTOS.md) | Requests y Responses: campos, validaciones, `DtoMapper` y cómo agregar un DTO nuevo |
| [`SERVICES.md`](/lospaisas/docs/SERVICES.md) | Lógica de negocio de cada Service y convenciones de la capa de servicio |
| [`EXEPTION.md`](/lospaisas/docs/EXEPTION.md) | Manejo de excepciones en API y vistas |

---

## Pruebas

Durante el desarrollo se hicieron pruebas manuales con **Postman** sobre la API y con el navegador sobre la interfaz web: creación y consulta de torneos, equipos, jugadores y árbitros; creación de partidos; registro y actualización de eventos con reversión de efectos; y actualización automática de marcador, estadísticas y tabla de posiciones.

Flujo de prueba representativo:

```text
Registrar GOL_JUGADA
        ↓
Actualizar marcador
        ↓
Actualizar estadística del jugador
        ↓
Modificar el evento
        ↓
Revertir los efectos anteriores
        ↓
Aplicar los nuevos efectos
        ↓
Eliminar el evento
        ↓
Revertir nuevamente sus efectos
```

> Las colecciones de Postman deben usar **body JSON** en los endpoints que antes recibían parámetros en la URL (ver el resumen de cambios al final de `ENDPOINTS.md`).

A nivel de pruebas automatizadas, el proyecto cuenta por ahora únicamente con el test de contexto de Spring Boot (`LospaisasApplicationTests`), que se ejecuta con `./mvnw test`.

---

## Limitaciones conocidas

- **Sin seguridad por ahora.** Por indicación del proyecto no se utiliza Spring Security ni ningún framework de seguridad: no hay autenticación ni autorización, y la contraseña de los usuarios se guarda tal como llega, sin hashing. Cualquier cliente puede crear usuarios con cualquier rol. Debe resolverse antes de un despliegue real (detalle en `DTOS.md`, sección 8).
- **Jugadores creados desde la interfaz.** Al crear un jugador desde la web se genera por detrás un usuario interno con un correo autogenerado y una contraseña por defecto.
- **Recálculo tras finalizar un partido.** Editar o eliminar un evento de un partido ya `FINALIZADO` actualiza marcador y estadísticas, pero no recalcula la tabla de posiciones ni `partidos_jugados`.
- **Partidos jugados.** Se suma un partido jugado a todos los jugadores de ambos equipos, porque el modelo aún no maneja alineaciones.
- **Reglas de integridad pendientes.** Aún no hay bloqueo explícito para eliminar un torneo con equipos o jornadas, ni un equipo con jugadores (ver `EXEPTION.md`).
- **Pruebas automatizadas.** Solo existe el test de contexto de Spring Boot.

---

## Estado del proyecto y próximos pasos

El **backend** tiene implementadas y probadas las funcionalidades principales: gestión de los recursos, lógica de partidos, eventos, marcadores, estadísticas y tabla de posiciones, con servicios separados en interfaces e implementaciones, DTOs con validación de entrada y manejo centralizado de excepciones.

La **interfaz web** ya cubre el dashboard, torneos (con fixture y rankings), equipos, jugadores y árbitros, integrada en la misma aplicación.

Próximos pasos:

- Vistas web para crear y administrar jornadas, partidos y eventos de partido.
- Autenticación y autorización, con hashing de contraseñas.
- Completar las reglas de integridad pendientes y recalcular la tabla al modificar partidos finalizados.
- Ampliar las pruebas automatizadas.