# Estructura del Proyecto — `lospaisas`

Este documento detalla la organización completa del código fuente y directorio de archivos del sistema **lospaisas**. La aplicación es una solución monolítica desarrollada con **Spring Boot** para la administración de torneos de fútbol, la cual integra dos capas de presentación sobre los mismos servicios de negocio:

1. **API REST JSON (`/api/**`):** Endpoints desacoplados interceptados por `GlobalExceptionHandler`.
2. **Interfaz Web Thymeleaf (`/torneos`, `/equipos`, etc.):** Renderizado dinámico desde el servidor interceptado por `VistaExceptionHandler`.

---

## 1. Diagrama de Arquitectura en Capas

```text
Cliente HTTP (API REST JSON)                Navegador Web (HTML Thymeleaf)
            │                                             │
            ▼                                             ▼
  @RestController (api/**)                     @Controller (web/**)
   recibe Request (@Valid)                      construye Request (@ModelAttribute)
   devuelve Response JSON                       devuelve plantilla HTML / Redirect
            │                                             │
            └──────────────────────┬──────────────────────┘
                                   │
                                   ▼
                         Service (Interfaz)
                                   │
                                   ▼
                        ServiceImpl (Lógica)
                        ├── Reglas de negocio & Transacciones (@Transactional)
                        ├── Inyección de Repositories
                        └── DtoMapper (Entidad ↔ Response)
                                   │
                                   ▼
                              Repository
                                   │
                                   ▼
                     Entidad JPA (MySQL / H2)
```

---

## 2. Árbol de Directorios del Repositorio

```text
Tournament-manager/
│-- .github/
│   └── modernize/java-upgrade/hooks/scripts/
│       ├── recordToolUse.ps1
│       └── recordToolUse.sh
│-- .vscode/
│   └── settings.json
│-- lospaisas/
│   ├── .github/
│   │   └── modernize/java-upgrade/hooks/scripts/
│   │       ├── recordToolUse.ps1
│   │       └── recordToolUse.sh
│   ├── .mvn/
│   │   └── wrapper/
│   │       └── maven-wrapper.properties
│   ├── docs/
│   │   ├── CLASS_DIAGRAM.md
│   │   ├── DATABASE.md
│   │   ├── DTOS.md
│   │   ├── ENDPOINTS.md
│   │   ├── EXEPTION.md
│   │   ├── SERVICES.md
│   │   └── STRUCTURE.md
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lospaisas/
│   │   │   │   ├── controllers/
│   │   │   │   │   ├── web/
│   │   │   │   │   │   ├── ArbitroWebController.java
│   │   │   │   │   │   ├── DashboardWebController.java
│   │   │   │   │   │   ├── EquipoWebController.java
│   │   │   │   │   │   ├── JugadorWebController.java
│   │   │   │   │   │   └── TorneoWebController.java
│   │   │   │   │   ├── ArbitroController.java
│   │   │   │   │   ├── EquipoController.java
│   │   │   │   │   ├── EstadisticaJugadorController.java
│   │   │   │   │   ├── EventoPartidoController.java
│   │   │   │   │   ├── JornadaController.java
│   │   │   │   │   ├── JugadorController.java
│   │   │   │   │   ├── PartidoController.java
│   │   │   │   │   ├── RolController.java
│   │   │   │   │   ├── TablaPosicionController.java
│   │   │   │   │   ├── TorneoController.java
│   │   │   │   │   └── UsuarioController.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── request/
│   │   │   │   │   │   ├── ArbitroRequest.java
│   │   │   │   │   │   ├── CambiarEstadoRequest.java
│   │   │   │   │   │   ├── EquipoRequest.java
│   │   │   │   │   │   ├── EventoPartidoRequest.java
│   │   │   │   │   │   ├── EventoPartidoUpdateRequest.java
│   │   │   │   │   │   ├── JornadaRequest.java
│   │   │   │   │   │   ├── JugadorRequest.java
│   │   │   │   │   │   ├── MarcadorRequest.java
│   │   │   │   │   │   ├── PartidoRequest.java
│   │   │   │   │   │   ├── RolRequest.java
│   │   │   │   │   │   ├── TorneoRequest.java
│   │   │   │   │   │   ├── UsuarioRequest.java
│   │   │   │   │   │   └── UsuarioUpdateRequest.java
│   │   │   │   │   ├── response/
│   │   │   │   │   │   ├── ArbitroResponse.java
│   │   │   │   │   │   ├── EquipoResponse.java
│   │   │   │   │   │   ├── EstadisticaJugadorResponse.java
│   │   │   │   │   │   ├── EventoPartidoResponse.java
│   │   │   │   │   │   ├── JornadaFixtureResponse.java
│   │   │   │   │   │   ├── JornadaResponse.java
│   │   │   │   │   │   ├── JugadorResponse.java
│   │   │   │   │   │   ├── PartidoResponse.java
│   │   │   │   │   │   ├── RolResponse.java
│   │   │   │   │   │   ├── TablaPosicionResponse.java
│   │   │   │   │   │   ├── TorneoResponse.java
│   │   │   │   │   │   └── UsuarioResponse.java
│   │   │   │   │   └── DtoMapper.java
│   │   │   │   ├── exceptions/
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   ├── RecursoNoEncontradoException.java
│   │   │   │   │   ├── ValidacionException.java
│   │   │   │   │   └── VistaExceptionHandler.java
│   │   │   │   ├── models/
│   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── Arbitro.java
│   │   │   │   │   │   ├── Equipo.java
│   │   │   │   │   │   ├── EstadisticaJugador.java
│   │   │   │   │   │   ├── EventoPartido.java
│   │   │   │   │   │   ├── Jornada.java
│   │   │   │   │   │   ├── Jugador.java
│   │   │   │   │   │   ├── Partido.java
│   │   │   │   │   │   ├── Rol.java
│   │   │   │   │   │   ├── TablaPosicion.java
│   │   │   │   │   │   ├── Torneo.java
│   │   │   │   │   │   └── Usuario.java
│   │   │   │   │   └── enums/
│   │   │   │   │       ├── EstadoPartido.java
│   │   │   │   │       ├── Posiciones.java
│   │   │   │   │       ├── Roles.java
│   │   │   │   │       └── TipoEvento.java
│   │   │   │   ├── repositories/
│   │   │   │   │   ├── ArbitroRepository.java
│   │   │   │   │   ├── EquipoRepository.java
│   │   │   │   │   ├── EstadisticaJugadorRepository.java
│   │   │   │   │   ├── EventoPartidoRepository.java
│   │   │   │   │   ├── JornadaRepository.java
│   │   │   │   │   ├── JugadorRepository.java
│   │   │   │   │   ├── PartidoRepository.java
│   │   │   │   │   ├── RolRepository.java
│   │   │   │   │   ├── TablaPosicionRepository.java
│   │   │   │   │   ├── TorneoRepository.java
│   │   │   │   │   └── UsuarioRepository.java
│   │   │   │   ├── services/
│   │   │   │   │   ├── impl/
│   │   │   │   │   │   ├── ArbitroServiceImpl.java
│   │   │   │   │   │   ├── EquipoServiceImpl.java
│   │   │   │   │   │   ├── EstadisticaJugadorServiceImpl.java
│   │   │   │   │   │   ├── EventoPartidoServiceImpl.java
│   │   │   │   │   │   ├── JornadaServiceImpl.java
│   │   │   │   │   │   ├── JugadorServiceImpl.java
│   │   │   │   │   │   ├── PartidoServiceImpl.java
│   │   │   │   │   │   ├── RolServiceImpl.java
│   │   │   │   │   │   ├── TablaPosicionServiceImpl.java
│   │   │   │   │   │   ├── TorneoServiceImpl.java
│   │   │   │   │   │   └── UsuarioServiceImpl.java
│   │   │   │   │   ├── ArbitroService.java
│   │   │   │   │   ├── EquipoService.java
│   │   │   │   │   ├── EstadisticaJugadorService.java
│   │   │   │   │   ├── EventoPartidoService.java
│   │   │   │   │   ├── JornadaService.java
│   │   │   │   │   ├── JugadorService.java
│   │   │   │   │   ├── PartidoService.java
│   │   │   │   │   ├── RolService.java
│   │   │   │   │   ├── TablaPosicionService.java
│   │   │   │   │   ├── TorneoService.java
│   │   │   │   │   └── UsuarioService.java
│   │   │   │   └── LospaisasApplication.java
│   │   │   └── resources/
│   │   │       ├── static/
│   │   │       │   ├── css/app.css
│   │   │       │   └── img/logo.svg
│   │   │       ├── templates/
│   │   │       │   ├── admin/dashboard.html
│   │   │       │   ├── arbitro/
│   │   │       │   ├── equipo/
│   │   │       │   ├── fragments/
│   │   │       │   ├── jugador/
│   │   │       │   ├── layout/
│   │   │       │   ├── torneo/
│   │   │       │   ├── error.html
│   │   │       │   └── index.html
│   │   │       ├── application.properties
│   │   │       └── application.properties.example
│   │   └── test/java/com/lospaisas/LospaisasApplicationTests.java
│   ├── .gitattributes
│   ├── mvnw / mvnw.cmd
│   └── pom.xml
│-- .gitignore
└── README.md
```

---

## 3. Descripción de Paquetes (`src/main/java/com/lospaisas/`)

### 3.1 `controllers/` — API REST

Contiene controladores anotados con `@RestController` mapeados bajo `/api/v1/...`. Reciben `Request` validados con `@Valid` y responden exclusivamente en formato JSON (`XResponse`). 

* `TorneoController`: CRUD y búsquedas de torneos.
* `EquipoController`: CRUD y listados por torneo.
* `JugadorController`: Registro e inscripción de futbolistas por plantilla.
* `PartidoController`: Programación, actualización de marcador y cambios de estado.
* `EventoPartidoController`: Registro de incidencias (goles, autogoles, tarjetas).
* `EstadisticaJugadorController`, `TablaPosicionController`, `JornadaController`, `ArbitroController`, `UsuarioController`, `RolController`.

### 3.2 `controllers/web/` — Controladores Web (Thymeleaf MVC)

Controladores anotados con `@Controller` que atienden las rutas navegables desde el explorador. Capturan entradas vía `@ModelAttribute`, invocan a los servicios y retornan plantillas HTML o redirecciones (`redirect:`).

* `TorneoWebController`: Rutas `/torneos` y `/admin/torneos` (incluye vistas de fixture y rankings).
* `EquipoWebController`: Rutas `/equipos` y `/admin/equipos` (incluye listas de goleadores y asistentes del club).
* `JugadorWebController`: Rutas `/jugadores` y `/admin/jugadores`.
* `ArbitroWebController`: Rutas `/arbitros` y `/admin/arbitros`.
* `DashboardWebController`: Ruta `/admin/dashboard`.

### 3.3 `dto/` — Objetos de Transferencia de Datos

Define las estructuras de comunicación inmutables (`record`). (Ver `DTOS.md`).

* **`request/`:** Objetos de solicitud con Bean Validation (`@NotBlank`, `@NotNull`, `@Min`, `@Max`).
* **`response/`:** Objetos de salida de solo lectura que evitan la fuga de datos de dominio o información sensible.
* **`DtoMapper.java`:** Clase utilitaria con métodos estáticos sobrecargados y *null-safe* para la conversión `Entidad → Response`.

### 3.4 `exceptions/` — Arquitectura de Errores

Centraliza la captura y tratamiento de excepciones. (Ver `EXEPTION.md`).

* **Excepciones de Negocio:** `RecursoNoEncontradoException` (404) y `ValidacionException` (400).
* `GlobalExceptionHandler`: Intercepta `@RestController` y devuelve JSON uniformes (`Status`, `Message`, `Timestamp`, `Errors`).
* `VistaExceptionHandler`: Intercepta `@Controller` en `controllers.web`, inyecta mensajes de error mediante Flash Attributes y realiza redirecciones seguras (`Referer`).

### 3.5 `models/` — Dominio y Persistencia

* **`entity/`:** Clases `@Entity` relacionales (`Torneo`, `Equipo`, `Jugador`, `Partido`, `EventoPartido`, `EstadisticaJugador`, `TablaPosicion`, `Jornada`, `Arbitro`, `Usuario`, `Rol`). Permanece encapsulado en servicios y repositorios.
* **`enums/`:** Enumeraciones del sistema (`EstadoPartido`, `Posiciones`, `Roles`, `TipoEvento`).

### 3.6 `repositories/` — Capa de Datos JPA

Interfaces que extienden de `JpaRepository<T, UUID>` para ejecutar operaciones de persistencia y consultas SQL optimizadas (ej. `findByTorneoId`, `existsByEquipoLocalIdOrEquipoVisitanteId`).

### 3.7 `services/` — Lógica de Negocio

* **`services/*.java`:** Interfaces con contratos públicos que reciben `Request` y retornan `Response`.
* **`services/impl/*.java`:** Implementaciones anotadas con `@Service` y `@Transactional` que ejecutan la lógica transaccional, resolución de UUIDs a entidades y conversión a DTOs.

---

## 4. Recursos del Sistema (`src/main/resources/`)

### 4.1 `static/`
* `css/app.css`: Hojas de estilos del panel administrativo y Vistas Públicas.
* `img/logo.svg`: Logotipo y assets gráficos de la aplicación.

### 4.2 `templates/` (Plantillas Thymeleaf)
* `admin/`: `dashboard.html`.
* `torneo/`: `listaTorneo.html`, `formularioTorneo.html`, `opcionesTorneo.html`, `confirmarEliminarTorneo.html`, `fixture.html`, `rankings.html`.
* `equipo/`: `menuEquipo.html`, `opcionesEquipo.html`, `formularioEquipo.html`, `confirmarEliminarEquipo.html`, `goleadoresEquipo.html`, `asistentesEquipo.html`.
* `jugador/`: `menuJugador.html`, `opcionesJugador.html`, `formularioJugador.html`, `confirmarEliminarJugador.html`.
* `arbitro/`: `listaArbitros.html`, `menuArbitro.html`, `opcionesArbitro.html`, `formularioArbitro.html`, `confirmarEliminarArbitro.html`.
* `fragments/`: Componentes reutilizables (`navbar.html`, `sidebar.html`, `marcador.html`, `badge-estado.html`, `mensajes.html`, `errores-form.html`, `confirmar-eliminar.html`, `tabla-vacia.html`).
* `layout/`: Diseños base de página (`base.html`, `admin.html`).
* `index.html` & `error.html`: Vistas principales de aterrizaje y página de error global HTML.

---

## 5. Convenciones de Desarrollo

1. **Simetría entre Capas:** Toda nueva entidad debe incluir su correspondiente `Repository`, `Service`, `ServiceImpl`, DTOs (`Request`/`Response`), mapeo en `DtoMapper` y `Controller`.
2. **Encapsulamiento de Entidades:** Las entidades JPA nunca salen de la capa de servicio hacia la capa de presentación (`Controller`).
3. **Inyección por Constructor:** Inyección de dependencias declaradas como `final` mediante `@RequiredArgsConstructor` de Lombok.
4. **Transaccionalidad Atómica:** Las operaciones complejas entre múltiples entidades (ej. crear equipo + crear fila en tabla de posiciones) deben llevar `@Transactional`.