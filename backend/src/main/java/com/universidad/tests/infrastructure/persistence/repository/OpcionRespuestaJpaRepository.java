
package com.universidad.tests.infrastructure.persistence.repository;

import com.universidad.tests.infrastructure.persistence.entity.OpcionRespuestaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OpcionRespuestaJpaRepository
    extends JpaRepository<OpcionRespuestaJpaEntity, Long> {

    List<OpcionRespuestaJpaEntity>
    findByPregunta_IdPreguntaOrderByOrdenPresentacionAsc(
        Long idPregunta
    );

    Optional<OpcionRespuestaJpaEntity>
    findByPregunta_IdPreguntaAndCodigo(
        Long idPregunta,
        String codigo
    );

    boolean existsByPregunta_IdPreguntaAndCodigo(
        Long idPregunta,
        String codigo
    );

}

