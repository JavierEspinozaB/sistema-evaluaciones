package com.universidad.tests.infrastructure.persistence.repository;

import com.universidad.tests.domain.model.EstadoEvaluacion;
import com.universidad.tests.infrastructure.persistence.entity.EvaluacionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluacionJpaRepository extends JpaRepository<EvaluacionJpaEntity, Long> {

    boolean existsByIdSujetoAndIdTestAndIdPeriodoAndEstado(
            Long idSujeto, Long idTest, Long idPeriodo, EstadoEvaluacion estado);
}
