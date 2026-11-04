
package com.universidad.tests.infrastructure.persistence.repository;

import com.universidad.tests.infrastructure.persistence.entity.VersionFormularioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VersionFormularioJpaRepository
    extends JpaRepository<VersionFormularioJpaEntity, Long> {

    List<VersionFormularioJpaEntity>
    findByTest_IdTestOrderByCodigoVersionAsc(Long idTest);

    Optional<VersionFormularioJpaEntity>
    findByTest_IdTestAndCodigoVersion(
        Long idTest,
        String codigoVersion
    );

    boolean existsByTest_IdTestAndCodigoVersion(
        Long idTest,
        String codigoVersion
    );
}
