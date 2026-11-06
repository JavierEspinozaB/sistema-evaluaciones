package com.universidad.tests.infrastructure.persistence.repository;

import com.universidad.tests.infrastructure.persistence.entity.RespuestaEvaluacionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataRespuestaRepository
        extends JpaRepository<RespuestaEvaluacionEntity, Long> {

    List<RespuestaEvaluacionEntity> findByEvaluacionIdAndSubpruebaCodigoIgnoreCase(
            String evaluacionId, String subpruebaCodigo);

    Optional<RespuestaEvaluacionEntity>
            findByEvaluacionIdAndSubpruebaCodigoIgnoreCaseAndPreguntaIdAndOrdenEntrada(
                    String evaluacionId,
                    String subpruebaCodigo,
                    String preguntaId,
                    int ordenEntrada);

    long countByEvaluacionIdAndSubpruebaCodigoIgnoreCase(
            String evaluacionId, String subpruebaCodigo);
}

