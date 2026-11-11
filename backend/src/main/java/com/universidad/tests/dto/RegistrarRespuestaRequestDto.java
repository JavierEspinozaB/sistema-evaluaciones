package com.universidad.tests.dto;

import java.util.List;

public record RegistrarRespuestaRequestDto(
        List<ItemRespuestaDto> respuestas,
        String preguntaId,
        String opcion,
        String opcionId,
        String valorTexto,
        Integer ordenEntrada,
        List<PalabraFvDto> palabras) {

    public RegistrarRespuestaRequestDto(
            String preguntaId, String opcion, String valorTexto, Integer ordenEntrada) {
        this(null, preguntaId, opcion, opcion, valorTexto, ordenEntrada, null);
    }

    public RegistrarRespuestaRequestDto(List<ItemRespuestaDto> respuestas) {
        this(respuestas, null, null, null, null, null, null);
    }

    public List<ItemRespuestaDto> obtenerListaRespuestas() {
        if (respuestas != null && !respuestas.isEmpty()) {
            return respuestas;
        }
        if (preguntaId != null
                || opcion != null
                || opcionId != null
                || valorTexto != null
                || ordenEntrada != null) {
            String opt = opcion != null ? opcion : opcionId;
            return List.of(new ItemRespuestaDto(preguntaId, opt, opt, valorTexto, ordenEntrada));
        }
        return List.of();
    }
}
