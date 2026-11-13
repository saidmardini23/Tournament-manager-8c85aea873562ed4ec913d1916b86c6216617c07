package com.lospaisas.services;

import java.util.List;
import java.util.UUID;

import com.lospaisas.dto.request.ArbitroRequest;
import com.lospaisas.dto.response.ArbitroResponse;

public interface ArbitroService {

    ArbitroResponse crearArbitro(ArbitroRequest request);

    List<ArbitroResponse> listarArbitros();

    List<ArbitroResponse> listarArbitros(String buscar);

    ArbitroResponse buscarPorId(UUID id);

    ArbitroResponse actualizarArbitro(UUID id, ArbitroRequest request);

    void eliminarArbitro(UUID id);

}
