# Base de Datos — Torneo Manager (Los Paisas)

Este documento detalla el diseño relacional, el diagrama Entidad-Relación y el diccionario de datos de la base de datos MySQL utilizada por el sistema.

---

## 📐 Diagrama Entidad-Relación (Mermaid)

```mermaid
erDiagram
    usuarios {
        uuid id PK
        string correo
        string nombre
        string password_hash
        string estado
    }

    roles {
        uuid id PK
        string nombre
    }

    roles_usuario {
        uuid id_usuario FK
        uuid id_rol FK
    }

    torneos {
        uuid id PK
        string nombre
        string descripcion
        date fecha_inicio
        date fecha_fin
        string estado
        string formato
    }

    equipos {
        uuid id PK
        uuid id_torneo FK
        string nombre
        string entrenador
    }

    jugadores {
        uuid id PK
        uuid id_usuario FK
        uuid id_equipo FK
        string posicion
    }

    arbitros {
        uuid id PK
        string nombre
        int experiencia
        string licencia
    }

    jornadas {
        uuid id PK
        uuid id_torneo FK
        int numero_jornada
    }

    partidos {
        uuid id PK
        uuid id_jornada FK
        uuid id_equipo_local FK
        uuid id_equipo_visitante FK
        uuid id_arbitro FK
        int goles_local
        int goles_visitante
        enum estado_partido
        datetime fecha
    }

    eventos_partido {
        uuid id PK
        uuid id_partido FK
        uuid id_jugador FK
        enum tipo_evento
        int minuto
        string descripcion
    }

    tabla_posiciones {
        uuid id PK
        uuid id_torneo FK
        uuid id_equipo FK
        int puntos
        int partidos_jugados
        int partidos_ganados
        int partidos_empatados
        int partidos_perdidos
        int goles_favor
        int goles_contra
        int diferencia_goles
    }

    estadisticas_jugadores {
        uuid id PK
        uuid id_torneo FK
        uuid id_jugador FK
        uuid id_equipo FK
        int goles
        int asistencias
        int tarjetas_amarillas
        int tarjetas_rojas
        int partidos_jugados
    }

    usuarios ||--o{ roles_usuario : "tiene asignado"
    roles ||--o{ roles_usuario : "pertenece a"
    usuarios ||--o| jugadores : "es"

    torneos ||--|{ equipos : "inscribe"
    equipos ||--|{ jugadores : "compone"

    torneos ||--|{ jornadas : "organiza"
    jornadas ||--|{ partidos : "contiene"

    equipos ||--o{ partidos : "juega como local"
    equipos ||--o{ partidos : "juega como visitante"
    arbitros ||--o{ partidos : "pita"

    partidos ||--o{ eventos_partido : "registra"
    jugadores ||--o{ eventos_partido : "realiza"

    torneos ||--|{ tabla_posiciones : "incluye"
    equipos ||--|| tabla_posiciones : "tiene estadisticas en"

    torneos ||--|{ estadisticas_jugadores : "registra en"
    jugadores ||--|| estadisticas_jugadores : "acumula"
    equipos ||--o{ estadisticas_jugadores : "pertenece a"
```
## 🗂️ Diccionario de Tablas

### 1. Usuarios y Seguridad
* **`usuarios`**: Cuentas registradas en el sistema (autenticación).
* **`roles`**: Catálogo de roles de acceso (Ej: `ADMIN`, `USUARIO`, `ORGANIZADOR`).
* **`roles_usuario`**: Tabla intermedia N:M para asignar uno o más roles a un usuario.

### 2. Estructura del Torneo
* **`torneos`**: Información principal del torneo (`fecha_inicio`, `fecha_fin`, `formato`, `estado`).
* **`equipos`**: Clubes o equipos inscritos en un torneo específico (`id_torneo`).
* **`jugadores`**: Mapeo entre un `usuario`, su `equipo` asignado y su `posicion` en el campo.
* **`arbitros`**: Registro de colegiados encargados de impartir justicia en los partidos (`experiencia`, `licencia`).

### 3. Calendario y Encuentros
* **`jornadas`**: Fases o fechas del calendario pertenecientes a un torneo (`numero_jornada`).
* **`partidos`**: Enfrentamiento directo entre dos equipos (`id_equipo_local`, `id_equipo_visitante`), asignado a una jornada y pitado por un árbitro. Mantiene el marcador general y su estado (`PROGRAMADO`, `EN_JUEGO`, `FINALIZADO`, `SUSPENDIDO`).
* **`eventos_partido`**: Sucesos puntuales dentro del encuentro (`GOL`, `ASISTENCIA`, `AMARILLA`, `ROJA`, `SUSTITUCION`) vinculados al jugador que lo realizó y al minuto correspondiente.

### 4. Tablas Acumulativas / Rendimiento
* **`tabla_posiciones`**: Cálculo resumido de la fase de grupos o liga (Puntos, DG, GF, GC, PG, PE, PP) por equipo dentro de un torneo.
* **`estadisticas_jugadores`**: Consolidado individual del desempeño de cada jugador en un torneo (Tabla de goleo, asistencias, tarjetas y partidos jugados).