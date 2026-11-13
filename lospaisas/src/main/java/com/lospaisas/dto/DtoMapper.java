package com.lospaisas.dto;

import com.lospaisas.dto.response.*;
import com.lospaisas.models.entity.*;

/**
 * Clase de utilidad para mapear entidades de dominio a DTOs de respuesta
 * (Response).
 * 
 * Contiene métodos estáticos responsables de transformar las entidades
 * en sus respectivos objetos de transferencia de datos, garantizando
 * protecciones contra NullPointerException (NPE) al acceder a atributos
 * o relaciones opcionales y cargadas perezosamente (lazy loading).
 * 
 * NOTA: Esta clase no debe ser instanciada.
 */
public final class DtoMapper {

    private DtoMapper() {
    }

    public static TorneoResponse toResponse(Torneo t) {
        return new TorneoResponse(t.getId(), t.getNombre(), t.getDescripcion(),
                t.getFechaInicio(), t.getFechaFin(), t.getEstado(), t.getFormato());
    }

    public static EquipoResponse toResponse(Equipo e) {
        Torneo t = e.getTorneo();
        return new EquipoResponse(e.getId(), e.getNombre(), e.getEntrenador(),
                t != null ? t.getId() : null, t != null ? t.getNombre() : null);
    }

    public static JugadorResponse toResponse(Jugador j) {
        Equipo e = j.getEquipo();

        return new JugadorResponse(
                j.getId(),
                j.getNombre(),
                j.getApellido(),
                e != null ? e.getId() : null,
                e != null ? e.getNombre() : null,
                j.getPosicion());
    }

    public static UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getEmail(),
                u.getRol() != null ? u.getRol().getNombre() : null);
    }

    public static RolResponse toResponse(Rol r) {
        return new RolResponse(r.getId(), r.getNombre());
    }

    public static ArbitroResponse toResponse(Arbitro a) {
        return new ArbitroResponse(a.getId(), a.getNombre(), a.getApellido(),
                a.getExperiencia(), a.getLicencia());
    }

    public static JornadaResponse toResponse(Jornada j) {
        return new JornadaResponse(j.getId(),
                j.getTorneo() != null ? j.getTorneo().getId() : null, j.getNumeroJornada());
    }

    public static PartidoResponse toResponse(Partido p) {
        Equipo l = p.getEquipoLocal();
        Equipo v = p.getEquipoVisitante();
        Arbitro a = p.getArbitro();
        return new PartidoResponse(p.getId(),
                p.getJornada() != null ? p.getJornada().getId() : null,
                l != null ? l.getId() : null, l != null ? l.getNombre() : null,
                v != null ? v.getId() : null, v != null ? v.getNombre() : null,
                a != null ? a.getId() : null,
                a != null ? a.getNombre() + " " + a.getApellido() : null,
                p.getGolesLocal(), p.getGolesVisitante(), p.getEstado(), p.getFecha());
    }

    public static EventoPartidoResponse toResponse(EventoPartido ev) {
        Jugador j = ev.getJugador();
        return new EventoPartidoResponse(ev.getId(),
                ev.getPartido() != null ? ev.getPartido().getId() : null,
                j != null ? j.getId() : null,
                j != null ? j.getNombre() + " " + j.getApellido() : null, // Fix: Usa el nombre y apellido directos del
                                                                          // jugador
                ev.getTipoEvento(), ev.getMinuto(), ev.getDescripcion());
    }

    public static EstadisticaJugadorResponse toResponse(EstadisticaJugador s) {
        Jugador j = s.getJugador();
        Equipo e = s.getEquipo();
        return new EstadisticaJugadorResponse(s.getId(),
                j != null ? j.getId() : null,
                j != null ? j.getNombre() + " " + j.getApellido() : null, // Fix: Usa el nombre y apellido directos del
                                                                          // jugador
                e != null ? e.getId() : null, e != null ? e.getNombre() : null,
                s.getGoles(), s.getAsistencias(), s.getTarjetasAmarillas(),
                s.getTarjetasRojas(), s.getPartidosJugados());
    }

    public static TablaPosicionResponse toResponse(TablaPosicion tp) {
        Equipo e = tp.getEquipo();
        return new TablaPosicionResponse(
                e != null ? e.getId() : null, e != null ? e.getNombre() : null,
                tp.getPuntos(), tp.getPartidosJugados(),
                tp.getPartidosGanados(), tp.getPartidosEmpatados(), tp.getPartidosPerdidos(),
                tp.getGolesFavor(), tp.getGolesContra(), tp.getDiferenciaGoles());
    }
}
