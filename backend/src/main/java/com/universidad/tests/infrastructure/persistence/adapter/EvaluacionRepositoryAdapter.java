package com.universidad.tests.infrastructure.persistence.adapter;

import com.universidad.tests.domain.model.EstadoSubprueba;
import com.universidad.tests.domain.model.Evaluacion;
import com.universidad.tests.domain.model.EvaluacionSubprueba;
import com.universidad.tests.domain.repository.EvaluacionRepository;
import com.universidad.tests.infrastructure.persistence.entity.EvaluacionEntity;
import com.universidad.tests.infrastructure.persistence.entity.EvaluacionSubpruebaEntity;
import com.universidad.tests.infrastructure.persistence.repository.SpringDataEvaluacionRepository;
import com.universidad.tests.infrastructure.persistence.repository.SpringDataSubpruebaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class EvaluacionRepositoryAdapter implements EvaluacionRepository {

    private final SpringDataEvaluacionRepository evaluacionRepo;
    private final SpringDataSubpruebaRepository subpruebaRepo;

    public EvaluacionRepositoryAdapter(
            SpringDataEvaluacionRepository evaluacionRepo,
            SpringDataSubpruebaRepository subpruebaRepo) {
        this.evaluacionRepo = evaluacionRepo;
        this.subpruebaRepo = subpruebaRepo;
    }

    @Override
    public Optional<Evaluacion> buscarPorId(String id) {
        return evaluacionRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Evaluacion guardar(Evaluacion evaluacion) {
        EvaluacionEntity entity = evaluacionRepo.findById(evaluacion.getId()).orElse(null);
        Instant ahora = Instant.now();
        if (entity == null) {
            entity =
                    new EvaluacionEntity(
                            evaluacion.getId(),
                            evaluacion.getSujetoId(),
                            evaluacion.getEstado(),
                            ahora,
                            ahora);
        } else {
            entity.setEstado(evaluacion.getEstado());
            entity.setActualizadoEn(ahora);
        }
        EvaluacionEntity guardada = evaluacionRepo.save(entity);
        return toDomain(guardada);
    }

    @Override
    public Optional<EvaluacionSubprueba> buscarSubprueba(
            String evaluacionId, String subpruebaCodigo) {
        return subpruebaRepo
                .findByEvaluacion_IdAndCodigoIgnoreCase(evaluacionId, subpruebaCodigo)
                .map(this::toDomainSubprueba);
    }

    @Override
    public void guardarSubprueba(EvaluacionSubprueba subprueba) {
        subpruebaRepo
                .findByEvaluacion_IdAndCodigoIgnoreCase(
                        subprueba.getEvaluacionId(), subprueba.getCodigo())
                .ifPresent(
                        entity -> {
                            entity.setEstado(subprueba.getEstado().name());
                            entity.setFechaInicio(subprueba.getFechaInicio());
                            entity.setFechaFinLimite(subprueba.getFechaFinLimite());
                            entity.setFechaCierre(subprueba.getFechaCierre());
                            subpruebaRepo.save(entity);
                        });
    }

    private Evaluacion toDomain(EvaluacionEntity entity) {
        List<EvaluacionSubprueba> subpruebas =
                entity.getSubpruebas().stream().map(this::toDomainSubprueba).toList();

        return new Evaluacion(entity.getId(), entity.getSujetoId(), entity.getEstado(), subpruebas);
    }

    private EvaluacionSubprueba toDomainSubprueba(EvaluacionSubpruebaEntity entity) {
        return new EvaluacionSubprueba(
                entity.getId(),
                entity.getEvaluacion().getId(),
                entity.getCodigo(),
                entity.getNombre(),
                entity.getOrden(),
                entity.getDuracionSegundos(),
                EstadoSubprueba.valueOf(entity.getEstado()),
                entity.getInstrucciones(),
                entity.isPermitirRetroceso(),
                entity.getFechaInicio(),
                entity.getFechaFinLimite(),
                entity.getFechaCierre());
    }
}
