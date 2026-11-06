package com.universidad.tests.mapper;

import com.universidad.tests.domain.model.Evaluacion;
import com.universidad.tests.domain.model.EvaluacionSubprueba;
import com.universidad.tests.domain.model.Opcion;
import com.universidad.tests.domain.model.Pregunta;
import com.universidad.tests.domain.model.RespuestaEvaluacion;
import com.universidad.tests.dto.EvaluacionResponseDto;
import com.universidad.tests.dto.OpcionResponseDto;
import com.universidad.tests.dto.PreguntaResponseDto;
import com.universidad.tests.dto.PreguntasSubpruebaResponseDto;
import com.universidad.tests.dto.SubpruebaResponseDto;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MotorSubpruebasMapper {

    public EvaluacionResponseDto toEvaluacionDto(Evaluacion evaluacion, Instant ahora) {
        List<SubpruebaResponseDto> subpruebas =
                evaluacion.getSubpruebas().stream().map(s -> toSubpruebaDto(s, ahora)).toList();

        return new EvaluacionResponseDto(
                evaluacion.getId(), evaluacion.getSujetoId(), evaluacion.getEstado(), subpruebas);
    }

    public SubpruebaResponseDto toSubpruebaDto(EvaluacionSubprueba subprueba, Instant ahora) {
        Long segundosRestantes = subprueba.calcularSegundosRestantes(ahora);

        return new SubpruebaResponseDto(
                subprueba.getCodigo(),
                subprueba.getNombre(),
                subprueba.getOrden(),
                subprueba.getDuracionSegundos(),
                subprueba.getEstado().name(),
                segundosRestantes,
                subprueba.getFechaInicio(),
                subprueba.getFechaFinLimite(),
                subprueba.getInstrucciones(),
                subprueba.isPermitirRetroceso());
    }

    public PreguntasSubpruebaResponseDto toPreguntasSubpruebaDto(
            EvaluacionSubprueba subprueba,
            List<Pregunta> preguntas,
            List<RespuestaEvaluacion> respuestas,
            Instant ahora) {

        Map<String, String> opcionIdALetra =
                preguntas.stream()
                        .flatMap(p -> p.getOpciones().stream())
                        .collect(Collectors.toMap(Opcion::getId, Opcion::getEtiqueta, (a, b) -> a));

        Map<String, String> respuestasMap =
                respuestas.stream()
                        .collect(
                                Collectors.toMap(
                                        RespuestaEvaluacion::getPreguntaId,
                                        r -> {
                                            if (r.getOpcionId() != null) {
                                                String letra = opcionIdALetra.get(r.getOpcionId());
                                                return letra != null ? letra : r.getOpcionId();
                                            } else if (r.getValorTexto() != null) {
                                                return r.getValorTexto();
                                            } else {
                                                return "";
                                            }
                                        },
                                        (existente, reemplazo) -> reemplazo));

        List<PreguntaResponseDto> preguntasDto =
                preguntas.stream()
                        .map(
                                p ->
                                        new PreguntaResponseDto(
                                                p.getId(),
                                                p.getNumero(),
                                                p.getEnunciado(),
                                                p.getTipoRespuesta().name(),
                                                p.getRecursoMultimedia(),
                                                toOpcionesDto(p.getOpciones()),
                                                respuestasMap.get(p.getId())))
                        .toList();

        return new PreguntasSubpruebaResponseDto(
                subprueba.getCodigo(),
                subprueba.calcularSegundosRestantes(ahora),
                subprueba.isPermitirRetroceso(),
                preguntasDto);
    }

    public List<OpcionResponseDto> toOpcionesDto(List<Opcion> opciones) {
        return opciones.stream()
                .map(
                        o ->
                                new OpcionResponseDto(
                                        o.getEtiqueta(), o.getTexto(), o.getId(), o.getEtiqueta()))
                .toList();
    }
}
