package com.universidad.tests.infrastructure.persistence.repository;

import com.universidad.tests.infrastructure.persistence.entity.EvaluacionSubpruebaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataSubpruebaRepository
        extends JpaRepository<EvaluacionSubpruebaEntity, Long> {
    Optional<EvaluacionSubpruebaEntity> findByEvaluacion_IdAndCodigoIgnoreCase(
            String evaluacionId, String codigo);
}
