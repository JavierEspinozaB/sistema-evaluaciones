package com.universidad.tests.infrastructure.persistence.repository;

import com.universidad.tests.infrastructure.persistence.entity.PreguntaCatalogoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataPreguntaRepository
        extends JpaRepository<PreguntaCatalogoEntity, String> {
    List<PreguntaCatalogoEntity> findBySubpruebaCodigoIgnoreCaseOrderByOrdenAsc(
            String subpruebaCodigo);
}
