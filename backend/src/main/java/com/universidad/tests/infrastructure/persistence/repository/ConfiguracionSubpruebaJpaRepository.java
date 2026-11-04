
package com.universidad.tests.infrastructure.persistence.repository;

import com.universidad.tests.infrastructure.persistence.entity.ConfiguracionSubpruebaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConfiguracionSubpruebaJpaRepository
    extends JpaRepository<ConfiguracionSubpruebaJpaEntity, Long> {

    List<ConfiguracionSubpruebaJpaEntity>
    findByVersionFormulario_IdVersionFormularioOrderByOrdenAplicacionAsc(
        Long idVersionFormulario
    );

    Optional<ConfiguracionSubpruebaJpaEntity>
    findByVersionFormulario_IdVersionFormularioAndSubprueba_IdSubprueba(
        Long idVersionFormulario,
        Long idSubprueba
    );

    boolean existsByVersionFormulario_IdVersionFormularioAndSubprueba_IdSubprueba(
        Long idVersionFormulario,
        Long idSubprueba
    );
}
