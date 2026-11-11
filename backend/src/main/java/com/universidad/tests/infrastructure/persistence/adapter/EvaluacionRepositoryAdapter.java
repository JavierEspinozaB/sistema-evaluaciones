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

    private final org.springframework.jdbc.core.JdbcTemplate jdbc;
    private final SpringDataEvaluacionRepository evaluacionRepo;
    private final SpringDataSubpruebaRepository subpruebaRepo;

    public EvaluacionRepositoryAdapter(
            SpringDataEvaluacionRepository evaluacionRepo,
            SpringDataSubpruebaRepository subpruebaRepo,
            org.springframework.jdbc.core.JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.evaluacionRepo = evaluacionRepo;
        this.subpruebaRepo = subpruebaRepo;
    }

    @Override
    public List<String> listarConVencidas(Instant ahora, int limite) {
        return jdbc.queryForList(
                "SELECT DISTINCT evaluacion_id FROM evaluacion_subprueba WHERE estado='ACTIVA' AND fecha_fin_limite <= ? ORDER BY evaluacion_id LIMIT ?",
                String.class,
                java.sql.Timestamp.from(ahora),
                limite);
    }

    @Override
    public Optional<Evaluacion> buscarPorId(String id) {
        return evaluacionRepo.buscarBloqueada(id).map(this::toDomain);
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
        entity.setTestId(evaluacion.getTestId());
        entity.setPeriodoId(evaluacion.getPeriodoId());
        entity.setVersionFormularioId(evaluacion.getVersionFormularioId());
        entity.setFechaInicio(evaluacion.getFechaInicio());
        entity.setFechaFin(evaluacion.getFechaFin());
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
                            entity.setLetraInicial(subprueba.getLetraInicial());
                            entity.setFechaInicio(subprueba.getFechaInicio());
                            entity.setFechaFinLimite(subprueba.getFechaFinLimite());
                            entity.setFechaCierre(subprueba.getFechaCierre());
                            subpruebaRepo.saveAndFlush(entity);
                        });
    }

    private Evaluacion toDomain(EvaluacionEntity entity) {
        List<EvaluacionSubprueba> subpruebas =
                entity.getSubpruebas().stream().map(this::toDomainSubprueba).toList();

        Evaluacion evaluacion =
                new Evaluacion(
                        entity.getId(), entity.getSujetoId(), entity.getEstado(), subpruebas);
        evaluacion.setTestId(entity.getTestId());
        evaluacion.setPeriodoId(entity.getPeriodoId());
        evaluacion.setVersionFormularioId(entity.getVersionFormularioId());
        evaluacion.setFechaInicio(entity.getFechaInicio());
        evaluacion.setFechaFin(entity.getFechaFin());
        return evaluacion;
    }

    private EvaluacionSubprueba toDomainSubprueba(EvaluacionSubpruebaEntity entity) {
        return jdbc.queryForObject(
                "SELECT c.orden_aplicacion,c.tiempo_limite_segundos,c.instrucciones_ref,c.maximo_entradas,s.codigo,s.nombre FROM configuracion_subprueba c JOIN subprueba s ON s.id_subprueba=c.id_subprueba JOIN version_formulario v ON v.id_version_formulario=c.id_version_formulario WHERE c.id_configuracion_subprueba=? AND v.id_version_formulario::text=? AND v.id_test::text=? AND v.estado IN ('PUBLICADA','RETIRADA')",
                (rs, n) -> {
                    if (!rs.getString("codigo").equalsIgnoreCase(entity.getCodigo())
                            || rs.getObject("tiempo_limite_segundos") == null)
                        throw new IllegalStateException(
                                "Configuración de aplicación incompatible con el catálogo.");
                    EvaluacionSubprueba configurada =
                            new EvaluacionSubprueba(
                                    entity.getId(),
                                    entity.getEvaluacion().getId(),
                                    rs.getString("codigo"),
                                    rs.getString("nombre"),
                                    rs.getInt("orden_aplicacion"),
                                    rs.getInt("tiempo_limite_segundos"),
                                    EstadoSubprueba.valueOf(entity.getEstado()),
                                    rs.getString("instrucciones_ref"),
                                    entity.isPermitirRetroceso(),
                                    entity.getFechaInicio(),
                                    entity.getFechaFinLimite(),
                                    entity.getFechaCierre());
                    configurada.setConfiguracionId(entity.getConfiguracionId());
                    configurada.setLetraInicial(entity.getLetraInicial());
                    configurada.setMaximoEntradas(
                            rs.getObject("maximo_entradas") == null
                                    ? 75
                                    : Math.min(75, rs.getInt("maximo_entradas")));
                    return configurada;
                },
                entity.getConfiguracionId(),
                entity.getEvaluacion().getVersionFormularioId(),
                entity.getEvaluacion().getTestId());
    }
}
