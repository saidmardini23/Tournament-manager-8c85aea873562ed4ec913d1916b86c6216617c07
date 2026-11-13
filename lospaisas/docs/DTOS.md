# DTOs (Data Transfer Objects) — Request / Response (`com.lospaisas.dto`)

Este documento especifica la arquitectura de la capa de **DTOs** (Data Transfer Objects) del backend: qué son, por qué existen, cómo están organizados, las reglas de validación de entrada, las estructuras de respuesta, el funcionamiento del `DtoMapper` y la guía paso a paso para extender la capa.

---

## 1. ¿Por qué existen los DTOs?

Antes de la refactorización, los controladores recibían y devuelvían directamente las **entidades JPA** de dominio (`Torneo`, `Equipo`, `Jugador`, `Usuario`...). Esta práctica causaba serios problemas de seguridad, acoplamiento y mantenibilidad:

| Problema | Consecuencia Técnica |
|---|---|
| **Fuga de datos sensibles** | La entidad expone todos sus atributos al cliente. Por ejemplo, `Usuario.passwordHash` se devolvía directamente en el JSON de respuesta. |
| **Vulnerabilidad de *Mass Assignment*** | El cliente podía enviar atributos que no debía controlar (como el `id` primario, audit fields o relaciones JPA completas) modificando la base de datos inadvertidamente. |
| **Mezcla de responsabilidades** | Las validaciones de entrada (`@NotBlank`, `@Size`, `@Email`) contaminaban la capa de dominio/persistencia JPA. |
| **Sobrecarga en la red y la memoria** | Para asociar un equipo a un torneo, el cliente debía enviar la estructura JSON completa del torneo con todas sus relaciones en lugar de una simple referencia de ID. |

### La Solución Arquitectónica

Separar limpiamente la persistencia de la comunicación:

- **Entidad JPA:** Representa el modelo relacional del dominio en la base de datos. Permanece encapsulada en la capa de servicios y repositorios.
- **DTO Request:** Captura, valida y transporta lo que **entra** al sistema desde el cliente (REST JSON o Formulario HTML).
- **DTO Response:** Estructura de solo lectura con lo que **sale** del sistema hacia el cliente.

```text
Cliente (JSON REST / HTML Form)
     ↓
XRequest  ──  @Valid / @ModelAttribute valida el formato (Controller)
     ↓
ServiceImpl  ──  resuelve IDs a Entidades, aplica reglas de negocio y transacciones
     ↓
Entidad JPA ↔ Repository ↔ Base de datos (MySQL / H2)
     ↓
DtoMapper  ──  convierte la Entidad JPA en XResponse (null-safe)
     ↓
XResponse (JSON REST / Modelo Thymeleaf)
```

---

## 2. Ubicación en el Proyecto

```text
com.lospaisas.dto
├── request/        # DTOs de entrada (con anotaciones de Bean Validation)
├── response/       # DTOs de salida (para API REST y Vistas Thymeleaf)
└── DtoMapper.java  # Clase utilitaria static (Conversión Entidad -> Response)
```

---

## 3. Reglas de Diseño

1. **Inmutabilidad con `record` de Java:** Todos los DTOs se implementan como `record` de Java. Son inmutables por definición, no requieren Lombok (`@Data`, `@Getter`, `@Setter`) y las anotaciones de validación se colocan directamente sobre los componentes del record.
2. **Relaciones mediante IDs:** Las relaciones entre entidades se envían como identificadores de clave primaria (`UUID`), no como objetos anidados. Por ejemplo, `EquipoRequest` recibe `"torneoId": "..."`. El `ServiceImpl` busca la entidad en la BD y lanza `RecursoNoEncontradoException` (404) si no existe.
3. **Validaciones en los Request:** Las validaciones de formato (`@NotBlank`, `@NotNull`, `@Size`, `@Min`, `@Max`, `@Email`) viven exclusivamente en los DTOs de solicitud (`request/`). Las entidades JPA no llevan estas anotaciones.
4. **Protección de Datos Sensibles:** Los DTOs de salida (`response/`) filtran cualquier dato confidencial. `UsuarioResponse` nunca incluye `passwordHash`.
5. **Inclusión de Nombres Descriptivos:** Para evitar que el cliente tenga que hacer peticiones adicionales para mostrar un texto, los Response incluyen el nombre de la relación principal junto con su ID (ej. `torneoId` y `torneoNombre`, `equipoId` y `equipoNombre`).
6. **Separación de Validaciones:** La validación de **formato** la hace `@Valid` en los controladores REST/Web. Las **reglas de negocio** (equipo local distinto del visitante, jugador perteneciente al partido, etc.) se procesan en la capa de `ServiceImpl`.
7. **Independencia de Jugador y Usuario:** El registro de un futbolista no depende de una cuenta de usuario. `JugadorRequest` recibe los datos personales (`nombre`, `apellido`, `equipoId`, `posicion`) directamente sin requerir `usuarioId`.
8. **DTOs de Agregación / Fixture:** DTOs como `JornadaFixtureResponse` agrupan estructuras complejas (ej. una jornada con su lista de partidos) evitando procesamientos de ordenamiento o filtrado en los controladores o plantillas Thymeleaf.

---

## 4. Requests (DTOs de Entrada)

Se envían como cuerpo JSON (`@RequestBody`) en APIs REST o mediante enlace de datos (`@ModelAttribute`) en formularios HTML de Thymeleaf. Todos se validan con `@Valid`.

| DTO | Utilizado en | Campo | Anotaciones de Validación / Reglas |
|---|---|---|---|
| `TorneoRequest` | `POST/PUT /api/torneos`<br>Form Torneo | `nombre`<br>`descripcion`<br>`fechaInicio`<br>`fechaFin`<br>`estado`<br>`formato` | `@NotBlank`, `@Size(min = 3, max = 100)`<br>`@Size(max = 500)` (opcional)<br>`@NotNull` (`YYYY-MM-DD`)<br>Opcional (no puede ser anterior a `fechaInicio`)<br>Opcional, máx. 30 (enum `EstadoTorneo`)<br>Opcional, máx. 50 (enum `FormatoTorneo`) |
| `EquipoRequest` | `POST/PUT /api/equipos`<br>Form Equipo | `nombre`<br>`entrenador`<br>`torneoId` | `@NotBlank`, `@Size(min = 3, max = 50)` (único en BD)<br>`@NotBlank`, `@Size(min = 3, max = 80)`<br>`@NotNull` (UUID de torneo existente) |
| `JugadorRequest` | `POST/PUT /api/jugadores`<br>Form Jugador | `nombre`<br>`apellido`<br>`equipoId`<br>`posicion` | `@NotBlank`, `@Size(min = 2, max = 50)`<br>`@NotBlank`, `@Size(min = 2, max = 50)`<br>`@NotNull` (UUID de equipo existente)<br>`@NotNull` (enum `Posiciones`) |
| `UsuarioRequest` | `POST /api/usuarios`<br>Form Registro | `nombre`<br>`email`<br>`password`<br>`rolId` | `@NotBlank`, `@Size(min = 3, max = 50)`<br>`@NotBlank`, `@Email`, `@Size(max = 100)` (único)<br>`@NotBlank`, `@Size(min = 8, max = 72)`<br>`@NotNull` (UUID de rol existente) |
| `UsuarioUpdateRequest` | `PUT /api/usuarios/{id}`<br>Form Editar Usuario | `nombre`, `email`, `rolId`<br>`password` | Mismas validaciones que en `UsuarioRequest`<br>**Opcional**: si es `null` o vacío, se conserva la actual; si viene, `@Size(min = 8, max = 72)` |
| `RolRequest` | `POST/PUT /api/roles` | `nombre` | `@NotNull` (enum `Roles`: `ADMIN`, `ORGANIZADOR`, etc.) |
| `ArbitroRequest` | `POST/PUT /api/arbitros`<br>Form Árbitro | `nombre`, `apellido`<br>`experiencia`<br>`licencia` | `@NotBlank`, `@Size(min = 2, max = 50)`<br>`@NotNull`, `@Min(0)`, `@Max(60)` (años)<br>Opcional, `@Size(max = 30)` |
| `JornadaRequest` | `POST /api/jornadas`<br>Form Jornada | `torneoId`<br>`numeroJornada` | `@NotNull` (UUID de torneo existente)<br>`@NotNull`, `@Min(1)` |
| `PartidoRequest` | `POST /api/partidos`<br>Form Partido | `jornadaId`, `equipoLocalId`, `equipoVisitanteId`, `arbitroId`<br>`fecha` | Todos `@NotNull` (UUIDs de entidades existentes)<br>`@NotNull` (`YYYY-MM-DDTHH:mm:ss`) |
| `CambiarEstadoRequest` | `PUT /api/partidos/{id}/estado` | `estado` | `@NotNull` (enum `EstadoPartido`) |
| `MarcadorRequest` | `PUT /api/partidos/{id}/marcador` | `golesLocal`, `golesVisitante` | `@NotNull`, `@Min(0)`, `@Max(99)` |
| `EventoPartidoRequest` | `POST /api/eventos-partido` | `partidoId`, `jugadorId`<br>`tipoEvento`<br>`minuto` | `@NotNull` (UUIDs existentes)<br>`@NotNull` (enum `TipoEvento`)<br>`@NotNull`, `@Min(0)`, `@Max(130)` |
| `EventoPartidoUpdateRequest` | `PUT /api/eventos-partido/{id}` | `tipoEvento`<br>`minuto` | `@NotNull` (enum `TipoEvento`)<br>`@NotNull`, `@Min(0)`, `@Max(130)` |

---

### Ejemplos de Implementación de Request (Records Java)

#### `JugadorRequest`
```java
package com.lospaisas.dto.request;

import com.lospaisas.models.enums.Posiciones;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record JugadorRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    String nombre,

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    String apellido,

    @NotNull(message = "El equipo es obligatorio")
    UUID equipoId,

    @NotNull(message = "La posición es obligatoria")
    Posiciones posicion
) {}
```

#### `PartidoRequest`
```java
package com.lospaisas.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record PartidoRequest(
    @NotNull(message = "La jornada es obligatoria")
    UUID jornadaId,

    @NotNull(message = "El equipo local es obligatorio")
    UUID equipoLocalId,

    @NotNull(message = "El equipo visitante es obligatorio")
    UUID equipoVisitanteId,

    @NotNull(message = "El árbitro es obligatorio")
    UUID arbitroId,

    @NotNull(message = "La fecha y hora del partido son obligatorias")
    LocalDateTime fecha
) {}
```

---

## 5. Responses (DTOs de Salida)

| DTO | Componentes / Campos Retornados |
|---|---|
| `TorneoResponse` | `UUID id`, `String nombre`, `String descripcion`, `LocalDate fechaInicio`, `LocalDate fechaFin`, `String estado`, `String formato` |
| `EquipoResponse` | `UUID id`, `String nombre`, `String entrenador`, `UUID torneoId`, `String torneoNombre` |
| `JugadorResponse` | `UUID id`, `String nombre`, `String apellido`, `UUID equipoId`, `String equipoNombre`, `Posiciones posicion` |
| `UsuarioResponse` | `UUID id`, `String nombre`, `String email`, `String rol` (Nombre del rol). **Jamás exone `passwordHash`** |
| `RolResponse` | `UUID id`, `Roles nombre` |
| `ArbitroResponse` | `UUID id`, `String nombre`, `String apellido`, `Integer experiencia`, `String licencia` |
| `JornadaResponse` | `UUID id`, `UUID torneoId`, `Integer numeroJornada` |
| `JornadaFixtureResponse` | `JornadaResponse jornada`, `List<PartidoResponse> partidos` |
| `PartidoResponse` | `UUID id`, `UUID jornadaId`, `UUID equipoLocalId`, `String equipoLocalNombre`, `UUID equipoVisitanteId`, `String equipoVisitanteNombre`, `UUID arbitroId`, `String arbitroNombre`, `Integer golesLocal`, `Integer golesVisitante`, `EstadoPartido estado`, `LocalDateTime fecha` |
| `EventoPartidoResponse` | `UUID id`, `UUID partidoId`, `UUID jugadorId`, `String jugadorNombre`, `TipoEvento tipoEvento`, `Integer minuto`, `String descripcion` |
| `EstadisticaJugadorResponse` | `UUID id`, `UUID jugadorId`, `String jugadorNombre`, `UUID equipoId`, `String equipoNombre`, `Integer goles`, `Integer asistencias`, `Integer tarjetasAmarillas`, `Integer tarjetasRojas`, `Integer partidosJugados` |
| `TablaPosicionResponse` | `UUID equipoId`, `String equipoNombre`, `Integer puntos`, `Integer partidosJugados`, `Integer partidosGanados`, `Integer partidosEmpatados`, `Integer partidosPerdidos`, `Integer golesFavor`, `Integer golesContra`, `Integer diferenciaGoles` |

---

## 6. `DtoMapper`

Clase utilitaria de alcance general (`public final class DtoMapper`) con constructor privado y métodos `public static` sobrecargados. Transforma **Entidades JPA $\rightarrow$ Response DTOs**.

### Principios Operativos de `DtoMapper`

1. **Métodos Sobrecargados `toResponse(...)`:** Java infiere qué método ejecutar basándose en el tipo de entidad JPA pasado por parámetro (`Torneo`, `Equipo`, `Jugador`, etc.).
2. **Protección Contra Nulos (*Null-Safety*):** Utiliza operadores ternarios (`e != null ? e.getId() : null`) para evitar lanzamientos de `NullPointerException` en caso de relaciones opcionales o desvinculadas.
3. **Independencia de Entidades:** Construye `jugadorNombre` concatenando `j.getNombre() + " " + j.getApellido()` directamente desde la entidad `Jugador`, garantizando la desvinculación de la entidad `Usuario`.
4. **Conversión Inversa Excluida:** El Mapper **no realiza conversión Request $\rightarrow$ Entidad**. Esta lógica reside en cada `ServiceImpl`, donde es necesario consultar los Repositories para convertir IDs a entidades gestionadas por JPA.
5. **Contexto Transaccional:** Al ser invocado desde Services anotados con `@Transactional`, el Mapper puede navegar relaciones de carga diferida (`FetchType.LAZY`) dentro de la misma sesión de persistencia sin causar `LazyInitializationException`.

---

### Código Fuente Representativo de `DtoMapper`

```java
package com.lospaisas.dto;

import com.lospaisas.dto.response.*;
import com.lospaisas.models.*;

public final class DtoMapper {

    private DtoMapper() {}

    public static EquipoResponse toResponse(Equipo equipo) {
        if (equipo == null) return null;
        Torneo torneo = equipo.getTorneo();
        return new EquipoResponse(
            equipo.getId(),
            equipo.getNombre(),
            equipo.getEntrenador(),
            torneo != null ? torneo.getId() : null,
            torneo != null ? torneo.getNombre() : null
        );
    }

    public static JugadorResponse toResponse(Jugador jugador) {
        if (jugador == null) return null;
        Equipo equipo = jugador.getEquipo();
        return new JugadorResponse(
            jugador.getId(),
            jugador.getNombre(),
            jugador.getApellido(),
            equipo != null ? equipo.getId() : null,
            equipo != null ? equipo.getNombre() : null,
            jugador.getPosicion()
        );
    }

    public static PartidoResponse toResponse(Partido partido) {
        if (partido == null) return null;
        Equipo local = partido.getEquipoLocal();
        Equipo visitante = partido.getEquipoVisitante();
        Arbitro arbitro = partido.getArbitro();
        
        return new PartidoResponse(
            partido.getId(),
            partido.getJornada() != null ? partido.getJornada().getId() : null,
            local != null ? local.getId() : null,
            local != null ? local.getNombre() : null,
            visitante != null ? visitante.getId() : null,
            visitante != null ? visitante.getNombre() : null,
            arbitro != null ? arbitro.getId() : null,
            arbitro != null ? arbitro.getNombre() + " " + arbitro.getApellido() : null,
            partido.getGolesLocal(),
            partido.getGolesVisitante(),
            partido.getEstado(),
            partido.getFecha()
        );
    }

    public static EstadisticaJugadorResponse toResponse(EstadisticaJugador s) {
        if (s == null) return null;
        Jugador j = s.getJugador();
        Equipo e = s.getEquipo();
        return new EstadisticaJugadorResponse(
            s.getId(),
            j != null ? j.getId() : null,
            j != null ? j.getNombre() + " " + j.getApellido() : null,
            e != null ? e.getId() : null,
            e != null ? e.getNombre() : null,
            s.getGoles(),
            s.getAsistencias(),
            s.getTarjetasAmarillas(),
            s.getTarjetasRojas(),
            s.getPartidosJugados()
        );
    }
}
```

---

## 7. Paso a Paso: Cómo Agregar un Nuevo DTO (Checklist)

Supongamos que se incorpora la entidad `Sancion`.

1. **Crear `SancionRequest`:** En `com.lospaisas.dto.request` como un `record` Java. Agregar las anotaciones de validation (`@NotNull`, `@NotBlank`, etc.) y definir las relaciones como `UUID`.
2. **Crear `SancionResponse`:** En `com.lospaisas.dto.response` como un `record` Java inmutable con las propiedades públicas necesarias.
3. **Agregar el método en `DtoMapper`:** Crear la sobrecarga `public static SancionResponse toResponse(Sancion s)` verificando la protección *null-safe* de relaciones.
4. **Actualizar la interfaz del Servicio:** Definir métodos usando los DTOs: `SancionResponse registrarSancion(SancionRequest request)`.
5. **Implementar en `SancionServiceImpl`:**
   - Buscar los IDs relacionados en sus repositorios (lanzando `RecursoNoEncontradoException` si no existen).
   - Instanciar la entidad `Sancion`.
   - Guardar en el repositorio (`sancionRepository.save(...)`).
   - Retornar `DtoMapper.toResponse(sancionGuardada)`.
6. **Actualizar el Controlador:** Usar `@Valid @RequestBody SancionRequest` en REST o `@ModelAttribute SancionRequest` en Controllers de vistas Web.
7. **Documentar:** Actualizar este documento (`DTOS.md`), `SERVICES.md` y `ENDPOINTS.md`.

---

## 8. Limitaciones Conocidas y Consideraciones Técnicas

- **Almacenamiento de Contraseñas:** Actualmente, la contraseña recibida en `UsuarioRequest.password()` se guarda de manera directa en la columna `passwordHash` sin encriptación. Al integrar Spring Security, la modificación queda aislada en `UsuarioServiceImpl` (aplicando `passwordEncoder.encode(...)`). El límite `@Size(max = 72)` sobre el password ya está preparado para el tope máximo de BCrypt.
- **Asignación de Roles Públcos:** El campo `rolId` en `UsuarioRequest` puede ser enviado por cualquier cliente debido a la ausencia actual de roles/autorizaciones a nivel de endpoint. Con seguridad activa, el asignador de roles debe restringirse a administradores o asignarse por defecto en el servicio.
- **Traducción de Restricciones Unicas de Base de Datos:** Los campos únicos (`Equipo.nombre`, `Usuario.email`, `Rol.nombre`) no se consultan previamente en los DTOs de entrada. Si se envía un duplicado, la base de datos lanza `DataIntegrityViolationException`, la cual es capturada por `VistaExceptionHandler` / `GlobalExceptionHandler` retornando un código `409 Conflict` con un mensaje descriptivo.
- **Migración de la estructura de Jugadores:** Se desvinculó la columna `id_usuario` de la tabla `jugadores`. La información del deportista habita completamente en la entidad `Jugador`. En entornos existentes se debe correr la migración SQL correspondiente antes de eliminar la columna antigua.

---

Este documento refleja la arquitectura definitiva de la capa de transporte de datos del proyecto. Debe actualizarse obligatoriamente ante cualquier cambio en validaciones, nuevos componentes o modificaciones de DTOs.