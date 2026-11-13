# Documentación de Services — Sistema de Torneos (`com.lospaisas`)

## 1. Propósito y Arquitectura General

La capa `services` concentra la **lógica de negocio** del sistema. Los Services reciben las solicitudes de los Controllers (web Thymeleaf y API REST), aplican las reglas de dominio, gestionan transacciones y coordinan la persistencia mediante los Repositories.

Cada servicio sigue el patrón de diseño de desacoplamiento por interfaz e implementación:

- **Interfaz** (`services/XService.java`): Define el contrato explícito expuesto a los controladores y otros servicios.
- **Implementación** (`services/impl/XServiceImpl.java`): Contiene la lógica operativa, orquestación de repositorios y conversión DTO/Entidad.

```text
Controller (Web / REST)
  ├── Envia: XRequest (validado con @Valid o @ModelAttribute)
  ↓
Service (Interfaz)
  ↓
ServiceImpl (Lógica de negocio, consultas SQL optimizadas, reglas transaccionales)
  ├── Convierte identificadores UUID a Entidades JPA mediante Repositories
  ├── Entidad ↔ Repository ↔ Base de datos (MySQL / H2)
  ↓
DtoMapper (Convierte Entidad → XResponse)
  ↓
Retorna: XResponse (JSON / Modelo Thymeleaf)
```

---

## 2. Convenciones de los Services (Guía de Diseño)

### 2.1 Los Services exponen DTOs, no entidades JPA

Ninguna entidad persistente de JPA sale de la capa de servicio hacia un Controller. Los métodos públicos de la interfaz siempre reciben un `Request` y devuelven un `Response` o colecciones de los mismos (ver `DTOS.md`).

```java
EquipoResponse crearEquipo(EquipoRequest request);
List<EquipoResponse> listarEquipos();
```

### 2.2 `buscarPorId` devuelve un Response, no una entidad

Por convención arquitectónica, **un Service no debe llamar a `buscarPorId` de otro Service para obtener una entidad**. Si una operación necesita una entidad relacionada de otro dominio (por ejemplo, obtener la entidad `Torneo` al crear un `Equipo`), el `ServiceImpl` debe inyectar el **Repository** del dominio correspondiente y buscar la entidad directamente:

```java
Torneo torneo = torneoRepository.findById(request.torneoId())
        .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado"));
```

### 2.3 Métodos auxiliares privados `obtenerX`

Dentro de cada `ServiceImpl` existen métodos auxiliares de acceso privado (`obtenerEquipo`, `obtenerPartido`, `obtenerUsuario`...) que buscan la **entidad JPA** por ID y lanzan `RecursoNoEncontradoException` si no existe. Se utilizan internamente para operaciones de actualización, borrado y mapeo dentro del contexto de persistencia.

### 2.4 Métodos internos entre Services (manejan entidades)

Determinadas operaciones requieren comunicación directa entre servicios mediante objetos de dominio. Estos métodos se conservan en la interfaz del servicio pero están destinados exclusivamente para consumo entre `ServiceImpl`:

| Método | Quién lo llama | Propósito |
|---|---|---|
| `EstadisticaJugadorService.actualizarEstadisticas(...)` | `EventoPartidoServiceImpl` | Suma goles, asistencias o tarjetas al registrar un evento |
| `EstadisticaJugadorService.revertirEstadisticas(...)` | `EventoPartidoServiceImpl` | Resta estadísticas al editar o eliminar un evento |
| `EstadisticaJugadorService.registrarPartidoJugado(Partido)` | `PartidoServiceImpl` | Incrementa `partidosJugados` a los futbolistas al finalizar un partido |
| `TablaPosicionService.actualizarTabla(Partido)` | `PartidoServiceImpl` | Calcula y suma puntos/goles en la tabla al finalizar un partido |
| `TablaPosicionService.inicializarPosicion(Torneo, Equipo)` | `EquipoServiceImpl` | Crea la fila inicial con valores en 0 al registrar un equipo |
| `TablaPosicionService.eliminarPosicion(Equipo)` | `EquipoServiceImpl` | Elimina la fila de la tabla de posiciones antes de borrar un equipo |
| `UsuarioService.obtenerEntidadPorEmail(String)` | Módulo de Autenticación | Devuelve la entidad `Usuario` con su hash de credenciales |

### 2.5 Métodos sobrecargados (*Method Overloading*) y métodos `default`

Para mantener los controladores limpios de lógica de filtrado y Streams:
- Las interfaces de los servicios utilizan métodos `default` para resolver delegaciones de parámetros opcionales. Por ejemplo:
  ```java
  default List<EstadisticaJugadorResponse> obtenerRankingGoleadores(UUID torneoId) {
      return obtenerRankingGoleadores(torneoId, null);
  }
  ```
- La capa de implementación (`ServiceImpl`) y los Repositories SQL asumen las búsquedas por texto (`buscar`) o filtrados por UUID opcionales directamente en la base de datos.

### 2.6 Excepciones y traducción centralizada de errores

| Excepción | Cuándo se lanza | Respuesta API REST | Manejo Web (Thymeleaf) |
|---|---|---|---|
| `RecursoNoEncontradoException` | Un ID o relación referenciada no existe | `404 Not Found` | Capturado por `VistaExceptionHandler`, agrega atributo flash y redirige a la vista segura. |
| `ValidacionException` | Incumplimiento de regla de negocio | `400 Bad Request` | Capturado por `VistaExceptionHandler`, notifica el error sin romper la navegación. |
| `DataIntegrityViolationException` | Violación de clave única o foránea en BD | `409 Conflict` | Traducido en mensaje amigable sobre conflicto de datos. |

---

## 3. TorneoService

**Interfaz:** `TorneoService` — **Implementación:** `TorneoServiceImpl`

### Función principal

Gestiona la creación, consulta, búsqueda, actualización y eliminación de torneos de fútbol.

### Métodos

- `TorneoResponse crearTorneo(TorneoRequest request)`
  - Guarda un nuevo torneo en la base de datos y retorna su DTO `TorneoResponse`.
- `List<TorneoResponse> listarTorneos()`
  - Obtiene todos los torneos registrados.
- `List<TorneoResponse> listarTorneos(String buscar)`
  - **Sobrecarga:** Filtra los torneos por coincidencia insensible a mayúsculas en el nombre. Si `buscar` es nulo o vacío, retorna la lista completa.
- `TorneoResponse buscarPorId(UUID id)`
  - Busca un torneo por su ID. Lanza `RecursoNoEncontradoException` si no existe.
- `TorneoResponse actualizarTorneo(UUID id, TorneoRequest request)`
  - Modifica nombre, descripción, fecha de inicio, fecha de fin, estado y formato del torneo.
- `void eliminarTorneo(UUID id)`
  - Elimina el torneo especificado.

---

## 4. EquipoService

**Interfaz:** `EquipoService` — **Implementación:** `EquipoServiceImpl`

### Función principal

Gestiona la creación de equipos, su asignación a torneos y la inicialización o eliminación de su estado en la tabla de posiciones.

### Dependencias de Repositorios y Servicios

`EquipoRepository`, `TorneoRepository` (para resolver `torneoId`), `PartidoRepository` (para verificar existencia de encuentros) y `TablaPosicionService`.

### Métodos

- `EquipoResponse crearEquipo(EquipoRequest request)`
  - Valida la existencia del `Torneo`.
  - Registra el nuevo `Equipo`.
  - Invoca a `TablaPosicionService.inicializarPosicion(torneo, equipo)` para crear su fila en la tabla de posiciones con estadísticas en 0.
  - Ejecutado en una única transacción: si falla la tabla de posiciones, se revierte la creación del equipo.
- `List<EquipoResponse> listarEquipos()`
  - Obtiene la totalidad de los equipos.
- `List<EquipoResponse> listarEquipos(UUID torneoId)` / `listarPorTorneo(UUID torneoId)`
  - **Sobrecarga optimizada:** Consulta a la base de datos mediante `EquipoRepository.findByTorneoId(torneoId)`. Si `torneoId == null`, retorna todos los equipos.
- `EquipoResponse buscarPorId(UUID id)`
  - Obtiene el DTO del equipo por ID.
- `EquipoResponse actualizarEquipo(UUID id, EquipoRequest request)`
  - Actualiza el nombre y el entrenador.
  - **Regla de negocio:** No permite cambiar el equipo de torneo. Si el `torneoId` del request difiere del torneo actual del equipo, lanza `ValidacionException` ("No se puede cambiar el torneo de un equipo ya registrado").
- `void eliminarEquipo(UUID id)`
  - Verifica mediante `PartidoRepository.existsByEquipoLocalIdOrEquipoVisitanteId` si el equipo ya disputó o tiene asignados partidos.
  - Si tiene partidos, lanza `ValidacionException` ("No se puede eliminar un equipo que tiene partidos registrados").
  - Si no tiene partidos, elimina su fila de la tabla de posiciones con `TablaPosicionService.eliminarPosicion(equipo)` y posteriormente borra la entidad `Equipo`.

### Reglas de Negocio

| Regla | Método | Excepción |
|---|---|---|
| No se puede cambiar el torneo de un equipo | `actualizarEquipo` | `ValidacionException` (400) |
| Un equipo con partidos no se puede eliminar | `eliminarEquipo` | `ValidacionException` (400) |

---

## 5. JugadorService

**Interfaz:** `JugadorService` — **Implementación:** `JugadorServiceImpl`

### Función principal

Gestiona los futbolistas pertenecientes a cada equipo.

### Separación Arquitectónica: Jugador vs. Usuario

Un **Jugador** es un futbolista inscrito en un equipo. Un **Usuario** es una cuenta con credenciales para acceder al sistema. El registro de un jugador no requiere una cuenta de usuario ni la presencia de un `usuarioId`. Los datos personales (`nombre`, `apellido`) residen en la entidad `Jugador`.

### Métodos

- `JugadorResponse crearJugador(JugadorRequest request)`
  - Busca la entidad `Equipo` por `equipoId`.
  - Registra el nuevo jugador con sus datos personales y su `Posiciones` (enum).
- `List<JugadorResponse> listarJugadores()`
  - Retorna todos los futbolistas registrados.
- `JugadorResponse buscarPorId(UUID id)`
  - Obtiene la información del jugador por ID.
- `JugadorResponse actualizarJugador(UUID id, JugadorRequest request)`
  - Actualiza nombre, apellido, equipo asignado y posición.
- `void eliminarJugador(UUID id)`
  - Elimina el registro del futbolista.
- `List<JugadorResponse> buscarPorEquipo(UUID equipoId)`
  - Obtiene los futbolistas pertenecientes a un equipo específico.
- `List<JugadorResponse> buscarPorEquipo(UUID equipoId, String buscar)`
  - **Sobrecarga:** Obtiene los jugadores de un equipo y los filtra por coincidencia en `nombre` o `apellido`.

---

## 6. JornadaService

**Interfaz:** `JornadaService` — **Implementación:** `JornadaServiceImpl`

### Función principal

Gestiona la creación y consulta de las fechas o jornadas de un torneo, así como la construcción del DTO de fixture estructurado.

### Métodos

- `JornadaResponse crearJornada(JornadaRequest request)`
  - Busca el torneo por `torneoId` y asigna el `numeroJornada`.
- `List<JornadaResponse> listarJornadas()`
  - Lista la totalidad de jornadas registradas.
- `List<JornadaResponse> listarPorTorneo(UUID torneoId)`
  - Retorna las jornadas correspondientes a un torneo específico.
- `JornadaResponse buscarPorId(UUID id)`
  - Obtiene una jornada por ID.
- `void eliminarJornada(UUID id)`
  - Elimina la jornada mediante `deleteById`.
- `List<JornadaFixtureResponse> obtenerFixture(UUID torneoId, String estado, UUID equipoId)`
  - Construye la respuesta estructurada del fixture agrupando cada jornada con su lista de `PartidoResponse`.
  - Aplica filtros opcionales por estado del partido (`PROGRAMADO`, `FINALIZADO`, etc.) y pertenencia de un `equipoId`.

---

## 7. PartidoService

**Interfaz:** `PartidoService` — **Implementación:** `PartidoServiceImpl`

### Función principal

Administra la programación de encuentros, asignación de árbitros, cambios de estado y marcadores de los partidos.

### Dependencias

`PartidoRepository`, `JornadaRepository`, `EquipoRepository`, `ArbitroRepository`, `TablaPosicionService` y `EstadisticaJugadorService`.

### Métodos

- `PartidoResponse crearPartido(PartidoRequest request)`
  - Busca la jornada, los equipos (local y visitante) y el árbitro.
  - **Regla de negocio:** Valida que el equipo local y el visitante no sean el mismo. Si son iguales, lanza `ValidacionException` ("El equipo local y el equipo visitante no pueden ser el mismo").
  - Inicializa el partido en estado `PROGRAMADO` con marcador `0 - 0`.
- `PartidoResponse buscarPorId(UUID id)`
  - Consulta un partido por ID.
- `List<PartidoResponse> listarPartidos()`
  - Retorna todos los partidos.
- `List<PartidoResponse> listarPorTorneo(UUID torneoId)`
  - Ejecuta la consulta SQL optimizada `PartidoRepository.findByJornadaTorneoId(torneoId)`.
- `List<PartidoResponse> obtenerProximosPartidos(int limite)`
  - Retorna los próximos partidos en estado `PROGRAMADO` ordenados por fecha ascendente mediante `findTop5ByEstadoOrderByFechaAsc`.
- `PartidoResponse cambiarEstado(UUID partidoId, EstadoPartido estado)`
  - Actualiza la propiedad `estado` del partido.
  - **Garantía de Idempotencia:** Detecta cuando el estado cambia a `FINALIZADO` por primera vez (`estadoAnterior != FINALIZADO`). Solo en esta transición invoca a:
    1. `TablaPosicionService.actualizarTabla(partido)`
    2. `EstadisticaJugadorService.registrarPartidoJugado(partido)`
- `PartidoResponse actualizarMarcador(UUID partidoId, int golesLocal, int golesVisitante)`
  - Actualiza directamente los campos `golesLocal` y `golesVisitante`. Valida que los valores no sean negativos.
- `void eliminarPartido(UUID id)`
  - Elimina el encuentro deportivo.

---

## 8. ArbitroService

**Interfaz:** `ArbitroService` — **Implementación:** `ArbitroServiceImpl`

### Métodos

- `ArbitroResponse crearArbitro(ArbitroRequest request)`
  - Registra un nuevo árbitro con nombre, apellido, años de experiencia y número de licencia.
- `List<ArbitroResponse> listarArbitros()`
  - Lista todos los árbitros.
- `List<ArbitroResponse> listarArbitros(String buscar)`
  - **Sobrecarga:** Filtra árbitros por coincidencia en `nombre` o `apellido`.
- `ArbitroResponse buscarPorId(UUID id)`
  - Obtiene el DTO del árbitro.
- `ArbitroResponse actualizarArbitro(UUID id, ArbitroRequest request)`
  - Actualiza la información personal y profesional del árbitro.
- `void eliminarArbitro(UUID id)`
  - Elimina el registro del árbitro.

---

## 9. UsuarioService

**Interfaz:** `UsuarioService` — **Implementación:** `UsuarioServiceImpl`

### Métodos

- `UsuarioResponse crearUsuario(UsuarioRequest request)`
  - Busca el `Rol` por `rolId` y registra una nueva cuenta.
- `List<UsuarioResponse> listarUsuarios()`
  - Lista los usuarios registrados (ocultando el hash de clave).
- `UsuarioResponse buscarPorId(UUID id)`
  - Obtiene la información pública del usuario.
- `UsuarioResponse buscarPorEmail(String email)`
  - Busca un usuario por su correo electrónico.
- `UsuarioResponse actualizarUsuario(UUID id, UsuarioUpdateRequest request)`
  - Actualiza nombre, email y rol. La contraseña se actualiza únicamente si el campo `password` no es nulo.
- `void eliminarUsuario(UUID id)`
  - Elimina la cuenta de usuario.
- `Usuario obtenerEntidadPorEmail(String email)` *(Método Interno)*
  - Devuelve la **entidad JPA** `Usuario` (incluye `passwordHash`). Reservado para el módulo de autenticación.

---

## 10. RolService

**Interfaz:** `RolService` — **Implementación:** `RolServiceImpl`

### Métodos

- `RolResponse crearRol(RolRequest request)`
  - Registra un rol (enum `Roles`: `ADMIN`, `ORGANIZADOR`, `ENTRENADOR`, `ARBITRO`, `ESPECTADOR`).
- `List<RolResponse> listarRoles()`
  - Lista los roles disponibles.
- `RolResponse buscarPorId(UUID id)`
  - Obtiene un rol por ID.
- `RolResponse actualizarRol(UUID id, RolRequest request)`
  - Modifica el nombre del rol.
- `void eliminarRol(UUID id)`
  - Elimina un rol.

---

## 11. EventoPartidoService

**Interfaz:** `EventoPartidoService` — **Implementación:** `EventoPartidoServiceImpl`

### Función principal

Registra las incidencias de un partido (goles, tarjetas, sustituciones) y mantiene sincronizados automáticamente el marcador del partido y las estadísticas del jugador.

### Dependencias

`EventoPartidoRepository`, `PartidoRepository`, `JugadorRepository`, `PartidoService` y `EstadisticaJugadorService`.

### Métodos y Flujo de Trabajo

- `EventoPartidoResponse registrarEvento(EventoPartidoRequest request)`
  - Valida la existencia del partido y del jugador.
  - **Regla de negocio:** Verifica que el jugador pertenezca al equipo local o visitante del partido. Si no pertenece, lanza `ValidacionException` ("El jugador no pertenece a ninguno de los equipos del partido").
  - Valida que el minuto del evento esté entre 0 y 130.
  - Guarda la entidad `EventoPartido`.
  - **Sincronización de Goles:** Si el tipo de evento es un gol (`GOL_JUGADA`, `GOL_CABEZA`, `GOL_PENAL`, `GOL_TIRO_LIBRE`), invoca a `PartidoService.actualizarMarcador` sumando 1 al marcador del equipo del jugador.
  - **Regla de Autogol (`AUTOGOL`):** Suma 1 gol en el marcador del **equipo contrario** al del jugador que comete la acción. No incrementa los goles individuales del futbolista.
  - **Sincronización de Estadísticas:** Invoca a `EstadisticaJugadorService.actualizarEstadisticas` para actualizar goles, asistencias o tarjetas.

```text
Registrar Evento (POST)
       ↓
Validar Pertenencia del Jugador al Partido
       ↓
Guardar EventoPartido
       ├────────────────────────────────────────┐
       ↓                                        ↓
Si es Gol / Autogol                     Actualizar Estadísticas
       ↓                                        ↓
Invoca PartidoService.actualizarMarcador   Invoca EstadisticaJugadorService
       ↓                                 (Goles, Asistencias, Tarjetas)
Marcador del Partido Actualizado
```

- `List<EventoPartidoResponse> listarPorPartido(UUID partidoId)`
  - Obtiene las incidencias registradas en un partido mediante `EventoPartidoRepository.findByPartidoId(partidoId)`.
- `EventoPartidoResponse actualizarEvento(UUID eventoId, EventoPartidoUpdateRequest request)`
  - Aplica un patrón de **Reversión y Reaplicación**:
    1. Revierte el efecto del evento anterior en el marcador y estadísticas (`revertirEstadisticas`).
    2. Modifica el tipo de evento y minuto.
    3. Aplica los efectos del nuevo evento sobre el marcador y estadísticas.
- `void eliminarEvento(UUID eventoId)`
  - Revierte los efectos del evento sobre marcador y estadísticas, y posteriormente elimina el registro de la BD.

---

## 12. EstadisticaJugadorService

**Interfaz:** `EstadisticaJugadorService` — **Implementación:** `EstadisticaJugadorServiceImpl`

### Función principal

Consolida el rendimiento individual de los futbolistas a lo largo de un torneo.

### Métodos Internos y Públicos

- `void actualizarEstadisticas(Jugador j, Torneo t, Equipo e, TipoEvento tipo)` *(Interno)*
  - Incrementa la métrica correspondiente en la entidad `EstadisticaJugador` según el evento.
- `void revertirEstadisticas(Jugador j, Torneo t, Equipo e, TipoEvento tipo)` *(Interno)*
  - Decrementa la métrica correspondiente (sin permitir valores menores a 0).
- `void registrarPartidoJugado(Partido partido)` *(Interno)*
  - Incrementa en 1 la columna `partidosJugados` a todos los jugadores inscritos en las plantillas del equipo local y visitante. Se invoca cuando el encuentro pasa a `FINALIZADO`.
- `EstadisticaJugadorResponse obtenerEstadisticaPorJugador(UUID jugadorId)`
  - Consulta las estadísticas acumuladas de un futbolista especifico.
- `List<EstadisticaJugadorResponse> obtenerRankingGoleadores(UUID torneoId, UUID equipoId)`
  - Retorna la tabla de goleadores (máximo 50) ordenada por goles descendente. Permite un filtro opcional por `equipoId`.
- `List<EstadisticaJugadorResponse> obtenerRankingAsistencias(UUID torneoId, UUID equipoId)`
  - Retorna la tabla de asistentes (máximo 50) ordenada por asistencias descendente. Permite un filtro opcional por `equipoId`.

---

## 13. TablaPosicionService

**Interfaz:** `TablaPosicionService` — **Implementación:** `TablaPosicionServiceImpl`

### Función principal

Mantiene la tabla de posiciones del torneo.

### Métodos

- `void actualizarTabla(Partido partido)` *(Interno)*
  - Procesa partidos en estado `FINALIZADO`.
  - Actualiza para ambos equipos: partidos jugados, ganados, empatados, perdidos, goles a favor, goles en contra, diferencia de goles y puntos.
  - **Sistema de Puntuación:** Victoria = 3 pts, Empate = 1 pt, Derrota = 0 pts.
- `void inicializarPosicion(Torneo torneo, Equipo equipo)` *(Interno)*
  - Invocado al crear un equipo. Crea su fila inicial en la tabla con todos los valores en 0.
- `void eliminarPosicion(Equipo equipo)` *(Interno)*
  - Invocado al eliminar un equipo sin partidos. Elimina su fila de la tabla de posiciones.
- `List<TablaPosicionResponse> obtenerTabla(UUID torneoId)` / `obtenerTablaCompleta(UUID torneoId, UUID equipoId)`
  - Retorna la tabla de posiciones ordenada según los siguientes criterios de desempate:
    1. **Puntos** (descendente)
    2. **Diferencia de Goles** (descendente)
    3. **Goles a Favor** (descendente)

---

## 14. Anotaciones y Transaccionalidad

- `@Service`: Declara la clase como componente de servicio Spring en la capa de negocio.
- `@Transactional`: Se aplica a nivel de clase en `jakarta.transaction.Transactional`. Garantiza que las operaciones complejas (ej. guardar un equipo e inicializar su posición en la tabla) se ejecuten en un bloque atómico. Si ocurre una excepción, los cambios en la BD se revierten automáticamente (*rollback*). Además, permite que el `DtoMapper` acceda a relaciones lazy dentro de la misma sesión de persistencia.
- `@RequiredArgsConstructor`: Genera la inyección de dependencias por constructor de los componentes `final`.

---

## 15. Integración General entre Services

```text
                                EventoPartidoService
                                         │
               ┌─────────────────────────┴────────────────────────┐
               ↓                                                  ↓
        PartidoService                                EstadisticaJugadorService
               │                                                  │
    (Actualiza Marcador)                                 (Actualiza Goles/Cards)
               │                                                  │
               └─────────────────────────┬────────────────────────┘
                                         ↓
                        Al Finalizar Partido (FINALIZADO)
                                         │
               ┌─────────────────────────┴────────────────────────┐
               ↓                                                  ↓
      TablaPosicionService                             EstadisticaJugadorService
(Actualiza Pts, GF, GC, DG)                            (Partidos Jugados + 1)
```

---

## 16. Estado Actual del Sistema

1. **Controladores Delgados:** Los controladores Thymeleaf y REST no contienen lógica de filtrado ni manipulación de Streams.
2. **Excepciones Centralizadas:** `VistaExceptionHandler` y `GlobalExceptionHandler` traducen excepciones de negocio a flujos web o respuestas JSON `400/404/409`.
3. **Optimización SQL:** Consultas de búsqueda y filtrados masivos se resuelven directamente en el motor de base de datos.
4. **Modelos Independientes:** `Jugador` opera de forma completamente independiente de `Usuario`.

---

## 17. Checklist para Desarrolladores (Antes de Modificar un Service)

1. **Mantener DTOs en Firmas:** Nunca devuelvas entidades JPA en métodos consumidos por Controllers.
2. **Inyección de Repositories:** Si necesitas una entidad de otro dominio, inyecta su `Repository` directamente; no llames al `buscarPorId` del otro Service.
3. **Uso de Métodos Auxiliares:** Utiliza los métodos privados `obtenerX` para buscar entidades internamente y lanzar `RecursoNoEncontradoException`.
4. **Verificar Reglas Transaccionales:** Asegúrate de que las operaciones compuestas estén bajo `@Transactional`.
5. **Validar antes de Borrar:** Si una eliminación rompe relaciones, valida en el Service y lanza `ValidacionException` con un mensaje claro en lugar de dejar fallar la clave foránea en SQL.
6. **Sincronización:** Si agregas campos a una entidad, actualiza su `Request`, `Response`, `DtoMapper` y las tablas correspondientes en `SERVICES.md`, `DTOS.md` y `ENDPOINTS.md`.