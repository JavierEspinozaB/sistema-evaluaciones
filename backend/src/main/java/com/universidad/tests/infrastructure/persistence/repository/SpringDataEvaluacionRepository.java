package com.universidad.tests.infrastructure.persistence.repository;

import com.universidad.tests.infrastructure.persistence.entity.EvaluacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataEvaluacionRepository extends JpaRepository<EvaluacionEntity, String> {
    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query(
            "select e from EvaluacionEntity e where e.id = :id")
    java.util.Optional<EvaluacionEntity> buscarBloqueada(
            @org.springframework.data.repository.query.Param("id") String id);
}
