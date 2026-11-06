package com.universidad.tests.infrastructure.persistence.adapter;

import com.universidad.tests.domain.model.RespuestaEvaluacion;
import com.universidad.tests.domain.repository.RespuestaRepository;
import com.universidad.tests.infrastructure.persistence.entity.RespuestaEvaluacionEntity;
import com.universidad.tests.infrastructure.persistence.repository.SpringDataRespuestaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class RespuestaRepositoryAdapter implements RespuestaRepository {

    private final SpringDataRespuestaRepository respuestaRepo;

    public RespuestaRepositoryAdapter(SpringDataRespuestaRepository respuestaRepo) {
        this.respuestaRepo = respuestaRepo;
    }

    @Override
    public List<RespuestaEvaluacion> listarPorEvaluacionYSubprueba(
            String evaluacionId, String subpruebaCodigo) {
        return respuestaRepo
                .findByEvaluacionIdAndSubpruebaCodigoIgnoreCase(evaluacionId, subpruebaCodigo)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<RespuestaEvaluacion> buscarRespuesta(
            String evaluacionId, String subpruebaCodigo, String preguntaId, int ordenEntrada) {
        return respuestaRepo
                .findByEvaluacionIdAndSubpruebaCodigoIgnoreCaseAndPreguntaIdAndOrdenEntrada(
                        evaluacionId, subpruebaCodigo, preguntaId, ordenEntrada)
                .map(this::toDomain);
    }

    @Override
    public RespuestaEvaluacion guardar(RespuestaEvaluacion respuesta) {
        try {
            RespuestaEvaluacionEntity entity;
            if (respuesta.getId() != null) {
                entity =
                        respuestaRepo
                                .findById(respuesta.getId())
                                .orElse(new RespuestaEvaluacionEntity());
            } else {
                Optional<RespuestaEvaluacionEntity> existente =
                        respuestaRepo
                                .findByEvaluacionIdAndSubpruebaCodigoIgnoreCaseAndPreguntaIdAndOrdenEntrada(
                                        respuesta.getEvaluacionId(),
                                        respuesta.getSubpruebaCodigo(),
                                        respuesta.getPreguntaId(),
                                        respuesta.getOrdenEntrada());
                entity = existente.orElseGet(RespuestaEvaluacionEntity::new);
            }

            entity.setEvaluacionId(respuesta.getEvaluacionId());
            entity.setSubpruebaCodigo(respuesta.getSubpruebaCodigo());
            entity.setPreguntaId(respuesta.getPreguntaId());
            entity.setOpcionId(respuesta.getOpcionId());
            entity.setValorTexto(respuesta.getValorTexto());
            entity.setOrdenEntrada(respuesta.getOrdenEntrada());
            entity.setFechaRegistro(respuesta.getFechaRegistro());

            RespuestaEvaluacionEntity guardada = respuestaRepo.saveAndFlush(entity);
            return toDomain(guardada);
        } catch (DataIntegrityViolationException e) {
            return respuestaRepo
                    .findByEvaluacionIdAndSubpruebaCodigoIgnoreCaseAndPreguntaIdAndOrdenEntrada(
                            respuesta.getEvaluacionId(),
                            respuesta.getSubpruebaCodigo(),
                            respuesta.getPreguntaId(),
                            respuesta.getOrdenEntrada())
                    .map(
                            existente -> {
                                existente.setOpcionId(respuesta.getOpcionId());
                                existente.setValorTexto(respuesta.getValorTexto());
                                existente.setFechaRegistro(respuesta.getFechaRegistro());
                                return toDomain(respuestaRepo.saveAndFlush(existente));
                            })
                    .orElse(respuesta);
        }
    }

    @Override
    public long contarPorEvaluacionYSubprueba(String evaluacionId, String subpruebaCodigo) {
        return respuestaRepo.countByEvaluacionIdAndSubpruebaCodigoIgnoreCase(
                evaluacionId, subpruebaCodigo);
    }

    private RespuestaEvaluacion toDomain(RespuestaEvaluacionEntity entity) {
        return new RespuestaEvaluacion(
                entity.getId(),
                entity.getEvaluacionId(),
                entity.getSubpruebaCodigo(),
                entity.getPreguntaId(),
                entity.getOpcionId(),
                entity.getValorTexto(),
                entity.getOrdenEntrada(),
                entity.getFechaRegistro());
    }
}
