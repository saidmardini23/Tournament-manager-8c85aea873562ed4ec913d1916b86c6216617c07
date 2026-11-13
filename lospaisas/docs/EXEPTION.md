# Documentación de Excepciones — Sistema de Torneos

## 1. Propósito

El manejo de errores está separado en dos mecanismos, según el tipo de controller que origina la excepción:

| Mecanismo | Clase | Atiende | Responde |
|---|---|---|---|
| API REST | `GlobalExceptionHandler` | Controllers con `@RestController` | JSON con el código HTTP correspondiente |
| Vistas Thymeleaf | `VistaExceptionHandler` | Controllers del paquete `com.lospaisas.controllers.web` | Redirect con mensaje flash |

Los Services lanzan excepciones; los Controllers no arman respuestas de error. Cada handler traduce la excepción al formato que corresponde a su tipo de cliente.

```text
Service lanza excepción
        ↓
¿Qué controller la originó?
        │
        ├── @RestController ──→ GlobalExceptionHandler ──→ ResponseEntity { status, JSON }
        │
        └── controllers.web ──→ VistaExceptionHandler ──→ flash "error" + redirect
```

---

## 2. Excepciones propias del proyecto

Ambas están en `com.lospaisas.exceptions`.

| Excepción | Cuándo se lanza | HTTP (API) |
|---|---|---|
| `RecursoNoEncontradoException` | El recurso, o una relación referenciada por ID, no existe | 404 |
| `ValidacionException` | Se incumple una regla de negocio | 400 |

### Ejemplos de uso en los Services

```java
private Torneo obtenerTorneo(UUID id) {
    return torneoRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado"));
}

if (equipoLocal.getId().equals(equipoVisitante.getId())) {
    throw new ValidacionException("El equipo local y visitante no pueden ser el mismo");
}
```

### Reglas de negocio validadas

| Service | Regla | Mensaje |
|---|---|---|
| `PartidoServiceImpl.crearPartido` | Equipo local y visitante distintos | "El equipo local y visitante no pueden ser el mismo" |
| `PartidoServiceImpl.actualizarMarcador` | Goles no negativos | "Los goles no pueden ser negativos" |
| `EventoPartidoServiceImpl.registrarEvento` | El jugador pertenece a uno de los dos equipos | "El jugador no pertenece a ninguno de los equipos del partido" |
| `EventoPartidoServiceImpl.actualizarEvento` | El jugador sigue perteneciendo a uno de los dos equipos | "El jugador ya no pertenece a ninguno de los equipos del partido" |
| `EventoPartidoServiceImpl` (`validarMinuto`) | Minuto no negativo | "El minuto no puede ser negativo" |
| `EquipoServiceImpl.actualizarEquipo` | El torneo de un equipo ya registrado no se puede cambiar | "No se puede cambiar el torneo de un equipo ya registrado" |
| `EquipoServiceImpl.eliminarEquipo` | Un equipo con partidos no se puede eliminar | "No se puede eliminar un equipo que tiene partidos registrados" |
| `EquipoWebController` (crear y editar) | Debe seleccionarse un torneo | "Debes seleccionar un torneo válido." |

### Reglas pendientes de implementar

| Service | Regla | Mensaje sugerido |
|---|---|---|
| `TorneoServiceImpl.eliminarTorneo` | Un torneo con equipos o jornadas no se puede eliminar | "No se puede eliminar un torneo que ya tiene equipos o jornadas" |
| `EquipoServiceImpl.eliminarEquipo` | Un equipo con jugadores no se puede eliminar | "No se puede eliminar un equipo que tiene jugadores registrados" |

---

## 3. API REST — `GlobalExceptionHandler`

Anotado con `@RestControllerAdvice(annotations = RestController.class)`: solo atiende controllers marcados con `@RestController`. Todas las respuestas son JSON.

| # | Excepción capturada | HTTP | Cuerpo de la respuesta |
|---|---|---|---|
| 1 | `RecursoNoEncontradoException` | 404 | `{ "error": "<mensaje de la excepción>" }` |
| 2 | `ValidacionException` | 400 | `{ "error": "<mensaje de la excepción>" }` |
| 3 | `MethodArgumentNotValidException` (`@Valid` sobre el Request) | 400 | `{ "<campo>": "<mensaje>", ... }` (un par por campo inválido) |
| 3.1 | `HttpMessageNotReadableException` (JSON mal formado, enum, fecha o UUID inválido en el body) | 400 | `{ "error": "El cuerpo de la petición es inválido o tiene un formato incorrecto (revisa JSON, enums, fechas y UUIDs)" }` |
| 3.2 | `MethodArgumentTypeMismatchException` (UUID o tipo inválido en `@PathVariable` / `@RequestParam`) | 400 | `{ "error": "El parámetro '<nombre>' tiene un formato inválido" }` |
| 3.3 | `DataIntegrityViolationException` (unicidad o FK en la BD) | 409 | `{ "error": "La operación viola una restricción de datos (registro duplicado o con relaciones activas)" }` |
| 4 | `Exception` (cualquier otra) | 500 | `{ "error": "Ha ocurrido un error interno en el servidor" }` |

### Notas

- El cuerpo del handler 3 es distinto al resto: devuelve un mapa campo → mensaje, no la clave `error`. El cliente debe tratar ambos formatos.
- Errores web propios de Spring (ruta inexistente, 405, 415, falta un parámetro): el handler 4 detecta que la excepción es un `ErrorResponse` y conserva su status original con `{ "error": "<detalle>" }`, en vez de convertirlo en 500. Si el detalle es nulo, usa "Solicitud inválida".
- Logs: el 3.3 registra un `warn` con la excepción; el 4 registra un `error`.
- Spring elige el handler más específico, así que `Exception` solo atrapa lo que ningún otro cubre.
- Un controller REST sin `@RestController` queda **sin** este handler. Todos los controllers REST deben usarlo.

---

## 4. Vistas Thymeleaf — `VistaExceptionHandler`

Anotado con `@ControllerAdvice(basePackages = "com.lospaisas.controllers.web")`: solo atiende controllers del paquete `controllers.web` (y subpaquetes). No responde JSON: guarda el mensaje como atributo flash `error` y redirige.

| Excepción capturada | Mensaje en el flash `error` | Log |
|---|---|---|
| `RecursoNoEncontradoException` | Mensaje de la excepción | — |
| `ValidacionException` | Mensaje de la excepción | — |
| `DataIntegrityViolationException` | "No se puede completar la operación porque el registro tiene datos relacionados" | `warn` con la excepción |
| `Exception` (cualquier otra) | "Ha ocurrido un error inesperado. Intenta de nuevo" | `error` con la excepción |

### A dónde redirige

El destino se calcula a partir del header `Referer` (solo se usa la **ruta**, nunca el host):

| Situación | Destino |
|---|---|
| Hay `Referer` y su ruta termina en `/confirmar-eliminar` | La misma ruta cambiando el final por `/opciones` |
| Hay `Referer` con otra ruta | Esa ruta (sin parámetros de consulta) |
| La ruta de destino es igual a la de la petición actual | `/` (evita un bucle de redirecciones) |
| No hay `Referer`, está vacío o no se puede interpretar | `/` |

El `/confirmar-eliminar` → `/opciones` conserva el comportamiento anterior de los borrados: el error se ve en la página de opciones del registro.

### Requisito en las plantillas

Toda página a la que pueda redirigir el handler debe mostrar el flash `error` (por ejemplo, con `th:if="${error}"`). Las plantillas de `/opciones` ya lo hacen; hay que revisar el resto.

### Efecto sobre el mensaje de los borrados

Si un borrado falla por una restricción de la base de datos, el usuario ve el mensaje genérico de integridad. Para un mensaje específico (por ejemplo, "el árbitro está asignado a partidos"), la validación debe hacerse en el Service con `ValidacionException`.

---

## 5. Cómo debe usar las excepciones un controller web

| Tipo de acción | Qué hace el controller | Qué hace el handler |
|---|---|---|
| Ver una página (GET) | Llama al service y arma el `Model`, sin try/catch | Si el recurso no existe, redirige con el mensaje |
| Eliminar (POST `/{id}/eliminar`) | Llama a un solo método del service, sin try/catch ni `RedirectAttributes` | Redirige a `/opciones` con el mensaje de la regla |
| Crear o editar (POST con formulario) | `try` con `catch (ValidacionException \| RecursoNoEncontradoException e)`: repinta el formulario con los datos escritos y el `error` | Atiende cualquier otra excepción (por ejemplo, una restricción de la BD) |

### Por qué los formularios conservan un catch

Al repintar el formulario hay que devolver lo que el usuario escribió y las listas de los `<select>`; eso solo lo sabe el controller. Si lo hiciera el handler, el usuario perdería lo escrito. Por eso el catch se acota a las dos excepciones de negocio y el resto pasa al handler. Efecto: un error inesperado al crear o editar lleva al usuario a la página anterior con el mensaje, y el formulario puede aparecer vacío.

```java
try {
    equipoService.crearEquipo(request);
    return "redirect:/torneos/" + torneoId;
} catch (ValidacionException | RecursoNoEncontradoException e) {
    model.addAttribute("error", e.getMessage());
    model.addAttribute("equipo", new EquipoRequest(nombre, entrenador, torneoId));
    return "equipo/formularioEquipo";
}
```

### Qué no debe hacer un controller web

- No capturar `Exception` genérica ni mostrar `e.getMessage()` de errores inesperados.
- No orquestar borrados en cascada ni varias escrituras: cada llamada a un service es una transacción distinta, así que un fallo a la mitad deja datos a medio modificar.
- No lanzar excepciones de negocio para reglas del dominio: eso va en el Service. La única excepción es validar que un parámetro obligatorio del formulario llegó.

---

## 6. Validación de formato vs. regla de negocio

| Tipo de validación | Dónde se hace | Excepción | API | Vista |
|---|---|---|---|---|
| Formato (nulos, longitud, rangos) | `@Valid` sobre el Request en el controller REST | `MethodArgumentNotValidException` | 400 | — |
| Existencia de un recurso o relación | `ServiceImpl` | `RecursoNoEncontradoException` | 404 | Redirect con mensaje |
| Regla de negocio | `ServiceImpl` | `ValidacionException` | 400 | Formulario repintado o redirect con mensaje |
| Restricción de la BD (FK, unicidad) | Base de datos | `DataIntegrityViolationException` | 409 | Redirect con mensaje genérico |

La violación de integridad es la **última línea de defensa**: indica que una regla de negocio no se validó antes en el Service. Lo correcto es validar primero y lanzar `ValidacionException` con un mensaje específico.

### Ejemplo: eliminar un recurso con relaciones

```java
torneoRepository.delete(obtenerTorneo(id));

if (equipoRepository.existsByTorneoId(id)) {
    throw new ValidacionException("No se puede eliminar un torneo que tiene equipos");
}
```

El primer caso deja que la base de datos rechace el DELETE (409 en la API, mensaje genérico en la vista). El segundo da un 400 con un mensaje claro.

---

## 7. Cómo agregar una excepción nueva

1. Crear la clase en `com.lospaisas.exceptions`.
2. Agregar un método `@ExceptionHandler` en `GlobalExceptionHandler` (status y cuerpo `{ "error": ... }`).
3. Agregar el handler equivalente en `VistaExceptionHandler` (mensaje flash y redirect), o confirmar que el handler genérico ya la cubre de forma aceptable.
4. Documentarla en este archivo (secciones 2, 3 y 4).
5. Lanzarla solo desde los Services.

---

## 8. Verificación de la separación

Dos comandos para comprobar el alcance de los handlers (deben salir vacíos):

```bash
grep -rL "@RestController" src/main/java/com/lospaisas/controllers --include=*.java | grep -v "/web/"
grep -rl "@RestController" src/main/java/com/lospaisas/controllers/web
```

- El primero lista controllers fuera de `web` sin `@RestController`: se quedarían sin handler JSON.
- El segundo lista `@RestController` dentro de `web`: los atraparían los dos handlers a la vez.

Pruebas manuales con la app corriendo:

| Prueba | Resultado esperado |
|---|---|
| `GET /api/torneos/00000000-0000-0000-0000-000000000000` | 404 con `{"error":"Torneo no encontrado"}` |
| `GET /api/torneos/abc` | 400 con `{"error":"El parámetro 'id' tiene un formato inválido"}` |
| POST a la API con body inválido | 400 con JSON |
| `DELETE /api/equipos/{id}` de un equipo con partidos | 400 con `{"error":"No se puede eliminar un equipo que tiene partidos registrados"}` |
| Abrir `/torneos/00000000-0000-0000-0000-000000000000` en el navegador | Redirect a la página anterior con el mensaje de error, nunca JSON |
| Eliminar desde la interfaz un equipo con partidos | Vuelve a `/opciones` con el mensaje de la regla |
| Crear un equipo sin torneo desde la interfaz | Formulario repintado con los datos escritos y el error |

---

## 9. Recomendaciones

1. Usar `ValidacionException` para toda regla de negocio; no usar excepciones genéricas (`RuntimeException`, `IllegalArgumentException`).
2. Mensajes claros y orientados al usuario: qué no se puede hacer y por qué.
3. No colocar validaciones de negocio en los Controllers.
4. Mantener el formato `{ "error": "..." }` en las respuestas JSON nuevas, salvo las de `@Valid`.
5. Si aparece un 409 (o el mensaje genérico de integridad en la vista) en una operación común, es señal de que falta una validación en el Service.
6. Los controllers REST siempre con `@RestController` y fuera de `controllers.web`; los de vistas siempre con `@Controller` y dentro de `controllers.web`.

Este documento describe el comportamiento actual del manejo de excepciones y debe actualizarse cuando se agreguen nuevas excepciones o reglas de negocio.