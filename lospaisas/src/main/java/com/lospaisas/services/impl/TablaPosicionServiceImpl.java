package com.lospaisas.services.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lospaisas.dto.DtoMapper;
import com.lospaisas.dto.response.TablaPosicionResponse;
import com.lospaisas.models.entity.Equipo;
import com.lospaisas.models.entity.Partido;
import com.lospaisas.models.entity.TablaPosicion;
import com.lospaisas.models.entity.Torneo;
import com.lospaisas.models.enums.EstadoPartido;
import com.lospaisas.repositories.EquipoRepository;
import com.lospaisas.repositories.TablaPosicionRepository;
import com.lospaisas.services.TablaPosicionService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TablaPosicionServiceImpl implements TablaPosicionService {

    private final TablaPosicionRepository tablaPosicionRepository;
    private final EquipoRepository equipoRepository;

    public TablaPosicionServiceImpl(TablaPosicionRepository tablaPosicionRepository,
            EquipoRepository equipoRepository) {
        this.tablaPosicionRepository = tablaPosicionRepository;
        this.equipoRepository = equipoRepository;
    }

    /**
     * Recalcula y actualiza la posición de los equipos local y visitante tras la
     * finalización de un partido.
     */
    @Override
    public void actualizarTabla(Partido partido) {
        if (partido.getEstado() != EstadoPartido.FINALIZADO) {
            return;
        }

        Equipo equipoLocal = partido.getEquipoLocal();
        Equipo equipoVisitante = partido.getEquipoVisitante();
        Torneo torneo = partido.getJornada().getTorneo();

        TablaPosicion posicionLocal = obtenerOCrearPosicion(torneo, equipoLocal);
        TablaPosicion posicionVisitante = obtenerOCrearPosicion(torneo, equipoVisitante);

        int golesLocal = partido.getGolesLocal();
        int golesVisitante = partido.getGolesVisitante();

        // Partidos jugados
        posicionLocal.setPartidosJugados(posicionLocal.getPartidosJugados() + 1);
        posicionVisitante.setPartidosJugados(posicionVisitante.getPartidosJugados() + 1);

        // Goles
        posicionLocal.setGolesFavor(posicionLocal.getGolesFavor() + golesLocal);
        posicionVisitante.setGolesFavor(posicionVisitante.getGolesFavor() + golesVisitante);
        posicionLocal.setGolesContra(posicionLocal.getGolesContra() + golesVisitante);
        posicionVisitante.setGolesContra(posicionVisitante.getGolesContra() + golesLocal);
        ;

        // Resultado del encuentro
        if (golesLocal > golesVisitante) {
            posicionLocal.setPartidosGanados(posicionLocal.getPartidosGanados() + 1);
            posicionLocal.setPuntos(posicionLocal.getPuntos() + 3);
            posicionVisitante.setPartidosPerdidos(posicionVisitante.getPartidosPerdidos() + 1);
        } else if (golesVisitante > golesLocal) {
            posicionVisitante.setPartidosGanados(posicionVisitante.getPartidosGanados() + 1);
            posicionVisitante.setPuntos(posicionVisitante.getPuntos() + 3);
            posicionLocal.setPartidosPerdidos(posicionLocal.getPartidosPerdidos() + 1);
        } else {
            posicionLocal.setPartidosEmpatados(posicionLocal.getPartidosEmpatados() + 1);
            posicionVisitante.setPartidosEmpatados(posicionVisitante.getPartidosEmpatados() + 1);
            posicionLocal.setPuntos(posicionLocal.getPuntos() + 1);
            posicionVisitante.setPuntos(posicionVisitante.getPuntos() + 1);
        }

        // Diferencia de goles
        posicionLocal.setDiferenciaGoles(posicionLocal.getGolesFavor() - posicionLocal.getGolesContra());
        posicionVisitante.setDiferenciaGoles(posicionVisitante.getGolesFavor() - posicionVisitante.getGolesContra());

        tablaPosicionRepository.save(posicionLocal);
        tablaPosicionRepository.save(posicionVisitante);
    }

    /**
     * Obtiene la tabla de posiciones de un torneo ordenada jerárquicamente por:
     * 1. Puntos acumulados (Descendente)
     * 2. Diferencia de goles (Descendente)
     * 3. Goles a favor (Descendente)
     */
    @Override
    public List<TablaPosicionResponse> obtenerTabla(UUID torneoId) {
        List<TablaPosicion> tabla = tablaPosicionRepository.findByTorneoId(torneoId);

        tabla.sort(
                Comparator.comparing(TablaPosicion::getPuntos, Comparator.reverseOrder())
                        .thenComparing(TablaPosicion::getDiferenciaGoles, Comparator.reverseOrder())
                        .thenComparing(TablaPosicion::getGolesFavor, Comparator.reverseOrder()));

        return tabla.stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    /**
     * Busca la posición de un equipo en un torneo específico. Si no existe, genera
     * la instancia inicial.
     */
    private TablaPosicion obtenerOCrearPosicion(Torneo torneo, Equipo equipo) {
        return tablaPosicionRepository.findByTorneoId(torneo.getId())
                .stream()
                .filter(pos -> pos.getEquipo().getId().equals(equipo.getId()))
                .findFirst()
                .orElseGet(() -> crearPosicionInicial(torneo, equipo));
    }

    private TablaPosicion crearPosicionInicial(Torneo torneo, Equipo equipo) {
        TablaPosicion nuevaPosicion = new TablaPosicion();
        nuevaPosicion.setTorneo(torneo);
        nuevaPosicion.setEquipo(equipo);
        nuevaPosicion.setPuntos(0);
        nuevaPosicion.setPartidosJugados(0);
        nuevaPosicion.setPartidosGanados(0);
        nuevaPosicion.setPartidosEmpatados(0);
        nuevaPosicion.setPartidosPerdidos(0);
        nuevaPosicion.setGolesFavor(0);
        nuevaPosicion.setGolesContra(0);
        nuevaPosicion.setDiferenciaGoles(0);

        return tablaPosicionRepository.save(nuevaPosicion);
    }

    @Override
    public void inicializarPosicion(Torneo torneo, Equipo equipo) {
        obtenerOCrearPosicion(torneo, equipo);
    }

    @Override
    public void eliminarPosicion(Equipo equipo) {
        tablaPosicionRepository.findByTorneoId(equipo.getTorneo().getId())
                .stream()
                .filter(pos -> pos.getEquipo().getId().equals(equipo.getId()))
                .forEach(tablaPosicionRepository::delete);
    }

    @Override
    public List<TablaPosicionResponse> obtenerTablaCompleta(UUID torneoId, UUID equipoId) {
        // 1. Obtener la tabla calculada desde BD
        List<TablaPosicionResponse> tablaCalculada = obtenerTabla(torneoId);

        // 2. Obtener los IDs de equipos que ya están en la tabla
        List<UUID> equiposConPosicion = tablaCalculada.stream()
                .map(TablaPosicionResponse::equipoId)
                .toList();

        List<TablaPosicionResponse> tablaCompleta = new ArrayList<>(tablaCalculada);

        // 3. Incluir equipos del torneo que no tengan registros aún
        equipoRepository.findByTorneoId(torneoId).forEach(e -> {
            if (!equiposConPosicion.contains(e.getId())) {
                tablaCompleta.add(new TablaPosicionResponse(
                        e.getId(), e.getNombre(), 0, 0, 0, 0, 0, 0, 0, 0));
            }
        });

        // 4. Filtrar por equipoId si viene especificado
        if (equipoId != null) {
            return tablaCompleta.stream()
                    .filter(p -> equipoId.equals(p.equipoId()))
                    .toList();
        }

        return tablaCompleta;
    }
}
