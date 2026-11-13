# Endpoints de la API — Sistema de Torneos

Documentación completa, oficial y detallada de los endpoints expuestos por la API REST del backend (`com.lospaisas`).

La API está organizada por recursos. Los endpoints CRUD permiten administrar las entidades principales, mientras que algunos endpoints específicos ejecutan operaciones avanzadas relacionadas con la lógica del torneo, programación de jornadas, control de partidos, registro de eventos, cálculo de estadísticas y tabla de posiciones.

Todos los endpoints reciben y devuelven **DTOs** (`Request` / `Response`), nunca entidades JPA directamente. El detalle de cada campo y sus anotaciones de validación se encuentra en [`DTOS.md`](DTOS.md).

---

## Convenciones generales

### Formato

- **Base URL local:** `http://localhost:8080/api`
- **Formato de datos:** Los cuerpos de las peticiones y respuestas utilizan **JSON** (`Content-Type: application/json`).
- **Identificadores (IDs):** Son cadenas de texto en formato `UUID` v4 estándar (ej. `a1b2c3d4-0000-4000-8000-000000000001`).
- **Fechas y horas:** Siguen el estándar ISO 8601:
  - `LocalDate`: `YYYY-MM-DD` (ejemplo: `2026-10-01`).
  - `LocalDateTime`: `YYYY-MM-DDTHH:mm:ss` (ejemplo: `2026-10-10T16:00:00`).
- **Enumeraciones (Enums):** Se envían en texto y en mayúsculas exactas (`FINALIZADO`, `DELANTERO`, `GOL_JUGADA`, etc.).
- **Relaciones:** Se envían únicamente como IDs primarios (`"torneoId": "..."`), evitando el envío de objetos anidados en las peticiones.
- **CORS:** Habilitado para cualquier origen (`@CrossOrigin("*")`) en todos los controladores REST.

---

### Códigos de estado HTTP

| Código | Cuándo ocurre |
|---|---|
| `200 OK` | Consulta exitosa (`GET`) o actualización completada (`PUT`). |
| `201 Created` | Recurso creado exitosamente (`POST`). |
| `204 No Content` | Recurso eliminado correctamente (`DELETE`). |
| `400 Bad Request` | Datos de entrada inválidos: falla una validación de formato, JSON mal estructurado, enum o UUID con formato incorrecto, o regla de negocio incumplida. |
| `404 Not Found` | El recurso solicitado (o una entidad referenciada por su ID) no existe en la base de datos. |
| `409 Conflict` | Violación de restricción de integridad en BD: valor duplicado (ej. nombre de equipo, email) o intento de eliminar un registro que posee relaciones activas. |
| `500 Internal Server Error` | Error no controlado o inesperado dentro del servidor. |

---

### Formato de los errores

Los errores son capturados y estructurados de forma centralizada mediante `GlobalExceptionHandler` (para solicitudes REST) y `VistaExceptionHandler` (para peticiones web/Thymeleaf).

#### 1. Falla de validación (`@Valid` $\rightarrow$ `400 Bad Request`)
Devuelve un objeto JSON con un mapa explícito de cada campo rechazado y su mensaje correspondiente:

```json
{
  "nombre": "el tamaño debe estar entre 3 y 50",
  "torneoId": "no debe ser null"
}
```

> *Nota:* Los mensajes por defecto de Bean Validation dependen de la configuración regional (*locale*) del servidor.

#### 2. Excepciones de negocio y recursos no encontrados
Devuelve un objeto JSON estandarizado con la clave `error`:

```json
{ 
  "error": "El equipo local y el equipo visitante no pueden ser el mismo" 
}
```

#### Matriz detallada de respuesta ante excepciones

| Situación | Excepción Interna | Código HTTP | Mensaje devuelto (`error`) |
|---|---|---|---|
| Recurso no encontrado | `RecursoNoEncontradoException` | 404 | El mensaje definido en el Service (ej: `Partido no encontrado`). |
| Regla de negocio incumplida | `ValidacionException` | 400 | El mensaje del Service (ej: `No se puede cambiar el torneo de un equipo ya registrado`). |
| JSON mal formado, Enum o fecha inválida | `HttpMessageNotReadableException` | 400 | `El cuerpo de la petición es inválido o tiene un formato incorrecto (...)` |
| UUID con formato incorrecto en URL | `MethodArgumentTypeMismatchException` | 400 | `El parámetro 'id' tiene un formato inválido` |
| Valor duplicado o restricción de FK | `DataIntegrityViolationException` | 409 | `La operación viola una restricción de datos (...)` |
| Ruta o método HTTP no soportado | `NoHandlerFoundException` / `HttpRequestMethodNotSupportedException` | 404 / 405 / 415 | Detalle estándar generado por Spring Boot. |
| Error interno no previsto | Excepción genérica `Exception` | 500 | `Ha ocurrido un error interno en el servidor` |

*El detalle técnico y trazas de los errores 409 y 500 no se exponen al cliente por motivos de seguridad; quedan registrados únicamente en los logs del servidor.*

---

## 1. Torneos

### `TorneoController` — `/api/torneos`

Permite administrar la información principal de los torneos.

| Método | Endpoint | Parámetros Query | Request Body | Response Body | Estado | Descripción |
|---|---|---|---|---|---|---|
| `POST` | `/api/torneos` | — | `TorneoRequest` | `TorneoResponse` | `201` | Registra un nuevo torneo |
| `GET` | `/api/torneos` | `buscar` (opcional) | — | `List<TorneoResponse>` | `200` | Listar torneos (filtra por nombre si se envía `buscar`) |
| `GET` | `/api/torneos/{id}` | — | — | `TorneoResponse` | `200` | Consultar un torneo por su UUID |
| `PUT` | `/api/torneos/{id}` | — | `TorneoRequest` | `TorneoResponse` | `200` | Actualizar los datos de un torneo |
| `DELETE` | `/api/torneos/{id}` | — | — | — | `204` | Eliminar un torneo |

#### Ejemplo de Body (`POST` / `PUT`):

```json
{
  "nombre": "Copa Los Paisas 2026",
  "descripcion": "Torneo amateur de fin de año",
  "fechaInicio": "2026-11-01",
  "fechaFin": "2026-12-20",
  "estado": "PROGRAMADO",
  "formato": "LIGA"
}
```

---

## 2. Equipos

### `EquipoController` — `/api/equipos`

Permite administrar los equipos y su vinculación con los torneos. Al registrar un equipo, `EquipoServiceImpl` invoca a `TablaPosicionService.inicializarPosicion(...)` dentro de la misma transacción, creando su fila correspondiente en la tabla de posiciones con todos los valores inicializados en 0.

| Método | Endpoint | Parámetros Query | Request Body | Response Body | Estado | Descripción |
|---|---|---|---|---|---|---|
| `POST` | `/api/equipos` | — | `EquipoRequest` | `EquipoResponse` | `201` | Crear un equipo e inicializar su posición |
| `GET` | `/api/equipos` | `torneoId` (opcional) | — | `List<EquipoResponse>` | `200` | Listar equipos (filtra por torneo si viene `torneoId`) |
| `GET` | `/api/equipos/{id}` | — | — | `EquipoResponse` | `200` | Consultar un equipo por su ID |
| `PUT` | `/api/equipos/{id}` | — | `EquipoRequest` | `EquipoResponse` | `200` | Actualizar datos (no permite cambiar de torneo) |
| `DELETE` | `/api/equipos/{id}` | — | — | — | `204` | Eliminar un equipo sin partidos registrados |

#### Ejemplo de Body (`POST` / `PUT`):

```json
{
  "nombre": "Atlético Paisas",
  "entrenador": "Carlos Gómez",
  "torneoId": "3f2b8c1e-5a4d-4e7f-9b21-0c6d8a1e4f10"
}
```

#### Respuesta de salida (`EquipoResponse`):

```json
{
  "id": "a1b2c3d4-0000-4000-8000-000000000001",
  "nombre": "Atlético Paisas",
  "entrenador": "Carlos Gómez",
  "torneoId": "3f2b8c1e-5a4d-4e7f-9b21-0c6d8a1e4f10",
  "torneoNombre": "Copa Los Paisas 2026"
}
```

#### Reglas de negocio específicas:
- Si el `nombre` ya existe en la BD, la API responde `409 Conflict`.
- Si se intenta cambiar el `torneoId` original en una actualización (`PUT`), responde `400 Bad Request` ("No se puede cambiar el torneo de un equipo ya registrado").
- Si se intenta eliminar un equipo que ya posee partidos registrados (como local o visitante), responde `400 Bad Request` ("No se puede eliminar un equipo que tiene partidos registrados").

---

## 3. Jugadores

### `JugadorController` — `/api/jugadores`

Gestiona a los futbolistas de los equipos. 

**Separación entre Jugador y Usuario:** Un jugador representa a la persona que participa deportivamente en un equipo. No requiere una cuenta de usuario ni credenciales para ser registrado. Por ello, `JugadorRequest` recibe los datos personales (`nombre`, `apellido`) y no requiere `usuarioId`.

| Método | Endpoint | Parámetros Query | Request Body | Response Body | Estado | Descripción |
|---|---|---|---|---|---|---|
| `POST` | `/api/jugadores` | — | `JugadorRequest` | `JugadorResponse` | `201` | Crear un jugador asociado a un equipo |
| `GET` | `/api/jugadores` | — | — | `List<JugadorResponse>` | `200` | Listar todos los jugadores registrados |
| `GET` | `/api/jugadores/{id}` | — | — | `JugadorResponse` | `200` | Consultar un jugador por su ID |
| `GET` | `/api/jugadores/equipo/{equipoId}` | `buscar` (opcional) | — | `List<JugadorResponse>` | `200` | Listar jugadores de un equipo (con filtro por coincidencia en nombre o apellido) |
| `PUT` | `/api/jugadores/{id}` | — | `JugadorRequest` | `JugadorResponse` | `200` | Actualizar datos personales o posición |
| `DELETE` | `/api/jugadores/{id}` | — | — | — | `204` | Eliminar un jugador |

#### Ejemplo de Body (`POST` / `PUT`):

```json
{
  "nombre": "Carlos",
  "apellido": "Mendoza",
  "equipoId": "a1b2c3d4-0000-4000-8000-000000000001",
  "posicion": "DELANTERO"
}
```

> `posicion` debe ser un valor válido del enum `Posiciones` (`PORTERO`, `DEFENSA`, `CENTROCAMPISTA`, `DELANTERO`).

#### Respuesta (`JugadorResponse`):

```json
{
  "id": "9a8b7c6d-0000-4000-8000-000000000060",
  "nombre": "Carlos",
  "apellido": "Mendoza",
  "equipoId": "a1b2c3d4-0000-4000-8000-000000000001",
  "equipoNombre": "Atlético Paisas",
  "posicion": "DELANTERO"
}
```

---

## 4. Usuarios

### `UsuarioController` — `/api/usuarios`

Permite administrar las cuentas de acceso al sistema y sus roles.

| Método | Endpoint | Request Body | Response Body | Estado | Descripción |
|---|---|---|---|---|---|
| `POST` | `/api/usuarios` | `UsuarioRequest` | `UsuarioResponse` | `201` | Crear una cuenta de usuario |
| `GET` | `/api/usuarios` | — | `List<UsuarioResponse>` | `200` | Listar todos los usuarios |
| `GET` | `/api/usuarios/{id}` | — | `UsuarioResponse` | `200` | Consultar usuario por ID |
| `PUT` | `/api/usuarios/{id}` | `UsuarioUpdateRequest` | `UsuarioResponse` | `200` | Actualizar datos de usuario (contraseña opcional) |
| `DELETE` | `/api/usuarios/{id}` | — | — | `204` | Eliminar usuario |

#### Body de creación (`POST`):

```json
{
  "nombre": "Juan Pérez",
  "email": "juan@correo.com",
  "password": "miClave123",
  "rolId": "c9d8e7f6-0000-4000-8000-000000000020"
}
```

#### Body de actualización (`PUT`):
La contraseña es opcional; si no se envía (`null`), se conserva la contraseña actual del usuario.

```json
{
  "nombre": "Juan Pérez",
  "email": "juan@correo.com",
  "rolId": "c9d8e7f6-0000-4000-8000-000000000020"
}
```

#### Respuesta (`UsuarioResponse`) — **Nunca incluye datos sensibles**:

```json
{
  "id": "b7c1d2e3-0000-4000-8000-000000000010",
  "nombre": "Juan Pérez",
  "email": "juan@correo.com",
  "rol": "ADMIN"
}
```

---

## 5. Roles

### `RolController` — `/api/roles`

Gestiona los roles asignables a los usuarios.

| Método | Endpoint | Request Body | Response Body | Estado | Descripción |
|---|---|---|---|---|---|
| `POST` | `/api/roles` | `RolRequest` | `RolResponse` | `201` | Crear un rol |
| `GET` | `/api/roles` | — | `List<RolResponse>` | `200` | Listar todos los roles |
| `GET` | `/api/roles/{id}` | — | `RolResponse` | `200` | Consultar rol por ID |
| `PUT` | `/api/roles/{id}` | `RolRequest` | `RolResponse` | `200` | Actualizar rol |
| `DELETE` | `/api/roles/{id}` | — | — | `204` | Eliminar rol |

#### Body (`POST` / `PUT`):

```json
{ 
  "nombre": "ADMIN" 
}
```

> `nombre` debe coincidir con un valor del enum `Roles` (`ADMIN`, `ORGANIZADOR`, `ENTRENADOR`, `ARBITRO`, `ESPECTADOR`).

---

## 6. Árbitros

### `ArbitroController` — `/api/arbitros`

Gestiona los árbitros disponibles para ser asignados a los partidos.

| Método | Endpoint | Parámetros Query | Request Body | Response Body | Estado | Descripción |
|---|---|---|---|---|---|---|
| `POST` | `/api/arbitros` | — | `ArbitroRequest` | `ArbitroResponse` | `201` | Crear un árbitro |
| `GET` | `/api/arbitros` | `buscar` (opcional) | — | `List<ArbitroResponse>` | `200` | Listar árbitros (filtra por coincidencia en nombre o apellido) |
| `GET` | `/api/arbitros/{id}` | — | — | `ArbitroResponse` | `200` | Consultar árbitro por ID |
| `PUT` | `/api/arbitros/{id}` | — | `ArbitroRequest` | `ArbitroResponse` | `200` | Actualizar datos del árbitro |
| `DELETE` | `/api/arbitros/{id}` | — | — | — | `204` | Eliminar un árbitro |

#### Body (`POST` / `PUT`):

```json
{
  "nombre": "Luis",
  "apellido": "Ramírez",
  "experiencia": 8,
  "licencia": "LIC-2041"
}
```

---

## 7. Jornadas y Fixture

### `JornadaController` — `/api/jornadas`

Permite estructurar las jornadas de un torneo y consultar el fixture agrupado de partidos.

| Método | Endpoint | Parámetros Query | Request Body | Response Body | Estado | Descripción |
|---|---|---|---|---|---|---|
| `POST` | `/api/jornadas` | — | `JornadaRequest` | `JornadaResponse` | `201` | Crear una jornada para un torneo |
| `GET` | `/api/jornadas` | — | — | `List<JornadaResponse>` | `200` | Listar todas las jornadas |
| `GET` | `/api/jornadas/torneo/{torneoId}` | — | — | `List<JornadaResponse>` | `200` | Listar jornadas de un torneo |
| `GET` | `/api/jornadas/fixture/{torneoId}` | `estado`, `equipoId` (opcionales) | — | `List<JornadaFixtureResponse>` | `200` | Obtener fixture estructurado (jornadas con sus partidos) |
| `GET` | `/api/jornadas/{id}` | — | — | `JornadaResponse` | `200` | Consultar una jornada por ID |
| `DELETE` | `/api/jornadas/{id}` | — | — | — | `204` | Eliminar una jornada por ID |

#### Body de creación (`POST`):

```json
{
  "torneoId": "3f2b8c1e-5a4d-4e7f-9b21-0c6d8a1e4f10",
  "numeroJornada": 1
}
```

#### Ejemplo de Fixture Response (`GET /api/jornadas/fixture/{torneoId}`):

```json
[
  {
    "jornada": {
      "id": "d1e2f3a4-0000-4000-8000-000000000030",
      "torneoId": "3f2b8c1e-5a4d-4e7f-9b21-0c6d8a1e4f10",
      "numeroJornada": 1
    },
    "partidos": [
      {
        "id": "f0e1d2c3-0000-4000-8000-000000000050",
        "jornadaId": "d1e2f3a4-0000-4000-8000-000000000030",
        "equipoLocalId": "a1b2c3d4-0000-4000-8000-000000000001",
        "equipoLocalNombre": "Atlético Paisas",
        "equipoVisitanteId": "a1b2c3d4-0000-4000-8000-000000000002",
        "equipoVisitanteNombre": "Deportivo Norte",
        "arbitroId": "e5f6a7b8-0000-4000-8000-000000000040",
        "arbitroNombre": "Luis Ramírez",
        "golesLocal": 2,
        "golesVisitante": 1,
        "estado": "FINALIZADO",
        "fecha": "2026-11-08T16:00:00"
      }
    ]
  }
]
```

> *Notas:* 
> - `JornadaController` no posee endpoint `PUT` debido a que el número de jornada no es modificable individualmente.
> - `DELETE /api/jornadas/{id}` elimina la jornada mediante `deleteById`.

---

## 8. Partidos

### `PartidoController` — `/api/partidos`

Gestiona los partidos, el fixture, el estado de los encuentros y los marcadores.

| Método | Endpoint | Parámetros Query | Request Body | Response Body | Estado | Descripción |
|---|---|---|---|---|---|---|
| `POST` | `/api/partidos` | — | `PartidoRequest` | `PartidoResponse` | `201` | Programar un nuevo partido |
| `GET` | `/api/partidos` | — | — | `List<PartidoResponse>` | `200` | Listar todos los partidos |
| `GET` | `/api/partidos/torneo/{torneoId}` | — | — | `List<PartidoResponse>` | `200` | Consultar partidos de un torneo |
| `GET` | `/api/partidos/proximos` | `limite` (def: 5) | — | `List<PartidoResponse>` | `200` | Próximos partidos programados (ordenados por fecha) |
| `GET` | `/api/partidos/{id}` | — | — | `PartidoResponse` | `200` | Consultar partido por ID |
| `PUT` | `/api/partidos/{id}/estado` | — | `CambiarEstadoRequest` | `PartidoResponse` | `200` | Modificar estado del partido |
| `PUT` | `/api/partidos/{id}/marcador` | — | `MarcadorRequest` | `PartidoResponse` | `200` | Actualizar goles del marcador |
| `DELETE` | `/api/partidos/{id}` | — | — | — | `204` | Eliminar un partido |

#### Crear partido (`POST /api/partidos`):

```json
{
  "jornadaId": "d1e2f3a4-0000-4000-8000-000000000030",
  "equipoLocalId": "a1b2c3d4-0000-4000-8000-000000000001",
  "equipoVisitanteId": "a1b2c3d4-0000-4000-8000-000000000002",
  "arbitroId": "e5f6a7b8-0000-4000-8000-000000000040",
  "fecha": "2026-11-08T16:00:00"
}
```

El partido se inicializa con marcador `0 - 0` y estado `PROGRAMADO`. Si el equipo local y el visitante son iguales, la API responde `400 Bad Request`.

#### Operación especial: Cambiar estado (`PUT /api/partidos/{id}/estado`):

```json
{ 
  "estado": "FINALIZADO" 
}
```

Valores de `EstadoPartido`: `PROGRAMADO`, `EN_JUEGO`, `FINALIZADO`, `SUSPENDIDO`.

##### Lógica de Transición a `FINALIZADO`:
Cuando el partido pasa a estado `FINALIZADO` **por primera vez** (`estadoAnterior != FINALIZADO`), el backend ejecuta automáticamente:
1. Cálculo y actualización de la tabla de posiciones (`TablaPosicionService.actualizarTabla`).
2. Registro de partidos jugados para los futbolistas (`EstadisticaJugadorService.registrarPartidoJugado`).

*Si se repite la llamada enviando nuevamente `FINALIZADO`, el sistema detecta que ya estaba finalizado y no vuelve a sumar puntos ni partidos jugados.*

#### Operación especial: Actualizar marcador (`PUT /api/partidos/{id}/marcador`):

```json
{ 
  "golesLocal": 2, 
  "golesVisitante": 1 
}
```

Permite ajustar manualmente los goles. Los valores deben ser enteros entre 0 y 99.

---

## 9. Eventos de Partido

### `EventoPartidoController` — `/api/eventos-partido`

Administra las acciones ocurridas durante el desarrollo de un partido (goles, tarjetas, sustituciones, asistencias).

| Método | Endpoint | Request Body | Response Body | Estado | Descripción |
|---|---|---|---|---|---|
| `POST` | `/api/eventos-partido` | `EventoPartidoRequest` | `EventoPartidoResponse` | `201` | Registrar un evento (actualiza marcador/stats automáticamente) |
| `GET` | `/api/eventos-partido/partido/{partidoId}` | — | `List<EventoPartidoResponse>` | `200` | Listar todos los eventos de un partido |
| `PUT` | `/api/eventos-partido/{id}` | `EventoPartidoUpdateRequest` | `EventoPartidoResponse` | `200` | Actualizar tipo/minuto de evento (revierte efectos previos) |
| `DELETE` | `/api/eventos-partido/{id}` | — | — | `204` | Eliminar evento (revierte efectos en marcador/stats) |

#### Registrar evento (`POST /api/eventos-partido`):

```json
{
  "partidoId": "f0e1d2c3-0000-4000-8000-000000000050",
  "jugadorId": "9a8b7c6d-0000-4000-8000-000000000060",
  "tipoEvento": "GOL_JUGADA",
  "minuto": 34
}
```

#### Diagrama del flujo de procesamiento al registrar un evento:

```text
Registrar evento
       ↓
Validar partido y jugador en BD
       ↓
Comprobar pertenencia (¿Jugador pertenece a equipo local o visitante del partido?)
       ↓
Validar rango del minuto (0 a 130)
       ↓
Guardar EventoPartido en base de datos
       ↓
 ┌─────┴────────────────────────────┐
 ↓                                  ↓
¿Es tipo Gol?              Actualizar Estadísticas
 (GOL_JUGADA, GOL_CABEZA,        (EstadisticaJugadorService)
  GOL_PENAL, GOL_TIRO_LIBRE, GOL)        ↓
 ↓                          Suma goles, asistencias o
Actualizar Marcador          tarjetas según evento
 (PartidoService)
```

#### Manejo del Autogol (`AUTOGOL`):
El evento `AUTOGOL` incrementa automáticamente el marcador del **equipo contrario** al del jugador que cometió la falta. Sin embargo, **no suma goles individuales** en la ficha estadística del futbolista.

#### Validación de pertenencia:
Si el jugador no pertenece ni al equipo local ni al visitante del partido, la API rechaza el registro con `400 Bad Request` ("El jugador no pertenece a ninguno de los equipos del partido").

#### Actualización de evento (`PUT /api/eventos-partido/{id}`):

```json
{
  "tipoEvento": "GOL_PENAL",
  "minuto": 35
}
```

El flujo de actualización aplica una reversión completa antes de asignar los nuevos valores:

```text
Buscar evento existente
       ↓
Revertir efectos del evento anterior (restar gol del marcador / restar estadística)
       ↓
Actualizar tipoEvento y minuto
       ↓
Guardar cambios
       ↓
Aplicar efectos del nuevo evento (sumar nuevo gol / sumar nueva estadística)
```

#### Consideración sobre modificación de eventos en partidos FINALIZADOS:
Si un evento se actualiza o elimina después de que el partido ya fue marcado como `FINALIZADO`, se actualizan el marcador del partido y las estadísticas del jugador. No obstante, **no se recalcula automáticamente la tabla de posiciones ni la columna `partidos_jugados`**.

---

## 10. Estadísticas de Jugadores

### `EstadisticaJugadorController` — `/api/estadisticas-jugadores`

Permite consultar la información acumulada de rendimiento individual por jugador en cada torneo.

| Método | Endpoint | Parámetros Query | Response Body | Estado | Descripción |
|---|---|---|---|---|---|
| `GET` | `/api/estadisticas-jugadores/goleadores/{torneoId}` | `equipoId` (opcional) | `List<EstadisticaJugadorResponse>` | `200` | Consultar el ranking de goleadores |
| `GET` | `/api/estadisticas-jugadores/asistencias/{torneoId}` | `equipoId` (opcional) | `List<EstadisticaJugadorResponse>` | `200` | Consultar el ranking de asistencias |
| `GET` | `/api/estadisticas-jugadores/jugador/{jugadorId}` | — | `EstadisticaJugadorResponse` | `200` | Consultar estadísticas individuales de un jugador |

#### Ejemplo de respuesta (`EstadisticaJugadorResponse`):

```json
{
  "id": "1a2b3c4d-0000-4000-8000-000000000080",
  "jugadorId": "9a8b7c6d-0000-4000-8000-000000000060",
  "jugadorNombre": "Carlos Mendoza",
  "equipoId": "a1b2c3d4-0000-4000-8000-000000000001",
  "equipoNombre": "Atlético Paisas",
  "goles": 8,
  "asistencias": 3,
  "tarjetasAmarillas": 1,
  "tarjetasRojas": 0,
  "partidosJugados": 5
}
```

---

## 11. Tabla de Posiciones

### `TablaPosicionController` — `/api/tabla-posiciones`

Permite consultar el estado de la clasificación del torneo.

| Método | Endpoint | Parámetros Query | Response Body | Estado | Descripción |
|---|---|---|---|---|---|
| `GET` | `/api/tabla-posiciones/{torneoId}` | `equipoId` (opcional) | `List<TablaPosicionResponse>` | `200` | Consultar la tabla de posiciones ordenada |

#### Ejemplo de respuesta (`TablaPosicionResponse`):

```json
[
  {
    "equipoId": "a1b2c3d4-0000-4000-8000-000000000001",
    "equipoNombre": "Atlético Paisas",
    "puntos": 9,
    "partidosJugados": 3,
    "partidosGanados": 3,
    "partidosEmpatados": 0,
    "partidosPerdidos": 0,
    "golesFavor": 7,
    "golesContra": 2,
    "diferenciaGoles": 5
  }
]
```

#### Criterios de ordenamiento:
La lista devuelta por la API se entrega ordenada utilizando el siguiente criterio de desempatado:
1. **Puntos** (de mayor a menor).
2. **Diferencia de goles** (de mayor a menor).
3. **Goles a favor** (de mayor a menor).

---

# Resumen general de controladores

| Controller | Entidad Administrada | Operación Principal |
|---|---|---|
| `TorneoController` | Torneo | Gestión de torneos |
| `EquipoController` | Equipo | Gestión de equipos e inicialización en la tabla |
| `JugadorController` | Jugador | Administración de futbolistas |
| `UsuarioController` | Usuario | Cuentas de usuario |
| `RolController` | Rol | Roles del sistema |
| `ArbitroController` | Arbitro | Gestión de arbitraje |
| `JornadaController` | Jornada | Programación de fechas y Fixture |
| `PartidoController` | Partido | Programación, estado y marcadores |
| `EventoPartidoController` | EventoPartido | Registro de goles, tarjetas e incidencias |
| `EstadisticaJugadorController` | EstadisticaJugador | Consulta de rankings de goleadores y asistencias |
| `TablaPosicionController` | TablaPosicion | Consulta de la clasificación |

---

# Resumen de cambios respecto a versiones anteriores de la API

Para clientes o aplicaciones frontend que consuman estos servicios, estos son los cambios importantes respecto a versiones previas:

| Endpoint | Formato Anterior | Formato Actual |
|---|---|---|
| `POST /api/jugadores` | Requería `usuarioId` en el JSON | Requiere `nombre` y `apellido` directamente en `JugadorRequest` |
| `POST /api/jornadas` | Parámetros URL (`?torneoId=...`) | Body JSON `JornadaRequest` |
| `POST /api/partidos` | Parámetros URL | Body JSON `PartidoRequest` |
| `PUT /api/partidos/{id}/estado` | `?estado=FINALIZADO` | Body JSON `{ "estado": "FINALIZADO" }` en mayúsculas |
| `PUT /api/partidos/{id}/marcador` | `?golesLocal=2&golesVisitante=1` | Body JSON `MarcadorRequest` |
| `POST /api/eventos-partido` | Parámetros URL | Body JSON `EventoPartidoRequest` |
| `PUT /api/eventos-partido/{id}` | Query Params | Body JSON `EventoPartidoUpdateRequest` |
| Todos los `POST`/`PUT` | Se enviaba la entidad JPA anidada | Se envían únicamente los IDs primarios en el DTO |
| Respuestas de Error | Estructura no estandarizada | `{ "error": "..." }` o map `{ campo: mensaje }` |