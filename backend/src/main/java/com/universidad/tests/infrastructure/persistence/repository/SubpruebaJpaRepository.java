
package com.universidad.tests.infrastructure.persistence.repository;

import com.universidad.tests.infrastructure.persistence.entity.SubpruebaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubpruebaJpaRepository
    extends JpaRepository<SubpruebaJpaEntity, Long> {

    List<SubpruebaJpaEntity> findByTest_IdTestOrderByCodigoAsc(
        Long idTest
    );

    Optional<SubpruebaJpaEntity> findByTest_IdTestAndCodigo(
        Long idTest,
        String codigo
    );

    boolean existsByTest_IdTestAndCodigo(
        Long idTest,
        String codigo
    );
}
