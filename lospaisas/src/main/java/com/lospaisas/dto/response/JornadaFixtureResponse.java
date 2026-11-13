package com.lospaisas.dto.response;

import java.util.List;

public record JornadaFixtureResponse(
        JornadaResponse jornada,
        List<PartidoResponse> partidos) {
}